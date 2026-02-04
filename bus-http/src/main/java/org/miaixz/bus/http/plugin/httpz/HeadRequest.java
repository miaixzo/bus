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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.bodys.RequestBody;

import java.util.Map;

/**
 * Represents an HTTP HEAD request. This class encapsulates all the parameters and configuration for a HEAD request.
 * HEAD requests are used to retrieve the headers for a resource without fetching the response body.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HeadRequest extends HttpRequest {

    /**
     * Constructs a new {@code HeadRequest}.
     *
     * @param url     The request URL.
     * @param tag     A tag for this request, used for cancellation.
     * @param params  The query parameters for the request.
     * @param headers The request headers.
     * @param id      A unique identifier for this request.
     */
    public HeadRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers, String id) {
        super(url, tag, params, headers, null, null, null, id);
    }

    /**
     * Builds the request body. For a HEAD request, the body is always null.
     *
     * @return Always returns {@code null}.
     */
    @Override
    protected RequestBody buildRequestBody() {
        return null;
    }

    /**
     * Builds the final Httpd {@link Request} object.
     *
     * @param requestBody The request body, which will be null for a HEAD request.
     * @return The constructed {@link Request} object.
     */
    @Override
    protected Request buildRequest(RequestBody requestBody) {
        // Constructs the request using the HEAD method.
        return builder.head().build();
    }

}
