#!/bin/bash


mvn -q -o compile

LD_LIBRARY_PATH=/shared/grandpa/opt/xpressmp_client_64/lib:/shared/grandpa/opt/share/gurobi405/linux64/lib

JAVA=java

#BASEDIR=$(dirname $0)
BASEDIR=.
LIBDIR=$BASEDIR/target/dependency

XPRESSMP_LIB=/shared/grandpa/opt/xpressmp_client_64/lib/xprb.jar:/shared/grandpa/opt/xpressmp_client_64/lib/xprm.jar:/shared/grandpa/opt/xpressmp_client_64/lib/xprs.jar

CP=models/verb:config:target/classes:$XPRESSMP_LIB
for file in `ls $LIBDIR`; do
    CP=$CP:"$LIBDIR/$file"
done

MEMORY="-Xmx12g -Xms12g"

OPTIONS="-ea -XX:+UseParallelGC -cp $CP $MEMORY -Xverify:all -XX:+HeapDumpOnOutOfMemoryError"

time nice $JAVA $OPTIONS edu.illinois.cs.cogcomp.srl.testers.SRLClassifierCuratorClientTester $*