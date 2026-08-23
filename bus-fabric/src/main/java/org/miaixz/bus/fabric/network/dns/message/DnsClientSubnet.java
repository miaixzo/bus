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
package org.miaixz.bus.fabric.network.dns.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * EDNS Client Subnet value extracted from a DNS query.
 *
 * @author Kimi Liu
 */
public class DnsClientSubnet {

    /**
     * EDNS Client Subnet address family code for IPv4.
     */
    private static final int FAMILY_IPV4 = 1;

    /**
     * EDNS Client Subnet address family code for IPv6.
     */
    private static final int FAMILY_IPV6 = 2;

    /**
     * IPv4 address byte length.
     */
    private static final int IPV4_BYTES = 4;

    /**
     * IPv6 address byte length.
     */
    private static final int IPV6_BYTES = 16;

    /**
     * EDNS Client Subnet address family code.
     */
    private final int family;

    /**
     * Source prefix length in bits.
     */
    private final int sourcePrefixLength;

    /**
     * Scope prefix length in bits.
     */
    private final int scopePrefixLength;

    /**
     * Network address normalized to the source prefix.
     */
    private final InetAddress address;

    /**
     * Creates an EDNS Client Subnet value.
     *
     * @param address            subnet address
     * @param sourcePrefixLength source prefix length in bits
     * @param scopePrefixLength  scope prefix length in bits
     */
    public DnsClientSubnet(final InetAddress address, final int sourcePrefixLength, final int scopePrefixLength) {
        if (address == null) {
            throw new ValidateException("DNS client subnet address must not be null");
        }
        this.family = family(address);
        validatePrefix(sourcePrefixLength, address.getAddress().length, "DNS client subnet source prefix");
        validatePrefix(scopePrefixLength, address.getAddress().length, "DNS client subnet scope prefix");
        this.sourcePrefixLength = sourcePrefixLength;
        this.scopePrefixLength = scopePrefixLength;
        this.address = normalize(address, sourcePrefixLength);
    }

    /**
     * Decodes an EDNS Client Subnet option.
     *
     * @param family             EDNS address family code
     * @param sourcePrefixLength source prefix length in bits
     * @param scopePrefixLength  scope prefix length in bits
     * @param wireAddress        truncated wire address bytes
     * @return decoded client subnet value
     */
    static DnsClientSubnet fromWire(
            final int family,
            final int sourcePrefixLength,
            final int scopePrefixLength,
            final byte[] wireAddress) {
        final int length = addressLength(family);
        validatePrefix(sourcePrefixLength, length, "DNS client subnet source prefix");
        validatePrefix(scopePrefixLength, length, "DNS client subnet scope prefix");
        final int expected = wireAddressLength(sourcePrefixLength);
        if (wireAddress == null || wireAddress.length != expected) {
            throw new ProtocolException("DNS client subnet address length is invalid");
        }
        final byte[] full = new byte[length];
        System.arraycopy(wireAddress, 0, full, 0, wireAddress.length);
        try {
            return new DnsClientSubnet(InetAddress.getByAddress(full), sourcePrefixLength, scopePrefixLength);
        } catch (final UnknownHostException e) {
            throw new ProtocolException("DNS client subnet address family is invalid", e);
        }
    }

    /**
     * Returns the EDNS Client Subnet address family.
     *
     * @return address family code
     */
    public int family() {
        return family;
    }

    /**
     * Returns the source prefix length.
     *
     * @return source prefix length in bits
     */
    public int sourcePrefixLength() {
        return sourcePrefixLength;
    }

    /**
     * Returns the scope prefix length.
     *
     * @return scope prefix length in bits
     */
    public int scopePrefixLength() {
        return scopePrefixLength;
    }

    /**
     * Returns the normalized subnet address.
     *
     * @return normalized subnet address
     */
    public InetAddress address() {
        return address;
    }

    /**
     * Returns the address bytes as they must appear in EDNS Client Subnet RDATA.
     *
     * @return truncated network address bytes
     */
    byte[] wireAddress() {
        return Arrays.copyOf(address.getAddress(), wireAddressLength(sourcePrefixLength));
    }

    /**
     * Returns whether another object has the same subnet fields.
     *
     * @param other object being compared
     * @return true when the object has the same address family, prefixes, and normalized address
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DnsClientSubnet subnet)) {
            return false;
        }
        return family == subnet.family && sourcePrefixLength == subnet.sourcePrefixLength
                && scopePrefixLength == subnet.scopePrefixLength && address.equals(subnet.address);
    }

    /**
     * Returns the hash code for this subnet.
     *
     * @return stable hash code based on subnet fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(family, sourcePrefixLength, scopePrefixLength, address);
    }

    /**
     * Returns a stable textual form for cache partitioning and diagnostics.
     *
     * @return textual subnet form
     */
    @Override
    public String toString() {
        return address.getHostAddress() + Symbol.SLASH + sourcePrefixLength + Symbol.SLASH + scopePrefixLength;
    }

    /**
     * Returns the EDNS family code for an address.
     *
     * @param address internet address
     * @return EDNS Client Subnet family code
     */
    private static int family(final InetAddress address) {
        final int length = address.getAddress().length;
        if (length == IPV4_BYTES) {
            return FAMILY_IPV4;
        }
        if (length == IPV6_BYTES) {
            return FAMILY_IPV6;
        }
        throw new ValidateException("DNS client subnet address family is unsupported");
    }

    /**
     * Returns the address byte length for an EDNS Client Subnet family.
     *
     * @param family EDNS Client Subnet family code
     * @return address byte length
     */
    private static int addressLength(final int family) {
        if (family == FAMILY_IPV4) {
            return IPV4_BYTES;
        }
        if (family == FAMILY_IPV6) {
            return IPV6_BYTES;
        }
        throw new ProtocolException("DNS client subnet address family is unsupported");
    }

    /**
     * Validates a source or scope prefix.
     *
     * @param prefixLength prefix length in bits
     * @param addressBytes address byte length
     * @param name         diagnostic name
     */
    private static void validatePrefix(final int prefixLength, final int addressBytes, final String name) {
        if (prefixLength < 0 || prefixLength > addressBytes * 8) {
            throw new ValidateException(name + " is out of range");
        }
    }

    /**
     * Returns the number of address bytes required for a source prefix.
     *
     * @param sourcePrefixLength source prefix length in bits
     * @return truncated wire address byte length
     */
    private static int wireAddressLength(final int sourcePrefixLength) {
        return (sourcePrefixLength + 7) / 8;
    }

    /**
     * Normalizes an address by clearing bits outside the source prefix.
     *
     * @param address            source address
     * @param sourcePrefixLength source prefix length in bits
     * @return normalized address
     */
    private static InetAddress normalize(final InetAddress address, final int sourcePrefixLength) {
        final byte[] bytes = address.getAddress();
        int remaining = sourcePrefixLength;
        for (int index = 0; index < bytes.length; index++) {
            if (remaining >= 8) {
                remaining -= 8;
                continue;
            }
            if (remaining <= 0) {
                bytes[index] = 0;
            } else {
                bytes[index] = (byte) (bytes[index] & (0xff << (8 - remaining)));
                remaining = 0;
            }
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (final UnknownHostException e) {
            throw new ValidateException("DNS client subnet address is invalid", e);
        }
    }

}
