package net.pipe01.cclidar.blocks;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.pipe01.cclidar.CCLIDARClient;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

public class LidarBlockEntityRenderer implements BlockEntityRenderer<LidarBlockEntity> {
    private final BakedModel sensorModel;
    private final ModelBlockRenderer modelBlockRenderer;

    public static final RenderType LASER = RenderType.create(
            "laser",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.TRIANGLES,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> CCLIDARClient.LASER_SHADER))
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setCullState(RenderStateShard.CullStateShard.NO_CULL)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .createCompositeState(false)
    );

    public LidarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        sensorModel = Minecraft.getInstance().getModelManager().getModel(CCLIDARClient.LIDAR_SENSOR_MODEL_LOCATION);
        modelBlockRenderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
    }

    @Override
    public @NonNull AABB getRenderBoundingBox(@NonNull LidarBlockEntity blockEntity) {
        if (!blockEntity.isShowLaser()) {
            return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
        }

        float range = blockEntity.getRange();
        return AABB.ofSize(blockEntity.getBlockPos().getCenter(), range * 2, range * 2, range * 2);
    }

    @Override
    public void render(@NonNull LidarBlockEntity lidarBlockEntity, float partialTick, PoseStack poseStack, @NonNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        BlockState blockState = lidarBlockEntity.getBlockState();
        FrontAndTop orientation = blockState.getValue(LidarBlock.ORIENTATION);
        Direction facing = orientation.front();

        poseStack.translate(0.5, 0.5, 0.5);

        Quaternionf rotation = new Quaternionf();
        if (facing == Direction.UP || facing == Direction.DOWN) {
            switch (orientation.top()) {
                case NORTH -> rotation = Axis.YP.rotationDegrees(180);
                case WEST -> rotation = Axis.YP.rotationDegrees(270);
                case EAST -> rotation = Axis.YP.rotationDegrees(90);
            }
        }

        rotation = rotation.mul(switch (facing) {
            case DOWN -> Axis.XP.rotationDegrees(90);
            case UP -> Axis.XP.rotationDegrees(270);
            case NORTH -> Axis.YP.rotationDegrees(180);
            case SOUTH -> Axis.YP.rotationDegrees(0);
            case WEST -> Axis.YP.rotationDegrees(270);
            case EAST -> Axis.YP.rotationDegrees(90);
        });

        rotation = rotation.mul(Axis.YN.rotationDegrees(lidarBlockEntity.getCurrentAngle(partialTick)));

        poseStack.mulPose(rotation);

        poseStack.translate(-0.5, -0.5, -0.5);

        RenderType renderType = RenderType.solid();
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        modelBlockRenderer.renderModel(
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

        if (lidarBlockEntity.isShowLaser()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(rotation);
            renderCone(poseStack, bufferSource, lidarBlockEntity.getVerticalFov(), lidarBlockEntity.getRange());
            poseStack.popPose();
        }
    }

    private void renderCone(PoseStack poseStack, @NonNull MultiBufferSource bufferSource, float fov, float range) {
        VertexConsumer buffer = bufferSource.getBuffer(LASER);

        PoseStack.Pose pose = poseStack.last();

        float z = (float) Math.cos(fov / 2);
        float y = (float) Math.sin(fov / 2);

        float zm = 1 - z;

        Vector3f leftTip = new Vector3f(0, y * range, z * range);
        Vector3f rightTip = new Vector3f(0, -y * range, z * range);
        Vector3f leftExtraTip = new Vector3f(leftTip.x, leftTip.y, leftTip.z + zm * range);
        Vector3f rightExtraTip = new Vector3f(rightTip.x, rightTip.y, rightTip.z + zm * range);

        buffer.addVertex(pose, 0, 0, 0).setUv(0, 0);
        buffer.addVertex(pose, leftTip).setUv(z, y);
        buffer.addVertex(pose, rightTip).setUv(z, -y);

        buffer.addVertex(pose, leftTip).setUv(z, y);
        buffer.addVertex(pose, rightExtraTip).setUv(1, -y);
        buffer.addVertex(pose, leftExtraTip).setUv(1, y);

        buffer.addVertex(pose, leftTip).setUv(z, y);
        buffer.addVertex(pose, rightTip).setUv(z, -y);
        buffer.addVertex(pose, rightExtraTip).setUv(1, -y);
    }
}
