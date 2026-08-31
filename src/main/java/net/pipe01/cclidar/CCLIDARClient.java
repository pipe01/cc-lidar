package net.pipe01.cclidar;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.pipe01.cclidar.blocks.LidarBlockEntityRenderer;

import java.io.IOException;

@Mod(value = CCLIDAR.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CCLIDAR.MODID, value = Dist.CLIENT)
public class CCLIDARClient {
    public static ModelResourceLocation LIDAR_SENSOR_MODEL_LOCATION = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(CCLIDAR.MODID, "block/lidar_sensor"));

    public static ShaderInstance LASER_SHADER;

    public CCLIDARClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                CCLIDAR.LIDAR_BLOCK_ENTITY.get(),
                LidarBlockEntityRenderer::new
        );
    }

    @SubscribeEvent
    static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        event.register(LIDAR_SENSOR_MODEL_LOCATION);
    }

    @SubscribeEvent
    static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(
                                CCLIDAR.MODID,
                                "laser"
                        ),
                        DefaultVertexFormat.POSITION_TEX
                ),
                shader -> LASER_SHADER = shader
        );
    }
}
