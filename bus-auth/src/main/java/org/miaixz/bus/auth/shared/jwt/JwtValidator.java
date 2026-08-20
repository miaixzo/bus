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
package org.miaixz.bus.auth.shared.jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.AudienceValidator;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.guard.ReplayGuard;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Applies shared issuer, audience, temporal, exact-claim, and optional replay policy to a verified JWT.
 * <p>
 * Protocol-specific claims remain owned by their protocol implementation. Expected validation refusals are returned as
 * rejected outcomes, while replay-cache operational failures retain the failed outcome produced by ReplayGuard.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JwtValidator {

    /**
     * Shared exact issuer comparison primitive.
     */
    private final IssuerValidator issuerValidator;
    /**
     * Shared audience allow-list primitive.
     */
    private final AudienceValidator audienceValidator;
    /**
     * Shared Fabric-clock temporal primitive.
     */
    private final TimeGuard timeGuard;
    /**
     * Shared atomic replay registration primitive.
     */
    private final ReplayGuard replayGuard;

    /**
     * Creates a JWT validator from existing framework guard primitives.
     *
     * @param issuerValidator   exact issuer validator
     * @param audienceValidator audience allow-list validator
     * @param timeGuard         shared-clock temporal validator
     * @param replayGuard       atomic replay validator
     */
    public JwtValidator(final IssuerValidator issuerValidator, final AudienceValidator audienceValidator,
            final TimeGuard timeGuard, final ReplayGuard replayGuard) {
        this.issuerValidator = Assert.notNull(issuerValidator, "JWT issuer validator must not be null");
        this.audienceValidator = Assert.notNull(audienceValidator, "JWT audience validator must not be null");
        this.timeGuard = Assert.notNull(timeGuard, "JWT time guard must not be null");
        this.replayGuard = Assert.notNull(replayGuard, "JWT replay guard must not be null");
    }

    /**
     * Creates non-sensitive rejection detail using an existing Bus error code.
     *
     * @param description safe generic rejection description
     * @return structured Outcome failure
     */
    private static Outcome.Failure failure(final String description) {
        return new Outcome.Failure(ErrorCode._401, description, new JsonValue.ObjectValue(Map.of()));
    }

    /**
     * Validates common JWT claims and atomically records jti when replay policy is enabled.
     *
     * @param jwt          cryptographically verified JWT
     * @param requirements explicit common-claim requirements
     * @param context      current immutable authentication context
     * @param timeout      shared end-to-end operation budget
     * @return stage containing the accepted JWT, expected rejection, or replay-cache failure
     */
    public CompletionStage<Outcome<Jwt>> validate(
            final Jwt jwt,
            final Requirements requirements,
            final Context context,
            final Timeout.Budget timeout) {
        Assert.notNull(jwt, "JWT must not be null");
        Assert.notNull(requirements, "JWT validation requirements must not be null");
        Assert.notNull(context, "JWT validation context must not be null");
        Assert.notNull(timeout, "JWT validation budget must not be null");
        try {
            validateClaims(jwt.claims(), requirements, timeout);
        } catch (ValidateException cause) {
            return CompletableFuture.completedFuture(Outcome.rejected(failure("JWT common claim validation failed")));
        }
        if (requirements.replay().isEmpty()) {
            return CompletableFuture.completedFuture(Outcome.succeeded(jwt));
        }
        final Replay replay = requirements.replay().getOrThrow();
        return replayGuard.register(
                replay.namespace(),
                replay.protocol(),
                replay.authority(),
                "jwt-jti",
                jwt.claims().jwtId().orElseThrow(),
                jwt.claims().expiration().orElseThrow(),
                timeout).thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(jwt);
                    case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
                });
    }

    /**
     * Applies all synchronous common-claim rules before replay registration.
     *
     * @param claims       verified JWT Claims Set
     * @param requirements validation requirements
     * @param timeout      shared operation budget
     */
    private void validateClaims(final JwtClaims claims, final Requirements requirements, final Timeout.Budget timeout) {
        final String issuer = claims.issuer().orElseThrow(() -> new ValidateException("JWT issuer claim is required"));
        issuerValidator.validate(requirements.issuer(), issuer);
        if (claims.audiences().isEmpty()) {
            throw new ValidateException("JWT audience claim is required");
        }
        audienceValidator.validate(claims.audiences(), requirements.audiences());
        if (requirements.subjectRequired() && claims.subject().isEmpty()) {
            throw new ValidateException("JWT subject claim is required");
        }
        if (requirements.jwtIdRequired() && claims.jwtId().isEmpty()) {
            throw new ValidateException("JWT identifier claim is required");
        }
        validateTimes(claims, requirements, timeout);
        for (Map.Entry<String, JsonValue> requirement : requirements.requiredClaims().values().entrySet()) {
            if (!claims.claim(requirement.getKey()).filter(requirement.getValue()::equals).isPresent()) {
                throw new ValidateException("JWT required claim is absent or has a different JSON value");
            }
        }
    }

    /**
     * Applies required-presence, TimeGuard, ordering, and maximum-age rules.
     *
     * @param claims       verified JWT Claims Set
     * @param requirements validation requirements
     * @param timeout      shared operation budget
     */
    private void validateTimes(final JwtClaims claims, final Requirements requirements, final Timeout.Budget timeout) {
        final Optional<Instant> expiration = claims.expiration();
        final Optional<Instant> issuedAt = claims.issuedAt();
        if (requirements.expirationRequired() && expiration.isEmpty()) {
            throw new ValidateException("JWT expiration claim is required");
        }
        if (requirements.issuedAtRequired() && issuedAt.isEmpty()) {
            throw new ValidateException("JWT issued-at claim is required");
        }
        expiration.ifPresent(value -> timeGuard.validateExpiration(value, timeout));
        issuedAt.ifPresent(value -> timeGuard.validateIssuedAt(value, timeout));
        claims.notBefore().ifPresent(value -> timeGuard.validateNotBefore(value, timeout));
        if (issuedAt.isPresent() && expiration.isPresent()
                && !issuedAt.getOrThrow().isBefore(expiration.getOrThrow())) {
            throw new ValidateException("JWT issued-at claim must precede expiration");
        }
        if (claims.notBefore().isPresent() && expiration.isPresent()
                && !claims.notBefore().getOrThrow().isBefore(expiration.getOrThrow())) {
            throw new ValidateException("JWT not-before claim must precede expiration");
        }
        if (requirements.maximumAge().isPresent()) {
            final Instant issued = issuedAt
                    .orElseThrow(() -> new ValidateException("JWT maximum age requires issued-at"));
            try {
                if (!issued.plus(requirements.maximumAge().getOrThrow()).isAfter(timeout.clock().now())) {
                    throw new ValidateException("JWT exceeds the permitted maximum age");
                }
            } catch (ArithmeticException cause) {
                throw new ValidateException("JWT maximum-age calculation exceeds the supported time range", cause);
            }
        }
    }

    /**
     * Defines common JWT claim requirements without introducing protocol-specific semantics.
     *
     * @param issuer             exact required issuer
     * @param audiences          non-empty allowed audience set
     * @param subjectRequired    whether sub must be present
     * @param expirationRequired whether exp must be present
     * @param issuedAtRequired   whether iat must be present
     * @param jwtIdRequired      whether jti must be present
     * @param maximumAge         optional positive age measured from iat
     * @param replay             optional atomic jti replay registration context
     * @param requiredClaims     exact additional JsonValue claim requirements
     * @author Kimi Liu
     */
    public record Requirements(String issuer, Set<String> audiences, boolean subjectRequired,
            boolean expirationRequired, boolean issuedAtRequired, boolean jwtIdRequired, Optional<Duration> maximumAge,
            Optional<Replay> replay, JsonValue.ObjectValue requiredClaims) {

        /**
         * Validates and freezes common claim requirements.
         *
         * @throws IllegalArgumentException if a component or collection entry is {@code null}
         * @throws ValidateException        if issuer/audience is blank, maximum age is not positive, or replay lacks
         *                                  exp/jti
         */
        public Requirements {
            Assert.notBlank(issuer, "JWT required issuer must not be blank");
            Assert.notNull(audiences, "JWT allowed audiences must not be null");
            Assert.notNull(maximumAge, "JWT maximum age container must not be null");
            Assert.notNull(replay, "JWT replay requirement must not be null");
            Assert.notNull(requiredClaims, "JWT exact required claims must not be null");
            audiences = Set.copyOf(audiences);
            requiredClaims = new JsonValue.ObjectValue(requiredClaims.values());
            if (audiences.isEmpty()) {
                throw new ValidateException("JWT allowed audiences must not be empty");
            }
            for (String audience : audiences) {
                Assert.notBlank(audience, "JWT allowed audience must not be blank");
            }
            if (maximumAge.filter(age -> age.isZero() || age.isNegative()).isPresent()) {
                throw new ValidateException("JWT maximum age must be positive");
            }
            if (replay.isPresent() && (!expirationRequired || !jwtIdRequired)) {
                throw new ValidateException("JWT replay validation requires exp and an explicit jti requirement");
            }
        }

    }

    /**
     * Supplies formal protocol isolation fields required by ReplayGuard for a JWT jti.
     *
     * @param namespace external registration namespace
     * @param protocol  formal protocol that owns the JWT
     * @param authority stable Provider or Source authority
     * @author Kimi Liu
     */
    public record Replay(String namespace, Protocol protocol, String authority) {

        /**
         * Validates replay isolation fields.
         */
        public Replay {
            Assert.notBlank(namespace, "JWT replay namespace must not be blank");
            Assert.notNull(protocol, "JWT replay protocol must not be null");
            Assert.notBlank(authority, "JWT replay authority must not be blank");
        }

    }

}
