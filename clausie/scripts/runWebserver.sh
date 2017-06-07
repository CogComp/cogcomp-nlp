#!/bin/bash -e

export MAVEN_OPTS="-Xmx10g"

mvn -pl external compile  exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.pipeline.server.ClausIEServer -Dexec.args="$*" # -DargLine="-Xmx10g"
