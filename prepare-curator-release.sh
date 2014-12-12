#!/bin/bash

svn up

VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v 'INFO'`

mvn clean package
mvn dependency:copy-dependencies

bash compileModelsToJar.sh 

LIBDIR=curator-release/lib
rm -rdf $LIBDIR/*

cp target/illinoisSRL-$VERSION.jar $LIBDIR
cp target/illinoisSRL-verb-models-$VERSION.jar $LIBDIR
cp target/illinoisSRL-nom-models-$VERSION.jar $LIBDIR

cp target/dependency/JLIS-core-0.5.jar $LIBDIR
cp target/dependency/JLIS-multiclass-0.5.jar $LIBDIR
cp target/dependency/LBJ-2.8.2.jar $LIBDIR
cp target/dependency/LBJLibrary-2.8.2.jar $LIBDIR
cp target/dependency/brown-clusters-1.0.jar $LIBDIR
cp target/dependency/commons-codec-1.8.jar $LIBDIR
cp target/dependency/commons-collections-3.2.1.jar $LIBDIR
cp target/dependency/commons-configuration-1.6.jar $LIBDIR
cp target/dependency/commons-lang-2.5.jar $LIBDIR
cp target/dependency/commons-logging-1.1.1.jar $LIBDIR
cp target/dependency/coreUtilities-0.1.8.jar $LIBDIR
cp target/dependency/curator-interfaces-0.7.jar $LIBDIR
cp target/dependency/edison-0.5.jar $LIBDIR
cp target/dependency/gson-2.2.4.jar $LIBDIR
cp target/dependency/httpclient-4.1.2.jar $LIBDIR
cp target/dependency/httpcore-4.1.3.jar $LIBDIR
cp target/dependency/inference-0.3.jar $LIBDIR
cp target/dependency/jgrapht-0.8.3.jar $LIBDIR
cp target/dependency/jwnl-1.4_rc3.jar $LIBDIR
cp target/dependency/libthrift-0.8.0.jar $LIBDIR
cp target/dependency/logback-classic-0.9.28.jar $LIBDIR
cp target/dependency/logback-core-0.9.28.jar $LIBDIR
cp target/dependency/slf4j-api-1.6.1.jar $LIBDIR
cp target/dependency/snowball-1.0.jar $LIBDIR
cp target/dependency/trove4j-3.0.3.jar $LIBDIR
cp target/dependency/verb-nom-data-1.0.jar $LIBDIR

