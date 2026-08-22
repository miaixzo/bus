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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.dns.DnsResult;
import org.miaixz.bus.fabric.network.dns.zone.CidrBlock;

/**
 * Enforces one {@link AddressPolicy} over client destinations and server peers. Client validation checks the logical
 * scheme and port, every DNS answer, the selected numeric address, and the post-connect numeric address used for DNS
 * rebinding detection. Server validation applies the separate peer CIDR set. IPv4-mapped IPv6 addresses are reduced to
 * their IPv4 bytes before classification and CIDR matching. The guard performs no DNS lookup, opens no socket, owns no
 * resource, and is immutable and thread-safe.
 *
 * @author Kimi Liu
 */
public class AddressGuard {

    /**
     * Special-purpose networks that are not globally routable by default.
     */
    private static final List<CidrBlock> NON_GLOBAL_NETWORKS = List.of(
            CidrBlock.parse("0.0.0.0/8"),
            CidrBlock.parse("10.0.0.0/8"),
            CidrBlock.parse("100.64.0.0/10"),
            CidrBlock.parse("127.0.0.0/8"),
            CidrBlock.parse("169.254.0.0/16"),
            CidrBlock.parse("172.16.0.0/12"),
            CidrBlock.parse("192.0.0.0/24"),
            CidrBlock.parse("192.0.2.0/24"),
            CidrBlock.parse("192.88.99.0/24"),
            CidrBlock.parse("192.168.0.0/16"),
            CidrBlock.parse("198.18.0.0/15"),
            CidrBlock.parse("198.51.100.0/24"),
            CidrBlock.parse("203.0.113.0/24"),
            CidrBlock.parse("224.0.0.0/4"),
            CidrBlock.parse("240.0.0.0/4"),
            CidrBlock.parse("::/128"),
            CidrBlock.parse("::1/128"),
            CidrBlock.parse("100::/64"),
            CidrBlock.parse("2001:2::/48"),
            CidrBlock.parse("2001:10::/28"),
            CidrBlock.parse("2001:20::/28"),
            CidrBlock.parse("2001:db8::/32"),
            CidrBlock.parse("2002::/16"),
            CidrBlock.parse("3fff::/20"),
            CidrBlock.parse("5f00::/16"),
            CidrBlock.parse("fc00::/7"),
            CidrBlock.parse("fe80::/10"),
            CidrBlock.parse("ff00::/8"));

    /**
     * Globally reachable protocol assignments inside a broader special-purpose network.
     */
    private static final List<CidrBlock> GLOBAL_EXCEPTIONS = List
            .of(CidrBlock.parse("192.0.0.9/32"), CidrBlock.parse("192.0.0.10/32"));

    /**
     * Immutable policy enforced by this guard.
     */
    private final AddressPolicy policy;

    /**
     * Creates a guard for one explicit immutable policy.
     *
     * @param policy non-null address policy
     * @throws ValidateException when the policy is {@code null}
     */
    public AddressGuard(AddressPolicy policy) throws ValidateException {
        if (policy == null) {
            throw new ValidateException("Address policy must not be null");
        }
        this.policy = policy;
    }

    /**
     * Validates the logical client address and every address in one DNS result, then returns the first numeric address
     * in resolver order. A disallowed answer rejects the entire result rather than silently selecting another answer.
     *
     * @param target logical client destination
     * @param result DNS result for the target host
     * @return first validated normalized numeric address
     * @throws ValidateException when an argument is null, the result host differs from the target host, or no address
     *                           was resolved
     * @throws ProtocolException when the scheme, port, or any resolved address violates the policy
     */
    public InetAddress checkTarget(Address target, DnsResult result) throws ValidateException, ProtocolException {
        requireTarget(target);
        if (result == null) {
            throw new ValidateException("DNS result must not be null");
        }
        if (!target.host().equals(result.host())) {
            throw new ValidateException("DNS result host must match the target host");
        }
        return checkTarget(target, result.addresses());
    }

    /**
     * Validates the logical client address and every supplied DNS address without performing a lookup.
     *
     * @param target    logical client destination
     * @param addresses complete resolver-ordered DNS address snapshot
     * @return first validated normalized numeric address
     * @throws ValidateException when an argument is null, an element is null, or the list is empty
     * @throws ProtocolException when the scheme, port, or any address violates the target policy
     */
    public InetAddress checkTarget(Address target, List<InetAddress> addresses)
            throws ValidateException, ProtocolException {
        requireTarget(target);
        List<InetAddress> normalized = normalizeAll(addresses);
        for (InetAddress address : normalized) {
            requireAllowed(address, policy.targetNetworks());
        }
        return normalized.get(0);
    }

    /**
     * Validates every numeric address used to reach a configured intermediary without applying the logical target
     * scheme and port rules.
     *
     * @param result complete resolver result for the physical route
     * @return first validated normalized numeric route address
     * @throws ValidateException when the result or its address list is invalid
     * @throws ProtocolException when any route address violates the network policy
     */
    public InetAddress checkRoute(DnsResult result) throws ValidateException, ProtocolException {
        if (result == null) {
            throw new ValidateException("Route DNS result must not be null");
        }
        return checkRoute(result.addresses());
    }

    /**
     * Validates every supplied numeric address used to reach a configured intermediary.
     *
     * @param addresses complete resolver-ordered route address snapshot
     * @return first validated normalized numeric route address
     * @throws ValidateException when the address list is invalid
     * @throws ProtocolException when any route address violates the network policy
     */
    public InetAddress checkRoute(List<InetAddress> addresses) throws ValidateException, ProtocolException {
        List<InetAddress> normalized = normalizeAll(addresses);
        for (InetAddress address : normalized) {
            requireAllowed(address, policy.targetNetworks());
        }
        return normalized.get(0);
    }

    /**
     * Validates a local binding scheme and port, then returns its first numeric address. Local wildcard and interface
     * addresses are valid binding choices, while multicast addresses are rejected.
     *
     * @param local  logical local binding
     * @param result complete resolver result for the local host
     * @return first validated normalized binding address
     * @throws ValidateException when the binding or result is invalid
     * @throws ProtocolException when the scheme, port, or address is not valid for a local binding
     */
    public InetAddress checkBinding(Address local, DnsResult result) throws ValidateException, ProtocolException {
        requireTarget(local);
        if (result == null) {
            throw new ValidateException("Binding DNS result must not be null");
        }
        if (!local.host().equals(result.host())) {
            throw new ValidateException("Binding DNS result host must match the local host");
        }
        List<InetAddress> normalized = normalizeAll(result.addresses());
        for (InetAddress address : normalized) {
            if (address.isMulticastAddress()) {
                throw rejected();
            }
        }
        return normalized.get(0);
    }

    /**
     * Validates a post-connect numeric address against a fresh complete DNS result and requires it to be one of that
     * result's answers. Callers use this method immediately before application data to reject DNS rebinding.
     *
     * @param target           logical client destination
     * @param freshResult      fresh DNS result resolved for this connection attempt
     * @param connectedAddress actual numeric address selected by the connection
     * @return normalized connected address
     * @throws ValidateException when an argument or DNS result is invalid
     * @throws ProtocolException when policy validation fails or the actual address is absent from the fresh result
     */
    public InetAddress checkConnectedTarget(Address target, DnsResult freshResult, InetAddress connectedAddress)
            throws ValidateException, ProtocolException {
        checkTarget(target, freshResult);
        InetAddress normalizedConnected = normalize(connectedAddress);
        boolean found = freshResult.addresses().stream().map(AddressGuard::normalize)
                .anyMatch(candidate -> candidate.equals(normalizedConnected));
        if (!found) {
            throw rejected();
        }
        requireAllowed(normalizedConnected, policy.targetNetworks());
        return normalizedConnected;
    }

    /**
     * Validates a connected intermediary address against the complete DNS result used for that physical route.
     *
     * @param freshResult      complete DNS result used for the connection attempt
     * @param connectedAddress actual numeric address selected by the connection
     * @return normalized connected route address
     * @throws ValidateException when an argument or DNS result is invalid
     * @throws ProtocolException when policy validation fails or the connected address is absent from the result
     */
    public InetAddress checkConnectedRoute(DnsResult freshResult, InetAddress connectedAddress)
            throws ValidateException, ProtocolException {
        checkRoute(freshResult);
        InetAddress normalizedConnected = normalize(connectedAddress);
        boolean found = freshResult.addresses().stream().map(AddressGuard::normalize)
                .anyMatch(candidate -> candidate.equals(normalizedConnected));
        if (!found) {
            throw rejected();
        }
        requireAllowed(normalizedConnected, policy.targetNetworks());
        return normalizedConnected;
    }

    /**
     * Validates one accepted server peer against the peer network policy.
     *
     * @param peer numeric peer address reported by the accepted channel or datagram
     * @return normalized validated peer address
     * @throws ValidateException when the peer is {@code null}
     * @throws ProtocolException when the peer address violates the policy
     */
    public InetAddress checkPeer(InetAddress peer) throws ValidateException, ProtocolException {
        InetAddress normalized = normalize(peer);
        requireAllowed(normalized, policy.peerNetworks());
        return normalized;
    }

    /**
     * Requires an accepted transport peer to match an explicitly configured peer network before its forwarded address
     * metadata can be trusted.
     *
     * @param peer numeric transport peer
     * @return normalized trusted peer address
     * @throws ValidateException when the peer is {@code null}
     * @throws ProtocolException when no explicit peer network contains the address
     */
    public InetAddress checkTrustedPeer(InetAddress peer) throws ValidateException, ProtocolException {
        InetAddress normalized = normalize(peer);
        if (normalized.isAnyLocalAddress() || normalized.isMulticastAddress()
                || policy.peerNetworks().stream().noneMatch(network -> network.contains(normalized))) {
            throw rejected();
        }
        return normalized;
    }

    /**
     * Returns the immutable policy enforced by this guard.
     *
     * @return address policy
     */
    public AddressPolicy policy() {
        return policy;
    }

    /**
     * Validates the logical scheme and destination port before DNS answers are considered.
     *
     * @param target logical target
     * @throws ValidateException when the target is {@code null}
     * @throws ProtocolException when the target scheme or port is not allowed
     */
    private void requireTarget(Address target) throws ValidateException, ProtocolException {
        if (target == null) {
            throw new ValidateException("Target address must not be null");
        }
        if (!policy.allowedSchemes().contains(target.protocol()) || !policy.allowedPorts().contains(target.port())) {
            throw rejected();
        }
    }

    /**
     * Normalizes a complete DNS result while preserving order and removing duplicate byte addresses.
     *
     * @param addresses DNS addresses
     * @return non-empty normalized immutable address list
     * @throws ValidateException when the list or an element is null or the list is empty
     */
    private static List<InetAddress> normalizeAll(List<InetAddress> addresses) throws ValidateException {
        if (addresses == null || addresses.isEmpty()) {
            throw new ValidateException("DNS addresses must not be empty");
        }
        Set<InetAddress> unique = new LinkedHashSet<>();
        for (InetAddress address : addresses) {
            unique.add(normalize(address));
        }
        return List.copyOf(new ArrayList<>(unique));
    }

    /**
     * Applies global-route classification and an explicit CIDR exception set.
     *
     * @param address          normalized numeric address
     * @param explicitNetworks CIDRs allowed for this target or peer direction
     * @throws ProtocolException when an address is always forbidden or a non-global address lacks a matching CIDR
     */
    private static void requireAllowed(InetAddress address, Set<CidrBlock> explicitNetworks) throws ProtocolException {
        if (address.isAnyLocalAddress() || address.isMulticastAddress()) {
            throw rejected();
        }
        if (!isGloballyRoutable(address) && explicitNetworks.stream().noneMatch(network -> network.contains(address))) {
            throw rejected();
        }
    }

    /**
     * Returns whether an address is globally routable according to the embedded special-purpose network registry.
     *
     * @param address normalized address
     * @return {@code true} when the address is not covered by a non-global network
     */
    private static boolean isGloballyRoutable(InetAddress address) {
        if (GLOBAL_EXCEPTIONS.stream().anyMatch(network -> network.contains(address))) {
            return true;
        }
        return NON_GLOBAL_NETWORKS.stream().noneMatch(network -> network.contains(address));
    }

    /**
     * Normalizes an address and converts raw IPv4-mapped IPv6 bytes into an IPv4 address.
     *
     * @param address address to normalize
     * @return normalized address
     * @throws ValidateException when the address is {@code null} or mapped bytes cannot be represented
     */
    private static InetAddress normalize(InetAddress address) throws ValidateException {
        if (address == null) {
            throw new ValidateException("Numeric address must not be null");
        }
        byte[] bytes = address.getAddress();
        if (isMapped(bytes)) {
            try {
                return InetAddress.getByAddress(Arrays.copyOfRange(bytes, Normal._16 - Normal._4, Normal._16));
            } catch (UnknownHostException exception) {
                throw new ValidateException("Mapped IPv4 address is invalid", exception);
            }
        }
        return address;
    }

    /**
     * Identifies raw IPv4-mapped IPv6 bytes using the ten-zero and two-{@code ff} prefix.
     *
     * @param bytes address bytes
     * @return {@code true} for an IPv4-mapped IPv6 address
     */
    private static boolean isMapped(byte[] bytes) {
        if (bytes.length != Normal._16) {
            return false;
        }
        for (int index = Normal._0; index < Normal._10; index++) {
            if (bytes[index] != Normal._0) {
                return false;
            }
        }
        return bytes[Normal._10] == (byte) 0xff && bytes[Normal._11] == (byte) 0xff;
    }

    /**
     * Creates the stable shared IP-rejection exception without leaking an address or host.
     *
     * @return protocol exception carrying the shared IP rejection error
     */
    private static ProtocolException rejected() {
        return new ProtocolException(ErrorCode._100903);
    }

}
