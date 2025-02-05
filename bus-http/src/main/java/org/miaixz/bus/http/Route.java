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
 * 连接用于到达抽象源服务器的具体路由 在创建连接时，客户机有许多选项 每个路由都是这些选项的特定选择
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Route {

    final Address address;
    final Proxy proxy;
    final InetSocketAddress inetSocketAddress;

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

    public Address address() {
        return address;
    }

    /**
     * Returns the {@link Proxy} of this route.
     *
     * <strong>Warning:</strong> This may disagree with {@link Address#proxy} when it is null. When the address's proxy
     * is null, the proxy selector is used.
     */
    public Proxy proxy() {
        return proxy;
    }

    public InetSocketAddress socketAddress() {
        return inetSocketAddress;
    }

    /**
     * Returns true if this route tunnels HTTPS through an HTTP proxy. See
     * <a href="http://www.ietf.org/rfc/rfc2817.txt">RFC 2817, Section 5.2</a>.
     */
    public boolean requiresTunnel() {
        return address.sslSocketFactory != null && proxy.type() == Proxy.Type.HTTP;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Route && ((Route) other).address.equals(address) && ((Route) other).proxy.equals(proxy)
                && ((Route) other).inetSocketAddress.equals(inetSocketAddress);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + address.hashCode();
        result = 31 * result + proxy.hashCode();
        result = 31 * result + inetSocketAddress.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Route{" + inetSocketAddress + "}";
    }

}
