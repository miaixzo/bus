/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.core.lang.Normal;

/**
 * 实现类，提供DICOM实现相关的UID和版本名称。 该类用于标识DICOM实现的唯一标识符和版本信息，符合DICOM标准中的要求。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Implementation {

    /**
     * 实现类UID，用于唯一标识此DICOM实现
     */
    private static final String IMPL_CLASS_UID = "1.3.51.0.42.1.1";

    /**
     * 实现版本名称，用于标识此DICOM实现的版本
     */
    private static final String IMPL_VERS_NAME = versionName();

    /**
     * 生成版本名称
     *
     * @return 版本名称字符串，最大长度为16个字符
     */
    private static String versionName() {
        StringBuilder sb = new StringBuilder(Normal._16);
        sb.append("miaixz-");
        sb.append(Implementation.class.getPackage().getImplementationVersion());
        return sb.substring(0, Math.min(16, sb.length()));
    }

    /**
     * 获取实现类UID
     *
     * @return 实现类UID字符串
     */
    public static String getClassUID() {
        return IMPL_CLASS_UID;
    }

    /**
     * 获取实现版本名称
     *
     * @return 实现版本名称字符串
     */
    public static String getVersionName() {
        return IMPL_VERS_NAME;
    }

}
