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
package org.miaixz.bus.fabric.network.dns.zone;

import java.net.InetAddress;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.policy.DnsPolicyRule;

/**
 * Immutable DNS view containing zones visible to one client group.
 *
 * @author Kimi Liu
 */
public class DnsView {

    /**
     * Default view name.
     */
    public static final String DEFAULT = "default";

    /**
     * View name.
     */
    private final String name;

    /**
     * Client CIDR blocks matched by this view.
     */
    private final List<CidrBlock> clientCidrs;

    /**
     * Zones visible in this view.
     */
    private final List<DnsZone> zones;

    /**
     * Policy rules scoped to this view.
     */
    private final List<DnsPolicyRule> policies;

    /**
     * Creates a DNS view.
     *
     * @param name  view name
     * @param zones visible zones
     */
    public DnsView(final String name, final List<DnsZone> zones) {
        this(name, defaultCidrs(), zones, List.of());
    }

    /**
     * Creates a DNS view.
     *
     * @param name        view name
     * @param clientCidrs client CIDR blocks matched by this view
     * @param zones       visible zones
     */
    public DnsView(final String name, final List<CidrBlock> clientCidrs, final List<DnsZone> zones) {
        this(name, clientCidrs, zones, List.of());
    }

    /**
     * Creates a DNS view.
     *
     * @param name        view name
     * @param clientCidrs client CIDR blocks matched by this view
     * @param zones       visible zones
     * @param policies    policy rules scoped to this view
     */
    public DnsView(final String name, final List<CidrBlock> clientCidrs, final List<DnsZone> zones,
            final List<DnsPolicyRule> policies) {
        if (name == null || name.isBlank()) {
            throw new ValidateException("DNS view name must be non-blank");
        }
        this.name = name.trim();
        this.clientCidrs = immutableCidrs(clientCidrs);
        this.zones = immutableZones(zones);
        this.policies = immutablePolicies(policies);
    }

    /**
     * Creates the default view.
     *
     * @param zones visible zones
     * @return default DNS view
     */
    public static DnsView defaults(final List<DnsZone> zones) {
        return new DnsView(DEFAULT, zones);
    }

    /**
     * Returns the view name.
     *
     * @return non-blank view name
     */
    public String name() {
        return name;
    }

    /**
     * Returns client CIDR blocks matched by this view.
     *
     * @return immutable client CIDR blocks
     */
    public List<CidrBlock> clientCidrs() {
        return clientCidrs;
    }

    /**
     * Returns zones visible in this view.
     *
     * @return immutable zones
     */
    public List<DnsZone> zones() {
        return zones;
    }

    /**
     * Returns policy rules scoped to this view.
     *
     * @return immutable policy rules
     */
    public List<DnsPolicyRule> policies() {
        return policies;
    }

    /**
     * Returns the longest matching CIDR prefix length for a client address.
     *
     * @param address client address
     * @return matching prefix length, or {@code -1}
     */
    public int matchPrefixLength(final InetAddress address) {
        int best = -1;
        for (final CidrBlock cidr : clientCidrs) {
            if (cidr.contains(address) && cidr.prefixLength() > best) {
                best = cidr.prefixLength();
            }
        }
        return best;
    }

    /**
     * Returns default client CIDRs matching all IPv4 and IPv6 clients.
     *
     * @return immutable default CIDR blocks
     */
    private static List<CidrBlock> defaultCidrs() {
        return List.of(CidrBlock.ipv4Any(), CidrBlock.ipv6Any());
    }

    /**
     * Validates and copies client CIDR blocks.
     *
     * @param clientCidrs source CIDR blocks
     * @return immutable CIDR blocks
     */
    private static List<CidrBlock> immutableCidrs(final List<CidrBlock> clientCidrs) {
        if (clientCidrs == null || clientCidrs.isEmpty()) {
            throw new ValidateException("DNS view client CIDRs must not be empty");
        }
        for (final CidrBlock cidr : clientCidrs) {
            if (cidr == null) {
                throw new ValidateException("DNS view client CIDRs must not contain null");
            }
        }
        return List.copyOf(clientCidrs);
    }

    /**
     * Validates and copies zones.
     *
     * @param zones source zones
     * @return immutable zones
     */
    private static List<DnsZone> immutableZones(final List<DnsZone> zones) {
        if (zones == null) {
            throw new ValidateException("DNS view zones must not be null");
        }
        for (final DnsZone zone : zones) {
            if (zone == null) {
                throw new ValidateException("DNS view zones must not contain null");
            }
        }
        return List.copyOf(zones);
    }

    /**
     * Validates and copies policy rules.
     *
     * @param policies source policy rules
     * @return immutable policy rules
     */
    private static List<DnsPolicyRule> immutablePolicies(final List<DnsPolicyRule> policies) {
        if (policies == null) {
            throw new ValidateException("DNS view policies must not be null");
        }
        for (final DnsPolicyRule policy : policies) {
            if (policy == null) {
                throw new ValidateException("DNS view policies must not contain null");
            }
        }
        return List.copyOf(policies);
    }

}
