#!/bin/bash

set -e

docker-compose -f docker-compose.cli.yml -p 9090:9090 run cli \
  java -classpath query/target/scala-2.11/osmesa-query.jar osmesa.query.Main "$@"

