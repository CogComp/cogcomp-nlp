#!/bin/sh

if [ "$#" -ne 4 ]; then
    echo "usage: $0 {deploy|install} MODELS-DIRECTORY VERSION TYPE (=conll|ontonotes|etc.)"
	exit
fi
# deploy or install
action=$1

# The jar containing the models
directory=$2

# The model version
version=$3

# The type of the model (ontonotes or conll)
type=$4

if [ ! -d ${directory} ]; then
	echo "Cannot find directory $directory"
	exit
fi

# Create the jar file
file=illinois-ner-${version}-models-${type}.jar
jar cf ${file} ${directory}

m2repository=scp://bilbo.cs.uiuc.edu:/mounts/bilbo/disks/0/www/cogcomp/html/m2repo
repoId=CogcompSoftware

deployOpts=;
if [ ${action} == "deploy" ]; then
	deployOpts="-Durl=$m2repository -DrepositoryId=$repoId"
fi

grp=edu.illinois.cs.cogcomp
artifact=illinois-ner
classifier=models-${type}
 
mvn ${action}:${action}-file ${deployOpts} \
	-Dfile=${file} \
	-DgroupId=${grp} \
	-DartifactId=${artifact} \
	-Dversion=${version} \
	-Dclassifier=${classifier} \
	-Dpackaging=jar

  # Clean up
  rm ${file}
