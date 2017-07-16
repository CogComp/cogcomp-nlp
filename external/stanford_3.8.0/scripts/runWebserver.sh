#!/bin/bash -e

export MAVEN_OPTS="-Xmx35g"

mvn -pl external/stanford_3.8.0 compile  exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.pipeline.server.Stanford_3_8_0_Server -Dexec.args="$*" # -DargLine="-Xmx10g"
