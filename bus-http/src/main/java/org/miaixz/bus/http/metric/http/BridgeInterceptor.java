/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.metric.http;

import java.io.IOException;
import java.util.List;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.io.source.GzipSource;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.RealResponseBody;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.metric.CookieJar;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NewChain;

/**
 * Bridges from application code to network code. First, it builds a network request from a user request. Then it
 * proceeds to call the network. Finally, it builds a user response from the network response.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class BridgeInterceptor implements Interceptor {

    /**
     * The cookie jar for managing cookies.
     */
    private final CookieJar cookieJar;

    /**
     * Constructs a new BridgeInterceptor.
     *
     * @param cookieJar The cookie jar to use for cookie management.
     */
    public BridgeInterceptor(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    /**
     * Intercepts the request to add necessary headers, handle cookies, and manage content encoding.
     *
     * @param chain The interceptor chain.
     * @return The response from the server, potentially with modifications (e.g., decompression).
     * @throws IOException if an I/O error occurs during the network call.
     */
    @Override
    public Response intercept(NewChain chain) throws IOException {
        Request userRequest = chain.request();
        Request.Builder requestBuilder = userRequest.newBuilder();

        RequestBody body = userRequest.body();
        if (body != null) {
            MediaType contentType = body.contentType();
            if (contentType != null) {
                requestBuilder.header(HTTP.CONTENT_TYPE, contentType.toString());
            }

            long contentLength = body.contentLength();
            if (contentLength != -1) {
                requestBuilder.header(HTTP.CONTENT_LENGTH, Long.toString(contentLength));
                requestBuilder.removeHeader(HTTP.TRANSFER_ENCODING);
            } else {
                requestBuilder.header(HTTP.TRANSFER_ENCODING, "chunked");
                requestBuilder.removeHeader(HTTP.CONTENT_LENGTH);
            }
        }

        if (userRequest.header(HTTP.HOST) == null) {
            requestBuilder.header(HTTP.HOST, Builder.hostHeader(userRequest.url(), false));
        }

        if (userRequest.header(HTTP.CONNECTION) == null) {
            requestBuilder.header(HTTP.CONNECTION, HTTP.KEEP_ALIVE);
        }

        // If we add an "Accept-Encoding: gzip" header field we're responsible for also decompressing
        // the transfer stream.
        boolean transparentGzip = false;
        if (userRequest.header(HTTP.ACCEPT_ENCODING) == null && userRequest.header("Range") == null) {
            transparentGzip = true;
            requestBuilder.header(HTTP.ACCEPT_ENCODING, "gzip");
        }

        List<Cookie> cookies = cookieJar.loadForRequest(userRequest.url());
        if (!cookies.isEmpty()) {
            requestBuilder.header(HTTP.COOKIE, cookieHeader(cookies));
        }

        if (userRequest.header(HTTP.USER_AGENT) == null) {
            requestBuilder.header(HTTP.USER_AGENT, "Httpd/" + Version.all());
        }

        Response networkResponse = chain.proceed(requestBuilder.build());

        Headers.receiveHeaders(cookieJar, userRequest.url(), networkResponse.headers());

        Response.Builder responseBuilder = networkResponse.newBuilder().request(userRequest);

        if (transparentGzip && "gzip".equalsIgnoreCase(networkResponse.header(HTTP.CONTENT_ENCODING))
                && Headers.hasBody(networkResponse)) {
            GzipSource responseBody = new GzipSource(networkResponse.body().source());
            Headers strippedHeaders = networkResponse.headers().newBuilder().removeAll(HTTP.CONTENT_ENCODING)
                    .removeAll(HTTP.CONTENT_LENGTH).build();
            responseBuilder.headers(strippedHeaders);
            String contentType = networkResponse.header(HTTP.CONTENT_TYPE);
            responseBuilder.body(new RealResponseBody(contentType, -1L, IoKit.buffer(responseBody)));
        }

        return responseBuilder.build();
    }

    /**
     * Returns a 'Cookie' HTTP request header with all cookies, like {@code "a=1; b=2"}.
     *
     * @param cookies The list of cookies.
     * @return The cookie header string.
     */
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
