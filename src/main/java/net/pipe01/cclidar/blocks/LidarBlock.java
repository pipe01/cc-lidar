package net.pipe01.cclidar.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;

public class LidarBlock extends Block implements EntityBlock {
    private static final VoxelShape[] SHAPES = {
            getDirectionShape(Direction.DOWN),
            getDirectionShape(Direction.UP),
            getDirectionShape(Direction.NORTH),
            getDirectionShape(Direction.SOUTH),
            getDirectionShape(Direction.WEST),
            getDirectionShape(Direction.EAST),
    };

    public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

    public LidarBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(stateDefinition.any()
            .setValue(ORIENTATION, FrontAndTop.NORTH_UP)
        );
    }

    private static VoxelShape getDirectionShape(@NonNull Direction direction) {
        boolean positive = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        int mainAxis = direction.getAxis().choose(0, 1, 2);

        int thickness = 6;
        int padding = 2;

        float[] points = new float[6];
        points[mainAxis] = positive ? 0 : 16 - thickness;
        points[mainAxis + 3] = positive ? thickness : 16;
        points[(mainAxis + 1) % 3] = padding;
        points[(mainAxis + 2) % 3] = padding;
        points[(mainAxis + 1) % 3 + 3] = 16 - padding;
        points[(mainAxis + 2) % 3 + 3] = 16 - padding;

        return box(points[0], points[1], points[2], points[3], points[4], points[5]);
    }

    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new LidarBlockEntity(pos, state);
    }

    @Override
    public @NonNull VoxelShape getCollisionShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @NonNull VoxelShape getShape(BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return SHAPES[state.getValue(ORIENTATION).front().get3DDataValue()];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction face = ctx.getClickedFace();

        FrontAndTop orientation = switch (face) {
            case UP -> FrontAndTop.fromFrontAndTop(face, ctx.getHorizontalDirection().getOpposite());
            case DOWN -> FrontAndTop.fromFrontAndTop(face, ctx.getHorizontalDirection());
            default -> FrontAndTop.fromFrontAndTop(face, Direction.UP);
        };

        return stateDefinition.any().setValue(ORIENTATION, orientation);
    }

    @Override
    protected @NonNull BlockState rotate(@NonNull BlockState state, @NonNull Rotation rotation) {
        FrontAndTop orientation = state.getValue(ORIENTATION);
        FrontAndTop newOrientation = switch (orientation.front()) {
            case UP, DOWN -> FrontAndTop.fromFrontAndTop(orientation.front(), rotation.rotate(orientation.top()));
            default -> FrontAndTop.fromFrontAndTop(rotation.rotate(orientation.front()), orientation.top());
        };
        return state.setValue(ORIENTATION, newOrientation);
    }
}
