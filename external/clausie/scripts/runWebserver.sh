#!/bin/bash -e

export MAVEN_OPTS="-Xmx10g"

mvn -pl external/clausie compile  exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.pipeline.server.ClausIE_Server -Dexec.args="$*" # -DargLine="-Xmx10g"
