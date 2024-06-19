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
package org.miaixz.bus.core.net.ip;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.MaskBit;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;

/**
 * IPV4地址工具类
 * 名词解释：
 * <ul>
 *     <li>ip字符串：点分十进制，形如：xxx.xxx.xxx.xxx</li>
 *     <li>ip的Long类型：有效位32位，每8位可以转为一个十进制数，例如：0xC0A802FA， 转为点分十进制是：192.168.2.250</li>
 *     <li>掩码地址：点分十进制，例如：255.255.255.0</li>
 *     <li>掩码位：int类型，例如 24, 它代表的掩码地址为：255.255.255.0；掩码位和掩码地址的相互转换，请使用 {@link MaskBit}</li>
 *     <li>CIDR：无类域间路由，形如：xxx.xxx.xxx.xxx/掩码位，192.168.1.101/24</li>
 *     <li>全量地址：区间内所有ip地址，包含区间两端</li>
 *     <li>可用地址：区间内所有ip地址，但是不包含区间两端</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IPv4 {

    /**
     * localhost默认解析的ip地址的数值形式
     */
    public static final long LOCAL_IP_NUM = IPv4.ipv4ToLong(Protocol.HOST_IPV4);
    /**
     * Ipv4最小值数值形式
     */
    public static final long IPV4_NUM_MIN = IPv4.ipv4ToLong(Protocol.IPV4_STR_MIN);
    /**
     * Ipv4未使用地址最小值数值形式
     */
    public static final long IPV4_UNUSED_NUM_MIN = IPv4.ipv4ToLong(Protocol.IPV4_STR_MIN);

    /**
     * Ipv4未使用地址最大值数值形式
     */
    public static final long IPV4_UNUSED_NUM_MAX = IPv4.ipv4ToLong(Protocol.IPV4_UNUSED_STR_MAX);

    /**
     * Ipv4最小掩码位
     */
    public static final int IPV4_MASK_BIT_MIN = 0;

    /**
     * Ipv4有意义的最小掩码位
     */
    public static final int IPV4_MASK_BIT_VALID_MIN = 1;
    /**
     * Ipv4有意义的最小掩码字符串
     */
    public static final String IPV4_MASK_VALID_MIN = MaskBit.get(IPV4_MASK_BIT_VALID_MIN);

    /**
     * Ipv4最大掩码位
     */
    public static final int IPV4_MASK_BIT_MAX = 32;
    /**
     * Ipv4最大掩码字符串
     */
    public static final String IPV4_MASK_MAX = MaskBit.get(IPV4_MASK_BIT_MAX);
    /**
     * Ipv4 本地回环地址最小值字符串形式
     */
    public static final String IPV4_LOOPBACK_STR_MIN = "127.0.0.0";
    /**
     * Ipv4 本地回环地址最小值数值形式
     */
    public static final long IPV4_LOOPBACK_NUM_MIN = IPv4.ipv4ToLong(IPV4_LOOPBACK_STR_MIN);
    /**
     * Ipv4 本地回环地址最大值字符串形式
     */
    public static final String IPV4_LOOPBACK_STR_MAX = "127.255.255.255";
    /**
     * Ipv4 本地回环地址最大值数值形式
     */
    public static final long IPV4_LOOPBACK_NUM_MAX = IPv4.ipv4ToLong(IPV4_LOOPBACK_STR_MAX);
    /**
     * Ipv4 A类地址最小值字符串形式
     */
    public static final String IPV4_A_STR_MIN = "0.0.0.0";
    /**
     * Ipv4 A类地址最小值数值形式
     */
    public static final long IPV4_A_NUM_MIN = IPv4.ipv4ToLong(IPV4_A_STR_MIN);
    /**
     * Ipv4 A类地址最大值字符串形式
     */
    public static final String IPV4_A_STR_MAX = "127.255.255.255";
    /**
     * Ipv4 A类地址最大值数值形式
     */
    public static final long IPV4_A_NUM_MAX = IPv4.ipv4ToLong(IPV4_A_STR_MAX);
    /**
     * Ipv4 A类地址第一个公网网段最小值字符串形式
     */
    public static final String IPV4_A_PUBLIC_1_STR_MIN = "1.0.0.0";
    /**
     * Ipv4 A类地址第一个公网网段最小值数值形式
     */
    public static final long IPV4_A_PUBLIC_1_NUM_MIN = IPv4.ipv4ToLong(IPV4_A_PUBLIC_1_STR_MIN);
    /**
     * Ipv4 A类地址第一个公网网段最大值字符串形式
     */
    public static final String IPV4_A_PUBLIC_1_STR_MAX = "9.255.255.255";
    /**
     * Ipv4 A类地址第一个公网网段最大值数值形式
     */
    public static final long IPV4_A_PUBLIC_1_NUM_MAX = IPv4.ipv4ToLong(IPV4_A_PUBLIC_1_STR_MAX);
    /**
     * Ipv4 A类地址私网网段最小值字符串形式
     */
    public static final String IPV4_A_PRIVATE_STR_MIN = "10.0.0.0";
    /**
     * Ipv4 A类地址私网网段最小值数值形式
     */
    public static final long IPV4_A_PRIVATE_NUM_MIN = IPv4.ipv4ToLong(IPV4_A_PRIVATE_STR_MIN);
    /**
     * Ipv4 A类地址私网网段最大值字符串形式
     */
    public static final String IPV4_A_PRIVATE_STR_MAX = "10.255.255.255";
    /**
     * Ipv4 A类地址私网网段最大值数值形式
     */
    public static final long IPV4_A_PRIVATE_NUM_MAX = IPv4.ipv4ToLong(IPV4_A_PRIVATE_STR_MAX);
    /**
     * Ipv4 A类地址第二个公网网段最小值字符串形式
     */
    public static final String IPV4_A_PUBLIC_2_STR_MIN = "11.0.0.0";
    /**
     * Ipv4 A类地址第二个公网网段最小值数值形式
     */
    public static final long IPV4_A_PUBLIC_2_NUM_MIN = IPv4.ipv4ToLong(IPV4_A_PUBLIC_2_STR_MIN);

    /**
     * Ipv4 A类地址第二个公网网段最大值字符串形式
     */
    public static final String IPV4_A_PUBLIC_2_STR_MAX = "126.255.255.255";
    /**
     * Ipv4 A类地址第二个公网网段最大值数值形式
     */
    public static final long IPV4_A_PUBLIC_2_NUM_MAX = IPv4.ipv4ToLong(IPV4_A_PUBLIC_2_STR_MAX);
    /**
     * Ipv4 B类地址最小值字符串形式
     */
    public static final String IPV4_B_STR_MIN = "128.0.0.0";
    /**
     * Ipv4 B类地址最小值数值形式
     */
    public static final long IPV4_B_NUM_MIN = IPv4.ipv4ToLong(IPV4_B_STR_MIN);

    /**
     * Ipv4 B类地址最大值字符串形式
     */
    public static final String IPV4_B_STR_MAX = "191.255.255.255";
    /**
     * Ipv4 B类地址最大值数值形式
     */
    public static final long IPV4_B_NUM_MAX = IPv4.ipv4ToLong(IPV4_B_STR_MAX);

    /**
     * Ipv4 B类地址第一个公网网段最小值字符串形式
     */
    public static final String IPV4_B_PUBLIC_1_STR_MIN = "128.0.0.0";
    /**
     * Ipv4 B类地址第一个公网网段最小值数值形式
     */
    public static final long IPV4_B_PUBLIC_1_NUM_MIN = IPv4.ipv4ToLong(IPV4_B_PUBLIC_1_STR_MIN);

    /**
     * Ipv4 B类地址第一个公网网段最大值字符串形式
     */
    public static final String IPV4_B_PUBLIC_1_STR_MAX = "172.15.255.255";
    /**
     * Ipv4 B类地址第一个公网网段最大值数值形式
     */
    public static final long IPV4_B_PUBLIC_1_NUM_MAX = IPv4.ipv4ToLong(IPV4_B_PUBLIC_1_STR_MAX);

    /**
     * Ipv4 B类地址私网网段最小值字符串形式
     */
    public static final String IPV4_B_PRIVATE_STR_MIN = "172.16.0.0";
    /**
     * Ipv4 B类地址私网网段最小值数值形式
     */
    public static final long IPV4_B_PRIVATE_NUM_MIN = IPv4.ipv4ToLong(IPV4_B_PRIVATE_STR_MIN);

    /**
     * Ipv4 B类地址私网网段最大值字符串形式
     */
    public static final String IPV4_B_PRIVATE_STR_MAX = "172.31.255.255";
    /**
     * Ipv4 B类地址私网网段最大值数值形式
     */
    public static final long IPV4_B_PRIVATE_NUM_MAX = IPv4.ipv4ToLong(IPV4_B_PRIVATE_STR_MAX);

    /**
     * Ipv4 B类地址第二个公网网段最小值字符串形式
     */
    public static final String IPV4_B_PUBLIC_2_STR_MIN = "172.32.0.0";
    /**
     * Ipv4 B类地址第二个公网网段最小值数值形式
     */
    public static final long IPV4_B_PUBLIC_2_NUM_MIN = IPv4.ipv4ToLong(IPV4_B_PUBLIC_2_STR_MIN);

    /**
     * Ipv4 B类地址第二个公网网段最大值字符串形式
     */
    public static final String IPV4_B_PUBLIC_2_STR_MAX = "191.255.255.255";
    /**
     * Ipv4 B类地址第二个公网网段最大值数值形式
     */
    public static final long IPV4_B_PUBLIC_2_NUM_MAX = IPv4.ipv4ToLong(IPV4_B_PUBLIC_2_STR_MAX);
    /**
     * Ipv4 C类地址最小值字符串形式
     */
    public static final String IPV4_C_STR_MIN = "192.0.0.0";
    /**
     * Ipv4 C类地址最小值数值形式
     */
    public static final long IPV4_C_NUM_MIN = IPv4.ipv4ToLong(IPV4_C_STR_MIN);

    /**
     * Ipv4 C类地址最大值字符串形式
     */
    public static final String IPV4_C_STR_MAX = "223.255.255.255";
    /**
     * Ipv4 C类地址第一个公网网段最小值字符串形式
     */
    public static final String IPV4_C_PUBLIC_1_STR_MIN = "192.0.0.0";
    /**
     * Ipv4 C类地址第一个公网网段最小值数值形式
     */
    public static final long IPV4_C_PUBLIC_1_NUM_MIN = IPv4.ipv4ToLong(IPV4_C_PUBLIC_1_STR_MIN);
    /**
     * Ipv4 C类地址第一个公网网段最大值字符串形式
     */
    public static final String IPV4_C_PUBLIC_1_STR_MAX = "192.167.255.255";
    /**
     * Ipv4 C类地址第一个公网网段最大值数值形式
     */
    public static final long IPV4_C_PUBLIC_1_NUM_MAX = IPv4.ipv4ToLong(IPV4_C_PUBLIC_1_STR_MAX);
    /**
     * Ipv4 C类地址私网网段最小值字符串形式
     */
    public static final String IPV4_C_PRIVATE_STR_MIN = "192.168.0.0";
    /**
     * Ipv4 C类地址私网网段最小值数值形式
     */
    public static final long IPV4_C_PRIVATE_NUM_MIN = IPv4.ipv4ToLong(IPV4_C_PRIVATE_STR_MIN);
    /**
     * Ipv4 C类地址私网网段最大值字符串形式
     */
    public static final String IPV4_C_PRIVATE_STR_MAX = "192.168.255.255";
    /**
     * Ipv4 C类地址私网网段最大值数值形式
     */
    public static final long IPV4_C_PRIVATE_NUM_MAX = IPv4.ipv4ToLong(IPV4_C_PRIVATE_STR_MAX);
    /**
     * Ipv4 C类地址第二个公网网段最小值字符串形式
     */
    public static final String IPV4_C_PUBLIC_2_STR_MIN = "192.169.0.0";
    /**
     * Ipv4 C类地址第二个公网网段最小值数值形式
     */
    public static final long IPV4_C_PUBLIC_2_NUM_MIN = IPv4.ipv4ToLong(IPV4_C_PUBLIC_2_STR_MIN);
    /**
     * Ipv4 C类地址第二个公网网段最大值字符串形式
     */
    public static final String IPV4_C_PUBLIC_2_STR_MAX = "223.255.255.255";
    /**
     * Ipv4 C类地址第二个公网网段最大值数值形式
     */
    public static final long IPV4_C_PUBLIC_2_NUM_MAX = IPv4.ipv4ToLong(IPV4_C_PUBLIC_2_STR_MAX);
    /**
     * Ipv4 D类地址最小值字符串形式
     */
    public static final String IPV4_D_STR_MIN = "224.0.0.0";
    /**
     * Ipv4 D类地址最小值数值形式
     */
    public static final long IPV4_D_NUM_MIN = IPv4.ipv4ToLong(IPV4_D_STR_MIN);
    /**
     * Ipv4 D类地址最大值字符串形式
     */
    public static final String IPV4_D_STR_MAX = "239.255.255.255";
    /**
     * Ipv4 D类地址最大值数值形式
     */
    public static final long IPV4_D_NUM_MAX = IPv4.ipv4ToLong(IPV4_D_STR_MAX);
    /**
     * Ipv4 D类地址专用网段(用于广播)最小值字符串形式
     */
    public static final String IPV4_D_DEDICATED_STR_MIN = "224.0.0.0";
    /**
     * Ipv4 D类地址专用网段(用于广播)最小值数值形式
     */
    public static final long IPV4_D_DEDICATED_NUM_MIN = IPv4.ipv4ToLong(IPV4_D_DEDICATED_STR_MIN);
    /**
     * Ipv4 D类地址专用网段(用于广播)最大值字符串形式
     */
    public static final String IPV4_D_DEDICATED_STR_MAX = "224.0.0.255";
    /**
     * Ipv4 D类地址专用网段(用于广播)最大值数值形式
     */
    public static final long IPV4_D_DEDICATED_NUM_MAX = IPv4.ipv4ToLong(IPV4_D_DEDICATED_STR_MAX);
    /**
     * Ipv4 D类地址公用网段(用于组播)最小值字符串形式
     */
    public static final String IPV4_D_PUBLIC_STR_MIN = "224.0.1.0";
    /**
     * Ipv4 D类地址公用网段(用于组播)最小值数值形式
     */
    public static final long IPV4_D_PUBLIC_NUM_MIN = IPv4.ipv4ToLong(IPV4_D_PUBLIC_STR_MIN);
    /**
     * Ipv4 D类地址公用网段(用于组播)最大值字符串形式
     */
    public static final String IPV4_D_PUBLIC_STR_MAX = "238.255.255.255";
    /**
     * Ipv4 D类地址公用网段(用于组播)最大值数值形式
     */
    public static final long IPV4_D_PUBLIC_NUM_MAX = IPv4.ipv4ToLong(IPV4_D_PUBLIC_STR_MAX);
    /**
     * Ipv4 D类地址私用网段(用于测试)最小值字符串形式
     */
    public static final String IPV4_D_PRIVATE_STR_MIN = "239.0.0.0";
    /**
     * Ipv4 D类地址私用网段(用于测试)最小值数值形式
     */
    public static final long IPV4_D_PRIVATE_NUM_MIN = IPv4.ipv4ToLong(IPV4_D_PRIVATE_STR_MIN);
    /**
     * Ipv4 D类地址私用网段(用于测试)最大值字符串形式
     */
    public static final String IPV4_D_PRIVATE_STR_MAX = "239.255.255.255";
    /**
     * Ipv4 D类地址私用网段(用于测试)最大值数值形式
     */
    public static final long IPV4_D_PRIVATE_NUM_MAX = IPv4.ipv4ToLong(IPV4_D_PRIVATE_STR_MAX);
    /**
     * Ipv4 E类地址最小值字符串形式
     */
    public static final String IPV4_E_STR_MIN = "240.0.0.0";
    /**
     * Ipv4 E类地址最小值数值形式
     */
    public static final long IPV4_E_NUM_MIN = IPv4.ipv4ToLong(IPV4_E_STR_MIN);
    /**
     * Ipv4 E类地址最大值字符串形式
     */
    public static final String IPV4_E_STR_MAX = "255.255.255.255";
    /**
     * Ipv4 E类地址最大值数值形式
     */
    public static final long IPV4_E_NUM_MAX = IPv4.ipv4ToLong(IPV4_E_STR_MAX);
    private static volatile String localhostName;
    /**
     * Ipv4 C类地址最大值数值形式
     */
    long IPV4_C_NUM_MAX = IPv4.ipv4ToLong(IPV4_C_STR_MAX);

    /**
     * 获取主机名称，一次获取会缓存名称
     * 注意此方法会触发反向DNS解析，导致阻塞，阻塞时间取决于网络！
     *
     * @return 主机名称
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
     * 获得本机MAC地址，默认使用获取到的IPv4本地地址对应网卡
     *
     * @return 本机MAC地址
     */
    public static String getLocalMacAddress() {
        return NetKit.getMacAddress(getLocalhost());
    }

    /**
     * 获得本机物理地址
     *
     * @return 本机物理地址
     */
    public static byte[] getLocalHardwareAddress() {
        return NetKit.getHardwareAddress(getLocalhost());
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
    public static InetAddress getLocalhost() {
        return Instances.get(IPv4.class.getName(), IPv4::getLocalhostDirectly);
    }

    /**
     * 获取本机网卡IPv4地址，不使用缓存，规则如下：
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
    public static InetAddress getLocalhostDirectly() {
        return getLocalhostDirectly(false);
    }

    /**
     * 获取本机网卡IPv4地址，不使用缓存，规则如下：
     *
     * <ul>
     *     <li>必须非回路（loopback）地址、IPv4地址</li>
     *     <li>多网卡则返回第一个满足条件的地址</li>
     *     <li>如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址</li>
     * </ul>
     *
     * <p>
     * 此方法不会抛出异常，获取失败将返回{@code null}
     * <p>
     *
     * @param includeSiteLocal 是否包含局域网地址，如10.0.0.0 ~ 10.255.255.255、172.16.0.0 ~ 172.31.255.255、192.168.0.0 ~ 192.168.255.255
     * @return 本机网卡IP地址，获取失败返回{@code null}
     */
    public static InetAddress getLocalhostDirectly(final boolean includeSiteLocal) {
        final LinkedHashSet<InetAddress> localAddressList = NetKit.localAddressList(address ->
                // 需为IPV4地址
                address instanceof Inet4Address
                        // 非loopback地址，指127.*.*.*的地址
                        && !address.isLoopbackAddress()
                        // 非地区本地地址，指：
                        // 10.0.0.0 ~ 10.255.255.255
                        // 172.16.0.0 ~ 172.31.255.255
                        // 192.168.0.0 ~ 192.168.255.255
                        && (includeSiteLocal || !address.isSiteLocalAddress())
                        // 非链路本地地址：169.254.0.0/16
                        && !address.isLinkLocalAddress()
        );

        if (CollKit.isNotEmpty(localAddressList)) {
            // 如果存在多网卡，返回首个地址
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
            host = Protocol.HOST_IPV4;
        }

        final String targetHost;
        final int port;
        final int index = host.indexOf(Symbol.COLON);
        if (index != -1) {
            // host:port形式
            targetHost = host.substring(0, index);
            port = Integer.parseInt(host.substring(index + 1));
        } else {
            targetHost = host;
            port = defaultPort;
        }

        return new InetSocketAddress(targetHost, port);
    }

    /**
     * 根据 ip地址 和 掩码地址 获得 CIDR格式字符串
     *
     * @param ip   IP地址，点分十进制，如：xxx.xxx.xxx.xxx
     * @param mask 掩码地址，点分十进制，如：255.255.255.0
     * @return 返回 {@literal xxx.xxx.xxx.xxx/掩码位} 的格式，例如：192.168.1.101/24
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
            final String[] range = CharsBacker.splitToArray(ipRange, Symbol.MINUS);
            return list(range[0], range[1]);
        } else if (ipRange.contains(Symbol.SLASH)) {
            // X.X.X.X/X
            final String[] param = CharsBacker.splitToArray(ipRange, Symbol.SLASH);
            return list(param[0], Integer.parseInt(param[1]), isAll);
        } else {
            return ListKit.of(ipRange);
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
            return ListKit.zero();
        }

        final long startIp = getBeginIpLong(ip, maskBit);
        final long endIp = getEndIpLong(ip, maskBit);
        if (isAll) {
            return list(startIp, endIp);
        }

        // 可用地址: 排除开始和结束的地址
        if (startIp + 1 > endIp - 1) {
            return ListKit.zero();
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
        final Matcher matcher = Pattern.IPV4_PATTERN.matcher(strIp);
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
    public static String getBeginIpString(final String ip, final int maskBit) {
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
    public static String getEndIpString(final String ip, final int maskBit) {
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
        return getBeginIpLong(ip, maskBit) + (Protocol.IPV4_NUM_MAX & ~MaskBit.getMaskIpLong(maskBit));
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
     * @param maskBit 掩码位，取值范围：({@link #IPV4_MASK_BIT_VALID_MIN}, {@link #IPV4_MASK_BIT_MAX}]
     * @param isAll   true:全量地址，false:可用地址
     * @return 子网内地址总数
     */
    public static int countByMaskBit(final int maskBit, final boolean isAll) {
        Assert.isTrue(maskBit > IPV4_MASK_BIT_VALID_MIN && maskBit <= IPV4_MASK_BIT_MAX,
                "Not support mask bit: {}", maskBit);
        //如果掩码位等于32，则可用地址为0
        if (maskBit == IPV4_MASK_BIT_MAX && !isAll) {
            return 0;
        }

        final int count = 1 << (IPV4_MASK_BIT_MAX - maskBit);
        return isAll ? count : count - 2;
    }

    /**
     * 根据 掩码位 获取 掩码地址
     *
     * @param maskBit 掩码位，如：24，取值范围：[{@link #IPV4_MASK_BIT_VALID_MIN}, {@link #IPV4_MASK_BIT_MAX}]
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
     * @param maskBit 掩码位，有效范围：[{@link #IPV4_MASK_BIT_VALID_MIN}, {@link #IPV4_MASK_BIT_MAX}]
     * @return true：掩码位合法；false：掩码位不合法
     */
    public static boolean isMaskBitValid(final int maskBit) {
        return maskBit >= IPV4_MASK_BIT_VALID_MIN && maskBit <= IPV4_MASK_BIT_MAX;
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
        return isInnerIP(ipv4ToLong(ipAddress));
    }

    /**
     * 是否为内网地址
     *
     * @param ipNum IP地址数值形式
     * @return 是否为内网IP
     * @see #isInnerIP(String)
     */
    public static boolean isInnerIP(final long ipNum) {
        return isBetween(ipNum, IPV4_A_PRIVATE_NUM_MIN, IPV4_A_PRIVATE_NUM_MAX)
                || isBetween(ipNum, IPV4_B_PRIVATE_NUM_MIN, IPV4_B_PRIVATE_NUM_MAX)
                || isBetween(ipNum, IPV4_C_PRIVATE_NUM_MIN, IPV4_C_PRIVATE_NUM_MAX)
                || LOCAL_IP_NUM == ipNum;
    }

    /**
     * 是否为公网地址
     * <p>
     * 公网IP：
     * <pre>
     * A类 1.0.0.0-9.255.255.255，11.0.0.0-126.255.255.255
     * B类 128.0.0.0-172.15.255.255，172.32.0.0-191.255.255.255
     * C类 192.0.0.0-192.167.255.255，192.169.0.0-223.255.255.255
     * </pre>
     *
     * @param ipAddress IP地址，点分十进制
     * @return 是否为公网IP
     */
    public static boolean isPublicIP(final String ipAddress) {
        return isPublicIP(ipv4ToLong(ipAddress));
    }

    /**
     * 是否为公网地址
     *
     * @param ipNum IP地址数值形式
     * @return 是否为公网IP
     * @see #isPublicIP(String)
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
     * 获取ip(Long类型)指定部分的十进制值，即，{@literal X.X.X.X }形式中每个部分的值
     * <p>例如，ip为{@literal 0xC0A802FA}：
     * <ul>
     * <li>第1部分的值为：{@literal 0xC0}，十进制值为：192</li>
     * <li>第2部分的值为：{@literal 0xA8}，十进制值为：168</li>
     * <li>第3部分的值为：{@literal 0x02}，十进制值为：2</li>
     * <li>第4部分的值为：{@literal 0xFA}，十进制值为：250</li>
     * </ul>
     *
     * @param ip       ip地址，Long类型
     * @param position 指定位置，取值范围：[1,4]
     * @return ip地址指定部分的十进制值
     */
    public static int getPartOfIp(final long ip, final int position) {
        switch (position) {
            case 1:
                return (int) (ip >> 24) & 0xFF;
            case 2:
                return (int) (ip >> 16) & 0xFF;
            case 3:
                return (int) (ip >> 8) & 0xFF;
            case 4:
                return (int) ip & 0xFF;
            default:
                throw new IllegalArgumentException("Illegal position of ip Long: " + position);
        }
    }

    /**
     * 检测指定 IP 地址是否匹配通配符 wildcard
     *
     * @param wildcard  通配符，如 192.168.*.1
     * @param ipAddress 待检测的 IP 地址
     * @return 是否匹配
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
            return Protocol.IPV4_NUM_MAX & addr;
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
     * 校验 掩码位数，合法范围为：[{@link #IPV4_MASK_BIT_VALID_MIN}, {@link #IPV4_MASK_BIT_MAX}]，不合法则抛出异常
     *
     * @param maskBit 掩码位数
     */
    private static void assertMaskBitValid(final int maskBit) {
        Assert.isTrue(isMaskBitValid(maskBit), "Invalid maskBit：{}", maskBit);
    }

}
