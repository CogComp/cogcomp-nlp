TESTFILE=test/testIn.txt
OUTFILE=test/testOut.txt
REFFILE=test/testRefOut.txt

mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkerDemo -Dexec.args="$TESTFILE $OUTFILE"

DIFFFILE=test/testDiff.txt
rm -f ${DIFFFILE}
diff $REFFILE $OUTFILE > $DIFFFILE

if [ -e ${DIFFFILE} ]; then
    if [ -s ${DIFFFILE} ]; then
	echo "$0: *** TEST FAILED ***: Differences found between new output and reference output.  See $DIFFFILE for details." 
    else
	echo "$0: Test passed: no difference between new output and reference output."
	rm -f $DIFFFILE
    fi
else
    echo "$0: Error: couldn't find the diff file '$DIFFFILE'."
fi
