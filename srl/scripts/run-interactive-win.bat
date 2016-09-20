@REM
@REM This software is released under the University of Illinois/Research and Academic Use License. See
@REM the LICENSE file in the root folder for details. Copyright (c) 2016
@REM
@REM Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
@REM http://cogcomp.cs.illinois.edu/
@REM

rem Moving to root directory
cd ..

rem Compiling the source code
call mvn clean compile

rem Copying dependencies
call mvn -q dependency:copy-dependencies

rem Starting the SRL system
java -Xmx8g -cp target\classes;target\dependency\* edu.illinois.cs.cogcomp.srl.SemanticRoleLabeler config\srl-config.properties