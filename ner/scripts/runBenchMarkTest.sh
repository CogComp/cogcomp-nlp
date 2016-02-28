#!/bin/sh
# This is the script that will take as input a testfile / a directory
# with many test files that have gold standard annotations for NER,
# predict NER annotations for the test files using the prelearned model
# in the package and then evaluate the result with respect to the gold
# annotations. On CoNLL test set, using this script will give you F1
# score close to 90.5

# !!! IMPORTANT !!! 
# -- you need to set the variable 'test' below to point to some 
#    gold-standard data in column format. We include a single file
#    from the CoNLL corpus as a way to allow you to run a 
#    sanity check, but this is not a full evaluation. 

mvn lbj:clean
mvn lbj:compile
mvn compile
mvn dependency:copy-dependencies

#test="test/Test/"
#test="data/Reuters/ColumnFormatDocumentsSplit/TrainPlusDev/"
#test="data/Reuters/ColumnFormatDocumentsSplit/Test/"
#test="data/MUC7Columns/MUC7.NE.formalrun.sentences.columns.gold"
test="data/Ontonotes/ColumnFormat/Test"
#test="/Users/redman/Projects/IllinoisNER/shelley/data/GoldData/Ontonotes/ColumnFormat/Test"

#configFile="config/conll.config"

configFile="config/ontonotes.config"

# Classpath
cpath="target/classes:target/dependency/*"

java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NerTagger -test ${test} -c ${configFile}
