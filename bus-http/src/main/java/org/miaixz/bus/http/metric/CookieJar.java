/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.metric;

import org.miaixz.bus.http.Cookie;
import org.miaixz.bus.http.UnoUrl;

import java.util.Collections;
import java.util.List;

/**
 * Provides a policy and persistence for HTTP cookies.
 * <p>
 * As a policy, implementations of this interface are responsible for selecting which cookies to accept and reject. A
 * reasonable policy is to reject all cookies, though this may interfere with session-based authentication schemes that
 * require cookies.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CookieJar {

    /**
     * A cookie jar that never accepts any cookies.
     */
    CookieJar NO_COOKIES = new CookieJar() {

        @Override
        public void saveFromResponse(UnoUrl url, List<Cookie> cookies) {
        }

        @Override
        public List<Cookie> loadForRequest(UnoUrl url) {
            return Collections.emptyList();
        }
    };

    /**
     * Saves {@code cookies} from an HTTP response to this store, according to this jar's policy. Note that this method
     * may be called a second time for a single HTTP response if that response includes a trailer. For this obscure HTTP
     * feature, {@code cookies} contains only the trailer's cookies.
     *
     * @param url     The URL of the response.
     * @param cookies The list of cookies to save.
     */
    void saveFromResponse(UnoUrl url, List<Cookie> cookies);

    /**
     * Loads cookies from this jar for an HTTP request to {@code url}. This method returns a possibly empty list of
     * cookies for the network request. A simple implementation will return accepted cookies that have not yet expired
     * and that {@linkplain Cookie#matches match} {@code url}.
     *
     * @param url The URL of the request.
     * @return The list of cookies to include in the request.
     */
    List<Cookie> loadForRequest(UnoUrl url);

}
