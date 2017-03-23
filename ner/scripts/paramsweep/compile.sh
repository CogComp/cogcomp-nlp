#!/bin/bash
learnrate=(.1 .06)
thickness=(50 60)

for i in ${learnrate[@]}; do
    for j in ${thickness[@]}; do
    	NEWDIR="r"$i"-t"$j"/ner/"
        echo
        echo
        echo "DOING "$NEWDIR
        bash -c "cd $NEWDIR ; mvn lbjava:clean clean"
        bash -c "cd $NEWDIR ; mvn package -DskipTests dependency:copy-dependencies"
    done;
done;
