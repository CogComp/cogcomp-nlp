#!/bin/bash -e 
#fail on first error

BLD=1
TMPDIR="tmp9812"
NAME=""
FORMAT="zip"

function HELP {
    echo "$0: a simple script to package a standard CCG NLP (maven) project."
    echo "Required arguments:"
    echo "-N <name>:  package name (and name of distribution directory)"
    echo "\n---------\n\n"
    echo "Optional arguments:"
    echo "-b: don't build the project (assumes you already ran 'mvn install' and 'mvn dependency:copy-dependencies')"
    echo "-F <format>: compression format (must be 'zip' or 'tgz')"
    echo "-T <dirname>: temporary directory name"
}

# h: help
# b: skip maven build
# N: package name
# F: format (zip/tar.gz), default Zip
# T: tmp directory to use

while getopts bhN:F: opt; do
    case $opt in 
	h) HELP
	exit -1
	;;
	b) BLD=0
	;;
	N) NAME=$OPTARG
	;;
	F) FORMAT=$OPTARG
	;;
	T) TMPDIR=$OPTARG
	;;
	\?) echo "$O: invalid argument '$opt'.\n";
	exit -1
	;;
	:) echo "$0 requires name argument. Run with option 'h' for help info."
	exit -1
	;;
    esac
done


# '!=' -- string inequality
if [[ $FORMAT != "zip" && $OPTARG != "tgz" ]]; then
    echo "$0: Format argument can be either 'zip' (default) or 'tgz'."
    echo "Format '$FORMAT' is not handled.";
    exit -1
fi

if [  -z $NAME ]; then
    echo "$0: you must specify the package name with flag '-N' (no spaces or dodgy control characters, please...)"
    exit -1;
fi


# where the package components will be copied and then zipped

# generate the jars and library jars so they can be copied
if [ $BLD -eq 1 ]; then
    mvn install && mvn dependency:copy-dependencies
    echo "$0: building project jars and copying library jars to tmp directory '$TMPDIR'..."
fi

mkdir -p $TMPDIR

CPYDIR="$TMPDIR/$NAME"


# standard directories for CCG software distribution...
DIST=$CPYDIR/dist
LIB=$CPYDIR/lib

mkdir -p $DIST
mkdir -p $LIB


# populate the distribution dir

echo "$0: copying files for distribution..."
cp target/*jar $DIST

cp target/dependency/* $LIB



if [ -e doc ]; then
    cp -r doc/ $CPYDIR
fi

cp -r scripts/ $CPYDIR

cp -r test $CPYDIR

cp -r config $CPYDIR

cp pom.xml $CPYDIR

cp README.md $CPYDIR

cd $TMPDIR

echo "$0: zipping up the distribution files..."
zip -r $NAME.zip $NAME

cd -

echo "$0: Done creating release zip.  Moving it to '../$NAME.zip'."

mv $TMPDIR/$NAME.zip ..
rm -rf $TMPDIR

echo "$0: cleaned up. Done."