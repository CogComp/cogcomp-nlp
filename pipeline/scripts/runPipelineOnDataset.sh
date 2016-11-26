#!/bin/bash

####
# Purpose: to run the Illinois NLP preprocessor on xml-formatted documents, but respect
#    char offsets in original documents. I.e., whitespace the documents.
#
# To be run from the main directory of the software. 
# Expects a config file and a directory containing files to process.
#
# Takes 3 arguments: name of configuration file (to be 
#   found in config directory), input text file,
#   and output text file.
#


echoerr() { echo "$@" 1>&2; }



CONFIGFILE=src/test/resources/xmlcorpus.properties
CORPUSNAME="TestCorpusPreprocessor"
CORPUSDIR=src/test/resources/xmlcorpus


DIST="target"
LIB="target/dependency"
CONFIG="config"


MAIN="edu.illinois.cs.cogcomp.nlp.main.RunPipeline"
FLAGS="-Xmx20g -XX:MaxPermSize=1g -Xverify:all"



CP=$DIST/*:$CONFIG

JARS=`ls $LIB`

for FILE in $JARS; do
    CP="$CP:$LIB/$FILE"
done


CMD="java $FLAGS -cp $CP $MAIN $CONFIGFILE $CORPUSNAME $CORPUSDIR"

echoerr "$0: running command '$CMD'..."

$CMD

echoerr "$0: done."





