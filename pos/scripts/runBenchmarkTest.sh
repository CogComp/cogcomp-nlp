#!/bin/bash

###
# runs a benchmark evaluation for the LBJPOS tagger. Default
#    is sections 22-24 of the Wall Street Journal, derived
#    from the Penn Treebank parse trees. 
# To run this script, you will need the Penn Treebank. 
# You can generate the data in the format required using
#    the script provided in scripts/generateParenPosFormat.pl.


VERSION=2.0.1
MAIN_JAR=dist/illinois-pos-${VERSION}.jar
MAIN=edu.illinois.cs.cogcomp.lbj.pos.TestPOS
LBJ=lib/LBJava-1.0.jar

TESTDATA="test/wsj-test"

CP="$MAIN_JAR:$LBJ"

CMD="java -Xmx500m -cp $CP $MAIN $TESTDATA"

echo "$0: running command '$CMD'..."

$CMD

echo "$0: done."
