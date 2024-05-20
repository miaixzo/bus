/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.unix.platform.aix.driver;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility to query ls
 *
 * @author Kimi Liu
 * @since Java 17+
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
