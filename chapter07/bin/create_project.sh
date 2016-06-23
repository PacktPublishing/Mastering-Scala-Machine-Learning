#!/bin/sh

mkdir -p src/{main,test}/{java,resources,scala}
mkdir lib project target

# create an initial build.sbt file
cat > build.sbt << EOF
name := "Working with Graph Algorithms"

version := "1.0"
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.assembla.scala-incubator" %% "graph-core" % "1.10.1",
  "com.assembla.scala-incubator" %% "graph-constrained" % "1.10.1",
  "com.assembla.scala-incubator" %% "graph-json" % "1.10.0",
  "net.liftweb" %% "lift-json" % "2.6.2")
EOF

cat << EOF > project/build.properties
sbt.version=0.13.9
EOF
