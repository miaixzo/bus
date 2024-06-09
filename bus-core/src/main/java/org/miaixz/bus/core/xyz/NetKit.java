/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.NonAuthenticator;
import org.miaixz.bus.core.net.ip.IPv4;
import org.miaixz.bus.core.text.CharsBacker;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Predicate;

/**
 * 网络相关工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NetKit {

    /**
     * 根据long值获取ip v4地址
     *
     * @param longIP IP的long表示形式
     * @return IP V4 地址
     * @see IPv4#longToIpv4(long)
     */
    public static String longToIpv4(final long longIP) {
        return IPv4.longToIpv4(longIP);
    }

    /**
     * 根据ip地址计算出long型的数据
     *
     * @param strIP IP V4 地址
     * @return long值
     * @see IPv4#ipv4ToLong(String)
     */
    public static long ipv4ToLong(final String strIP) {
        return IPv4.ipv4ToLong(strIP);
    }

    /**
     * 检测本地端口可用性
     *
     * @param port 被检测的端口
     * @return 是否可用
     */
    public static boolean isUsableLocalPort(final int port) {
        if (!isValidPort(port)) {
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
        return port >= 0 && port <= Normal._65535;
    }

    /**
     * 查找1024~65535范围内的可用端口
     * 此方法只检测给定范围内的随机一个端口，检测65535-1024次
     * 来自org.springframework.util.SocketUtils
     *
     * @return 可用的端口
     */
    public static int getUsableLocalPort() {
        return getUsableLocalPort(Normal._1024);
    }

    /**
     * 查找指定范围内的可用端口，最大值为65535
     * 此方法只检测给定范围内的随机一个端口，检测65535-minPort次
     * 来自org.springframework.util.SocketUtils
     *
     * @param minPort 端口最小值（包含）
     * @return 可用的端口
     */
    public static int getUsableLocalPort(final int minPort) {
        return getUsableLocalPort(minPort, Normal._65535);
    }

    /**
     * 查找指定范围内的可用端口
     * 此方法只检测给定范围内的随机一个端口，检测maxPort-minPort次
     * 来自org.springframework.util.SocketUtils
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
     * 来自org.springframework.util.SocketUtils
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
     * @see IPv4#isInnerIP(String)
     */
    public static boolean isInnerIP(final String ipAddress) {
        return IPv4.isInnerIP(ipAddress);
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
        return StringKit.builder(ip.length()).append(ip, 0, ip.lastIndexOf(".") + 1).append(Symbol.STAR).toString();
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
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(t -> t instanceof Inet4Address);

        return toIpList(localAddressList);
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
        final LinkedHashSet<InetAddress> localAddressList = localAddressList(null);
        return toIpList(localAddressList);
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
     * 获取本机网卡IP地址，这个地址为所有网卡中非回路地址的第一个
     * 如果获取失败调用 {@link InetAddress#getLocalHost()}方法获取。
     * 此方法不会抛出异常，获取失败将返回{@code null}
     * <p>
     * 参考：<a href="http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java">
     * http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java</a>
     *
     * @return 本机网卡IP地址，获取失败返回{@code null}
     */
    public static String getLocalhostStringV4() {
        final InetAddress localhost = IPv4.getLocalhost();
        if (null != localhost) {
            return localhost.getHostAddress();
        }
        return null;
    }

    /**
     * 获取本机网卡IPv4地址，规则如下：
     *
     * <ul>
     *     <li>必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv4地址</li>
     *     <li>多网卡则返回第一个满足条件的地址</li>
     *     <li>如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址</li>
     * </ul>
     *
     * <p>
     * 此方法不会抛出异常，获取失败将返回{@code null}
     * <p>
     *
     * @return 本机网卡IP地址，获取失败返回{@code null}
     */
    public static InetAddress getLocalhostV4() {
        return IPv4.getLocalhost();
    }

    /**
     * 获得本机MAC地址，默认使用获取到的IPv4本地地址对应网卡
     *
     * @return 本机MAC地址
     */
    public static String getLocalMacAddressV4() {
        return IPv4.getLocalMacAddress();
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
            IoKit.closeQuietly(out);
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
            return InetAddress.getByName(ip).isReachable(timeout); // 当返回值是true时，说明host是可用的，false则不可。
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
     * @param user 用户名
     * @param pass 密码，考虑安全，此处不使用String
     */
    public static void setGlobalAuthenticator(final String user, final char[] pass) {
        setGlobalAuthenticator(new NonAuthenticator(user, pass));
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
     *     NetKit.attrNames("xxx.cn", "TXT")
     * </pre>
     *
     * @param hostName  主机域名
     * @param attrNames 属性
     * @return DNS信息
     */
    public static List<String> getDnsInfo(final String hostName, final String... attrNames) {
        final String uri = StringKit.addPrefixIfNot(hostName, "dns:");
        final Attributes attributes = Keys.getAttributes(uri, attrNames);

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
     * 获取地址名称，如果无名称返回地址
     * 如果提供的地址为{@code null}返回{@code null}
     *
     * @param address {@link InetAddress}，提供{@code null}返回{@code null}
     * @return 地址名称或地址
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

        return toMacAddress(getHardwareAddress(inetAddress), separator);
    }

    /**
     * 获得指定地址信息中的硬件地址（MAC地址）
     *
     * @param inetAddress {@link InetAddress}
     * @return 硬件地址
     */
    public static byte[] getHardwareAddress(final InetAddress inetAddress) {
        if (null == inetAddress) {
            return null;
        }

        try {
            // 获取地址对应网卡
            final NetworkInterface networkInterface =
                    NetworkInterface.getByInetAddress(inetAddress);
            if (null != networkInterface) {
                return networkInterface.getHardwareAddress();
            }
        } catch (final SocketException e) {
            throw new InternalException(e);
        }
        return null;
    }

    /**
     * 将bytes类型的mac地址转换为可读字符串，通常地址每个byte位使用16进制表示，并用指定分隔符分隔
     *
     * @param mac       MAC地址（网卡硬件地址）
     * @param separator 分隔符
     * @return MAC地址字符串
     */
    public static String toMacAddress(final byte[] mac, final String separator) {
        if (null == mac) {
            return null;
        }

        final StringBuilder sb = new StringBuilder(
                // 字符串长度 = byte个数*2（每个byte转16进制后占2位） + 分隔符总长度
                mac.length * 2 + (mac.length - 1) * separator.length());
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

}
