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
package org.miaixz.bus.health.mac;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.logger.Logger;

/**
 * Caches macOS SMC keys after configuration lookup or runtime discovery.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class SmcKeyCache {

    /**
     * Configuration property that names the keys explicitly.
     */
    private final String configProperty;

    /**
     * Description used in diagnostic log messages.
     */
    private final String description;

    /**
     * Fallback keys used when discovery cannot complete.
     */
    private final List<String> fallback;

    /**
     * Lock used while resolving the keys.
     */
    private final Object lock = new Object();

    /**
     * Resolved keys, published after a completed resolution.
     */
    private volatile List<String> keys;

    /**
     * Creates a new key cache.
     *
     * @param configProperty The {@link Builder} property that names the keys outright.
     * @param description    The sensor description for log messages.
     * @param fallback       The keys to use when discovery cannot complete.
     */
    public SmcKeyCache(String configProperty, String description, List<String> fallback) {
        this.configProperty = configProperty;
        this.description = description;
        this.fallback = fallback;
    }

    /**
     * Returns the keys to read, resolving them on first successful use.
     *
     * @param discovery Discovers the keys from SMC and returns {@code null} if discovery could not complete.
     * @return The keys to read, never {@code null}.
     */
    public List<String> get(Supplier<List<String>> discovery) {
        List<String> resolved = keys;
        if (resolved != null) {
            return resolved;
        }
        synchronized (lock) {
            if (keys != null) {
                return keys;
            }
            List<String> configured = SmcKeyIndex.parseConfiguredKeys(Builder.get(configProperty, Normal.EMPTY));
            if (!configured.isEmpty()) {
                Logger.debug(false, "Health", "Using configured {} keys {}", description, configured);
                keys = configured;
                return configured;
            }
            List<String> discovered = discovery.get();
            if (discovered == null) {
                Logger.debug(
                        false,
                        "Health",
                        "{} key discovery did not complete; using {} this time.",
                        description,
                        fallback.isEmpty() ? "no keys" : "the fallback list");
                return fallback;
            }
            Logger.debug(false, "Health", "Using {} {} keys: {}", discovered.size(), description, discovered);
            keys = discovered;
            return discovered;
        }
    }

    /**
     * Returns the fallback keys.
     *
     * @return The fallback keys.
     */
    public List<String> fallback() {
        return fallback;
    }

}
