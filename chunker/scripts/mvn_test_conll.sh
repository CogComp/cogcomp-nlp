#!/usr/bin/env bash
TESTFILE=test/testCoNLL.txt

#	Use the default chunker model
if [ $# -eq 0 ]; then
    mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkTester -Dexec.args="$TESTFILE"
else
#   Use the specified chunker model
    MODELDIR=$1
    MODELNAME=$2
    mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkTester -Dexec.args="$TESTFILE $MODELDIR $MODELNAME"
fi