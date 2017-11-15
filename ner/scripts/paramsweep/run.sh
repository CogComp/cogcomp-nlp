#!/bin/sh
#
# This is a simple test script. It takes one integer argument, the number of
# processes to run simultaneously. If this argument is not provided, it will 
# assume one process for each core.
#

# Classpath
DIST=./cogcomp-nlp/ner/target
LIB=./cogcomp-nlp/ner/target/dependency
cpath=".:target/test-classes"
for JAR in `ls $DIST/*.jar`; do
    cpath="$cpath:$JAR"
done
for JAR in `ls $LIB/*.jar`; do
    cpath="$cpath:$JAR"
done

CMD="java -classpath  ${cpath} edu.illinois.cs.cogcomp.ner.ParameterSweep $1"
echo "$0: running command '$CMD'..."
${CMD}
