package net.pipe01.cclidar.blocks;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LidarPeripheral implements IPeripheral {
    private final LidarBlockEntity lidarBlockEntity;

    public LidarPeripheral(LidarBlockEntity lidarBlockEntity) {
        this.lidarBlockEntity = lidarBlockEntity;
    }

    @Override
    public @NonNull String getType() {
        return "lidar_sensor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        return iPeripheral instanceof LidarPeripheral o && lidarBlockEntity == o.lidarBlockEntity;
    }

    @Override
    public void detach(@NonNull IComputerAccess computer) {
        lidarBlockEntity.setShowLaser(false);
        lidarBlockEntity.setRotationPeriod(0);
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> test(IArguments args) throws LuaException {
        int steps = args.getInt(0);
        int detailLevel = args.optInt(1, 0);

        var result = new HashMap<String, Object>();
        result.put("rays", lidarBlockEntity.getHits(steps, detailLevel));
        result.put("horizontalAngle", lidarBlockEntity.getCurrentAngle());
        return result;
    }

    @LuaFunction(mainThread = true)
    public final void setRotationPeriod(double speed) {
        lidarBlockEntity.setRotationPeriod((float)speed);
    }

    @LuaFunction(mainThread = true)
    public final void setHorizontalFov(double fov) {
        lidarBlockEntity.setHorizontalFov((float)fov);
    }

    @LuaFunction(mainThread = true)
    public final void setVerticalFov(double fov) {
        lidarBlockEntity.setVerticalFov((float) Math.toRadians(fov));
    }

    @LuaFunction(mainThread = true)
    public final void setRange(double range) {
        lidarBlockEntity.setRange((float)range);
    }

    @LuaFunction(mainThread = true)
    public final void setIgnoreFluids(boolean ignore) {
        lidarBlockEntity.setIgnoreFluids(ignore);
    }

    @LuaFunction(mainThread = true)
    public final void setBackAndForth(boolean v) {
        lidarBlockEntity.setBackAndForth(v);
    }

    @LuaFunction(mainThread = true)
    public final void setShowLaser(boolean v) {
        lidarBlockEntity.setShowLaser(v);
    }

    @LuaFunction(mainThread = true)
    public final float getCurrentAngle() {
        return lidarBlockEntity.getCurrentAngle();
    }
}
