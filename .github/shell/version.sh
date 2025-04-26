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
# 升级版本，包括：
# 1. 升级pom.xml中的版本号
# 2. 替换README.md和version中的版本号
#------------------------------------------------

set -o errexit

pwd=$(pwd)

# show logo
"$(dirname ${BASH_SOURCE[0]})"/logo.sh

if [ -z "$1" ]; then
        echo "ERROR: 新版本不存在，请指定参数1"
        exit
fi

# 替换所有模块pom.xml中的版本
mvn versions:set -DnewVersion=$1

# 不带-SNAPSHOT的版本号，用于替换其它地方
version=${1%-SNAPSHOT}

# 替换其它地方的版本
echo "当前路径：${pwd}"

if [ -n "$1" ];then
    old_version=$(cat "${pwd}"/VERSION)
    echo "$old_version 替换为新版本 ${version}"
else
    # 参数错误，退出
    echo "ERROR: 请指定新版本！"
    exit
fi

if [ -z "$old_version" ]; then
    echo "ERROR: 旧版本不存在，请确认/VERSION中信息正确"
    exit
fi

# 替换README.md中的版本
sed -i "s/${old_version}/${version}/g" "$pwd"/README.md

# 保留新版本号
echo "$version" > "$pwd"/VERSION

