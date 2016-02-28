#!/bin/bash

# Fail on error
set -u
set -e

####
# Deploys the latest Maven version of the package and generates a distribution package
# from a standard CCG NLP maven project layout
# 
# WARNING: assumes you have run tests and that the code passes
#   them to a satisfactory level.

# Get the current version and remove -SNAPSHOT  from it
VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v INFO | grep -v WARNING`

## DON"T FORGET TO CHANGE VERSION IF THIS IS A NEW RELEASE!!!
PACKAGE_NAME="illinois-ner"
echo "Full package name is:  ${PACKAGE_NAME}-${VERSION}"

size=${#VERSION}
if [ ${size} -gt 15 ]; then
    >&2 echo "**********************************************************"
    >&2 echo "ERROR: SOMETHING'S PROBABLY WRONG WITH THE VERSION NUMBER!"
    >&2 echo "**********************************************************"
    echo "VERSION var is: ${VERSION}"
    exit
fi

## Deploy the Maven release
echo "Deploying the Maven project... "
mvn lbjava:clean lbjava:compile
# skip tests as the time constraint is a bit tight on some machines
mvn package -DskipTests=true
mvn dependency:copy-dependencies
mvn javadoc:jar #deploy


## Generate the distribution package
echo -n "Generating the distribution package ..."

## Create a temporary directory
TEMP_DIR="temp34676"
PACKAGE_DIR="${TEMP_DIR}/${PACKAGE_NAME}-${VERSION}"


if [ -d "${PACKAGE_DIR}" ]; then
# check if user wants to delete it.
read -p "${PACKAGE_DIR} exists. Delete it before continuing?  " yn
case $yn in
    [Yy]* ) echo "Removing ${PACKAGE_DIR}"; rm -rf ${PACKAGE_DIR}; break;;
    [Nn]* ) exit;;
    * ) echo "Please answer yes or no.";;
esac
fi


# create the directory structure
mkdir -p ${PACKAGE_DIR}/lib
mkdir ${PACKAGE_DIR}/dist
mkdir -p ${PACKAGE_DIR}/doc/javadoc
mkdir -p ${PACKAGE_DIR}/src/lbj
mkdir ${PACKAGE_DIR}/test
mkdir ${PACKAGE_DIR}/scripts
mkdir ${PACKAGE_DIR}/config
mkdir ${PACKAGE_DIR}/models

# actually move files over
mv target/${PACKAGE_NAME}-${VERSION}.jar ${PACKAGE_DIR}/dist/
mv target/${PACKAGE_NAME}-${VERSION}-sources.jar ${PACKAGE_DIR}/src/
cp src/main/lbj/LbjTagger.lbj ${PACKAGE_DIR}/src/lbj/
mv target/${PACKAGE_NAME}-${VERSION}-javadoc.jar ${PACKAGE_DIR}/doc/javadoc
mv target/dependency/*model*jar ${PACKAGE_DIR}/models
mv target/dependency/* ${PACKAGE_DIR}/lib/
cp doc/* ${PACKAGE_DIR}/doc

cp pom.xml ${PACKAGE_DIR}


# copy scripts over
cd scripts
cp  plaintextannotate-linux.sh plaintextannotate-windows.bat runBenchMarkTest.sh startServer.sh testClient.sh train.sh ../${PACKAGE_DIR}/scripts
cd ..

# copy test data and config files over.
cp -r test/* ${PACKAGE_DIR}/test
cp config/* ${PACKAGE_DIR}/config

# zip up the final product
cd ${TEMP_DIR}
zip -r ../${PACKAGE_NAME}-${VERSION}.zip ${PACKAGE_NAME}-${VERSION}
cd ..

rm -rf ${TEMP_DIR}
echo "Distribution package created: ${PACKAGE_NAME}-${VERSION}.zip"