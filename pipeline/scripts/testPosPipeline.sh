#!/usr/bin/env bash

cd ..
mvn install -DskipTests=true -pl pipeline
cd pipeline
mvn dependency:copy-dependencies


DIST="target"
CL="target/test-classes"
LIB="target/dependency"


MAIN="edu.illinois.cs.cogcomp.pipeline.main.ViewConstructorPipelineTest"
FLAGS="-Xmx2g  -Xverify:all"



CP=$DIST/*:$CL

JARS=`ls $LIB`

for FILE in $JARS; do
    CP="$CP:$LIB/$FILE"
done


CMD="java $FLAGS -cp $CP $MAIN"

echo "$0: running command '$CMD'..."

$CMD

echo "$0: done."

