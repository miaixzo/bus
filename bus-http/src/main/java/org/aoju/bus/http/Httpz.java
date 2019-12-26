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

import org.aoju.bus.http.magic.GetBuilder;
import org.aoju.bus.http.magic.HttpBuilder;
import org.aoju.bus.http.magic.PostBuilder;
import org.aoju.bus.http.magic.PutBuilder;
import org.aoju.bus.logger.Logger;

import javax.net.ssl.*;
import java.security.SecureRandom;

/**
 * 发送HTTP请求辅助类
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class Httpz {

    private static Httpd httpd;

    static {
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
        httpd = builder.sslSocketFactory(sslSocketFactory, trustManager).hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).build();
    }

    public Httpz(Httpd httpd) {
        this.httpd = httpd;
    }

    public static HttpBuilder newBuilder() {
        return new HttpBuilder(httpd);
    }

    public static HttpBuilder newBuilder(Httpd httpd) {
        return new HttpBuilder(httpd);
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

}
