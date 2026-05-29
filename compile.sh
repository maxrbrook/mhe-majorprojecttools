#!/bin/bash
set -e

echo '>>>> RUNNING COMPILE SCRIPT'
#read -p ">> PLEASE ENTER THE VERSION TO BUILD: " DIR

#cd $DIR
echo '>> CREATING sources.txt FILE'
find ./src/ -type f -name "*.java" > sources.txt
echo '>> sources.txt CREATED'
echo '>> COMPILING SOURCE TO .CLASS FILES'
javac -classpath '.:bin/lib/*' @sources.txt -d target -Xlint:unchecked
echo '>> CLASS FILES CREATED; CREATING .JAR'
jar cvfm bin/MajorProjectTools.jar config/Manifest.txt -C target .
echo '>> .JAR FILE CREATED'
