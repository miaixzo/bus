/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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

import org.aoju.bus.http.magic.GetBuilder;
import org.aoju.bus.http.magic.HttpBuilder;
import org.aoju.bus.http.magic.PostBuilder;
import org.aoju.bus.http.magic.PutBuilder;
import org.aoju.bus.logger.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;

/**
 * 发送HTTP请求辅助类
 *
 * @author Kimi Liu
 * @version 5.6.3
 * @since JDK 1.8+
 */
public class Httpz {

    private static Client client = new Client(geHtttpd());

    private static Httpd geHtttpd() {
        Httpd.Builder builder = new Httpd().newBuilder();
        final X509TrustManager trustManager = new org.aoju.bus.http.secure.X509TrustManager();
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }
        return builder.sslSocketFactory(sslSocketFactory, trustManager).hostnameVerifier((hostname, session) -> true).build();
    }

    public static HttpBuilder newBuilder() {
        return new HttpBuilder(client.getHttpd());
    }

    public static HttpBuilder newBuilder(Httpd client) {
        return new HttpBuilder(client);
    }

    public static GetBuilder get() {
        return client.get();
    }

    public static PostBuilder post() {
        return client.post();
    }

    public static PutBuilder put() {
        return client.put();
    }

    public static Client getClient() {
        return client;
    }

    public static void setClient(Client httpClient) {
        Httpz.client = httpClient;
    }

    public static class Client {

        private Httpd httpd;

        public Client(Httpd httpd) {
            this.httpd = httpd;
        }

        public GetBuilder get() {
            return new GetBuilder(httpd);
        }

        public PostBuilder post() {
            return new PostBuilder(httpd);
        }

        public PutBuilder put() {
            return new PutBuilder(httpd);
        }

        public Httpd getHttpd() {
            return httpd;
        }

        public void setHttpd(Httpd httpd) {
            this.httpd = httpd;
        }

    }

}
