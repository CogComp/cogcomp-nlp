#!/bin/bash

JAVA=java

BASEDIR=.
LIBDIR=$BASEDIR/target/dependency

CP=$BASEDIR/models_new:target/classes:$LIBDIR/*:config

MEMORY="-Xmx10g"

OPTIONS="-ea -cp $CP $MEMORY"

$JAVA $OPTIONS edu.illinois.cs.cogcomp.srl.testers.SRLClassifierTesterNoConstraints $*
