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

import org.aoju.bus.http.Headers;
import org.aoju.bus.http.Protocol;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.bodys.ResponseBody;
import org.aoju.bus.http.metric.Handshake;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * HTTP响应处理
 *
 * @author Kimi Liu
 * @version 6.2.3
 * @since JDK 1.8+
 */
public class HttpResponse {

    private Response response;

    public HttpResponse(Response response) {
        this.response = response;
    }

    public Request request() {
        return response.request();
    }

    public Protocol protocol() {
        return response.protocol();
    }

    public int code() {
        return response.code();
    }

    public boolean isSuccessful() {
        return response.isSuccessful();
    }

    public String message() {
        return response.message();
    }

    public Handshake handshake() {
        return response.handshake();
    }

    public List<String> headers(String name) {
        return response.headers(name);
    }

    public String header(String name) {
        return response.header(name, null);
    }

    public String header(String name, String defaultValue) {
        return response.header(name, defaultValue);
    }

    public Headers headers() {
        return response.headers();
    }

    public ResponseBody peekBody(long byteCount) throws IOException {
        return response.peekBody(byteCount);
    }

    public ResponseBody body() {
        return response.body();
    }

    public final String string() throws IOException {
        return body().string();
    }

    public final String string(String charset) throws IOException {
        return new String(body().bytes(), charset);
    }

    public final byte[] bytes() throws IOException {
        return body().bytes();
    }

    public final InputStream byteStream() {
        return body().source().inputStream();
    }

    public Response getResponse() {
        return response;
    }

}
