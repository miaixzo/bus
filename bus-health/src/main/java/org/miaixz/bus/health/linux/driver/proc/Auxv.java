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
package org.miaixz.bus.health.linux.driver.proc;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.Platform;
import org.miaixz.bus.health.linux.ProcPath;

/**
 * Reads auxiliary vector from {@code /proc/self/auxv}
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class Auxv {

    /**
     * end of vector
     */
    public static final int AT_NULL = 0;

    /**
     * system page size
     */
    public static final int AT_PAGESZ = 6;

    /**
     * arch dependent hints at CPU capabilities
     */
    public static final int AT_HWCAP = 16;

    /**
     * frequency at which times() increments
     */
    public static final int AT_CLKTCK = 17;

    /**
     * Constructs a new Auxv instance.
     */
    public Auxv() {
        // No initialization required.
    }

    /**
     * Retrieve the auxiliary vector for the current process
     *
     * @return A map of auxiliary vector keys to their respective values
     * @see <a href= "https://github.com/torvalds/linux/blob/v3.19/include/uapi/linux/auxvec.h">auxvec.h</a>
     */
    public static Map<Integer, Long> queryAuxv() {
        return parseAuxv(Builder.readAllBytesAsBuffer(ProcPath.AUXV), Platform.is64Bit());
    }

    /**
     * Parses an auxiliary vector.
     *
     * @param buff    The vector in the byte order of the process that wrote it.
     * @param is64Bit Whether each key and value occupies eight bytes.
     * @return A map of auxiliary vector keys to values.
     */
    static Map<Integer, Long> parseAuxv(ByteBuffer buff, boolean is64Bit) {
        Map<Integer, Long> auxvMap = new HashMap<>();
        int key;
        do {
            key = (int) readLong(buff, is64Bit);
            if (key != AT_NULL) {
                auxvMap.put(key, readLong(buff, is64Bit));
            }
        } while (key != AT_NULL);
        return auxvMap;
    }

    /**
     * Gets the clock ticks per second from an auxiliary vector.
     *
     * @param auxv The auxiliary vector.
     * @return The clock ticks per second.
     */
    public static long userHz(Map<Integer, Long> auxv) {
        return positiveValue(auxv, AT_CLKTCK, "getconf CLK_TCK", Normal._100);
    }

    /**
     * Gets the page size from an auxiliary vector.
     *
     * @param auxv The auxiliary vector.
     * @return The page size in bytes.
     */
    public static long pageSize(Map<Integer, Long> auxv) {
        return positiveValue(auxv, AT_PAGESZ, "getconf PAGE_SIZE", Normal._4096);
    }

    /**
     * Reads a native-width integer from the vector.
     *
     * @param buff    The vector buffer.
     * @param is64Bit Whether the value occupies eight bytes.
     * @return The value.
     */
    private static long readLong(ByteBuffer buff, boolean is64Bit) {
        return is64Bit ? Parsing.readLongFromBuffer(buff) : Parsing.readIntFromBuffer(buff);
    }

    /**
     * Resolves a positive auxiliary vector value with a command fallback.
     *
     * @param auxv         The auxiliary vector.
     * @param key          The auxiliary vector key.
     * @param command      The fallback command.
     * @param defaultValue The final default value.
     * @return A positive value.
     */
    private static long positiveValue(Map<Integer, Long> auxv, int key, String command, long defaultValue) {
        long value = auxv.getOrDefault(key, (long) Normal._0);
        if (value <= Normal._0) {
            value = Parsing.parseLongOrDefault(Executor.getFirstAnswer(command), Normal._0);
        }
        return value > Normal._0 ? value : defaultValue;

    }

}
