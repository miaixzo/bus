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
package org.miaixz.bus.fabric.guard.route;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.network.dns.zone.CidrBlock;

/**
 * Defines an immutable network address policy for explicit Fabric client and server installation points. The policy
 * owns defensive snapshots of allowed URI schemes, destination ports, client target exceptions, and server peer
 * exceptions. It performs configuration validation only; DNS resolution, address classification, rebinding detection,
 * and connection admission belong to {@link AddressGuard}. Empty CIDR sets allow only globally routable addresses and
 * never act as an unrestricted address wildcard.
 *
 * @author Kimi Liu
 */
public class AddressPolicy {

    /**
     * Immutable non-empty set of allowed network schemes.
     */
    private final Set<Protocol> allowedSchemes;

    /**
     * Immutable non-empty set of allowed destination ports in the range 1 through 65535.
     */
    private final Set<Integer> allowedPorts;

    /**
     * Immutable normalized client target CIDR exceptions.
     */
    private final Set<String> allowedTargetCidrs;

    /**
     * Immutable normalized server peer CIDR exceptions.
     */
    private final Set<String> allowedPeerCidrs;

    /**
     * Parsed immutable client target exceptions used without reparsing configuration.
     */
    private final Set<CidrBlock> targetNetworks;

    /**
     * Parsed immutable server peer exceptions used without reparsing configuration.
     */
    private final Set<CidrBlock> peerNetworks;

    /**
     * Creates a validated immutable address policy. CIDR entries are exceptions for non-global addresses and must be
     * numeric network addresses with host bits cleared. Multicast and unspecified networks are rejected. Loopback and
     * link-local addresses may be registered only as an exact host prefix, preventing a broad local-network exception.
     *
     * @param allowedSchemes     non-empty allowed schemes
     * @param allowedPorts       non-empty destination ports in the range 1 through 65535
     * @param allowedTargetCidrs client target CIDR notation, possibly empty
     * @param allowedPeerCidrs   server peer CIDR notation, possibly empty
     * @throws ValidateException when a collection or element is null, a required set is empty, a port is outside its
     *                           valid range, or CIDR notation violates the network policy constraints
     */
    public AddressPolicy(Set<Protocol> allowedSchemes, Set<Integer> allowedPorts, Set<String> allowedTargetCidrs,
            Set<String> allowedPeerCidrs) throws ValidateException {
        this.allowedSchemes = schemes(allowedSchemes);
        this.allowedPorts = ports(allowedPorts);
        this.targetNetworks = networks(allowedTargetCidrs, "Target CIDR set must not be null");
        this.peerNetworks = networks(allowedPeerCidrs, "Peer CIDR set must not be null");
        this.allowedTargetCidrs = notations(targetNetworks);
        this.allowedPeerCidrs = notations(peerNetworks);
    }

    /**
     * Returns the immutable allowed scheme snapshot.
     *
     * @return allowed schemes
     */
    public Set<Protocol> allowedSchemes() {
        return allowedSchemes;
    }

    /**
     * Returns the immutable allowed destination port snapshot.
     *
     * @return allowed ports
     */
    public Set<Integer> allowedPorts() {
        return allowedPorts;
    }

    /**
     * Returns normalized immutable CIDR notation permitted as client target exceptions.
     *
     * @return allowed target CIDRs
     */
    public Set<String> allowedTargetCidrs() {
        return allowedTargetCidrs;
    }

    /**
     * Returns normalized immutable CIDR notation permitted as server peer exceptions.
     *
     * @return allowed peer CIDRs
     */
    public Set<String> allowedPeerCidrs() {
        return allowedPeerCidrs;
    }

    /**
     * Returns parsed target networks to the package guard without exposing mutable state.
     *
     * @return immutable parsed target networks
     */
    Set<CidrBlock> targetNetworks() {
        return targetNetworks;
    }

    /**
     * Returns parsed peer networks to the package guard without exposing mutable state.
     *
     * @return immutable parsed peer networks
     */
    Set<CidrBlock> peerNetworks() {
        return peerNetworks;
    }

    /**
     * Validates and snapshots the required scheme set.
     *
     * @param values candidate schemes
     * @return immutable scheme set
     * @throws ValidateException when the set is null, empty, or contains null
     */
    private static Set<Protocol> schemes(Set<Protocol> values) throws ValidateException {
        if (values == null || values.isEmpty() || values.stream().anyMatch(Objects::isNull)) {
            throw new ValidateException("Allowed schemes must be non-empty and contain no null elements");
        }
        return Set.copyOf(values);
    }

    /**
     * Validates and snapshots the required port set.
     *
     * @param values candidate ports
     * @return immutable port set
     * @throws ValidateException when the set is null, empty, contains null, or contains an out-of-range port
     */
    private static Set<Integer> ports(Set<Integer> values) throws ValidateException {
        if (values == null || values.isEmpty()) {
            throw new ValidateException("Allowed ports must not be empty");
        }
        LinkedHashSet<Integer> checked = new LinkedHashSet<>();
        for (Integer value : values) {
            if (value == null || value < Normal._1 || value > Normal._65535) {
                throw new ValidateException("Allowed port must be between 1 and 65535");
            }
            checked.add(value);
        }
        return Set.copyOf(checked);
    }

    /**
     * Parses and snapshots a CIDR collection without resolving host names.
     *
     * @param values      candidate CIDR notation
     * @param nullMessage stable null-collection failure text
     * @return immutable parsed networks
     * @throws ValidateException when the collection or an entry is invalid
     */
    private static Set<CidrBlock> networks(Set<String> values, String nullMessage) throws ValidateException {
        if (values == null) {
            throw new ValidateException(nullMessage);
        }
        LinkedHashSet<CidrBlock> networks = new LinkedHashSet<>();
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            CidrBlock block = parseNetwork(value);
            if (!unique.add(block.notation())) {
                throw new ValidateException("CIDR set must not contain duplicate networks");
            }
            networks.add(block);
        }
        return Set.copyOf(networks);
    }

    /**
     * Converts parsed networks to immutable normalized notation.
     *
     * @param networks parsed networks
     * @return immutable normalized notation
     */
    private static Set<String> notations(Set<CidrBlock> networks) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (CidrBlock network : networks) {
            values.add(network.notation());
        }
        return Set.copyOf(values);
    }

    /**
     * Parses one numeric CIDR and enforces network and local-address exception constraints.
     *
     * @param notation candidate CIDR notation
     * @return validated CIDR block
     * @throws ValidateException when the notation is not a permitted numeric network
     */
    private static CidrBlock parseNetwork(String notation) throws ValidateException {
        if (notation == null || notation.isBlank() || !notation.equals(notation.trim())) {
            throw new ValidateException("CIDR notation must be non-blank text without surrounding whitespace");
        }
        String[] parts = notation.split(Symbol.SLASH, -Normal._1);
        if (parts.length != Normal._2) {
            throw new ValidateException("CIDR notation must contain exactly one prefix separator");
        }
        InetAddress address = numericAddress(parts[0]);
        int prefixLength;
        try {
            prefixLength = Integer.parseInt(parts[Normal._1]);
        } catch (NumberFormatException exception) {
            throw new ValidateException("CIDR prefix length is invalid", exception);
        }
        CidrBlock block = new CidrBlock(address, prefixLength);
        if (!Arrays.equals(address.getAddress(), block.network())) {
            throw new ValidateException("CIDR address must have all host bits cleared");
        }
        if (address.isAnyLocalAddress() || address.isMulticastAddress()) {
            throw new ValidateException("Unspecified and multicast CIDR networks are not allowed");
        }
        int exactPrefix = address.getAddress().length * Byte.SIZE;
        if ((address.isLoopbackAddress() || address.isLinkLocalAddress()) && prefixLength != exactPrefix) {
            throw new ValidateException("Loopback and link-local CIDR entries must identify one exact address");
        }
        return block;
    }

    /**
     * Parses a numeric IPv4 or IPv6 address while rejecting host names, scoped literals, and ambiguous IPv4 text.
     *
     * @param value numeric address text
     * @return parsed numeric address
     * @throws ValidateException when the text is not a strict numeric address
     */
    private static InetAddress numericAddress(String value) throws ValidateException {
        if (value == null || value.isEmpty() || value.indexOf(Symbol.C_PERCENT) >= 0) {
            throw new ValidateException("CIDR address must be an unscoped numeric address");
        }
        if (value.indexOf(Symbol.C_COLON) >= 0) {
            try {
                InetAddress address = InetAddress.getByName(value);
                if (address.getAddress().length != Normal._16) {
                    throw new ValidateException("IPv4-mapped CIDR notation must use its IPv4 network");
                }
                return address;
            } catch (UnknownHostException exception) {
                throw new ValidateException("CIDR IPv6 address is invalid", exception);
            }
        }
        String[] octets = value.split("\\.", -Normal._1);
        if (octets.length != Normal._4) {
            throw new ValidateException("CIDR IPv4 address must contain four octets");
        }
        byte[] bytes = new byte[Normal._4];
        for (int index = 0; index < octets.length; index++) {
            String octet = octets[index];
            if (octet.isEmpty() || octet.length() > Normal._1 && octet.charAt(Normal._0) == '0') {
                throw new ValidateException("CIDR IPv4 octets must use canonical decimal text");
            }
            int number;
            try {
                number = Integer.parseInt(octet);
            } catch (NumberFormatException exception) {
                throw new ValidateException("CIDR IPv4 octet is invalid", exception);
            }
            if (number < Normal._0 || number >= Normal._256) {
                throw new ValidateException("CIDR IPv4 octet is out of range");
            }
            bytes[index] = (byte) number;
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException exception) {
            throw new ValidateException("CIDR IPv4 address is invalid", exception);
        }
    }

}
