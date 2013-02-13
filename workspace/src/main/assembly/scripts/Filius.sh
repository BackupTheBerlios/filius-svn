#!/bin/bash
# 
# This script is meant as supportive start script for
# UNIX-like systems, e.g., Linux or Mac OS X
#

# change to directory where filius is installed
relpath=$0
cd ${relpath%`basename $0`}

# start filius
java -jar filius.jar $@

# change back to former directory
cd - > /dev/null
