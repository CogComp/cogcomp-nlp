#!/bin/bash


mvn -q -o compile

LD_LIBRARY_PATH=/shared/grandpa/opt/xpressmp_client_64/lib:/shared/grandpa/opt/share/gurobi405/linux64/lib

JAVA=java

BASEDIR=$(dirname $0)
LIBDIR=$BASEDIR/target/dependency

XPRESSMP_LIB=/shared/grandpa/opt/xpressmp_client_64/lib/xprb.jar:/shared/grandpa/opt/xpressmp_client_64/lib/xprm.jar:/shared/grandpa/opt/xpressmp_client_64/lib/xprs.jar

CP=target/classes:models:$XPRESSMP_LIB
for file in `ls $LIBDIR`; do
    CP=$CP:"$LIBDIR/$file"
done

MEMORY="-Xmx14g"

OPTIONS="-ea -cp $CP $MEMORY -Xverify:all"

time nice $JAVA $OPTIONS edu.illinois.cs.cogcomp.srl.testers.SRLSystemComparator $*