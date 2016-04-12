mvn exec:java -Dexec.arguments="-Xmx8g" -Dexec.mainClass=edu.illinois.cs.cogcomp.pos.TrainedPOSTagger -Dexec.args="$*"
