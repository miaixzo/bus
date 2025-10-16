#!/bin/bash

#################################################################################
#                                                                               #
# The MIT License (MIT)                                                         #
#                                                                               #
# Copyright (c) 2015-2025 miaixz.org and other contributors.                    #
#                                                                               #
# Permission is hereby granted, free of charge, to any person obtaining a copy  #
# of this software and associated documentation files (the "Software"), to deal #
# in the Software without restriction, including without limitation the rights  #
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     #
# copies of the Software, and to permit persons to whom the Software is         #
# furnished to do so, subject to the following conditions:                      #
#                                                                               #
# The above copyright notice and this permission notice shall be included in    #
# all copies or substantial portions of the Software.                           #
#                                                                               #
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    #
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      #
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   #
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        #
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, #
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     #
# THE SOFTWARE.                                                                 #
#                                                                               #
#################################################################################

#------------------------------------------------
# This script upgrades the project version. It includes:
# 1. Updating the version number in pom.xml files.
# 2. Replacing the version number in README.md and the VERSION file.
#------------------------------------------------

set -o errexit

pwd=$(pwd)

# Display the LOGO
"$(dirname ${BASH_SOURCE[0]})"/logo.sh

if [ -z "$1" ]; then
        echo "ERROR: New version not specified. Please provide it as the first argument."
        exit 1
fi

# Set the version in all module pom.xml files
mvn versions:set -DnewVersion=$1

# Get the version number without the -SNAPSHOT suffix for use elsewhere
version=${1%-SNAPSHOT}

# Replace the version number in other files
echo "Current path: ${pwd}"

if [ -n "$1" ];then
    old_version=$(cat "${pwd}"/VERSION)
    echo "Replacing old version ${old_version} with new version ${version}"
else
    # Argument error, exit
    echo "ERROR: Please specify the new version!"
    exit 1
fi

if [ -z "$old_version" ]; then
    echo "ERROR: Old version not found. Please verify the contents of the /VERSION file."
    exit 1
fi

# Replace the version in README.md
sed -i "s/${old_version}/${version}/g" "$pwd"/README.md

# Save the new version number to the VERSION file
echo "$version" > "$pwd"/VERSION