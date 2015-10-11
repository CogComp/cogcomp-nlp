#!/bin/bash
# mvn clean compile
# mvn -q dependency:copy-dependencies

CP=target/classes:config:target/dependency/*

MEMORY="-Xmx100g"

OPTIONS="-ea $MEMORY -cp $CP "

MAINCLASS=edu.illinois.cs.cogcomp.srl.Main

time nice java $OPTIONS $MAINCLASS "$@"
