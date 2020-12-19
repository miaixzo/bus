/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.UnoUrl;

import java.net.Proxy;

/**
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
 */
public final class RequestLine {

    private RequestLine() {
    }

    public static String get(Request request, Proxy.Type proxyType) {
        StringBuilder result = new StringBuilder();
        result.append(request.method());
        result.append(Symbol.C_SPACE);

        if (includeAuthorityInRequestLine(request, proxyType)) {
            result.append(request.url());
        } else {
            result.append(requestPath(request.url()));
        }

        result.append(" HTTP/1.1");
        return result.toString();
    }

    private static boolean includeAuthorityInRequestLine(Request request, Proxy.Type proxyType) {
        return !request.isHttps() && proxyType == Proxy.Type.HTTP;
    }

    public static String requestPath(UnoUrl url) {
        String path = url.encodedPath();
        String query = url.encodedQuery();
        return query != null ? (path + Symbol.C_QUESTION_MARK + query) : path;
    }

}
