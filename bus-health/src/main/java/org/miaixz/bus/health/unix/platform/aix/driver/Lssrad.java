/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.platform.aix.driver;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to query lssrad
 *
 * @author Kimi Liu
 * @since Java 17+
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
