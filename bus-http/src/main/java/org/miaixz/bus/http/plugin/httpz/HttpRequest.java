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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.bodys.MultipartBody;
import org.miaixz.bus.http.bodys.RequestBody;

/**
 * An abstract base class for representing an HTTP request. It encapsulates common request properties such as URL,
 * parameters, headers, and body, and provides a template for building the final Httpd {@link Request} object.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class HttpRequest {

    /**
     * The unique identifier for this request.
     */
    protected String id;
    /**
     * The request URL.
     */
    protected String url;
    /**
     * The request body as a raw string (e.g., for JSON).
     */
    protected String body;
    /**
     * The request parameters (e.g., for form submissions or query strings).
     */
    protected Map<String, String> params;
    /**
     * The pre-encoded request parameters.
     */
    protected Map<String, String> encodedParams;
    /** The request headers. */
    protected Map<String, String> headers;
    /**
     * The multipart body for file uploads.
     */
    protected MultipartBody multipartBody;
    /**
     * A list of files for multipart uploads.
     */
    protected List<MultipartFile> list;
    /**
     * The builder for the final Httpd Request.
     */
    protected Request.Builder builder = new Request.Builder();

    /**
     * Constructs an HttpRequest.
     *
     * @param url           The request URL.
     * @param tag           A tag for the request, used for cancellation.
     * @param params        The request parameters.
     * @param headers       The request headers.
     * @param list          A list of files for multipart uploads.
     * @param body          The raw request body string.
     * @param multipartBody The multipart body.
     * @param id            The unique request identifier.
     */
    protected HttpRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers,
            List<MultipartFile> list, String body, MultipartBody multipartBody, String id) {
        this(url, tag, params, null, headers, list, body, multipartBody, id);
    }

    /**
     * Constructs an HttpRequest with both standard and pre-encoded parameters.
     *
     * @param url           The request URL.
     * @param tag           A tag for the request, used for cancellation.
     * @param params        The standard request parameters.
     * @param encodedParams The pre-encoded request parameters.
     * @param headers       The request headers.
     * @param list          A list of files for multipart uploads.
     * @param body          The raw request body string.
     * @param multipartBody The multipart body.
     * @param id            The unique request identifier.
     */
    protected HttpRequest(String url, Object tag, Map<String, String> params, Map<String, String> encodedParams,
            Map<String, String> headers, List<MultipartFile> list, String body, MultipartBody multipartBody,
            String id) {
        this.url = url;
        this.params = params;
        this.encodedParams = encodedParams;
        this.headers = headers;
        this.list = list;
        this.body = body;
        this.multipartBody = multipartBody;
        this.id = id;
        if (null == url) {
            throw new IllegalArgumentException("url can not be null.");
        }
        builder.url(url).tag(tag);
        headers();
    }

    /**
     * A static factory method to create a {@link RequestBody} from an {@link InputStream}.
     *
     * @param contentType The media type of the content.
     * @param is          The input stream to read from.
     * @return A new {@link RequestBody} instance.
     * @throws NullPointerException if the input stream is null.
     */
    public static RequestBody createRequestBody(final MediaType contentType, final InputStream is) {
        if (null == is)
            throw new NullPointerException("is == null");

        return new RequestBody() {

            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                try {
                    return is.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                Source source = null;
                try {
                    source = IoKit.source(is);
                    sink.writeAll(source);
                } finally {
                    IoKit.close(source);
                }
            }
        };
    }

    /**
     * Abstract method to be implemented by subclasses to construct the specific request body (e.g., for POST, PUT).
     *
     * @return The constructed {@link RequestBody}.
     */
    protected abstract RequestBody buildRequestBody();

    /**
     * Abstract method to be implemented by subclasses to construct the final {@link Request} object with the correct
     * HTTP method.
     *
     * @param requestBody The request body to be used.
     * @return The final {@link Request} object.
     */
    protected abstract Request buildRequest(RequestBody requestBody);

    /**
     * Builds a {@link RequestCall}, which is the executable representation of this request.
     *
     * @param httpd The {@link Httpd} client instance.
     * @return A new {@link RequestCall}.
     */
    public RequestCall build(Httpd httpd) {
        return new RequestCall(this, httpd);
    }

    /**
     * Creates the final {@link Request} object. This is a convenience method that combines building the request body
     * and the request itself.
     *
     * @param callback The callback for the request (not used in this implementation).
     * @return The constructed {@link Request}.
     */
    public Request createRequest(Callback callback) {
        return buildRequest(buildRequestBody());
    }

    /**
     * Appends the configured headers to the internal {@link Request.Builder}.
     */
    protected void headers() {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (null == headers || headers.isEmpty())
            return;
        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        builder.headers(headerBuilder.build());
    }

    /**
     * Gets the unique identifier for this request.
     *
     * @return The request ID string.
     */
    public String getId() {
        return id;
    }

}
