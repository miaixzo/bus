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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.http.DnsX;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Httpz;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.accord.ConnectionSuite;
import org.miaixz.bus.http.cache.Cache;
import org.miaixz.bus.http.metric.CookieJar;
import org.miaixz.bus.http.metric.Dispatcher;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.secure.Authenticator;
import org.miaixz.bus.http.secure.CertificatePinner;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A fluent builder for configuring and creating an {@link Httpz.Client} instance. This class wraps an
 * {@link Httpd.Builder} to provide a simplified, chainable interface for setting up the underlying HTTP client.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpBuilder {

    /** The underlying builder from the core HTTP client. */
    private final Httpd.Builder builder;

    /**
     * Creates a new builder with a default client configuration.
     */
    public HttpBuilder() {
        this.builder = new Httpd.Builder();
    }

    /**
     * Creates a new builder based on the configuration of an existing {@link Httpd} client.
     *
     * @param httpd The client whose configuration will be copied.
     */
    public HttpBuilder(Httpd httpd) {
        this.builder = httpd.newBuilder();
    }

    /**
     * Sets the default connect timeout for new connections.
     *
     * @param timeout The timeout value.
     * @param unit    The time unit for the timeout.
     * @return This builder instance for chaining.
     */
    public HttpBuilder connectTimeout(long timeout, TimeUnit unit) {
        builder.connectTimeout(timeout, unit);
        return this;
    }

    /**
     * Sets the default read timeout for new connections.
     *
     * @param timeout The timeout value.
     * @param unit    The time unit for the timeout.
     * @return This builder instance for chaining.
     */
    public HttpBuilder readTimeout(long timeout, TimeUnit unit) {
        builder.readTimeout(timeout, unit);
        return this;
    }

    /**
     * Sets the default write timeout for new connections.
     *
     * @param timeout The timeout value.
     * @param unit    The time unit for the timeout.
     * @return This builder instance for chaining.
     */
    public HttpBuilder writeTimeout(long timeout, TimeUnit unit) {
        builder.writeTimeout(timeout, unit);
        return this;
    }

    /**
     * Sets the ping interval for web socket connections.
     *
     * @param interval The interval value.
     * @param unit     The time unit for the interval.
     * @return This builder instance for chaining.
     */
    public HttpBuilder pingInterval(long interval, TimeUnit unit) {
        builder.pingInterval(interval, unit);
        return this;
    }

    /**
     * Sets the HTTP proxy that will be used by connections created by this client.
     *
     * @param proxy The proxy to use.
     * @return This builder instance for chaining.
     */
    public HttpBuilder proxy(Proxy proxy) {
        builder.proxy(proxy);
        return this;
    }

    /**
     * Sets the proxy selection policy for this client.
     *
     * @param proxySelector The proxy selector.
     * @return This builder instance for chaining.
     */
    public HttpBuilder proxySelector(ProxySelector proxySelector) {
        builder.proxySelector(proxySelector);
        return this;
    }

    /**
     * Sets the cookie handler for this client.
     *
     * @param cookieJar The cookie jar.
     * @return This builder instance for chaining.
     */
    public HttpBuilder cookieJar(CookieJar cookieJar) {
        builder.cookieJar(cookieJar);
        return this;
    }

    /**
     * Sets the response cache to be used by this client.
     *
     * @param cache The cache.
     * @return This builder instance for chaining.
     */
    public HttpBuilder cache(Cache cache) {
        builder.cache(cache);
        return this;
    }

    /**
     * Sets the DNS service used to lookup IP addresses for hostnames.
     *
     * @param dnsX The custom DNS service.
     * @return This builder instance for chaining.
     */
    public HttpBuilder dns(DnsX dnsX) {
        builder.dns(dnsX);
        return this;
    }

    /**
     * Sets the socket factory used to create connections.
     *
     * @param socketFactory The socket factory.
     * @return This builder instance for chaining.
     */
    public HttpBuilder socketFactory(SocketFactory socketFactory) {
        builder.socketFactory(socketFactory);
        return this;
    }

    /**
     * Sets the SSL socket factory used to secure HTTPS connections.
     *
     * @param sslSocketFactory The SSL socket factory.
     * @return This builder instance for chaining.
     * @deprecated Use {@link #sslSocketFactory(SSLSocketFactory, X509TrustManager)} instead.
     */
    @Deprecated
    public HttpBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        builder.sslSocketFactory(sslSocketFactory);
        return this;
    }

    /**
     * Sets the SSL socket factory and trust manager used to secure HTTPS connections.
     *
     * @param sslSocketFactory The SSL socket factory.
     * @param trustManager     The trust manager.
     * @return This builder instance for chaining.
     */
    public HttpBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        builder.sslSocketFactory(sslSocketFactory, trustManager);
        return this;
    }

    /**
     * Sets the verifier used to confirm that response certificates apply to the requested hostnames.
     *
     * @param hostnameVerifier The hostname verifier.
     * @return This builder instance for chaining.
     */
    public HttpBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
        builder.hostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * Sets the certificate pinner used to constrain which certificates are trusted.
     *
     * @param certificatePinner The certificate pinner.
     * @return This builder instance for chaining.
     */
    public HttpBuilder certificatePinner(CertificatePinner certificatePinner) {
        builder.certificatePinner(certificatePinner);
        return this;
    }

    /**
     * Sets the authenticator used to respond to challenges from origin servers.
     *
     * @param authenticator The authenticator.
     * @return This builder instance for chaining.
     */
    public HttpBuilder authenticator(Authenticator authenticator) {
        builder.authenticator(authenticator);
        return this;
    }

    /**
     * Sets the authenticator used to respond to challenges from proxy servers.
     *
     * @param proxyAuthenticator The proxy authenticator.
     * @return This builder instance for chaining.
     */
    public HttpBuilder proxyAuthenticator(Authenticator proxyAuthenticator) {
        builder.proxyAuthenticator(proxyAuthenticator);
        return this;
    }

    /**
     * Sets the connection pool used to recycle HTTP and HTTPS connections.
     *
     * @param connectPool The connection pool.
     * @return This builder instance for chaining.
     */
    public HttpBuilder connectionPool(ConnectionPool connectPool) {
        builder.connectionPool(connectPool);
        return this;
    }

    /**
     * Configures this client to follow redirects from HTTPS to HTTP and from HTTP to HTTPS.
     *
     * @param followProtocolRedirects {@code true} to follow redirects across protocols.
     * @return This builder instance for chaining.
     */
    public HttpBuilder followSslRedirects(boolean followProtocolRedirects) {
        builder.followSslRedirects(followProtocolRedirects);
        return this;
    }

    /**
     * Configures this client to follow redirects.
     *
     * @param followRedirects {@code true} to follow redirects.
     * @return This builder instance for chaining.
     */
    public HttpBuilder followRedirects(boolean followRedirects) {
        builder.followRedirects(followRedirects);
        return this;
    }

    /**
     * Configures this client to retry or not retry IO failures.
     *
     * @param retryOnConnectionFailure {@code true} to retry on connection failures.
     * @return This builder instance for chaining.
     */
    public HttpBuilder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
        builder.retryOnConnectionFailure(retryOnConnectionFailure);
        return this;
    }

    /**
     * Sets the dispatcher used to execute asynchronous requests.
     *
     * @param dispatcher The dispatcher.
     * @return This builder instance for chaining.
     */
    public HttpBuilder dispatcher(Dispatcher dispatcher) {
        builder.dispatcher(dispatcher);
        return this;
    }

    /**
     * Configures the protocols used by this client.
     *
     * @param protocols The list of protocols.
     * @return This builder instance for chaining.
     */
    public HttpBuilder protocols(List<Protocol> protocols) {
        builder.protocols(protocols);
        return this;
    }

    /**
     * Configures the connection specs used by this client.
     *
     * @param connectSuites The list of connection specs.
     * @return This builder instance for chaining.
     */
    public HttpBuilder connectionSpecs(List<ConnectionSuite> connectSuites) {
        builder.connectionSpecs(connectSuites);
        return this;
    }

    /**
     * Adds an application interceptor to this client.
     *
     * @param interceptor The interceptor to add.
     * @return This builder instance for chaining.
     */
    public HttpBuilder addInterceptor(Interceptor interceptor) {
        builder.addInterceptor(interceptor);
        return this;
    }

    /**
     * Adds a network interceptor to this client.
     *
     * @param interceptor The interceptor to add.
     * @return This builder instance for chaining.
     */
    public HttpBuilder addNetworkInterceptor(Interceptor interceptor) {
        builder.addNetworkInterceptor(interceptor);
        return this;
    }

    /**
     * Returns the underlying {@link Httpd.Builder} for advanced configuration.
     *
     * @return The raw builder.
     */
    public Httpd.Builder getBuilder() {
        return builder;
    }

    /**
     * A convenience method to configure a custom {@link SSLContext}. This method also sets a permissive trust manager
     * and hostname verifier.
     *
     * @param sslContext The SSL context to use.
     * @return This builder instance for chaining.
     */
    public HttpBuilder sslContext(SSLContext sslContext) {
        builder.sslSocketFactory(sslContext.getSocketFactory(), SSLContextBuilder.newTrustManager())
                .hostnameVerifier((hostname, session) -> true);
        return this;
    }

    /**
     * Creates an {@link Httpz.Client} with the configured settings.
     *
     * @return A new {@link Httpz.Client} instance.
     */
    public Httpz.Client build() {
        return new Httpz.Client(builder.build());
    }

}
