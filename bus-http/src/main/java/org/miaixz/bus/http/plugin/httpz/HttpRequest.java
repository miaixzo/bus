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
 * HTTP请求处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class HttpRequest {

    protected String id;
    protected String url;
    protected String body;
    protected Map<String, String> params;
    protected Map<String, String> encodedParams;
    protected Map<String, String> headers;
    protected MultipartBody multipartBody;
    protected List<MultipartFile> list;
    protected Request.Builder builder = new Request.Builder();

    protected HttpRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers,
            List<MultipartFile> list, String body, MultipartBody multipartBody, String id) {
        this(url, tag, params, null, headers, list, body, multipartBody, id);
    }

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

    public static RequestBody createRequestBody(final MediaType mediaType, final InputStream is) {
        if (null == is)
            throw new NullPointerException("is == null");

        return new RequestBody() {
            @Override
            public MediaType mediaType() {
                return mediaType;
            }

            @Override
            public long length() {
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

    protected abstract RequestBody buildRequestBody();

    protected abstract Request buildRequest(RequestBody requestBody);

    public RequestCall build(Httpd httpd) {
        return new RequestCall(this, httpd);
    }

    public Request createRequest(Callback callback) {
        return buildRequest(buildRequestBody());
    }

    protected void headers() {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (null == headers || headers.isEmpty())
            return;
        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        builder.headers(headerBuilder.build());
    }

    public String getId() {
        return id;
    }

}
