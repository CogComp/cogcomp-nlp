#!/usr/bin/env bash
TESTFILE=src/test/resources/testIn.txt
OUTFILE=testOut.txt

mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkerDemo -Dexec.args="$TESTFILE $OUTFILE"

echo "Input: "
cat $TESTFILE
echo "Output: "
cat $OUTFILE
