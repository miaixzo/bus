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
package org.miaixz.bus.http.metric.http;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.RealConnection;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.NewChain;

/**
 * Encodes requests and decodes responses using HTTP/2 frames.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Http2Codec implements HttpCodec {

    /**
     * HTTP/2 request headers to be skipped. See http://tools.ietf.org/html/draft-ietf-httpbis-http2-09#section-8.1.3.
     */
    private static final List<String> HTTP_2_SKIPPED_REQUEST_HEADERS = Builder.immutableList(
            HTTP.CONNECTION,
            HTTP.HOST,
            HTTP.KEEP_ALIVE,
            HTTP.PROXY_CONNECTION,
            HTTP.TE,
            HTTP.TRANSFER_ENCODING,
            HTTP.ENCODING,
            HTTP.UPGRADE,
            HTTP.TARGET_METHOD_UTF8,
            HTTP.TARGET_PATH_UTF8,
            HTTP.TARGET_SCHEME_UTF8,
            HTTP.TARGET_AUTHORITY_UTF8);

    /**
     * HTTP/2 response headers to be skipped.
     */
    private static final List<String> HTTP_2_SKIPPED_RESPONSE_HEADERS = Builder.immutableList(
            HTTP.CONNECTION,
            HTTP.HOST,
            HTTP.KEEP_ALIVE,
            HTTP.PROXY_CONNECTION,
            HTTP.TE,
            HTTP.TRANSFER_ENCODING,
            HTTP.ENCODING,
            HTTP.UPGRADE);

    private final NewChain chain;
    private final RealConnection realConnection;
    private final Http2Connection connection;
    private final Protocol protocol;
    private volatile Http2Stream stream;
    private volatile boolean canceled;

    /**
     * Constructs a new Http2Codec.
     *
     * @param client         The Httpd client instance.
     * @param realConnection The real connection to the server.
     * @param chain          The interceptor chain.
     * @param connection     The HTTP/2 connection.
     */
    public Http2Codec(Httpd client, RealConnection realConnection, NewChain chain, Http2Connection connection) {
        this.realConnection = realConnection;
        this.chain = chain;
        this.connection = connection;
        this.protocol = client.protocols().contains(Protocol.H2_PRIOR_KNOWLEDGE) ? Protocol.H2_PRIOR_KNOWLEDGE
                : Protocol.HTTP_2;
    }

    /**
     * Converts an HTTP request to a list of HTTP/2 headers.
     *
     * @param request The HTTP request.
     * @return A list of HTTP/2 headers.
     */
    public static List<Http2Header> http2HeadersList(Request request) {
        Headers headers = request.headers();
        List<Http2Header> result = new ArrayList<>(headers.size() + 4);
        result.add(new Http2Header(Http2Header.TARGET_METHOD, request.method()));
        result.add(new Http2Header(Http2Header.TARGET_PATH, RequestLine.requestPath(request.url())));
        String host = request.header("Host");
        if (host != null) {
            result.add(new Http2Header(Http2Header.TARGET_AUTHORITY, host)); // Optional.
        }
        result.add(new Http2Header(Http2Header.TARGET_SCHEME, request.url().scheme()));

        for (int i = 0, size = headers.size(); i < size; i++) {
            // header names must be lowercase.
            String name = StringKit.upperFirst(headers.name(i));
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name)
                    || name.equals(HTTP.TE) && "trailers".equals(headers.value(i))) {
                result.add(new Http2Header(name, headers.value(i)));
            }
        }
        return result;
    }

    /**
     * Creates a {@link Response.Builder} from a list of HTTP/2 headers.
     *
     * @param headerBlock The block of headers.
     * @param protocol    The HTTP protocol.
     * @return A {@link Response.Builder}.
     * @throws IOException if an I/O error occurs.
     */
    public static Response.Builder readHttp2HeadersList(Headers headerBlock, Protocol protocol) throws IOException {
        StatusLine statusLine = null;
        Headers.Builder headersBuilder = new Headers.Builder();
        for (int i = 0, size = headerBlock.size(); i < size; i++) {
            String name = headerBlock.name(i);
            String value = headerBlock.value(i);
            if (name.equals(HTTP.RESPONSE_STATUS_UTF8)) {
                statusLine = StatusLine.parse("HTTP/1.1 " + value);
            } else if (!HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                Internal.instance.addLenient(headersBuilder, name, value);
            }
        }
        if (statusLine == null)
            throw new ProtocolException("Expected ':status' header not present");

        return new Response.Builder().protocol(protocol).code(statusLine.code).message(statusLine.message)
                .headers(headersBuilder.build());
    }

    /**
     * Returns the connection that carries the transport.
     *
     * @return The real connection.
     */
    @Override
    public RealConnection connection() {
        return realConnection;
    }

    /**
     * Creates a sink to write the request body.
     *
     * @param request       The request.
     * @param contentLength The length of the content.
     * @return A sink for writing the request body.
     */
    @Override
    public Sink createRequestBody(Request request, long contentLength) {
        return stream.getSink();
    }

    /**
     * Writes the request headers to the stream.
     *
     * @param request The request.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void writeRequestHeaders(Request request) throws IOException {
        if (stream != null)
            return;

        boolean hasRequestBody = request.body() != null;
        List<Http2Header> requestHeaders = http2HeadersList(request);
        stream = connection.newStream(requestHeaders, hasRequestBody);
        // We may have been asked to cancel while creating the new stream and sending the request
        // headers, but there was still no stream to close.
        if (canceled) {
            stream.closeLater(Http2ErrorCode.CANCEL);
            throw new IOException("Canceled");
        }
        stream.readTimeout().timeout(chain.readTimeoutMillis(), TimeUnit.MILLISECONDS);
        stream.writeTimeout().timeout(chain.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Flushes the request to the underlying connection.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void flushRequest() throws IOException {
        connection.flush();
    }

    /**
     * Finishes writing the request.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void finishRequest() throws IOException {
        stream.getSink().close();
    }

    /**
     * Reads the response headers from the stream.
     *
     * @param expectContinue true if a 100-continue response is expected.
     * @return A {@link Response.Builder} with the response headers.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        Headers headers = stream.takeHeaders();
        Response.Builder responseBuilder = readHttp2HeadersList(headers, protocol);
        if (expectContinue && Internal.instance.code(responseBuilder) == HTTP.HTTP_CONTINUE) {
            return null;
        }
        return responseBuilder;
    }

    /**
     * Reports the content length of the response.
     *
     * @param response The response.
     * @return The content length.
     */
    @Override
    public long reportedContentLength(Response response) {
        return Headers.contentLength(response);
    }

    /**
     * Opens a source to read the response body.
     *
     * @param response The response.
     * @return A source for reading the response body.
     */
    @Override
    public Source openResponseBodySource(Response response) {
        return stream.getSource();
    }

    /**
     * Returns the trailers of the response.
     *
     * @return The response trailers.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public Headers trailers() throws IOException {
        return stream.trailers();
    }

    /**
     * Cancels the stream.
     */
    @Override
    public void cancel() {
        canceled = true;
        if (stream != null)
            stream.closeLater(Http2ErrorCode.CANCEL);
    }

}
