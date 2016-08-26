#!/bin/sh
#/bin/bash
# File to be tagged
inputfile="test/SampleInputs/testPhrase1.txt"
#inputfile="test/SampleInputs/longParagraph.txt"

# Tagged file to be created
outputfile="test/SampleOutputs/NERTest.conll.tagged.txt"
#outputfile="test/SampleOutputs/NERTest.ontonotes.tagged.txt"

# Config file
configfile="config/conll.config"
#configfile="config/ontonotes.config"

mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.ner.NerTagger -Dexec.args="-annotate ${inputfile} ${outputfile} ${configfile}"