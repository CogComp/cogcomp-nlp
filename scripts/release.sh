#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <package-name>"
  exit
fi

# Get the current version
VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v INFO`

## DON'T FORGET TO CHANGE VERSION IF THIS IS A NEW RELEASE!!!
PACKAGE_NAME=$1

echo "The script should run the following commands for package: ${PACKAGE_NAME}-${VERSION}"

## Deploy the Maven release
echo "mvn javadoc:jar deploy"

## Update the GitLab repository (also create a tag)
echo "git tag v${VERSION} -m \"Releasing ${PACKAGE_NAME}-${VERSION}\""

echo "git push --tags"


## Generate the distribution package
echo -n "Generating the distribution package ..."

## Create a temporary directory
TEMP_DIR="temp90614"
PACKAGE_DIR="${TEMP_DIR}/${PACKAGE_NAME}-${VERSION}"

mvn dependency:copy-dependencies

mkdir -p ${PACKAGE_DIR}
mkdir ${PACKAGE_DIR}/lib
mkdir ${PACKAGE_DIR}/dist
mkdir -p ${PACKAGE_DIR}/doc/javadoc
mkdir ${PACKAGE_DIR}/src
mkdir ${PACKAGE_DIR}/scripts

mv target/${PACKAGE_NAME}-${VERSION}.jar ${PACKAGE_DIR}/dist/
mv target/${PACKAGE_NAME}-${VERSION}-sources.jar ${PACKAGE_DIR}/src/
unzip target/${PACKAGE_NAME}-${VERSION}-javadoc.jar -d ${PACKAGE_DIR}/doc/javadoc
mv target/dependency/* ${PACKAGE_DIR}/lib/
cp doc/* ${PACKAGE_DIR}/doc
cp scripts/* ${PACKAGE_DIR}/scripts

cd ${TEMP_DIR}
zip -r ../${PACKAGE_NAME}.zip ${PACKAGE_NAME}-${VERSION}
cd ..

rm -rf ${TEMP_DIR}
echo "Distribution package created: ${PACKAGE_NAME}.zip"
