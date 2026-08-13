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
package org.miaixz.bus.auth.protocol.ssf;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Claims;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.cache.StateStore;
import org.miaixz.bus.auth.protocol.jwt.JWT.VerificationPolicy;
import org.miaixz.bus.auth.protocol.jwt.JWTCreator;
import org.miaixz.bus.auth.protocol.jwt.JWTPayload;
import org.miaixz.bus.auth.protocol.jwt.JWTVerifier;
import org.miaixz.bus.auth.protocol.jwt.KeyResolver;
import org.miaixz.bus.auth.protocol.jwt.signature.JWTSigner;
import org.miaixz.bus.auth.protocol.ssf.SSF.Event;
import org.miaixz.bus.auth.runtime.Limits;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.fabric.Clock;

/**
 * Creates and verifies signed Security Event Tokens using the existing hardened JWT implementation.
 *
 * @author Kimi Liu
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
     * Maximum resolver candidates evaluated for one SET signature.
     */
    private static final int MAXIMUM_KEY_CANDIDATES = 128;

    /**
     * Prevents construction.
     */
    private SecurityEventToken() {
        // No initialization required.
    }

    /**
     * Creates one compact SET.
     *
     * @param event  allowed event
     * @param policy trusted JWT policy
     * @param clock  trusted Fabric clock
     * @param random secure JWT identifier entropy
     * @param json   JSON provider
     * @param limits immutable protocol limits
     * @param signer trusted signer
     * @return compact SET
     * @throws ValidateException if an issuance input is null or violates the JWT policy
     */
    public static String create(
            final Event event,
            final VerificationPolicy policy,
            final Clock clock,
            final SecureRandom random,
            final JsonProvider json,
            final Limits limits,
            final JWTSigner signer) {
        final Event source = Assert.notNull(event, () -> new ValidateException("SSF event must not be null"));
        final Limits bounds = Assert.notNull(limits, () -> new ValidateException("SSF limits must not be null"));
        final LinkedHashMap<String, Object> payload = new LinkedHashMap<>();
        payload.put(SUBJECT, source.subject());
        payload.put(EVENTS, Map.of(source.type(), source.claims()));
        return JWTCreator.create(
                Claims.from(payload),
                policy,
                clock,
                random,
                json,
                bounds.maxHeaderBytes(),
                bounds.maxJsonBytes(),
                bounds.maxJwtBytes(),
                signer);
    }

    /**
     * Verifies one compact SET including signature, issuer, audience, time, jti replay, and event allow-list.
     *
     * @param token      compact SET
     * @param policy     trusted JWT policy requiring replay protection
     * @param invocation operation context
     * @param json       JSON provider
     * @param clock      trusted Fabric clock
     * @param keys       trusted key resolver
     * @param states     atomic replay store
     * @param limits     immutable protocol limits
     * @return verified event stage
     * @throws ValidateException if the policy or limits are null, or replay protection is disabled
     */
    public static CompletionStage<Event> verify(
            final String token,
            final VerificationPolicy policy,
            final Context invocation,
            final JsonProvider json,
            final Clock clock,
            final KeyResolver keys,
            final StateStore states,
            final Limits limits) {
        final VerificationPolicy trusted = Assert
                .notNull(policy, () -> new ValidateException("SSF receiver policy must not be null"));
        final Limits bounds = Assert.notNull(limits, () -> new ValidateException("SSF limits must not be null"));
        Assert.isTrue(
                trusted.requireReplay(),
                () -> new ValidateException("SSF receiver policy must require JWT replay protection"));
        return JWTVerifier
                .verify(
                        token,
                        trusted,
                        invocation,
                        json,
                        clock,
                        keys,
                        states,
                        bounds.maxJwtBytes(),
                        bounds.maxHeaderBytes(),
                        bounds.maxJsonBytes(),
                        bounds.maxJsonDepth(),
                        MAXIMUM_KEY_CANDIDATES)
                .thenApply(SecurityEventToken::success).thenApply(SecurityEventToken::event);
    }

    /**
     * Returns the verified payload or rejects a non-success JWT outcome.
     *
     * @param outcome internal JWT verification result
     * @return verified payload
     * @throws ValidateException if verification did not produce a non-null success payload
     */
    private static JWTPayload success(final Outcome<JWTPayload> outcome) {
        if (outcome instanceof Outcome.Success<JWTPayload> success && success.value() != null) {
            return success.value();
        }
        throw new ValidateException("SSF token verification failed");
    }

    /**
     * Maps one verified JWT payload to exactly one allowed event.
     *
     * @param payload verified payload
     * @return event
     * @throws ValidateException if required SET claims or the single event are invalid
     */
    static Event event(final JWTPayload payload) {
        final Map<String, Object> snapshot = Assert
                .notNull(payload, () -> new ValidateException("SSF JWT payload must not be null")).snapshot();
        final Object subject = snapshot.get(SUBJECT);
        final Object events = snapshot.get(EVENTS);
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
