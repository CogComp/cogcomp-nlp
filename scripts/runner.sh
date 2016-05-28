#!/bin/sh

cpath="target/classes:target/dependency/*:config"
DIR="/shared/corpora/transliteration/wikidata.urom"
TRAIN=$DIR/wikidata.Kannada
TEST=$DIR/wikidata.Kannada

head -n 200 $DIR/wikidata.Yoruba > /tmp/wikidata.ab
head -n 10000 $DIR/wikidata.Danish >> /tmp/wikidata.ab

tail -n 83 $DIR/wikidata.Yoruba > /tmp/wikidata.ab.test


TRAIN=/tmp/wikidata.ab
TEST=/tmp/wikidata.ab.test


CMD="java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.transliteration.Runner $TRAIN $TEST"
echo "Running: $CMD"
${CMD}