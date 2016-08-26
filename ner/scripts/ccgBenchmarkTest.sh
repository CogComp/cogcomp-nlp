#!/bin/sh
# This is the script that will take as input a testfile / a directory
# with many test files that have gold standard annotations for NER,
# predict NER annotations for the test files using the prelearned model
# in the package and then evaluate the result with respect to the gold
# annotations. On CoNLL test set, using this script will give you F1
# score close to 90.5

# THIS SCRIPT IS NOT INCLUDED IN THE RELEASE PACKAGE. IT IS INTENDED FOR INTERNAL USE ONLY.

mvn compile
mvn dependency:copy-dependencies

test="/shared/corpora/ratinov2/NER/Data/GoldData/Reuters/BracketsFormatDocumentsSplitMyTokenization/Test/"

configFile="config/ner.properties"

# Classpath
cpath="target/classes:target/dependency/*"

java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.ner.NerTagger -test ${test} -r ${configFile}
