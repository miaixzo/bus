/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.compare;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.xyz.CompareKit;

import java.io.Serializable;

/**
 * 版本比较器
 * 比较两个版本的大小
 * 排序时版本从小到大排序，即比较时小版本在前，大版本在后
 * 支持如：1.3.20.8，6.82.20160101，8.5a/8.5c等版本形式
 * 参考：java.lang.module.ModuleDescriptor.Version
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VersionCompare extends NullCompare<String> implements Serializable {

    /**
     * 单例
     */
    public static final VersionCompare INSTANCE = new VersionCompare();
    private static final long serialVersionUID = -1L;

    /**
     * 默认构造
     */
    public VersionCompare() {
        this(false);
    }

    /**
     * 默认构造
     *
     * @param nullGreater 是否{@code null}最大，排在最后
     */
    public VersionCompare(final boolean nullGreater) {
        super(nullGreater, (VersionCompare::compareVersion));
    }

    /**
     * 比较两个版本
     * null版本排在最小：即：
     * <pre>
     * compare(null, "v1") &lt; 0
     * compare("v1", "v1")  = 0
     * compare(null, null)   = 0
     * compare("v1", null) &gt; 0
     * compare("1.0.0", "1.0.2") &lt; 0
     * compare("1.0.2", "1.0.2a") &lt; 0
     * compare("1.13.0", "1.12.1c") &gt; 0
     * compare("V0.0.20170102", "V0.0.20170101") &gt; 0
     * </pre>
     *
     * @param version1 版本1
     * @param version2 版本2
     */
    private static int compareVersion(final String version1, final String version2) {
        return CompareKit.compare(Version.of(version1), Version.of(version2));
    }

}
