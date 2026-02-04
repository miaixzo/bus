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
