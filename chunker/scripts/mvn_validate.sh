#!/usr/bin/env bash
TESTFILE=src/test/resources/testIn.txt
OUTFILE=testOut.txt
REFFILE=src/test/resources/testRefOut-demo.txt

mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkerDemo -Dexec.args="$TESTFILE $OUTFILE"

DIFFFILE=testDiff.txt
rm -f ${DIFFFILE}
diff $REFFILE $OUTFILE > $DIFFFILE

if [ -e ${DIFFFILE} ]; then
    if [ -s ${DIFFFILE} ]; then
	echo "$0: *** TEST FAILED ***: Differences found between new output and reference output.  See $OUTFILE and $DIFFFILE for details."
    else
	echo "$0: Test passed: no difference between new output and reference output."
	rm -f $DIFFFILE
	rm -f $OUTFILE
    fi
else
    echo "$0: Error: couldn't find the diff file '$DIFFFILE'."
fi
