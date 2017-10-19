#!/bin/bash

for d in $(find . -name L1r\* -print) 
do
		echo "Building "$d
		bash -c "cd $d ; mvn package -DskipTests dependency:copy-dependencies"
done;