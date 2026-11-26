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
package org.miaixz.bus.auth.guard.outbound;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.guard.GuardEvaluator.Mode;
import org.miaixz.bus.auth.guard.GuardEvaluator.Rule;
import org.miaixz.bus.auth.guard.GuardEvaluator.Verified;
import org.miaixz.bus.auth.guard.token.ServiceKeyProvider;
import org.miaixz.bus.auth.guard.token.ServiceTokenIssuer;
import org.miaixz.bus.auth.guard.token.ServiceTokenProvider;
import org.miaixz.bus.auth.guard.token.ServiceTokenVerifier;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jwt.JWT;

/**
 * Applies next-hop rules and signs and caches service tokens for approved destinations.
 *
 * @author Kimi Liu
 */
public final class OutboundIssuer {

    /**
     * Signing policy for the local service.
     */
    private final OutboundPolicy policy;

    /**
     * Supplies the active signing key.
     */
    private final ServiceKeyProvider keys;

    /**
     * Performs cryptographic token operations using evaluator-selected keys and claims.
     */
    private final ServiceTokenProvider tokens;

    /**
     * Issued tokens for the current key version, isolated by destination and forwarding claims.
     */
    private final Map<Destination, Cached> issued = new HashMap<>();

    /**
     * Key version associated with the signing cache.
     */
    private ServiceKeyProvider.Key currentKey;

    /**
     * Creates an issuer with the supplied policy and token providers.
     *
     * @param policy configured next-hop policy
     * @param keys   active signing key source
     * @param tokens cryptographic service token provider
     */
    public OutboundIssuer(OutboundPolicy policy, ServiceKeyProvider keys, ServiceTokenProvider tokens) {
        this.policy = Objects.requireNonNull(policy);
        this.keys = Objects.requireNonNull(keys);
        this.tokens = Objects.requireNonNull(tokens);
    }

    /**
     * Reports whether the effective next-hop policy enables service token issuance.
     *
     * @return whether outbound issuance is enabled
     */
    public boolean enabled() {
        return policy.enabled();
    }

    /**
     * Signs the authenticated caller and selected route together for the destination service.
     *
     * @param route           original gateway route, or null for a direct client
     * @param audience        trusted destination service code
     * @param path            context-relative destination path
     * @param fixed           fixed channel requirement, or null
     * @param asset           outbound mode declared by the asset, or null
     * @param assetControlled whether this route requires an explicit asset opt-in
     * @param caller          service identity verified at this hop, or null for a direct client
     * @param assetId         identifier selected by the forwarding hop, or null when omitted
     * @return compact token or null when the route does not use service identity
     * @throws IllegalStateException when a required token cannot be issued
     */
    public String issue(
            String route,
            String audience,
            String path,
            Mode fixed,
            Mode asset,
            boolean assetControlled,
            Verified caller,
            String assetId) {
        if (fixed == Mode.OFF || fixed == null && (asset == Mode.OFF || assetControlled && asset == null))
            return null;
        Rule candidate = Rule.match(policy.rules(), route, audience, path);
        if (fixed != Mode.REQUIRED && asset != Mode.REQUIRED && (candidate == null || candidate.mode() == Mode.OFF)) {
            if (audience == null && policy.rules().stream()
                    .anyMatch(rule -> rule.mode() == Mode.REQUIRED && rule.matches(route, path)))
                throw new IllegalStateException("Required service identity is unavailable");
            return null;
        }
        if (!policy.enabled() || audience == null || candidate == null || candidate.mode() != Mode.REQUIRED)
            throw new IllegalStateException("Required service identity is unavailable");
        return issue(audience, caller == null ? null : caller.subject(), assetId);
    }

    /**
     * Issues an identity for a previously selected trusted destination. The caller must select that destination before
     * invoking this method.
     *
     * @param audience verified destination service code
     * @return compact service token
     */
    public String issue(String audience) {
        return issue(audience, null, null);
    }

    /**
     * Retrieves a cached issuer for the next-hop audience and optional original caller.
     *
     * @param audience selected destination service
     * @param origin   verified subject of the caller, or null for direct signing
     * @param assetId  registered route identifier, or null when omitted
     * @return compact service token
     */
    private synchronized String issue(String audience, String origin, String assetId) {
        if (!policy.enabled() || audience == null || audience.isBlank())
            throw new IllegalStateException("Required service identity is unavailable");
        try {
            Instant now = Instant.now();
            ServiceKeyProvider.Key key = keys.keys(now).signing();
            if (key == null || !key.active(now))
                throw new IllegalStateException("Required service identity is unavailable");
            if (currentKey == null || !key.version().equals(currentKey.version())) {
                issued.clear();
                currentKey = key;
            } else if (key != currentKey && !Arrays.equals(key.secret(), currentKey.secret())) {
                throw new IllegalStateException("Required service identity is unavailable");
            }
            var destination = new Destination(audience, origin, assetId);
            Cached cached = issued.get(destination);
            if (cached != null && now.isBefore(cached.refresh()))
                return cached.compact();
            var request = new ServiceTokenProvider.Issue(policy.subject(), audience, policy.ttl(), origin, assetId);
            String compact = tokens.issue(request, key);
            Instant expiration = verifyIssuedClaims(compact, request, now);
            Instant refresh = now.plusMillis(policy.ttl().toMillis() * 4 / 5);
            issued.put(destination, new Cached(compact, refresh.isBefore(expiration) ? refresh : expiration));
            return compact;
        } catch (RuntimeException failure) {
            throw new IllegalStateException("Required service identity is unavailable");
        }
    }

    /**
     * Ensures custom signers preserve the framework-selected token type, destination, lifetime, and forwarding claims.
     *
     * @param compact compact token returned by the signer
     * @param request claims and lifetime selected by the evaluator
     * @param now     time at which signing began
     * @return the issued expiration time used to bound cache reuse
     */
    private static Instant verifyIssuedClaims(String compact, ServiceTokenProvider.Issue request, Instant now) {
        var parsed = JWT.parse(compact);
        var data = parsed.claims();
        Instant expiration = data.expiration().orElseThrow();
        Instant issueTime = data.issuedAt().orElseThrow();
        if (!JwaAlgorithm.HS256.name().equals(parsed.header().algorithm())
                || !request.subject().equals(data.issuer().orElse(null))
                || !request.subject().equals(data.subject().orElse(null))
                || !List.of(request.audience()).equals(data.audiences()) || !expiration.isAfter(now)
                || issueTime.isAfter(Instant.now()) || !expiration.isAfter(issueTime)
                || expiration.isAfter(issueTime.plus(request.lifetime()))
                || !Objects
                        .equals(request.origin(), ServiceTokenVerifier.forwardingClaim(data, ServiceTokenIssuer.ORIGIN))
                || !Objects
                        .equals(request.asset(), ServiceTokenVerifier.forwardingClaim(data, ServiceTokenIssuer.ASSET)))
            throw new IllegalStateException("Required service identity is unavailable");
        return expiration;
    }

    /**
     * Compact token and the instant when it must be refreshed.
     *
     * @param compact signed token for a single destination and claim set
     * @param refresh exclusive end of cache use
     */
    private record Cached(String compact, Instant refresh) {
    }

    /**
     * Cache key separating direct tokens and forwarded tokens by their signed service identities.
     *
     * @param audience next-hop service code
     * @param origin   verified caller code, or null for a direct token
     * @param asset    selected route identifier, or null when omitted
     */
    private record Destination(String audience, String origin, String asset) {
    }

}
