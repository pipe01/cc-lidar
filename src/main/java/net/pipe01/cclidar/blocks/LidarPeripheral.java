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
    public Double[] test(LidarBlockEntity lidarBlockEntity, double fov, int steps, double range) {
        return lidarBlockEntity.getHits((float)fov, steps, range);
    }

    @LuaFunction
    public void setSweepAngle(LidarBlockEntity lidarBlockEntity, double angle) {
        lidarBlockEntity.setSweepAngle((float)angle);
    }

    @LuaFunction
    public void setIgnoreFluids(LidarBlockEntity lidarBlockEntity, boolean ignore) {
        lidarBlockEntity.setIgnoreFluids(ignore);
    }
}
