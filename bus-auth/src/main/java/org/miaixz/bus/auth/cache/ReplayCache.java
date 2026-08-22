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

import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.cache.CacheX;

/**
 * Records isolated authentication artifact digests for atomic replay detection.
 * <p>
 * Separate key scopes cover OAuth refresh-family reuse, JWT {@code jti}, DPoP {@code jti}, SAML assertion identifiers,
 * and RADIUS authenticators. The key is an irreversible digest over space, purpose, issuer, and artifact value; the
 * stored string is only a non-sensitive purpose label. Callers use create-if-absent and treat {@code false} as replay.
 * Raw nonce, family identifier, assertion, token, and authenticator values must never be stored.
 * </p>
 *
 * @author Kimi Liu
 */
public class ReplayCache extends AuthCache<String> {

    /**
     * Isolates replay markers from every other bus-cache consumer.
     */
    private static final String PURPOSE = "replay";

    /**
     * Creates a replay-marker cache view backed entirely by bus-cache.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache scope
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public ReplayCache(final CacheX<String, Object> cache, final String deployment, final Clock clock) {
        super(cache, deployment, PURPOSE, String.class, clock);
    }

    /**
     * Records a replay marker when absent.
     *
     * @param key   replay digest
     * @param value marker and expiry
     * @return creation stage
     */
    public CompletionStage<Boolean> mark(final String key, final ExpiringValue<String> value) {
        return super.doIssue(key, value);
    }

    /**
     * Finds an active replay marker.
     *
     * @param key replay digest
     * @return stored marker stage
     */
    public CompletionStage<ExpiringValue<String>> find(final String key) {
        return super.doFind(key);
    }

}
