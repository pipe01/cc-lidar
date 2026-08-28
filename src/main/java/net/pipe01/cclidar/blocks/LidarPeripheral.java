package net.pipe01.cclidar.blocks;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class LidarPeripheral implements IPeripheral {
    private final LidarBlockEntity lidarBlockEntity;

    public LidarPeripheral(LidarBlockEntity lidarBlockEntity) {
        this.lidarBlockEntity = lidarBlockEntity;
    }

    @Override
    public @NonNull String getType() {
        return "lidar";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        return iPeripheral instanceof LidarPeripheral o && lidarBlockEntity == o.lidarBlockEntity;
    }

    @LuaFunction(mainThread = true)
    public final Double[] test(double fov, int steps, double range) {
        return lidarBlockEntity.getHits((float)fov, steps, range);
    }

    @LuaFunction
    public final void setSweepAngle(double angle) {
        lidarBlockEntity.setSweepAngle((float)angle);
    }

    @LuaFunction
    public final void setIgnoreFluids(boolean ignore) {
        lidarBlockEntity.setIgnoreFluids(ignore);
    }
}
