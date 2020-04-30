/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.health;

/**
 * 操作系统信息支持
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class Platform {

    public static int getOSType() {
        return com.sun.jna.Platform.getOSType();
    }

    public static boolean isMac() {
        return com.sun.jna.Platform.isMac();
    }

    public static boolean isAndroid() {
        return com.sun.jna.Platform.isAndroid();
    }

    public static boolean isLinux() {
        return com.sun.jna.Platform.isLinux();
    }

    public static boolean isAIX() {
        return com.sun.jna.Platform.isAIX();
    }

    public static boolean isWindowsCE() {
        return com.sun.jna.Platform.isWindowsCE();
    }

    public static boolean isWindows() {
        return com.sun.jna.Platform.isWindows();
    }

    public static boolean isSolaris() {
        return com.sun.jna.Platform.isSolaris();
    }

    public static boolean isFreeBSD() {
        return com.sun.jna.Platform.isFreeBSD();
    }

    public static boolean isOpenBSD() {
        return com.sun.jna.Platform.isOpenBSD();
    }

    public static boolean isNetBSD() {
        return com.sun.jna.Platform.isNetBSD();
    }

    public static boolean isGNU() {
        return com.sun.jna.Platform.isGNU();
    }

    public static boolean iskFreeBSD() {
        return com.sun.jna.Platform.iskFreeBSD();
    }

    public static boolean isX11() {
        return com.sun.jna.Platform.isX11();
    }

    public static boolean hasRuntimeExec() {
        return com.sun.jna.Platform.hasRuntimeExec();
    }

    public static boolean is64Bit() {
        return com.sun.jna.Platform.is64Bit();
    }

    public static boolean isIntel() {
        return com.sun.jna.Platform.isIntel();
    }

    public static boolean isPPC() {
        return com.sun.jna.Platform.isPPC();
    }

    public static boolean isARM() {
        return com.sun.jna.Platform.isARM();
    }

    public static boolean isSPARC() {
        return com.sun.jna.Platform.isSPARC();
    }

    public static boolean isMIPS() {
        return com.sun.jna.Platform.isMIPS();
    }

    /**
     * 根据当前操作系统类型/arch/名称生成一个规范的字符串前缀
     *
     * @return 路径前缀
     */
    public static String getNativeLibraryResourcePrefix() {
        return getNativeLibraryResourcePrefix(getOSType(), System.getProperty("os.arch"), System.getProperty("os.name"));
    }

    /**
     * 根据给定的操作系统类型/arch/名称生成一个规范的字符串前缀。
     *
     * @param osType 从 {@link #getOSType()} 获取
     * @param arch   从 <code>os.arch</code> 获取系统属性
     * @param name   从 <code>os.name</code> 获取系统属性
     * @return the path prefix
     */
    static String getNativeLibraryResourcePrefix(int osType, String arch, String name) {
        String osPrefix;
        arch = arch.toLowerCase().trim();
        if ("powerpc".equals(arch)) {
            arch = "ppc";
        } else if ("powerpc64".equals(arch)) {
            arch = "ppc64";
        } else if ("i386".equals(arch)) {
            arch = "x86";
        } else if ("x86_64".equals(arch) || "amd64".equals(arch)) {
            arch = "x86-64";
        }
        switch (osType) {
            case com.sun.jna.Platform.ANDROID:
                if (arch.startsWith("arm")) {
                    arch = "arm";
                }
                osPrefix = "android-" + arch;
                break;
            case com.sun.jna.Platform.WINDOWS:
                osPrefix = "win32-" + arch;
                break;
            case com.sun.jna.Platform.WINDOWSCE:
                osPrefix = "w32ce-" + arch;
                break;
            case com.sun.jna.Platform.MAC:
                osPrefix = "darwin";
                break;
            case com.sun.jna.Platform.LINUX:
                osPrefix = "linux-" + arch;
                break;
            case com.sun.jna.Platform.SOLARIS:
                osPrefix = "sunos-" + arch;
                break;
            case com.sun.jna.Platform.FREEBSD:
                osPrefix = "freebsd-" + arch;
                break;
            case com.sun.jna.Platform.OPENBSD:
                osPrefix = "openbsd-" + arch;
                break;
            case com.sun.jna.Platform.NETBSD:
                osPrefix = "netbsd-" + arch;
                break;
            case com.sun.jna.Platform.KFREEBSD:
                osPrefix = "kfreebsd-" + arch;
                break;
            default:
                osPrefix = name.toLowerCase();
                int space = osPrefix.indexOf(" ");
                if (space != -1) {
                    osPrefix = osPrefix.substring(0, space);
                }
                osPrefix += "-" + arch;
                break;
        }
        return osPrefix;
    }

    public enum OS {
        /**
         * Microsoft Windows
         */
        WINDOWS,
        /**
         * A flavor of Linux
         */
        LINUX,
        /**
         * macOS (OS X)
         */
        MACOSX,
        /**
         * Solaris (SunOS)
         */
        SOLARIS,
        /**
         * FreeBSD
         */
        FREEBSD,
        /**
         * OpenBSD, WindowsCE, or an unspecified system
         */
        UNKNOWN
    }

}
