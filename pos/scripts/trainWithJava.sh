#!/bin/bash

###
# Trains the models directly with the class files that LBJ creates
# see POSTrain.java for more details. 
# 
classDir=target/classes/
classpath="${classDir}"
data="/shared/corpora/corporaWeb/written/eng/POS/00-18.br"
modelDir="models/edu/illinois/cs/cogcomp/lbj/pos"
LIB=target/dependency


mkdir -p $modelDir

for JAR in `ls $LIB`; do
    classpath="${classpath}:$LIB/$JAR"
done

MAIN=edu.illinois.cs.cogcomp.lbj.pos.POSTrain


CMD="java -Xmx500m -cp $classpath $MAIN $data $modelDir"

echo "$0: running command '$CMD'..."

$CMD

echo "$0: done."
