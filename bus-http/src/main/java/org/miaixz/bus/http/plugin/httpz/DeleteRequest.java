/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.bodys.RequestBody;

import java.util.Map;

/**
 * Represents an HTTP DELETE request. This class encapsulates all the parameters and configuration for a DELETE request.
 * DELETE requests are used to delete a specified resource and typically do not have a request body.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeleteRequest extends HttpRequest {

    /**
     * Constructs a new {@code DeleteRequest}.
     *
     * @param url     The request URL.
     * @param tag     A tag for this request, used for cancellation.
     * @param params  The query parameters for the request.
     * @param headers The request headers.
     * @param id      A unique identifier for this request.
     */
    public DeleteRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers, String id) {
        super(url, tag, params, headers, null, null, null, id);
    }

    /**
     * Builds the request body. For a DELETE request, the body is always null.
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
     * @param requestBody The request body, which will be null for a DELETE request.
     * @return The constructed {@link Request} object.
     */
    @Override
    protected Request buildRequest(RequestBody requestBody) {
        // Constructs the request using the DELETE method.
        return builder.delete().build();
    }

}
