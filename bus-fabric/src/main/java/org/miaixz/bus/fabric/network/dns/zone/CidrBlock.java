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
import java.net.UnknownHostException;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable IPv4 or IPv6 CIDR block used to select DNS split-horizon views.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CidrBlock {

    /**
     * IPv4 address byte length.
     */
    private static final int IPV4_BYTES = 4;

    /**
     * IPv6 address byte length.
     */
    private static final int IPV6_BYTES = 16;

    /**
     * Network address bytes.
     */
    private final byte[] network;

    /**
     * Prefix length in bits.
     */
    private final int prefixLength;

    /**
     * Textual CIDR notation.
     */
    private final String notation;

    /**
     * Creates a CIDR block.
     *
     * @param network      network address
     * @param prefixLength prefix length in bits
     */
    public CidrBlock(final InetAddress network, final int prefixLength) {
        if (network == null) {
            throw new ValidateException("CIDR network address must not be null");
        }
        final byte[] address = network.getAddress();
        validatePrefix(address.length, prefixLength);
        this.network = masked(address, prefixLength);
        this.prefixLength = prefixLength;
        this.notation = network.getHostAddress() + Symbol.SLASH + prefixLength;
    }

    /**
     * Parses CIDR notation.
     *
     * @param notation CIDR notation such as {@code 10.0.0.0/8}
     * @return CIDR block
     */
    public static CidrBlock parse(final String notation) {
        if (notation == null || notation.isBlank()) {
            throw new ValidateException("CIDR notation must be non-blank");
        }
        final String[] parts = notation.trim().split(Symbol.SLASH, -1);
        if (parts.length != 2) {
            throw new ValidateException("CIDR notation must contain one prefix separator");
        }
        try {
            return new CidrBlock(InetAddress.getByName(parts[0]), Integer.parseInt(parts[1]));
        } catch (final UnknownHostException | NumberFormatException e) {
            throw new ValidateException("CIDR notation is invalid", e);
        }
    }

    /**
     * Returns an all-clients IPv4 CIDR block.
     *
     * @return IPv4 any block
     */
    public static CidrBlock ipv4Any() {
        return parse("0.0.0.0/0");
    }

    /**
     * Returns an all-clients IPv6 CIDR block.
     *
     * @return IPv6 any block
     */
    public static CidrBlock ipv6Any() {
        return parse("::/0");
    }

    /**
     * Returns whether an address is contained in this CIDR block.
     *
     * @param address client address
     * @return true when the address is inside this CIDR block
     */
    public boolean contains(final InetAddress address) {
        if (address == null) {
            return false;
        }
        final byte[] candidate = address.getAddress();
        return candidate.length == network.length && Arrays.equals(masked(candidate, prefixLength), network);
    }

    /**
     * Returns the network address.
     *
     * @return defensive copy of network address bytes
     */
    public byte[] network() {
        return Arrays.copyOf(network, network.length);
    }

    /**
     * Returns the prefix length.
     *
     * @return prefix length in bits
     */
    public int prefixLength() {
        return prefixLength;
    }

    /**
     * Returns the textual CIDR notation.
     *
     * @return CIDR notation
     */
    public String notation() {
        return notation;
    }

    /**
     * Returns the textual CIDR notation.
     *
     * @return CIDR notation
     */
    @Override
    public String toString() {
        return notation;
    }

    /**
     * Validates a prefix length against an address family.
     *
     * @param addressBytes address byte length
     * @param prefixLength prefix length in bits
     */
    private static void validatePrefix(final int addressBytes, final int prefixLength) {
        final int maximum = addressBytes == IPV4_BYTES ? IPV4_BYTES * 8 : IPV6_BYTES * 8;
        if (addressBytes != IPV4_BYTES && addressBytes != IPV6_BYTES) {
            throw new ValidateException("CIDR address family is unsupported");
        }
        if (prefixLength < 0 || prefixLength > maximum) {
            throw new ValidateException("CIDR prefix length is out of range");
        }
    }

    /**
     * Applies a network mask to address bytes.
     *
     * @param address      source address bytes
     * @param prefixLength prefix length in bits
     * @return masked address bytes
     */
    private static byte[] masked(final byte[] address, final int prefixLength) {
        final byte[] masked = Arrays.copyOf(address, address.length);
        int remaining = prefixLength;
        for (int index = 0; index < masked.length; index++) {
            if (remaining >= 8) {
                remaining -= 8;
                continue;
            }
            if (remaining <= 0) {
                masked[index] = 0;
            } else {
                masked[index] = (byte) (masked[index] & (0xff << (8 - remaining)));
                remaining = 0;
            }
        }
        return masked;
    }

}
