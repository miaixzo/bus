/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.http.internal.http.second;

import org.aoju.bus.core.io.*;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.*;
import org.aoju.bus.http.internal.Internal;
import org.aoju.bus.http.internal.connection.StreamAllocation;
import org.aoju.bus.http.internal.http.*;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Encode requests and responses using HTTP/2 frames.
 *
 * @author Kimi Liu
 * @version 3.0.6
 * @since JDK 1.8
 */
public final class Http2Codec implements HttpCodec {

    private static final String CONNECTION = "connection";
    private static final String HOST = "host";
    private static final String KEEP_ALIVE = "keep-alive";
    private static final String PROXY_CONNECTION = "proxy-connection";
    private static final String TRANSFER_ENCODING = "transfer-encoding";
    private static final String TE = "te";
    private static final String ENCODING = "encoding";
    private static final String UPGRADE = "upgrade";

    /**
     * See http://tools.ietf.org/html/draft-ietf-httpbis-http2-09#section-8.1.3.
     */
    private static final List<String> HTTP_2_SKIPPED_REQUEST_HEADERS = Internal.immutableList(
            CONNECTION,
            HOST,
            KEEP_ALIVE,
            PROXY_CONNECTION,
            TE,
            TRANSFER_ENCODING,
            ENCODING,
            UPGRADE,
            Header.TARGET_METHOD_UTF8,
            Header.TARGET_PATH_UTF8,
            Header.TARGET_SCHEME_UTF8,
            Header.TARGET_AUTHORITY_UTF8);
    private static final List<String> HTTP_2_SKIPPED_RESPONSE_HEADERS = Internal.immutableList(
            CONNECTION,
            HOST,
            KEEP_ALIVE,
            PROXY_CONNECTION,
            TE,
            TRANSFER_ENCODING,
            ENCODING,
            UPGRADE);
    final StreamAllocation streamAllocation;
    private final Interceptor.Chain chain;
    private final Http2Connection connection;
    private final Protocol protocol;
    private Http2Stream stream;

    public Http2Codec(HttpClient client, Interceptor.Chain chain, StreamAllocation streamAllocation,
                      Http2Connection connection) {
        this.chain = chain;
        this.streamAllocation = streamAllocation;
        this.connection = connection;
        this.protocol = client.protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)
                ? Protocol.H2_PRIOR_KNOWLEDGE
                : Protocol.HTTP_2;
    }

    public static List<Header> http2HeadersList(Request request) {
        Headers headers = request.headers();
        List<Header> result = new ArrayList<>(headers.size() + 4);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.url())));
        String host = request.header("Host");
        if (host != null) {
            result.add(new Header(Header.TARGET_AUTHORITY, host)); // Optional.
        }
        result.add(new Header(Header.TARGET_SCHEME, request.url().scheme()));

        for (int i = 0, size = headers.size(); i < size; i++) {
            // header names must be lowercase.
            ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name.utf8())) {
                result.add(new Header(name, headers.value(i)));
            }
        }
        return result;
    }

    /**
     * Returns headers for a name value block containing an HTTP/2 response.
     */
    public static Response.Builder readHttp2HeadersList(Headers headerBlock,
                                                        Protocol protocol) throws IOException {
        StatusLine statusLine = null;
        Headers.Builder headersBuilder = new Headers.Builder();
        for (int i = 0, size = headerBlock.size(); i < size; i++) {
            String name = headerBlock.name(i);
            String value = headerBlock.value(i);
            if (name.equals(Header.RESPONSE_STATUS_UTF8)) {
                statusLine = StatusLine.parse("HTTP/1.1 " + value);
            } else if (!HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                Internal.instance.addLenient(headersBuilder, name, value);
            }
        }
        if (statusLine == null) throw new ProtocolException("Expected ':status' header not present");

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
        if (stream != null) return;

        boolean hasRequestBody = request.body() != null;
        List<Header> requestHeaders = http2HeadersList(request);
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
        if (expectContinue && Internal.instance.code(responseBuilder) == StatusLine.HTTP_CONTINUE) {
            return null;
        }
        return responseBuilder;
    }

    @Override
    public ResponseBody openResponseBody(Response response) throws IOException {
        streamAllocation.eventListener.responseBodyStart(streamAllocation.call);
        String contentType = response.header("Content-Type");
        long contentLength = HttpHeaders.contentLength(response);
        Source source = new StreamFinishingSource(stream.getSource());
        return new RealResponseBody(contentType, contentLength, IoUtils.buffer(source));
    }

    @Override
    public void cancel() {
        if (stream != null) stream.closeLater(ErrorCode.CANCEL);
    }

    class StreamFinishingSource extends ForwardingSource {
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
