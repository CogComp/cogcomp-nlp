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