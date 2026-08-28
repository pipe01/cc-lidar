package net.pipe01.cclidar.blocks;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.pipe01.cclidar.CCLIDAR;
import org.jspecify.annotations.NonNull;

public class LidarBlockEntity extends BlockEntity {
    private static final float MIN_SWEEP_ANGLE = -(float)Math.PI / 2;
    private static final float MAX_SWEEP_ANGLE = (float)Math.PI / 2;

    public LidarBlockEntity(BlockPos pos, BlockState blockState) {
        super(CCLIDAR.LIDAR_BLOCK_ENTITY.get(), pos, blockState);
    }

    // "horizontal" rotation
    private float currentAngle = 0;
    private boolean ignoreFluids = true;

    // angle is "vertical" rotation
    private Double hitTest(Level level, float angle, double range) {
        Direction facing = getBlockState().getValue(LidarBlock.FACING);

        Vec3 start = worldPosition.getCenter();
        Vec3 forward = Vec3.atLowerCornerOf(facing.getNormal());

        forward = switch (facing) {
            case DOWN -> forward.zRot(-currentAngle).xRot(angle);
            case UP -> forward.zRot(currentAngle).xRot(angle);
            case NORTH -> forward.yRot(currentAngle).xRot(-angle);
            case SOUTH -> forward.yRot(-currentAngle).xRot(-angle);
            case WEST -> forward.yRot(currentAngle).zRot(angle);
            case EAST -> forward.yRot(-currentAngle).zRot(angle);
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

    public float getSweepAngle() {
        return currentAngle;
    }

    public void setSweepAngle(float currentAngle) {
        this.currentAngle = Math.clamp(currentAngle, MIN_SWEEP_ANGLE, MAX_SWEEP_ANGLE);
        this.setChanged();
    }

    public boolean isIgnoreFluids() {
        return ignoreFluids;
    }

    public void setIgnoreFluids(boolean ignoreFluids) {
        this.ignoreFluids = ignoreFluids;
        this.setChanged();
    }

    public Double[] getHits(float fov, int steps, double range) {
        var level = getLevel();

        if (level != null) {
            Double[] hits = new Double[steps];

            for (int i = 0; i < steps; i++) {
                hits[i] = hitTest(level, (fov / (steps - 1)) * i - fov / 2, range);
            }

            return hits;
        }

        return null;
    }

    @Override
    protected void loadAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.loadAdditional(tag, registries);

        this.ignoreFluids = tag.getBoolean("ignoreFluids");
        this.currentAngle = tag.getFloat("currentAngle");
    }

    @Override
    protected void saveAdditional(@NonNull CompoundTag tag, HolderLookup.@NonNull Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("ignoreFluids", this.ignoreFluids);
        tag.putFloat("currentAngle", this.currentAngle);
    }
}
