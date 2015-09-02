#!/bin/bash

if [ $# -eq 1 ];
then
    java -Xmx2g -Xss128m -jar maze-pathfinding.jar $1
else
    if [ $# -eq 2 ];
    then
        java -Xmx2g -Xss128m -jar maze-pathfinding.jar $1 $2
    else
        echo "Invalid number of input parameters. Usage:"
        echo "sh plan.sh <path_to_input_file> [<path_to_output_file>]"
        exit 1
    fi
fi