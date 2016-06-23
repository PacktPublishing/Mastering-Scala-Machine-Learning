#!/bin/sh

mkdir -p src/{main,test}/{java,resources,scala}
mkdir lib project target

# create an initial build.sbt file
cat << EOF > build.sbt
name := "Integrating Scala with R and Python"

version := "1.0"
scalaVersion := "2.11.7"

libraryDependencies += "org.python" % "jython-standalone" % "2.7.0"
EOF

cat << EOF > project/build.properties
sbt.version=0.13.9
EOF
