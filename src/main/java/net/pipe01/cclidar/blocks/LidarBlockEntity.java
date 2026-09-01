package net.pipe01.cclidar.blocks;

import dan200.computercraft.api.detail.BlockReference;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.pipe01.cclidar.CCLIDAR;
import net.pipe01.cclidar.Config;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class LidarBlockEntity extends BlockEntity {
    public LidarBlockEntity(BlockPos pos, BlockState blockState) {
        super(CCLIDAR.LIDAR_BLOCK_ENTITY.get(), pos, blockState);
    }

    private boolean ignoreFluids = true;
    private float rotationPeriod = 0; // ticks per horizontal sweep
    private float horizontalFov = 90; // degrees
    private float verticalFov = (float) Math.PI / 2; // radians
    private float range = 10;
    private boolean backAndForth = true;
    private boolean showLaser = false;

    public static class Hit extends HashMap<String, Object> {
        public void setDetails(Map<String, Object> details) {
            put("details", details);
        }

        public void setDistance(double distance) {
            put("distance", distance);
        }

        public void setVerticalAngle(double angle) {
            put("verticalAngle", angle);
        }
    }

    private Hit hitTest(Level level, Vector3d center, float horAngle, float vertAngle, int detailLevel) {
        FrontAndTop orientation = getBlockState().getValue(LidarBlock.ORIENTATION);

        Vec3 start = worldPosition.getCenter();
        Vec3 forward = Vec3.atLowerCornerOf(orientation.front().getNormal());

        forward = switch (orientation.front()) {
            case NORTH -> forward.yRot(-horAngle).xRot(vertAngle);
            case SOUTH -> forward.yRot(-horAngle).xRot(-vertAngle);
            case WEST -> forward.yRot(-horAngle).zRot(-vertAngle);
            case EAST -> forward.yRot(-horAngle).zRot(vertAngle);
            case UP -> switch (orientation.top()) {
                case NORTH -> forward.zRot(horAngle).xRot(vertAngle);
                case SOUTH -> forward.zRot(-horAngle).xRot(-vertAngle);
                case WEST -> forward.xRot(horAngle).zRot(-vertAngle);
                case EAST -> forward.xRot(-horAngle).zRot(vertAngle);
                default -> Vec3.ZERO;
            };
            case DOWN -> switch (orientation.top()) {
                case NORTH -> forward.zRot(-horAngle).xRot(vertAngle);
                case SOUTH -> forward.zRot(horAngle).xRot(-vertAngle);
                case WEST -> forward.xRot(-horAngle).zRot(-vertAngle);
                case EAST -> forward.xRot(horAngle).zRot(vertAngle);
                default -> Vec3.ZERO;
            };
        };

        var hit = level.clip(new ClipContext(
                start,
                start.add(forward.scale(range)),
                ClipContext.Block.VISUAL,
                ignoreFluids ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY,
                CollisionContext.empty()
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            Vector3d hitPos = SableCompanion.INSTANCE.projectOutOfSubLevel(level, JOMLConversion.toJOML(hit.getLocation()));
            double dist = center.distance(hitPos);

            Hit hitResult = new Hit();
            hitResult.setDistance(dist);
            hitResult.setVerticalAngle(Math.toDegrees(vertAngle));

            if (detailLevel == 1) {
                hitResult.setDetails(VanillaDetailRegistries.BLOCK_IN_WORLD.getBasicDetails(new BlockReference(level, hit.getBlockPos())));
            } else if (detailLevel == 2) {
                hitResult.setDetails(VanillaDetailRegistries.BLOCK_IN_WORLD.getDetails(new BlockReference(level, hit.getBlockPos())));
            }

            return hitResult;
        }
        return null;
    }

    public Hit[] getHits(int steps, int detailLevel) {
        Level level = getLevel();

        if (level != null) {
            level.getProfiler().push("lidar hit test column");

            range = Math.clamp(range, 1, Config.MAX_LIDAR_RANGE.getAsInt());
            steps = Math.clamp(steps, 1, Config.MAX_VERTICAL_RESOLUTION.getAsInt());

            float horAngle = (float) Math.toRadians(getCurrentAngle());
            float stepAngle = verticalFov / (steps - 1);
            Vector3d center = SableCompanion.INSTANCE.projectOutOfSubLevel(level, JOMLConversion.atCenterOf(getBlockPos()));

            Hit[] hits = new Hit[steps];
            for (int i = 0; i < steps; i++) {
                hits[i] = hitTest(level, center, horAngle, steps == 1 ? 0 : (stepAngle * i - verticalFov / 2), detailLevel);
            }

            level.getProfiler().pop();
            return hits;
        }

        return null;
    }

    public float getCurrentAngle() {
        return getCurrentAngle(0);
    }

    public float getCurrentAngle(float partialTick) {
        if (rotationPeriod >= 2 && getLevel() != null) {
            double t = getLevel().getGameTime() + (double)partialTick;

            if (backAndForth) {
                float s = rotationPeriod - 1;
                double x = 2 * Math.abs(t / (s * 2) - Math.floor(t / (s * 2) + 0.5)) * horizontalFov;
                return (float)x - horizontalFov / 2;
            }

            double prog = (t % rotationPeriod) / (rotationPeriod - 1);
            return (float)prog * horizontalFov - horizontalFov / 2;
        }
        return 0;
    }

    public void setRotationPeriod(float rotationPeriod) {
        this.rotationPeriod = rotationPeriod < 2 ? 0 : rotationPeriod;
        this.updated();
    }

    public void setHorizontalFov(float horizontalFov) {
        this.horizontalFov = Math.clamp(horizontalFov, 0, 180);
        this.updated();
    }

    public float getVerticalFov() {
        return verticalFov;
    }

    public void setVerticalFov(float verticalFov) {
        this.verticalFov = Math.clamp(verticalFov, 0, (float) Math.PI);
        this.updated();
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
        this.updated();
    }

    public void setIgnoreFluids(boolean ignoreFluids) {
        this.ignoreFluids = ignoreFluids;
        this.updated();
    }

    public void setBackAndForth(boolean backAndForth) {
        this.backAndForth = backAndForth;
        this.updated();
    }

    public boolean isShowLaser() {
        return showLaser;
    }

    public void setShowLaser(boolean showLaser) {
        this.showLaser = showLaser;
        this.updated();
    }

    private void updated() {
        this.setChanged();

        if (this.getLevel() != null) {
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.loadAdditional(tag, registries);

        this.ignoreFluids = tag.getBoolean("ignoreFluids");
        this.rotationPeriod = tag.getFloat("rotationPeriod");
        this.horizontalFov = tag.getFloat("horizontalFov");
        this.verticalFov = tag.getFloat("verticalFov");
        this.range = tag.getFloat("range");
        this.backAndForth = tag.getBoolean("backAndForth");
        this.showLaser = tag.getBoolean("showLaser");
    }

    @Override
    protected void saveAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("ignoreFluids", this.ignoreFluids);
        tag.putFloat("rotationPeriod", this.rotationPeriod);
        tag.putFloat("horizontalFov", this.horizontalFov);
        tag.putFloat("verticalFov", this.verticalFov);
        tag.putFloat("range", this.range);
        tag.putBoolean("backAndForth", this.backAndForth);
        tag.putBoolean("showLaser", this.showLaser);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
