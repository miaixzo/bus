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
package org.miaixz.bus.http.plugin.httpz;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.Httpd;

import java.util.Map;

/**
 * A builder for creating HTTP DELETE requests using a fluent interface. This class allows for setting the URL, query
 * parameters, headers, a tag, and a request ID.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeleteBuilder extends RequestBuilder<DeleteBuilder> {

    /**
     * Constructs a new {@code DeleteBuilder}.
     *
     * @param httpd The {@link Httpd} client instance.
     */
    public DeleteBuilder(Httpd httpd) {
        super(httpd);
    }

    /**
     * Builds the {@link RequestCall} for the DELETE request. If any query parameters have been added, they will be
     * appended to the URL.
     *
     * @return A {@code RequestCall} object ready to be executed.
     */
    @Override
    public RequestCall build() {
        if (null != params) {
            // Append query parameters to the URL.
            url = append(url, params);
        }
        return new DeleteRequest(url, tag, params, headers, id).build(httpd);
    }

    /**
     * Appends the given query parameters to the URL string.
     *
     * @param url    The original URL.
     * @param params The map of query parameters to append.
     * @return The new URL with the appended query parameters.
     */
    protected String append(String url, Map<String, String> params) {
        if (null == url || null == params || params.isEmpty()) {
            return url;
        }
        StringBuilder builder = new StringBuilder();
        params.forEach((k, v) -> {
            if (builder.length() == 0) {
                // Add '?' before the first parameter.
                builder.append(Symbol.QUESTION_MARK);
            } else {
                // Add '&' before subsequent parameters.
                builder.append(Symbol.AND);
            }
            builder.append(k);
            builder.append(Symbol.EQUAL).append(v);
        });
        return url + builder;
    }

}
