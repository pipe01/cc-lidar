package net.pipe01.cclidar.mixins.create;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.pipe01.cclidar.blocks.LidarBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LidarBlock.class)
public abstract class LidarBlockMixin implements IWrenchable {
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        FrontAndTop orientation = originalState.getValue(LidarBlock.ORIENTATION);

        Direction.Axis wasAxis = orientation.front().getAxis();
        Direction.Axis targetAxis = targetedFace.getAxis();

        FrontAndTop newOrientation;
        if (wasAxis.isVertical() && targetAxis.isVertical()) {
            newOrientation = FrontAndTop.fromFrontAndTop(orientation.front(), orientation.top().getClockWise());
        } else {
            Direction newFront = Direction.getNearest(Vec3.atLowerCornerOf(orientation.front().getNormal().cross(targetedFace.getNormal())));
            Direction newUp;

            if (!wasAxis.isVertical() && !targetAxis.isVertical())
                newUp = orientation.front();
            else
                newUp = Direction.UP;

            newOrientation = FrontAndTop.fromFrontAndTop(newFront, newUp);
        }

        //noinspection ConstantValue
        return originalState.setValue(LidarBlock.ORIENTATION, newOrientation == null ? orientation : newOrientation);
    }
}
