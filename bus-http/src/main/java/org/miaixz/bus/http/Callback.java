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
package org.miaixz.bus.http;

import java.io.IOException;

/**
 * A generic callback interface for asynchronous operations.
 * <p>
 * This interface defines callback methods for handling successful and failed operations, particularly for HTTP
 * requests, enabling asynchronous response processing. Implementations must ensure that resources like the response
 * body are properly closed and handle potential exceptions.
 *
 * @param <T> The type of the data passed to the generic {@link #on(Object)} callback.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Callback<T> {

    /**
     * Called when the request could not be executed due to cancellation, a connectivity problem, or a timeout.
     * <p>
     * Because networks can fail during an exchange, it is possible that the remote server accepted the request before
     * the failure.
     * </p>
     *
     * @param call The call that was executed.
     * @param ex   The exception that occurred.
     */
    default void onFailure(NewCall call, IOException ex) {
    }

    /**
     * Called when the request fails, including a request ID to differentiate multiple requests. This is an alternative
     * to {@link #onFailure(NewCall, IOException)} for more specific use cases.
     *
     * @param newCall   The call that was executed.
     * @param exception The exception that occurred.
     * @param id        The request identifier.
     */
    default void onFailure(NewCall newCall, Exception exception, String id) {
    }

    /**
     * Called when the remote server returns an HTTP response.
     * <p>
     * Implementations must close the {@link Response#body} to release resources. Note that a transport-level success
     * (e.g., a 200 OK) does not guarantee an application-level success (e.g., a 404 or 500 status code).
     * </p>
     *
     * @param call     The call that was executed.
     * @param response The response from the server.
     * @throws IOException If an error occurs while processing the response.
     */
    default void onResponse(NewCall call, Response response) throws IOException {
    }

    /**
     * Called when the request is successful, including a request ID to differentiate multiple requests. This is an
     * alternative to {@link #onResponse(NewCall, Response)} for more specific use cases.
     *
     * @param newCall  The call that was executed.
     * @param response The response from the server.
     * @param id       The request identifier.
     */
    default void onResponse(NewCall newCall, Response response, String id) {
    }

    /**
     * A generic data callback for handling specific types of callback data, such as STOMP messages or other
     * asynchronous events.
     *
     * @param data The callback data.
     */
    default void on(T data) {
    }

}
