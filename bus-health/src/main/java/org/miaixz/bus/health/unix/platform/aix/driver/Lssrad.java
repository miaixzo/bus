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
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Utility to query lssrad
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Lssrad {

    private Lssrad() {
    }

    /**
     * Query {@code lssrad} to get numa node and physical package info
     *
     * @return A map of processor number to a pair containing the ref (NUMA equivalent) and srad (package)
     */
    public static Map<Integer, Pair<Integer, Integer>> queryNodesPackages() {
        /*-
        # lssrad -av
        REF1        SRAD        MEM        CPU
        0
                       0       32749.12    0-63
                       1        9462.00    64-67 72-75
                                           80-83 88-91
        1
                       2        2471.19    92-95
        2
                       3        1992.00
                       4         249.00
         */
        int node = 0;
        int slot = 0;
        Map<Integer, Pair<Integer, Integer>> nodeMap = new HashMap<>();
        List<String> lssrad = Executor.runNative("lssrad -av");
        // remove header
        if (!lssrad.isEmpty()) {
            lssrad.remove(0);
        }
        for (String s : lssrad) {
            String t = s.trim();
            if (!t.isEmpty()) {
                if (Character.isDigit(s.charAt(0))) {
                    node = Parsing.parseIntOrDefault(t, 0);
                } else {
                    if (t.contains(".")) {
                        String[] split = Pattern.SPACES_PATTERN.split(t, 3);
                        slot = Parsing.parseIntOrDefault(split[0], 0);
                        t = split.length > 2 ? split[2] : Normal.EMPTY;
                    }
                    for (Integer proc : Parsing.parseHyphenatedIntList(t)) {
                        nodeMap.put(proc, Pair.of(node, slot));
                    }
                }
            }
        }
        return nodeMap;
    }

}
