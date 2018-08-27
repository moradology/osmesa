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
    exit 0
  else
    echo "The stream is not updating (current segment: $CURR_VALUE; last segment: $LAST_VALUE)"
    exit 1
fi
