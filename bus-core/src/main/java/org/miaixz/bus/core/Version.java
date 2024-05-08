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
package org.miaixz.bus.core;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 用于识别当前版本号和版权声明!
 * Version is Licensed under the MIT License, Version 3.0.0 (the "License")
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Version {

    /**
     * 版本信息
     */
    public static final String _VERSION = "8.0.0.RELEASE";

    /**
     * JDK版本
     */
    public static final int _JVM_VERSION;

    /**
     * 是否大于等于JDK17
     */
    public static final boolean _IS_JDK17;

    /**
     * 是否Android环境
     */
    public static final boolean _IS_ANDROID;

    /**
     * 是否完整模式,默认使用完整模式
     */
    private boolean complete = true;

    static {
        // JVM版本
        _JVM_VERSION = _getJvmVersion();
        _IS_JDK17 = _JVM_VERSION >= 17;

        // JVM名称
        final String jvmName = _getJvmName();
        _IS_ANDROID = jvmName.equals("Dalvik");
    }

    /**
     * 获取 Version 的版本号,版本号的命名规范
     *
     * <pre>
     * [大版本].[小版本].[发布流水号]
     * </pre>
     * <p>
     * 这里有点说明
     * <ul>
     * <li>大版本 - 表示API的版本,如果没有重大变化,基本上同样的大版本号,使用方式是一致的
     * <li>质量号 - alpha内部测试,beta 公测品质,RELEASE 生产品质
     * <li>小版本 - 每次发布增加1
     * </ul>
     *
     * @return 项目的版本号
     */
    public static String get() {
        return _VERSION;
    }

    /**
     * 主要版本号
     *
     * @return 版本号
     */
    public static String major() {
        return StringKit.splitToArray(get(), Symbol.DOT)[0];
    }

    /**
     * 次要版本号
     *
     * @return 次要号
     */
    public static String minor() {
        return StringKit.splitToArray(get(), Symbol.DOT)[1];
    }

    /**
     * 阶段版本号
     *
     * @return 阶段号
     */
    public static String stage() {
        return StringKit.splitToArray(get(), Symbol.DOT)[2];
    }

    /**
     * 版本质量
     *
     * @return 质量
     */
    public static String level() {
        return StringKit.splitToArray(get(), Symbol.DOT)[3];
    }

    /**
     * 完整版本号
     *
     * @return the agent
     */
    public static String all() {
        return major() + minor() + stage();
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
        return ObjectKit.compare(get(), version, complete);
    }

    /**
     * 获取JVM名称
     *
     * @return JVM名称
     */
    private static String _getJvmName() {
        return System.getProperty("java.vm.name");
    }

    /**
     * 根据{@code java.specification.version}属性值，获取版本号
     *
     * @return 版本号
     */
    private static int _getJvmVersion() {
        int jvmVersion = -1;
        try {
            String javaSpecVer = System.getProperty("java.specification.version");
            if (StringKit.isNotBlank(javaSpecVer)) {
                if (javaSpecVer.startsWith("1.")) {
                    javaSpecVer = javaSpecVer.substring(2);
                }
                if (javaSpecVer.indexOf(Symbol.C_DOT) == -1) {
                    jvmVersion = Integer.parseInt(javaSpecVer);
                }
            }
        } catch (Throwable ignore) {
            // 默认JDK17
            jvmVersion = 17;
        }

        return jvmVersion;
    }

}
