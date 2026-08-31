package net.pipe01.cclidar;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_LIDAR_RANGE = BUILDER
            .comment("Maximum range allowed on all LiDAR scans")
            .defineInRange("maxLidarRange", 100, 1, 512);

    public static final ModConfigSpec.IntValue MAX_VERTICAL_RESOLUTION = BUILDER
            .comment("Maximum vertical resolution allowed on all LiDAR scans")
            .defineInRange("maxVerticalResolution", 50, 1, 512);

    static final ModConfigSpec SPEC = BUILDER.build();
}
