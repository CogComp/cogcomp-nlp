#!/bin/bash

###
# runs a sanity check, processing a sample text file and comparing the
#   output to a reference file.  Success indicates that the package
#   has been installed correctly and works on your system.
#

VERSION=2.0.1
MAIN_JAR=dist/illinois-pos-${VERSION}.jar
MAIN=edu.illinois.cs.cogcomp.lbj.pos.POSTagPlain
LBJ=lib/LBJava-1.0.jar

TEST_IN="test/testIn.txt"
TEST_OUT="test/testOut.txt"
REF_FILE="test/testRefOutput.txt"
TEST_FILTERED="test/testFiltered.txt"
DIFF_FILE="test/test.diff"

CP="$MAIN_JAR:$LBJ"

CMD="java -Xmx500m -cp $CP $MAIN $TEST_IN"

echo "$0: running command '$CMD > $TEST_OUT'..."

$CMD > $TEST_OUT

echo "$0: comparing tagger output to reference output..."

rm -f $DIFF_FILE

# remove logging messages with time information
egrep -v "ch.qos|c.q.l" $TEST_OUT > $TEST_FILTERED

diff $REF_FILE $TEST_FILTERED > $DIFF_FILE


if [ -e $DIFF_FILE ]; then 
    if [ -s $DIFF_FILE ]; then
	echo "$0: *** TEST FAILED ***: Differences found between new output and reference output.  See $DIFF_FILE for details." 
    else
	echo "$0: Test passed: no difference between new output and reference output."
    fi
else
    echo "$0: Error: couldn't find the diff file '$DIFF_FILE'."
fi


echo "$0: done."
