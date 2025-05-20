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

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.Objects;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.accord.Connection;
import org.miaixz.bus.http.accord.ConnectionSuite;
import org.miaixz.bus.http.secure.Authenticator;
import org.miaixz.bus.http.secure.CertificatePinner;

/**
 * 到源服务器的连接规范
 * <p>
 * 定义了连接源服务器的配置，包括主机名、端口、代理、协议和安全设置。 共享相同 {@code Address} 的 HTTP 请求可能复用相同的 {@link Connection}。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Address {

    /**
     * 服务器的 URL
     */
    final UnoUrl url;
    /**
     * DNS 解析服务
     */
    final DnsX dns;
    /**
     * 套接字工厂
     */
    final SocketFactory socketFactory;
    /**
     * 代理身份验证器
     */
    final Authenticator proxyAuthenticator;
    /**
     * 支持的协议列表
     */
    final List<Protocol> protocols;
    /**
     * 支持的连接套件列表
     */
    final List<ConnectionSuite> connectionSuites;
    /**
     * 代理选择器
     */
    final ProxySelector proxySelector;
    /**
     * 显式指定的代理
     */
    final Proxy proxy;
    /**
     * SSL 套接字工厂
     */
    final SSLSocketFactory sslSocketFactory;
    /**
     *
     * 主机名验证器
     */
    final HostnameVerifier hostnameVerifier;
    /**
     * 证书固定器
     */
    final CertificatePinner certificatePinner;

    /**
     * 构造函数，初始化 Address 实例
     *
     * @param uriHost            主机名
     * @param uriPort            端口
     * @param dns                DNS 解析服务
     * @param socketFactory      套接字工厂
     * @param sslSocketFactory   SSL 套接字工厂
     * @param hostnameVerifier   主机名验证器
     * @param certificatePinner  证书固定器
     * @param proxyAuthenticator 代理身份验证器
     * @param proxy              显式代理
     * @param protocols          支持的协议列表
     * @param connectionSuites   支持的连接套件列表
     * @param proxySelector      代理选择器
     * @throws NullPointerException 如果必需参数为 null
     */
    public Address(String uriHost, int uriPort, DnsX dns, SocketFactory socketFactory,
            SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier, CertificatePinner certificatePinner,
            Authenticator proxyAuthenticator, Proxy proxy, List<Protocol> protocols,
            List<ConnectionSuite> connectionSuites, ProxySelector proxySelector) {
        this.url = new UnoUrl.Builder().scheme(null != sslSocketFactory ? Protocol.HTTPS.name : Protocol.HTTP.name)
                .host(uriHost).port(uriPort).build();

        if (null == dns)
            throw new NullPointerException("dns == null");
        this.dns = dns;

        if (null == socketFactory)
            throw new NullPointerException("socketFactory == null");
        this.socketFactory = socketFactory;

        if (null == proxyAuthenticator) {
            throw new NullPointerException("proxyAuthenticator == null");
        }
        this.proxyAuthenticator = proxyAuthenticator;

        if (null == protocols)
            throw new NullPointerException("protocols == null");
        this.protocols = Builder.immutableList(protocols);

        if (null == connectionSuites)
            throw new NullPointerException("connectionSpecs == null");
        this.connectionSuites = Builder.immutableList(connectionSuites);

        if (null == proxySelector)
            throw new NullPointerException("proxySelector == null");
        this.proxySelector = proxySelector;

        this.proxy = proxy;
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
        this.certificatePinner = certificatePinner;
    }

    /**
     * 获取服务器 URL
     *
     * @return 服务器的 URL（路径、查询和片段为空）
     */
    public UnoUrl url() {
        return url;
    }

    /**
     * 获取 DNS 解析服务
     *
     * @return DNS 解析服务
     */
    public DnsX dns() {
        return dns;
    }

    /**
     * 获取套接字工厂
     *
     * @return 套接字工厂
     */
    public SocketFactory socketFactory() {
        return socketFactory;
    }

    /**
     * 获取代理身份验证器
     *
     * @return 代理身份验证器
     */
    public Authenticator proxyAuthenticator() {
        return proxyAuthenticator;
    }

    /**
     * 获取支持的协议列表
     *
     * @return 不可修改的协议列表（至少包含 HTTP/1.1）
     */
    public List<Protocol> protocols() {
        return protocols;
    }

    /**
     * 获取支持的连接套件列表
     *
     * @return 不可修改的连接套件列表
     */
    public List<ConnectionSuite> connectionSpecs() {
        return connectionSuites;
    }

    /**
     * 获取代理选择器
     *
     * @return 代理选择器
     */
    public ProxySelector proxySelector() {
        return proxySelector;
    }

    /**
     * 获取显式指定的代理
     *
     * @return 显式代理（可能为 null）
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * 获取 SSL 套接字工厂
     *
     * @return SSL 套接字工厂（非 HTTPS 时为 null）
     */
    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * 获取主机名验证器
     *
     * @return 主机名验证器（非 HTTPS 时为 null）
     */
    public HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * 获取证书固定器
     *
     * @return 证书固定器（非 HTTPS 时为 null）
     */
    public CertificatePinner certificatePinner() {
        return certificatePinner;
    }

    /**
     * 比较两个 Address 对象是否相等
     *
     * @param other 另一个对象
     * @return true 如果两个 Address 对象相等
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Address && url.equals(((Address) other).url) && equalsNonHost((Address) other);
    }

    /**
     * 计算 Address 对象的哈希码
     *
     * @return 哈希码值
     */
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

    /**
     * 比较非主机相关的属性是否相等
     *
     * @param that 另一个 Address 对象
     * @return true 如果非主机属性相等
     */
    boolean equalsNonHost(Address that) {
        return this.dns.equals(that.dns) && this.proxyAuthenticator.equals(that.proxyAuthenticator)
                && this.protocols.equals(that.protocols) && this.connectionSuites.equals(that.connectionSuites)
                && this.proxySelector.equals(that.proxySelector) && Objects.equals(this.proxy, that.proxy)
                && Objects.equals(this.sslSocketFactory, that.sslSocketFactory)
                && Objects.equals(this.hostnameVerifier, that.hostnameVerifier)
                && Objects.equals(this.certificatePinner, that.certificatePinner)
                && this.url().port() == that.url().port();
    }

    /**
     * 返回 Address 的字符串表示
     *
     * @return 包含主机名、端口和代理信息的字符串
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append("Address{").append(url.host()).append(Symbol.COLON)
                .append(url.port());

        if (null != proxy) {
            result.append(", proxy=").append(proxy);
        } else {
            result.append(", proxySelector=").append(proxySelector);
        }

        result.append(Symbol.BRACE_RIGHT);
        return result.toString();
    }

}