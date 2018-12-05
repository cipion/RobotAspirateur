#!/bin/bash

cd /home/RobotAspirateur/DriverCapteur/bin/

nohup ./CapteurRobotAspirateur &

cd /home/RobotAspirateur/DriverMoteur/bin/
nohup ./RobotAspirateur &

exit 0
