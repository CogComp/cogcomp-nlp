#!/usr/bin/env bash

#   Specify the dir and name to save the resulting model
MODELDIR=model
MODELNAME=mychunker

#   Specify the training round
ROUND=5
#ROUND=50

export MAVEN_OPTS="-Xmx35g"

mkdir -p $MODELDIR

mvn compile exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.Test
