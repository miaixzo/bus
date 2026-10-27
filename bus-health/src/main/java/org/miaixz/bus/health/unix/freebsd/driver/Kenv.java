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
package org.miaixz.bus.health.unix.freebsd.driver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Reads the FreeBSD kernel environment, including SMBIOS values copied there by the loader.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class Kenv {

    /** Prevents instantiation. */
    private Kenv() {
    }

    /**
     * Reads every kernel environment variable.
     *
     * @return a map of variable names to unquoted values
     */
    public static Map<String, String> queryKenv() {
        return parseKenv(Executor.runNative("kenv"));
    }

    /**
     * Parses {@code name="value"} pairs emitted by {@code kenv}.
     *
     * @param lines the command output
     * @return a map of variable names to unquoted values
     */
    static Map<String, String> parseKenv(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, String> entry : Parsing.parseStringListToMap(lines, Symbol.EQUAL).entrySet()) {
            values.put(entry.getKey(), Parsing.getDoubleQuoteStringValue(entry.getValue()));
        }
        return values;
    }

}
