#!/usr/bin/env bash
#----Please make sure "illinois-chunker-model" is removed from the dependency list in pom.xml----#

#   Specify the training set
TRAINFILE=/shared/corpora/corporaWeb/written/eng/chunking/conll2000distributions/train.txt

#   Specify the dir and name to save the resulting model
MODELDIR=model
MODELNAME=mychunker

#   Specify the training round
ROUND=5
#ROUND=50

mkdir -p $MODELDIR

mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkerTrain -Dexec.args="$TRAINFILE $MODELDIR $MODELNAME $ROUND"

#   Specify the portion of dev set
#   mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkerTrain -Dexec.args="$TRAINFILE $MODELDIR $MODELNAME $ROUND 0.2"