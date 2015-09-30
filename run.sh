#mvn -q dependency:copy-dependencies
#mvn -q compile
# module load sun-jdk/1.8.0
CP="./config/:./target/classes/:./target/dependency/*"

OPTIONS="-Xss40m -ea -cp $CP"
PACKAGE_PREFIX="edu.illinois.cs.cogcomp"

MAIN="$PACKAGE_PREFIX.srl.Main"

time nice java $OPTIONS $MAIN $CONFIG_STR $*
