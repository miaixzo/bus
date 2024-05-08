/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.collection.Iterator.EnumerationIterator;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.net.MaskBit;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;

/**
 * 网络相关工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NetKit {

    /**
     * 默认最小端口，1024
     */
    public static final int PORT_RANGE_MIN = Normal._1024;
    /**
     * 默认最大端口，65535
     */
    public static final int PORT_RANGE_MAX = 0xFFFF;
    /**
     * A类私有地址的最小值
     */
    public static final long A_INNER_IP_LONG_BEGIN = ipv4ToLong("10.0.0.0");

    /**
     * A类私有地址的最大值
     */
    public static final long A_INNER_IP_LONG_END = ipv4ToLong("10.255.255.255");

    /**
     * B类私有地址的最小值
     */
    public static final long B_INNER_IP_LONG_BEGIN = ipv4ToLong("172.16.0.0");

    /**
     * B类私有地址的最大值
     */
    public static final long B_INNER_IP_LONG_END = ipv4ToLong("172.31.255.255");

    /**
     * C类私有地址的最小值
     */
    public static final long C_INNER_IP_LONG_BEGIN = ipv4ToLong("192.168.0.0");

    /**
     * C类私有地址的最大值
     */
    public static final long C_INNER_IP_LONG_END = ipv4ToLong("192.168.255.255");

    /**
     * 将IPv6地址字符串转为大整数
     *
     * @param ipv6Str 字符串
     * @return 大整数, 如发生异常返回 null
     */
    public static BigInteger ipv6ToBigInteger(final String ipv6Str) {
        try {
            final InetAddress address = InetAddress.getByName(ipv6Str);
            if (address instanceof Inet6Address) {
                return new BigInteger(1, address.getAddress());
            }
        } catch (final UnknownHostException ignore) {
        }
        return null;
    }

    /**
     * 将大整数转换成ipv6字符串
     *
     * @param bigInteger 大整数
     * @return IPv6字符串, 如发生异常返回 null
     */
    public static String bigIntegerToIPv6(final BigInteger bigInteger) {
        try {
            return InetAddress.getByAddress(bigInteger.toByteArray()).toString().substring(1);
        } catch (final UnknownHostException ignore) {
            return null;
        }
    }

    /**
     * 检测本地端口可用性
     *
     * @param port 被检测的端口
     * @return 是否可用
     */
    public static boolean isUsableLocalPort(final int port) {
        if (false == isValidPort(port)) {
            // 给定的IP未在指定端口范围中
            return false;
        }

        // 某些绑定非127.0.0.1的端口无法被检测到
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
     * 是否为有效的端口
     * 此方法并不检查端口是否被占用
     *
     * @param port 端口号
     * @return 是否有效
     */
    public static boolean isValidPort(final int port) {
        // 有效端口是0～65535
        return port >= 0 && port <= PORT_RANGE_MAX;
    }

    /**
     * 查找1024~65535范围内的可用端口
     * 此方法只检测给定范围内的随机一个端口,检测65535-1024次
     *
     * @return 可用的端口
     */
    public static int getUsableLocalPort() {
        return getUsableLocalPort(PORT_RANGE_MIN);
    }

    /**
     * 查找指定范围内的可用端口,最大值为65535
     * 此方法只检测给定范围内的随机一个端口,检测65535-minPort次
     *
     * @param minPort 端口最小值（包含）
     * @return 可用的端口
     */
    public static int getUsableLocalPort(final int minPort) {
        return getUsableLocalPort(minPort, PORT_RANGE_MAX);
    }

    /**
     * 查找指定范围内的可用端口
     * 此方法只检测给定范围内的随机一个端口,检测maxPort-minPort次
     *
     * @param minPort 端口最小值（包含）
     * @param maxPort 端口最大值（包含）
     * @return 可用的端口
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

        throw new InternalException("Could not find an available port in the range [{}, {}] after {} attempts", minPort, maxPort, maxPort - minPort);
    }

    /**
     * 获取多个本地可用端口
     *
     * @param numRequested 尝试次数
     * @param minPort      端口最小值（包含）
     * @param maxPort      端口最大值（包含）
     * @return 可用的端口
     */
    public static TreeSet<Integer> getUsableLocalPorts(final int numRequested, final int minPort, final int maxPort) {
        final TreeSet<Integer> availablePorts = new TreeSet<>();
        int attemptCount = 0;
        while ((++attemptCount <= numRequested + 100) && availablePorts.size() < numRequested) {
            availablePorts.add(getUsableLocalPort(minPort, maxPort));
        }

        if (availablePorts.size() != numRequested) {
            throw new InternalException("Could not find {} available  ports in the range [{}, {}]", numRequested, minPort, maxPort);
        }
        return availablePorts;
    }

    /**
     * 相对URL转换为绝对URL
     *
     * @param absoluteBasePath 基准路径，绝对
     * @param relativePath     相对路径
     * @return 绝对URL
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
     * 隐藏掉IP地址的最后一部分为 * 代替
     *
     * @param ip IP地址
     * @return 隐藏部分后的IP
     */
    public static String hideIpPart(final String ip) {
        return StringKit.builder(ip.length()).append(ip, 0, ip.lastIndexOf(".") + 1).append("*").toString();
    }

    /**
     * 隐藏掉IP地址的最后一部分为 * 代替
     *
     * @param ip IP地址
     * @return 隐藏部分后的IP
     */
    public static String hideIpPart(final long ip) {
        return hideIpPart(longToIpv4(ip));
    }

    /**
     * 构建InetSocketAddress
     * 当host中包含端口时（用“：”隔开），使用host中的端口，否则使用默认端口
     * 给定host为空时使用本地host（127.0.0.1）
     *
     * @param host        Host
     * @param defaultPort 默认端口
     * @return InetSocketAddress
     */
    public static InetSocketAddress buildInetSocketAddress(String host, final int defaultPort) {
        if (StringKit.isBlank(host)) {
            host = Http.HOST_IPV4;
        }

        final String destHost;
        final int port;
        final int index = host.indexOf(Symbol.COLON);
        if (index != -1) {
            // host:port形式
            destHost = host.substring(0, index);
            port = Integer.parseInt(host.substring(index + 1));
        } else {
            destHost = host;
            port = defaultPort;
        }
        return new InetSocketAddress(destHost, port);
    }

    /**
     * 通过域名得到IP
     *
     * @param hostName HOST
     * @return ip address or hostName if UnknownHostException
     */
    public static String getIpByHost(final String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (final UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * 获取指定名称的网卡信息
     *
     * @param name 网络接口名，例如Linux下默认是eth0
     * @return 网卡，未找到返回{@code null}
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
     * 获取本机所有网卡
     *
     * @return 所有网卡，异常返回{@code null}
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
     * 获得本机的IPv4地址列表
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表 {@link LinkedHashSet}
     */
    public static LinkedHashSet<String> localIpv4s() {
        return toIpList(localAddressList(t -> t instanceof Inet4Address));
    }

    /**
     * 获得本机的IPv6地址列表
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表 {@link LinkedHashSet}
     */
    public static LinkedHashSet<String> localIpv6s() {
        return toIpList(localAddressList(t -> t instanceof Inet6Address));
    }

    /**
     * 地址列表转换为IP地址列表
     *
     * @param addressList 地址{@link Inet4Address} 列表
     * @return IP地址字符串列表
     */
    public static LinkedHashSet<String> toIpList(final Set<InetAddress> addressList) {
        final LinkedHashSet<String> ipSet = new LinkedHashSet<>();
        for (final InetAddress address : addressList) {
            ipSet.add(address.getHostAddress());
        }
        return ipSet;
    }

    /**
     * 获得本机的IP地址列表（包括Ipv4和Ipv6）
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表 {@link LinkedHashSet}
     */
    public static LinkedHashSet<String> localIps() {
        return toIpList(localAddressList(null));
    }

    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @param addressPredicate 过滤器，{@link Predicate#test(Object)}为{@code true}保留，null表示不过滤，获取所有地址
     * @return 过滤后的地址对象列表
     */
    public static LinkedHashSet<InetAddress> localAddressList(final Predicate<InetAddress> addressPredicate) {
        return localAddressList(null, addressPredicate);
    }

    /**
     * 获取所有满足过滤条件的本地IP地址对象
     *
     * @param networkInterfaceFilter 过滤器，null表示不过滤，获取所有网卡
     * @param addressPredicate       过滤器，{@link Predicate#test(Object)}为{@code true}保留，null表示不过滤，获取所有地址
     * @return 过滤后的地址对象列表
     */
    public static LinkedHashSet<InetAddress> localAddressList(final Predicate<NetworkInterface> networkInterfaceFilter, final Predicate<InetAddress> addressPredicate) {
        final Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            throw new InternalException(e);
        }

        if (networkInterfaces == null) {
            throw new InternalException("Get network interface error!");
        }

        final LinkedHashSet<InetAddress> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterfaceFilter != null && false == networkInterfaceFilter.test(networkInterface)) {
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
     * 获取本机网卡IP地址，这个地址为所有网卡中非回路地址的第一个
     * 如果获取失败调用 {@link InetAddress#getLocalHost()}方法获取。
     * 此方法不会抛出异常，获取失败将返回{@code null}
     * <p>
     * 参考：<a href="http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java">
     * http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java</a>
     *
     * @return 本机网卡IP地址，获取失败返回{@code null}
     */
    public static String getLocalhostString() {
        final InetAddress localhost = getLocalhost();
        if (null != localhost) {
            return localhost.getHostAddress();
        }
        return null;
    }

    /**
     * 获取本机网卡IP地址，规则如下：
     *
     * <pre>
     * 1. 查找所有网卡地址，必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv4地址
     * 2. 如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址
     * </pre>
     * <p>
     * 此方法不会抛出异常,获取失败将返回<code>null</code>
     *
     * @return 本机网卡IP地址, 获取失败返回<code>null</code>
     */
    public static InetAddress getLocalhost() {
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(address -> {
            // 非loopback地址，指127.*.*.*的地址
            return false == address.isLoopbackAddress()
                    // 需为IPV4地址
                    && address instanceof Inet4Address;
        });

        if (CollKit.isNotEmpty(localAddressList)) {
            InetAddress address2 = null;
            for (final InetAddress inetAddress : localAddressList) {
                if (false == inetAddress.isSiteLocalAddress()) {
                    // 非地区本地地址，指10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
                    return inetAddress;
                } else if (null == address2) {
                    address2 = inetAddress;
                }
            }

            if (null != address2) {
                return address2;
            }
        }

        try {
            return InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            // ignore
        }
        return null;
    }

    /**
     * 获得本机MAC地址
     *
     * @return 本机MAC地址
     */
    public static String getLocalMacAddress() {
        return getMacAddress(getLocalhost());
    }

    /**
     * 获得指定地址信息中的MAC地址，使用分隔符“-”
     *
     * @param inetAddress {@link InetAddress}
     * @return MAC地址，用-分隔
     */
    public static String getMacAddress(final InetAddress inetAddress) {
        return getMacAddress(inetAddress, Symbol.MINUS);
    }

    /**
     * 获得指定地址信息中的MAC地址
     *
     * @param inetAddress {@link InetAddress}
     * @param separator   分隔符，推荐使用“-”或者“:”
     * @return MAC地址，用-分隔
     */
    public static String getMacAddress(final InetAddress inetAddress, final String separator) {
        if (null == inetAddress) {
            return null;
        }

        final byte[] mac = getHardwareAddress(inetAddress);
        if (null != mac) {
            final StringBuilder sb = new StringBuilder();
            String s;
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append(separator);
                }
                // 字节转换为整数
                s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            return sb.toString();
        }

        return null;
    }

    /**
     * 获得指定地址信息中的硬件地址
     *
     * @param inetAddress {@link InetAddress}
     * @return 硬件地址
     */
    public static byte[] getHardwareAddress(final InetAddress inetAddress) {
        if (null == inetAddress) {
            return null;
        }

        try {
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
     * 获得本机物理地址
     *
     * @return 本机物理地址
     */
    public static byte[] getLocalHardwareAddress() {
        return getHardwareAddress(getLocalhost());
    }

    /**
     * 获取主机名称，一次获取会缓存名称
     *
     * @return 主机名称
     */
    public static String getLocalHostName() {
        final InetAddress localhost = getLocalhost();
        if (null != localhost) {
            String name = localhost.getHostName();
            if (StringKit.isEmpty(name)) {
                name = localhost.getHostAddress();
            }
            return name;
        }
        return null;
    }

    /**
     * 创建 {@link InetSocketAddress}
     *
     * @param host 域名或IP地址，空表示任意地址
     * @param port 端口，0表示系统分配临时端口
     * @return {@link InetSocketAddress}
     */
    public static InetSocketAddress createAddress(final String host, final int port) {
        if (StringKit.isBlank(host)) {
            return new InetSocketAddress(port);
        }
        return new InetSocketAddress(host, port);
    }

    /**
     * 简易的使用Socket发送数据
     *
     * @param host    Server主机
     * @param port    Server端口
     * @param isBlock 是否阻塞方式
     * @param data    需要发送的数据
     * @throws InternalException IO异常
     */
    public static void netCat(final String host, final int port, final boolean isBlock, final ByteBuffer data) throws InternalException {
        try (final SocketChannel channel = SocketChannel.open(createAddress(host, port))) {
            channel.configureBlocking(isBlock);
            channel.write(data);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 使用普通Socket发送数据
     *
     * @param host Server主机
     * @param port Server端口
     * @param data 数据
     * @throws InternalException IO异常
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
            IoKit.close(out);
        }
    }

    /**
     * 是否在CIDR规则配置范围内
     * 方法来自：【成都】小邓
     *
     * @param ip   需要验证的IP
     * @param cidr CIDR规则
     * @return 是否在范围内
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
     * Unicode域名转puny code
     *
     * @param unicode Unicode域名
     * @return puny code
     */
    public static String idnToASCII(final String unicode) {
        return IDN.toASCII(unicode);
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    public static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && StringKit.indexOf(ip, Symbol.C_COMMA) > 0) {
            final List<String> ips = StringKit.splitTrim(ip, Symbol.C_COMMA);
            for (final String subIp : ips) {
                if (false == isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 检测给定字符串是否为未知，多用于检测HTTP请求相关
     *
     * @param checkString 被检测的字符串
     * @return 是否未知
     */
    public static boolean isUnknown(final String checkString) {
        return StringKit.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

    /**
     * 检测IP地址是否能ping通
     *
     * @param ip IP地址
     * @return 返回是否ping通
     */
    public static boolean ping(final String ip) {
        return ping(ip, 200);
    }

    /**
     * 检测IP地址是否能ping通
     *
     * @param ip      IP地址
     * @param timeout 检测超时（毫秒）
     * @return 是否ping通
     */
    public static boolean ping(final String ip, final int timeout) {
        try {
            // 当返回值是true时，说明host是可用的，false则不可
            return InetAddress.getByName(ip).isReachable(timeout);
        } catch (final Exception ex) {
            return false;
        }
    }

    /**
     * 解析Cookie信息
     *
     * @param cookieStr Cookie字符串
     * @return cookie字符串
     */
    public static List<HttpCookie> parseCookies(final String cookieStr) {
        if (StringKit.isBlank(cookieStr)) {
            return Collections.emptyList();
        }
        return HttpCookie.parse(cookieStr);
    }

    /**
     * 检查远程端口是否开启
     *
     * @param address 远程地址
     * @param timeout 检测超时
     * @return 远程端口是否开启
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
     * 设置全局验证
     *
     * @param authenticator 验证器
     */
    public static void setGlobalAuthenticator(final Authenticator authenticator) {
        Authenticator.setDefault(authenticator);
    }

    /**
     * 获取DNS信息，如TXT信息：
     * <pre class="code">
     *     NetKit.attrNames("miaixz.org", "TXT")
     * </pre>
     *
     * @param hostName  主机域名
     * @param attrNames 属性
     * @return DNS信息
     */
    public static List<String> getDnsInfo(final String hostName, final String... attrNames) {
        final String uri = StringKit.addPrefixIfNot(hostName, "dns:");
        final Attributes attributes = ClassKit.getAttributes(uri, attrNames);

        final List<String> infos = new ArrayList<>();
        for (final Attribute attribute : new EnumerationIterator<>(attributes.getAll())) {
            try {
                infos.add((String) attribute.get());
            } catch (final NamingException ignore) {
                //ignore
            }
        }
        return infos;
    }

    /**
     * 根据 ip地址 和 掩码地址 获得 CIDR格式字符串
     *
     * @param ip   IP地址，点分十进制，如：xxx.xxx.xxx.xxx
     * @param mask 掩码地址，点分十进制，如：255.255.255.0
     * @return 返回 {@literal xxx.xxx.xxx.xxx/掩码位} 的格式
     */
    public static String formatIpBlock(final String ip, final String mask) {
        return ip + Symbol.SLASH + getMaskBitByMask(mask);
    }

    /**
     * 智能获取指定区间内的所有IP地址
     *
     * @param ipRange IP区间，支持 {@literal X.X.X.X-X.X.X.X} 或 {@literal X.X.X.X/X}
     * @param isAll   true:全量地址，false:可用地址；该参数仅在ipRange为X.X.X.X/X时才生效
     * @return 区间内的所有IP地址，点分十进制格式
     */
    public static List<String> list(final String ipRange, final boolean isAll) {
        if (ipRange.contains(Symbol.MINUS)) {
            // X.X.X.X-X.X.X.X
            final String[] range = StringKit.splitToArray(ipRange, Symbol.MINUS);
            return list(range[0], range[1]);
        } else if (ipRange.contains(Symbol.SLASH)) {
            // X.X.X.X/X
            final String[] param = StringKit.splitToArray(ipRange, Symbol.SLASH);
            return list(param[0], Integer.parseInt(param[1]), isAll);
        } else {
            return CollKit.of(ipRange);
        }
    }

    /**
     * 根据 IP地址 和 掩码位数 获取 子网所有ip地址
     *
     * @param ip      IP地址，点分十进制
     * @param maskBit 掩码位，例如24、32
     * @param isAll   true:全量地址，false:可用地址
     * @return 子网所有ip地址
     */
    public static List<String> list(final String ip, final int maskBit, final boolean isAll) {
        assertMaskBitValid(maskBit);
        // 避免后续的计算异常
        if (countByMaskBit(maskBit, isAll) == 0) {
            return new ArrayList<>(0);
        }

        final long startIp = getBeginIpLong(ip, maskBit);
        final long endIp = getEndIpLong(ip, maskBit);
        if (isAll) {
            return list(startIp, endIp);
        }

        // 可用地址: 排除开始和结束的地址
        if (startIp + 1 > endIp - 1) {
            return new ArrayList<>(0);
        }
        return list(startIp + 1, endIp - 1);
    }

    /**
     * 获得 指定区间内 所有ip地址
     *
     * @param ipFrom 开始IP，包含，点分十进制
     * @param ipTo   结束IP，包含，点分十进制
     * @return 区间内所有ip地址
     */
    public static List<String> list(final String ipFrom, final String ipTo) {
        return list(ipv4ToLong(ipFrom), ipv4ToLong(ipTo));
    }

    /**
     * 得到指定区间内的所有IP地址
     *
     * @param ipFrom 开始IP, 包含
     * @param ipTo   结束IP, 包含
     * @return 区间内所有ip地址，点分十进制表示
     */
    public static List<String> list(final long ipFrom, final long ipTo) {
        // 确定ip数量
        final int count = countByIpRange(ipFrom, ipTo);

        final List<String> ips = new ArrayList<>(count);
        final StringBuilder sb = StringKit.builder(15);
        for (long ip = ipFrom, end = ipTo + 1; ip < end; ip++) {
            sb.setLength(0);
            ips.add(sb.append((int) (ip >> 24) & 0xFF).append(Symbol.C_DOT)
                    .append((int) (ip >> 16) & 0xFF).append(Symbol.C_DOT)
                    .append((int) (ip >> 8) & 0xFF).append(Symbol.C_DOT)
                    .append((int) ip & 0xFF)
                    .toString());
        }
        return ips;
    }

    /**
     * 根据 ip的long值 获取 ip字符串，即：xxx.xxx.xxx.xxx
     *
     * @param ip IP的long表示形式
     * @return 点分十进制ip地址
     */
    public static String longToIpv4(final long ip) {
        return StringKit.builder(15)
                .append((int) (ip >> 24) & 0xFF).append(Symbol.C_DOT)
                .append((int) (ip >> 16) & 0xFF).append(Symbol.C_DOT)
                .append((int) (ip >> 8) & 0xFF).append(Symbol.C_DOT)
                .append((int) ip & 0xFF)
                .toString();
    }

    /**
     * 将 ip字符串 转换为 long值
     * <p>方法别名：inet_aton</p>
     *
     * @param strIp ip地址，点分十进制，xxx.xxx.xxx.xxx
     * @return ip的long值
     */
    public static long ipv4ToLong(final String strIp) {
        final Matcher matcher = RegEx.IPV4.matcher(strIp);
        Assert.isTrue(matcher.matches(), "Invalid IPv4 address: {}", strIp);
        return matchAddress(matcher);
    }

    /**
     * 根据 ip 和 掩码位 获取 子网的起始IP（字符串型）
     * <p>方法别名：inet_ntoa</p>
     *
     * @param ip      给定的IP，点分十进制，如：xxx.xxx.xxx.xxx
     * @param maskBit 给定的掩码位，如：30
     * @return 起始IP的字符串表示
     */
    public static String getBeginIpStr(final String ip, final int maskBit) {
        return longToIpv4(getBeginIpLong(ip, maskBit));
    }

    /**
     * 根据 ip 和 掩码位 获取 子网的起始IP（Long型）
     *
     * @param ip      给定的IP，点分十进制，如：xxx.xxx.xxx.xxx
     * @param maskBit 给定的掩码位，如：30
     * @return 起始IP的长整型表示
     */
    public static long getBeginIpLong(final String ip, final int maskBit) {
        assertMaskBitValid(maskBit);
        return ipv4ToLong(ip) & MaskBit.getMaskIpLong(maskBit);
    }

    /**
     * 根据 ip 和 掩码位 获取 子网的终止IP（字符串型）
     *
     * @param ip      给定的IP，点分十进制，如：xxx.xxx.xxx.xxx
     * @param maskBit 给定的掩码位，如：30
     * @return 终止IP的字符串表示
     */
    public static String getEndIpStr(final String ip, final int maskBit) {
        return longToIpv4(getEndIpLong(ip, maskBit));
    }

    /**
     * 根据 ip 和 掩码位 获取 子网的终止IP（Long型）
     *
     * @param ip      给定的IP，点分十进制，如：xxx.xxx.xxx.xxx
     * @param maskBit 给定的掩码位，如：30
     * @return 终止IP的长整型表示
     */
    public static long getEndIpLong(final String ip, final int maskBit) {
        return getBeginIpLong(ip, maskBit) + ~MaskBit.getMaskIpLong(maskBit);
    }

    /**
     * 将 子网掩码 转换为 掩码位
     *
     * @param mask 掩码的点分十进制表示，例如 255.255.255.0
     * @return 掩码位，例如 24
     * @throws IllegalArgumentException 子网掩码非法
     */
    public static int getMaskBitByMask(final String mask) {
        final Integer maskBit = MaskBit.getMaskBit(mask);
        Assert.notNull(maskBit, "Invalid netmask：{}", mask);
        return maskBit;
    }

    /**
     * 获取 子网内的 地址总数
     *
     * @param maskBit 掩码位，取值范围：[1, 32]
     * @param isAll   true:全量地址，false:可用地址
     * @return 子网内地址总数
     */
    public static int countByMaskBit(final int maskBit, final boolean isAll) {
        assertMaskBitValid(maskBit);
        //如果掩码位等于32，则可用地址为0
        if (maskBit == 32 && false == isAll) {
            return 0;
        }

        final int count = 1 << (32 - maskBit);
        return isAll ? count : count - 2;
    }

    /**
     * 根据 掩码位 获取 掩码地址
     *
     * @param maskBit 掩码位，如：24，取值范围：[1, 32]
     * @return 掩码地址，点分十进制，如:255.255.255.0
     */
    public static String getMaskByMaskBit(final int maskBit) {
        assertMaskBitValid(maskBit);
        return MaskBit.get(maskBit);
    }

    /**
     * 根据 开始IP 与 结束IP 获取 掩码地址
     *
     * @param fromIp 开始IP，包含，点分十进制
     * @param toIp   结束IP，包含，点分十进制
     * @return 掩码地址，点分十进制
     */
    public static String getMaskByIpRange(final String fromIp, final String toIp) {
        final long toIpLong = ipv4ToLong(toIp);
        final long fromIpLong = ipv4ToLong(fromIp);
        Assert.isTrue(fromIpLong <= toIpLong, "Start IP must be less than or equal to end IP!");

        return StringKit.builder(15)
                .append(255 - getPartOfIp(toIpLong, 1) + getPartOfIp(fromIpLong, 1)).append(Symbol.C_DOT)
                .append(255 - getPartOfIp(toIpLong, 2) + getPartOfIp(fromIpLong, 2)).append(Symbol.C_DOT)
                .append(255 - getPartOfIp(toIpLong, 3) + getPartOfIp(fromIpLong, 3)).append(Symbol.C_DOT)
                .append(255 - getPartOfIp(toIpLong, 4) + getPartOfIp(fromIpLong, 4))
                .toString();
    }

    /**
     * 获得 指定区间内的 ip数量
     *
     * @param fromIp 开始IP，包含，点分十进制
     * @param toIp   结束IP，包含，点分十进制
     * @return IP数量
     */
    public static int countByIpRange(final String fromIp, final String toIp) {
        return countByIpRange(ipv4ToLong(fromIp), ipv4ToLong(toIp));
    }

    /**
     * 获得 指定区间内的 ip数量
     *
     * @param fromIp 开始IP，包含
     * @param toIp   结束IP，包含
     * @return IP数量
     */
    public static int countByIpRange(final long fromIp, final long toIp) {
        Assert.isTrue(fromIp <= toIp, "Start IP must be less than or equal to end IP!");

        int count = 1;
        count += (getPartOfIp(toIp, 4) - getPartOfIp(fromIp, 4))
                + ((getPartOfIp(toIp, 3) - getPartOfIp(fromIp, 3)) << 8)
                + ((getPartOfIp(toIp, 2) - getPartOfIp(fromIp, 2)) << 16)
                + ((getPartOfIp(toIp, 1) - getPartOfIp(fromIp, 1)) << 24);
        return count;
    }

    /**
     * 判断掩码是否合法
     *
     * @param mask 掩码的点分十进制表示，例如 255.255.255.0
     * @return true：掩码合法；false：掩码不合法
     */
    public static boolean isMaskValid(final String mask) {
        return MaskBit.getMaskBit(mask) != null;
    }

    /**
     * 判断掩码位是否合法
     *
     * @param maskBit 掩码位，有效范围：[1, 32]
     * @return true：掩码位合法；false：掩码位不合法
     */
    public static boolean isMaskBitValid(final int maskBit) {
        return maskBit >= Normal._1 && maskBit <= Normal._32;
    }

    /**
     * 判定是否为内网IPv4
     * 私有IP：
     * <pre>
     * A类 10.0.0.0-10.255.255.255
     * B类 172.16.0.0-172.31.255.255
     * C类 192.168.0.0-192.168.255.255
     * </pre>
     * 当然，还有127这个网段是环回地址
     *
     * @param ipAddress IP地址，点分十进制
     * @return 是否为内网IP
     */
    public static boolean isInnerIP(final String ipAddress) {
        final long ipNum = ipv4ToLong(ipAddress);
        return isBetween(ipNum, A_INNER_IP_LONG_BEGIN, A_INNER_IP_LONG_END)
                || isBetween(ipNum, B_INNER_IP_LONG_BEGIN, B_INNER_IP_LONG_END)
                || isBetween(ipNum, C_INNER_IP_LONG_BEGIN, C_INNER_IP_LONG_END)
                || Http.HOST_IPV4.equals(ipAddress);
    }

    /**
     * 将匹配到的Ipv4地址转为Long类型
     *
     * @param matcher 匹配到的Ipv4正则
     * @return ip的long值
     */
    private static long matchAddress(final Matcher matcher) {
        int addr = 0;
        // 每个点分十进制数字 转为 8位二进制
        addr |= Integer.parseInt(matcher.group(1));
        addr <<= 8;
        addr |= Integer.parseInt(matcher.group(2));
        addr <<= 8;
        addr |= Integer.parseInt(matcher.group(3));
        addr <<= 8;
        addr |= Integer.parseInt(matcher.group(4));
        // int的最高位无法直接使用，转为Long
        if (addr < 0) {
            return 0xffffffffL & addr;
        }
        return addr;
    }

    /**
     * 指定IP是否在指定范围内
     *
     * @param userIp 用户IP
     * @param begin  开始IP，包含
     * @param end    结束IP，包含
     * @return 是否在范围内
     */
    private static boolean isBetween(final long userIp, final long begin, final long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    /**
     * 校验 掩码位数，合法范围为：[1,32]，不合法则抛出异常
     *
     * @param maskBit 掩码位数
     */
    private static void assertMaskBitValid(final int maskBit) {
        Assert.isTrue(isMaskBitValid(maskBit), "Invalid maskBit：{}", maskBit);
    }

    /**
     * 获取ip(Long类型)指定部分的十进制值，即，{@literal X.X.X.X }形式中每个部分的值
     * <p>例如，ip为{@literal 0xC0A802FA}，第1部分的值为：
     * <ul>
     * <li>第1部分的值为：@literal 0xC0}，十进制值为：192</li>
     * <li>第2部分的值为：@literal 0xA8}，十进制值为：168</li>
     * <li>第3部分的值为：@literal 0x02}，十进制值为：2</li>
     * <li>第4部分的值为：@literal 0xFA}，十进制值为：250</li>
     * </ul>
     * </p>
     *
     * @param ip       ip地址，Long类型
     * @param position 指定位置，取值范围：[1,4]
     * @return ip地址指定部分的十进制值
     */
    private static int getPartOfIp(final long ip, final int position) {
        switch (position) {
            case 1:
                return ((int) ip >> 24) & 0xFF;
            case 2:
                return ((int) ip >> 16) & 0xFF;
            case 3:
                return ((int) ip >> 8) & 0xFF;
            case 4:
                return ((int) ip) & 0xFF;
            default:
                throw new IllegalArgumentException("Illegal position of ip Long: " + position);
        }
    }

}
