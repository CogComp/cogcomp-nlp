#!/bin/sh
#
# This is a simple test script. It takes three arguments, the first specifies the directory
# containing the testing data, the second specifies the format, and the last specifies the configuration file.
#

if [ "$#" -ne 3 ]; then
    echo "usage: $0 TESTING_DATA_DIRECTORY FORMAT CONFIGURATION_FILE\n(See NerTagger.java for details)"
	exit
fi

# make sure expected directories are so, and everything required exists.
if ! [ -e "$1" ] 
then
	echo "training directory $1 did not exist!"
	exit
fi

if ! [ -e "$3" ]
then
	echo "configuration file $3 did not exist!"
	exit
fi

if ! [ -d "$1" ] 
then
	echo "training directory $1 is not a directory!"
	exit
fi


if ! [ -f "$3" ]
then
	echo "configuration file $3 is not a regular file!"
	exit
fi

# set test directory, format, and config file
test=$1
format=$2
configFile=$3

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

CMD="java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.ner.NerTagger -test $test $format $configFile"

echo "$0: running command '$CMD'..."

${CMD}
