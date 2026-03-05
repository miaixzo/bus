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

import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.NewCall;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;

/**
 * An abstract {@link Callback} implementation for handling responses where the body is expected to be a text string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class TextCallback implements Callback {

    /**
     * Handles the successful HTTP response by consuming the response body as a string and passing it to the
     * {@link #onSuccess(NewCall, String, String)} method.
     *
     * @param call     The {@link NewCall} that resulted in this response.
     * @param response The HTTP {@link Response}.
     * @param id       The unique ID of the request.
     */
    @Override
    public void onResponse(NewCall call, Response response, String id) {
        try {
            onSuccess(call, response.body().string(), id);
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    /**
     * Handles request failures by logging the error.
     *
     * @param call The {@link NewCall} that failed.
     * @param e    The {@link Exception} that caused the failure.
     * @param id   The unique ID of the request.
     */
    @Override
    public void onFailure(NewCall call, Exception e, String id) {
        Logger.error("onFailure id:{}", id);
        Logger.error(e.getMessage(), e);
    }

    /**
     * Abstract callback method invoked when a successful response with a text body is received. Subclasses must
     * implement this method to handle the response string.
     *
     * @param call     The {@link NewCall} that resulted in this response.
     * @param response The response body as a string.
     * @param id       The unique ID of the request.
     */
    public abstract void onSuccess(NewCall call, String response, String id);

}
