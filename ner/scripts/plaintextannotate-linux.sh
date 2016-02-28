#!/bin/bash

########
# Tests NER package by running it on a sample input and comparing that to 
#    a reference output
# If you are developing the code/working with maven, you will first 
#    need to run the following commands:
#    > mvn dependency:copy-dependencies
#    > mvn cp target/dependency/* lib
# and assuming you have compiled the code using the directions in 
#    doc/README.md,
#    > cp target/*jar dist


# File to be tagged
TEST_IN="test/SampleInputs/testPhrase1.txt"
#inputfile="test/SampleInputs/longParagraph.txt"

# Tagged file to be created
TEST_OUT="test/SampleOutputs/NERTest.conll.tagged.txt"
#outputfile="test/SampleOutputs/NERTest.ontonotes.tagged.txt"

TEST_FILTERED="test/SampleOutputs/NERTest.conll.tagged.filtered.txt"

# Config file
CONFIG="config/conll.config"
#configfile="config/ontonotes.config"

DIFF_FILE="$TEST_FILTERED.diff"

# Classpath
cpath="."

for i in dist/*.jar; do
    cpath=${cpath}:${i}
done

for i in lib/*.jar; do
    cpath=${cpath}:${i}
done

for i in models/*.jar; do
    cpath=${cpath}:${i}
done


CMD="java -classpath $cpath -Xmx4g edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NerTagger -annotate ${TEST_IN} ${TEST_OUT} ${CONFIG}"

echo "$0: running command '$CMD'..."

${CMD}

# Reference file
REF_FILE="test/SampleOutputs/NERTest.conll.tagged.txt.reference"


rm -f ${DIFF_FILE}

# remove logging messages with time information
egrep -v "ch.qos|c.q.l" ${TEST_OUT} > ${TEST_FILTERED}

diff ${REF_FILE} ${TEST_FILTERED} > ${DIFF_FILE}


if [ -e ${DIFF_FILE} ]; then
    if [ -s ${DIFF_FILE} ]; then
	echo "$0: *** TEST FAILED ***: Differences found between new output and reference output.  See $DIFF_FILE for details." 
    else
	echo "$0: Test passed: no difference between new output and reference output."
    fi
else
    echo "$0: Error: couldn't find the diff file '$DIFF_FILE'."
fi


echo "$0: done."



