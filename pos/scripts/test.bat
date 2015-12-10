setlocal enabledelayedexpansion enableextensions

set VERSION=2.0.1
set MAIN_JAR=dist\illinois-pos-%VERSION%.jar
set MAIN=edu.illinois.cs.cogcomp.lbj.pos.POSTagPlain
set LBJ=lib\LBJava-1.0.jar
 
set TESTFILE="test\testIn-msdos.txt"

set TESTOUT="test\testOut-msdos.txt"


set CP=%MAIN_JAR;%LBJ%

echo "classpath is:"
echo %CP%

java -Xmx500m -cp %CP% %MAIN% %TESTFILE%
