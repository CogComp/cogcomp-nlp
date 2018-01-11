#mvn compile
#mvn dependency:copy-dependencies
nice java -Xmx10g -cp ./target/*:./target/dependency/* edu.illinois.cs.cogcomp.datalessclassification.ta.W2VDatalessAnnotator $@
