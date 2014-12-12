#!/bin/bash


mvn -q -o compile

LD_LIBRARY_PATH=/shared/grandpa/opt/xpressmp_client_64/lib:/shared/grandpa/opt/share/gurobi450/linux64/lib

JAVA=java

#BASEDIR=$(dirname $0)
BASEDIR=.
LIBDIR=$BASEDIR/target/dependency

XPRESSMP_LIB=/shared/grandpa/opt/xpressmp_client_64/lib/xprb.jar:/shared/grandpa/opt/xpressmp_client_64/lib/xprm.jar:/shared/grandpa/opt/xpressmp_client_64/lib/xprs.jar
GUROBI_LIB=/shared/grandpa/opt/share/gurobi450/linux64/lib/gurobi.jar

CP=models_new:config:target/classes:$GUROBI_LIB:$XPRESSMP_LIB:$LIBDIR/*

MEMORY="-Xmx10g -Xms8g"

OPTIONS="-ea -XX:+UseParallelGC -cp $CP $MEMORY -Xverify:none"

time nice $JAVA $OPTIONS edu.illinois.cs.cogcomp.srl.testers.SRLClassifierTester $*
