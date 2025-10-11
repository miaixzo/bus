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
