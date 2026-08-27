package net.pipe01.cclidar.blocks;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.PeripheralType;

public class LidarPeripheral implements GenericPeripheral {
    @Override
    public String id() {
        return "lidar";
    }

    @LuaFunction(mainThread = true)
    public int test(LidarBlockEntity lidarBlockEntity) {
        return 42;
    }
}
