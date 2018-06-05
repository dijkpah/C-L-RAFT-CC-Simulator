#!/bin/bash

if [ "$#" == 0 ]; then
	java -jar simulator.jar -m=unstructured
else
  mkdir -p ./logs/
  for var in "$@"
  do
	filename=$(basename "$var" .xlsx)
    java -jar simulator.jar -m=unstructured "$var" > "./logs/$filename.log"
  done
fi
