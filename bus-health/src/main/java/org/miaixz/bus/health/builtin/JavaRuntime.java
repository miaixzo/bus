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
package org.miaixz.bus.health.builtin;

import org.miaixz.bus.core.lang.System;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Platform;

import java.io.Serializable;

/**
 * 代表当前运行的JRE的信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JavaRuntime implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String JAVA_RUNTIME_NAME = Platform.get(System.RUNTIME_NAME, false);
    private final String JAVA_RUNTIME_VERSION = Platform.get(System.RUNTIME_VERSION, false);
    private final String JAVA_HOME = Platform.get(System.HOME, false);
    private final String JAVA_EXT_DIRS = Platform.get(System.EXT_DIRS, false);
    private final String JAVA_ENDORSED_DIRS = Platform.get(System.ENDORSED_DIRS, false);
    private final String JAVA_CLASS_PATH = Platform.get(System.CLASS_PATH, false);
    private final String JAVA_CLASS_VERSION = Platform.get(System.CLASS_VERSION, false);
    private final String JAVA_LIBRARY_PATH = Platform.get(System.LIBRARY_PATH, false);
    private final String SUN_BOOT_CLASS_PATH = Platform.get(System.SUN_CLASS_PATH, false);
    private final String SUN_ARCH_DATA_MODEL = Platform.get(System.SUN_DATA_MODEL, false);

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
     * 取得当前JRE的名称(取自系统属性：<code>java.runtime.name</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2： <code>"Java(TM) 2 Runtime Environment, Standard Edition"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getName() {
        return JAVA_RUNTIME_NAME;
    }

    /**
     * 取得当前JRE的版本(取自系统属性：<code>java.runtime.version</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"1.4.2-b28"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getVersion() {
        return JAVA_RUNTIME_VERSION;
    }

    /**
     * 取得当前JRE的安装目录(取自系统属性：<code>java.home</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/jre"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getHomeDir() {
        return JAVA_HOME;
    }

    /**
     * 取得当前JRE的扩展目录列表(取自系统属性：<code>java.ext.dirs</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/jre/lib/ext:..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getExtDirs() {
        return JAVA_EXT_DIRS;
    }

    /**
     * 取得当前JRE的endorsed目录列表(取自系统属性：<code>java.endorsed.dirs</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/jre/lib/endorsed:..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getEndorsedDirs() {
        return JAVA_ENDORSED_DIRS;
    }

    /**
     * 取得当前JRE的系统classpath(取自系统属性：<code>java.class.path</code>)
     *
     * <p>
     * 例如：<code>"/home/admin/myclasses:/home/admin/..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getClassPath() {
        return JAVA_CLASS_PATH;
    }

    /**
     * 取得当前JRE的系统classpath(取自系统属性：<code>java.class.path</code>)
     *
     * <p>
     * 例如：<code>"/home/admin/myclasses:/home/admin/..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String[] getClassPathArray() {
        return StringKit.splitToArray(getClassPath(), Platform.get("path.separator", false));
    }

    /**
     * 取得当前JRE的class文件格式的版本(取自系统属性：<code>java.class.version</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"48.0"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getClassVersion() {
        return JAVA_CLASS_VERSION;
    }

    /**
     * 取得当前JRE的library搜索路径(取自系统属性：<code>java.library.path</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/bin:..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getLibraryPath() {
        return JAVA_LIBRARY_PATH;
    }

    /**
     * 取得当前JRE的library搜索路径(取自系统属性：<code>java.library.path</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"/opt/jdk1.4.2/bin:..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String[] getLibraryPathArray() {
        return StringKit.splitToArray(getLibraryPath(), Platform.get("path.separator", false));
    }

    /**
     * 取得当前JRE的URL协议packages列表(取自系统属性：<code>java.library.path</code>)
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"sun.net.www.protocol|..."</code>
     * </p>
     *
     * @return 属性值, 如果不能取得(因为Java安全限制)或值不存在,则返回<code>null</code>
     */
    public final String getProtocolPackages() {
        return Platform.get("java.protocol.handler.pkgs", true);
    }

    /**
     * 将当前运行的JRE信息转换成字符串
     *
     * @return JRE信息的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        Builder.append(builder, "Java Runtime Name:      ", getName());
        Builder.append(builder, "Java Runtime Version:   ", getVersion());
        Builder.append(builder, "Java Home Dir:          ", getHomeDir());
        Builder.append(builder, "Java Extension Dirs:    ", getExtDirs());
        Builder.append(builder, "Java Endorsed Dirs:     ", getEndorsedDirs());
        Builder.append(builder, "Java Class Path:        ", getClassPath());
        Builder.append(builder, "Java Class Version:     ", getClassVersion());
        Builder.append(builder, "Java Library Path:      ", getLibraryPath());
        Builder.append(builder, "Java Protocol Packages: ", getProtocolPackages());
        return builder.toString();
    }

}
