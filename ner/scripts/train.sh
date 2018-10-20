#!/bin/sh
#
# This is a simple training script. It takes 4 arguments, the first specifies the directory
# containing the training data, the second specifies the directory containing the test data,
# the third specifies the input file format, and the last argument specifies the config file.
#

if [ "$#" -ne 4 ]; then
    echo "usage: $0 TRAINING_DATA_DIRECTORY TESTING_DATA_DIRECTORY FORMAT CONFIGURATION_FILE\n(See NerTagger.java for details)"
	exit
fi

# make sure expected directories are so, and everything required exists.
if ! [ -e "$1" ] 
then
	echo "training directory $1 did not exist!"
	exit
fi

if ! [ -e "$2" ] 
then
	echo "test directory $2 did not exist!"
	exit
fi

if ! [ -e "$4" ]
then
	echo "configuration file $4 did not exist!"
	exit
fi

if ! [ -d "$1" ] 
then
	echo "training directory $1 is not a directory!"
	exit
fi

if ! [ -d "$2" ] 
then
	echo "test directory $2 is not a directory!"
	exit
fi

if ! [ -f "$4" ]
then
	echo "configuration file $4 is not a regular file!"
	exit
fi

# set training directory, test directory, format, and config file
train=$1
test=$2
format=$3
configFile=$4

# Classpath
DIST=target
LIB=target/dependency
cpath=".:target/test-classes:target/classes"
for JAR in `ls $DIST/*jar`; do
    cpath="$cpath:$JAR"
done
for JAR in `ls $LIB/*jar`; do
    cpath="$cpath:$JAR"
done

CMD="java -classpath  ${cpath} -Xmx12g edu.illinois.cs.cogcomp.ner.NerTagger -train $train $test $format $configFile"

echo "$0: running command '$CMD'..."

${CMD}
