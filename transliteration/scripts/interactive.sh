#!/bin/sh

cpath="target/classes:target/dependency/*:config"
MODEL=$1

CMD="java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.transliteration.Interactive $MODEL"
echo "Running: $CMD"
${CMD}
