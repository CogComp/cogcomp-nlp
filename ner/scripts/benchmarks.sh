#!/bin/sh

###
# run a benchmark test class that evaluates the current NER models on a 
#   range of corpora.

# you need to create this directory, which must contain the benchmark 
# configuration and data:
#  <your-benchmark-dir>
#       |_<data-set-1>
#             |
#             |_config/
#             |_train/
#             |_test/
# 
# config can contain multiple configurations
# for each, a benchmark test will be conducted by training on the data in
#     train/, and evaluating on the data in test/.


DATADIR=benchmarkData
DIST=target
LIB=target/dependency

if [ ! -e $DIST ]; then 
    mvn install -DskipTests=true
fi

if [ ! -e $LIB ]; then
    mvn dependency:copy-dependencies
fi



# Classpath
cpath=".:target/test-classes"

for JAR in `ls $DIST/*jar`; do
    cpath="$cpath:$JAR"
done

for JAR in `ls $LIB/*jar`; do
    cpath="$cpath:$JAR"
done

CMD="java -classpath  ${cpath} -Xmx12g edu.illinois.cs.cogcomp.ner.NerBenchmark -d $DATADIR $1 $2 $3"

echo "$0: running command '$CMD'..."


$CMD
