/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.storage;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 存储服务工具类
 */
public class Builder {

    /**
     * 构建对象键，拼接前缀（如果存在）与路径和文件名。
     *
     * @param prefix   前缀路径
     * @param path     路径
     * @param fileName 文件名
     * @return 规范化后的对象键
     */
    public static String buildObjectKey(String prefix, String path, String fileName) {
        String normalizedPrefix = buildNormalizedPrefix(prefix);
        String normalizedPath = StringKit.isBlank(path) ? Normal.EMPTY
                : path.replaceAll("/{2,}", "/").replaceAll("^/|/$", Normal.EMPTY);

        if (StringKit.isBlank(normalizedPrefix) && StringKit.isBlank(normalizedPath)) {
            return fileName;
        } else if (StringKit.isBlank(normalizedPrefix)) {
            return normalizedPath + "/" + fileName;
        } else if (StringKit.isBlank(normalizedPath)) {
            return normalizedPrefix + "/" + fileName;
        } else {
            return normalizedPrefix + "/" + normalizedPath + "/" + fileName;
        }
    }

    /**
     * 构建规范化后的前缀路径。
     *
     * @param prefix 原始前缀路径
     * @return 规范化后的前缀路径
     */
    public static String buildNormalizedPrefix(String prefix) {
        return StringKit.isBlank(prefix) ? Normal.EMPTY
                : prefix.replaceAll("/{2,}", "/").replaceAll("^/|/$", Normal.EMPTY);
    }

}