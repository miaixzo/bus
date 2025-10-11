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
package org.miaixz.bus.core.xyz;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Predicate;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.NonAuthenticator;
import org.miaixz.bus.core.net.ip.IPv4;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * Network related utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NetKit {

    /**
     * Converts a long value to an IPv4 address string.
     *
     * @param longIP The long representation of the IP address.
     * @return The IPv4 address string.
     * @see IPv4#longToIpv4(long)
     */
    public static String longToIpv4(final long longIP) {
        return IPv4.longToIpv4(longIP);
    }

    /**
     * Converts an IPv4 address string to its long representation.
     *
     * @param strIP The IPv4 address string.
     * @return The long representation of the IP address.
     * @see IPv4#ipv4ToLong(String)
     */
    public static long ipv4ToLong(final String strIP) {
        return IPv4.ipv4ToLong(strIP);
    }

    /**
     * Checks if a local port is usable (not occupied).
     *
     * @param port The port to check.
     * @return {@code true} if the port is usable, {@code false} otherwise.
     */
    public static boolean isUsableLocalPort(final int port) {
        if (!isValidPort(port)) {
            // The given IP is not within the specified port range.
            return false;
        }

        // Some ports bound to non-127.0.0.1 cannot be detected.
        try (final ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
        } catch (final IOException ignored) {
            return false;
        }

        try (final DatagramSocket ds = new DatagramSocket(port)) {
            ds.setReuseAddress(true);
        } catch (final IOException ignored) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a port number is valid. This method does not check if the port is occupied.
     *
     * @param port The port number.
     * @return {@code true} if the port is valid (0-65535), {@code false} otherwise.
     */
    public static boolean isValidPort(final int port) {
        // Valid ports are 0 to 65535
        return port >= 0 && port <= Normal._65535;
    }

    /**
     * Finds an available local port in the range 1024 to 65535. This method checks a random port within the given range
     * up to (65535 - 1024) times.
     *
     * @return An available port.
     * @throws InternalException If no available port is found within the range after multiple attempts.
     */
    public static int getUsableLocalPort() {
        return getUsableLocalPort(Normal._1024);
    }

    /**
     * Finds an available local port within the specified range, up to a maximum of 65535. This method checks a random
     * port within the given range up to (65535 - minPort) times.
     *
     * @param minPort The minimum port number (inclusive).
     * @return An available port.
     * @throws InternalException If no available port is found within the range after multiple attempts.
     */
    public static int getUsableLocalPort(final int minPort) {
        return getUsableLocalPort(minPort, Normal._65535);
    }

    /**
     * Finds an available local port within the specified range. This method checks a random port within the given range
     * up to (maxPort - minPort) times.
     *
     * @param minPort The minimum port number (inclusive).
     * @param maxPort The maximum port number (inclusive).
     * @return An available port.
     * @throws InternalException If no available port is found within the range after multiple attempts.
     */
    public static int getUsableLocalPort(final int minPort, final int maxPort) {
        final int maxPortExclude = maxPort + 1;
        int randomPort;
        for (int i = minPort; i < maxPortExclude; i++) {
            randomPort = RandomKit.randomInt(minPort, maxPortExclude);
            if (isUsableLocalPort(randomPort)) {
                return randomPort;
            }
        }

        throw new InternalException("Could not find an available port in the range [{}, {}] after {} attempts", minPort,
                maxPort, maxPort - minPort);
    }

    /**
     * Retrieves multiple usable local ports within a specified range.
     *
     * @param numRequested The number of usable ports to find.
     * @param minPort      The minimum port number (inclusive).
     * @param maxPort      The maximum port number (inclusive).
     * @return A {@link TreeSet} containing the available ports.
     * @throws InternalException If the requested number of ports cannot be found within the range after multiple
     *                           attempts.
     */
    public static TreeSet<Integer> getUsableLocalPorts(final int numRequested, final int minPort, final int maxPort) {
        final TreeSet<Integer> availablePorts = new TreeSet<>();
        int attemptCount = 0;
        while ((++attemptCount <= numRequested + 100) && availablePorts.size() < numRequested) {
            availablePorts.add(getUsableLocalPort(minPort, maxPort));
        }

        if (availablePorts.size() != numRequested) {
            throw new InternalException("Could not find {} available  ports in the range [{}, {}]", numRequested,
                    minPort, maxPort);
        }

        return availablePorts;
    }

    /**
     * Determines if an IPv4 address is a private IP address. Private IP ranges:
     * 
     * <pre>
     * A class: 10.0.0.0 - 10.255.255.255
     * B class: 172.16.0.0 - 172.31.255.255
     * C class: 192.168.0.0 - 192.168.255.255
     * </pre>
     * 
     * Also, the 127.x.x.x range is a loopback address.
     *
     * @param ipAddress The IP address to check.
     * @return {@code true} if the IP address is a private IP, {@code false} otherwise.
     * @see IPv4#isInnerIP(String)
     */
    public static boolean isInnerIP(final String ipAddress) {
        return IPv4.isInnerIP(ipAddress);
    }

    /**
     * Converts a relative URL to an absolute URL.
     *
     * @param absoluteBasePath The base path, which must be absolute.
     * @param relativePath     The relative path.
     * @return The absolute URL string.
     * @throws InternalException If an error occurs during URL conversion.
     */
    public static String toAbsoluteUrl(final String absoluteBasePath, final String relativePath) {
        try {
            final URL absoluteUrl = new URL(absoluteBasePath);
            return new URL(absoluteUrl, relativePath).toString();
        } catch (final Exception e) {
            throw new InternalException(e, "To absolute url [{}] base [{}] error!", relativePath, absoluteBasePath);
        }
    }

    /**
     * Hides the last part of an IP address with an asterisk (*).
     *
     * @param ip The IP address string.
     * @return The IP address with the last part hidden.
     */
    public static String hideIpPart(final String ip) {
        return StringKit.builder(ip.length()).append(ip, 0, ip.lastIndexOf(".") + 1).append(Symbol.STAR).toString();
    }

    /**
     * Hides the last part of an IP address (represented as a long) with an asterisk (*).
     *
     * @param ip The IP address as a long value.
     * @return The IP address with the last part hidden.
     */
    public static String hideIpPart(final long ip) {
        return hideIpPart(longToIpv4(ip));
    }

    /**
     * Resolves a hostname to its IP address.
     *
     * @param hostName The hostname.
     * @return The IP address string, or the hostname itself if {@link UnknownHostException} occurs.
     */
    public static String getIpByHost(final String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (final UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * Retrieves a network interface by its name.
     *
     * @param name The name of the network interface (e.g., "eth0" on Linux).
     * @return The {@link NetworkInterface} object, or {@code null} if not found.
     */
    public static NetworkInterface getNetworkInterface(final String name) {
        final Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            return null;
        }

        NetworkInterface netInterface;
        while (networkInterfaces.hasMoreElements()) {
            netInterface = networkInterfaces.nextElement();
            if (null != netInterface && name.equals(netInterface.getName())) {
                return netInterface;
            }
        }

        return null;
    }

    /**
     * Retrieves all network interfaces on the local machine.
     *
     * @return A {@link Collection} of {@link NetworkInterface} objects, or {@code null} if a {@link SocketException}
     *         occurs.
     */
    public static Collection<NetworkInterface> getNetworkInterfaces() {
        final Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            return null;
        }

        return CollKit.addAll(new ArrayList<>(), networkInterfaces);
    }

    /**
     * Retrieves a sorted list of local IPv4 addresses. The IP list is ordered according to the system device order.
     *
     * @return A {@link LinkedHashSet} of IPv4 address strings.
     */
    public static LinkedHashSet<String> localIpv4s() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(t -> t instanceof Inet4Address);

        return toIpList(localAddressList);
    }

    /**
     * Converts a set of {@link InetAddress} objects to a {@link LinkedHashSet} of IP address strings.
     *
     * @param addressList A set of {@link InetAddress} objects.
     * @return A {@link LinkedHashSet} of IP address strings.
     */
    public static LinkedHashSet<String> toIpList(final Set<InetAddress> addressList) {
        final LinkedHashSet<String> ipSet = new LinkedHashSet<>();
        for (final InetAddress address : addressList) {
            ipSet.add(address.getHostAddress());
        }

        return ipSet;
    }

    /**
     * Retrieves a sorted list of local IP addresses (including IPv4 and IPv6). The IP list is ordered according to the
     * system device order.
     *
     * @return A {@link LinkedHashSet} of IP address strings.
     */
    public static LinkedHashSet<String> localIps() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(null);
        return toIpList(localAddressList);
    }

    /**
     * Retrieves all local IP address objects that satisfy the given filter condition.
     *
     * @param addressPredicate The predicate to filter {@link InetAddress} objects. If {@code null}, no filtering is
     *                         applied.
     * @return A {@link LinkedHashSet} of filtered {@link InetAddress} objects.
     * @throws InternalException If an error occurs while getting network interfaces.
     */
    public static LinkedHashSet<InetAddress> localAddressList(final Predicate<InetAddress> addressPredicate) {
        return localAddressList(null, addressPredicate);
    }

    /**
     * Retrieves all local IP address objects that satisfy the given network interface and address filter conditions.
     *
     * @param networkInterfaceFilter The predicate to filter {@link NetworkInterface} objects. If {@code null}, no
     *                               filtering is applied.
     * @param addressPredicate       The predicate to filter {@link InetAddress} objects. If {@code null}, no filtering
     *                               is applied.
     * @return A {@link LinkedHashSet} of filtered {@link InetAddress} objects.
     * @throws InternalException If an error occurs while getting network interfaces.
     */
    public static LinkedHashSet<InetAddress> localAddressList(
            final Predicate<NetworkInterface> networkInterfaceFilter,
            final Predicate<InetAddress> addressPredicate) {
        final Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            throw new InternalException(e);
        }

        Assert.notNull(networkInterfaces, () -> new InternalException("Get network interface error!"));

        final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterfaceFilter != null && !networkInterfaceFilter.test(networkInterface)) {
                continue;
            }
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress != null && (null == addressPredicate || addressPredicate.test(inetAddress))) {
                    ipSet.add(inetAddress);
                }
            }
        }

        return ipSet;
    }

    /**
     * Retrieves the local machine's IPv4 address. This method returns the first non-loopback address found across all
     * network interfaces. If retrieval fails, it falls back to {@link InetAddress#getLocalHost()}. This method does not
     * throw exceptions and returns {@code null} on failure.
     *
     * @return The local machine's IPv4 address string, or {@code null} if retrieval fails.
     * @see <a href=
     *      "http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java">Getting
     *      the IP address of the current machine using Java</a>
     */
    public static String getLocalhostStringV4() {
        final InetAddress localhost = IPv4.getLocalhost();
        if (null != localhost) {
            return localhost.getHostAddress();
        }
        return null;
    }

    /**
     * Retrieves the local machine's IPv4 address based on the following rules:
     * <ul>
     * <li>Must be a non-loopback, non-site-local, IPv4 address.</li>
     * <li>If multiple network cards exist, the first address that meets the conditions is returned.</li>
     * <li>If no address meets the requirements, {@link InetAddress#getLocalHost()} is used to get the address.</li>
     * </ul>
     * <p>
     * This method does not throw exceptions and returns {@code null} on failure.
     *
     * @return The local machine's IPv4 address, or {@code null} if retrieval fails.
     */
    public static InetAddress getLocalhostV4() {
        return IPv4.getLocalhost();
    }

    /**
     * Retrieves the local machine's MAC address, using the network interface corresponding to the obtained IPv4 local
     * address by default.
     *
     * @return The local machine's MAC address string.
     */
    public static String getLocalMacAddressV4() {
        return IPv4.getLocalMacAddress();
    }

    /**
     * Creates an {@link InetSocketAddress}.
     *
     * @param host The hostname or IP address. An empty string indicates any local address.
     * @param port The port number. 0 indicates that the system should assign a temporary port.
     * @return A new {@link InetSocketAddress}.
     */
    public static InetSocketAddress createAddress(final String host, final int port) {
        if (StringKit.isBlank(host)) {
            return new InetSocketAddress(port);
        }
        return new InetSocketAddress(host, port);
    }

    /**
     * Sends data using a simple SocketChannel.
     *
     * @param host    The server host.
     * @param port    The server port.
     * @param isBlock Whether to use blocking mode.
     * @param data    The data to send as a {@link ByteBuffer}.
     * @throws InternalException If an I/O error occurs.
     */
    public static void netCat(final String host, final int port, final boolean isBlock, final ByteBuffer data)
            throws InternalException {
        try (final SocketChannel channel = SocketChannel.open(createAddress(host, port))) {
            channel.configureBlocking(isBlock);
            channel.write(data);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Sends data using a regular {@link Socket}.
     *
     * @param host The server host.
     * @param port The server port.
     * @param data The data to send as a byte array.
     * @throws InternalException If an I/O error occurs.
     */
    public static void netCat(final String host, final int port, final byte[] data) throws InternalException {
        OutputStream out = null;
        try (final Socket socket = new Socket(host, port)) {
            out = socket.getOutputStream();
            out.write(data);
            out.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(out);
        }
    }

    /**
     * Checks if an IP address is within a given CIDR range. Method source: [Chengdu] Xiaodeng
     *
     * @param ip   The IP address to validate.
     * @param cidr The CIDR rule (e.g., "192.168.1.0/24").
     * @return {@code true} if the IP is within the range, {@code false} otherwise.
     * @throws IllegalArgumentException If the CIDR string is invalid.
     */
    public static boolean isInRange(final String ip, final String cidr) {
        final int maskSplitMarkIndex = cidr.lastIndexOf(Symbol.SLASH);
        if (maskSplitMarkIndex < 0) {
            throw new IllegalArgumentException("Invalid cidr: " + cidr);
        }

        final long mask = (-1L << 32 - Integer.parseInt(cidr.substring(maskSplitMarkIndex + 1)));
        final long cidrIpAddr = ipv4ToLong(cidr.substring(0, maskSplitMarkIndex));

        return (ipv4ToLong(ip) & mask) == (cidrIpAddr & mask);
    }

    /**
     * Converts a Unicode domain name to Punycode.
     *
     * @param unicode The Unicode domain name.
     * @return The Punycode representation of the domain name.
     */
    public static String idnToASCII(final String unicode) {
        return IDN.toASCII(unicode);
    }

    /**
     * Retrieves the first non-"unknown" IP address from a multi-stage reverse proxy string.
     *
     * @param ip The IP address string obtained from a proxy header.
     * @return The first non-"unknown" IP address.
     */
    public static String getMultistageReverseProxyIp(String ip) {
        // Multi-stage reverse proxy detection
        if (ip != null && StringKit.indexOf(ip, Symbol.C_COMMA) > 0) {
            final List<String> ips = CharsBacker.splitTrim(ip, Symbol.COMMA);
            for (final String subIp : ips) {
                if (!isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * Checks if the given string is considered "unknown", often used for HTTP request headers.
     *
     * @param checkString The string to check.
     * @return {@code true} if the string is blank or equals "unknown" (case-insensitive), {@code false} otherwise.
     */
    public static boolean isUnknown(final String checkString) {
        return StringKit.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    /**
     * Checks if an IP address is reachable (pingable).
     *
     * @param ip The IP address.
     * @return {@code true} if the IP address is reachable, {@code false} otherwise.
     */
    public static boolean ping(final String ip) {
        return ping(ip, 200);
    }

    /**
     * Checks if an IP address is reachable (pingable) within a specified timeout.
     *
     * @param ip      The IP address.
     * @param timeout The timeout in milliseconds.
     * @return {@code true} if the IP address is reachable within the timeout, {@code false} otherwise.
     */
    public static boolean ping(final String ip, final int timeout) {
        try {
            return InetAddress.getByName(ip).isReachable(timeout); // If the return value is true, the host is
                                                                   // available; otherwise, it is not.
        } catch (final Exception ex) {
            return false;
        }
    }

    /**
     * Parses a Cookie string into a list of {@link HttpCookie} objects.
     *
     * @param cookieStr The Cookie string.
     * @return A {@link List} of {@link HttpCookie} objects, or an empty list if the input string is blank.
     */
    public static List<HttpCookie> parseCookies(final String cookieStr) {
        if (StringKit.isBlank(cookieStr)) {
            return Collections.emptyList();
        }
        return HttpCookie.parse(cookieStr);
    }

    /**
     * Checks if a remote port is open.
     *
     * @param address The remote address.
     * @param timeout The connection timeout in milliseconds.
     * @return {@code true} if the remote port is open, {@code false} otherwise.
     */
    public static boolean isOpen(final InetSocketAddress address, final int timeout) {
        try (final Socket sc = new Socket()) {
            sc.connect(address, timeout);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Sets a global authenticator for HTTP authentication.
     *
     * @param user The username.
     * @param pass The password as a character array (for security reasons, not a String).
     */
    public static void setGlobalAuthenticator(final String user, final char[] pass) {
        setGlobalAuthenticator(new NonAuthenticator(user, pass));
    }

    /**
     * Sets a global authenticator for HTTP authentication.
     *
     * @param authenticator The {@link Authenticator} to set as default.
     */
    public static void setGlobalAuthenticator(final Authenticator authenticator) {
        Authenticator.setDefault(authenticator);
    }

    /**
     * Retrieves DNS information, such as TXT records.
     * 
     * <pre class="code">
     * NetKit.getDnsInfo("example.com", "TXT")
     * </pre>
     *
     * @param hostName  The hostname or domain name.
     * @param attrNames The attribute names to retrieve (e.g., "TXT", "MX").
     * @return A {@link List} of DNS information strings.
     */
    public static List<String> getDnsInfo(final String hostName, final String... attrNames) {
        final String uri = StringKit.addPrefixIfNot(hostName, "dns:");
        final Attributes attributes = Keys.getAttributes(uri, attrNames);

        final List<String> infos = new ArrayList<>();
        for (final Attribute attribute : new EnumerationIterator<>(attributes.getAll())) {
            try {
                infos.add((String) attribute.get());
            } catch (final NamingException ignore) {
                // ignore
            }
        }
        return infos;
    }

    /**
     * Retrieves the host name of an {@link InetAddress}. If the host name is empty, the host address is returned. If
     * the provided address is {@code null}, {@code null} is returned.
     *
     * @param address The {@link InetAddress}. Returns {@code null} if the input is {@code null}.
     * @return The host name or host address, or {@code null} if the input address is {@code null}.
     */
    public static String getAddressName(final InetAddress address) {
        if (null == address) {
            return null;
        }
        String name = address.getHostName();
        if (StringKit.isEmpty(name)) {
            name = address.getHostAddress();
        }
        return name;
    }

    /**
     * Retrieves the MAC address from the given {@link InetAddress}, using a hyphen "-" as a separator.
     *
     * @param inetAddress The {@link InetAddress}.
     * @return The MAC address string, separated by hyphens.
     */
    public static String getMacAddress(final InetAddress inetAddress) {
        return getMacAddress(inetAddress, Symbol.MINUS);
    }

    /**
     * Retrieves the MAC address from the given {@link InetAddress} using a specified separator.
     *
     * @param inetAddress The {@link InetAddress}.
     * @param separator   The separator string (e.g., "-" or ":").
     * @return The MAC address string, separated by the specified separator.
     */
    public static String getMacAddress(final InetAddress inetAddress, final String separator) {
        if (null == inetAddress) {
            return null;
        }

        return toMacAddress(getHardwareAddress(inetAddress), separator);
    }

    /**
     * Retrieves the hardware address (MAC address) from the given {@link InetAddress}.
     *
     * @param inetAddress The {@link InetAddress}.
     * @return The hardware address as a byte array, or {@code null} if an error occurs or the address is invalid.
     * @throws InternalException If a {@link SocketException} occurs during retrieval.
     */
    public static byte[] getHardwareAddress(final InetAddress inetAddress) {
        if (null == inetAddress) {
            return null;
        }

        try {
            // Get the network interface corresponding to the address
            final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            if (null != networkInterface) {
                return networkInterface.getHardwareAddress();
            }
        } catch (final SocketException e) {
            throw new InternalException(e);
        }
        return null;
    }

    /**
     * Converts a byte array MAC address to a human-readable string, typically using hexadecimal representation for each
     * byte and separated by a specified delimiter.
     *
     * @param mac       The MAC address as a byte array.
     * @param separator The separator string.
     * @return The MAC address string.
     */
    public static String toMacAddress(final byte[] mac, final String separator) {
        if (null == mac) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(
                // String length = number of bytes * 2 (each byte becomes 2 hex digits) + total length of separators
                mac.length * 2 + (mac.length - 1) * separator.length());
        String s;
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append(separator);
            }
            // Convert byte to integer
            s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }
        return sb.toString();
    }

}
