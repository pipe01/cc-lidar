local args = {...}
local out = peripheral.find("monitor") or term

local supportsPixelMode = out.setGraphicsMode ~= nil

local pixelsMode = supportsPixelMode
local depthMode = false
local fov = 90
local range = 20
local pixelBlockSize = 6 -- 1,2,3,4,6,9,12,18,27,36,54
local backAndForth = true
local ignoreFluids = false

local passedArgs = false
while #args > 0 do
    passedArgs = true

    local arg = table.remove(args, 1)

    if arg == "--text" then
        pixelsMode = false
    elseif arg == "--depth" then
        depthMode = true
    elseif arg == "--fov" then
        fov = tonumber(table.remove(args, 1))
    elseif arg == "--range" then
        range = tonumber(table.remove(args, 1))
    elseif arg == "--res" then
        pixelBlockSize = tonumber(table.remove(args, 1))
    elseif arg == "--single" then
        backAndForth = false
    elseif arg == "--ignore-fluids" then
        ignoreFluids = true
    elseif arg == "--help" then
        print("Options:")
        if supportsPixelMode then
            print("  --text             Forces text mode instead of 256-bit color")
            print("  --res <resolution> Sets the pixel size when running on 256-bit color mode. Should be one of 1,2,3,4,6,9,12,18,27,36,54")
        end
        print("  --depth            Enables depth visualization")
        print("  --fov <fov>        Sets the field of view of the sensor")
        print("  --range <range>    Sets the range of the sensor")
        print("  --single           Jumps from one end of the FOV to the other instead of bouncing between them")
        print("  --ignore-fluids    Sees blocks behind fluids instead of the fluid itself")
        return
    else
        error("Unknown argument "..arg)
    end
end

local lidar = peripheral.find("lidar_sensor")
if lidar == nil then
    print("No LiDAR sensor found")
    return
end

if pixelsMode then
    print("Running on 256-bit color mode")
    out.setGraphicsMode(2)
elseif out.setGraphicsMode ~= nil then
    print("Running on text mode")
    out.setGraphicsMode(0)
end

if not passedArgs then
    print("Hint: run \"lidartest --help\" to see all available options")
end

local width, height = out.getSize(pixelsMode and 2 or 0)
local blockSize = pixelsMode and pixelBlockSize or 1

local horSteps = math.floor(width / blockSize)
local vertSteps = math.floor(height / blockSize)

lidar.setRotationSpeed(horSteps)
lidar.setHorizontalFov(fov)
lidar.setBackAndForth(backAndForth)
lidar.setIgnoreFluids(ignoreFluids)

out.clear()

local maxColors = pixelsMode and 255 or 15

local colors = {}
local colorCounter = 0

function colorToPalette(index)
    if pixelsMode then
        return index
    end
    return bit.blshift(1, index)
end
out.setPaletteColor(colorToPalette(0), 0x6495ED)

if depthMode then
    for i=0,maxColors do
        local c = i / maxColors
        out.setPaletteColor(colorToPalette(i), c, c, c)
    end
end

function round(x)
    return x >= 0 and math.floor(x + 0.5) or math.ceil(x - 0.5)
end

while true do
    local hit = lidar.test(fov, vertSteps, range, depthMode and 0 or 2)
    if hit == nil then
        break
    end

    local progress = (hit.horizontalAngle + fov / 2) / fov
    local col = round(progress * (horSteps - 1)) + 1

    if not depthMode then
        for j=1,vertSteps do
            local d = hit.rays[j]

            if d ~= nil and d.details.mapColor ~= nil and colors[d.details.mapColor] == nil then
                local colorIndex = colorCounter + 2 -- skip reserved color
                colorCounter = colorCounter + 1

                out.setPaletteColor(colorToPalette(colorIndex), d.details.mapColor)
                colors[d.details.mapColor] = colorIndex
            end
        end
    end

    for j=1,vertSteps do
        local d = hit.rays[j]

        local colorIndex = 0
        if d ~= nil then
            if depthMode then
                colorIndex = round(maxColors * d.distance / range)
            else
                if d.details.mapColor ~= nil then
                    colorIndex = colors[d.details.mapColor]
                else
                    colorIndex = 1
                end
            end
        end

        if pixelsMode then
            out.drawPixels((col - 1) * blockSize, (j - 1) * blockSize, colorIndex, blockSize, blockSize)
        else
            out.setCursorPos(col, j)
            out.setBackgroundColor(colorToPalette(colorIndex))
            out.write(" ")
        end
    end
end
