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