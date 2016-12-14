#!/bin/bash -e

 mvn -pl pipeline compile  exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.pipeline.server.MainServer -DargLine="-Xmx10g"