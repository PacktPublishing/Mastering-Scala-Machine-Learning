#!/bin/sh

mkdir -p src/{main,test}/{java,resources,scala}
mkdir lib project target

# create an initial build.sbt file
cat << EOF > build.sbt
name := "Working with Unstructured Data"

version := "1.0"
scalaVersion := "2.11.7"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.1"
EOF

cat << EOF > project/build.properties
sbt.version=0.13.9
EOF
