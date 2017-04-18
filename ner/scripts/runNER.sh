#!/bin/sh

###
# Run the NER command line interface. Execute this binary and you will be
# presented with simple menu driven command line interface allowing you
# to set input and output directories or files, or to enable input and output
# to standard input and standard output. You can also change or set any
# property.
#
# Usage:
#  $ ./scripts/runNER.sh <optional config file>
#
# If no config file is specified, default parameters are used.
# An example config file can be found in config/ner.properties

DIST=target
LIB=target/dependency
mvn compile
# ensure the binary is built.
if [ `ls $DIST/*jar | wc -l` -eq 0 ]; then
    mvn -DskipTests=true package
fi

if [ ! -e $LIB ]; then
    mvn dependency:copy-dependencies
fi

# Build classpath
for JAR in `ls $DIST/*jar`; do
    cpath="$cpath:$JAR"
done

for JAR in `ls $LIB/*jar`; do
    cpath="$cpath:$JAR"
done

CMD="java -classpath  ${cpath} -Xms3g -Xmx10g edu.illinois.cs.cogcomp.ner.Main $1 $2"
$CMD
