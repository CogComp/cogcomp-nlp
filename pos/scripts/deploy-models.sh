#!/bin/sh

if [ "$#" -ne 2 ]; then
	echo "usage: $0 <models directory> <version>"
	exit
fi

directory=$1
version=$2

if [ ! -d ${directory} ]; then
	echo "Cannot find directory $directory"
	exit
fi

file=illinois-pos-model-${version}.jar
jar cf ${file} ${directory}

m2repository=scp://legolas.cs.illinois.edu:/srv/data/cogcomp/html/m2repo
repoId=CogcompSoftware

deployOpts="-Durl=$m2repository -DrepositoryId=$repoId"

grp=edu.illinois.cs.cogcomp
artifact=illinois-pos-models

mvn deploy:deploy-file ${deployOpts} \
	-Dfile=${file} \
	-DgroupId=${grp} \
	-DartifactId=${artifact} \
	-Dversion=${version} \
	-Dpackaging=jar

rm ${file}

