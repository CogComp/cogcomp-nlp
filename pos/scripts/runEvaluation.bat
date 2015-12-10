setlocal enabledelayedexpansion enableextensions

set VERSION=2.0.1
set MAIN_JAR=dist\illinois-pos-%VERSION%.jar
set MAIN=edu.illinois.cs.cogcomp.lbj.pos.POSTagPlain
set LBJ=lib\LBJava-1.0.jar
 
REM change TESTDATA to point to corpus on your machine
set TESTDATA="data\22-24-msdos.br"

set CP=%MAIN_JAR;%LBJ%

echo "classpath is:"
echo %CP%

java -Xmx500m -cp %CP% %MAIN% %TESTDATA%
