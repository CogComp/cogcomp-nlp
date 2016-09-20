rem Moving to root directory
cd ..

rem Compiling the source code
call mvn clean compile

rem Copying dependencies
call mvn -q dependency:copy-dependencies

rem Starting the SRL system
java -Xmx8g -cp target\classes;target\dependency\* edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler config\srl-config.properties