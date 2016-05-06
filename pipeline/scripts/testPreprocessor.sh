#!/bin/bash 

### 
# PURPOSE: test the Illinois Temporal Extractor using a fixed
#   input file and reference output. 
# executor must have read/write/execute privileges in test dir. 
#

CONFIG="config"
TEST="test"

RUNSCRIPT="runPreprocessor.sh"
CONFIG_FILE="$CONFIG/pipeline-config.properties"
TEST_IN="$TEST/testIn.txt"
TEST_OUT_TXT="$TEST/testOut.txt"
TEST_OUT_SER="$TEST/testOut.ser"
TEST_FILTERED="$TEST/testFinal.txt"
REF_FILE="$TEST/referenceOut.txt"
DIFF_FILE="$TEST/test.diff"

if [ ! -e $REF_FILE ]; then
    echo "ERROR: $0: no reference file '$REF_FILE' found." 
    exit -1
fi


CMD="./scripts/$RUNSCRIPT $CONFIG_FILE $TEST_IN $TEST_OUT_TXT $TEST_OUT_SER"

echo "$0: running command '$CMD'"

$CMD

# test whether diff of test and reference output is empty

rm -f $TEST/test.diff

egrep -v "ch.qos|c.q.l" $TEST_OUT_TXT > $TEST_FILTERED

diff $REF_FILE $TEST_FILTERED > $DIFF_FILE


if [ -e $DIFF_FILE ]; then 
    if [ -s $DIFF_FILE ]; then
	echo "$0: Differences found between new output and reference output:" 
	cat $DIFF_FILE
    else
	echo "$0: Test passed: no difference between new output and reference output."
    fi
else
    echo "$0: Error: couldn't find the diff file '$DIFF_FILE'."
fi

