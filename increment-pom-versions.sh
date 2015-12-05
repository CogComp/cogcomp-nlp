#!/bin/bash
 
#################################################################################
# This script, run from the root of a Maven single or multi-module project, will
# update the pom files to increment the build number on the version.
# If a newer version of a parent pom exists, that will also be updated to the
# current latest version.
# If desired, dependencies (all or a subset) can also be updated. This is
# generally useful for internal libraries where you'd probably want to switch to
# using the latest as soon as possible.
#
# Here are some examples as to what this will to with the version
# (existing --> after script run):
# 1.0-SNAPSHOT --> 1.1
# 1.0.0.0 --> 1.0.0.1
# 1.0.0.1 --> 1.0.0.2
# 8 --> 9
#
# Nonstandard versions (e.g. with alphabetic characters) have NOT been tested
# thoroughly with this! Test with -d (dry run) to make sure this doesn't hose
# things.
#################################################################################

# The following can contain multiple <groupId>:<artifactId> pairings. If more are
# desired, each should be space-delimited. The format of each is:
# "groupId:artifactId[:type:classifier:version]"
# See http://mojo.codehaus.org/versions-maven-plugin/use-latest-versions-mojo.html#includesList
# for more information. If this is left as an empty string, no dependency updates
# will be done.
#UPDATE_DEPENDENCIES_WITH_GROUPID="com.example*:*"
#UPDATE_DEPENDENCIES_WITH_GROUPID="com.example.foo:bar:1.*"
#UPDATE_DEPENDENCIES_WITH_GROUPID="com.example.foo:* com.example.bar:* com.example.fluffy:*"
UPDATE_DEPENDENCIES_WITH_GROUPID=""

#################################################################################

MAVEN_BIN=`which mvn`

MAVEN_VERSIONS_PLUGIN="org.codehaus.mojo:versions-maven-plugin:1.3.1"
MAVEN_VERSIONS_PLUGIN_SET_GOAL="${MAVEN_VERSIONS_PLUGIN}:set -DgenerateBackupPoms=false"
MAVEN_VERSIONS_PLUGIN_UPDATE_PARENT_GOAL="${MAVEN_VERSIONS_PLUGIN}:update-parent -DgenerateBackupPoms=false -DallowSnapshots=true"
MAVEN_VERSIONS_PLUGIN_UPDATE_DEPENDENCIES_GOAL="${MAVEN_VERSIONS_PLUGIN}:use-latest-versions -DgenerateBackupPoms=false -DallowSnapshots=false"

MAVEN_HELP_PLUGIN="org.apache.maven.plugins:maven-help-plugin:2.1.1"
MAVEN_HELP_PLUGIN_EVALUATE_VERSION_GOAL="${MAVEN_HELP_PLUGIN}:evaluate -Dexpression=project.version"

COMMIT=false

LAST_COMMIT_HASH=`git log -1 --pretty=format:"%H"`

function printUsage() {
  echo "Usage: ${0} [option(s)]"
  echo
  echo "  -h                Show this help text."
  echo "  -d                At the end also commit to the master branch of origin"
  echo "  -v new_version    Override generated version to the specific version."
  echo "                    This should be in the format: major.minor.patch.build. (e.g. 2.8.1.0)"
}

while getopts ":v:bhdj" OPT; do
  case ${OPT} in
    d)
      COMMIT=true
      ;;
    h)
      printUsage
      exit 0
      ;;
    v)
      NEXT_PROJECT_VERSION="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: -${OPTARG}" >&2
      printUsage
      exit 30
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      printUsage
      exit 1
      ;;
  esac
done

function validatePomExists() {
  CURRENT_DIRECTORY=`pwd`
  if [ -f pom.xml ] ; then
    echo "Found pom.xml file: [${CURRENT_DIRECTORY}/pom.xml]"
  else
    echo "ERROR: No pom.xml file detected in current directory [${CURRENT_DIRECTORY}]. Exiting script with error status."
    exit 50
  fi
}

function validatePom() {
  ${MAVEN_BIN} validate
  STATUS=`echo $?`
  if [ ${STATUS} -ne 0 ] ; then
    echo "ERROR: Maven POM did not validate successfully. Exiting script with error status."
    exit 40
  fi
}

function initCurrentProjectVersion() {
  echo -n "Detecting current project version number..."

  CURRENT_PROJECT_VERSION=`${MAVEN_BIN} ${MAVEN_HELP_PLUGIN_EVALUATE_VERSION_GOAL} | egrep '^[0-9\.]*(-SNAPSHOT)?$'`
  if [ -z ${CURRENT_PROJECT_VERSION} ] ; then
    echo "  ERROR: Couldn't detect current version. Validating pom in case there was a validation issue."
    validatePom
    echo "  ERROR: Couldn't detect current version. Exiting with error status."
    exit 20
  else
    echo "  Version found: [${CURRENT_PROJECT_VERSION}]"
  fi
}

function initNextProjectVersion() {
  local CLEANED=`echo ${CURRENT_PROJECT_VERSION} | sed -e 's/[^0-9][^0-9]*$//'`
  local CURRENT_BUILD_NUMBER=`echo ${CLEANED} | sed -e 's/[0-9]*\.//g'`
  local NEXT_BUILD_NUMBER=`expr ${CURRENT_BUILD_NUMBER} + 1`

  echo "Sanitized current project version: [${CLEANED}]"
  echo "Current build number in project version: [${CURRENT_BUILD_NUMBER}]"
  echo "Calculated next build number: [${NEXT_BUILD_NUMBER}]"

  if [ -z ${NEXT_PROJECT_VERSION} ] ; then
    NEXT_PROJECT_VERSION=`echo ${CLEANED} | sed -e "s/[0-9][0-9]*\([^0-9]*\)$/${NEXT_BUILD_NUMBER}/"`
  else
    echo "Version number was overridden on the command line. Using [${NEXT_PROJECT_VERSION}] to calculate next version."
    NEXT_PROJECT_VERSION="${NEXT_PROJECT_VERSION}"
  fi

  echo "Next project version: [${NEXT_PROJECT_VERSION}]"
}

function updateProjectPomsToNextVersion() {
  echo "Updating project version to [${NEXT_PROJECT_VERSION}]..."
  ${MAVEN_BIN} ${MAVEN_VERSIONS_PLUGIN_SET_GOAL} -DnewVersion=${NEXT_PROJECT_VERSION}
}

function updateToLatestParentPom() {
  echo "Updating parent pom to latest version..."
  ${MAVEN_BIN} ${MAVEN_VERSIONS_PLUGIN_UPDATE_PARENT_GOAL}
}

function updateToLatestDependencies() {
  echo "Updating dependencies to latest versions..."
  for DEPENDENCY in ${UPDATE_DEPENDENCIES_WITH_GROUPID} ; do
    echo "Updating dependencies matching [${DEPENDENCY}]..."
    ${MAVEN_BIN} ${MAVEN_VERSIONS_PLUGIN_UPDATE_DEPENDENCIES_GOAL} -DincludesList=${DEPENDENCY}
  done
}

function commitBuildNumberChanges() {
  echo "Preparing updated files for commit..."
  git status

  # add the now updated pom files
  echo "Adding pom files..."
  for POM in `find . -name pom.xml` ; do
    git add ${POM}
    echo "   - ${POM}"
  done

  echo "Committing changes..."
  local BUILD_NUMBER_CHANGES_COMMIT_MESSAGE="Auto commit from CI - incremented build number from [${CURRENT_PROJECT_VERSION}] to [${NEXT_PROJECT_VERSION}]."
  git commit -m "${BUILD_NUMBER_CHANGES_COMMIT_MESSAGE}"

  echo "Pushing changes to master..."
  git push origin master
}

#################################################################################
# Here's where the fun begins.
#################################################################################

# Make sure that there's a pom that we can do anything with.
validatePomExists

# Figure out which commit was last done and expose a variable with the branch
# name where that commit happened.
# TODO: don't do this yet... it's flaky and I need to figure out why.
#       Assume master for now.
LAST_COMMIT_BRANCH="master"

#################################################################################
# Update the project POMs with the new build number.
#################################################################################
initCurrentProjectVersion

initNextProjectVersion

updateProjectPomsToNextVersion

updateToLatestParentPom

updateToLatestDependencies


#################################################################################
# Commit/Push updated files up to the repository
#################################################################################
if [ ${COMMIT} = true ] ; then
  commitBuildNumberChanges
else
  echo "Dry run specified. Skipping commit/push process."
fi

echo "Version updated successfully!"
