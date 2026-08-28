package net.pipe01.cclidar.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.pipe01.cclidar.CCLIDARClient;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;

public class LidarBlockEntityRenderer implements BlockEntityRenderer<LidarBlockEntity> {
    private final BakedModel sensorModel;
    private final ModelBlockRenderer modelBlockRenderer;

    public LidarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        sensorModel = Minecraft.getInstance().getModelManager().getModel(CCLIDARClient.LIDAR_SENSOR_MODEL_LOCATION);
        modelBlockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
    }

    @Override
    public void render(@NonNull LidarBlockEntity lidarBlockEntity, float partialTick, PoseStack poseStack, @NonNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        BlockState blockState = lidarBlockEntity.getBlockState();
        Direction facing = blockState.getValue(LidarBlock.FACING);

        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal()).scale(-0.1f);
        poseStack.translate(normal.x, normal.y, normal.z);

        Quaternionf facingRotation = switch (facing) {
            case DOWN -> Axis.XP.rotationDegrees(90);
            case UP -> Axis.XP.rotationDegrees(270);
            case NORTH -> Axis.YP.rotationDegrees(180);
            case SOUTH -> Axis.YP.rotationDegrees(0);
            case WEST -> Axis.YP.rotationDegrees(270);
            case EAST -> Axis.YP.rotationDegrees(90);
        };

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(facingRotation.mul(Axis.YN.rotationDegrees(lidarBlockEntity.getCurrentAngle(partialTick))));
        poseStack.translate(-0.5, -0.5, -0.5);

        RenderType renderType = RenderType.solid();
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer,
                blockState,
                sensorModel,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                renderType
        );

        poseStack.popPose();
    }
}
