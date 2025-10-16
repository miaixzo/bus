/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
 * @since Java 17+
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
