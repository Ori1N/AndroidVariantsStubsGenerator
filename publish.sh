#!/usr/bin/env bash

# set jdk 7
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_79.jdk/Contents/Home

MODULE=variantsstubsgenerator-"$1"
VERSION="$2"

# clean and build module
./gradlew :"$MODULE":clean :"$MODULE":assemble

# rename pom
cd "$MODULE"
mv "$MODULE"-*.pom "$MODULE"-"$VERSION".pom

# rename artifacts
cd build/libs
mv "$MODULE".jar "$MODULE"-"$VERSION".jar
mv "$MODULE"-sources.jar "$MODULE"-"$VERSION"-sources.jar
mv "$MODULE"-javadoc.jar "$MODULE"-"$VERSION"-javadoc.jar

cd ../../..

#revert to jdk 8
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_77.jdk/Contents/Home
