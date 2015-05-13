#!/bin/bash -e


VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v 'INFO'`

tmpdir=tmp-Srl-verb-$RANDOM

rm -rdf ${tmpdir}
mkdir -p ${tmpdir}/models

for parser in STANFORD CHARNIAK; do
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
    cp ./models/Nom*${parser}* ${tmpdir}/models

    cd ${tmpdir}
    rm -rdf ../target/illinois-srl-models-nom-${parser}-${VERSION}.jar
    jar cf ../target/illinois-srl-models-nom-${parser}-${VERSION}.jar models
    cd ..
    rm ${tmpdir}/models/*
done
rm -rdf ${tmpdir}