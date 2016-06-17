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
# -- the models are read from the path "data/Models/"; this path
#    is specified in the config file specified below 

# !!! WARNING !!!
# if you just copy-dependencies like below, you will put the old models on
#   the classpath. I've commented out these commands. You'll have to do
#   somethign sensible to run on newly-trained models.
#mvn lbj:clean
#mvn lbj:compile
#mvn compile
#mvn dependency:copy-dependencies

#test="test/Test/"
#test="data/Reuters/ColumnFormatDocumentsSplit/TrainPlusDev/"
#test="data/Reuters/ColumnFormatDocumentsSplit/Test/"
#test="data/MUC7Columns/MUC7.NE.formalrun.sentences.columns.gold"
test="data/Ontonotes/ColumnFormat/Test"

#configFile="config/conll.config"
configFile="config/ontonotes.config"

# Classpath
cpath="target/classes:target/dependency/*"

java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.ner.NerTagger -test ${test} -c ${configFile}
