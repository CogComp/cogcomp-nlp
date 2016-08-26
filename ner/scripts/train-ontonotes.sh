#!/bin/sh
# generate/update the binary files and dependencies
DATA_ROOT="data/GoldData/Ontonotes/ColumnFormat"

train="$DATA_ROOT/TrainAndDev/"
test="$DATA_ROOT/Test/"

# you will probably want to create a new config file
#configFile="config/conll.config"

configFile="config/ontonotes.config"

# Classpath
cpath="target/classes:target/dependency/*:data/NewModels/ontonotes/*:config"

CMD="java -classpath  ${cpath} -Xmx20g edu.illinois.cs.cogcomp.ner.NerTagger -train $train -test $test -c $configFile"

echo "$0: running command '$CMD'..."

${CMD}
