#!/bin/bash
#
# This script will generate a modified version of the LBJava file
# with the default parameters set to the values specified by the 
# alternative learning rates and thicknesses. Note that the defaults
# are overriden by the configuration arguments for both models.
#
learnrateL1=(.01 .001)
thicknessL1=(10 100)
learnrateL2=(.2 .02)
thicknessL2=(20 200)

for l1lr in ${learnrateL1[@]}; do
    for l1t in ${thicknessL1[@]}; do
        for  l2lr in ${learnrateL2[@]}; do
            for  l2t in ${thicknessL2[@]}; do
            	    NEWDIR="L1r"$l1lr"-t"$l1t"+L2r"$l2lr"-t"$l2t
	            cp -rf ./cogcomp-nlp/ ./$NEWDIR
	            NEWNERDIR=$NEWDIR"/ner"
	            for d in $(find $NEWNERDIR/benchmark -maxdepth 4 -name *.config) 
	            do
  			        echo $d
  			
  			        # each file is expected to be a config file, and must have the 
  			        # appropriate arguments so the replacement will work.
  			        sed 's/learningRatePredictionsLevel1[^\n]*/learningRatePredictionsLevel1 '"$l1lr"'/' $d > $d.tmp1
  			        sed 's/learningRatePredictionsLevel2[^\n]*/learningRatePredictionsLevel2 '"$l2lr"'/' $d.tmp1 > $d.tmp2
  			        sed 's/thicknessPredictionsLevel1[^\n]*/thicknessPredictionsLevel1 '"$l1t"'/' $d.tmp2 > $d.tmp3
  			        sed 's/thicknessPredictionsLevel2[^\n]*/thicknessPredictionsLevel2 '"$l2t"'/' $d.tmp3 > $d.tmp4
  			        mv $d.tmp4 $d
  			        rm -rf $d.tmp1 $d.tmp2 $d.tmp3
  			    done;
  			done;
		done;
    done;
done;