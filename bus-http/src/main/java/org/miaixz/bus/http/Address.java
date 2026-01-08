/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
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
 * A specification for a connection to an origin server. For an HTTP client, this is the server that terminates the HTTP
 * request.
 *
 * <p>
 * This class holds the configuration for a connection, including the server's hostname, port, proxy, and security
 * settings. HTTP requests that share the same {@code Address} may also share the same underlying {@link Connection}.
 *
 * @author Kimi Liu
 * @see Connection
 * @see UnoUrl
 * @since Java 17+
 */
public final class Address {

    /**
     * The URL of the origin server. The path, query, and fragment of this URL are always empty. This is never null.
     */
    final UnoUrl url;

    /**
     * The DNS service for resolving hostnames. This is never null.
     */
    final DnsX dns;

    /**
     * The socket factory for creating connections. This is never null.
     */
    final SocketFactory socketFactory;

    /**
     * The authenticator for proxy servers. This is never null.
     */
    final Authenticator proxyAuthenticator;

    /**
     * The list of supported application-layer protocols, such as {@code http/1.1} and {@code h2}. This is never null.
     */
    final List<Protocol> protocols;

    /**
     * The list of connection suites to use for TLS connections. This is never null.
     */
    final List<ConnectionSuite> connectionSuites;

    /**
     * The proxy selector for choosing a proxy server. This is never null.
     */
    final ProxySelector proxySelector;

    /**
     * The explicitly specified proxy, or null for no proxy.
     */
    final Proxy proxy;

    /**
     * The SSL socket factory for HTTPS connections. This is null for non-HTTPS connections.
     */
    final SSLSocketFactory sslSocketFactory;

    /**
     * The verifier for hostnames in SSL certificates. This is null for non-HTTPS connections.
     */
    final HostnameVerifier hostnameVerifier;

    /**
     * The certificate pinner for verifying server certificates. This is null for non-HTTPS connections.
     */
    final CertificatePinner certificatePinner;

    /**
     * Constructs a new Address.
     *
     * @param uriHost            the hostname of the origin server.
     * @param uriPort            the port of the origin server.
     * @param dns                the DNS service.
     * @param socketFactory      the socket factory.
     * @param sslSocketFactory   the SSL socket factory, or null for non-HTTPS connections.
     * @param hostnameVerifier   the hostname verifier, or null for non-HTTPS connections.
     * @param certificatePinner  the certificate pinner, or null for non-HTTPS connections.
     * @param proxyAuthenticator the proxy authenticator.
     * @param proxy              the explicit proxy, or null to use the proxy selector.
     * @param protocols          the list of supported protocols.
     * @param connectionSuites   the list of supported connection suites.
     * @param proxySelector      the proxy selector.
     */
    public Address(String uriHost, int uriPort, DnsX dns, SocketFactory socketFactory,
            SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier, CertificatePinner certificatePinner,
            Authenticator proxyAuthenticator, Proxy proxy, List<Protocol> protocols,
            List<ConnectionSuite> connectionSuites, ProxySelector proxySelector) {
        this.url = new UnoUrl.Builder().scheme(null != sslSocketFactory ? Protocol.HTTPS.name : Protocol.HTTP.name)
                .host(uriHost).port(uriPort).build();

        if (null == dns) {
            throw new NullPointerException("dns == null");
        }
        this.dns = dns;

        if (null == socketFactory) {
            throw new NullPointerException("socketFactory == null");
        }
        this.socketFactory = socketFactory;

        if (null == proxyAuthenticator) {
            throw new NullPointerException("proxyAuthenticator == null");
        }
        this.proxyAuthenticator = proxyAuthenticator;

        if (null == protocols) {
            throw new NullPointerException("protocols == null");
        }
        this.protocols = Builder.immutableList(protocols);

        if (null == connectionSuites) {
            throw new NullPointerException("connectionSpecs == null");
        }
        this.connectionSuites = Builder.immutableList(connectionSuites);

        if (null == proxySelector) {
            throw new NullPointerException("proxySelector == null");
        }
        this.proxySelector = proxySelector;

        this.proxy = proxy;
        this.sslSocketFactory = sslSocketFactory;
        this.hostnameVerifier = hostnameVerifier;
        this.certificatePinner = certificatePinner;
    }

    /**
     * Returns the URL of the origin server.
     * <p>
     * The path, query, and fragment of this URL are always empty.
     *
     * @return the server's URL.
     */
    public UnoUrl url() {
        return url;
    }

    /**
     * Returns the DNS service used to resolve hostnames for this address.
     *
     * @return the DNS service.
     */
    public DnsX dns() {
        return dns;
    }

    /**
     * Returns the socket factory for this address.
     *
     * @return the socket factory.
     */
    public SocketFactory socketFactory() {
        return socketFactory;
    }

    /**
     * Returns the authenticator for proxy servers.
     *
     * @return the proxy authenticator.
     */
    public Authenticator proxyAuthenticator() {
        return proxyAuthenticator;
    }

    /**
     * Returns the list of protocols supported by this address, such as {@code http/1.1} and {@code h2}.
     *
     * @return an immutable list of protocols.
     */
    public List<Protocol> protocols() {
        return protocols;
    }

    /**
     * Returns the list of connection suites supported by this address.
     *
     * @return an immutable list of connection suites.
     */
    public List<ConnectionSuite> connectionSpecs() {
        return connectionSuites;
    }

    /**
     * Returns this address's proxy selector.
     *
     * @return the proxy selector.
     */
    public ProxySelector proxySelector() {
        return proxySelector;
    }

    /**
     * Returns this address's explicitly specified proxy. This is null if a proxy selector is preferred.
     *
     * @return the explicit proxy, which may be null.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * Returns the SSL socket factory for this address, or null if this is not an HTTPS address.
     *
     * @return the SSL socket factory, or null.
     */
    public SSLSocketFactory sslSocketFactory() {
        return sslSocketFactory;
    }

    /**
     * Returns the hostname verifier for this address, or null if this is not an HTTPS address.
     *
     * @return the hostname verifier, or null.
     */
    public HostnameVerifier hostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Returns the certificate pinner for this address, or null if this is not an HTTPS address.
     *
     * @return the certificate pinner, or null.
     */
    public CertificatePinner certificatePinner() {
        return certificatePinner;
    }

    /**
     * Indicates whether some other object is "equal to" this one. Two addresses are equal if they share the same URL
     * and non-host properties.
     *
     * @param other the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Address && url.equals(((Address) other).url) && equalsNonHost((Address) other);
    }

    /**
     * Returns a hash code value for the object. This is computed from the URL, DNS, proxy authenticator, protocols,
     * connection suites, proxy selector, and SSL/TLS settings.
     *
     * @return a hash code value for this object.
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
     * Compares the non-host properties of two addresses for equality. This includes DNS, proxy settings, protocols, and
     * SSL configuration.
     *
     * @param that the other Address object to compare.
     * @return true if the non-host properties are equal, false otherwise.
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
     * Returns a string representation of this address, including the host, port, and proxy information.
     *
     * @return a string representation of this address.
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
