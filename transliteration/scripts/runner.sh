#!/bin/sh

cpath="target/classes:target/dependency/*:config"
DIR="/path/to/transliteration/data"
TRAIN=$DIR/train.data
TEST=$DIR/test.data

CMD="java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.transliteration.Runner $TRAIN $TEST"
echo "Running: $CMD"
${CMD}
