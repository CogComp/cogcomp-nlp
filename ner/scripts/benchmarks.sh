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

CMD="java -classpath  ${cpath} -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8001 -Xmx12g edu.illinois.cs.cogcomp.ner.NerBenchmark $1 $2 $3 $4 $5"

echo "$0: running command '$CMD'..."


$CMD
