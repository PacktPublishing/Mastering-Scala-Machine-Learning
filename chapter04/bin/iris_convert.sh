#!/bin/sh

SOURCE="${BASH_SOURCE[0]}"
BIN_DIR="$( dirname "$SOURCE" )"
while [ -h "$SOURCE" ]
  do
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
    BIN_DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd )"
  done

BIN_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
cd "$BIN_DIR/../data/iris"

awk -F, '/setosa/ {print "0 1:"$1" 2:"$2" 3:"$3" 4:"$4;}; /versicolor/ {print "1 1:"$1" 2:"$2" 3:"$3" 4:"$4;}; /virginica/ {print "1 1:"$1" 2:"$2" 3:"$3" 4:"$4;};' < iris.data
