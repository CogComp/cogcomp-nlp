#!/bin/bash

JAVA=java

# BASEDIR=$(dirname $0)
BASEDIR=.
LIBDIR=$BASEDIR/target/dependency

CP=$BASEDIR/models:$BASEDIR/target/classes:$BASEDIR/config
for file in `ls $LIBDIR`; do
    CP=$CP:"$LIBDIR/$file"
done

MEMORY="-Xmx10g"

OPTIONS="-ea -cp $CP $MEMORY -Xverify:all"

$JAVA $OPTIONS edu.illinois.cs.cogcomp.srl.testers.IdentifierThresholdTuner $*