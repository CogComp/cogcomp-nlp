#!/usr/bin/env bash
 mvn exec:java -Dexec.mainClass="edu.illinois.cs.cogcomp.comma.CommaLabeler" -Dexec.args="data/infile.txt data/outfile.txt"

