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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.io.*;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.*;
import org.aoju.bus.http.accord.StreamAllocation;
import org.aoju.bus.http.bodys.RealResponseBody;
import org.aoju.bus.http.bodys.ResponseBody;
import org.aoju.bus.http.metric.Interceptor;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 使用HTTP/2帧对请求和响应进行编码.
 *
 * @author Kimi Liu
 * @version 6.2.8
 * @since JDK 1.8+
 */
public final class Http2Codec implements HttpCodec {

    private static final List<String> HTTP_2_SKIPPED_REQUEST_HEADERS = Builder.immutableList(
            Header.CONNECTION,
            Header.HOST,
            Header.KEEP_ALIVE,
            Header.PROXY_CONNECTION,
            Header.TE,
            Header.TRANSFER_ENCODING,
            Header.ENCODING,
            Header.UPGRADE,
            Http.TARGET_METHOD_UTF8,
            Http.TARGET_PATH_UTF8,
            Http.TARGET_SCHEME_UTF8,
            Http.TARGET_AUTHORITY_UTF8);
    private static final List<String> HTTP_2_SKIPPED_RESPONSE_HEADERS = Builder.immutableList(
            Header.CONNECTION,
            Header.HOST,
            Header.KEEP_ALIVE,
            Header.PROXY_CONNECTION,
            Header.TE,
            Header.TRANSFER_ENCODING,
            Header.ENCODING,
            Header.UPGRADE);
    final StreamAllocation streamAllocation;
    private final Interceptor.Chain chain;
    private final Http2Connection connection;
    private final Protocol protocol;
    private Http2Stream stream;

    public Http2Codec(Httpd client, Interceptor.Chain chain, StreamAllocation streamAllocation,
                      Http2Connection connection) {
        this.chain = chain;
        this.streamAllocation = streamAllocation;
        this.connection = connection;
        this.protocol = client.protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)
                ? Protocol.H2_PRIOR_KNOWLEDGE
                : Protocol.HTTP_2;
    }

    public static List<HttpHeaders> http2HeadersList(Request request) {
        Headers headers = request.headers();
        List<HttpHeaders> result = new ArrayList<>(headers.size() + 4);
        result.add(new HttpHeaders(HttpHeaders.TARGET_METHOD, request.method()));
        result.add(new HttpHeaders(HttpHeaders.TARGET_PATH, RequestLine.requestPath(request.url())));
        String host = request.header("Host");
        if (null != host) {
            result.add(new HttpHeaders(HttpHeaders.TARGET_AUTHORITY, host));
        }
        result.add(new HttpHeaders(HttpHeaders.TARGET_SCHEME, request.url().scheme()));

        for (int i = 0, size = headers.size(); i < size; i++) {
            // 标题名称必须小写
            ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name.utf8())) {
                result.add(new HttpHeaders(name, headers.value(i)));
            }
        }
        return result;
    }

    public static Response.Builder readHttp2HeadersList(Headers headerBlock,
                                                        Protocol protocol) throws IOException {
        StatusLine statusLine = null;
        Headers.Builder headersBuilder = new Headers.Builder();
        for (int i = 0, size = headerBlock.size(); i < size; i++) {
            String name = headerBlock.name(i);
            String value = headerBlock.value(i);
            if (name.equals(Http.RESPONSE_STATUS_UTF8)) {
                statusLine = StatusLine.parse("HTTP/1.1 " + value);
            } else if (!HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                Builder.instance.addLenient(headersBuilder, name, value);
            }
        }
        if (null == statusLine) throw new ProtocolException("Expected ':status' header not present");

        return new Response.Builder()
                .protocol(protocol)
                .code(statusLine.code)
                .message(statusLine.message)
                .headers(headersBuilder.build());
    }

    @Override
    public Sink createRequestBody(Request request, long contentLength) {
        return stream.getSink();
    }

    @Override
    public void writeRequestHeaders(Request request) throws IOException {
        if (null != stream) return;

        boolean hasRequestBody = null != request.body();
        List<HttpHeaders> requestHeaders = http2HeadersList(request);
        stream = connection.newStream(requestHeaders, hasRequestBody);
        stream.readTimeout().timeout(chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
        stream.writeTimeout().timeout(chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void flushRequest() throws IOException {
        connection.flush();
    }

    @Override
    public void finishRequest() throws IOException {
        stream.getSink().close();
    }

    @Override
    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        Headers headers = stream.takeHeaders();
        Response.Builder responseBuilder = readHttp2HeadersList(headers, protocol);
        if (expectContinue && Builder.instance.code(responseBuilder) == Http.HTTP_CONTINUE) {
            return null;
        }
        return responseBuilder;
    }

    @Override
    public ResponseBody openResponseBody(Response response) {
        streamAllocation.eventListener.responseBodyStart(streamAllocation.call);
        String contentType = response.header(Header.CONTENT_TYPE);
        long contentLength = HttpHeaders.contentLength(response);
        Source source = new StreamFinishingSource(stream.getSource());
        return new RealResponseBody(contentType, contentLength, IoKit.buffer(source));
    }

    @Override
    public void cancel() {
        if (null != stream) stream.closeLater(ErrorCode.CANCEL);
    }

    class StreamFinishingSource extends DelegateSource {
        boolean completed = false;
        long bytesRead = 0;

        StreamFinishingSource(Source delegate) {
            super(delegate);
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            try {
                long read = delegate().read(sink, byteCount);
                if (read > 0) {
                    bytesRead += read;
                }
                return read;
            } catch (IOException e) {
                endOfInput(e);
                throw e;
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            endOfInput(null);
        }

        private void endOfInput(IOException e) {
            if (completed) return;
            completed = true;
            streamAllocation.streamFinished(false, Http2Codec.this, bytesRead, e);
        }
    }

}
