#!/bin/bash

###
# Trains the models directly with the class files that LBJ creates
#     -- assumes you have just run "mvn lbj:compile", "mvn compile" and 
##    "mvn dependency:copy-dependencies" -- so that compiled classes are in 
#     target/classes and dependencies are in target/dependency
#
# specify the model directory ('MODEL_DIR') and training data file 
#   ('TRAIN_DATA') 
# expects training data to be in column format 

classDir=target/classes/
classpath="${classDir}"
LIB="target/dependency"

TRAIN_DATA="/shared/corpora/corporaWeb/written/eng/chunking/conll2000distributions/train.txt"
MODEL_DIR="edu/illinois/cs/cogcomp/chunker/main/lbjava/"
#MODEL_NAME="illinois-chunker"

for JAR in `ls $LIB`; do
    classpath="${classpath}:$LIB/$JAR"
done

MAIN=edu.illinois.cs.cogcomp.chunker.main.ChunkerTrain
LBJ=lib/LBJava-1.0.jar

CP="$MAIN_JAR:$LBJ:$classpath"

CMD="java -Xmx2g -cp $CP $MAIN $MODEL_DIR $TRAIN_DATA"

$CMD

echo "$0: done."
