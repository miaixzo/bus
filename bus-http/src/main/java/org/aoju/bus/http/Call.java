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
package org.aoju.bus.http;

import org.aoju.bus.core.io.segment.Timeout;
import org.aoju.bus.http.bodys.ResponseBody;

import java.io.IOException;

/**
 * A call is a request that has been prepared for execution. A call can be canceled. As this object
 * represents a single request/response pair (stream), it cannot be executed twice.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface Call extends Cloneable {

    /**
     * @return the original request that initiated this call.
     */
    Request request();

    /**
     * Invokes the request immediately, and blocks until the response can be processed or is in
     * error.
     *
     * <p>To avoid leaking resources callers should close the {@link Response} which in turn will
     * close the underlying {@link ResponseBody}.
     *
     * <pre>{@code
     *
     *   // ensure the response (and underlying response body) is closed
     *   try (Response response = client.newCall(request).execute()) {
     *     ...
     *   }
     *
     * }</pre>
     *
     * <p>The caller may read the response body with the response's {@link Response#body} method. To
     * avoid leaking resources callers must {@linkplain ResponseBody close the response body} or the
     * Response.
     *
     * <p>Note that transport-layer success (receiving a HTTP response code, headers and body) does
     * not necessarily indicate application-layer success: {@code response} may still indicate an
     * unhappy HTTP response code like 404 or 500.
     *
     * @return Response
     * @throws IOException if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout. Because networks can fail during an exchange, it is possible that the
     *                     remote server accepted the request before the failure.
     */
    Response execute() throws IOException;

    /**
     * Schedules the request to be executed at some point in the future.
     *
     * <p>The {@link Client#dispatcher dispatcher} defines when the request will run: usually
     * immediately unless there are several other requests currently being executed.
     *
     * <p>This client will later call back {@code responseCallback} with either an HTTP response or a
     * failure exception.
     *
     * @param responseCallback Callback
     */
    void enqueue(Callback responseCallback);

    /**
     * Cancels the request, if possible. Requests that are already complete cannot be canceled.
     */
    void cancel();

    /**
     * Returns true if this call has been either {@linkplain #execute() executed} or {@linkplain
     * #enqueue(Callback) enqueued}. It is an error to execute a call more than once.
     *
     * @return the boolean
     */
    boolean isExecuted();

    boolean isCanceled();

    /**
     * Returns a timeout that spans the entire call: resolving DNS, connecting, writing the request
     * body, server processing, and reading the response body. If the call requires redirects or
     * retries all must complete within one timeout period.
     *
     * <p>Configure the client's default timeout with {@link Client.Builder#callTimeout}.
     *
     * @return the Timeout
     */
    Timeout timeout();

    /**
     * Create a new, identical call to this one which can be enqueued or executed even if this call
     * has already been.
     *
     * @return the Call
     */
    Call clone();

    interface Factory {
        Call newCall(Request request);
    }

}
