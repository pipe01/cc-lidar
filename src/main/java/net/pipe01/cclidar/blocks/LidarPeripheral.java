package net.pipe01.cclidar.blocks;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;
import net.minecraft.world.phys.Vec3;

public class LidarPeripheral implements GenericPeripheral {
    @Override
    public String id() {
        return "lidar";
    }

    @LuaFunction(mainThread = true)
    public Double test(LidarBlockEntity lidarBlockEntity) {
        return lidarBlockEntity.getHit();
    }
}
