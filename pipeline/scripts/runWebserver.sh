#!/bin/bash -e

export MAVEN_OPTS="-Xmx35g"

mvn -pl pipeline compile  exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.pipeline.server.MainServer -Dexec.args="$*" # -DargLine="-Xmx10g"
