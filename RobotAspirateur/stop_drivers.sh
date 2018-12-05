#!/bin/bash

numProc=`ps -eaf | grep RobotAspirateur | grep -v grep | awk '{print $2}'`
echo "arret du processus RobotAspirateur : $numProc"
kill $numProc


exit 0
