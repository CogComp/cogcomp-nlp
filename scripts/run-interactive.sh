#!/bin/bash
mvn compile 
mvn -q dependency:copy-dependencies

CP=target/classes:config:target/dependency/*

MEMORY="-Xmx25g"

OPTIONS="-ea $MEMORY -cp $CP "

MAINCLASS=edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler

time nice java $OPTIONS $MAINCLASS "$@"
