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
package org.aoju.bus.health.common.unix.freebsd;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.logger.Logger;

/**
 * Provides access to sysctl calls on FreeBSD
 *
 * @author Kimi Liu
 * @version 5.2.9
 * @since JDK 1.8+
 */
public class BsdSysctlUtils {

    private static final String SYSCTL_FAIL = "Failed syctl call: {}, Error code: {}";

    private BsdSysctlUtils() {
    }

    /**
     * Executes a sysctl call with an int result
     *
     * @param name name of the sysctl
     * @param def  default int value
     * @return The int result of the call if successful; the default otherwise
     */
    public static int sysctl(String name, int def) {
        IntByReference size = new IntByReference(FreeBsdLibc.INT_SIZE);
        Pointer p = new Memory(size.getValue());
        if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, p, size, null, 0)) {
            Logger.error("Failed sysctl call: {}, Error code: {}", name, Native.getLastError());
            return def;
        }
        return p.getInt(0);
    }

    /**
     * Executes a sysctl call with a long result
     *
     * @param name name of the sysctl
     * @param def  default long value
     * @return The long result of the call if successful; the default otherwise
     */
    public static long sysctl(String name, long def) {
        IntByReference size = new IntByReference(FreeBsdLibc.UINT64_SIZE);
        Pointer p = new Memory(size.getValue());
        if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, p, size, null, 0)) {
            Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
            return def;
        }
        return p.getLong(0);
    }

    /**
     * Executes a sysctl call with a String result
     *
     * @param name name of the sysctl
     * @param def  default String value
     * @return The String result of the call if successful; the default otherwise
     */
    public static String sysctl(String name, String def) {
        // Call first time with null pointer to get value of size
        IntByReference size = new IntByReference();
        if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, null, size, null, 0)) {
            Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
            return def;
        }
        // Add 1 to size for null terminated string
        Pointer p = new Memory(size.getValue() + 1);
        if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, p, size, null, 0)) {
            Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
            return def;
        }
        return p.getString(0);
    }

    /**
     * Executes a sysctl call with a Structure result
     *
     * @param name   name of the sysctl
     * @param struct structure for the result
     * @return True if structure is successfuly populated, false otherwise
     */
    public static boolean sysctl(String name, Structure struct) {
        if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, struct.getPointer(), new IntByReference(struct.size()), null,
                0)) {
            Logger.error(SYSCTL_FAIL, name, Native.getLastError());
            return false;
        }
        struct.read();
        return true;
    }
}
