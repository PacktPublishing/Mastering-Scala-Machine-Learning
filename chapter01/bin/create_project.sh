#!/bin/sh

mkdir -p src/{main,test}/{java,resources,scala}
mkdir lib project target

# create an initial build.sbt file
cat > build.sbt << EOF
name := "Exploratory Data Analysis"

version := "1.0"
scalaVersion := "2.11.7"
EOF

cat > project/build.properties << EOF
sbt.version=0.13.9
EOF
