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
package org.miaixz.bus.auth.shared;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.cache.ReplayCache;
import org.miaixz.bus.auth.guard.AlgorithmGuard;
import org.miaixz.bus.auth.guard.ReplayGuard;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.guard.body.LimitGuard;
import org.miaixz.bus.fabric.guard.route.AddressGuard;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;
import org.miaixz.bus.fabric.guard.tls.TlsGuard;

/**
 * Freezes the non-relaxable authentication security policy for every configured industry protocol.
 * <p>
 * One instance is shared by all Provider, Source, and Vendor runtimes. It stores policy values and composes existing
 * authentication and Fabric guards; it performs no cryptographic primitive, network I/O, protocol serialization, or
 * options resolution and supplies no permissive defaults.
 * </p>
 *
 * @author Kimi Liu
 */
public final class SecurityBaseline {

    /**
     * Immutable policy index keyed by exact bus-core Protocol.
     */
    private final Map<Protocol, Policy> policies;

    /**
     * Creates a complete explicit protocol security baseline.
     *
     * @param policies non-empty protocol policy map
     * @throws IllegalArgumentException if the map, a key, or a value is {@code null}
     * @throws ValidateException        if the map is empty
     */
    public SecurityBaseline(final Map<Protocol, Policy> policies) {
        Assert.notNull(policies, "Security baseline policy map must not be null");
        if (policies.isEmpty()) {
            throw new ValidateException("Security baseline must contain at least one protocol policy");
        }
        final Map<Protocol, Policy> copy = new LinkedHashMap<>(policies.size());
        policies.forEach(
                (protocol, policy) -> copy.put(
                        Assert.notNull(protocol, "Security baseline Protocol must not be null"),
                        Assert.notNull(policy, "Security baseline Policy must not be null")));
        this.policies = Map.copyOf(copy);
    }

    /**
     * Tests whether every candidate network permission is contained by the baseline permission set.
     *
     * @param candidate profile-specific address policy
     * @param baseline  shared baseline address policy
     * @return {@code true} when schemes, ports, target CIDRs, and peer CIDRs are all subsets
     */
    private static boolean addressSubset(final AddressPolicy candidate, final AddressPolicy baseline) {
        return baseline.allowedSchemes().containsAll(candidate.allowedSchemes())
                && baseline.allowedPorts().containsAll(candidate.allowedPorts())
                && baseline.allowedTargetCidrs().containsAll(candidate.allowedTargetCidrs())
                && baseline.allowedPeerCidrs().containsAll(candidate.allowedPeerCidrs());
    }

    /**
     * Returns the complete immutable protocol policy map.
     *
     * @return immutable security policies
     */
    public Map<Protocol, Policy> policies() {
        return policies;
    }

    /**
     * Returns the required security policy for an exact protocol.
     *
     * @param protocol exact industry protocol
     * @return configured protocol security policy
     * @throws IllegalArgumentException if {@code protocol} is {@code null}
     * @throws NotFoundException        if no policy exists for the protocol
     */
    public Policy require(final Protocol protocol) {
        Assert.notNull(protocol, "Security baseline Protocol must not be null");
        final Policy policy = policies.get(protocol);
        if (policy == null) {
            throw new NotFoundException("Security baseline policy not found for Protocol " + protocol.name());
        }
        return policy;
    }

    /**
     * Verifies that a profile policy only tightens the shared protocol baseline.
     *
     * @param protocol  exact owning protocol
     * @param candidate profile-specific policy candidate
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if any security dimension is relaxed
     */
    public void validateTightening(final Protocol protocol, final Policy candidate) {
        final Policy baseline = require(protocol);
        Assert.notNull(candidate, "Candidate security policy must not be null");
        if (!baseline.algorithms().containsAll(candidate.algorithms())) {
            throw new ValidateException("Profile algorithm allowlist must be a baseline subset");
        }
        if (candidate.minimumEntropyBits() < baseline.minimumEntropyBits()) {
            throw new ValidateException("Profile minimum entropy must not be lower than the baseline");
        }
        if (candidate.maximumClockSkew().compareTo(baseline.maximumClockSkew()) > 0) {
            throw new ValidateException("Profile maximum clock skew must not exceed the baseline");
        }
        if (candidate.minimumReplayWindow().compareTo(baseline.minimumReplayWindow()) < 0) {
            throw new ValidateException("Profile replay window must not be shorter than the baseline");
        }
        if (candidate.maximumMessageBytes() > baseline.maximumMessageBytes()) {
            throw new ValidateException("Profile message limit must not exceed the baseline");
        }
        if (!addressSubset(candidate.addressPolicy(), baseline.addressPolicy())) {
            throw new ValidateException("Profile network address policy must be a baseline subset");
        }
        if (baseline.secureTransportRequired() && !candidate.secureTransportRequired()) {
            throw new ValidateException("Profile must not disable baseline secure transport");
        }
    }

    /**
     * Creates the stateless shared algorithm selection guard.
     *
     * @return new stateless AlgorithmGuard
     */
    public AlgorithmGuard algorithmGuard() {
        return new AlgorithmGuard();
    }

    /**
     * Creates a TimeGuard using one protocol's maximum skew and the supplied shared Clock.
     *
     * @param protocol exact owning protocol
     * @param clock    shared Fabric time source
     * @return protocol-scoped TimeGuard
     */
    public TimeGuard timeGuard(final Protocol protocol, final Clock clock) {
        return new TimeGuard(clock, require(protocol).maximumClockSkew());
    }

    /**
     * Creates a ReplayGuard over the required atomic replay cache.
     *
     * @param cache atomic replay digest cache
     * @return replay guard
     */
    public ReplayGuard replayGuard(final ReplayCache cache) {
        return new ReplayGuard(cache);
    }

    /**
     * Creates a Fabric AddressGuard from one protocol's immutable address policy.
     *
     * @param protocol exact owning protocol
     * @return protocol-scoped Fabric address guard
     */
    public AddressGuard addressGuard(final Protocol protocol) {
        return new AddressGuard(require(protocol).addressPolicy());
    }

    /**
     * Returns the shared secure TLS guard when the protocol baseline requires secure transport.
     *
     * @param protocol exact owning protocol
     * @return secure TLS guard or empty when this baseline permits another protected transport
     */
    public Optional<TlsGuard> tlsGuard(final Protocol protocol) {
        return require(protocol).secureTransportRequired() ? Optional.of(TlsGuard.requireSecure()) : Optional.empty();
    }

    /**
     * Creates a Fabric body-size guard from one protocol's maximum message size.
     *
     * @param protocol exact owning protocol
     * @return protocol-scoped Fabric body limit guard
     */
    public LimitGuard messageGuard(final Protocol protocol) {
        return LimitGuard.of(require(protocol).maximumMessageBytes());
    }

    /**
     * Carries immutable lower bounds and upper limits for one exact authentication protocol.
     *
     * @param algorithms              exact protocol algorithm allowlist
     * @param minimumEntropyBits      minimum generated-secret entropy in bits
     * @param maximumClockSkew        maximum accepted timestamp displacement
     * @param minimumReplayWindow     minimum replay-digest retention duration
     * @param maximumMessageBytes     maximum accepted encoded message bytes
     * @param addressPolicy           immutable Fabric network address policy
     * @param secureTransportRequired whether Fabric secure-TLS guard is mandatory
     * @author Kimi Liu
     */
    public record Policy(Set<String> algorithms, int minimumEntropyBits, Duration maximumClockSkew,
            Duration minimumReplayWindow, long maximumMessageBytes, AddressPolicy addressPolicy,
            boolean secureTransportRequired) {

        /**
         * Creates a validated immutable protocol security policy.
         *
         * @param algorithms              exact algorithm identifiers; empty only when the protocol has no algorithm
         *                                selection here
         * @param minimumEntropyBits      positive minimum generated-secret entropy
         * @param maximumClockSkew        non-negative maximum timestamp displacement
         * @param minimumReplayWindow     positive minimum replay retention
         * @param maximumMessageBytes     positive Fabric-supported message limit
         * @param addressPolicy           immutable Fabric address policy
         * @param secureTransportRequired whether secure TLS transport is mandatory
         * @throws IllegalArgumentException if a collection, entry, duration, or policy is {@code null}
         * @throws ValidateException        if an algorithm is blank/none or a numeric or duration bound is invalid
         */
        public Policy {
            Assert.notNull(algorithms, "Security policy algorithm set must not be null");
            for (String algorithm : algorithms) {
                Assert.notBlank(algorithm, "Security policy algorithm must not be blank");
                if ("none".equalsIgnoreCase(algorithm)) {
                    throw new ValidateException("Security policy must not allow unsecured algorithm none");
                }
            }
            algorithms = Set.copyOf(algorithms);
            if (minimumEntropyBits <= 0) {
                throw new ValidateException("Security policy minimum entropy must be positive");
            }
            Assert.notNull(maximumClockSkew, "Security policy maximum clock skew must not be null");
            if (maximumClockSkew.isNegative()) {
                throw new ValidateException("Security policy maximum clock skew must not be negative");
            }
            Assert.notNull(minimumReplayWindow, "Security policy replay window must not be null");
            if (minimumReplayWindow.isNegative() || minimumReplayWindow.isZero()) {
                throw new ValidateException("Security policy replay window must be positive");
            }
            LimitGuard.of(maximumMessageBytes);
            Assert.notNull(addressPolicy, "Security policy address policy must not be null");
        }

    }

}
