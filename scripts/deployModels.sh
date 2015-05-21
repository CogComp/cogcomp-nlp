#!/bin/sh

VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v 'INFO'`

echo "Deploying illinois-srl-models-nom-CHARNIAK-${VERSION}.jar"
mvn deploy:deploy-file \
    -Dfile=target/illinois-srl-models-nom-CHARNIAK-${VERSION}.jar \
    -DgroupId=edu.illinois.cs.cogcomp \
    -DartifactId=illinois-srl \
    -Dversion=${VERSION} \
    -Dclassifier=models-nom-charniak \
    -Dpackaging=jar \
    -Durl=scp://bilbo.cs.illinois.edu:/mounts/bilbo/disks/0/www/cogcomp/html/m2repo \
    -DrepositoryId=CogcompSoftware

echo "Deploying illinois-srl-models-nom-STANFORD-${VERSION}.jar"
mvn deploy:deploy-file \
    -Dfile=target/illinois-srl-models-nom-STANFORD-${VERSION}.jar \
    -DgroupId=edu.illinois.cs.cogcomp \
    -DartifactId=illinois-srl \
    -Dversion=${VERSION} \
    -Dclassifier=models-nom-stanford \
    -Dpackaging=jar \
    -Durl=scp://bilbo.cs.illinois.edu:/mounts/bilbo/disks/0/www/cogcomp/html/m2repo \
    -DrepositoryId=CogcompSoftware

echo "Deploying illinois-srl-models-verb-CHARNIAK-${VERSION}.jar"
mvn deploy:deploy-file \
    -Dfile=target/illinois-srl-models-verb-CHARNIAK-${VERSION}.jar \
    -DgroupId=edu.illinois.cs.cogcomp \
    -DartifactId=illinois-srl \
    -Dversion=${VERSION} \
    -Dclassifier=models-verb-charniak \
    -Dpackaging=jar \
    -Durl=scp://bilbo.cs.illinois.edu:/mounts/bilbo/disks/0/www/cogcomp/html/m2repo \
    -DrepositoryId=CogcompSoftware

echo "Deploying illinois-srl-models-verb-STANFORD-${VERSION}.jar"
mvn deploy:deploy-file \
    -Dfile=target/illinois-srl-models-verb-STANFORD-${VERSION}.jar \
    -DgroupId=edu.illinois.cs.cogcomp \
    -DartifactId=illinois-srl \
    -Dversion=${VERSION} \
    -Dclassifier=models-verb-stanford \
    -Dpackaging=jar \
    -Durl=scp://bilbo.cs.illinois.edu:/mounts/bilbo/disks/0/www/cogcomp/html/m2repo \
    -DrepositoryId=CogcompSoftware