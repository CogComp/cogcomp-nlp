#!/bin/bash
#
#
# For each directory containing results, pull out the results, put into CSV, first column
# is the arguments, the second is the L1 F1 and the last is the L2 F1
for sweepdir in $(find . -maxdepth 1 -name L1r\*)
do
    if [ -f $sweepdir/ner/results.out ] ; then
        cd "$sweepdir"/ner
        l1=`cat results.out | tail -n 2` 
        l1="${l1/ Level 1: /}"
        l1="${l1/[ ]*Level 2: /,}"
        echo $sweepdir,$l1
        cd ../.. 
    else
        echo $sweepdir not started yet.
    fi
done

