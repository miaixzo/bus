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
public class JwtValidator {

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
    private final Optional<ReplayGuard> replayGuard;

    /**
     * Creates a synchronous JWT validator without replay-cache requirements.
     *
     * @param issuerValidator   exact issuer validator
     * @param audienceValidator audience allow-list validator
     * @param timeGuard         shared-clock temporal validator
     */
    public JwtValidator(final IssuerValidator issuerValidator, final AudienceValidator audienceValidator,
            final TimeGuard timeGuard) {
        this(issuerValidator, audienceValidator, timeGuard, Optional.empty());
    }

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
        this(issuerValidator, audienceValidator, timeGuard,
                Optional.of(Assert.notNull(replayGuard, "JWT replay guard must not be null")));
    }

    /**
     * Creates a JWT validator with an explicit optional replay capability.
     *
     * @param issuerValidator   exact issuer validator
     * @param audienceValidator audience allow-list validator
     * @param timeGuard         shared-clock temporal validator
     * @param replayGuard       optional atomic replay validator
     */
    private JwtValidator(final IssuerValidator issuerValidator, final AudienceValidator audienceValidator,
            final TimeGuard timeGuard, final Optional<ReplayGuard> replayGuard) {
        this.issuerValidator = Assert.notNull(issuerValidator, "JWT issuer validator must not be null");
        this.audienceValidator = Assert.notNull(audienceValidator, "JWT audience validator must not be null");
        this.timeGuard = Assert.notNull(timeGuard, "JWT time guard must not be null");
        this.replayGuard = Assert.notNull(replayGuard, "JWT replay guard container must not be null");
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
     * Validates every registered temporal claim that is present without imposing issuer or audience requirements.
     *
     * @param jwt cryptographically verified JWT
     * @return the same immutable JWT after successful temporal validation
     */
    public JWT validate(final JWT jwt) {
        Assert.notNull(jwt, "JWT must not be null");
        validatePresentTimes(jwt.claims());
        return jwt;
    }

    /**
     * Synchronously validates common JWT claims when replay registration is not requested.
     *
     * @param jwt          cryptographically verified JWT
     * @param requirements explicit common-claim requirements without replay registration
     * @return the same immutable JWT after successful common-claim validation
     * @throws ValidateException if replay registration is requested or a claim requirement fails
     */
    public JWT validate(final JWT jwt, final Requirements requirements) {
        Assert.notNull(jwt, "JWT must not be null");
        Assert.notNull(requirements, "JWT validation requirements must not be null");
        if (requirements.replay().isPresent()) {
            throw new ValidateException("Synchronous JWT validation does not perform replay registration");
        }
        validateClaims(jwt.claims(), requirements);
        return jwt;
    }

    /**
     * Validates common JWT claims and atomically records jti when replay policy is enabled.
     *
     * @param jwt          cryptographically verified JWT
     * @param requirements explicit common-claim requirements
     * @param context      current immutable authentication context
     * @param timeout      shared end-to-end operation timeout
     * @return stage containing the accepted JWT, expected rejection, or replay-cache failure
     */
    public CompletionStage<Outcome<JWT>> validate(
            final JWT jwt,
            final Requirements requirements,
            final Context context,
            final Timeout timeout) {
        Assert.notNull(jwt, "JWT must not be null");
        Assert.notNull(requirements, "JWT validation requirements must not be null");
        Assert.notNull(context, "JWT validation context must not be null");
        Assert.notNull(timeout, "JWT validation timeout must not be null");
        try {
            timeGuard.validateTimeout(timeout);
            validateClaims(jwt.claims(), requirements);
        } catch (ValidateException cause) {
            return CompletableFuture.completedFuture(Outcome.rejected(failure("JWT common claim validation failed")));
        }
        if (requirements.replay().isEmpty()) {
            return CompletableFuture.completedFuture(Outcome.succeeded(jwt));
        }
        final Replay replay = requirements.replay().getOrThrow();
        final ReplayGuard guard = replayGuard
                .orElseThrow(() -> new ValidateException("JWT replay validation requires a replay guard"));
        return guard.register(
                replay.space(),
                replay.protocol(),
                replay.authority(),
                "jwt-jti",
                jwt.claims().jwtId().orElseThrow(),
                jwt.claims().expiration().orElseThrow(),
                timeout).thenApply(outcome -> switch (outcome) {
                    case Outcome.Succeeded<Void> ignored -> Outcome.succeeded(jwt);
                    case Outcome.Rejected<Void> rejected -> Outcome.rejected(rejected.failure());
                    case Outcome.Failed<Void> failed -> Outcome.failed(failed.failure());
                    default -> throw new IllegalStateException("Unsupported Outcome implementation");
                });
    }

    /**
     * Applies all synchronous common-claim rules before replay registration.
     *
     * @param claims       verified JWT Claims Set
     * @param requirements validation requirements
     */
    private void validateClaims(final JwtClaims claims, final Requirements requirements) {
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
        validateTimes(claims, requirements);
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
     */
    private void validateTimes(final JwtClaims claims, final Requirements requirements) {
        final Optional<Instant> expiration = claims.expiration();
        final Optional<Instant> issuedAt = claims.issuedAt();
        if (requirements.expirationRequired() && expiration.isEmpty()) {
            throw new ValidateException("JWT expiration claim is required");
        }
        if (requirements.issuedAtRequired() && issuedAt.isEmpty()) {
            throw new ValidateException("JWT issued-at claim is required");
        }
        expiration.ifPresent(timeGuard::validateExpiration);
        issuedAt.ifPresent(timeGuard::validateIssuedAt);
        claims.notBefore().ifPresent(timeGuard::validateNotBefore);
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
            timeGuard.validateMaximumAge(issued, requirements.maximumAge().getOrThrow());
        }
    }

    /**
     * Validates present registered temporal claims and their relative ordering.
     *
     * @param claims verified JWT Claims Set
     */
    private void validatePresentTimes(final JwtClaims claims) {
        final Optional<Instant> expiration = claims.expiration();
        final Optional<Instant> issuedAt = claims.issuedAt();
        expiration.ifPresent(timeGuard::validateExpiration);
        issuedAt.ifPresent(timeGuard::validateIssuedAt);
        claims.notBefore().ifPresent(timeGuard::validateNotBefore);
        if (issuedAt.isPresent() && expiration.isPresent()
                && !issuedAt.getOrThrow().isBefore(expiration.getOrThrow())) {
            throw new ValidateException("JWT issued-at claim must precede expiration");
        }
        if (claims.notBefore().isPresent() && expiration.isPresent()
                && !claims.notBefore().getOrThrow().isBefore(expiration.getOrThrow())) {
            throw new ValidateException("JWT not-before claim must precede expiration");
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
     * @param space     external Source space
     * @param protocol  formal protocol that owns the JWT
     * @param authority stable Provider or Source authority
     * @author Kimi Liu
     */
    public record Replay(String space, Protocol protocol, String authority) {

        /**
         * Validates replay isolation fields.
         */
        public Replay {
            Assert.notBlank(space, "JWT replay space must not be blank");
            Assert.notNull(protocol, "JWT replay protocol must not be null");
            Assert.notBlank(authority, "JWT replay authority must not be blank");
        }

    }

}
