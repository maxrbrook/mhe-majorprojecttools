#!/bin/bash
set -e

echo '>>>> RUNNING COMPILE SCRIPT'
#read -p ">> PLEASE ENTER THE VERSION TO BUILD: " DIR

#cd $DIR
echo '>> COMPILING SOURCE TO .CLASS FILES'
javac -classpath '.:bin/lib/jackson/*' src/*.java -d classes -Xlint:unchecked
echo '>> CLASS FILES CREATED; CREATING .JAR'
jar cvfm bin/MajorProjectTools.jar config/Manifest.txt -C classes .
echo '>> .JAR FILE CREATED'
