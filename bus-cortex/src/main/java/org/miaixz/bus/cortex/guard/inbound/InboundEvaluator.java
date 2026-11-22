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
package org.miaixz.bus.cortex.guard.inbound;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.auth.FabricX;
import org.miaixz.bus.auth.guard.AudienceValidator;
import org.miaixz.bus.auth.guard.IssuerValidator;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jwt.JWT;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.auth.shared.jwt.JwtValidator;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.cortex.guard.GuardEvaluator.Asset;
import org.miaixz.bus.cortex.guard.GuardEvaluator.Mode;
import org.miaixz.bus.cortex.guard.GuardEvaluator.Rule;
import org.miaixz.bus.cortex.guard.GuardEvaluator.Verified;
import org.miaixz.bus.cortex.guard.inbound.InboundDecision.Status;
import org.miaixz.bus.cortex.guard.token.ServiceKeyProvider;
import org.miaixz.bus.cortex.guard.token.ServiceTokenIssuer;
import org.miaixz.bus.cortex.guard.token.ServiceTokenProvider;
import org.miaixz.bus.cortex.guard.token.ServiceTokenVerifier;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Selects the effective ingress policy and verifies the service identity for the current hop.
 *
 * @author Kimi Liu
 */
public final class InboundEvaluator {

    /**
     * Ingress policy for the local service.
     */
    private final InboundPolicy policy;

    /**
     * Supplies trusted verification candidates.
     */
    private final ServiceKeyProvider keys;

    /**
     * Performs cryptographic token operations using evaluator-selected keys and claims.
     */
    private final ServiceTokenProvider tokens;

    /**
     * Enforces the standard JWT claims for inbound callers.
     */
    private final JwtValidator claims;

    /**
     * Creates an inbound evaluator with the supplied policy and token providers.
     *
     * @param policy configured ingress policy
     * @param keys   trusted verification key source
     * @param tokens cryptographic service token provider
     */
    public InboundEvaluator(InboundPolicy policy, ServiceKeyProvider keys, ServiceTokenProvider tokens) {
        this.policy = Objects.requireNonNull(policy);
        this.keys = Objects.requireNonNull(keys);
        this.tokens = Objects.requireNonNull(tokens);
        this.claims = new JwtValidator(new IssuerValidator(), new AudienceValidator(),
                new TimeGuard(FabricX.clock(), policy.skew()));
    }

    /**
     * Determines the path-level inbound requirement without looking at any credential.
     *
     * @param path  context-relative ingress path
     * @param fixed fixed route requirement, or null for configurable paths
     * @return path-level requirement, before any asset override
     */
    public Mode mode(String path, Mode fixed) {
        return inboundMode(fixed, null, Rule.match(policy.rules(), null, null, path));
    }

    /**
     * Selects an inbound mode in fixed-boundary, asset and path priority order.
     *
     * @param fixed fixed route requirement
     * @param asset resolved asset policy
     * @param rule  effective path rule
     * @return inbound verification requirement
     */
    private static Mode inboundMode(Mode fixed, Asset asset, Rule rule) {
        if (fixed != null)
            return fixed;
        if (asset != null && asset.inbound() != null)
            return asset.inbound();
        return rule == null ? Mode.OFF : rule.mode();
    }

    /**
     * Evaluates configured inbound caller identity before business authorization.
     *
     * @param path   effective context-relative route path
     * @param tokens every instance of the service identity header
     * @param fixed  fixed route boundary, or null for rule-based selection
     * @param asset  policy from the selected asset, or null
     * @return decision and verified identity, if present
     */
    public InboundDecision evaluate(String path, List<String> tokens, Mode fixed, Asset asset) {
        Rule rule = Rule.match(policy.rules(), null, null, path);
        Mode mode = inboundMode(fixed, asset, rule);
        if (mode == Mode.OFF)
            return new InboundDecision(Status.ACCEPTED, null);
        if (!policy.enabled())
            return new InboundDecision(mode == Mode.REQUIRED ? Status.UNAVAILABLE : Status.ACCEPTED, null);
        Set<String> subjects = rule != null && !rule.codes().isEmpty() ? rule.codes() : policy.subjects();
        if (asset != null && !asset.subjects().isEmpty()) {
            Set<String> assigned = new HashSet<>(asset.subjects());
            if (!subjects.isEmpty())
                assigned.retainAll(subjects);
            subjects = assigned;
        }
        if (subjects.isEmpty())
            return new InboundDecision(Status.UNAVAILABLE, null);
        if (tokens.isEmpty() && mode == Mode.OPTIONAL)
            return new InboundDecision(Status.ACCEPTED, null);
        if (tokens.size() != Normal._1)
            return new InboundDecision(Status.REJECTED, null);
        Instant now = Instant.now();
        String compact = tokens.getFirst();
        try {
            var candidates = keys.keys(now).verification().stream().filter(key -> key.active(now)).toList();
            if (candidates.isEmpty())
                return new InboundDecision(Status.UNAVAILABLE, null);
            for (var candidate : candidates) {
                JWT verified = this.tokens.verify(compact, candidate);
                if (verified != null) {
                    Verified caller = validate(compact, verified, subjects);
                    if (caller == null || rule != null && !rule.origins().isEmpty() && (caller.origin() == null
                            || caller.asset() == null || !rule.origins().contains(caller.origin()))) {
                        return new InboundDecision(Status.REJECTED, null);
                    }
                    return new InboundDecision(Status.ACCEPTED, caller);
                }
            }
        } catch (RuntimeException failure) {
            return new InboundDecision(Status.UNAVAILABLE, null);
        }
        return new InboundDecision(Status.REJECTED, null);
    }

    /**
     * Checks the signed transport claims against the effective identity rules.
     *
     * @param compact  exact credential supplied for verification
     * @param verified JWT authenticated by the cryptographic provider
     * @param subjects caller identities configured for this request
     * @return verified service identity or null when the claims are invalid
     */
    private Verified validate(String compact, JWT verified, Set<String> subjects) {
        if (!compact.equals(verified.compact()) || !JwaAlgorithm.HS256.name().equals(verified.header().algorithm()))
            return null;
        JwtClaims data = verified.claims();
        String subject = data.subject().orElse(null);
        if (subject == null || !subjects.contains(subject))
            return null;
        try {
            claims.validate(
                    verified,
                    new JwtValidator.Requirements(subject, Set.of(policy.audience()), true, true, true, false,
                            Optional.empty(), Optional.empty(), new JsonValue.ObjectValue(Map.of())));
            String origin = ServiceTokenVerifier.forwardingClaim(data, ServiceTokenIssuer.ORIGIN);
            String asset = ServiceTokenVerifier.forwardingClaim(data, ServiceTokenIssuer.ASSET);
            return asset != null && origin == null ? null : new Verified(subject, policy.audience(), origin, asset);
        } catch (IllegalArgumentException | ValidateException invalid) {
            return null;
        }
    }

    /**
     * Request-local result of collecting the gateway's ingress service header.
     *
     * @param present whether the transport supplied the reserved header
     * @param values  preserved credential values for the identity check
     */
    public record Received(boolean present, List<String> values) {

        /**
         * Copies the values before the adapter changes its HTTP header view.
         *
         * @param present whether the transport supplied the reserved header
         * @param values  credential values to preserve
         */
        public Received {
            values = List.copyOf(values);
        }

        /**
         * Returns a safe description for diagnostics.
         *
         * @return header presence without credential values
         */
        @Override
        public String toString() {
            return "Received[present=" + present + "]";
        }

    }

}
