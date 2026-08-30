package net.pipe01.cclidar.blocks;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
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

    @LuaFunction(mainThread = true)
    public final Map<String, Object> test(IArguments args) throws LuaException {
        double fov = args.getDouble(0);
        int steps = args.getInt(1);
        double range = args.getDouble(2);
        int detailLevel = args.optInt(3, 0);

        var result = new HashMap<String, Object>();
        result.put("rays", lidarBlockEntity.getHits((float)fov, steps, range, detailLevel));
        result.put("horizontalAngle", lidarBlockEntity.getCurrentAngle());
        return result;
    }

    @LuaFunction
    public final void setRotationSpeed(double speed) {
        lidarBlockEntity.setRotationSpeed((float)speed);
    }

    @LuaFunction
    public final void setHorizontalFov(double fov) {
        lidarBlockEntity.setHorizontalFov((float)fov);
    }

    @LuaFunction
    public final void setIgnoreFluids(boolean ignore) {
        lidarBlockEntity.setIgnoreFluids(ignore);
    }

    @LuaFunction
    public final void setBackAndForth(boolean v) {
        lidarBlockEntity.setBackAndForth(v);
    }

    @LuaFunction
    public final float getCurrentAngle() {
        return lidarBlockEntity.getCurrentAngle();
    }
}
