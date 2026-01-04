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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.MaskBit;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

/**
 * A utility class for IPv4 addresses.
 *
 * <p>
 * Glossary:
 *
 * <ul>
 * <li><b>IP String:</b> Dot-decimal notation, e.g., {@code xxx.xxx.xxx.xxx}.</li>
 * <li><b>Long IP:</b> A 32-bit long integer representation of an IP address.</li>
 * <li><b>Mask Address:</b> Dot-decimal notation for a subnet mask, e.g., {@code 255.255.255.0}.</li>
 * <li><b>Mask Bit:</b> An integer representing the number of leading 1s in a subnet mask, e.g., 24.</li>
 * <li><b>CIDR:</b> Classless Inter-Domain Routing, e.g., {@code 192.168.1.101/24}.</li>
 * <li><b>Total Addresses:</b> All IP addresses within a range, inclusive of the start and end.</li>
 * <li><b>Available Addresses:</b> All usable IP addresses within a range, exclusive of the network and broadcast
 * addresses.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IPv4 {

    /**
     * Constructs a new IPv4. Utility class constructor for static access.
     */
    public IPv4() {
    }

    /**
     * The numeric value of the default localhost IP address (127.0.0.1).
     */
    public static final long LOCAL_IP_NUM = IPv4.ipv4ToLong(Protocol.HOST_IPV4);
    /**
     * The minimum numeric value for an IPv4 address (0.0.0.0).
     */
    public static final long IPV4_NUM_MIN = IPv4.ipv4ToLong(Protocol.IPV4_STR_MIN);
    /**
     * The minimum numeric value for an unused IPv4 address (0.0.0.0).
     */
    public static final long IPV4_UNUSED_NUM_MIN = IPv4.ipv4ToLong(Protocol.IPV4_STR_MIN);
    /**
     * The maximum numeric value for an unused IPv4 address (0.255.255.255).
     */
    public static final long IPV4_UNUSED_NUM_MAX = IPv4.ipv4ToLong(Protocol.IPV4_UNUSED_STR_MAX);
    /**
     * The minimum possible mask bit length for IPv4 (0 represents no mask).
     */
    public static final int IPV4_MASK_BIT_MIN = 0;
    /**
     * The minimum valid mask bit length for a usable IPv4 network (1).
     */
    public static final int IPV4_MASK_BIT_VALID_MIN = 1;
    /**
     * The minimum valid subnet mask string for IPv4 (128.0.0.0).
     */
    public static final String IPV4_MASK_VALID_MIN = MaskBit.get(IPV4_MASK_BIT_VALID_MIN);

    /**
     * The maximum possible mask bit length for IPv4 (32 represents /32 mask).
     */
    public static final int IPV4_MASK_BIT_MAX = 32;
    /**
     * The maximum subnet mask string for IPv4 (255.255.255.255).
     */
    public static final String IPV4_MASK_MAX = MaskBit.get(IPV4_MASK_BIT_MAX);
    /**
     * The minimum string representation of IPv4 loopback address range (127.0.0.0).
     */
    public static final String IPV4_LOOPBACK_STR_MIN = "127.0.0.0";
    /**
     * The minimum numeric value of IPv4 loopback address range (127.0.0.0).
     */
    public static final long IPV4_LOOPBACK_NUM_MIN = IPv4.ipv4ToLong(IPV4_LOOPBACK_STR_MIN);
    /**
     * The maximum string representation of the IPv4 loopback address range.
     */
    /**
     * The maximum string representation of IPv4 loopback address range (127.255.255.255).
     */
    public static final String IPV4_LOOPBACK_STR_MAX = "127.255.255.255";
    /**
     * The maximum numeric value of the IPv4 loopback address range.
     */
    /**
     * The maximum numeric value of IPv4 loopback address range (127.255.255.255).
     */
    public static final long IPV4_LOOPBACK_NUM_MAX = IPv4.ipv4ToLong(IPV4_LOOPBACK_STR_MAX);
    /**
     * The minimum string representation of Class A IP address (0.0.0.0).
     */
    public static final String IPV4_A_STR_MIN = "0.0.0.0";
    /**
     * The minimum numeric value of Class A IP address (0.0.0.0).
     */
    public static final long IPV4_A_NUM_MIN = IPv4.ipv4ToLong(IPV4_A_STR_MIN);
    /**
     * The maximum string representation of Class A IP address (127.255.255.255).
     */
    public static final String IPV4_A_STR_MAX = "127.255.255.255";
    /**
     * The maximum numeric value of Class A IP address (127.255.255.255).
     */
    public static final long IPV4_A_NUM_MAX = IPv4.ipv4ToLong(IPV4_A_STR_MAX);
    /**
     * The minimum string representation of Class A public network range 1 (1.0.0.0).
     */
    public static final String IPV4_A_PUBLIC_1_STR_MIN = "1.0.0.0";
    /**
     * The minimum numeric value of Class A public network range 1 (1.0.0.0).
     */
    public static final long IPV4_A_PUBLIC_1_NUM_MIN = IPv4.ipv4ToLong(IPV4_A_PUBLIC_1_STR_MIN);
    /**
     * The maximum string representation of Class A public network range 1 (9.255.255.255).
     */
    public static final String IPV4_A_PUBLIC_1_STR_MAX = "9.255.255.255";
    /**
     * The maximum numeric value of Class A public network range 1 (9.255.255.255).
     */
    public static final long IPV4_A_PUBLIC_1_NUM_MAX = IPv4.ipv4ToLong(IPV4_A_PUBLIC_1_STR_MAX);
    /**
     * The minimum string representation of Class A private network range (10.0.0.0).
     */
    public static final String IPV4_A_PRIVATE_STR_MIN = "10.0.0.0";
    /**
     * The minimum numeric value of Class A private network range (10.0.0.0).
     */
    public static final long IPV4_A_PRIVATE_NUM_MIN = IPv4.ipv4ToLong(IPV4_A_PRIVATE_STR_MIN);
    /**
     * The maximum string representation of Class A private network range (10.255.255.255).
     */
    public static final String IPV4_A_PRIVATE_STR_MAX = "10.255.255.255";
    /**
     * The maximum numeric value of Class A private network range (10.255.255.255).
     */
    public static final long IPV4_A_PRIVATE_NUM_MAX = IPv4.ipv4ToLong(IPV4_A_PRIVATE_STR_MAX);
    /**
     * The minimum string representation of Class A public network range 2 (11.0.0.0).
     */
    public static final String IPV4_A_PUBLIC_2_STR_MIN = "11.0.0.0";
    /**
     * The minimum numeric value of Class A public network range 2 (11.0.0.0).
     */
    public static final long IPV4_A_PUBLIC_2_NUM_MIN = IPv4.ipv4ToLong(IPV4_A_PUBLIC_2_STR_MIN);
    /**
     * The maximum string representation of Class A public network range 2 (126.255.255.255).
     */
    public static final String IPV4_A_PUBLIC_2_STR_MAX = "126.255.255.255";
    /**
     * The maximum numeric value of Class A public network range 2 (126.255.255.255).
     */
    public static final long IPV4_A_PUBLIC_2_NUM_MAX = IPv4.ipv4ToLong(IPV4_A_PUBLIC_2_STR_MAX);
    /**
     * The minimum string representation of Class B IP address (128.0.0.0).
     */
    public static final String IPV4_B_STR_MIN = "128.0.0.0";
    /**
     * The minimum numeric value of Class B IP address (128.0.0.0).
     */
    public static final long IPV4_B_NUM_MIN = IPv4.ipv4ToLong(IPV4_B_STR_MIN);
    /**
     * The maximum string representation of Class B IP address (191.255.255.255).
     */
    public static final String IPV4_B_STR_MAX = "191.255.255.255";
    /**
     * The maximum numeric value of Class B IP address (191.255.255.255).
     */
    public static final long IPV4_B_NUM_MAX = IPv4.ipv4ToLong(IPV4_B_STR_MAX);
    /**
     * The minimum string representation of Class B public network range 1 (128.0.0.0).
     */
    public static final String IPV4_B_PUBLIC_1_STR_MIN = "128.0.0.0";
    /**
     * The minimum numeric value of Class B public network range 1 (128.0.0.0).
     */
    public static final long IPV4_B_PUBLIC_1_NUM_MIN = IPv4.ipv4ToLong(IPV4_B_PUBLIC_1_STR_MIN);
    /**
     * The maximum string representation of Class B public network range 1 (172.15.255.255).
     */
    public static final String IPV4_B_PUBLIC_1_STR_MAX = "172.15.255.255";
    /**
     * The maximum numeric value of Class B public network range 1 (172.15.255.255).
     */
    public static final long IPV4_B_PUBLIC_1_NUM_MAX = IPv4.ipv4ToLong(IPV4_B_PUBLIC_1_STR_MAX);
    /**
     * The minimum string representation of Class B private network range (172.16.0.0).
     */
    public static final String IPV4_B_PRIVATE_STR_MIN = "172.16.0.0";
    /**
     * The minimum numeric value of Class B private network range (172.16.0.0).
     */
    public static final long IPV4_B_PRIVATE_NUM_MIN = IPv4.ipv4ToLong(IPV4_B_PRIVATE_STR_MIN);
    /**
     * The maximum string representation of Class B private network range (172.31.255.255).
     */
    public static final String IPV4_B_PRIVATE_STR_MAX = "172.31.255.255";
    /**
     * The maximum numeric value of Class B private network range (172.31.255.255).
     */
    public static final long IPV4_B_PRIVATE_NUM_MAX = IPv4.ipv4ToLong(IPV4_B_PRIVATE_STR_MAX);
    /**
     * The minimum string representation of Class B public network range 2 (172.32.0.0).
     */
    public static final String IPV4_B_PUBLIC_2_STR_MIN = "172.32.0.0";
    /**
     * The minimum numeric value of Class B public network range 2 (172.32.0.0).
     */
    public static final long IPV4_B_PUBLIC_2_NUM_MIN = IPv4.ipv4ToLong(IPV4_B_PUBLIC_2_STR_MIN);
    /**
     * The maximum string representation of Class B public network range 2 (191.255.255.255).
     */
    public static final String IPV4_B_PUBLIC_2_STR_MAX = "191.255.255.255";
    /**
     * The maximum numeric value of Class B public network range 2 (191.255.255.255).
     */
    public static final long IPV4_B_PUBLIC_2_NUM_MAX = IPv4.ipv4ToLong(IPV4_B_PUBLIC_2_STR_MAX);
    /**
     * The minimum string representation of Class C IP address (192.0.0.0).
     */
    public static final String IPV4_C_STR_MIN = "192.0.0.0";
    /**
     * The minimum numeric value of Class C IP address (192.0.0.0).
     */
    public static final long IPV4_C_NUM_MIN = IPv4.ipv4ToLong(IPV4_C_STR_MIN);
    /**
     * The maximum string representation of Class C IP address (223.255.255.255).
     */
    public static final String IPV4_C_STR_MAX = "223.255.255.255";
    /**
     * The maximum numeric value of Class C IP address (223.255.255.255).
     */
    public static final long IPV4_C_NUM_MAX = IPv4.ipv4ToLong(IPV4_C_STR_MAX);
    /**
     * The minimum string representation of Class C public network range 1 (192.0.0.0).
     */
    public static final String IPV4_C_PUBLIC_1_STR_MIN = "192.0.0.0";
    /**
     * The minimum numeric value of Class C public network range 1 (192.0.0.0).
     */
    public static final long IPV4_C_PUBLIC_1_NUM_MIN = IPv4.ipv4ToLong(IPV4_C_PUBLIC_1_STR_MIN);
    /**
     * The maximum string representation of Class C public network range 1 (192.167.255.255).
     */
    public static final String IPV4_C_PUBLIC_1_STR_MAX = "192.167.255.255";
    /**
     * The maximum numeric value of Class C public network range 1 (192.167.255.255).
     */
    public static final long IPV4_C_PUBLIC_1_NUM_MAX = IPv4.ipv4ToLong(IPV4_C_PUBLIC_1_STR_MAX);
    /**
     * The minimum string representation of Class C private network range (192.168.0.0).
     */
    public static final String IPV4_C_PRIVATE_STR_MIN = "192.168.0.0";
    /**
     * The minimum numeric value of Class C private network range (192.168.0.0).
     */
    public static final long IPV4_C_PRIVATE_NUM_MIN = IPv4.ipv4ToLong(IPV4_C_PRIVATE_STR_MIN);
    /**
     * The maximum string representation of Class C private network range (192.168.255.255).
     */
    public static final String IPV4_C_PRIVATE_STR_MAX = "192.168.255.255";
    /**
     * The maximum numeric value of Class C private network range (192.168.255.255).
     */
    public static final long IPV4_C_PRIVATE_NUM_MAX = IPv4.ipv4ToLong(IPV4_C_PRIVATE_STR_MAX);
    /**
     * The minimum string representation of Class C public network range 2 (192.169.0.0).
     */
    public static final String IPV4_C_PUBLIC_2_STR_MIN = "192.169.0.0";
    /**
     * The minimum numeric value of Class C public network range 2 (192.169.0.0).
     */
    public static final long IPV4_C_PUBLIC_2_NUM_MIN = IPv4.ipv4ToLong(IPV4_C_PUBLIC_2_STR_MIN);
    /**
     * The maximum string representation of Class C public network range 2 (223.255.255.255).
     */
    public static final String IPV4_C_PUBLIC_2_STR_MAX = "223.255.255.255";
    /**
     * The maximum numeric value of Class C public network range 2 (223.255.255.255).
     */
    public static final long IPV4_C_PUBLIC_2_NUM_MAX = IPv4.ipv4ToLong(IPV4_C_PUBLIC_2_STR_MAX);
    /**
     * The minimum string representation of Class D IP address for multicast (224.0.0.0).
     */
    public static final String IPV4_D_STR_MIN = "224.0.0.0";
    /**
     * The minimum numeric value of Class D IP address for multicast (224.0.0.0).
     */
    public static final long IPV4_D_NUM_MIN = IPv4.ipv4ToLong(IPV4_D_STR_MIN);
    /**
     * The maximum string representation of Class D IP address for multicast (239.255.255.255).
     */
    public static final String IPV4_D_STR_MAX = "239.255.255.255";
    /**
     * The maximum numeric value of Class D IP address for multicast (239.255.255.255).
     */
    public static final long IPV4_D_NUM_MAX = IPv4.ipv4ToLong(IPV4_D_STR_MAX);
    /**
     * The minimum string representation of Class D dedicated multicast range (224.0.0.0).
     */
    public static final String IPV4_D_DEDICATED_STR_MIN = "224.0.0.0";
    /**
     * The minimum numeric value of Class D dedicated multicast range (224.0.0.0).
     */
    public static final long IPV4_D_DEDICATED_NUM_MIN = IPv4.ipv4ToLong(IPV4_D_DEDICATED_STR_MIN);
    /**
     * The maximum string representation of Class D dedicated multicast range (224.0.0.255).
     */
    public static final String IPV4_D_DEDICATED_STR_MAX = "224.0.0.255";
    /**
     * The maximum numeric value of Class D dedicated multicast range (224.0.0.255).
     */
    public static final long IPV4_D_DEDICATED_NUM_MAX = IPv4.ipv4ToLong(IPV4_D_DEDICATED_STR_MAX);
    /**
     * The minimum string representation of Class D public multicast range (224.0.1.0).
     */
    public static final String IPV4_D_PUBLIC_STR_MIN = "224.0.1.0";
    /**
     * The minimum numeric value of Class D public multicast range (224.0.1.0). This range is used for public multicast
     * services and applications.
     */
    public static final long IPV4_D_PUBLIC_NUM_MIN = IPv4.ipv4ToLong(IPV4_D_PUBLIC_STR_MIN);
    /**
     * The maximum string representation of Class D public multicast range (238.255.255.255).
     */
    public static final String IPV4_D_PUBLIC_STR_MAX = "238.255.255.255";
    /**
     * The maximum numeric value of Class D public multicast range (238.255.255.255).
     */
    public static final long IPV4_D_PUBLIC_NUM_MAX = IPv4.ipv4ToLong(IPV4_D_PUBLIC_STR_MAX);
    /**
     * The minimum string representation of Class D private multicast range (239.0.0.0).
     */
    public static final String IPV4_D_PRIVATE_STR_MIN = "239.0.0.0";
    /**
     * The minimum numeric value of Class D private multicast range (239.0.0.0). This range is reserved for private
     * network multicast communication.
     */
    public static final long IPV4_D_PRIVATE_NUM_MIN = IPv4.ipv4ToLong(IPV4_D_PRIVATE_STR_MIN);
    /**
     * The maximum string representation of Class D private multicast range (239.255.255.255).
     */
    public static final String IPV4_D_PRIVATE_STR_MAX = "239.255.255.255";
    /**
     * The maximum numeric value of Class D private multicast range (239.255.255.255). This range is reserved for
     * private network multicast communication.
     */
    public static final long IPV4_D_PRIVATE_NUM_MAX = IPv4.ipv4ToLong(IPV4_D_PRIVATE_STR_MAX);
    /**
     * The minimum string representation of Class E IP address (240.0.0.0). This range is reserved for experimental
     * purposes and future use.
     */
    public static final String IPV4_E_STR_MIN = "240.0.0.0";
    /**
     * The minimum numeric value of Class E IP address (240.0.0.0). This range is reserved for experimental purposes and
     * future use.
     */
    public static final long IPV4_E_NUM_MIN = IPv4.ipv4ToLong(IPV4_E_STR_MIN);
    /**
     * The maximum string representation of Class E IP address (255.255.255.255). This range is reserved for
     * experimental purposes and future use.
     */
    public static final String IPV4_E_STR_MAX = "255.255.255.255";
    /**
     * The maximum numeric value of Class E IP address (255.255.255.255). This range is reserved for experimental
     * purposes and future use.
     */
    public static final long IPV4_E_NUM_MAX = IPv4.ipv4ToLong(IPV4_E_STR_MAX);

    /**
     * Cached local hostname.
     */
    private static volatile String localhostName;

    /**
     * Gets the cached hostname of the local machine. Note: This method can trigger a reverse DNS lookup, which may
     * cause a delay depending on network conditions.
     *
     * @return The hostname.
     */
    public static String getLocalHostName() {
        if (null == localhostName) {
            synchronized (IPv4.class) {
                if (null == localhostName) {
                    localhostName = NetKit.getAddressName(getLocalhostDirectly());
                }
            }
        }
        return localhostName;
    }

    /**
     * Gets the MAC address of the network interface associated with the default local IPv4 address.
     *
     * @return The MAC address string.
     */
    public static String getLocalMacAddress() {
        return NetKit.getMacAddress(getLocalhost());
    }

    /**
     * Gets the hardware address (MAC address) of the local machine.
     *
     * @return The hardware address as a byte array.
     */
    public static byte[] getLocalHardwareAddress() {
        return NetKit.getHardwareAddress(getLocalhost());
    }

    /**
     * Gets the preferred non-loopback, non-site-local IPv4 address for the local machine. The result is cached for
     * subsequent calls.
     *
     * @return The local {@link InetAddress}, or {@code null} if not found.
     */
    public static InetAddress getLocalhost() {
        return Instances.get(IPv4.class.getName(), IPv4::getLocalhostDirectly);
    }

    /**
     * Gets the preferred non-loopback, non-site-local IPv4 address for the local machine without using a cache.
     *
     * @return The local {@link InetAddress}, or {@code null} if not found.
     */
    public static InetAddress getLocalhostDirectly() {
        return getLocalhostDirectly(false);
    }

    /**
     * Gets the preferred IPv4 address for the local machine, with an option to include site-local addresses.
     *
     * @param includeSiteLocal If {@code true}, site-local (private) addresses will be considered.
     * @return The local {@link InetAddress}, or {@code null} if not found.
     */
    public static InetAddress getLocalhostDirectly(final boolean includeSiteLocal) {
        final LinkedHashSet<InetAddress> localAddressList = NetKit.localAddressList(
                address -> address instanceof Inet4Address && !address.isLoopbackAddress()
                        && (includeSiteLocal || !address.isSiteLocalAddress()) && !address.isLinkLocalAddress());

        if (CollKit.isNotEmpty(localAddressList)) {
            return CollKit.getFirst(localAddressList);
        }

        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            if (localHost instanceof Inet4Address) {
                return localHost;
            }
        } catch (final UnknownHostException e) {
            // ignore
        }

        return null;
    }

    /**
     * Builds an {@link InetSocketAddress} from a host string and a default port. If the host string contains a port, it
     * will be used; otherwise, the default port is used.
     *
     * @param host        The host string (e.g., "127.0.0.1" or "localhost:8080").
     * @param defaultPort The default port to use if none is specified in the host string.
     * @return A new {@link InetSocketAddress}.
     */
    public static InetSocketAddress buildInetSocketAddress(String host, final int defaultPort) {
        if (StringKit.isBlank(host)) {
            host = Protocol.HOST_IPV4;
        }

        final String targetHost;
        final int port;
        final int index = host.indexOf(Symbol.COLON);
        if (index != -1) {
            targetHost = host.substring(0, index);
            port = Integer.parseInt(host.substring(index + 1));
        } else {
            targetHost = host;
            port = defaultPort;
        }

        return new InetSocketAddress(targetHost, port);
    }

    /**
     * Formats an IP address and a netmask into CIDR notation.
     *
     * @param ip   The IP address in dot-decimal notation.
     * @param mask The subnet mask in dot-decimal notation.
     * @return The CIDR string (e.g., "192.168.1.101/24").
     */
    public static String formatIpBlock(final String ip, final String mask) {
        return ip + Symbol.SLASH + getMaskBitByMask(mask);
    }

    /**
     * Lists all IP addresses within a given range. The range can be specified in CIDR format (e.g., "192.168.1.0/24")
     * or as a hyphen-separated range (e.g., "192.168.1.1-192.168.1.10").
     *
     * @param ipRange The IP range string.
     * @param isAll   If {@code true}, includes network and broadcast addresses for CIDR ranges.
     * @return A list of IP address strings.
     */
    public static List<String> list(final String ipRange, final boolean isAll) {
        if (ipRange.contains(Symbol.MINUS)) {
            final String[] range = CharsBacker.splitToArray(ipRange, Symbol.MINUS);
            return list(range[0], range[1]);
        } else if (ipRange.contains(Symbol.SLASH)) {
            final String[] param = CharsBacker.splitToArray(ipRange, Symbol.SLASH);
            return list(param[0], Integer.parseInt(param[1]), isAll);
        } else {
            return ListKit.of(ipRange);
        }
    }

    /**
     * Lists all IP addresses within a subnet defined by an IP and a mask bit length.
     *
     * @param ip      An IP address within the subnet.
     * @param maskBit The mask bit length (e.g., 24).
     * @param isAll   If {@code true}, includes the network and broadcast addresses. If {@code false}, returns only the
     *                usable host addresses.
     * @return A list of IP address strings.
     */
    public static List<String> list(final String ip, final int maskBit, final boolean isAll) {
        assertMaskBitValid(maskBit);
        if (countByMaskBit(maskBit, isAll) == 0) {
            return ListKit.zero();
        }

        final long startIp = getBeginIpLong(ip, maskBit);
        final long endIp = getEndIpLong(ip, maskBit);
        if (isAll) {
            return list(startIp, endIp);
        }

        if (startIp + 1 > endIp - 1) {
            return ListKit.zero();
        }
        return list(startIp + 1, endIp - 1);
    }

    /**
     * Lists all IP addresses within a given start-end range (inclusive).
     *
     * @param ipFrom The starting IP address in dot-decimal notation.
     * @param ipTo   The ending IP address in dot-decimal notation.
     * @return A list of all IP addresses in the range.
     */
    public static List<String> list(final String ipFrom, final String ipTo) {
        return list(ipv4ToLong(ipFrom), ipv4ToLong(ipTo));
    }

    /**
     * Lists all IP addresses within a given start-end range (inclusive), using long representations.
     *
     * @param ipFrom The starting IP address as a long.
     * @param ipTo   The ending IP address as a long.
     * @return A list of all IP addresses in the range.
     */
    public static List<String> list(final long ipFrom, final long ipTo) {
        final int count = countByIpRange(ipFrom, ipTo);
        final List<String> ips = new ArrayList<>(count);
        final StringBuilder sb = StringKit.builder(15);
        for (long ip = ipFrom, end = ipTo + 1; ip < end; ip++) {
            sb.setLength(0);
            ips.add(
                    sb.append((int) (ip >> 24) & 0xFF).append(Symbol.C_DOT).append((int) (ip >> 16) & 0xFF)
                            .append(Symbol.C_DOT).append((int) (ip >> 8) & 0xFF).append(Symbol.C_DOT)
                            .append((int) ip & 0xFF).toString());
        }
        return ips;
    }

    /**
     * Converts a long representation of an IPv4 address to its dot-decimal string format.
     *
     * @param ip The long representation of the IP address.
     * @return The dot-decimal IP address string.
     */
    public static String longToIpv4(final long ip) {
        return StringKit.builder(15).append((int) (ip >> 24) & 0xFF).append(Symbol.C_DOT)
                .append((int) (ip >> 16) & 0xFF).append(Symbol.C_DOT).append((int) (ip >> 8) & 0xFF)
                .append(Symbol.C_DOT).append((int) ip & 0xFF).toString();
    }

    /**
     * Converts a dot-decimal IPv4 address string to its long representation.
     *
     * @param strIp The IP address in dot-decimal notation.
     * @return The long representation of the IP address.
     */
    public static long ipv4ToLong(final String strIp) {
        final Matcher matcher = Pattern.IPV4_PATTERN.matcher(strIp);
        Assert.isTrue(matcher.matches(), "Invalid IPv4 address: {}", strIp);
        return matchAddress(matcher);
    }

    /**
     * Gets the starting IP address of a subnet.
     *
     * @param ip      An IP address within the subnet.
     * @param maskBit The mask bit length.
     * @return The starting IP address as a string.
     */
    public static String getBeginIpString(final String ip, final int maskBit) {
        return longToIpv4(getBeginIpLong(ip, maskBit));
    }

    /**
     * Gets the starting IP address of a subnet.
     *
     * @param ip      An IP address within the subnet.
     * @param maskBit The mask bit length.
     * @return The starting IP address as a long.
     */
    public static long getBeginIpLong(final String ip, final int maskBit) {
        assertMaskBitValid(maskBit);
        return ipv4ToLong(ip) & MaskBit.getMaskIpLong(maskBit);
    }

    /**
     * Gets the ending (broadcast) IP address of a subnet.
     *
     * @param ip      An IP address within the subnet.
     * @param maskBit The mask bit length.
     * @return The ending IP address as a string.
     */
    public static String getEndIpString(final String ip, final int maskBit) {
        return longToIpv4(getEndIpLong(ip, maskBit));
    }

    /**
     * Gets the ending (broadcast) IP address of a subnet.
     *
     * @param ip      An IP address within the subnet.
     * @param maskBit The mask bit length.
     * @return The ending IP address as a long.
     */
    public static long getEndIpLong(final String ip, final int maskBit) {
        return getBeginIpLong(ip, maskBit) + (Protocol.IPV4_NUM_MAX & ~MaskBit.getMaskIpLong(maskBit));
    }

    /**
     * Converts a dot-decimal subnet mask to its bit length (e.g., "255.255.255.0" -> 24).
     *
     * @param mask The subnet mask in dot-decimal notation.
     * @return The mask bit length.
     * @throws IllegalArgumentException if the mask is invalid.
     */
    public static int getMaskBitByMask(final String mask) {
        final Integer maskBit = MaskBit.getMaskBit(mask);
        Assert.notNull(maskBit, "Invalid netmask: {}", mask);
        return maskBit;
    }

    /**
     * Calculates the total number of addresses in a subnet.
     *
     * @param maskBit The mask bit length (1-32).
     * @param isAll   If {@code true}, returns the total number of addresses. If {@code false}, returns the number of
     *                usable host addresses (total - 2).
     * @return The number of addresses.
     */
    public static int countByMaskBit(final int maskBit, final boolean isAll) {
        Assert.isTrue(
                maskBit >= IPV4_MASK_BIT_VALID_MIN && maskBit <= IPV4_MASK_BIT_MAX,
                "Unsupported mask bit: {}",
                maskBit);
        if (maskBit == IPV4_MASK_BIT_MAX && !isAll) {
            return 0;
        }
        final int count = 1 << (IPV4_MASK_BIT_MAX - maskBit);
        return isAll ? count : count - 2;
    }

    /**
     * Converts a mask bit length to its dot-decimal subnet mask string (e.g., 24 -> "255.255.255.0").
     *
     * @param maskBit The mask bit length (1-32).
     * @return The subnet mask string.
     */
    public static String getMaskByMaskBit(final int maskBit) {
        assertMaskBitValid(maskBit);
        return MaskBit.get(maskBit);
    }

    /**
     * Calculates the subnet mask for a given IP range.
     *
     * @param fromIp The starting IP address (inclusive).
     * @param toIp   The ending IP address (inclusive).
     * @return The subnet mask in dot-decimal notation.
     */
    public static String getMaskByIpRange(final String fromIp, final String toIp) {
        final long toIpLong = ipv4ToLong(toIp);
        final long fromIpLong = ipv4ToLong(fromIp);
        Assert.isTrue(fromIpLong <= toIpLong, "Start IP must be less than or equal to end IP!");

        return StringKit.builder(15).append(255 - getPartOfIp(toIpLong, 1) + getPartOfIp(fromIpLong, 1))
                .append(Symbol.C_DOT).append(255 - getPartOfIp(toIpLong, 2) + getPartOfIp(fromIpLong, 2))
                .append(Symbol.C_DOT).append(255 - getPartOfIp(toIpLong, 3) + getPartOfIp(fromIpLong, 3))
                .append(Symbol.C_DOT).append(255 - getPartOfIp(toIpLong, 4) + getPartOfIp(fromIpLong, 4)).toString();
    }

    /**
     * Calculates the number of IP addresses in a given range.
     *
     * @param fromIp The starting IP address (inclusive).
     * @param toIp   The ending IP address (inclusive).
     * @return The number of IPs in the range.
     */
    public static int countByIpRange(final String fromIp, final String toIp) {
        return countByIpRange(ipv4ToLong(fromIp), ipv4ToLong(toIp));
    }

    /**
     * Calculates the number of IP addresses in a given range.
     *
     * @param fromIp The starting IP address as a long.
     * @param toIp   The ending IP address as a long.
     * @return The number of IPs in the range.
     */
    public static int countByIpRange(final long fromIp, final long toIp) {
        Assert.isTrue(fromIp <= toIp, "Start IP must be less than or equal to end IP!");
        return (int) (toIp - fromIp + 1);
    }

    /**
     * Checks if a given string is a valid subnet mask.
     *
     * @param mask The mask string in dot-decimal notation.
     * @return {@code true} if the mask is valid.
     */
    public static boolean isMaskValid(final String mask) {
        return MaskBit.getMaskBit(mask) != null;
    }

    /**
     * Checks if a given mask bit length is valid for IPv4.
     *
     * @param maskBit The mask bit length (0-32).
     * @return {@code true} if the mask bit is valid.
     */
    public static boolean isMaskBitValid(final int maskBit) {
        return maskBit >= IPV4_MASK_BIT_MIN && maskBit <= IPV4_MASK_BIT_MAX;
    }

    /**
     * Checks if the given IPv4 address is a private (internal) network address.
     *
     * @param ipAddress The IP address in dot-decimal notation.
     * @return {@code true} if it is an internal IP.
     */
    public static boolean isInnerIP(final String ipAddress) {
        return isInnerIP(ipv4ToLong(ipAddress));
    }

    /**
     * Checks if the given IPv4 address is a private (internal) network address.
     *
     * @param ipNum The IP address as a long.
     * @return {@code true} if it is an internal IP.
     */
    public static boolean isInnerIP(final long ipNum) {
        return isBetween(ipNum, IPV4_A_PRIVATE_NUM_MIN, IPV4_A_PRIVATE_NUM_MAX)
                || isBetween(ipNum, IPV4_B_PRIVATE_NUM_MIN, IPV4_B_PRIVATE_NUM_MAX)
                || isBetween(ipNum, IPV4_C_PRIVATE_NUM_MIN, IPV4_C_PRIVATE_NUM_MAX)
                || isBetween(ipNum, IPV4_LOOPBACK_NUM_MIN, IPV4_LOOPBACK_NUM_MAX);
    }

    /**
     * Checks if the given IPv4 address is a public network address.
     *
     * @param ipAddress The IP address in dot-decimal notation.
     * @return {@code true} if it is a public IP.
     */
    public static boolean isPublicIP(final String ipAddress) {
        return isPublicIP(ipv4ToLong(ipAddress));
    }

    /**
     * Checks if the given IPv4 address is a public network address.
     *
     * @param ipNum The IP address as a long.
     * @return {@code true} if it is a public IP.
     */
    public static boolean isPublicIP(final long ipNum) {
        return isBetween(ipNum, IPV4_A_PUBLIC_1_NUM_MIN, IPV4_A_PUBLIC_1_NUM_MAX)
                || isBetween(ipNum, IPV4_A_PUBLIC_2_NUM_MIN, IPV4_A_PUBLIC_2_NUM_MAX)
                || isBetween(ipNum, IPV4_B_PUBLIC_1_NUM_MIN, IPV4_B_PUBLIC_1_NUM_MAX)
                || isBetween(ipNum, IPV4_B_PUBLIC_2_NUM_MIN, IPV4_B_PUBLIC_2_NUM_MAX)
                || isBetween(ipNum, IPV4_C_PUBLIC_1_NUM_MIN, IPV4_C_PUBLIC_1_NUM_MAX)
                || isBetween(ipNum, IPV4_C_PUBLIC_2_NUM_MIN, IPV4_C_PUBLIC_2_NUM_MAX);
    }

    /**
     * Gets a specific octet from the long representation of an IP address.
     *
     * @param ip       The IP address as a long.
     * @param position The position of the octet (1-4).
     * @return The decimal value of the octet.
     */
    public static int getPartOfIp(final long ip, final int position) {
        return switch (position) {
            case 1 -> (int) (ip >> 24) & 0xFF;
            case 2 -> (int) (ip >> 16) & 0xFF;
            case 3 -> (int) (ip >> 8) & 0xFF;
            case 4 -> (int) ip & 0xFF;
            default -> throw new IllegalArgumentException("Illegal position of ip Long: " + position);
        };
    }

    /**
     * Checks if an IP address matches a wildcard pattern (e.g., '192.168.1.*').
     *
     * @param wildcard  The wildcard pattern.
     * @param ipAddress The IP address to check.
     * @return {@code true} if the IP address matches the pattern.
     */
    public static boolean matches(final String wildcard, final String ipAddress) {
        if (!PatternKit.isMatch(Pattern.IPV4_PATTERN, ipAddress)) {
            return false;
        }

        final String[] wildcardSegments = CharsBacker.splitToArray(wildcard, Symbol.DOT);
        final String[] ipSegments = CharsBacker.splitToArray(ipAddress, Symbol.DOT);

        if (wildcardSegments.length != ipSegments.length) {
            return false;
        }

        for (int i = 0; i < wildcardSegments.length; i++) {
            if (!Symbol.STAR.equals(wildcardSegments[i]) && !wildcardSegments[i].equals(ipSegments[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Converts a regex {@link Matcher} group for an IPv4 address to a long.
     *
     * @param matcher The matcher that has successfully matched an IPv4 pattern.
     * @return The long representation of the IP.
     */
    private static long matchAddress(final Matcher matcher) {
        long addr = 0;
        addr |= Long.parseLong(matcher.group(1)) << 24;
        addr |= Long.parseLong(matcher.group(2)) << 16;
        addr |= Long.parseLong(matcher.group(3)) << 8;
        addr |= Long.parseLong(matcher.group(4));
        return addr;
    }

    /**
     * Checks if a given IP is within a specified range (inclusive).
     *
     * @param userIp The IP to check.
     * @param begin  The start of the range.
     * @param end    The end of the range.
     * @return {@code true} if the IP is within the range.
     */
    private static boolean isBetween(final long userIp, final long begin, final long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    /**
     * Asserts that a mask bit length is valid for IPv4.
     *
     * @param maskBit The mask bit length.
     */
    private static void assertMaskBitValid(final int maskBit) {
        Assert.isTrue(isMaskBitValid(maskBit), "Invalid maskBit: {}", maskBit);
    }

}
