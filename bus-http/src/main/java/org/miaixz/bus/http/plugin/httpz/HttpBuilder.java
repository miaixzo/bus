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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.http.DnsX;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Httpz;
import org.miaixz.bus.http.Protocol;
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
 * 请求参数构造器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpBuilder {

    private Httpd.Builder builder;

    public HttpBuilder() {
        this.builder = new Httpd.Builder();
    }

    public HttpBuilder(Httpd httpd) {
        this.builder = httpd.newBuilder();
    }

    public HttpBuilder connectTimeout(long timeout, TimeUnit unit) {
        builder.connectTimeout(timeout, unit);
        return this;
    }

    public HttpBuilder readTimeout(long timeout, TimeUnit unit) {
        builder.readTimeout(timeout, unit);
        return this;
    }

    public HttpBuilder writeTimeout(long timeout, TimeUnit unit) {
        builder.writeTimeout(timeout, unit);
        return this;
    }

    public HttpBuilder pingInterval(long interval, TimeUnit unit) {
        builder.pingInterval(interval, unit);
        return this;
    }

    public HttpBuilder proxy(Proxy proxy) {
        builder.proxy(proxy);
        return this;
    }

    public HttpBuilder proxySelector(ProxySelector proxySelector) {
        builder.proxySelector(proxySelector);
        return this;
    }

    public HttpBuilder cookieJar(CookieJar cookieJar) {
        builder.cookieJar(cookieJar);
        return this;
    }

    public HttpBuilder cache(Cache cache) {
        builder.cache(cache);
        return this;
    }

    public HttpBuilder dns(DnsX dnsX) {
        builder.dns(dnsX);
        return this;
    }

    public HttpBuilder socketFactory(SocketFactory socketFactory) {
        builder.socketFactory(socketFactory);
        return this;
    }

    public HttpBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        builder.sslSocketFactory(sslSocketFactory);
        return this;
    }

    public HttpBuilder sslSocketFactory(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        builder.sslSocketFactory(sslSocketFactory, trustManager);
        return this;
    }

    public HttpBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
        builder.hostnameVerifier(hostnameVerifier);
        return this;
    }

    public HttpBuilder certificatePinner(CertificatePinner certificatePinner) {
        builder.certificatePinner(certificatePinner);
        return this;
    }

    public HttpBuilder authenticator(Authenticator authenticator) {
        builder.authenticator(authenticator);
        return this;
    }

    public HttpBuilder proxyAuthenticator(Authenticator proxyAuthenticator) {
        builder.proxyAuthenticator(proxyAuthenticator);
        return this;
    }

    public HttpBuilder connectionPool(ConnectionPool connectPool) {
        builder.connectionPool(connectPool);
        return this;
    }

    public HttpBuilder followSslRedirects(boolean followProtocolRedirects) {
        builder.followSslRedirects(followProtocolRedirects);
        return this;
    }

    public HttpBuilder followRedirects(boolean followRedirects) {
        builder.followRedirects(followRedirects);
        return this;
    }

    public HttpBuilder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
        builder.retryOnConnectionFailure(retryOnConnectionFailure);
        return this;
    }

    public HttpBuilder dispatcher(Dispatcher dispatcher) {
        builder.dispatcher(dispatcher);
        return this;
    }

    public HttpBuilder protocols(List<Protocol> protocols) {
        builder.protocols(protocols);
        return this;
    }

    public HttpBuilder connectionSpecs(List<ConnectionSuite> connectSuites) {
        builder.connectionSpecs(connectSuites);
        return this;
    }

    public HttpBuilder addInterceptor(Interceptor interceptor) {
        builder.addInterceptor(interceptor);
        return this;
    }

    public HttpBuilder addNetworkInterceptor(Interceptor interceptor) {
        builder.addNetworkInterceptor(interceptor);
        return this;
    }

    public Httpd.Builder getBuilder() {
        return builder;
    }

    public HttpBuilder sslContext(SSLContext sslContext) {
        builder.sslSocketFactory(sslContext.getSocketFactory(), SSLContextBuilder.newTrustManager())
                .hostnameVerifier((hostname, session) -> true);
        return this;
    }

    public Httpz.Client build() {
        return new Httpz.Client(builder.build());
    }

}
