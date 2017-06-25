#!/bin/bash -e

export MAVEN_OPTS="-Xmx10g"

mvn -pl clausie compile  exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.pipeline.handlers.clausie.ClausIEServer -Dexec.args="$*" # -DargLine="-Xmx10g"
