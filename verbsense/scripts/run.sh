#!/bin/bash
svn up
mvn compile 
mvn -q dependency:copy-dependencies

CP=target/classes:config:target/dependency/*

MEMORY="-Xmx25g"

OPTIONS="-ea $MEMORY -cp $CP "

MAINCLASS=edu.illinois.cs.cogcomp.newsrl.Main

time nice java $OPTIONS $MAINCLASS "$@"
