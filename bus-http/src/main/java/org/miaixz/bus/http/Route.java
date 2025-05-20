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
package org.miaixz.bus.http;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * 表示连接到源服务器的具体路由。
 * <p>
 * 路由封装了 HTTP 客户端连接时选择的特定配置，包括目标地址、代理和套接字地址。 每个路由是客户端连接选项（如代理选择、TLS 配置）的具体实例，实例是不可变的。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Route {

    /**
     * 目标服务器的地址配置
     */
    final Address address;
    /**
     * 使用的代理
     */
    final Proxy proxy;
    /**
     * 目标套接字地址（IP 和端口）
     */
    final InetSocketAddress inetSocketAddress;

    /**
     * 构造函数，初始化 Route 实例。
     *
     * @param address           目标服务器的地址配置
     * @param proxy             使用的代理
     * @param inetSocketAddress 目标套接字地址
     * @throws NullPointerException 如果 address、proxy 或 inetSocketAddress 为 null
     */
    public Route(Address address, Proxy proxy, InetSocketAddress inetSocketAddress) {
        if (null == address) {
            throw new NullPointerException("address == null");
        }
        if (null == proxy) {
            throw new NullPointerException("proxy == null");
        }
        if (null == inetSocketAddress) {
            throw new NullPointerException("inetSocketAddress == null");
        }
        this.address = address;
        this.proxy = proxy;
        this.inetSocketAddress = inetSocketAddress;
    }

    /**
     * 获取目标服务器的地址配置。
     *
     * @return Address 对象
     */
    public Address address() {
        return address;
    }

    /**
     * 获取使用的代理。
     * <p>
     * <strong>警告：</strong> 如果 {@link Address#proxy} 为 null，此代理可能与地址配置中的代理不同， 因为地址未指定代理时会使用代理选择器。
     * </p>
     *
     * @return Proxy 对象
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * 获取目标套接字地址。
     *
     * @return InetSocketAddress 对象，表示目标 IP 和端口
     */
    public InetSocketAddress socketAddress() {
        return inetSocketAddress;
    }

    /**
     * 检查路由是否需要通过 HTTP 代理进行 HTTPS 隧道传输。
     * <p>
     * 参见 <a href="http://www.ietf.org/rfc/rfc2817.txt">RFC 2817, Section 5.2</a>。
     * </p>
     *
     * @return true 如果路由使用 HTTP 代理进行 HTTPS 隧道传输
     */
    public boolean requiresTunnel() {
        return address.sslSocketFactory != null && proxy.type() == Proxy.Type.HTTP;
    }

    /**
     * 比较两个 Route 对象是否相等。
     * <p>
     * 两个路由相等需满足：地址、代理和套接字地址均相同。
     * </p>
     *
     * @param other 另一个对象
     * @return true 如果两个 Route 对象相等
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Route && ((Route) other).address.equals(address) && ((Route) other).proxy.equals(proxy)
                && ((Route) other).inetSocketAddress.equals(inetSocketAddress);
    }

    /**
     * 计算 Route 对象的哈希码。
     *
     * @return 哈希码值
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + address.hashCode();
        result = 31 * result + proxy.hashCode();
        result = 31 * result + inetSocketAddress.hashCode();
        return result;
    }

    /**
     * 返回路由的字符串表示。
     *
     * @return 包含套接字地址的字符串
     */
    @Override
    public String toString() {
        return "Route{" + inetSocketAddress + "}";
    }

}