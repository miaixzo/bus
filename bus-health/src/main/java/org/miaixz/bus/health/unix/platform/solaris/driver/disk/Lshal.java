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
package org.miaixz.bus.health.unix.platform.solaris.driver.disk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to query lshal
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Lshal {

    private static final String LSHAL_CMD = "lshal";

    /**
     * Query lshal to get device major
     *
     * @return A map with disk names as the key and block device major as the value if lshal is installed; empty map
     *         otherwise
     */
    public static Map<String, Integer> queryDiskToMajorMap() {
        Map<String, Integer> majorMap = new HashMap<>();
        List<String> lshal = Executor.runNative(LSHAL_CMD);
        String diskName = null;
        for (String line : lshal) {
            if (line.startsWith("udi ")) {
                String udi = Parsing.getSingleQuoteStringValue(line);
                diskName = udi.substring(udi.lastIndexOf('/') + 1);
            } else {
                line = line.trim();
                if (line.startsWith("block.major") && diskName != null) {
                    majorMap.put(diskName, Parsing.getFirstIntValue(line));
                }
            }
        }
        return majorMap;
    }

}
