/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.Version;
import org.aoju.bus.core.io.GzipSource;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.MimeType;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.*;
import org.aoju.bus.http.bodys.RealResponseBody;
import org.aoju.bus.http.bodys.RequestBody;
import org.aoju.bus.http.metric.CookieJar;
import org.aoju.bus.http.metric.Interceptor;

import java.io.IOException;
import java.util.List;

/**
 * 从应用程序代码连接到网络代码。首先，它从用户请求构建网络请求。
 * 然后它继续调用网络。最后，它从网络响应构建用户响应
 *
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK 1.8+
 */
public final class BridgeInterceptor implements Interceptor {

    private final CookieJar cookieJar;

    public BridgeInterceptor(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userRequest = chain.request();
        Request.Builder requestBuilder = userRequest.newBuilder();

        RequestBody body = userRequest.body();
        if (body != null) {
            MimeType contentType = body.contentType();
            if (contentType != null) {
                requestBuilder.header(Header.CONTENT_TYPE, contentType.toString());
            }

            long contentLength = body.contentLength();
            if (contentLength != -1) {
                requestBuilder.header(Header.CONTENT_LENGTH, Long.toString(contentLength));
                requestBuilder.removeHeader(Header.TRANSFER_ENCODING);
            } else {
                requestBuilder.header(Header.TRANSFER_ENCODING, "chunked");
                requestBuilder.removeHeader(Header.CONTENT_LENGTH);
            }
        }

        if (userRequest.header(Header.HOST) == null) {
            requestBuilder.header(Header.HOST, Builder.hostHeader(userRequest.url(), false));
        }

        if (userRequest.header(Header.CONNECTION) == null) {
            requestBuilder.header(Header.CONNECTION, Header.KEEP_ALIVE);
        }

        // If we add an "Accept-Encoding: gzip" header field we're responsible for also decompressing
        // the transfer stream.
        boolean transparentGzip = false;
        if (userRequest.header(Header.ACCEPT_ENCODING) == null && userRequest.header("Range") == null) {
            transparentGzip = true;
            requestBuilder.header(Header.ACCEPT_ENCODING, "gzip");
        }

        List<Cookie> cookies = cookieJar.loadForRequest(userRequest.url());
        if (!cookies.isEmpty()) {
            requestBuilder.header(Header.COOKIE, cookieHeader(cookies));
        }

        if (userRequest.header(Header.USER_AGENT) == null) {
            requestBuilder.header(Header.USER_AGENT, "Httpd/" + Version.all());
        }

        Response networkResponse = chain.proceed(requestBuilder.build());

        HttpHeaders.receiveHeaders(cookieJar, userRequest.url(), networkResponse.headers());

        Response.Builder responseBuilder = networkResponse.newBuilder()
                .request(userRequest);

        if (transparentGzip
                && "gzip".equalsIgnoreCase(networkResponse.header(Header.CONTENT_ENCODING))
                && HttpHeaders.hasBody(networkResponse)) {
            GzipSource responseBody = new GzipSource(networkResponse.body().source());
            Headers strippedHeaders = networkResponse.headers().newBuilder()
                    .removeAll(Header.CONTENT_ENCODING)
                    .removeAll(Header.CONTENT_LENGTH)
                    .build();
            responseBuilder.headers(strippedHeaders);
            String contentType = networkResponse.header(Header.CONTENT_TYPE);
            responseBuilder.body(new RealResponseBody(contentType, -1L, IoKit.buffer(responseBody)));
        }

        return responseBuilder.build();
    }

    private String cookieHeader(List<Cookie> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        for (int i = 0, size = cookies.size(); i < size; i++) {
            if (i > 0) {
                cookieHeader.append("; ");
            }
            Cookie cookie = cookies.get(i);
            cookieHeader.append(cookie.name()).append(Symbol.C_EQUAL).append(cookie.value());
        }
        return cookieHeader.toString();
    }

}
