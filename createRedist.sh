#!/bin/bash

SKETCHBOOK=sketches
TMP=tmp
NAME=PapAR

## NAME must match
## fr.inria.papart.procam.Utils.LibraryName = "ProCam";


mkdir $TMP
mkdir $TMP/$NAME
mkdir $TMP/$NAME/library
mkdir $TMP/$NAME/examples


echo "Cleaning previous versions"
rm -rf libraries/$NAME
echo "Create archive of depedencies"
tar -zcf libs.tgz libraries


echo "Copy Library"
# Library
cp target/$NAME.jar $TMP/$NAME/library/$NAME.jar


# echo "Copy JavaCV, OpenCV and friends"
# libs are  javaCV and javaCV cppjars
# cp libs/* $NAME/library/


echo "Copy the sources"
# copy the source also
cp -R src $TMP/$NAME/
cp -R pom.xml $TMP/$NAME/

cp -R test $TMP/$NAME/

echo "Copy the JavaDoc"
cp -R target/site/apidocs $TMP/$NAME/

echo "Copy the Data"
cp -R data $TMP/$NAME/


echo "Copy Examples, Calibration & Apps"
# Examples
cp -R examples/* $TMP/$NAME/examples/


echo "Create the archive..."
cd $TMP

tar -zcf $NAME.tgz $NAME

mv $NAME.tgz  ..
cd ..


cp -r $TMP/$NAME libraries/

echo "Create full archive : Papart & Deps"
tar -zcf papar-complete.tgz libraries

echo "Clean "
rm -rf $TMP

echo "Creation OK"
