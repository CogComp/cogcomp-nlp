#!/bin/bash -e

export MAVEN_OPTS="-Xmx35g"

mvn -pl external/path-lstm compile  exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.pipeline.server.PathLSTM_Server -Dexec.args="$*" # -DargLine="-Xmx10g"
