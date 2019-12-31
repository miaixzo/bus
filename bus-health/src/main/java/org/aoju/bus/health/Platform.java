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

/**
 * Enum of supported operating systems.
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class Platform {

    public static final int getOSType() {
        return com.sun.jna.Platform.getOSType();
    }

    public static final boolean isMac() {
        return com.sun.jna.Platform.isMac();
    }

    public static final boolean isAndroid() {
        return com.sun.jna.Platform.isAndroid();
    }

    public static final boolean isLinux() {
        return com.sun.jna.Platform.isLinux();
    }

    public static final boolean isAIX() {
        return com.sun.jna.Platform.isAIX();
    }

    public static final boolean isWindowsCE() {
        return com.sun.jna.Platform.isWindowsCE();
    }

    public static final boolean isWindows() {
        return com.sun.jna.Platform.isWindows();
    }

    public static final boolean isSolaris() {
        return com.sun.jna.Platform.isSolaris();
    }

    public static final boolean isFreeBSD() {
        return com.sun.jna.Platform.isFreeBSD();
    }

    public static final boolean isOpenBSD() {
        return com.sun.jna.Platform.isOpenBSD();
    }

    public static final boolean isNetBSD() {
        return com.sun.jna.Platform.isNetBSD();
    }

    public static final boolean isGNU() {
        return com.sun.jna.Platform.isGNU();
    }

    public static final boolean iskFreeBSD() {
        return com.sun.jna.Platform.iskFreeBSD();
    }

    public static final boolean isX11() {
        return com.sun.jna.Platform.isX11();
    }

    public static final boolean hasRuntimeExec() {
        return com.sun.jna.Platform.hasRuntimeExec();
    }

    public static final boolean is64Bit() {
        return com.sun.jna.Platform.is64Bit();
    }

    public static final boolean isIntel() {
        return com.sun.jna.Platform.isIntel();
    }

    public static final boolean isPPC() {
        return com.sun.jna.Platform.isPPC();
    }

    public static final boolean isARM() {
        return com.sun.jna.Platform.isARM();
    }

    public static final boolean isSPARC() {
        return com.sun.jna.Platform.isSPARC();
    }

    public static final boolean isMIPS() {
        return com.sun.jna.Platform.isMIPS();
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
        UNKNOWN;
    }

}
