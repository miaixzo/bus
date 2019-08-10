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
package org.aoju.bus.http.internal.http;

import org.aoju.bus.http.HttpUrl;
import org.aoju.bus.http.Request;

import java.net.HttpURLConnection;
import java.net.Proxy;

/**
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
public final class RequestLine {

    private RequestLine() {
    }

    /**
     * Returns the request status line, like "GET / HTTP/1.1". This is exposed to the application by
     * {@link HttpURLConnection#getHeaderFields}, so it needs to be set even if the transport is
     * HTTP/2.
     */
    public static String get(Request request, Proxy.Type proxyType) {
        StringBuilder result = new StringBuilder();
        result.append(request.method());
        result.append(' ');

        if (includeAuthorityInRequestLine(request, proxyType)) {
            result.append(request.url());
        } else {
            result.append(requestPath(request.url()));
        }

        result.append(" HTTP/1.1");
        return result.toString();
    }

    /**
     * Returns true if the request line should contain the full URL with host and port (like "GET
     * http://android.com/foo HTTP/1.1") or only the path (like "GET /foo HTTP/1.1").
     */
    private static boolean includeAuthorityInRequestLine(Request request, Proxy.Type proxyType) {
        return !request.isHttps() && proxyType == Proxy.Type.HTTP;
    }

    /**
     * Returns the path to request, like the '/' in 'GET / HTTP/1.1'. Never empty, even if the request
     * URL is. Includes the query component if it exists.
     */
    public static String requestPath(HttpUrl url) {
        String path = url.encodedPath();
        String query = url.encodedQuery();
        return query != null ? (path + '?' + query) : path;
    }
}
