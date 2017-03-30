#!/bin/bash
#
# Link the benchmarkData directory into each of the directorys where the 
# benchmark will run, this is where each benchmark will look for it's data.
#
learnrate=(.3 .2 .1 .06)
thickness=(50 60)

for i in ${learnrate[@]}; do
    for j in ${thickness[@]}; do
	NEWDIR="r"$i"-t"$j"/ner/"
	bash -c "cd $NEWDIR ; rm -rf benchmark ; ln -sF ../../benchmark benchmark"
    done;
done;
