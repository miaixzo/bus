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
package org.miaixz.bus.health.unix.platform.aix.driver;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to query ls
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Ls {

    private Ls() {
    }

    /**
     * Query {@code ls} to get parition info
     *
     * @return A map of device name to a major-minor pair
     */
    public static Map<String, Pair<Integer, Integer>> queryDeviceMajorMinor() {
        // Map major and minor from ls
        /*-
         $ ls -l /dev
        brw-rw----  1 root system 10,  5 Sep 12  2017 hd2
        brw-------  1 root system 20,  0 Jun 28  1970 hdisk0
         */
        Map<String, Pair<Integer, Integer>> majMinMap = new HashMap<>();
        for (String s : Executor.runNative("ls -l /dev")) {
            // Filter to block devices
            if (!s.isEmpty() && s.charAt(0) == 'b') {
                // Device name is last space-delim string
                int idx = s.lastIndexOf(Symbol.C_SPACE);
                if (idx > 0 && idx < s.length()) {
                    String device = s.substring(idx + 1);
                    int major = Parsing.getNthIntValue(s, 2);
                    int minor = Parsing.getNthIntValue(s, 3);
                    majMinMap.put(device, Pair.of(major, minor));
                }
            }
        }
        return majMinMap;
    }

}
