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

file=illinois-chunker-model-${version}.jar
jar cf ${file} ${directory}

m2repository=scp://bilbo.cs.illinois.edu:/mounts/bilbo/disks/0/www/cogcomp/html/m2repo
repoId=CogcompSoftware

deployOpts="-Durl=$m2repository -DrepositoryId=$repoId"

grp=edu.illinois.cs.cogcomp
artifact=illinois-chunker-model

mvn deploy:deploy-file ${deployOpts} \
	-Dfile=${file} \
	-DgroupId=${grp} \
	-DartifactId=${artifact} \
	-Dversion=${version} \
	-Dpackaging=jar

rm ${file}

