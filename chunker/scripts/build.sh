classDir=target/classes/
lbjDir=src/main/java/
lbjsrcDir=src/main/lbj
dataDir=chunking
classpath="-cp target/dependency:target/dependency/*:${classDir}:${lbjDir}:${dataDir}"
javacArguments="-sourcepath src/main/java -d ${classDir}"
lbjArguments="-gsp ${lbjDir}"
javaArguments="-XX:MaxPermSize=1g -Xmx8g ${classpath}"
runLBJ="nice time java ${javaArguments} edu.illinois.cs.cogcomp.lbjava.Main ${javacArguments} ${lbjArguments}"
echo $classpath
echo "---> chunker" && ${runLBJ} ${lbjsrcDir}/chunk.lbj