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
package org.miaixz.bus.http.plugin.httpz;

import java.io.IOException;

import org.miaixz.bus.http.*;

/**
 * Represents an executable HTTP request. This class is responsible for building the final {@link Request} object from
 * an {@link HttpRequest} and executing it either synchronously or asynchronously.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RequestCall {

    /**
     * The HTTP client used to execute the call.
     */
    private final Httpd httpd;
    /**
     * The abstract representation of the HTTP request.
     */
    private final HttpRequest httpRequest;
    /**
     * The final, built Httpd Request object.
     */
    private Request request;
    /**
     * The underlying Httpd Call object.
     */
    private NewCall newCall;

    /**
     * Constructs a new RequestCall.
     *
     * @param request The abstract {@link HttpRequest} configuration.
     * @param httpd   The {@link Httpd} client instance.
     */
    public RequestCall(HttpRequest request, Httpd httpd) {
        this.httpRequest = request;
        this.httpd = httpd;
    }

    /**
     * Builds the underlying {@link NewCall} from the {@link HttpRequest} configuration.
     *
     * @param callback The callback to be used for the request.
     * @return The prepared {@link NewCall}.
     */
    public NewCall buildCall(Callback callback) {
        request = createRequest(callback);
        newCall = httpd.newCall(request);
        return newCall;
    }

    /**
     * A helper method to create the final {@link Request} object.
     *
     * @param callback The callback for the request.
     * @return The constructed {@link Request}.
     */
    private Request createRequest(Callback callback) {
        return httpRequest.createRequest(callback);
    }

    /**
     * Executes the HTTP request synchronously.
     *
     * @return The HTTP {@link Response}.
     * @throws Exception if an error occurs during execution.
     */
    public Response execute() throws Exception {
        buildCall(null);
        try {
            Response response = newCall.execute();
            if (response.isSuccessful()) {
                HttpzState.onReqSuccess();
            } else {
                HttpzState.onReqFailure(newCall.request().url().toString(), null);
            }
            return response;
        } catch (Exception e) {
            HttpzState.onReqFailure(newCall.request().url().toString(), e);
            throw e;
        }
    }

    /**
     * Executes the HTTP request asynchronously.
     *
     * @param callback The callback to handle the response or failure.
     */
    public void executeAsync(Callback callback) {
        buildCall(callback);
        execute(this, callback);
    }

    /**
     * A helper method to enqueue the call and wrap the callback to handle statistics.
     *
     * @param requestCall The RequestCall to execute.
     * @param callback    The user-provided callback.
     */
    private void execute(final RequestCall requestCall, Callback callback) {
        final String id = requestCall.getHttpRequest().getId();
        requestCall.getNewCall().enqueue(new Callback() {

            @Override
            public void onFailure(NewCall newCall, final IOException e) {
                HttpzState.onReqFailure(newCall.request().url().toString(), e);
                if (null != callback) {
                    callback.onFailure(newCall, e, id);
                }
            }

            @Override
            public void onResponse(final NewCall newCall, final Response response, String id) {
                HttpzState.onReqSuccess();
                if (null != callback) {
                    callback.onResponse(newCall, response, id);
                }
            }
        });

    }

    /**
     * @return The underlying Httpd {@link NewCall} instance.
     */
    public NewCall getNewCall() {
        return newCall;
    }

    /**
     * @return The final, built Httpd {@link Request} object.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * @return The abstract {@link HttpRequest} configuration.
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * Cancels the request if it is in flight.
     */
    public void cancel() {
        if (null != newCall) {
            newCall.cancel();
        }
    }

}
