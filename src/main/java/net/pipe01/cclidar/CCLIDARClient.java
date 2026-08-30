package net.pipe01.cclidar;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.pipe01.cclidar.blocks.LidarBlockEntityRenderer;

@Mod(value = CCLIDAR.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CCLIDAR.MODID, value = Dist.CLIENT)
public class CCLIDARClient {
    public static ModelResourceLocation LIDAR_SENSOR_MODEL_LOCATION = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(CCLIDAR.MODID, "block/lidar_sensor"));

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
}
