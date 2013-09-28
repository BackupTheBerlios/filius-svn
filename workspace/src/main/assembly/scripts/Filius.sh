#!/bin/bash
# 
# This script is meant as supportive start script for
# UNIX-like systems, e.g., Linux or Mac OS X
#

# change to directory where filius is installed
installation_path=`readlink -f $0`
cd ${installation_path%`basename ${installation_path}`}

# start filius
java -jar filius.jar $@

# change back to former directory
cd - > /dev/null
