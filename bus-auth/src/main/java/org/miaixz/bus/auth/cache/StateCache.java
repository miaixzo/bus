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
package org.miaixz.bus.auth.cache;

import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.fabric.Clock;

/**
 * Stores one-time callback correlation for OAuth, OpenID Connect, SAML, and Vendor browser interactions.
 * <p>
 * Keys are irreversible digests isolated by namespace, Source, and state purpose. Consumers must use
 * {@link #take(String)} so a callback state succeeds at most once. This wrapper adds a fixed state namespace before it
 * delegates the atomic operation to bus-cache.
 * </p>
 *
 * @author Kimi Liu
 */
public final class StateCache extends AuthCache<Callback.Correlation> {

    /**
     * Isolates callback correlation state from every other bus-cache consumer.
     */
    private static final String PURPOSE = "state";

    /**
     * Creates a callback-state cache view backed entirely by bus-cache.
     *
     * @param cache shared bus-cache backend
     */
    public StateCache(final CacheX<String, Object> cache, final String deployment,
            final Clock clock) {
        super(cache, deployment, PURPOSE, Callback.Correlation.class, clock);
    }

    public CompletionStage<Boolean> issue(final String key, final ExpiringValue<Callback.Correlation> value) {
        return super.doIssue(key, value);
    }

    public CompletionStage<ExpiringValue<Callback.Correlation>> consume(final String key) {
        return super.doConsume(key);
    }

}
