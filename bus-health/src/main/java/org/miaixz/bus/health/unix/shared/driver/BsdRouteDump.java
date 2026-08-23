/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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
package org.miaixz.bus.health.unix.shared.driver;

import com.sun.jna.Memory;
import com.sun.jna.platform.unix.LibCAPI.size_t;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.jna.ByRef.CloseableSizeTByReference;
import org.miaixz.bus.health.unix.shared.jna.CLibrary;
import org.miaixz.bus.logger.Logger;

/**
 * Fetches a BSD kernel routing table using a {@code NET_RT_DUMP} sysctl.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class BsdRouteDump {

    /**
     * Prevents instantiation.
     */
    private BsdRouteDump() {
    }

    /**
     * Fetches the routing table dump.
     *
     * @param libc the platform C library
     * @return the bytes returned by the kernel, or an empty array if the query fails
     */
    public static byte[] queryRouteDump(CLibrary libc) {
        int[] mib = { Normal._4, Normal._17, Normal._0, Normal._0, Normal._1, Normal._0 };
        try (CloseableSizeTByReference len = new CloseableSizeTByReference()) {
            if (Normal._0 != libc.sysctl(mib, Normal._6, null, len, null, size_t.ZERO)) {
                Logger.error(false, "Health", "Didn't get buffer length for NET_RT_DUMP");
                return Normal.EMPTY_BYTE_ARRAY;
            }
            long size = len.longValue();
            if (size <= Normal._0) {
                return Normal.EMPTY_BYTE_ARRAY;
            }
            try (Memory buffer = new Memory(size)) {
                if (Normal._0 != libc.sysctl(mib, Normal._6, buffer, len, null, size_t.ZERO)) {
                    Logger.error(false, "Health", "Didn't get buffer for NET_RT_DUMP");
                    return Normal.EMPTY_BYTE_ARRAY;
                }
                return buffer.getByteArray(Normal._0, (int) Math.min(size, len.longValue()));
            }
        }
    }

}
