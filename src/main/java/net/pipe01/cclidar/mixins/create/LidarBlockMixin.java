package net.pipe01.cclidar.mixins.create;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.pipe01.cclidar.blocks.LidarBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LidarBlock.class)
public abstract class LidarBlockMixin implements IWrenchable {
}
