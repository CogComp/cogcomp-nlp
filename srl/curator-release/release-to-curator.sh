#!/bin/bash -e

CURATOR_HOME=/shared/trollope/curator
CURATOR_DIR=$CURATOR_HOME/dist

cd illinois-verb-srl
ant clean dist
cp ./dist/illinois-verb-srl-server.jar $CURATOR_DIR/components
cp illinois-verb-srl-server.sh $CURATOR_DIR/bin
cd ..



cd illinois-nom-srl
ant clean dist
cp ./dist/illinois-nom-srl-server.jar $CURATOR_DIR/components
cp illinois-nom-srl-server.sh $CURATOR_DIR/bin
cd ..

cp lib/* $CURATOR_DIR/lib/

cp ./config/srl-config.properties $CURATOR_DIR/configs/

