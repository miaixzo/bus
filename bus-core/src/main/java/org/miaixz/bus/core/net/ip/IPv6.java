/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.net.ip;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.NetKit;

/**
 * A utility class for IPv6 addresses.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IPv6 {

    /**
     * Cached local hostname.
     */
    private static volatile String localhostName;

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
        final LinkedHashSet<InetAddress> localAddressList = NetKit.localAddressList(
                address -> address instanceof Inet6Address && !address.isLoopbackAddress() // Exclude
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
