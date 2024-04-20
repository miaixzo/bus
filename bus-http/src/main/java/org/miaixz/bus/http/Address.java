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
package org.miaixz.bus.http;

import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.accord.Connection;
import org.miaixz.bus.http.accord.ConnectionSuite;
import org.miaixz.bus.http.secure.Authenticator;
import org.miaixz.bus.http.secure.CertificatePinner;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.Objects;

/**
 * 到源服务器的连接的规范。对于简单的连接，这是服务器的主机名和端口。如果显式请求了
 * 代理(或显式请求了{@linkplain Proxy#NO_PROXY no proxy})则还包括该代理信息
 * 对于安全连接，该地址还包括SSL套接字工厂、主机名验证器和证书
 * 共享相同的{@code Address}的HTTP请求也可能共享相同的{@link Connection}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Address {

    /**
     * 服务器主机名和端口的URL
     */
    final UnoUrl url;
    /**
     * 用于解析主机名的IP地址的服务
     */
    final DnsX dns;
    /**
     * 用于新连接的套接字工厂
     */
    final SocketFactory socketFactory;
    /**
     * 客户端的代理身份验证器
     */
    final Authenticator proxyAuthenticator;
    /**
     * 返回客户端支持的协议,包含最低限度{@link Protocol#HTTP_1_1}的非空列表
     */
    final List<Protocol> protocols;
    final List<ConnectionSuite> connectionSuites;
    /**
     * 代理选择器。仅在代理为空时使用。如果无法访问该选择器的代理，则将尝试直接连接
     */
    final ProxySelector proxySelector;
    /**
     * 明确指定的HTTP代理，或null来委托给{@link ProxySelector 代理选择器}
     */
    final Proxy proxy;
    /**
     * SSL套接字工厂
     */
    final SSLSocketFactory sslSocketFactory;
    /**
     * 主机名验证器
     */
    final HostnameVerifier hostnameVerifier;
    /**
     * 此地址的证书Pinner
     */
    final CertificatePinner certificatePinner;

    public Address(String uriHost, int uriPort, DnsX dns, SocketFactory socketFactory,
                   SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier,
                   CertificatePinner certificatePinner, Authenticator proxyAuthenticator,
                   Proxy proxy, List<Protocol> protocols, List<ConnectionSuite> connectionSuites,
                   ProxySelector proxySelector) {
        this.url = new UnoUrl.Builder()
                .scheme(null != sslSocketFactory ? Http.HTTPS : Http.HTTP)
                .host(uriHost)
                .port(uriPort)
                .build();

        if (null == dns) throw new NullPointerException("dns == null");
        this.dns = dns;

        if (null == socketFactory) throw new NullPointerException("socketFactory == null");
        this.socketFactory = socketFactory;

        if (null == proxyAuthenticator) {
            throw new NullPointerException("proxyAuthenticator == null");
        }
        this.proxyAuthenticator = proxyAuthenticator;

        if (null == protocols) throw new NullPointerException("protocols == null");
        this.protocols = Builder.immutableList(protocols);

        if (null == connectionSuites) throw new NullPointerException("connectionSpecs == null");
        this.connectionSuites = Builder.immutableList(connectionSuites);

        if (null == proxySelector) throw new NullPointerException("proxySelector == null");
        this.proxySelector = proxySelector;

        this.proxy = proxy;
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
        this.certificatePinner = certificatePinner;
    }

    /**
     * Returns a URL with the hostname and port of the origin server. The path, query, and fragment of
     * this URL are always empty, since they are not significant for planning a route.
     */
    public UnoUrl url() {
        return url;
    }

    /**
     * Returns the service that will be used to resolve IP addresses for hostnames.
     */
    public DnsX dns() {
        return dns;
    }

    /**
     * Returns the socket factory for new connections.
     */
    public SocketFactory socketFactory() {
        return socketFactory;
    }

    /**
     * Returns the client's proxy authenticator.
     */
    public Authenticator proxyAuthenticator() {
        return proxyAuthenticator;
    }

    /**
     * Returns the protocols the client supports. This method always returns a non-null list that
     * contains minimally {@link Protocol#HTTP_1_1}.
     */
    public List<Protocol> protocols() {
        return protocols;
    }

    public List<ConnectionSuite> connectionSpecs() {
        return connectionSuites;
    }

    /**
     * Returns this address's proxy selector. Only used if the proxy is null. If none of this
     * selector's proxies are reachable, a direct connection will be attempted.
     */
    public ProxySelector proxySelector() {
        return proxySelector;
    }

    /**
     * Returns this address's explicitly-specified HTTP proxy, or null to delegate to the {@linkplain
     * #proxySelector proxy selector}.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * Returns the SSL socket factory, or null if this is not an HTTPS address.
     */
    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * Returns the hostname verifier, or null if this is not an HTTPS address.
     */
    public HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Returns this address's certificate pinner, or null if this is not an HTTPS address.
     */
    public CertificatePinner certificatePinner() {
        return certificatePinner;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Address
                && url.equals(((Address) other).url)
                && equalsNonHost((Address) other);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + url.hashCode();
        result = 31 * result + dns.hashCode();
        result = 31 * result + proxyAuthenticator.hashCode();
        result = 31 * result + protocols.hashCode();
        result = 31 * result + connectionSuites.hashCode();
        result = 31 * result + proxySelector.hashCode();
        result = 31 * result + Objects.hashCode(proxy);
        result = 31 * result + Objects.hashCode(sslSocketFactory);
        result = 31 * result + Objects.hashCode(hostnameVerifier);
        result = 31 * result + Objects.hashCode(certificatePinner);
        return result;
    }

    boolean equalsNonHost(Address that) {
        return this.dns.equals(that.dns)
                && this.proxyAuthenticator.equals(that.proxyAuthenticator)
                && this.protocols.equals(that.protocols)
                && this.connectionSuites.equals(that.connectionSuites)
                && this.proxySelector.equals(that.proxySelector)
                && Objects.equals(this.proxy, that.proxy)
                && Objects.equals(this.sslSocketFactory, that.sslSocketFactory)
                && Objects.equals(this.hostnameVerifier, that.hostnameVerifier)
                && Objects.equals(this.certificatePinner, that.certificatePinner)
                && this.url().port() == that.url().port();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder()
                .append("Address{")
                .append(url.host()).append(Symbol.COLON).append(url.port());

        if (null != proxy) {
            result.append(", proxy=").append(proxy);
        } else {
            result.append(", proxySelector=").append(proxySelector);
        }

        result.append(Symbol.BRACE_RIGHT);
        return result.toString();
    }

}
