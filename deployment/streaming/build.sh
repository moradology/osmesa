#!/bin/bash

set -e

touch docker/osmesa-analytics.jar
rm -rf docker/osmesa-analytics.jar

cd ../../src
sbt "project analytics" clean
sbt "project analytics" assembly
cp analytics/target/scala-2.11/osmesa-analytics.jar ../deployment/streaming/docker/osmesa-analytics.jar

cd ../deployment/streaming/docker
docker-compose build .
