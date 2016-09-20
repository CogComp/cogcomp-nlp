#!/bin/bash
mvn clean compile
mvn -q dependency:copy-dependencies

CP=target/classes:config:target/dependency/*

MEMORY="-Xmx8g"

OPTIONS="-ea $MEMORY -cp $CP "

MAINCLASS=edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler

java $OPTIONS $MAINCLASS config/srl-config.properties
