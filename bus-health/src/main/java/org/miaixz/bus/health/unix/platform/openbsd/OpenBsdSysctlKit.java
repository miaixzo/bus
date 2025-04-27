/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.platform.openbsd;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.unix.jna.OpenBsdLibc;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.LibCAPI.size_t;

/**
 * Provides access to sysctl calls on OpenBSD
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class OpenBsdSysctlKit {

    private static final String SYSCTL_N = "sysctl -n ";

    private static final String SYSCTL_FAIL = "Failed sysctl call: {}, Error code: {}";

    /**
     * Executes a sysctl call with an int result
     *
     * @param name name of the sysctl
     * @param def  default int value
     * @return The int result of the call if successful; the default otherwise
     */
    public static int sysctl(int[] name, int def) {
        int intSize = OpenBsdLibc.INT_SIZE;
        try (Memory p = new Memory(intSize);
                ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(intSize)) {
            if (0 != OpenBsdLibc.INSTANCE.sysctl(name, name.length, p, size, null, size_t.ZERO)) {
                Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                return def;
            }
            return p.getInt(0);
        }
    }

    /**
     * Executes a sysctl call with a long result
     *
     * @param name name of the sysctl
     * @param def  default long value
     * @return The long result of the call if successful; the default otherwise
     */
    public static long sysctl(int[] name, long def) {
        int uint64Size = OpenBsdLibc.UINT64_SIZE;
        try (Memory p = new Memory(uint64Size);
                ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(uint64Size)) {
            if (0 != OpenBsdLibc.INSTANCE.sysctl(name, name.length, p, size, null, size_t.ZERO)) {
                Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                return def;
            }
            return p.getLong(0);
        }
    }

    /**
     * Executes a sysctl call with a String result
     *
     * @param name name of the sysctl
     * @param def  default String value
     * @return The String result of the call if successful; the default otherwise
     */
    public static String sysctl(int[] name, String def) {
        // Call first time with null pointer to get value of size
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference()) {
            if (0 != OpenBsdLibc.INSTANCE.sysctl(name, name.length, null, size, null, size_t.ZERO)) {
                Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                return def;
            }
            // Add 1 to size for null terminated string
            try (Memory p = new Memory(size.longValue() + 1L)) {
                if (0 != OpenBsdLibc.INSTANCE.sysctl(name, name.length, p, size, null, size_t.ZERO)) {
                    Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                    return def;
                }
                return p.getString(0);
            }
        }
    }

    /**
     * Executes a sysctl call with a Structure result
     *
     * @param name   name of the sysctl
     * @param struct structure for the result
     * @return True if structure is successfuly populated, false otherwise
     */
    public static boolean sysctl(int[] name, Structure struct) {
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference(struct.size())) {
            if (0 != OpenBsdLibc.INSTANCE.sysctl(name, name.length, struct.getPointer(), size, null, size_t.ZERO)) {
                Logger.error(SYSCTL_FAIL, name, Native.getLastError());
                return false;
            }
        }
        struct.read();
        return true;
    }

    /**
     * Executes a sysctl call with a Pointer result
     *
     * @param name name of the sysctl
     * @return An allocated memory buffer containing the result on success, null otherwise. Its value on failure is
     *         undefined.
     */
    public static Memory sysctl(int[] name) {
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference()) {
            if (0 != OpenBsdLibc.INSTANCE.sysctl(name, name.length, null, size, null, size_t.ZERO)) {
                Logger.error(SYSCTL_FAIL, name, Native.getLastError());
                return null;
            }
            Memory m = new Memory(size.longValue());
            if (0 != OpenBsdLibc.INSTANCE.sysctl(name, name.length, m, size, null, size_t.ZERO)) {
                Logger.error(SYSCTL_FAIL, name, Native.getLastError());
                m.close();
                return null;
            }
            return m;
        }
    }

    /**
     * Executes a sysctl call with an int result
     *
     * @param name name of the sysctl
     * @param def  default int value
     * @return The int result of the call if successful; the default otherwise
     */
    public static int sysctl(String name, int def) {
        return Parsing.parseIntOrDefault(Executor.getFirstAnswer(SYSCTL_N + name), def);
    }

    /**
     * Executes a sysctl call with a long result
     *
     * @param name name of the sysctl
     * @param def  default long value
     * @return The long result of the call if successful; the default otherwise
     */
    public static long sysctl(String name, long def) {
        return Parsing.parseLongOrDefault(Executor.getFirstAnswer(SYSCTL_N + name), def);
    }

    /**
     * Executes a sysctl call with a String result
     *
     * @param name name of the sysctl
     * @param def  default String value
     * @return The String result of the call if successful; the default otherwise
     */
    public static String sysctl(String name, String def) {
        String v = Executor.getFirstAnswer(SYSCTL_N + name);
        if (null == v || v.isEmpty()) {
            return def;
        }
        return v;
    }

}
