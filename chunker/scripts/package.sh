#!/bin/bash

# Get the current version and remove -SNAPSHOT  from it
VERSION=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v INFO`


echo "VERSION: $VERSION"

## DON"T FORGET TO CHANGE VERSION IF THIS IS A NEW RELEASE!!!
PACKAGE_NAME="illinois-chunker"
echo "${PACKAGE_NAME}-${VERSION}"

## Create a temporary directory
TEMP_DIR="temp896536"
PACKAGE_DIR="${TEMP_DIR}/${PACKAGE_NAME}-${VERSION}"
MODEL_JAR="${PACKAGE_NAME}-${VERSION}-models.jar"

mvn dependency:copy-dependencies

mkdir -p ${PACKAGE_DIR}
mkdir ${PACKAGE_DIR}/lib
mkdir ${PACKAGE_DIR}/dist
mkdir -p ${PACKAGE_DIR}/doc/javadoc
mkdir ${PACKAGE_DIR}/src
mkdir ${PACKAGE_DIR}/test
mkdir ${PACKAGE_DIR}/scripts
mkdir ${PACKAGE_DIR}/models

JARNAME="${PACKAGE_NAME}-${VERSION}-model.jar"
cd models
jar cf ${PACKAGE_DIR}/models/$MODEL_JAR *
cd -

mv target/${PACKAGE_NAME}-${VERSION}.jar ${PACKAGE_DIR}/dist/
mv target/${PACKAGE_NAME}-${VERSION}-sources.jar ${PACKAGE_DIR}/src/
unzip target/${PACKAGE_NAME}-${VERSION}-javadoc.jar -d ${PACKAGE_DIR}/doc/javadoc
mv target/dependency/* ${PACKAGE_DIR}/lib/
cp doc/* ${PACKAGE_DIR}/doc
cp scripts/* ${PACKAGE_DIR}/scripts
cp test/* ${PACKAGE_DIR}/test

cd ${TEMP_DIR}
zip -r ../${PACKAGE_NAME}.zip ${PACKAGE_NAME}-${VERSION}
cd ..

rm -rf ${TEMP_DIR}
echo "Distribution package created: ${PACKAGE_NAME}.zip"