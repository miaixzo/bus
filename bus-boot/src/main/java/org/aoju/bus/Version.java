/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.consts.Symbol;

/**
 * 用于识别当前版本号和版权声明!
 * Version is Licensed under the MIT License, Version 3.0.0 (the "License")
 *
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
public class Version {

    /**
     * 是否完整模式，默认使用完整模式
     */
    private boolean complete = true;

    /**
     * 获取 Version 的版本号，版本号的命名规范
     *
     * <pre>
     * [大版本].[小版本].[发布流水号]
     * </pre>
     * <p>
     * 这里有点说明
     * <ul>
     * <li>大版本 - 表示API的版本，如果没有重大变化，基本上同样的大版本号，使用方式是一致的
     * <li>质量号 - alpha内部测试, beta 公测品质,RELEASE 生产品质
     * <li>小版本 - 每次发布增加1
     * </ul>
     *
     * @return 项目的版本号
     */

    public static String get() {
        return major() + "." + minor() + "." + stage() + "." + level();
    }

    /**
     * 主要版本号
     *
     * @return 版本号
     */
    public static String major() {
        return "3";
    }

    /**
     * 次要版本号
     *
     * @return 次要号
     */
    public static String minor() {
        return "6";
    }

    /**
     * 阶段版本号
     *
     * @return 阶段号
     */
    public static String stage() {
        return "8";
    }

    /**
     * 版本质量
     *
     * @return 质量
     */
    public static String level() {
        return "RELEASE";
    }

    /**
     * 比较2个版本号
     *
     * @param v1       v1
     * @param v2       v2
     * @param complete 是否完整的比较两个版本
     * @return (v1 < v2) ? -1 : ((v1 == v2) ? 0 : 1)
     */
    private static int compare(String v1, String v2, boolean complete) {
        // v1 null视为最小版本，排在前
        if (v1 == v2) {
            return 0;
        } else if (v1 == null) {
            return -1;
        } else if (v2 == null) {
            return 1;
        }
        // 去除空格
        v1 = v1.trim();
        v2 = v2.trim();
        if (v1.equals(v2)) {
            return 0;
        }
        String[] v1s = v1.split(Symbol.BACKSLASH + Symbol.DOT);
        String[] v2s = v2.split(Symbol.BACKSLASH + Symbol.DOT);
        int v1sLen = v1s.length;
        int v2sLen = v2s.length;
        int len = complete
                ? Math.max(v1sLen, v2sLen)
                : Math.min(v1sLen, v2sLen);

        for (int i = 0; i < len; i++) {
            String c1 = len > v1sLen || null == v1s[i] ? Normal.EMPTY : v1s[i];
            String c2 = len > v2sLen || null == v2s[i] ? Normal.EMPTY : v2s[i];

            int result = c1.compareTo(c2);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    /**
     * 不完整模式
     *
     * @return {Version}
     */
    public Version inComplete() {
        this.complete = false;
        return this;
    }

    /**
     * 比较版本号是否相同
     * <p>
     * example:
     * * Version.of("v0.3").eq("v0.4")
     *
     * @param version 字符串版本号
     * @return {boolean}
     */
    public boolean eq(String version) {
        return compare(version) == 0;
    }

    /**
     * 不相同
     * <p>
     * example:
     * * Version.of("v0.3").ne("v0.4")
     *
     * @param version 字符串版本号
     * @return {boolean}
     */
    public boolean ne(String version) {
        return compare(version) != 0;
    }

    /**
     * 大于
     *
     * @param version 版本号
     * @return 是否大于
     */
    public boolean gt(String version) {
        return compare(version) > 0;
    }

    /**
     * 大于和等于
     *
     * @param version 版本号
     * @return 是否大于和等于
     */
    public boolean gte(String version) {
        return compare(version) >= 0;
    }

    /**
     * 小于
     *
     * @param version 版本号
     * @return 是否小于
     */
    public boolean lt(String version) {
        return compare(version) < 0;
    }

    /**
     * 小于和等于
     *
     * @param version 版本号
     * @return 是否小于和等于
     */
    public boolean lte(String version) {
        return compare(version) <= 0;
    }

    /**
     * 和另外一个版本号比较
     *
     * @param version 版本号
     * @return {int}
     */
    private int compare(String version) {
        return compare(get(), version, complete);
    }

}
