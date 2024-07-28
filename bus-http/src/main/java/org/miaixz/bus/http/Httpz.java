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
package org.miaixz.bus.http;

import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.http.plugin.httpz.GetBuilder;
import org.miaixz.bus.http.plugin.httpz.HttpBuilder;
import org.miaixz.bus.http.plugin.httpz.PostBuilder;
import org.miaixz.bus.http.plugin.httpz.PutBuilder;

import javax.net.ssl.X509TrustManager;

/**
 * 发送HTTP请求辅助类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Httpz {

    private static Client client = new Client();

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

        public Client() {
            final X509TrustManager trustManager = SSLContextBuilder.newTrustManager();
            this.httpd = new Httpd().newBuilder()
                    .sslSocketFactory(SSLContextBuilder.newSslSocketFactory(trustManager), trustManager)
                    .hostnameVerifier((hostname, session) -> true).build();
        }

        public Client(Httpd httpd) {
            this.httpd = httpd;
        }

        /**
         * 取消所有请求
         */
        public static void cancelAll() {
            cancelAll(client.getHttpd());
        }

        /**
         * @param httpd 发送HTTP请求
         */
        public static void cancelAll(final Httpd httpd) {
            if (httpd != null) {
                for (NewCall call : httpd.dispatcher().queuedCalls()) {
                    call.cancel();
                }
                for (NewCall call : httpd.dispatcher().runningCalls()) {
                    call.cancel();
                }
            }
        }

        /**
         * 取消请求
         *
         * @param tag 标签
         */
        public static void cancel(final Object tag) {
            cancel(client.getHttpd(), tag);
        }

        /**
         * 取消请求
         *
         * @param httpd 发送HTTP请求
         * @param tag   标签
         */
        public static void cancel(final Httpd httpd, final Object tag) {
            if (httpd != null && tag != null) {
                for (NewCall call : httpd.dispatcher().queuedCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
                for (NewCall call : httpd.dispatcher().runningCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
            }
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
