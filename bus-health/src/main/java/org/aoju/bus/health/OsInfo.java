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
package org.aoju.bus.health;

import org.aoju.bus.core.consts.System;

/**
 * 代表当前OS的信息。
 *
 * @author Kimi Liu
 * @version 5.0.0
 * @since JDK 1.8+
 */
public class OsInfo {

    private final String OS_VERSION = HealthUtils.get(System.OS_VERSION, false);
    private final String OS_ARCH = HealthUtils.get(System.OS_ARCH, false);
    private final String OS_NAME = HealthUtils.get(System.OS_NAME, false);
    private final boolean IS_OS_AIX = getOSMatches("AIX");
    private final boolean IS_OS_HP_UX = getOSMatches("HP-UX");
    private final boolean IS_OS_IRIX = getOSMatches("Irix");
    private final boolean IS_OS_LINUX = getOSMatches("Linux") || getOSMatches("LINUX");
    private final boolean IS_OS_MAC = getOSMatches("Mac");
    private final boolean IS_OS_MAC_OSX = getOSMatches("Mac OS X");
    private final boolean IS_OS_OS2 = getOSMatches("OS/2");
    private final boolean IS_OS_SOLARIS = getOSMatches("Solaris");
    private final boolean IS_OS_SUN_OS = getOSMatches("SunOS");
    private final boolean IS_OS_WINDOWS = getOSMatches("Windows");
    private final boolean IS_OS_WINDOWS_2000 = getOSMatches("Windows", "5.0");
    private final boolean IS_OS_WINDOWS_95 = getOSMatches("Windows 9", "4.0");
    private final boolean IS_OS_WINDOWS_98 = getOSMatches("Windows 9", "4.1");
    private final boolean IS_OS_WINDOWS_ME = getOSMatches("Windows", "4.9");
    private final boolean IS_OS_WINDOWS_NT = getOSMatches("Windows NT");
    private final boolean IS_OS_WINDOWS_XP = getOSMatches("Windows", "5.1");

    private final boolean IS_OS_WINDOWS_7 = getOSMatches("Windows", "6.1");
    private final boolean IS_OS_WINDOWS_8 = getOSMatches("Windows", "6.2");
    private final boolean IS_OS_WINDOWS_8_1 = getOSMatches("Windows", "6.3");
    private final boolean IS_OS_WINDOWS_10 = getOSMatches("Windows", "10.0");

    // 由于改变file.encoding属性并不会改变系统字符编码，为了保持一致，通过LocaleUtil取系统默认编码。
    private final String FILE_SEPARATOR = HealthUtils.get(System.FILE_SEPARATOR, false);
    private final String LINE_SEPARATOR = HealthUtils.get(System.LINE_SEPARATOR, false);
    private final String PATH_SEPARATOR = HealthUtils.get(System.PATH_SEPARATOR, false);

    /**
     * 取得当前OS的架构（取自系统属性：<code>os.arch</code>）。
     *
     * <p>
     * 例如：<code>"x86"</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     * @since Java 1.1
     */
    public final String getArch() {
        return OS_ARCH;
    }

    /**
     * 取得当前OS的名称（取自系统属性：<code>os.name</code>）。
     *
     * <p>
     * 例如：<code>"Windows XP"</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     * @since Java 1.1
     */
    public final String getName() {
        return OS_NAME;
    }

    /**
     * 取得当前OS的版本（取自系统属性：<code>os.version</code>）。
     *
     * <p>
     * 例如：<code>"5.1"</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     * @since Java 1.1
     */
    public final String getVersion() {
        return OS_VERSION;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为AIX，则返回<code>true</code>
     */
    public final boolean isAix() {
        return IS_OS_AIX;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为HP-UX，则返回<code>true</code>
     */
    public final boolean isHpUx() {
        return IS_OS_HP_UX;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为IRIX，则返回<code>true</code>
     */
    public final boolean isIrix() {
        return IS_OS_IRIX;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Linux，则返回<code>true</code>
     */
    public final boolean isLinux() {
        return IS_OS_LINUX;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Mac，则返回<code>true</code>
     */
    public final boolean isMac() {
        return IS_OS_MAC;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为MacOS X，则返回<code>true</code>
     */
    public final boolean isMacOsX() {
        return IS_OS_MAC_OSX;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为OS2，则返回<code>true</code>
     */
    public final boolean isOs2() {
        return IS_OS_OS2;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Solaris，则返回<code>true</code>
     */
    public final boolean isSolaris() {
        return IS_OS_SOLARIS;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Sun OS，则返回<code>true</code>
     */
    public final boolean isSunOS() {
        return IS_OS_SUN_OS;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows，则返回<code>true</code>
     */
    public final boolean isWindows() {
        return IS_OS_WINDOWS;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows 2000，则返回<code>true</code>
     */
    public final boolean isWindows2000() {
        return IS_OS_WINDOWS_2000;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows 95，则返回<code>true</code>
     */
    public final boolean isWindows95() {
        return IS_OS_WINDOWS_95;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows 98，则返回<code>true</code>
     */
    public final boolean isWindows98() {
        return IS_OS_WINDOWS_98;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows ME，则返回<code>true</code>
     */
    public final boolean isWindowsME() {
        return IS_OS_WINDOWS_ME;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows NT，则返回<code>true</code>
     */
    public final boolean isWindowsNT() {
        return IS_OS_WINDOWS_NT;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows XP，则返回<code>true</code>
     */
    public final boolean isWindowsXP() {
        return IS_OS_WINDOWS_XP;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows 7，则返回<code>true</code>
     */
    public final boolean isWindows7() {
        return IS_OS_WINDOWS_7;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows 8，则返回<code>true</code>
     */
    public final boolean isWindoows8() {
        return IS_OS_WINDOWS_8;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows 8.1，则返回<code>true</code>
     */
    public final boolean isWindows8_1() {
        return IS_OS_WINDOWS_8_1;
    }

    /**
     * 判断当前OS的类型。
     *
     * <p>
     * 如果不能取得系统属性<code>os.name</code>（因为Java安全限制），则总是返回<code>false</code>
     * </p>
     *
     * @return 如果当前OS类型为Windows 10，则返回<code>true</code>
     */
    public final boolean isWindows10() {
        return IS_OS_WINDOWS_10;
    }

    /**
     * 匹配OS名称。
     *
     * @param osNamePrefix OS名称前缀
     * @return 如果匹配，则返回<code>true</code>
     */
    private final boolean getOSMatches(String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }

        return OS_NAME.startsWith(osNamePrefix);
    }

    /**
     * 匹配OS名称。
     *
     * @param osNamePrefix    OS名称前缀
     * @param osVersionPrefix OS版本前缀
     * @return 如果匹配，则返回<code>true</code>
     */
    private final boolean getOSMatches(String osNamePrefix, String osVersionPrefix) {
        if ((OS_NAME == null) || (OS_VERSION == null)) {
            return false;
        }

        return OS_NAME.startsWith(osNamePrefix) && OS_VERSION.startsWith(osVersionPrefix);
    }

    /**
     * 取得OS的文件路径的分隔符（取自系统属性：<code>file.separator</code>）。
     *
     * <p>
     * 例如：Unix为<code>"/"</code>，Windows为<code>"\\"</code>。
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     * @since Java 1.1
     */
    public final String getFileSeparator() {
        return FILE_SEPARATOR;
    }

    /**
     * 取得OS的文本文件换行符（取自系统属性：<code>line.separator</code>）。
     *
     * <p>
     * 例如：Unix为<code>"\n"</code>，Windows为<code>"\r\n"</code>。
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     * @since Java 1.1
     */
    public final String getLineSeparator() {
        return LINE_SEPARATOR;
    }

    /**
     * 取得OS的搜索路径分隔符（取自系统属性：<code>path.separator</code>）。
     *
     * <p>
     * 例如：Unix为<code>":"</code>，Windows为<code>";"</code>。
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     * @since Java 1.1
     */
    public final String getPathSeparator() {
        return PATH_SEPARATOR;
    }

    /**
     * 将OS的信息转换成字符串。
     *
     * @return OS的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        HealthUtils.append(builder, "OS Arch:        ", getArch());
        HealthUtils.append(builder, "OS Name:        ", getName());
        HealthUtils.append(builder, "OS Version:     ", getVersion());
        HealthUtils.append(builder, "File Separator: ", getFileSeparator());
        HealthUtils.append(builder, "Line Separator: ", getLineSeparator());
        HealthUtils.append(builder, "Path Separator: ", getPathSeparator());
        return builder.toString();
    }

}
