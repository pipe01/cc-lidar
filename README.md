# CC: LiDAR

This mod adds a single block: the LiDAR Sensor. LiDAR sensors give ComputerCraft computers the ability to see the world in front of them by firing a bunch of rays and detecting their return when they hit blocks.

![lidartest running on 256-bit color mode](https://cdn.modrinth.com/data/aj1d8xSa/images/0838e2635b3944c6a42498749808b36e14716e6c.png)

## Usage

Attach a LiDAR Sensor to any computer, then run the `lidartest` command to see a demo of what the sensor can do. Feel free to run `lidartest --help` to see more options.

When you're ready to integrate the sensor in your program, wrap the peripheral as usual and use the following methods to interact with it:

- `lidar.setRotationPeriod(period)`: sets how many ticks the horizontal sweep from one end of the FOV to the other will take, a lower period means a faster sweep. A period lower than 2 will stop the sweep.
- `lidar.setHorizontalFov(fov)`: (degrees) adjusts the amplitude of the sensor's horizontal sweep.
- `lidar.setVerticalFov(fov)`: (degrees) adjusts the amplitude of the column of rays.
- `lidar.setRange(range)`: maximum distance in blocks that a ray will travel.
- `lidar.setIgnoreFluids(ignore)`: if set to `true`, rays will go through fluids such as water and will instead hit the blocks behind it.
- `lidar.setBackAndForth(enable)`: by default the sensor will sweep left to right, then  right to left and repeat. This can be changed to instead go left to right, then jump back to left and repeat.
- `lidar.setShowLaser(show)`: if set to `true`, a visualization of the sensor's FOV cone will be shown.
- `lidar.getCurrentAngle()`: returns the angle that the sensor is currently looking at (not recommended, see `test`).
- `lidar.test(rayCount, [detail])`: this is the main method of the LiDAR Sensor, it will fire a column of rays aligned horizontally to the current sweep angle.
  - Arguments:
    - `rayCount`: number of rays to be shot. A higher amount will result in a higher resolution but also a higher performance impact.
    - `detail`: integer that controls the information that is returned.
  - Returns a `table`:
    - `horizontalAngle`: the sweep angle (in degrees) at which this column of rays was fired. Equivalent in concept to `lidar.getCurrentAngle()`, however that function has a 1-tick delay so it will return an incorrect value if called after `test`.
    - `rays`: an array of `rayCount` entries, where each is `nil` (if the ray didn't hit) or a `table` like:
      - `verticalAngle`: vertical angle (in degrees) at which this ray was fired with respect to the horizontal plane.
      - `distance`: distance that the ray traveled before hitting a surface.
      - If `detail == 1`, this table additionally contains:
        - `name`: the hit block's key.
        - `state`: the hit block's state.
      - If `detail == 2`, this table additionally contains all information described [here](https://tweaked.cc/reference/block_details.html).

## Performance

The peripheral is implemented using raycasts, which are quite computationally costly. It can easily degrade TPS in a server, so I would advise not to add this mod to public servers or at the very least limit what the sensor can do using the provided configuration values.

## Integrations

- Create: the LiDAR Sensor can be rotated using the wrench
- Sable (Create Aeronautics): the LiDAR sensor works when used inside a sublevel and can detect blocks in other sublevels
