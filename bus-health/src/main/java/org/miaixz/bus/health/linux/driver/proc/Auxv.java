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
package org.miaixz.bus.health.linux.driver.proc;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.linux.ProcPath;

/**
 * Utility to read auxiliary vector from {@code /proc/self/auxv}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Auxv {

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
     * Retrieve the auxiliary vector for the current process
     *
     * @return A map of auxiliary vector keys to their respective values
     * @see <a href= "https://github.com/torvalds/linux/blob/v3.19/include/uapi/linux/auxvec.h">auxvec.h</a>
     */
    public static Map<Integer, Long> queryAuxv() {
        ByteBuffer buff = Builder.readAllBytesAsBuffer(ProcPath.AUXV);
        Map<Integer, Long> auxvMap = new HashMap<>();
        int key;
        do {
            key = Builder.readNativeLongFromBuffer(buff).intValue();
            if (key > 0) {
                auxvMap.put(key, Builder.readNativeLongFromBuffer(buff).longValue());
            }
        } while (key > 0);
        return auxvMap;

    }

}
