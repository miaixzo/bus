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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * 版本比较器
 * 比较两个版本的大小
 * 排序时版本从小到大排序,既比较时小版本在前,大版本在后
 * 支持如：1.3.20.8,6.82.20160101,8.5a/8.5c等版本形式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VersionCompare implements Comparator<String>, Serializable {

    /**
     * 单例
     */
    public static final VersionCompare INSTANCE = new VersionCompare();
    private static final long serialVersionUID = 1L;

    /**
     * 默认构造
     */
    public VersionCompare() {

    }

    /**
     * 比较两个版本
     * null版本排在最小：既：
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
    @Override
    public int compare(String version1, String version2) {
        if (version1 == version2) {
            return 0;
        }
        if (null == version1 && null == version2) {
            return 0;
        } else if (null == version1) {// null视为最小版本,排在前
            return -1;
        } else if (null == version2) {
            return 1;
        }

        final List<String> v1s = StringKit.split(version1, Symbol.C_DOT);
        final List<String> v2s = StringKit.split(version2, Symbol.C_DOT);

        int diff = 0;
        int minLength = Math.min(v1s.size(), v2s.size());// 取最小长度值
        String v1;
        String v2;
        for (int i = 0; i < minLength; i++) {
            v1 = v1s.get(i);
            v2 = v2s.get(i);
            // 先比较长度
            diff = v1.length() - v2.length();
            if (0 == diff) {
                diff = v1.compareTo(v2);
            }
            if (diff != 0) {
                //已有结果,结束
                break;
            }
        }

        return (diff != 0) ? diff : v1s.size() - v2s.size();
    }

}
