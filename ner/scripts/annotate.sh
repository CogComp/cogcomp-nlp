#!/bin/sh
#
# This scipt will annotate documents in the input directory, producing results into the output 
# directory. It takes an input directory, an output directory and configuration file
#

if [ "$#" -ne 3 ]; then
    echo "usage: $0 INPUT_DATA_DIRECTORY OUTPUT_DATA_DIRECTORY CONFIGURATION_FILE"
	exit
fi

# make sure expected directories are so, and everything required exists.
if ! [ -e "$1" ] 
then
	echo "input directory $1 did not exist!"
	exit
fi

if ! [ -e "$2" ] 
then
	echo "output directory $2 did not exist!"
	exit
fi

if ! [ -e "$3" ] 
then
	echo "configuration file $3 did not exist!"
	exit
fi

if ! [ -d "$1" ] 
then
	echo "input directory $1 is not a directory!"
	exit
fi

if ! [ -d "$2" ] 
then
	echo "output directory $2 is not a directory!"
	exit
fi

if ! [ -f "$3" ] 
then
	echo "configuration file $3 is not a regular file!"
	exit
fi

# set training directory, test directory, and config file
input=$1
output=$2
configFile=$3

# Classpath
DIST=target
LIB=target/dependency
cpath=".:target/test-classes"
for JAR in `ls $DIST/*jar`; do
    cpath="$cpath:$JAR"
done
for JAR in `ls $LIB/*jar`; do
    cpath="$cpath:$JAR"
done

CMD="java -classpath  ${cpath} -Xmx12g edu.illinois.cs.cogcomp.ner.NerTagger -annotate $input $output $configFile"

echo "$0: running command '$CMD'..."

${CMD}