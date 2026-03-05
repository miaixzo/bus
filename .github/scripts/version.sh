#!/bin/bash

#############################################################################
#                                                                           #
# Copyright (c) 2015-2026 miaixz.org and other contributors.                #
#                                                                           #
# Licensed under the Apache License, Version 2.0 (the "License");           #
# you may not use this file except in compliance with the License.          #
# You may obtain a copy of the License at                                   #
#                                                                           #
#      https://www.apache.org/licenses/LICENSE-2.0                          #
#                                                                           #
# Unless required by applicable law or agreed to in writing, software       #
# distributed under the License is distributed on an "AS IS" BASIS,         #
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  #
# See the License for the specific language governing permissions and       #
# limitations under the License.                                            #
#                                                                           #
#############################################################################

#-------------------------------------------------------------------
# This script upgrades the project version. It includes:
# 1. Updating the version number in pom.xml files.
# 2. Replacing the version number in README.md and the VERSION file.
#-------------------------------------------------------------------

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