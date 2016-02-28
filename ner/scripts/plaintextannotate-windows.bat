set inputfile="test\SampleInputs\testPhrase1.txt"
set outputfile="test\SampleOutputs\NERTest.ontonotes.tagged.txt"
set configfile="config\ontonotes.config"
java -classpath  bin;.\dist\LbjNerTagger-2.3.jar;.\lib\commons-cli-1.2.jar;.\lib\coreUtilities-0.1.1.jar;.\lib\LBJ-2.8.2.jar;.\lib\log4j-1.2.13.jar;.\lib\lucene-core-2.4.1.jar;.\lib\stanford-ner.src.jar;.\lib\commons-configuration-1.6.jar;.\lib\curator-client-0.6.jar;.\lib\LBJLibrary-2.8.2.jar;.\lib\logback-classic-0.9.17.jar;.\lib\slf4j-api-1.6.1.jar;.\lib\commons-lang-2.5.jar;.\lib\curator-interfaces.jar;.\lib\libthrift-0.4.jar;.\lib\logback-core-0.9.17.jar;.\lib\stanford-ner.jar:bin:${CLASSPATH} -Xmx8g edu.illinois.cs.cogcomp.LbjNer.LbjTagger.NerTagger -annotate %inputfile% %outputfile% %configfile%
set outputfile="test\SampleOutputs\NERTest.ontonotes.tagged.txt"
set referencefile="test\SampleOutputs\NERTest.ontonotes.tagged.txt.reference"
fc %outputfile% %referencefile%

