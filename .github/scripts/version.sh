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
# This script updates the release version metadata. It includes:
# 1. Updating the VERSION file.
# 2. Updating root native-image index.json tested-versions.
#
# Usage:
#   bash .github/scripts/version.sh <version>
#
# Examples:
#   bash .github/scripts/version.sh 8.6.8
#   bash .github/scripts/version.sh 8.6.8-SNAPSHOT
#
# Notes:
#   The first argument is required.
#   When the new version ends with "-SNAPSHOT", the stored project version
#   uses the release part before "-SNAPSHOT".
#   The script can be executed from any directory inside or outside the project.
#-------------------------------------------------------------------

set -o errexit
set -o pipefail

root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)

# Display the LOGO
echo
"$(dirname ${BASH_SOURCE[0]})"/logo.sh
echo

if [ -z "$1" ]; then
    echo "ERROR: New version not specified. Please provide it as the first argument."
    exit 1
fi

version=${1%-SNAPSHOT}

if [ ! -f "${root}/VERSION" ]; then
    echo "ERROR: VERSION file not found."
    exit 1
fi

current_version=$(tr -d '[:space:]' < "${root}/VERSION")

echo "Version: ${current_version} -> ${version}"

if [[ ! "${version}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Updated native-image index.json files: 0"
    echo "ERROR: Version must use the MAJOR.MINOR.PATCH format."
    exit 1
fi

if [[ ! "${current_version}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "Updated native-image index.json files: 0"
    echo "ERROR: Current VERSION must use the MAJOR.MINOR.PATCH format."
    exit 1
fi

version_greater_than_current=$(awk -v current="${current_version}" -v candidate="${version}" '
    BEGIN {
        split(current, current_parts, ".");
        split(candidate, candidate_parts, ".");
        for (i = 1; i <= 3; i++) {
            current_part = current_parts[i] + 0;
            candidate_part = candidate_parts[i] + 0;
            if (candidate_part > current_part) {
                print "true";
                exit;
            }
            if (candidate_part < current_part) {
                print "false";
                exit;
            }
        }
        print "false";
    }
')

if [ "${version_greater_than_current}" != "true" ]; then
    echo "Updated native-image index.json files: 0"
    echo "ERROR: New version ${version} must be greater than current VERSION ${current_version}."
    exit 1
fi

export NEW_VERSION="${version}"

# Update root native-image indexes only. Versioned metadata folders such as 8.5.0/index.json are not changed.
updated_indexes=0
while IFS= read -r -d '' index_file; do
    relative_path=${index_file#"${root}/"}
    if [[ ! "${relative_path}" =~ ^bus-[^/]+/src/main/resources/META-INF/native-image/org\.miaixz/[^/]+/index\.json$ ]]; then
        continue
    fi

    if grep -Fq "\"${version}\"" "${index_file}"; then
        continue
    fi

    perl -0pi -e '
        my $version = $ENV{"NEW_VERSION"};
        s{("tested-versions"\s*:\s*\[)(.*?)(\n\s*\])}{
            my ($head, $body, $tail) = ($1, $2, $3);
            if ($body =~ /"\Q$version\E"/) {
                $head . $body . $tail;
            } else {
                my $comma = $body =~ /"\s*$/ ? "," : "";
                $head . $body . $comma . "\n      \"" . $version . "\"" . $tail;
            }
        }egs;
    ' "${index_file}"
    updated_indexes=$((updated_indexes + 1))
done < <(find "${root}" -path '*/target/*' -prune -o -name 'index.json' -print0)

printf "%s\n" "${version}" > "${root}/VERSION"

echo "Updated native-image index.json files: ${updated_indexes}"
echo "Updated VERSION file: ${root}/VERSION"
