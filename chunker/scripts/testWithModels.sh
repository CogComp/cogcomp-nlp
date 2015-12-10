#!/bin/bash

###
# runs the Illinois Chunker using the models you've trained on your own data, and then reports accuracy.
# see TestChunkerModels.java for more details.
#
classDir=target/classes/
classpath="${classDir}:models"
MODEL_DIR="edu/illinois/cs/cogcomp/lbj/chunk"
MODEL="models/illinois-chunker-2.0.1-model.jar"
DATA="/shared/corpora/corporaWeb/written/eng/chunking/conll2000distributions/test.txt"
MODEL_NAME="Chunker"

MAIN=edu.illinois.cs.cogcomp.lbj.chunk.TestChunkerModels

LIB="target/dependency"

for JAR in `ls $LIB`; do
    classpath="${classpath}:$LIB/$JAR"
done

CP="$classpath:.:$MODEL"

#CMD="java -Xmx500m -cp $CP $MAIN $DATA $MODEL_DIR $MODEL_NAME"
CMD="java -Xmx500m -cp $CP $MAIN $DATA"

echo "$0: running command '$CMD'..."
$CMD

echo "$0: done."
