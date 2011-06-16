#!/bin/bash

echo "Delete old files..."
rm -fr distpackage;

echo "Create needed directory structure..."
mkdir distpackage
mkdir distpackage/filius
mkdir distpackage/filius/lib
mkdir distpackage/filius/hilfe

echo "Copy files..."
cp Filius.exe distpackage/filius/.
cp Filius.sh distpackage/filius/.
cp Filius.sh distpackage/filius/Filius_MacOSX.command
cp Filius_mitLog.bat distpackage/filius/.
cp filius.jar distpackage/filius/.
cp lib/*jar distpackage/filius/lib/.
cp -r hilfe/* distpackage/filius/hilfe/.
cp -r config distpackage/filius/.
cp Changelog distpackage/filius/.

echo "Remove unneeded files..."
rm -f distpackage/filius/hilfe/entwurfsansichtDetail.html
rm -f `find distpackage/filius -name '.directory'`
rm -f `find distpackage/filius -name 'Thumbs.db'`
rm -rf `find distpackage/filius -name '.svn'`

echo "Create ZIP archive..."
cd distpackage
zip -r filius.zip filius
cd -

