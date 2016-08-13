#!/bin/bash

####
# Purpose: to run the Illinois NLP preprocessor. 
#
# To be run from the main directory of the software. 
# Expects a config file and a plain text file to process.
# 
# Expects to find logback.xml in config directory. 
#
# Takes 3 arguments: name of configuration file (to be 
#   found in config directory), input text file,
#   and output text file.
#


echoerr() { echo "$@" 1>&2; }



CONFIG_FILE=
TEXT_IN_FILE=
TEXT_OUT_FILE=
SER_OUT_FILE=

if [ $# -ne 4 ]; then
    echo "Usage: $0 configFile textInFile textOutFile serOutFile"
    exit -1
else
    CONFIG_FILE=$1
    TEXT_IN_FILE=$2
    TEXT_OUT_FILE=$3
    SER_OUT_FILE=$4
fi

DIST="dist"
LIB="lib"
CONFIG="config"

MAINJAR="illinois-nlp-pipeline-$VERSION.jar"
MAIN="edu.illinois.cs.cogcomp.nlp.main.PreprocessorTester"
FLAGS="-Xmx12g -XX:MaxPermSize=1g -Xverify:all"


CP=$DIST/*:$CONFIG

JARS=`ls $LIB`

for FILE in $JARS; do
    CP="$CP:$LIB/$FILE"
done


CMD="java $FLAGS -cp $CP $MAIN $CONFIG_FILE $TEXT_IN_FILE $TEXT_OUT_FILE $SER_OUT_FILE"

echoerr "$0: running command '$CMD'..."

$CMD

echoerr "$0: done."





