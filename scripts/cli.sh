#!/bin/bash

set -e

program="$1"
shift

case "$program" in
  "ingest")
    docker-compose -f docker-compose.cli.yml run cli \
      spark-submit ingest/target/scala-2.11/osmesa-ingest.jar "$@"
    ;;
  "sbt")
    docker-compose -f docker-compose.cli.yml run cli "$@"
    ;;
  "count")
    docker-compose -f docker-compose.cli.yml run cli \
      spark-submit --class osmesa.client.Count client/target/scala-2.11/osmesa-client.jar "$@"
    ;;
  "types")
    docker-compose -f docker-compose.cli.yml run cli \
      spark-submit --class osmesa.client.TypeNames client/target/scala-2.11/osmesa-client.jar "$@"
    ;;
  "cql")
    echo "CQL!"
    ;;
  *)
    echo "The first argument should be one of 'ingest' or 'sbt'."
    exit 1
    ;;
esac

exit 0
