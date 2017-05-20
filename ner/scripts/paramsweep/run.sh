#!/bin/sh
#
# This is a simple test script. It takes two arguments, the first specifies the directory
# containing the testing data, the second specifies the configuration file.
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
