#!/usr/bin/env bash
TESTFILE=test/testIn.txt
OUTFILE=test/testOut.txt

mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkerDemo -Dexec.args="$TESTFILE $OUTFILE"

echo "Input: "
cat $TESTFILE
echo "Output: "
cat $OUTFILE
