/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * The concrete route used by a connection to reach an abstract origin server. When creating a
 * connection the client has many options:
 *
 * <ul>
 * <li><strong>HTTP proxy:</strong> a proxy server may be explicitly configured for the client.
 * Otherwise the {@linkplain java.net.ProxySelector proxy selector} is used. It may return
 * multiple proxies to attempt.
 * <li><strong>IP address:</strong> whether connecting directly to an origin server or a proxy,
 * opening a socket requires an IP address. The DNS server may return multiple IP addresses
 * to attempt.
 * </ul>
 *
 * <p>Each route is a specific selection of these options.
 *
 * @author Kimi Liu
 * @version 5.0.1
 * @since JDK 1.8+
 */
public final class Route {

    final Address address;
    final Proxy proxy;
    final InetSocketAddress inetSocketAddress;

    public Route(Address address, java.net.Proxy proxy, InetSocketAddress inetSocketAddress) {
        if (address == null) {
            throw new NullPointerException("address == null");
        }
        if (proxy == null) {
            throw new NullPointerException("proxy == null");
        }
        if (inetSocketAddress == null) {
            throw new NullPointerException("inetSocketAddress == null");
        }
        this.address = address;
        this.proxy = proxy;
        this.inetSocketAddress = inetSocketAddress;
    }

    public Address address() {
        return address;
    }

    public Proxy proxy() {
        return proxy;
    }

    public InetSocketAddress socketAddress() {
        return inetSocketAddress;
    }

    public boolean requiresTunnel() {
        return address.sslSocketFactory != null && proxy.type() == Proxy.Type.HTTP;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Route
                && ((Route) other).address.equals(address)
                && ((Route) other).proxy.equals(proxy)
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
