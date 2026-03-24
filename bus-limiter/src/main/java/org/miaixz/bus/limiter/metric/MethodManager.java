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
package org.miaixz.bus.limiter.metric;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.limiter.magic.StrategyMode;
import org.miaixz.bus.logger.Logger;

/**
 * Manages the mapping between method names and their associated limiting strategy and annotation information. This
 * class provides a centralized registry for methods that are subject to limiting rules, allowing for quick retrieval of
 * their configured strategy and annotation details.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MethodManager {

    /**
     * A static map to store method information. The key is the method name (String), and the value is a {@link Pair}
     * containing the {@link StrategyMode} and the associated {@link Annotation}.
     */
    private static final Map<String, Pair<StrategyMode, Annotation>> map = new HashMap<>();

    /**
     * Adds a method and its associated limiting strategy and annotation to the manager. This method logs the
     * registration of the method for auditing purposes.
     *
     * @param name The unique name of the method to be registered.
     * @param pair A {@link Pair} containing the {@link StrategyMode} and the {@link Annotation} associated with the
     *             method.
     */
    public static void addMethod(String name, Pair<StrategyMode, Annotation> pair) {
        Logger.info("Register method:[{}][{}]", pair.getLeft().name(), name);
        map.put(name, pair);
    }

    /**
     * Retrieves the limiting strategy and annotation information for a given method name.
     *
     * @param name The name of the method to retrieve information for.
     * @return A {@link Pair} containing the {@link StrategyMode} and the {@link Annotation} associated with the method,
     *         or {@code null} if no information is found.
     */
    public static Pair<StrategyMode, Annotation> getAnnoInfo(String name) {
        return map.get(name);
    }

    /**
     * Checks if the manager contains information for a specific method name.
     *
     * @param name The name of the method to check.
     * @return {@code true} if the method is registered, {@code false} otherwise.
     */
    public static boolean contain(String name) {
        return map.containsKey(name);
    }

}
