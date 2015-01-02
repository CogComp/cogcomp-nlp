#!/bin/bash -e


VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v 'INFO'`

tmpdir=tmp-Srl-verb-$RANDOM

rm -rdf $tmpdir
mkdir -p $tmpdir/models

cp ./models/Verb* $tmpdir/models
cp ./models/lexicon.Verb.* $tmpdir/models

cd $tmpdir
rm -rdf ../target/illinoisSRL-verb-models-$VERSION.jar
jar cf ../target/illinoisSRL-verb-models-$VERSION.jar models
cd ..

rm -rdf $tmpdir


tmpdir=tmp-Srl-nom-$RANDOM

rm -rdf $tmpdir
mkdir -p $tmpdir/models

cp ./models/Nom* $tmpdir/models
cp ./models/lexicon.Nom.* $tmpdir/models

cd $tmpdir
rm -rdf ../target/illinoisSRL-nom-models-$VERSION.jar
jar cf ../target/illinoisSRL-nom-models-$VERSION.jar models
cd ..

rm -rdf $tmpdir

