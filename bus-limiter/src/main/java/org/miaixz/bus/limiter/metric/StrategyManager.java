/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.limiter.Provider;
import org.miaixz.bus.limiter.magic.StrategyMode;

/**
 * Manages and provides access to different limiting strategy implementations. This class acts as a registry for
 * {@link Provider} instances, mapping each {@link StrategyMode} to its corresponding provider responsible for executing
 * that strategy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StrategyManager {

    /**
     * A static map to cache {@link Provider} instances, keyed by their {@link StrategyMode}. This allows for quick
     * retrieval of the appropriate strategy executor.
     */
    private static final Map<StrategyMode, Provider> map = new HashMap<>();

    /**
     * Adds a {@link Provider} to the strategy manager. The provider is registered under its associated
     * {@link StrategyMode}.
     *
     * @param provider The {@link Provider} instance to be added.
     */
    public static void add(Provider provider) {
        map.put(provider.get(), provider);
    }

    /**
     * Retrieves the {@link Provider} responsible for executing a specific {@link StrategyMode}.
     *
     * @param strategyMode The {@link StrategyMode} for which to retrieve the provider.
     * @return The {@link Provider} instance associated with the given strategy mode, or {@code null} if not found.
     */
    public static Provider get(StrategyMode strategyMode) {
        return map.get(strategyMode);
    }

}
