/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.internal.http;

import org.aoju.bus.core.io.segment.Sink;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.bodys.ResponseBody;

import java.io.IOException;

/**
 * Encodes HTTP requests and decodes HTTP responses.
 *
 * @author Kimi Liu
 * @version 5.0.3
 * @since JDK 1.8+
 */
public interface HttpCodec {

    int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    Sink createRequestBody(Request request, long contentLength);

    void writeRequestHeaders(Request request) throws IOException;

    void flushRequest() throws IOException;

    void finishRequest() throws IOException;

    Response.Builder readResponseHeaders(boolean expectContinue) throws IOException;

    ResponseBody openResponseBody(Response response) throws IOException;

    void cancel();
}
