/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.http.Httpd;
import org.aoju.bus.http.metric.Handshake;
import org.aoju.bus.http.metric.Interceptor;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.URL;

/**
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public final class HttpsURLConnection extends DelegatingConnection {

    private final HttpURLConnection delegate;

    public HttpsURLConnection(URL url, Httpd client) {
        this(new HttpURLConnection(url, client));
    }

    public HttpsURLConnection(URL url, Httpd client, Interceptor filter) {
        this(new HttpURLConnection(url, client, filter));
    }

    public HttpsURLConnection(HttpURLConnection delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    protected Handshake handshake() {
        if (null == delegate.call) {
            throw new IllegalStateException("Connection has not yet been established");
        }

        return delegate.handshake;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return delegate.client.hostnameVerifier();
    }

    @Override
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        delegate.client = delegate.client.newBuilder()
                .hostnameVerifier(hostnameVerifier)
                .build();
    }

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return delegate.client.sslSocketFactory();
    }

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        if (null == sslSocketFactory) {
            throw new IllegalArgumentException("sslSocketFactory == null");
        }
        delegate.client = delegate.client.newBuilder()
                .sslSocketFactory(sslSocketFactory)
                .build();
    }

}