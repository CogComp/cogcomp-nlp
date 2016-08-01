#!/bin/sh

cpath="target/classes:target/dependency/*:config"
#DIR="/shared/corpora/transliteration/wikidata"
DIR="/shared/corpora/corporaWeb/lorelei/evaluation-20160705/LDC2016E57_LORELEI_IL3_Incident_Language_Pack_for_Year_1_Eval/set0/docs/"
TRAIN=$DIR/train.ug
TEST=$DIR/test.ug

#TRAIN="/home/mayhew2/IdeaProjects/illinois-transliteration/wikidata.deroman.Armenian"
#TEST="/home/mayhew2/IdeaProjects/illinois-transliteration/wikidata.deroman.Armenian"


#head -n 6000 $DIR/wikidata.Hebrew > /tmp/wikidata.heb

#TRAIN=/tmp/wikidata.heb
#TEST=/tmp/wikidata.heb


CMD="java -classpath  ${cpath} -Xmx8g edu.illinois.cs.cogcomp.transliteration.Runner $TRAIN $TEST"
echo "Running: $CMD"
${CMD}