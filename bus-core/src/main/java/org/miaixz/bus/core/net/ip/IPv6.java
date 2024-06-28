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

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.NetKit;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;

/**
 * IPv6工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IPv6 {

    private static volatile String localhostName;

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
            return InetAddress.getByAddress(
                    bigInteger.toByteArray()).toString().substring(1);
        } catch (final UnknownHostException ignore) {
            return null;
        }
    }

    /**
     * 获得本机的IPv6地址列表
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表 {@link LinkedHashSet}
     */
    public static LinkedHashSet<String> localIps() {
        final LinkedHashSet<InetAddress> localAddressList = NetKit.localAddressList(
                t -> t instanceof Inet6Address);

        return NetKit.toIpList(localAddressList);
    }

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
     * 获得本机MAC地址，默认使用获取到的IPv6本地地址对应网卡
     *
     * @return 本机MAC地址
     */
    public static String getLocalMacAddress() {
        return NetKit.getMacAddress(getLocalhost());
    }

    /**
     * 获得本机物理地址（IPv6网卡）
     *
     * @return 本机物理地址
     */
    public static byte[] getLocalHardwareAddress() {
        return NetKit.getHardwareAddress(getLocalhost());
    }

    /**
     * 获取本机网卡IPv6地址，规则如下：
     *
     * <ul>
     *     <li>必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv6地址</li>
     *     <li>多网卡则返回第一个满足条件的地址</li>
     *     <li>如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址</li>
     * </ul>
     *
     * <p>
     * 此方法不会抛出异常，获取失败将返回{@code null}
     * </p>
     *
     * @return 本机网卡IP地址，获取失败返回{@code null}
     */
    public static InetAddress getLocalhost() {
        return Instances.get(IPv6.class.getName(), IPv6::getLocalhostDirectly);
    }

    /**
     * 获取本机网卡IPv6地址，不使用缓存，规则如下：
     *
     * <ul>
     *     <li>必须非回路（loopback）地址、非局域网地址（siteLocal）、IPv6地址</li>
     *     <li>多网卡则返回第一个满足条件的地址</li>
     *     <li>如果无满足要求的地址，调用 {@link InetAddress#getLocalHost()} 获取地址</li>
     * </ul>
     *
     * <p>
     * 此方法不会抛出异常，获取失败将返回{@code null}
     * </p>
     *
     * @return 本机网卡IP地址，获取失败返回{@code null}
     */
    public static InetAddress getLocalhostDirectly() {
        final LinkedHashSet<InetAddress> localAddressList = NetKit.localAddressList(address ->
                // 需为IPV6地址
                address instanceof Inet6Address
                        // 非loopback地址，::1
                        && !address.isLoopbackAddress()
                        // 非地区本地地址，fec0::/10
                        && !address.isSiteLocalAddress()
                        // 非链路本地地址，fe80::/10
                        && !address.isLinkLocalAddress()
        );

        if (CollKit.isNotEmpty(localAddressList)) {
            // 如果存在多网卡，返回首个地址
            return CollKit.getFirst(localAddressList);
        }

        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            if (localHost instanceof Inet6Address) {
                return localHost;
            }
        } catch (final UnknownHostException e) {
            // ignore
        }

        return null;
    }

    /**
     * 规范IPv6地址，转换scope名称为scope data，如：
     * <pre>
     *     fe80:0:0:0:894:aeec:f37d:23e1%en0
     *                   |
     *     fe80:0:0:0:894:aeec:f37d:23e1%5
     * </pre>
     * 地址后的“%5” 叫做 scope data.
     *
     * @param address IPv6地址
     * @return 规范之后的IPv6地址，使用scope data
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
