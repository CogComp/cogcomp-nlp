#!/bin/bash
#
# Copy the benchmarkData directory into each of the directorys where the 
# benchmark will run, this is where each benchmark will look for it's data. 
# With this done, catenate the thickness and learning rate to the end of the
# config file.
#
learnrate=(.2 .01)
thickness=(20 200)

for i in ${learnrate[@]}; do
    for j in ${thickness[@]}; do
	NEWDIR="r"$i"-t"$j"/ner"
	bash -c "cd $NEWDIR ; rm -rf benchmark ; cp -r ../../benchmark benchmark"
	
	# now programmatically modify the config files.
        for EXPDIR in $NEWDIR/benchmark/*; do
            for CONFFILE in $EXPDIR/config/*.config; do
		 printf \
"thicknessPredictionsLevel1="$j\
"\nthicknessPredictionsLevel2="$j\
"\nlearningRatePredictionsLevel1="$i\
"\nlearningRatePredictionsLevel2="$i >> $CONFFILE
	    done;
        done;
    done;
done;
