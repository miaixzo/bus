/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.CompareKit;

/**
 * 字符串版本表示，用于解析版本号的不同部分并比较大小。 来自：java.lang.module.ModuleDescriptor.Version
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Version implements Comparable<Version>, Serializable {

    /**
     * 版本信息
     */
    public static final String _VERSION = "8.0.8";

    private static final long serialVersionUID = -1L;
    private final String version;
    private final List<Object> sequence;
    private final List<Object> pre;
    private final List<Object> build;

    /**
     * 版本对象，格式：tok+ ( '-' tok+)? ( '+' tok+)?，版本之间使用'.'或'-'分隔，版本号可能包含'+' 数字部分按照大小比较，字符串按照字典顺序比较。
     *
     * <ol>
     * <li>sequence: 主版本号</li>
     * <li>pre: 次版本号</li>
     * <li>build: 构建版本</li>
     * </ol>
     *
     * @param v 版本字符串
     */
    public Version(final String v) {
        Assert.notNull(v, "Null version string");
        final int n = v.length();
        if (n == 0) {
            this.version = v;
            this.sequence = Collections.emptyList();
            this.pre = Collections.emptyList();
            this.build = Collections.emptyList();
            return;
        }
        this.version = v;
        this.sequence = new ArrayList<>(4);
        this.pre = new ArrayList<>(2);
        this.build = new ArrayList<>(2);

        int i = 0;
        char c = v.charAt(i);
        // 不检查开头字符为数字，字母按照字典顺序的数字对待

        final List<Object> sequence = this.sequence;
        final List<Object> pre = this.pre;
        final List<Object> build = this.build;

        // 解析主版本
        i = takeNumber(v, i, sequence);

        while (i < n) {
            c = v.charAt(i);
            if (c == '.') {
                i++;
                continue;
            }
            if (c == Symbol.C_MINUS || c == Symbol.C_PLUS) {
                i++;
                break;
            }
            if (CharKit.isNumber(c)) {
                i = takeNumber(v, i, sequence);
            } else {
                i = takeString(v, i, sequence);
            }
        }

        if (c == Symbol.C_MINUS && i >= n) {
            return;
        }

        // 解析次版本
        while (i < n) {
            c = v.charAt(i);
            if (c >= '0' && c <= '9')
                i = takeNumber(v, i, pre);
            else
                i = takeString(v, i, pre);
            if (i >= n) {
                break;
            }
            c = v.charAt(i);
            if (c == '.' || c == Symbol.C_MINUS) {
                i++;
                continue;
            }
            if (c == Symbol.C_PLUS) {
                i++;
                break;
            }
        }

        if (c == Symbol.C_PLUS && i >= n) {
            return;
        }

        // 解析build版本
        while (i < n) {
            c = v.charAt(i);
            if (c >= '0' && c <= '9') {
                i = takeNumber(v, i, build);
            } else {
                i = takeString(v, i, build);
            }
            if (i >= n) {
                break;
            }
            c = v.charAt(i);
            if (c == '.' || c == Symbol.C_MINUS || c == Symbol.C_PLUS) {
                i++;
            }
        }
    }

    /**
     * 解析版本字符串为Version对象
     *
     * @param v 版本字符串
     * @return The resulting {@code Version}
     * @throws IllegalArgumentException 如果 {@code v} 为 {@code null}或 ""或无法解析的字符串，抛出此异常
     */
    public static Version of(final String v) {
        return new Version(v);
    }

    /**
     * 获取 Version 的版本号,版本号的命名规范
     *
     * <pre>
     * [大版本].[小版本].[发布流水号]
     * </pre>
     * 
     * 这里有点说明
     * <ul>
     * <li>大版本 - 表示API的版本,如果没有重大变化,基本上同样的大版本号,使用方式是一致的
     * <li>质量号 - alpha内部测试,beta 公测品质,RELEASE 生产品质
     * <li>小版本 - 每次发布增加1
     * </ul>
     *
     * @return 项目的版本号
     */
    /**
     * 完整版本号
     *
     * @return the agent
     */
    public static String all() {
        return _VERSION;
    }

    /**
     * 获取字符串中从位置i开始的数字，并加入到acc中 如 a123b，则从1开始，解析到acc中为[1, 2, 3]
     *
     * @param s   字符串
     * @param i   位置
     * @param acc 数字列表
     * @return 结束位置（不包含）
     */
    private static int takeNumber(final String s, int i, final List<Object> acc) {
        char c = s.charAt(i);
        int d = (c - '0');
        final int n = s.length();
        while (++i < n) {
            c = s.charAt(i);
            if (CharKit.isNumber(c)) {
                d = d * 10 + (c - '0');
                continue;
            }
            break;
        }
        acc.add(d);
        return i;
    }

    /**
     * 获取字符串中从位置i开始的字符串，并加入到acc中 字符串结束的位置为'.'、'-'、'+'和数字
     *
     * @param s   版本字符串
     * @param i   开始位置
     * @param acc 字符串列表
     * @return 结束位置（不包含）
     */
    private static int takeString(final String s, int i, final List<Object> acc) {
        final int b = i;
        final int n = s.length();
        while (++i < n) {
            final char c = s.charAt(i);
            if (c != '.' && c != Symbol.C_MINUS && c != Symbol.C_PLUS && !(c >= '0' && c <= '9')) {
                continue;
            }
            break;
        }
        acc.add(s.substring(b, i));
        return i;
    }

    @Override
    public int compareTo(final Version that) {
        int c = compareTokens(this.sequence, that.sequence);
        if (c != 0) {
            return c;
        }
        if (this.pre.isEmpty()) {
            if (!that.pre.isEmpty()) {
                return +1;
            }
        } else {
            if (that.pre.isEmpty()) {
                return -1;
            }
        }
        c = compareTokens(this.pre, that.pre);
        if (c != 0) {
            return c;
        }
        return compareTokens(this.build, that.build);
    }

    @Override
    public boolean equals(final Object ob) {
        if (!(ob instanceof Version)) {
            return false;
        }
        return compareTo((Version) ob) == 0;
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    // Take a string token starting at position i
    // Append it to the given list
    // Return the index of the first character not taken
    // Requires: s.charAt(i) is not '.'
    //

    @Override
    public String toString() {
        return version;
    }

    /**
     * 比较节点
     *
     * @param ts1 节点1
     * @param ts2 节点2
     * @return 比较结果
     */
    private int compareTokens(final List<Object> ts1, final List<Object> ts2) {
        final int n = Math.min(ts1.size(), ts2.size());
        for (int i = 0; i < n; i++) {
            final Object o1 = ts1.get(i);
            final Object o2 = ts2.get(i);
            if ((o1 instanceof Integer && o2 instanceof Integer) || (o1 instanceof String && o2 instanceof String)) {
                final int c = CompareKit.compare(o1, o2, null);
                if (c == 0) {
                    continue;
                }
                return c;
            }
            // Types differ, so convert number to string form
            final int c = o1.toString().compareTo(o2.toString());
            if (c == 0) {
                continue;
            }
            return c;
        }
        final List<Object> rest = ts1.size() > ts2.size() ? ts1 : ts2;
        final int e = rest.size();
        for (int i = n; i < e; i++) {
            final Object o = rest.get(i);
            if (o instanceof Integer && ((Integer) o) == 0) {
                continue;
            }
            return ts1.size() - ts2.size();
        }
        return 0;
    }

}
