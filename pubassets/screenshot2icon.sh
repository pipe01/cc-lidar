#!/bin/bash

# Install https://www.curseforge.com/minecraft/mc-mods/orthocamera-unofficial-neoforge-port
# Teleport to one block below the sensor block's coordinates at a 45º,45º angle
# Take a screenshot on orthographic mode
# The screenshot's size should be 2560x1383

magick "$1" -gravity center -crop 300x300-51+0 +repage icon.png
