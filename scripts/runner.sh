#/bin/sh

mvn compile
mvn exec:java -Dexec.mainClass=edu.illinois.cs.cogcomp.transliteration.Runner