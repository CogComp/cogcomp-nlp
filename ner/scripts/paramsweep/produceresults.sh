#!/bin/bash
#
#
# For each directory containing results, pull out the results, put into CSV, first column
# is the arguments, the second is the L1 F1 and the last is the L2 F1
for sweepdir in $(find . -maxdepth 1 -name L1r\*)
do
    if [ -f $sweepdir/ner/results.out ] ; then
        cd "$sweepdir"/ner

        # get the L1 score
        s1=`tail -n 2 results.out | sed -n 1p` 
        REGEXP="(0.[0-9]+)"
        if [[ $s1 =~ $REGEXP ]]; then
            scoreL1="${BASH_REMATCH[1]}" 
        fi

        # get the L2 score
        s1=`tail -n 1 results.out | sed -n 1p` 
        REGEXP="(0.[0-9]+)"
        if [[ $s1 =~ $REGEXP ]]; then
            scoreL2="${BASH_REMATCH[1]}" 
        fi

	#
	# pull out the learning rates and thicknesses.
	#
	REGEXP="./L1r(.[0-9]+)-t([0-9]+)\+L2r(.[0-9]+)-t([0-9]+)"
        if [[ $sweepdir =~ $REGEXP ]]; then
	    lr1="${BASH_REMATCH[1]}"
            t1="${BASH_REMATCH[2]}"
	    lr2="${BASH_REMATCH[3]}"
            t2="${BASH_REMATCH[4]}"
        fi
        echo $sweepdir,$lr1,$t1,$lr2,$t2,$scoreL1,$scoreL2
        cd ../.. 
    else
        echo $sweepdir not started yet.
	break
    fi
done

