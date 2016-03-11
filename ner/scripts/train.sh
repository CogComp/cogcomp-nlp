#!/bin/sh
# generate/update the binary files and dependencies
DATA_ROOT="/shared/corpora/ccgPapersData/NER/Data/GoldData/Reuters/ColumnFormatDocumentsSplit"
#DATA_ROOT="data/Ontonotes/ColumnFormat"

train="$DATA_ROOT/TrainPlusDev/"
test="$DATA_ROOT/Test/"

# you will probably want to create a new config file
configFile="config/ner.properties"

#configFile="config/ontonotes.config"

# Classpath
cpath="target/classes:target/dependency/*:config"

CMD="java -classpath  ${cpath} -Xmx20g edu.illinois.cs.cogcomp.ner.NerTagger -train $train 
$test -c $configFile"

echo "$0: running command '$CMD'..."

${CMD}
