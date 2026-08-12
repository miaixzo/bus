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
package org.miaixz.bus.auth.metric.ssf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.metric.AuthMetric.Invocation;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.JWT.VerificationPolicy;
import org.miaixz.bus.auth.metric.SSF;
import org.miaixz.bus.auth.metric.SSF.Event;
import org.miaixz.bus.auth.metric.jwt.JWTCreator;
import org.miaixz.bus.auth.metric.jwt.JWTPayload;
import org.miaixz.bus.auth.metric.jwt.JWTVerifier;
import org.miaixz.bus.auth.metric.jwt.signature.JWTSigner;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Creates and verifies signed Security Event Tokens using the existing hardened JWT implementation.
 */
public final class SecurityEventToken {

    /**
     * SET events claim.
     */
    public static final String EVENTS = "events";

    /**
     * JWT subject claim.
     */
    public static final String SUBJECT = "sub";

    /**
     * Prevents construction.
     */
    private SecurityEventToken() {
        // No initialization required.
    }

    /**
     * Creates one compact SET.
     *
     * @param event   allowed event
     * @param policy  trusted JWT policy
     * @param runtime runtime
     * @param signer  trusted signer
     * @return compact SET
     */
    public static String create(
            final Event event,
            final VerificationPolicy policy,
            final Runtime runtime,
            final JWTSigner signer) {
        final Event source = Assert.notNull(event, () -> new ValidateException("SSF event must not be null"));
        final LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put(SUBJECT, source.subject());
        payload.put(EVENTS, Map.of(source.type(), source.claims()));
        return JWTCreator.create(payload, policy, runtime, signer);
    }

    /**
     * Verifies one compact SET including signature, issuer, audience, time, jti replay, and event allow-list.
     *
     * @param token      compact SET
     * @param policy     trusted JWT policy requiring replay protection
     * @param invocation operation context
     * @param runtime    runtime
     * @return verified event stage
     */
    public static CompletionStage<Event> verify(
            final String token,
            final VerificationPolicy policy,
            final Invocation invocation,
            final Runtime runtime) {
        Assert.isTrue(
                policy.requireReplay(),
                () -> new ValidateException("SSF receiver policy must require JWT replay protection"));
        return JWTVerifier.verify(token, policy, invocation, runtime).thenApply(SecurityEventToken::event);
    }

    /**
     * Maps one verified JWT payload to exactly one allowed event.
     *
     * @param payload verified payload
     * @return event
     */
    static Event event(final JWTPayload payload) {
        final Object subject = payload.claims().get(SUBJECT);
        final Object events = payload.claims().get(EVENTS);
        if (!(subject instanceof String identifier) || !(events instanceof Map<?, ?> values) || values.size() != 1) {
            throw new ValidateException("SSF SET claims are invalid");
        }
        final Map.Entry<?, ?> entry = values.entrySet().iterator().next();
        if (!(entry.getKey() instanceof String type) || !SSF.ALLOWED_EVENTS.contains(type)
                || !(entry.getValue() instanceof Map<?, ?> claims)) {
            throw new ValidateException("SSF event claim is invalid");
        }
        final LinkedHashMap<String, Object> mapped = new LinkedHashMap<>();
        claims.forEach((key, value) -> {
            if (!(key instanceof String name)) {
                throw new ValidateException("SSF event claim name is invalid");
            }
            mapped.put(name, value);
        });
        return new Event(type, identifier, mapped);
    }

}
