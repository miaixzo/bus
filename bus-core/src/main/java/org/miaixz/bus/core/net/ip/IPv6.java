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
package org.miaixz.bus.core.net.ip;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.regex.Pattern;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.NetKit;

/**
 * Parses, validates, and converts IPv6 addresses.
 *
 * @author Kimi Liu
 */
public class IPv6 {

    /**
     * Marker for a compressed run of zero-valued IPv6 groups.
     */
    private static final String COMPRESSION = Symbol.COLON + Symbol.COLON;

    /**
     * Excess leading zeros before a four-digit hexadecimal group.
     */
    private static final Pattern HEX_PADDING = Pattern.compile("(^|:)0+(?=[0-9a-fA-F]{4}(?=:|$))");

    /**
     * Cached local hostname.
     */
    private static volatile String localhostName;

    /**
     * Constructs a new IPv6 instance.
     */
    public IPv6() {
        // No initialization required.
    }

    /**
     * Parses an IPv6 address into its sixteen bytes in network order. Any scope identifier is discarded.
     *
     * @param address The IPv6 address text without brackets.
     * @return The address bytes, or an empty array if the text is invalid.
     * @throws NullPointerException If the address is {@code null}.
     */
    public static byte[] parseAddressToBytes(final String address) {
        int scope = address.indexOf(Symbol.C_PERCENT);
        String text = scope < Normal._0 ? address : address.substring(Normal._0, scope);
        if (text.indexOf(Symbol.C_COLON) < Normal._0) {
            return Normal.EMPTY_BYTE_ARRAY;
        }
        int compression = text.indexOf(COMPRESSION);
        if (compression >= Normal._0 && compression != text.lastIndexOf(COMPRESSION)) {
            return Normal.EMPTY_BYTE_ARRAY;
        }
        byte[] bytes = new byte[Normal._16];
        String head = compression < Normal._0 ? text : text.substring(Normal._0, compression);
        if (compression >= Normal._0 && head.indexOf(Symbol.C_DOT) >= Normal._0) {
            return Normal.EMPTY_BYTE_ARRAY;
        }
        int headBytes = parseGroups(head, bytes);
        if (headBytes < Normal._0) {
            return Normal.EMPTY_BYTE_ARRAY;
        }
        if (compression < Normal._0) {
            return headBytes == Normal._16 ? bytes : Normal.EMPTY_BYTE_ARRAY;
        }
        byte[] tail = new byte[Normal._16];
        int tailBytes = parseGroups(text.substring(compression + COMPRESSION.length()), tail);
        if (tailBytes < Normal._0 || headBytes + tailBytes >= Normal._16) {
            return Normal.EMPTY_BYTE_ARRAY;
        }
        System.arraycopy(tail, Normal._0, bytes, Normal._16 - tailBytes, tailBytes);
        return bytes;
    }

    /**
     * Parses an unscoped IPv6 literal without DNS. Square brackets are optional. An IPv4-mapped literal yields an IPv4
     * address.
     *
     * @param literal The IPv6 address literal.
     * @return The parsed address.
     * @throws IllegalArgumentException If the literal is invalid or contains a scope identifier.
     * @throws NullPointerException     If the literal is {@code null}.
     */
    public static InetAddress parseLiteral(final String literal) {
        Objects.requireNonNull(literal, "IPv6 address literal");
        if (literal.indexOf(Symbol.C_PERCENT) >= Normal._0) {
            throw new IllegalArgumentException("Invalid unscoped IPv6 address literal");
        }
        String address = literal.startsWith(Symbol.BRACKET_LEFT) && literal.endsWith(Symbol.BRACKET_RIGHT)
                ? literal.substring(Normal._1, literal.length() - Normal._1)
                : literal;
        int tailStart = address.lastIndexOf(Symbol.C_COLON) + Normal._1;
        String tail = address.substring(tailStart);
        if (tail.indexOf(Symbol.C_DOT) >= Normal._0) {
            if (tail.length() > Normal._15) {
                throw new IllegalArgumentException("Invalid IPv6 address literal");
            }
            address = address.substring(Normal._0, tailStart) + IPv4.normalizeDecimalPadding(tail);
        }
        byte[] bytes = parseAddressToBytes(HEX_PADDING.matcher(address).replaceAll("$1"));
        try {
            return InetAddress.getByAddress(bytes);
        } catch (final UnknownHostException invalid) {
            throw new IllegalArgumentException("Invalid IPv6 address literal", invalid);
        }
    }

    /**
     * Writes colon-separated groups, including an optional final dotted IPv4 address.
     *
     * @param groups The group text.
     * @param bytes  The destination buffer.
     * @return The number of bytes written, or {@code -1} if the groups are invalid.
     */
    private static int parseGroups(final String groups, final byte[] bytes) {
        if (groups.isEmpty()) {
            return Normal._0;
        }
        String[] parts = groups.split(Symbol.COLON, Normal.__1);
        int offset = Normal._0;
        for (int i = Normal._0; i < parts.length; i++) {
            String part = parts[i];
            if (i == parts.length - Normal._1 && part.indexOf(Symbol.C_DOT) >= Normal._0) {
                byte[] ipv4 = IPv4.parseAddressToBytes(part);
                if (ipv4.length != Integer.BYTES
                        || part.chars().filter(digit -> digit == Symbol.C_DOT).count() != Normal._3
                        || offset + Integer.BYTES > bytes.length) {
                    return Normal.__1;
                }
                System.arraycopy(ipv4, Normal._0, bytes, offset, Integer.BYTES);
                return offset + Integer.BYTES;
            }
            int group = parseHexGroup(part);
            if (group < Normal._0 || offset + Short.BYTES > bytes.length) {
                return Normal.__1;
            }
            bytes[offset++] = (byte) (group >>> Byte.SIZE);
            bytes[offset++] = (byte) group;
        }
        return offset;
    }

    /**
     * Parses one group of up to four ASCII hexadecimal digits.
     *
     * @param part The group text.
     * @return The group value, or {@code -1} if the text is invalid.
     */
    private static int parseHexGroup(final String part) {
        if (part.isEmpty() || part.length() > Normal._4) {
            return Normal.__1;
        }
        int value = Normal._0;
        for (int i = Normal._0; i < part.length(); i++) {
            char digit = part.charAt(i);
            int number = digit < Normal._128 ? Character.digit(digit, Normal._16) : Normal.__1;
            if (number < Normal._0) {
                return Normal.__1;
            }
            value = (value << Normal._4) | number;
        }
        return value;
    }

    /**
     * Converts an IPv6 address string to a {@link BigInteger}.
     *
     * @param ipv6Str The IPv6 address string.
     * @return The {@link BigInteger} representation, or null on failure.
     */
    public static BigInteger ipv6ToBigInteger(final String ipv6Str) {
        try {
            final InetAddress address = InetAddress.getByName(ipv6Str);
            if (address instanceof Inet6Address) {
                return new BigInteger(1, address.getAddress());
            }
        } catch (final UnknownHostException ignore) {
            // Ignore invalid address format
        }
        return null;
    }

    /**
     * Converts a {@link BigInteger} to an IPv6 address string.
     *
     * @param bigInteger The big integer representing an IPv6 address.
     * @return The IPv6 address string, or null on failure.
     */
    public static String bigIntegerToIPv6(final BigInteger bigInteger) {
        if (bigInteger.compareTo(BigInteger.ZERO) < 0
                || bigInteger.compareTo(new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16)) > 0) {
            throw new IllegalArgumentException("BigInteger value is out of IPv6 range");
        }

        byte[] bytes = bigInteger.toByteArray();
        if (bytes.length > 16) {
            // If the byte array is longer than 16, remove leading zeros.
            final int offset = bytes[0] == 0 ? 1 : 0;
            final byte[] newBytes = new byte[16];
            System.arraycopy(bytes, offset, newBytes, 0, 16);
            bytes = newBytes;
        } else if (bytes.length < 16) {
            // If the byte array is shorter than 16, pad with leading zeros.
            final byte[] paddedBytes = new byte[16];
            System.arraycopy(bytes, 0, paddedBytes, 16 - bytes.length, bytes.length);
            bytes = paddedBytes;
        }

        try {
            return Inet6Address.getByAddress(bytes).getHostAddress();
        } catch (final UnknownHostException e) {
            // This should not happen with a 16-byte array.
            return null;
        }
    }

    /**
     * Gets a list of all IPv6 addresses for the local machine, ordered by network interface.
     *
     * @return A {@link LinkedHashSet} of IP address strings.
     */
    public static LinkedHashSet<String> localIps() {
        final LinkedHashSet<InetAddress> localAddressList = NetKit.localAddressList(t -> t instanceof Inet6Address);
        return NetKit.toIpList(localAddressList);
    }

    /**
     * Gets the cached hostname of the local machine. Note: This method can trigger a reverse DNS lookup, which may
     * cause a delay depending on network conditions.
     *
     * @return The hostname.
     */
    public static String getLocalHostName() {
        if (null == localhostName) {
            synchronized (IPv6.class) {
                if (null == localhostName) {
                    localhostName = NetKit.getAddressName(getLocalhostDirectly());
                }
            }
        }
        return localhostName;
    }

    /**
     * Gets the MAC address of the network interface associated with the default local IPv6 address.
     *
     * @return The MAC address string.
     */
    public static String getLocalMacAddress() {
        return NetKit.getMacAddress(getLocalhost());
    }

    /**
     * Gets the hardware address (MAC address as a byte array) of the network interface associated with the default
     * local IPv6 address.
     *
     * @return The hardware address as a byte array.
     */
    public static byte[] getLocalHardwareAddress() {
        return NetKit.getHardwareAddress(getLocalhost());
    }

    /**
     * Gets the preferred non-loopback, non-site-local IPv6 address for the local machine. The result is cached for
     * subsequent calls.
     *
     * @return The local {@link InetAddress}, or {@code null} if not found.
     */
    public static InetAddress getLocalhost() {
        return Instances.get(IPv6.class.getName(), IPv6::getLocalhostDirectly);
    }

    /**
     * Gets the preferred non-loopback, non-site-local IPv6 address for the local machine without using a cache.
     *
     * @return The local {@link InetAddress}, or {@code null} if not found.
     */
    public static InetAddress getLocalhostDirectly() {
        final LinkedHashSet<InetAddress> localAddressList = NetKit
                .localAddressList(address -> address instanceof Inet6Address && !address.isLoopbackAddress() // Exclude
                // loopback
                // address
                // (::1)
                        && !address.isSiteLocalAddress() // Exclude site-local address (fec0::/10)
                        && !address.isLinkLocalAddress() // Exclude link-local address (fe80::/10)
                );

        if (CollKit.isNotEmpty(localAddressList)) {
            return CollKit.getFirst(localAddressList);
        }

        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            if (localHost instanceof Inet6Address) {
                return localHost;
            }
        } catch (final UnknownHostException e) {
            // Ignore and return null
        }
        return null;
    }

    /**
     * Normalizes an IPv6 address by replacing the scope name (e.g., {@code %en0}) with its numeric scope ID (e.g.,
     * {@code %5}).
     *
     * <pre>
     * fe80:0:0:0:894:aeec:f37d:23e1%en0  ->  fe80:0:0:0:894:aeec:f37d:23e1%5
     * </pre>
     *
     * @param address The IPv6 address to normalize.
     * @return The normalized IPv6 address.
     */
    public static InetAddress normalizeV6Address(final Inet6Address address) {
        final String addr = address.getHostAddress();
        final int index = addr.lastIndexOf(Symbol.C_PERCENT);
        if (index > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, index) + Symbol.C_PERCENT + address.getScopeId());
            } catch (final UnknownHostException e) {
                throw new InternalException(e);
            }
        }
        return address;
    }

}
