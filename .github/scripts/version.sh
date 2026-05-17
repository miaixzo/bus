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
# 1. Updating bus-bom parent versions in pom.xml files.
# 2. Updating the VERSION file.
# 3. Updating the Version class _VERSION constant.
# 4. Adding the version to native-image index.json tested-versions.
#-------------------------------------------------------------------

set -o errexit
set -o pipefail

root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)

# Display the LOGO
"$(dirname ${BASH_SOURCE[0]})"/logo.sh

if [ -z "$1" ]; then
        echo "ERROR: New version not specified. Please provide it as the first argument."
        exit 1
fi

# Get the version number without the -SNAPSHOT suffix for use elsewhere
version=${1%-SNAPSHOT}

# Replace the version number in other files
echo "Current path: ${root}"

if [ -n "$1" ];then
    old_version=$(cat "${root}"/VERSION)
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

export OLD_VERSION="${old_version}"
export NEW_VERSION="${version}"

# Replace bus-bom parent versions and the owning bus-bom/bus-parent project versions in pom.xml files.
find "${root}" -path '*/target/*' -prune -o -name 'pom.xml' -print0 | while IFS= read -r -d '' pom; do
    perl -0pi -e '
        s{(<parent>\s*<groupId>org\.miaixz</groupId>\s*<artifactId>bus-bom</artifactId>\s*<version>)[^<]+(</version>)}{$1 . $ENV{"NEW_VERSION"} . $2}eg;
        s{(<groupId>org\.miaixz</groupId>\s*<artifactId>bus-bom</artifactId>\s*<version>)[^<]+(</version>)}{$1 . $ENV{"NEW_VERSION"} . $2}eg;
        s{(<groupId>org\.miaixz</groupId>\s*<artifactId>bus-parent</artifactId>\s*<version>)[^<]+(</version>)}{$1 . $ENV{"NEW_VERSION"} . $2}eg;
    ' "${pom}"
done

# Replace the version in README files when they contain the previous release number.
for readme in "${root}"/README.md "${root}"/README_CN.md; do
    if [ -f "${readme}" ]; then
        perl -0pi -e 's/\Q$ENV{"OLD_VERSION"}\E/$ENV{"NEW_VERSION"}/g' "${readme}"
    fi
done

# Replace the _VERSION constant in Version.java files.
find "${root}" -path '*/target/*' -prune -o -name 'Version.java' -print0 | while IFS= read -r -d '' version_file; do
    perl -0pi -e 's{(public\s+static\s+final\s+String\s+_VERSION\s*=\s*")[^"]+(")}{$1 . $ENV{"NEW_VERSION"} . $2}eg' "${version_file}"
done

# Add the new version to every native-image tested-versions list without duplicating existing entries.
find "${root}" -path '*/target/*' -prune -o -name 'index.json' -print0 | while IFS= read -r -d '' index_file; do
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
done

# Save the new version number to the VERSION file
echo "$version" > "${root}"/VERSION
