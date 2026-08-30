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
    private float rotationSpeed = 0; // ticks per horizontal sweep
    private float horizontalFov = 90; // degrees
    private boolean backAndForth = true;

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

    private Hit hitTest(Level level, Vector3d center, float horAngle, float vertAngle, double range, int detailLevel) {
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

    public Hit[] getHits(float fov, int steps, double range, int detailLevel) {
        Level level = getLevel();

        if (level != null) {
            range = Math.clamp(range, 1, Config.MAX_LIDAR_RANGE.getAsInt());
            steps = Math.clamp(steps, 1, Config.MAX_VERTICAL_RESOLUTION.getAsInt());

            float fovr = (float) Math.toRadians(fov);
            float horAngle = (float) Math.toRadians(getCurrentAngle());
            float stepAngle = fovr / (steps - 1);
            Vector3d center = SableCompanion.INSTANCE.projectOutOfSubLevel(level, JOMLConversion.atCenterOf(getBlockPos()));

            Hit[] hits = new Hit[steps];
            for (int i = 0; i < steps; i++) {
                hits[i] = hitTest(level, center, horAngle, steps == 1 ? 0 : (stepAngle * i - fovr / 2), range, detailLevel);
            }
            return hits;
        }

        return null;
    }

    public float getCurrentAngle() {
        return getCurrentAngle(0);
    }

    public float getCurrentAngle(float partialTick) {
        if (rotationSpeed >= 2 && getLevel() != null) {
            double t = getLevel().getGameTime() + (double)partialTick;

            if (backAndForth) {
                float s = rotationSpeed - 1;
                double x = 2 * Math.abs(t / (s * 2) - Math.floor(t / (s * 2) + 0.5)) * horizontalFov;
                return (float)x - horizontalFov / 2;
            }

            double prog = (t % rotationSpeed) / (rotationSpeed - 1);
            return (float)prog * horizontalFov - horizontalFov / 2;
        }
        return 0;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed < 2 ? 0 : rotationSpeed;
        this.updated();
    }

    public float getHorizontalFov() {
        return horizontalFov;
    }

    public void setHorizontalFov(float horizontalFov) {
        this.horizontalFov = Math.clamp(horizontalFov, 0, 180);
        this.updated();
    }

    public boolean isIgnoreFluids() {
        return ignoreFluids;
    }

    public void setIgnoreFluids(boolean ignoreFluids) {
        this.ignoreFluids = ignoreFluids;
        this.updated();
    }

    public boolean isBackAndForth() {
        return backAndForth;
    }

    public void setBackAndForth(boolean backAndForth) {
        this.backAndForth = backAndForth;
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
        this.rotationSpeed = tag.getFloat("rotationSpeed");
        this.horizontalFov = tag.getFloat("horizontalFov");
        this.backAndForth = tag.getBoolean("backAndForth");
    }

    @Override
    protected void saveAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("ignoreFluids", this.ignoreFluids);
        tag.putFloat("rotationSpeed", this.rotationSpeed);
        tag.putFloat("horizontalFov", this.horizontalFov);
        tag.putBoolean("backAndForth", this.backAndForth);
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
