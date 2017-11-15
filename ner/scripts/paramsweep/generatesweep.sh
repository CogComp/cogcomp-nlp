#!/bin/bash
#
# This script will generate a modified version of the LBJava file
# with the default parameters set to the values specified by the 
# alternative learning rates and thicknesses. Note that the defaults
# are overriden by the configuration arguments for both models.
#

#
# first build of cogcomp-nlp. Oddly enough, takes longer to build than to
# move it already built.
#
echo "Building cogcomp-nlp"
bash -c "cd cogcomp-nlp ; mvn package -DskipTests dependency:copy-dependencies"

#
# we don't need the contents of any of the build directories but those in the ner directory.
bash -c "cd cogcomp-nlp/temporal-normalizer ; mvn clean"
bash -c "cd cogcomp-nlp/verbsense ; mvn clean"
bash -c "cd cogcomp-nlp/big-data-utils ; mvn clean"
bash -c "cd cogcomp-nlp/core-utilities ; mvn clean"
bash -c "cd cogcomp-nlp/pipeline ; mvn clean"
bash -c "cd cogcomp-nlp/prepsrl ; mvn clean"
bash -c "cd cogcomp-nlp/corpusreaders ; mvn clean"
bash -c "cd cogcomp-nlp/quantifier ; mvn clean"
bash -c "cd cogcomp-nlp/corpusreaders ; mvn clean"
bash -c "cd cogcomp-nlp/curator ; mvn clean"
bash -c "cd cogcomp-nlp/commasrl ; mvn clean"
bash -c "cd cogcomp-nlp/external/stanford_3.8.0 ; mvn clean"
bash -c "cd cogcomp-nlp/external/external-commons ; mvn clean"
bash -c "cd cogcomp-nlp/external/path-lstm ; mvn clean"
bash -c "cd cogcomp-nlp/external/clausie ; mvn clean"
bash -c "cd cogcomp-nlp/external/stanford_3.3.1 ; mvn clean"
bash -c "cd cogcomp-nlp/tokenizer ; mvn clean"
bash -c "cd cogcomp-nlp/similarity ; mvn clean"
bash -c "cd cogcomp-nlp/lbjava-nlp-tools ; mvn clean"
bash -c "cd cogcomp-nlp/inference ; mvn clean"
bash -c "cd cogcomp-nlp/lemmatizer ; mvn clean"
bash -c "cd cogcomp-nlp/depparse ; mvn clean"
bash -c "cd cogcomp-nlp/edison ; mvn clean"
bash -c "cd cogcomp-nlp/md ; mvn clean"
bash -c "cd cogcomp-nlp/pos ; mvn clean"

# learning rates and thickness range
learnrateL1=(.01 .03)
thicknessL1=(10 30)
learnrateL2=(.02 .04)
thicknessL2=(20 40)

#
# copy the code all over the place.
#
echo
echo -------- cloning code into new directories --------------
echo
for l1lr in ${learnrateL1[@]}; do
    for l1t in ${thicknessL1[@]}; do
        for  l2lr in ${learnrateL2[@]}; do
            for  l2t in ${thicknessL2[@]}; do
            	    NEWDIR="L1r"$l1lr"-t"$l1t"+L2r"$l2lr"-t"$l2t
	            cp -rf ./cogcomp-nlp/ ./$NEWDIR
  		    done
        done
        echo "completed executable copy up to L1 thickness = "$l1t
    done
done

#
# To save disk space (and potentially LOTS of it), link the actual data directories 
# into the various parameter sweep benchmark directories. The benchmark directory at
# the same level as this script is expect to contain all the data directories. We will copy
# the configs since they are all different.
#
echo
echo -------- copy the benchmark configs, link the data --------------
echo
for sweepdir in $(find . -maxdepth 1 -name L1r\*)
do
	# get into sweep directory
    pushd $sweepdir
    echo
    echo
    echo "Linking \""$sweepdir"\""
    
    # make the directories for the config
    mkdir ./ner/benchmark/
    cd ./ner/benchmark
    
    for benchmarkdir in $(find ../../../benchmark/ -maxdepth 1 -name \*) 
    do
        # if there is no conf dir, just skip it.
        if [ -e "$benchmarkdir/config" ]
        then
        
    	        #mdkr the dir for this corpus
    	        CORPORA=`basename "$benchmarkdir"`
    	        echo "$benchmarkdir -> $CORPORA : in `pwd`"
            mkdir "$CORPORA"
            pushd "$CORPORA"
                
            CD=../$benchmarkdir/config
    		    cp -r "$CD" .
            DEV=../$benchmarkdir/dev/
            if [ -e "$DEV" ]
            then
		        ln -s "$DEV" ./dev
            fi;
            TRAIN=../$benchmarkdir/train/
            if [ -e "$TRAIN" ]
            then
                ln -s "$TRAIN" ./train
            fi
            TEST=../$benchmarkdir/test/
            if [ -e "$TEST" ]
            then
               ln -s "$TEST" ./test
            fi
            popd
        else
            echo "$benchmarkdir/conf did not exist"
        fi
    done
    popd 
done

#
# Now set up the learning rates and thicknesses by replacing the associated arguments in
# the various config files
#
echo
echo -------- generating runtime arguments --------------
echo
for l1lr in ${learnrateL1[@]}; do
    for l1t in ${thicknessL1[@]}; do
        for  l2lr in ${learnrateL2[@]}; do
            for  l2t in ${thicknessL2[@]}; do
            	    NEWDIR="L1r"$l1lr"-t"$l1t"+L2r"$l2lr"-t"$l2t
	            NEWNERDIR=$NEWDIR"/ner"
	            for d in $(find $NEWNERDIR/benchmark -maxdepth 4 -name *.config) 
	            do
  			
                    # each file is expected to be a config file, and must have the 
                    # appropriate arguments so the replacement will work.
                    sed 's/learningRatePredictionsLevel1[^\n]*/learningRatePredictionsLevel1 '"$l1lr"'/' $d > $d.tmp1
                    sed 's/learningRatePredictionsLevel2[^\n]*/learningRatePredictionsLevel2 '"$l2lr"'/' $d.tmp1 > $d.tmp2
                    sed 's/thicknessPredictionsLevel1[^\n]*/thicknessPredictionsLevel1 '"$l1t"'/' $d.tmp2 > $d.tmp3
                    sed 's/thicknessPredictionsLevel2[^\n]*/thicknessPredictionsLevel2 '"$l2t"'/' $d.tmp3 > $d.tmp4
                    mv $d.tmp4 $d
                    rm -rf $d.tmp1 $d.tmp2 $d.tmp3
                done
            done
        done
        echo "completed parameter adjustments up to L1 thickness = "$l1t
    done
done
