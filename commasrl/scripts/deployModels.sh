#!/bin/sh

VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v 'INFO'`

if [ ! -e "target/classes/edu/illinois/cs/cogcomp/comma/lbj/LocalCommaClassifier.lex" ]; then
    echo "Comma models not found"
    exit
fi

rm -rdf target/illinois-comma-models-${VERSION}.jar
jar cf target/illinois-comma-models-${VERSION}.jar target/classes/edu/illinois/cs/cogcomp/comma/lbj/LocalCommaClassifier.l*

echo "Compiled models to jar"

if [ -e "target/illinois-comma-models-${VERSION}.jar" ]; then
echo "Deploying illinois-comma-models-${VERSION}.jar"
mvn deploy:deploy-file \
    -Dfile=target/illinois-comma-models-${VERSION}.jar \
    -DgroupId=edu.illinois.cs.cogcomp \
    -DartifactId=illinois-comma \
    -Dversion=${VERSION} \
    -Dclassifier=models \
    -Dpackaging=jar \
    -Durl=scp://bilbo.cs.illinois.edu:/mounts/bilbo/disks/0/www/cogcomp/html/m2repo \
    -DrepositoryId=CogcompSoftware
fi