#!/bin/sh
DIST=target
if [ ! -e $DIST ]; then
    echo "here"
    mvn install -DskipTests=true
fi


mvn exec:java -Dexec.arguments=-Xmx8g -Dexec.mainClass=edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalNormalizerBenchmark -Dexec.args="-inputFolder data/te3-platinum -outputFolder te3_chunker_illininorm"
echo "Use chunker to extract + illinois-time to normalize"
(cd tools/ && python TE3-evaluation.py ../data/te3-platinum ../te3_chunker_illininorm)

mvn exec:java -Dexec.arguments=-Xmx8g -Dexec.mainClass=edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalNormalizerBenchmark -Dexec.args="-useGoldChunk -inputFolder data/te3-platinum -outputFolder te3_goldext_illininorm"
echo "Use gold extraction + illinois-time to normalize"
(cd tools/ && python TE3-evaluation.py ../data/te3-platinum ../te3_goldext_illininorm)

mvn exec:java -Dexec.arguments=-Xmx8g -Dexec.mainClass=edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalNormalizerBenchmark -Dexec.args="-useHeidelTime -inputFolder data/te3-platinum -outputFolder te3_chunker_htnorm"
echo "Use chunker to extract + HeidelTime to normalize"
(cd tools/ && python TE3-evaluation.py ../data/te3-platinum ../te3_chunker_htnorm)

mvn exec:java -Dexec.arguments=-Xmx8g -Dexec.mainClass=edu.illinois.cs.cogcomp.temporal.normalizer.main.TemporalNormalizerBenchmark -Dexec.args="-useGoldChunk -useHeidelTime -inputFolder data/te3-platinum -outputFolder te3_goldext_htnorm"
echo "Use gold extraction + HeidelTime to normalize"
(cd tools/ && python TE3-evaluation.py ../data/te3-platinum ../te3_goldext_htnorm)