/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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

import org.aoju.bus.http.internal.Internal;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;

/**
 * A specification for a connection to an origin server. For simple connections, this is the
 * server's hostname and port. If an explicit proxy is requested (or {@linkplain Proxy#NO_PROXY no
 * proxy} is explicitly requested), this also includes that proxy information. For secure
 * connections the address also includes the SSL socket factory, hostname verifier, and certificate
 * pinner.
 *
 * <p>HTTP requests that share the same {@code Address} may also share the same {@link Connection}.
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public final class Address {

    final HttpUrl url;
    final Dns dns;
    final SocketFactory socketFactory;
    final Authenticator proxyAuthenticator;
    final List<Protocol> protocols;
    final List<ConnectionSpec> connectionSpecs;
    final ProxySelector proxySelector;
    final Proxy proxy;
    final SSLSocketFactory sslSocketFactory;
    final HostnameVerifier hostnameVerifier;
    final CertificatePinner certificatePinner;

    public Address(String uriHost, int uriPort, Dns dns, SocketFactory socketFactory,
                   SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier,
                   CertificatePinner certificatePinner, Authenticator proxyAuthenticator,
                   Proxy proxy, List<Protocol> protocols, List<ConnectionSpec> connectionSpecs,
                   ProxySelector proxySelector) {
        this.url = new HttpUrl.Builder()
                .scheme(sslSocketFactory != null ? "https" : "http")
                .host(uriHost)
                .port(uriPort)
                .build();

        if (dns == null) throw new NullPointerException("dns == null");
        this.dns = dns;

        if (socketFactory == null) throw new NullPointerException("socketFactory == null");
        this.socketFactory = socketFactory;

        if (proxyAuthenticator == null) {
            throw new NullPointerException("proxyAuthenticator == null");
        }
        this.proxyAuthenticator = proxyAuthenticator;

        if (protocols == null) throw new NullPointerException("protocols == null");
        this.protocols = Internal.immutableList(protocols);

        if (connectionSpecs == null) throw new NullPointerException("connectionSpecs == null");
        this.connectionSpecs = Internal.immutableList(connectionSpecs);

        if (proxySelector == null) throw new NullPointerException("proxySelector == null");
        this.proxySelector = proxySelector;

        this.proxy = proxy;
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
        this.certificatePinner = certificatePinner;
    }

    /**
     * @return a URL with the hostname and port of the origin server. The path, query, and fragment of
     * this URL are always empty, since they are not significant for planning a route.
     */
    public HttpUrl url() {
        return url;
    }

    /**
     * @return the service that will be used to resolve IP addresses for hostnames.
     */
    public Dns dns() {
        return dns;
    }

    /**
     * @return the socket factory for new connections.
     */
    public SocketFactory socketFactory() {
        return socketFactory;
    }

    /**
     * @return the client's proxy authenticator.
     */
    public Authenticator proxyAuthenticator() {
        return proxyAuthenticator;
    }

    /**
     * @return the protocols the client supports. This method always returns a non-null list that
     * contains minimally {@link Protocol#HTTP_1_1}.
     */
    public List<Protocol> protocols() {
        return protocols;
    }

    public List<ConnectionSpec> connectionSpecs() {
        return connectionSpecs;
    }

    /**
     * @return this address's proxy selector. Only used if the proxy is null. If none of this
     * selector's proxies are reachable, a direct connection will be attempted.
     */
    public ProxySelector proxySelector() {
        return proxySelector;
    }

    /**
     * @return this address's explicitly-specified HTTP proxy, or null to delegate to the {@linkplain
     * #proxySelector proxy selector}.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * @return the SSL socket factory, or null if this is not an HTTPS address.
     */
    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * @return the hostname verifier, or null if this is not an HTTPS address.
     */
    public HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * @return this address's certificate pinner, or null if this is not an HTTPS address.
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
        result = 31 * result + connectionSpecs.hashCode();
        result = 31 * result + proxySelector.hashCode();
        result = 31 * result + (proxy != null ? proxy.hashCode() : 0);
        result = 31 * result + (sslSocketFactory != null ? sslSocketFactory.hashCode() : 0);
        result = 31 * result + (hostnameVerifier != null ? hostnameVerifier.hashCode() : 0);
        result = 31 * result + (certificatePinner != null ? certificatePinner.hashCode() : 0);
        return result;
    }

    boolean equalsNonHost(Address that) {
        return this.dns.equals(that.dns)
                && this.proxyAuthenticator.equals(that.proxyAuthenticator)
                && this.protocols.equals(that.protocols)
                && this.connectionSpecs.equals(that.connectionSpecs)
                && this.proxySelector.equals(that.proxySelector)
                && Internal.equal(this.proxy, that.proxy)
                && Internal.equal(this.sslSocketFactory, that.sslSocketFactory)
                && Internal.equal(this.hostnameVerifier, that.hostnameVerifier)
                && Internal.equal(this.certificatePinner, that.certificatePinner)
                && this.url().port() == that.url().port();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder()
                .append("Address{")
                .append(url.host()).append(":").append(url.port());

        if (proxy != null) {
            result.append(", proxy=").append(proxy);
        } else {
            result.append(", proxySelector=").append(proxySelector);
        }

        result.append("}");
        return result.toString();
    }
}
