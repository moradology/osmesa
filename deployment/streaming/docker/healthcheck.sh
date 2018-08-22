#!/bin/bash
set -e

LAST_FILE=/tmp/last.sequence
CURR_FILE=/tmp/current.sequence

if [ -f "$LAST_FILE" ] && [ -s "$LAST_FILE" ]
  then
    LAST_VALUE=$(cat $LAST_FILE)
  else
    LAST_VALUE=-1
fi

if [ -f "$CURR_FILE" ] && [ -s "$CURR_FILE" ]
  then
    CURR_VALUE=$(cat $CURR_FILE)
  else
    CURR_VALUE=-1
fi

if [ $LAST_VALUE -gt $CURR_VALUE ]
  then
    echo "The stream has stopped updating - not healthy"
    exit 1
  else
    echo $CURR_VALUE > $LAST_FILE
    exit 0
fi
