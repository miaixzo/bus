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
package org.miaixz.bus.auth;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.FabricX.AddressPolicy;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Aggregates the immutable, non-relaxable execution security rules for every configured authentication protocol.
 * <p>
 * One instance is shared by all compiled Source workers and Vendor adapters. It stores rule values only; it performs no
 * cryptographic primitive, guard construction, network I/O, protocol serialization, options resolution, or business
 * authorization decision and supplies no permissive defaults.
 * </p>
 *
 * @author Kimi Liu
 */
public class Policies {

    /**
     * Immutable security rule index keyed by exact bus-core protocol.
     */
    private final Map<Protocol, Rule> rules;

    /**
     * Creates a complete explicit protocol security rule set.
     *
     * @param rules non-empty protocol security rule map
     * @throws IllegalArgumentException if the map, a key, or a value is {@code null}
     * @throws ValidateException        if the map is empty
     */
    public Policies(final Map<Protocol, Rule> rules) {
        Assert.notNull(rules, "Security rule map must not be null");
        if (rules.isEmpty()) {
            throw new ValidateException("Policies must contain at least one protocol security rule");
        }
        final Map<Protocol, Rule> copy = new LinkedHashMap<>(rules.size());
        rules.forEach(
                (protocol, rule) -> copy.put(
                        Assert.notNull(protocol, "Security rule protocol must not be null"),
                        Assert.notNull(rule, "Protocol security rule must not be null")));
        this.rules = Map.copyOf(copy);
    }

    /**
     * Tests whether every candidate network permission is contained by the required permission set.
     *
     * @param candidate protocol-specific address policy
     * @param required  shared required address policy
     * @return {@code true} when schemes, ports, target CIDRs, and peer CIDRs are all permitted by the required policy
     */
    private static boolean addressSubset(final AddressPolicy candidate, final AddressPolicy required) {
        return required.allowedSchemes().containsAll(candidate.allowedSchemes())
                && required.allowedPorts().containsAll(candidate.allowedPorts())
                && required.allowedTargetCidrs().containsAll(candidate.allowedTargetCidrs())
                && required.allowedPeerCidrs().containsAll(candidate.allowedPeerCidrs());
    }

    /**
     * Returns the complete immutable protocol security rule map.
     *
     * @return immutable protocol security rules
     */
    public Map<Protocol, Rule> rules() {
        return rules;
    }

    /**
     * Returns the required security rule for an exact protocol.
     *
     * @param protocol exact bus-core protocol
     * @return configured protocol security rule
     * @throws IllegalArgumentException if {@code protocol} is {@code null}
     * @throws NotFoundException        if no rule exists for the protocol
     */
    public Rule require(final Protocol protocol) {
        Assert.notNull(protocol, "Security rule protocol must not be null");
        final Rule rule = rules.get(protocol);
        if (rule == null) {
            throw new NotFoundException("Security rule not found for protocol " + protocol.name());
        }
        return rule;
    }

    /**
     * Verifies that a candidate rule only tightens the shared protocol security requirements.
     *
     * @param protocol  exact owning protocol
     * @param candidate protocol-specific security rule candidate
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if any security dimension is relaxed
     */
    public void validateTightening(final Protocol protocol, final Rule candidate) {
        final Rule required = require(protocol);
        Assert.notNull(candidate, "Candidate security rule must not be null");
        if (!required.algorithms().containsAll(candidate.algorithms())) {
            throw new ValidateException("Candidate algorithm allowlist must be a required-rule subset");
        }
        if (candidate.minimumEntropyBits() < required.minimumEntropyBits()) {
            throw new ValidateException("Candidate minimum entropy must not be lower than the required rule");
        }
        if (candidate.maximumClockSkew().compareTo(required.maximumClockSkew()) > 0) {
            throw new ValidateException("Candidate maximum clock skew must not exceed the required rule");
        }
        if (candidate.minimumReplayWindow().compareTo(required.minimumReplayWindow()) < 0) {
            throw new ValidateException("Candidate replay window must not be shorter than the required rule");
        }
        if (candidate.maximumMessageBytes() > required.maximumMessageBytes()) {
            throw new ValidateException("Candidate message limit must not exceed the required rule");
        }
        if (!addressSubset(candidate.addressPolicy(), required.addressPolicy())) {
            throw new ValidateException("Candidate network address policy must be a required-rule subset");
        }
        if (required.secureTransportRequired() && !candidate.secureTransportRequired()) {
            throw new ValidateException("Candidate must not disable required secure transport");
        }
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
    public record Rule(Set<String> algorithms, int minimumEntropyBits, Duration maximumClockSkew,
            Duration minimumReplayWindow, long maximumMessageBytes, AddressPolicy addressPolicy,
            boolean secureTransportRequired) {

        /**
         * Creates a validated immutable protocol security rule.
         *
         * @param algorithms              exact algorithm identifiers; empty only when the protocol has no algorithm
         *                                selection here
         * @param minimumEntropyBits      positive minimum generated-secret entropy
         * @param maximumClockSkew        non-negative maximum timestamp displacement
         * @param minimumReplayWindow     positive minimum replay retention
         * @param maximumMessageBytes     positive Fabric-supported message limit
         * @param addressPolicy           immutable Fabric address policy
         * @param secureTransportRequired whether secure TLS transport is mandatory
         * @throws IllegalArgumentException if a collection, entry, duration, or address policy is {@code null}
         * @throws ValidateException        if an algorithm is blank/none or a numeric or duration bound is invalid
         */
        public Rule {
            Assert.notNull(algorithms, "Security rule algorithm set must not be null");
            for (String algorithm : algorithms) {
                Assert.notBlank(algorithm, "Security rule algorithm must not be blank");
                if (Normal.NONE.equalsIgnoreCase(algorithm)) {
                    throw new ValidateException("Security rule must not allow unsecured algorithm none");
                }
            }
            algorithms = Set.copyOf(algorithms);
            if (minimumEntropyBits <= 0) {
                throw new ValidateException("Security rule minimum entropy must be positive");
            }
            Assert.notNull(maximumClockSkew, "Security rule maximum clock skew must not be null");
            if (maximumClockSkew.isNegative()) {
                throw new ValidateException("Security rule maximum clock skew must not be negative");
            }
            Assert.notNull(minimumReplayWindow, "Security rule replay window must not be null");
            if (minimumReplayWindow.isNegative() || minimumReplayWindow.isZero()) {
                throw new ValidateException("Security rule replay window must be positive");
            }
            if (maximumMessageBytes <= 0L || maximumMessageBytes > Normal.MEBI * Normal._16) {
                throw new ValidateException("Security rule message limit must be between 1 and 16777216 bytes");
            }
            Assert.notNull(addressPolicy, "Security rule address policy must not be null");
        }

    }

}
