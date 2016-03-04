#!/usr/bin/env bash

## declare a list of modules which are use lbjava (no commas, just space)
declare -a folders=("pos")

## now loop through the above array
for f in "${folders[@]}"
do
    if [ -d "$f" ]
    then
        echo "Generating files for $f . . . "
        cd "$f"
        mvn lbjava:clean lbjava:generate
        cd ..
    else
        echo "Warning: Directory $f does not exists."
    fi
done
