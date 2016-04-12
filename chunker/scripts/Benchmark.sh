mvn exec:java -Dexec.arguments="-Xmx8g" -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.ChunkTester -Dexec.args="$*"
mvn exec:java -Dexec.arguments="-Xmx8g" -Dexec.mainClass=edu.illinois.cs.cogcomp.chunker.main.TrainedChunker -Dexec.args="$*"
