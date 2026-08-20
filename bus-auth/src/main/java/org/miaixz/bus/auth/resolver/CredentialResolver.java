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
package org.miaixz.bus.auth.resolver;

import java.time.Instant;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Credential;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Resolves externally managed credential reference status and non-sensitive metadata.
 * <p>
 * The descriptor confirms reference type, enablement, and optional expiry without acquiring secret, key, or certificate
 * material. Material resolution remains in the dedicated resolver chosen from the reference type.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface CredentialResolver {

    /**
     * Resolves one typed credential reference descriptor.
     *
     * @param request typed external credential reference
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a successful descriptor, expected rejection, or operational failure
     */
    CompletionStage<Outcome<Descriptor>> resolve(Credential.Reference request, Context context, Timeout.Budget timeout);

    /**
     * Carries immutable non-sensitive state for one external credential reference.
     *
     * @param reference resolved typed credential reference
     * @param enabled   whether external management currently permits its use
     * @param expiresAt optional absolute material expiration
     * @param metadata  immutable non-sensitive external metadata
     * @author Kimi Liu
     */
    record Descriptor(Credential.Reference reference, boolean enabled, Optional<Instant> expiresAt,
            JsonValue.ObjectValue metadata) {

        /**
         * Creates a detached credential descriptor without material.
         *
         * @param reference resolved typed credential reference
         * @param enabled   externally managed enablement state
         * @param expiresAt optional absolute expiration
         * @param metadata  non-sensitive metadata
         * @throws IllegalArgumentException if a component is {@code null}
         */
        public Descriptor {
            Assert.notNull(reference, "Credential descriptor reference must not be null");
            Assert.notNull(expiresAt, "Credential descriptor expiration container must not be null");
            expiresAt = Optional.ofNullable(expiresAt.getOrNull());
            Assert.notNull(metadata, "Credential descriptor metadata must not be null");
            metadata = new JsonValue.ObjectValue(metadata.values());
        }

    }

}
