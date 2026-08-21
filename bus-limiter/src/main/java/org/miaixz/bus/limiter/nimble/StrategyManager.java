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
package org.miaixz.bus.limiter.nimble;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.limiter.Provider;
import org.miaixz.bus.limiter.magic.StrategyMode;
import org.miaixz.bus.logger.Logger;

/**
 * Manages and provides access to different limiting strategy implementations. This class acts as a registry for
 * {@link Provider} instances, mapping each {@link StrategyMode} to its corresponding provider responsible for executing
 * that strategy.
 *
 * @author Kimi Liu
 */
public class StrategyManager {

    /**
     * Initializes the manager that resolves and caches limiter strategy Providers by strategy mode.
     */
    public StrategyManager() {
        // No initialization required.
    }

    /**
     * A static map to cache {@link Provider} instances, keyed by their {@link StrategyMode}. This allows for quick
     * retrieval of the appropriate strategy executor.
     */
    private static final Map<StrategyMode, Provider> map = new ConcurrentHashMap<>();

    /**
     * Adds a {@link Provider} to the strategy manager. The provider is registered under its associated
     * {@link StrategyMode}.
     *
     * @param provider The {@link Provider} instance to be added.
     */
    public static void add(Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Limiter provider must not be null");
        }
        final StrategyMode type = provider.type();
        if (type == null) {
            throw new IllegalArgumentException(
                    "Limiter provider type must not be null: " + provider.getClass().getName());
        }
        final Provider previous = map.putIfAbsent(type, provider);
        if (previous != null) {
            throw new IllegalStateException("Duplicate limiter Provider type " + type + ": "
                    + previous.getClass().getName() + " and " + provider.getClass().getName());
        }
        Logger.info(
                false,
                "Limiter",
                "Limiter strategy provider registered: strategy={}, provider={}, providerCount={}",
                type.name(),
                provider.getClass().getName(),
                map.size());
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

    /**
     * Clears all registered strategy providers.
     */
    public static void clear() {
        map.clear();
    }

}
