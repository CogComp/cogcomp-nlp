#!/bin/sh

cpath="target/classes:target/dependency/*:config"
DIR="/shared/corpora/transliteration/wikidata.urom"
#MODEL="models/probs-Hebrew.txt"
MODEL=$1

CMD="java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.transliteration.Interactive $MODEL"
echo "Running: $CMD"
${CMD}