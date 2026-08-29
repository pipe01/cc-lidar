package net.pipe01.cclidar;

import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.pipe01.cclidar.blocks.LidarBlock;
import net.pipe01.cclidar.blocks.LidarBlockEntity;
import net.pipe01.cclidar.blocks.LidarPeripheral;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CCLIDAR.MODID)
public class CCLIDAR {
    public static final String MODID = "cclidar";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<LidarBlock> LIDAR_BLOCK = BLOCKS.register(
            "lidar",
            registryName -> new LidarBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_YELLOW)
            ));
    public static final Supplier<BlockEntityType<LidarBlockEntity>> LIDAR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "lidar",
            () -> new BlockEntityType<>(
                    LidarBlockEntity::new,
                    Set.of(LIDAR_BLOCK.get()),
                    null
            )
    );
    public static final DeferredItem<BlockItem> LIDAR_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("lidar", LIDAR_BLOCK);

    public CCLIDAR(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerCapabilities);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(PeripheralCapability.get(), LIDAR_BLOCK_ENTITY.get(), (b, d) -> new LidarPeripheral(b));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        var location = event.getTabKey().location();
        if (location.getNamespace().equals("computercraft") && location.getPath().equals("tab")) {
            event.accept(LIDAR_BLOCK_ITEM);
        }
    }
}
