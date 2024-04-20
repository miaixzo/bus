/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.RealConnection;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.Internal;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用HTTP/2帧对请求和响应进行编码.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Http2Codec implements HttpCodec {

    /**
     * See http://tools.ietf.org/html/draft-ietf-httpbis-http2-09#section-8.1.3.
     */
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

    private final Interceptor.Chain chain;
    private final RealConnection realConnection;
    private final Http2Connection connection;
    private final Protocol protocol;
    private volatile Http2Stream stream;
    private volatile boolean canceled;

    public Http2Codec(Httpd client, RealConnection realConnection,
                      Interceptor.Chain chain, Http2Connection connection) {
        this.realConnection = realConnection;
        this.chain = chain;
        this.connection = connection;
        this.protocol = client.protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE)
                ? Protocol.H2_PRIOR_KNOWLEDGE
                : Protocol.HTTP_2;
    }

    public static List<Headers.Header> http2HeadersList(Request request) {
        Headers headers = request.headers();
        List<Headers.Header> result = new ArrayList<>(headers.size() + 4);
        result.add(new Headers.Header(Headers.Header.TARGET_METHOD, request.method()));
        result.add(new Headers.Header(Headers.Header.TARGET_PATH, RequestLine.requestPath(request.url())));
        String host = request.header("Host");
        if (host != null) {
            result.add(new Headers.Header(Headers.Header.TARGET_AUTHORITY, host)); // Optional.
        }
        result.add(new Headers.Header(Headers.Header.TARGET_SCHEME, request.url().scheme()));

        for (int i = 0, size = headers.size(); i < size; i++) {
            // header names must be lowercase.
            String name = StringKit.upperFirst(headers.name(i));
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name) || name.equals(Header.TE)
                    && "trailers".equals(headers.value(i))) {
                result.add(new Headers.Header(name, headers.value(i)));
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
            if (name.equals(Http.RESPONSE_STATUS_UTF8)) {
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
    public RealConnection connection() {
        return realConnection;
    }

    @Override
    public Sink createRequestBody(Request request, long contentLength) {
        return stream.getSink();
    }

    @Override
    public void writeRequestHeaders(Request request) throws IOException {
        if (stream != null) return;

        boolean hasRequestBody = request.body() != null;
        List<Headers.Header> requestHeaders = http2HeadersList(request);
        stream = connection.newStream(requestHeaders, hasRequestBody);
        // We may have been asked to cancel while creating the new stream and sending the request
        // headers, but there was still no stream to close.
        if (canceled) {
            stream.closeLater(ErrorCode.CANCEL);
            throw new IOException("Canceled");
        }
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
        if (expectContinue && Internal.instance.code(responseBuilder) == Http.HTTP_CONTINUE) {
            return null;
        }
        return responseBuilder;
    }

    @Override
    public long reportedContentLength(Response response) {
        return Headers.contentLength(response);
    }

    @Override
    public Source openResponseBodySource(Response response) {
        return stream.getSource();
    }

    @Override
    public Headers trailers() throws IOException {
        return stream.trailers();
    }

    @Override
    public void cancel() {
        canceled = true;
        if (stream != null) stream.closeLater(ErrorCode.CANCEL);
    }

}
