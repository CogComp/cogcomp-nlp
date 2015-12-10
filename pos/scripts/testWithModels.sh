#!/bin/bash

###
# runs the POS tagger using the models you've trained on your own data, and then reports accuracy.
# see TestPOSWModels.java for more details.
#
classDir=target/classes/
classpath="${classDir}"
modelDir="models/edu/illinois/cs/cogcomp/lbj/pos"
LIBDIR="target/dependency"

for JAR in `ls $LIBDIR`; do
    classpath="$classpath:$LIBDIR/$JAR"
done

MAIN=edu.illinois.cs.cogcomp.lbj.pos.TestPOSModels

CP="$classpath"

CMD="java -Xmx500m -cp $CP $MAIN $modelDir"

echo "$0: running command '$CMD'..."

$CMD

echo "$0: done."
