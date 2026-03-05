/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.accord.RealConnection;

import java.io.IOException;

/**
 * Encodes HTTP requests and decodes HTTP responses.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface HttpCodec {

    /**
     * The timeout to use when discarding an input stream. Since this is used for connection reuse, this timeout should
     * be significantly less than the time it takes to establish a new connection.
     */
    int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    /**
     * Returns the connection that carries this codec.
     *
     * @return The real connection.
     */
    RealConnection connection();

    /**
     * Returns an output stream that can stream the request body.
     *
     * @param request       The network request.
     * @param contentLength The content length of the request body.
     * @return A sink for the request body.
     * @throws IOException if an I/O error occurs.
     */
    Sink createRequestBody(Request request, long contentLength) throws IOException;

    /**
     * Writes the request headers to the network. This should update the HTTP engine's sentRequestMillis field.
     *
     * @param request The network request.
     * @throws IOException if an I/O error occurs.
     */
    void writeRequestHeaders(Request request) throws IOException;

    /**
     * Flushes the request to the underlying socket.
     *
     * @throws IOException if an I/O error occurs.
     */
    void flushRequest() throws IOException;

    /**
     * Flushes the request to the underlying socket, indicating that no more bytes will be transmitted.
     *
     * @throws IOException if an I/O error occurs.
     */
    void finishRequest() throws IOException;

    /**
     * Parses the response headers from the HTTP transport.
     *
     * @param expectContinue If this is an intermediate response with a "100" response code, this returns null.
     *                       Otherwise, this method will never return null.
     * @return A response builder.
     * @throws IOException if an I/O error occurs.
     */
    Response.Builder readResponseHeaders(boolean expectContinue) throws IOException;

    /**
     * Returns the reported content length of the response.
     *
     * @param response The response.
     * @return The reported content length.
     * @throws IOException if an I/O error occurs.
     */
    long reportedContentLength(Response response) throws IOException;

    /**
     * Opens a source to read the response body.
     *
     * @param response The response.
     * @return A source for the response body.
     * @throws IOException if an I/O error occurs.
     */
    Source openResponseBodySource(Response response) throws IOException;

    /**
     * Returns the trailer headers after the HTTP response.
     *
     * @return The trailer headers.
     * @throws IOException if an I/O error occurs.
     */
    Headers trailers() throws IOException;

    /**
     * Cancels this stream. Resources held by this stream will be cleaned up, though not necessarily synchronously. This
     * may happen after the connection pool thread.
     */
    void cancel();

}
