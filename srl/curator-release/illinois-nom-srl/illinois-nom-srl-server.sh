#!/bin/sh
START=$PWD
DIRNAME=`dirname "$0"`
SCRIPTS_HOME=`cd "$DIRNAME" > /dev/null && pwd`
CURATOR_BASE=$SCRIPTS_HOME/..
CURATOR_BASE=`cd "$CURATOR_BASE" > /dev/null && pwd`
LIBDIR=$CURATOR_BASE/lib
COMPONENTDIR=$CURATOR_BASE/components
CONFIGDIR=$CURATOR_BASE/configs
MODEL_JAR=illinoisSRL-nom-models-4.0.jar
BIN_JAR=illinoisSRL-4.0.jar

COMPONENT_CLASSPATH=$COMPONENTDIR/illinois-nom-srl-server.jar:$CURATOR_BASE:$COMPONENTDIR/illinois-abstract-server.jar:$COMPONENTDIR/curator-interfaces.jar

MODEL_CLASSPATH=$LIBDIR/$MODEL_JAR

LIB_CLASSPATH=$CURATOR_BASE:$LIBDIR/$BIN_JAR:$LIBDIR/commons-cli-1.2.jar:$LIBDIR/JLIS-core-0.5.jar:$LIBDIR/JLIS-multiclass-0.5.jar:$LIBDIR/LBJ-2.8.2.jar:$LIBDIR/LBJLibrary-2.8.2.jar:$LIBDIR/brown-clusters-1.0.jar:$LIBDIR/commons-codec-1.8.jar:$LIBDIR/commons-collections-3.2.1.jar:$LIBDIR/commons-configuration-1.6.jar:$LIBDIR/commons-lang-2.5.jar:$LIBDIR/commons-logging-1.1.1.jar:$LIBDIR/coreUtilities-0.1.8.jar:$LIBDIR/curator-interfaces-0.7.jar:$LIBDIR/edison-0.5.jar:$LIBDIR/gson-2.2.4.jar:$LIBDIR/httpclient-4.1.2.jar:$LIBDIR/httpcore-4.1.3.jar::$LIBDIR/inference-0.3.jar:$LIBDIR/jgrapht-0.8.3.jar:$LIBDIR/jwnl-1.4_rc3.jar:$LIBDIR/libthrift-0.8.0.jar:$LIBDIR/logback-classic-0.9.28.jar:$LIBDIR/logback-core-0.9.28.jar:$LIBDIR/slf4j-api-1.6.1.jar:$LIBDIR/snowball-1.0.jar:$LIBDIR/trove4j-3.0.3.jar:$LIBDIR/verb-nom-data-1.0.jar


CLASSPATH=$COMPONENT_CLASSPATH:$LIB_CLASSPATH:$MODEL_CLASSPATH:$CONFIGDIR

cd $CURATOR_BASE

echo "LIBDIR: $LIBDIR"

CMD="java -cp $CLASSPATH -Dhome=$CURATOR_BASE -Xmx4G edu.illinois.cs.cogcomp.annotation.server.IllinoisNomSRLServer -c $CONFIGDIR/srl-config.properties  $@"


echo $CMD
exec $CMD
cd $START
