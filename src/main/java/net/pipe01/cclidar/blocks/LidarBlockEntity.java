package net.pipe01.cclidar.blocks;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.pipe01.cclidar.CCLIDAR;
import org.joml.Vector3dc;

public class LidarBlockEntity extends BlockEntity {
    public LidarBlockEntity(BlockPos pos, BlockState blockState) {
        super(CCLIDAR.LIDAR_BLOCK_ENTITY.get(), pos, blockState);
    }

    public Double getHit() {
        var level = getLevel();

        if (level != null) {
            var hit = level.clip(new ClipContext(
                    worldPosition.getCenter(),
                    worldPosition.getCenter().add(Vec3.atLowerCornerOf(getBlockState().getValue(LidarBlock.FACING).getNormal()).multiply(3, 3, 3)),
                    ClipContext.Block.VISUAL,
                    ClipContext.Fluid.ANY,
                    CollisionContext.empty()
            ));

            if (hit.getType() == HitResult.Type.BLOCK) {
                return SableCompanion.INSTANCE.rectilinearDistanceWithSubLevels(level, worldPosition.getCenter(), hit.getLocation());
            }
            return null;
        }

        return null;
    }
}
