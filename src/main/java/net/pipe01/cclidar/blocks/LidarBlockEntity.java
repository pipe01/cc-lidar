package net.pipe01.cclidar.blocks;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jspecify.annotations.NonNull;

public class LidarBlockEntity extends BlockEntity {
    public LidarBlockEntity(BlockPos pos, BlockState blockState) {
        super(CCLIDAR.LIDAR_BLOCK_ENTITY.get(), pos, blockState);
    }

    private boolean ignoreFluids = true;
    private float rotationSpeed = 1; // ticks per horizontal sweep
    private float horizontalFov = 90; // degrees
    private boolean backAndForth = true;

    // angle is "vertical" rotation
    private Double hitTest(Level level, float horAngle, float vertAngle, double range) {
        Direction facing = getBlockState().getValue(LidarBlock.FACING);

        Vec3 start = worldPosition.getCenter();
        Vec3 forward = Vec3.atLowerCornerOf(facing.getNormal());

        forward = switch (facing) {
            case DOWN -> forward.zRot(horAngle).xRot(-vertAngle);
            case UP -> forward.zRot(-horAngle).xRot(-vertAngle);
            case NORTH -> forward.yRot(-horAngle).xRot(vertAngle);
            case SOUTH -> forward.yRot(-horAngle).xRot(-vertAngle);
            case WEST -> forward.yRot(-horAngle).zRot(-vertAngle);
            case EAST -> forward.yRot(-horAngle).zRot(vertAngle);
        };

        var hit = level.clip(new ClipContext(
                start,
                start.add(forward.scale(range)),
                ClipContext.Block.VISUAL,
                ignoreFluids ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY,
                CollisionContext.empty()
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            return SableCompanion.INSTANCE.rectilinearDistanceWithSubLevels(level, worldPosition.getCenter(), hit.getLocation());
        }
        return null;
    }

    public Double[] getHits(float fov, int steps, double range) {
        Level level = getLevel();

        if (level != null) {
            float fovr = (float) Math.toRadians(fov);
            float horAngle = (float) Math.toRadians(getCurrentAngle());
            float stepAngle = fovr / (steps - 1);

            Double[] hits = new Double[steps];
            for (int i = 0; i < steps; i++) {
                hits[i] = hitTest(level, horAngle, stepAngle * i - fovr / 2, range);
            }
            return hits;
        }

        return null;
    }

    public float getCurrentAngle() {
        return getCurrentAngle(0);
    }

    public float getCurrentAngle(float partialTick) {
        if (getLevel() != null) {
            double t = getLevel().getGameTime() + (double)partialTick;

            if (backAndForth) {
                double x = 2 * Math.abs(t / (rotationSpeed * 2) - Math.floor(t / (rotationSpeed * 2) + 0.5)) * horizontalFov;
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
        this.rotationSpeed = rotationSpeed;
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
