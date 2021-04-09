/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.magic;

import org.aoju.bus.core.io.BufferSink;
import org.aoju.bus.core.io.Source;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.Headers;
import org.aoju.bus.http.Httpd;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.bodys.MultipartBody;
import org.aoju.bus.http.bodys.RequestBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求处理
 *
 * @author Kimi Liu
 * @version 6.2.2
 * @since JDK 1.8+
 */
public abstract class HttpRequest {

    protected int id;
    protected String url;
    protected Map<String, String> params;
    protected Map<String, String> encodedParams;
    protected Map<String, String> headers;
    protected String body;
    protected List<PostRequest.FileInfo> fileInfos;
    protected MultipartBody multipartBody;
    protected Request.Builder builder = new Request.Builder();

    protected HttpRequest(String url, Object tag, Map<String, String> params,
                          Map<String, String> headers,
                          List<PostRequest.FileInfo> fileInfos,
                          String body,
                          MultipartBody multipartBody,
                          int id) {
        this(url, tag, params, null, headers, fileInfos, body, multipartBody, id);
    }

    protected HttpRequest(String url, Object tag, Map<String, String> params,
                          Map<String, String> encodedParams,
                          Map<String, String> headers,
                          List<PostRequest.FileInfo> fileInfos,
                          String body,
                          MultipartBody multipartBody,
                          int id) {
        this.url = url;
        this.params = params;
        this.encodedParams = encodedParams;
        this.headers = headers;
        this.fileInfos = fileInfos;
        this.body = body;
        this.multipartBody = multipartBody;
        this.id = id;
        if (null == url) {
            throw new IllegalArgumentException("url can not be null.");
        }
        builder.url(url).tag(tag);
        appendHeaders();
    }

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

    protected abstract RequestBody buildRequestBody();

    protected abstract Request buildRequest(RequestBody requestBody);

    public RequestCall build(Httpd httpd) {
        return new RequestCall(this, httpd);
    }

    public Request createRequest(AbsCallback absCallback) {
        return buildRequest(buildRequestBody());
    }

    protected void appendHeaders() {
        Headers.Builder headerBuilder = new Headers.Builder();
        if (null == headers || headers.isEmpty())
            return;
        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        builder.headers(headerBuilder.build());
    }

    public int getId() {
        return id;
    }

}
