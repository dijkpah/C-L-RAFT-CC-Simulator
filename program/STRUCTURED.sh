#!/bin/bash
cd "$(dirname "$0")"
if [ "$#" == 0 ]; then
	java -jar simulator.jar -m=structured
else
  mkdir -p ./logs/
  for var in "$@"
  do
	filename=$(basename "$var" .xlsx)
    java -jar simulator.jar -m=structured "$var" > "./logs/$filename.log"
  done
fi
