#!/bin/bash

###
# Runs a benchmark evaluation for the Illinois Chunker.
# Labelled test data needs to be provided (not included)


VERSION=2.0.0
MAIN_JAR=dist/illinois-chunker-${VERSION}.jar
MAIN=edu.illinois.cs.cogcomp.lbj.chunk.ChunkTester
LBJ=lib/LBJava-1.0.jar
POS=lib/illinois-pos-2.0.0.jar

TESTDATA="test/test.txt"

CP="$MAIN_JAR:$LBJ:$POS"

CMD="java -Xmx500m -cp $CP $MAIN $TESTDATA"

echo "$0: running command '$CMD'..."

${CMD}

echo "$0: done."
