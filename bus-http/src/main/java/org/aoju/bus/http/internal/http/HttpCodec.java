package org.aoju.bus.http.internal.http;

import org.aoju.bus.core.io.Sink;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.ResponseBody;

import java.io.IOException;

/**
 * Encodes HTTP requests and decodes HTTP responses.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface HttpCodec {
    /**
     * The timeout to use while discarding a stream of input data. Since this is used for connection
     * reuse, this timeout should be significantly less than the time it takes to establish a new
     * connection.
     */
    int DISCARD_STREAM_TIMEOUT_MILLIS = 100;

    /**
     * Returns an output stream where the request body can be streamed.
     */
    Sink createRequestBody(Request request, long contentLength);

    /**
     * This should update the HTTP engine's sentRequestMillis field.
     */
    void writeRequestHeaders(Request request) throws IOException;

    /**
     * Flush the request to the underlying socket.
     */
    void flushRequest() throws IOException;

    /**
     * Flush the request to the underlying socket and signal no more bytes will be transmitted.
     */
    void finishRequest() throws IOException;

    /**
     * Parses bytes of a response header from an HTTP transport.
     *
     * @param expectContinue true to return null if this is an intermediate response with a "100"
     *                       response code. Otherwise this method never returns null.
     */
    Response.Builder readResponseHeaders(boolean expectContinue) throws IOException;

    /**
     * Returns a stream that reads the response body.
     */
    ResponseBody openResponseBody(Response response) throws IOException;

    /**
     * Cancel this stream. Resources held by this stream will be cleaned up, though not synchronously.
     * That may happen later by the connection pool thread.
     */
    void cancel();
}
