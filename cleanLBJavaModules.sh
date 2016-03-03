#!/usr/bin/env bash

## declare a list of modules which are use lbjava
declare -a folders=("pos")

## now loop through the above array
for f in "${folders[@]}"
do
    cd "$f"
    mvn lbjava:clean
    cd ..
done



