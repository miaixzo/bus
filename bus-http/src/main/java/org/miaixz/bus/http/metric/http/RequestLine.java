/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.UnoUrl;

import java.net.Proxy;

/**
 * This class generates the request line of an HTTP/1.1 request.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RequestLine {

    private RequestLine() {
    }

    /**
     * Returns the request line for a given request.
     *
     * @param request   The request.
     * @param proxyType The type of proxy being used.
     * @return The request line.
     */
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

    /**
     * Returns true if the request line should contain the full URL with host and port (like "GET http://android.com/foo
     * HTTP/1.1") or only the path (like "GET /foo HTTP/1.1").
     *
     * @param request   The request.
     * @param proxyType The type of proxy being used.
     * @return {@code true} if the authority should be included.
     */
    private static boolean includeAuthorityInRequestLine(Request request, Proxy.Type proxyType) {
        return !request.isHttps() && proxyType == Proxy.Type.HTTP;
    }

    /**
     * Returns the path to request, like the '/' in 'GET / HTTP/1.1'. Never empty, even if the request URL is. Includes
     * the query component if it exists.
     *
     * @param url The URL.
     * @return The request path.
     */
    public static String requestPath(UnoUrl url) {
        String path = url.encodedPath();
        String query = url.encodedQuery();
        return null != query ? (path + Symbol.C_QUESTION_MARK + query) : path;
    }

}
