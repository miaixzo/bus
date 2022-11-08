/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.collection.EnumerationIterator;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.*;
import org.aoju.bus.core.net.MaskBit;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
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
            host = Http.HTTP_HOST_IPV4;
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
        if (ip != null && ip.indexOf(Symbol.COMMA) > 0) {
            final String[] ips = ip.trim().split(Symbol.COMMA);
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
     *     NetKit.attrNames("aoju.org", "TXT")
     * </pre>
     *
     * @param hostName  主机域名
     * @param attrNames 属性
     * @return DNS信息
     */
    public static List<String> getDnsInfo(final String hostName, final String... attrNames) {
        final String uri = StringKit.addPrefixIfNot(hostName, "dns:");
        final Attributes attributes = getAttributes(uri, attrNames);

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
     * 格式化IP段
     *
     * @param ip   IP地址
     * @param mask 掩码
     * @return 返回xxx.xxx.xxx.xxx/mask的格式
     */
    public static String formatIpBlock(final String ip, final String mask) {
        return ip + Symbol.SLASH + getMaskBitByMask(mask);
    }

    /**
     * 智能转换IP地址集合
     *
     * @param ipRange IP段，支持X.X.X.X-X.X.X.X或X.X.X.X/X
     * @param isAll   true:全量地址，false:可用地址；仅在ipRange为X.X.X.X/X时才生效
     * @return IP集
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
     * 根据IP地址、子网掩码获取IP地址区间
     *
     * @param ip      IP地址
     * @param maskBit 掩码位，例如24、32
     * @param isAll   true:全量地址，false:可用地址
     * @return 区间地址
     */
    public static List<String> list(final String ip, final int maskBit, final boolean isAll) {
        if (maskBit == Normal._32) {
            final List<String> list = new ArrayList<>();
            if (isAll) {
                list.add(ip);
            }
            return list;
        }

        String startIp = getBeginIpStr(ip, maskBit);
        String endIp = getEndIpStr(ip, maskBit);
        if (isAll) {
            return list(startIp, endIp);
        }

        int lastDotIndex = startIp.lastIndexOf(Symbol.C_DOT) + 1;
        startIp = StringKit.subPre(startIp, lastDotIndex) +
                (Integer.parseInt(Objects.requireNonNull(StringKit.subSuf(startIp, lastDotIndex))) + 1);
        lastDotIndex = endIp.lastIndexOf(Symbol.C_DOT) + 1;
        endIp = StringKit.subPre(endIp, lastDotIndex) +
                (Integer.parseInt(Objects.requireNonNull(StringKit.subSuf(endIp, lastDotIndex))) - 1);
        return list(startIp, endIp);
    }

    /**
     * 得到IP地址区间
     *
     * @param ipFrom 开始IP
     * @param ipTo   结束IP
     * @return 区间地址
     */
    public static List<String> list(final String ipFrom, final String ipTo) {
        final int[] ipf = Convert.convert(int[].class, StringKit.splitToArray(ipFrom, Symbol.C_DOT));
        final int[] ipt = Convert.convert(int[].class, StringKit.splitToArray(ipTo, Symbol.C_DOT));

        final List<String> ips = new ArrayList<>();
        for (int a = ipf[0]; a <= ipt[0]; a++) {
            for (int b = (a == ipf[0] ? ipf[1] : 0); b <= (a == ipt[0] ? ipt[1]
                    : 255); b++) {
                for (int c = (b == ipf[1] ? ipf[2] : 0); c <= (b == ipt[1] ? ipt[2]
                        : 255); c++) {
                    for (int d = (c == ipf[2] ? ipf[3] : 0); d <= (c == ipt[2] ? ipt[3]
                            : 255); d++) {
                        ips.add(a + "." + b + "." + c + "." + d);
                    }
                }
            }
        }
        return ips;
    }

    /**
     * 根据long值获取ip v4地址：xx.xx.xx.xx
     *
     * @param longIP IP的long表示形式
     * @return IP V4 地址
     */
    public static String longToIpv4(final long longIP) {
        final StringBuilder sb = StringKit.builder();
        // 直接右移24位
        sb.append(longIP >> 24 & 0xFF);
        sb.append(Symbol.C_DOT);
        // 将高8位置0，然后右移16位
        sb.append(longIP >> 16 & 0xFF);
        sb.append(Symbol.C_DOT);
        sb.append(longIP >> 8 & 0xFF);
        sb.append(Symbol.C_DOT);
        sb.append(longIP & 0xFF);
        return sb.toString();
    }

    /**
     * 根据ip地址(xxx.xxx.xxx.xxx)计算出long型的数据
     * 方法别名：inet_aton
     *
     * @param strIP IP V4 地址
     * @return long值
     */
    public static long ipv4ToLong(final String strIP) {
        final Matcher matcher = RegEx.IPV4.matcher(strIP);
        if (matcher.matches()) {
            return matchAddress(matcher);
        }
        throw new IllegalArgumentException("Invalid IPv4 address!");
    }

    /**
     * 根据 ip/掩码位 计算IP段的起始IP（字符串型）
     * 方法别名：inet_ntoa
     *
     * @param ip      给定的IP，如218.240.38.69
     * @param maskBit 给定的掩码位，如30
     * @return 起始IP的字符串表示
     */
    public static String getBeginIpStr(final String ip, final int maskBit) {
        return longToIpv4(getBeginIpLong(ip, maskBit));
    }

    /**
     * 根据 ip/掩码位 计算IP段的起始IP（Long型）
     *
     * @param ip      给定的IP，如218.240.38.69
     * @param maskBit 给定的掩码位，如30
     * @return 起始IP的长整型表示
     */
    public static Long getBeginIpLong(final String ip, final int maskBit) {
        return ipv4ToLong(ip) & ipv4ToLong(getMaskByMaskBit(maskBit));
    }

    /**
     * 根据 ip/掩码位 计算IP段的终止IP（字符串型）
     *
     * @param ip      给定的IP，如218.240.38.69
     * @param maskBit 给定的掩码位，如30
     * @return 终止IP的字符串表示
     */
    public static String getEndIpStr(final String ip, final int maskBit) {
        return longToIpv4(getEndIpLong(ip, maskBit));
    }

    /**
     * 根据子网掩码转换为掩码位
     *
     * @param mask 掩码的点分十进制表示，例如 255.255.255.0
     * @return 掩码位，例如 24
     * @throws IllegalArgumentException 子网掩码非法
     */
    public static int getMaskBitByMask(final String mask) {
        final Integer maskBit = MaskBit.getMaskBit(mask);
        if (maskBit == null) {
            throw new IllegalArgumentException("Invalid netmask " + mask);
        }
        return maskBit;
    }

    /**
     * 计算子网大小
     *
     * @param maskBit 掩码位
     * @param isAll   true:全量地址，false:可用地址
     * @return 地址总数
     */
    public static int countByMaskBit(final int maskBit, final boolean isAll) {
        //如果是可用地址的情况，掩码位小于等于0或大于等于32，则可用地址为0
        if ((false == isAll) && (maskBit <= 0 || maskBit >= 32)) {
            return 0;
        }

        final int count = (int) Math.pow(2, 32 - maskBit);
        return isAll ? count : count - 2;
    }

    /**
     * 根据掩码位获取掩码
     *
     * @param maskBit 掩码位
     * @return 掩码
     */
    public static String getMaskByMaskBit(final int maskBit) {
        return MaskBit.get(maskBit);
    }

    /**
     * 根据开始IP与结束IP计算掩码
     *
     * @param fromIp 开始IP
     * @param toIp   结束IP
     * @return 掩码x.x.x.x
     */
    public static String getMaskByIpRange(final String fromIp, final String toIp) {
        final long toIpLong = ipv4ToLong(toIp);
        final long fromIpLong = ipv4ToLong(fromIp);
        Assert.isTrue(fromIpLong < toIpLong, "to IP must be greater than from IP!");

        final String[] fromIpSplit = StringKit.splitToArray(fromIp, Symbol.C_DOT);
        final String[] toIpSplit = StringKit.splitToArray(toIp, Symbol.C_DOT);
        final StringBuilder mask = new StringBuilder();
        for (int i = 0; i < toIpSplit.length; i++) {
            mask.append(255 - Integer.parseInt(toIpSplit[i]) + Integer.parseInt(fromIpSplit[i])).append(Symbol.C_DOT);
        }
        return mask.substring(0, mask.length() - 1);
    }

    /**
     * 计算IP区间有多少个IP
     *
     * @param fromIp 开始IP
     * @param toIp   结束IP
     * @return IP数量
     */
    public static int countByIpRange(final String fromIp, final String toIp) {
        final long toIpLong = ipv4ToLong(toIp);
        final long fromIpLong = ipv4ToLong(fromIp);
        if (fromIpLong > toIpLong) {
            throw new IllegalArgumentException("to IP must be greater than from IP!");
        }
        int count = 1;
        final int[] fromIpSplit = StringKit.split(fromIp, Symbol.C_DOT).stream().mapToInt(Integer::parseInt).toArray();
        final int[] toIpSplit = StringKit.split(toIp, Symbol.C_DOT).stream().mapToInt(Integer::parseInt).toArray();
        for (int i = fromIpSplit.length - 1; i >= 0; i--) {
            count += (toIpSplit[i] - fromIpSplit[i]) * Math.pow(256, fromIpSplit.length - i - 1);
        }
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
     * @param maskBit 掩码位，例如 24
     * @return true：掩码位合法；false：掩码位不合法
     */
    public static boolean isMaskBitValid(final int maskBit) {
        return MaskBit.get(maskBit) != null;
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
     * @param ipAddress IP地址
     * @return 是否为内网IP
     */
    public static boolean isInnerIP(final String ipAddress) {
        final boolean isInnerIp;
        final long ipNum = ipv4ToLong(ipAddress);

        final long aBegin = ipv4ToLong("10.0.0.0");
        final long aEnd = ipv4ToLong("10.255.255.255");

        final long bBegin = ipv4ToLong("172.16.0.0");
        final long bEnd = ipv4ToLong("172.31.255.255");

        final long cBegin = ipv4ToLong("192.168.0.0");
        final long cEnd = ipv4ToLong("192.168.255.255");

        isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd) || Http.HTTP_HOST_IPV4.equals(ipAddress);
        return isInnerIp;
    }

    /**
     * 根据 ip/掩码位 计算IP段的终止IP（Long型）
     * 注：此接口返回负数，请使用转成字符串后再转Long型
     *
     * @param ip      给定的IP，如218.240.38.69
     * @param maskBit 给定的掩码位，如30
     * @return 终止IP的长整型表示
     */
    public static Long getEndIpLong(final String ip, final int maskBit) {
        return getBeginIpLong(ip, maskBit)
                + ~ipv4ToLong(getMaskByMaskBit(maskBit));
    }

    /**
     * 将匹配到的Ipv4地址的4个分组分别处理
     *
     * @param matcher 匹配到的Ipv4正则
     * @return ipv4对应long
     */
    private static long matchAddress(final Matcher matcher) {
        long addr = 0;
        for (int i = 1; i <= 4; ++i) {
            addr |= Long.parseLong(matcher.group(i)) << 8 * (4 - i);
        }
        return addr;
    }

    /**
     * 指定IP的long是否在指定范围内
     *
     * @param userIp 用户IP
     * @param begin  开始IP
     * @param end    结束IP
     * @return 是否在范围内
     */
    private static boolean isInner(final long userIp, final long begin, final long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    /**
     * 创建{@link InitialDirContext}
     *
     * @param environment 环境参数，{code null}表示无参数
     * @return {@link InitialDirContext}
     */
    public static InitialDirContext createInitialDirContext(final Map<String, String> environment) {
        try {
            if (MapKit.isEmpty(environment)) {
                return new InitialDirContext();
            }
            return new InitialDirContext(Convert.convert(Hashtable.class, environment));
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建{@link InitialContext}
     *
     * @param environment 环境参数，{code null}表示无参数
     * @return {@link InitialContext}
     */
    public static InitialContext createInitialContext(final Map<String, String> environment) {
        try {
            if (MapKit.isEmpty(environment)) {
                return new InitialContext();
            }
            return new InitialContext(Convert.convert(Hashtable.class, environment));
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取指定容器环境的对象的属性
     * 如获取DNS属性，则URI为类似：dns:aoju.cn
     *
     * @param uri     URI字符串，格式为[scheme:][name]/[domain]
     * @param attrIds 需要获取的属性ID名称
     * @return {@link Attributes}
     */
    public static Attributes getAttributes(final String uri, final String... attrIds) {
        try {
            return createInitialDirContext(null).getAttributes(uri, attrIds);
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

}
