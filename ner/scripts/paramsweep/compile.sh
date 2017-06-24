#!/bin/bash
learnrate=(.2 .01)
thickness=(20 200)

for i in ${learnrate[@]}; do
    for j in ${thickness[@]}; do
	NEWDIR="r"$i"-t"$j"/ner/"
        echo
        echo
        echo "Cleaning "$NEWDIR
	bash -c "cd $NEWDIR ; mvn lbjava:clean clean"
        echo "Building "$NEWDIR
	bash -c "cd $NEWDIR ; mvn package -DskipTests dependency:copy-dependencies"
    done;
done;
echo
echo
echo "DOING cogcomp-nlp/ner/"
bash -c "cd cogcomp-nlp/ner/; mvn lbjava:clean clean"
bash -c "cd cogcomp-nlp/ner/ ; mvn package -DskipTests dependency:copy-dependencies"