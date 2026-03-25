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
package org.miaixz.bus.http;

import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.http.bodys.ResponseBody;

import java.io.IOException;

/**
 * A call is a request that has been prepared for execution. A call can be canceled. As this object represents a single
 * request/response pair (stream), it cannot be executed twice.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface NewCall extends Cloneable {

    /**
     * Returns the original request that initiated this call.
     *
     * @return The original request.
     */
    Request request();

    /**
     * Invokes the request immediately, and blocks until the response can be processed or is in error.
     *
     * <p>
     * The caller can read the response body with the {@link Response#body} method. To avoid resource leaks, the caller
     * must {@linkplain ResponseBody close the response body} or the response.
     *
     * <p>
     * Note that transport-layer success (receiving a HTTP response code, headers and body) does not necessarily
     * indicate application-layer success: {@code response} may still indicate an unhappy HTTP response code like 404 or
     * 500.
     *
     * @return The response to the request.
     * @throws IOException           if the request could not be executed due to cancellation, a connectivity problem or
     *                               timeout. Because networks can fail during an exchange, it is possible that the
     *                               remote server accepted the request before the failure.
     * @throws IllegalStateException when the call has already been executed.
     */
    Response execute() throws IOException;

    /**
     * Schedules the request to be executed at some point in the future.
     *
     * <p>
     * The {@link Httpd#dispatcher dispatcher} defines when the request will run: usually immediately unless there are
     * several other requests currently being executed.
     *
     * <p>
     * This client will later call back {@code responseCallback} with either an HTTP response or a failure exception.
     *
     * @param callback The asynchronous callback.
     * @throws IllegalStateException when the call has already been executed.
     */
    void enqueue(Callback callback);

    /**
     * Cancels the request, if possible. Requests that are already complete cannot be canceled.
     */
    void cancel();

    /**
     * Returns true if this call has been either {@linkplain #execute() executed} or {@linkplain #enqueue(Callback)
     * enqueued}. It is an error to execute a call more than once.
     *
     * @return true if this call has been executed or enqueued.
     */
    boolean isExecuted();

    /**
     * Returns true if this call has been canceled.
     *
     * @return true if this call has been canceled.
     */
    boolean isCanceled();

    /**
     * Returns a timeout that spans the entire call: resolving DNS, connecting, writing the request body, server
     * processing, and reading the response body. If the call requires redirects or retries all must complete within one
     * timeout period.
     *
     * <p>
     * Configure the client's default timeout using {@link Httpd.Builder#callTimeout}.
     *
     * @return The timeout for the entire call.
     */
    Timeout timeout();

    /**
     * Creates a new, identical call to this one which can be enqueued or executed even if this call has already been.
     *
     * @return A new call that is a clone of this call.
     */
    NewCall clone();

    /**
     * A factory for creating {@link NewCall} instances.
     */
    interface Factory {

        /**
         * Creates a new call for the given request.
         *
         * @param request The network request information.
         * @return A new call instance.
         */
        NewCall newCall(Request request);

    }

}
