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
