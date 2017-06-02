#!/bin/bash -e

export MAVEN_OPTS="-Xmx35g"

mvn -pl external compile  exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.pipeline.server.ExternalAnnotatorsServer -Dexec.args="$*" # -DargLine="-Xmx10g"
