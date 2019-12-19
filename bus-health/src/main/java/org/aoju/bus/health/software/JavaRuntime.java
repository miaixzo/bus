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
package org.aoju.bus.health.software;

import org.aoju.bus.core.lang.System;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Systemd;

/**
 * 代表当前运行的JRE的信息
 *
 * @author Kimi Liu
 * @version 5.3.3
 * @since JDK 1.8+
 */
public class JavaRuntime {

    private final String JAVA_RUNTIME_NAME = Systemd.get(System.RUNTIME_NAME, false);
    private final String JAVA_RUNTIME_VERSION = Systemd.get(System.RUNTIME_VERSION, false);
    private final String JAVA_HOME = Systemd.get(System.HOME, false);
    private final String JAVA_EXT_DIRS = Systemd.get(System.EXT_DIRS, false);
    private final String JAVA_ENDORSED_DIRS = Systemd.get(System.ENDORSED_DIRS, false);
    private final String JAVA_CLASS_PATH = Systemd.get(System.CLASS_PATH, false);
    private final String JAVA_CLASS_VERSION = Systemd.get(System.CLASS_VERSION, false);
    private final String JAVA_LIBRARY_PATH = Systemd.get(System.LIBRARY_PATH, false);
    private final String SUN_BOOT_CLASS_PATH = Systemd.get(System.SUN_CLASS_PATH, false);
    private final String SUN_ARCH_DATA_MODEL = Systemd.get(System.SUN_DATA_MODEL, false);

    public final String getSunBoothClassPath() {
        return SUN_BOOT_CLASS_PATH;
    }

    /**
     * JVM is 32M <code>or</code> 64M
     *
     * @return 32 <code>or</code> 64
     */
    public final String getSunArchDataModel() {
        return SUN_ARCH_DATA_MODEL;
    }

    /**
     * 取得当前JRE的名称（取自系统属性：<code>java.runtime.name</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2： <code>"Java(TM) 2 Runtime Environment, Standard Edition"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.3
     */
    public final String getName() {
        return JAVA_RUNTIME_NAME;
    }

    /**
     * 取得当前JRE的版本（取自系统属性：<code>java.runtime.version</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"1.4.2-b28"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.3
     */
    public final String getVersion() {
        return JAVA_RUNTIME_VERSION;
    }

    /**
     * 取得当前JRE的安装目录（取自系统属性：<code>java.home</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/jre"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */
    public final String getHomeDir() {
        return JAVA_HOME;
    }

    /**
     * 取得当前JRE的扩展目录列表（取自系统属性：<code>java.ext.dirs</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/jre/lib/ext:..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.3
     */
    public final String getExtDirs() {
        return JAVA_EXT_DIRS;
    }

    /**
     * 取得当前JRE的endorsed目录列表（取自系统属性：<code>java.endorsed.dirs</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/jre/lib/endorsed:..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.4
     */
    public final String getEndorsedDirs() {
        return JAVA_ENDORSED_DIRS;
    }

    /**
     * 取得当前JRE的系统classpath（取自系统属性：<code>java.class.path</code>）
     *
     * <p>
     * 例如：<code>"/home/admin/myclasses:/home/admin/..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */
    public final String getClassPath() {
        return JAVA_CLASS_PATH;
    }

    /**
     * 取得当前JRE的系统classpath（取自系统属性：<code>java.class.path</code>）
     *
     * <p>
     * 例如：<code>"/home/admin/myclasses:/home/admin/..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */
    public final String[] getClassPathArray() {
        return StringUtils.split(getClassPath(), Systemd.get("path.separator", false));
    }

    /**
     * 取得当前JRE的class文件格式的版本（取自系统属性：<code>java.class.version</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"48.0"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */
    public final String getClassVersion() {
        return JAVA_CLASS_VERSION;
    }

    /**
     * 取得当前JRE的library搜索路径（取自系统属性：<code>java.library.path</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/bin:..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getLibraryPath() {
        return JAVA_LIBRARY_PATH;
    }

    /**
     * 取得当前JRE的library搜索路径（取自系统属性：<code>java.library.path</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/bin:..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String[] getLibraryPathArray() {
        return StringUtils.split(getLibraryPath(), Systemd.get("path.separator", false));
    }

    /**
     * 取得当前JRE的URL协议packages列表（取自系统属性：<code>java.library.path</code>）
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"sun.net.www.protocol|..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getProtocolPackages() {
        return Systemd.get("java.protocol.handler.pkgs", true);
    }

    /**
     * 将当前运行的JRE信息转换成字符串
     *
     * @return JRE信息的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        Systemd.append(builder, "Java Runtime Name:      ", getName());
        Systemd.append(builder, "Java Runtime Version:   ", getVersion());
        Systemd.append(builder, "Java Home Dir:          ", getHomeDir());
        Systemd.append(builder, "Java Extension Dirs:    ", getExtDirs());
        Systemd.append(builder, "Java Endorsed Dirs:     ", getEndorsedDirs());
        Systemd.append(builder, "Java Class Path:        ", getClassPath());
        Systemd.append(builder, "Java Class Version:     ", getClassVersion());
        Systemd.append(builder, "Java Library Path:      ", getLibraryPath());
        Systemd.append(builder, "Java Protocol Packages: ", getProtocolPackages());
        return builder.toString();
    }

}
