#!/bin/sh

cat << EOF | g8 scalatra/scalatra-sbt
org.akozlov.examples
Advanced Model Monitoring
1.0.0
ServletWithMetrics
org.akozlov.examples.chapter10



EOF

mv advanced-model-monitoring/{README.md,sbt,project} .
rm -rf advanced-model-monitoring
