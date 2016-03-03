#!/usr/bin/env bash

## declare a list of modules which are use lbjava
declare -a folders=("pos")

## now loop through the above array
for f in "${folders[@]}"
do
    if [ -d "$f" ]
    then
        echo "Cleaning the lbj files for $f . . . "
        cd "$f"
        mvn lbjava:clean
        cd ..
    else
        echo "Warning: Directory $f does not exists."
    fi
done



