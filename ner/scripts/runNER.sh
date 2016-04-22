#!/bin/sh

###
# Run the NER command line interface. Execute this binary you will be 
# presented with simple menu driven command line interface allowing users
# to set input and output directories or files, or to enable input and output
# to standard input and standard output. You can also change or set any
# property.
DATADIR=benchmarkData
DIST=target
LIB=target/dependency

# ensure the binary is built.
if [ ! -e $DIST ]; then 
    mvn install -DskipTests=true
fi
if [ ! -e $LIB ]; then
    mvn dependency:copy-dependencies
fi

# Classpath
cpath=".:target/test-classes"

for JAR in `ls $DIST/*jar`; do
    cpath="$cpath:$JAR"
done

for JAR in `ls $LIB/*jar`; do
    cpath="$cpath:$JAR"
done

CMD="java -classpath  ${cpath} -Xms2g -Xmx3g edu.illinois.cs.cogcomp.ner.Main $1"
$CMD
