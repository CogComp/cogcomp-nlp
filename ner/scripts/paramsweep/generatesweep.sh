#!/bin/bash
#
# This script will generate a modified version of the LBJava file
# with the default parameters set to the values specified by the 
# alternative learning rates and thicknesses. Note that the defaults
# are overriden by the configuration arguments for both models.
#
learnrate=(.2 .01)
thickness=(20 200)

for i in ${learnrate[@]}; do
    for j in ${thickness[@]}; do
	    NEWDIR="r"$i"-t"$j
	    cp -rf ./cogcomp-nlp/ ./$NEWDIR
	    NEWNERDIR=$NEWDIR"/ner"
	    sed 's/SparseAveragedPerceptron\([^\)]*\)/SparseAveragedPerceptron\('"$i"', 0, '"$j"'/' $NEWNERDIR/src/main/lbj/LbjTagger.lbj > $NEWNERDIR/src/main/lbj/poogers.lbj
	    mv $NEWNERDIR/src/main/lbj/poogers.lbj $NEWNERDIR/src/main/lbj/LbjTagger.lbj
    done;
done;