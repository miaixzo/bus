/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.jna.ByRef.CloseableSizeTByReference;
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.LibCAPI.size_t;

/**
 * Provides access to sysctl calls on macOS.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class SysctlKit {

    private static final String SYSCTL_FAIL = "Failed sysctl call: {}, Error code: {}";

    /**
     * Executes a sysctl call with an int result.
     *
     * @param name name of the sysctl.
     * @param def  default int value.
     * @return The int result of the call if successful; the default otherwise.
     */
    public static int sysctl(String name, int def) {
        return sysctl(name, def, true);
    }

    /**
     * Executes a sysctl call with an int result.
     *
     * @param name       name of the sysctl.
     * @param def        default int value.
     * @param logWarning whether to log the warning if not available.
     * @return The int result of the call if successful; the default otherwise.
     */
    public static int sysctl(String name, int def, boolean logWarning) {
        int intSize = com.sun.jna.platform.mac.SystemB.INT_SIZE;
        try (Memory p = new Memory(intSize); CloseableSizeTByReference size = new CloseableSizeTByReference(intSize)) {
            if (0 != SystemB.INSTANCE.sysctlbyname(name, p, size, null, size_t.ZERO)) {
                if (logWarning) {
                    Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                }
                return def;
            }
            return p.getInt(0);
        }
    }

    /**
     * Executes a sysctl call with a long result.
     *
     * @param name name of the sysctl.
     * @param def  default long value.
     * @return The long result of the call if successful; the default otherwise.
     */
    public static long sysctl(String name, long def) {
        int uint64Size = com.sun.jna.platform.mac.SystemB.UINT64_SIZE;
        try (Memory p = new Memory(uint64Size);
                CloseableSizeTByReference size = new CloseableSizeTByReference(uint64Size)) {
            if (0 != SystemB.INSTANCE.sysctlbyname(name, p, size, null, size_t.ZERO)) {
                Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                return def;
            }
            return p.getLong(0);
        }
    }

    /**
     * Executes a sysctl call with a String result.
     *
     * @param name name of the sysctl.
     * @param def  default String value.
     * @return The String result of the call if successful; the default otherwise.
     */
    public static String sysctl(String name, String def) {
        return sysctl(name, def, true);
    }

    /**
     * Executes a sysctl call with a String result.
     *
     * @param name       name of the sysctl.
     * @param def        default String value.
     * @param logWarning whether to log the warning if not available.
     * @return The String result of the call if successful; the default otherwise.
     */
    public static String sysctl(String name, String def, boolean logWarning) {
        // Call first time with null pointer to get value of size
        try (CloseableSizeTByReference size = new CloseableSizeTByReference()) {
            if (0 != SystemB.INSTANCE.sysctlbyname(name, null, size, null, size_t.ZERO)) {
                if (logWarning) {
                    Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                }
                return def;
            }
            // Add 1 to size for null terminated string
            try (Memory p = new Memory(size.longValue() + 1L)) {
                if (0 != SystemB.INSTANCE.sysctlbyname(name, p, size, null, size_t.ZERO)) {
                    if (logWarning) {
                        Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                    }
                    return def;
                }
                return p.getString(0);
            }
        }
    }

    /**
     * Executes a sysctl call with a Structure result.
     *
     * @param name   name of the sysctl.
     * @param struct structure for the result.
     * @return True if structure is successfully populated, false otherwise.
     */
    public static boolean sysctl(String name, Structure struct) {
        try (CloseableSizeTByReference size = new CloseableSizeTByReference(struct.size())) {
            if (0 != SystemB.INSTANCE.sysctlbyname(name, struct.getPointer(), size, null, size_t.ZERO)) {
                Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                return false;
            }
        }
        struct.read();
        return true;
    }

    /**
     * Executes a sysctl call with a Pointer result.
     *
     * @param name name of the sysctl.
     * @return An allocated memory buffer containing the result on success, null otherwise. Its value on failure is
     *         undefined.
     */
    public static Memory sysctl(String name) {
        try (CloseableSizeTByReference size = new CloseableSizeTByReference()) {
            if (0 != SystemB.INSTANCE.sysctlbyname(name, null, size, null, size_t.ZERO)) {
                Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                return null;
            }
            Memory m = new Memory(size.longValue());
            if (0 != SystemB.INSTANCE.sysctlbyname(name, m, size, null, size_t.ZERO)) {
                Logger.warn(SYSCTL_FAIL, name, Native.getLastError());
                m.close();
                return null;
            }
            return m;
        }
    }

}
