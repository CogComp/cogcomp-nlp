#!/bin/sh

VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v 'INFO'`

tmpdir=tmp-Srl-verb-$RANDOM

rm -rdf ${tmpdir}
mkdir -p ${tmpdir}/models

for parser in STANFORD CHARNIAK; do
    if [ ! -e "./models/Verb.Classifier.PARSE_${parser}.lex" ]; then
        echo "$parser Verb models not found"
        continue
    fi

    cp ./models/Verb*${parser}* ${tmpdir}/models

    cd ${tmpdir}
    rm -rdf ../target/illinois-srl-models-verb-${parser}-${VERSION}.jar
    jar cf ../target/illinois-srl-models-verb-${parser}-${VERSION}.jar models
    cd ..
    rm ${tmpdir}/models/*
done
rm -rdf ${tmpdir}

tmpdir=tmp-Srl-nom-$RANDOM

rm -rdf ${tmpdir}
mkdir -p ${tmpdir}/models

for parser in STANFORD CHARNIAK; do
    if [ ! -e "./models/Nom.Classifier.PARSE_${parser}.lex" ]; then
        echo "$parser Nom models not found"
        continue
    fi

    cp ./models/Nom*${parser}* ${tmpdir}/models

    cd ${tmpdir}
    rm -rdf ../target/illinois-srl-models-nom-${parser}-${VERSION}.jar
    jar cf ../target/illinois-srl-models-nom-${parser}-${VERSION}.jar models
    cd ..
    rm ${tmpdir}/models/*
done
rm -rdf ${tmpdir}

echo "Compiled models to jars"

if [ -e "target/illinois-srl-models-nom-CHARNIAK-${VERSION}.jar" ]; then
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
fi

if [ -e "target/illinois-srl-models-nom-STANFORD-${VERSION}.jar" ]; then
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
fi

if [ -e "target/illinois-srl-models-verb-CHARNIAK-${VERSION}.jar" ]; then
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
fi

if [ -e "target/illinois-srl-models-verb-STANFORD-${VERSION}.jar" ]; then
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
fi