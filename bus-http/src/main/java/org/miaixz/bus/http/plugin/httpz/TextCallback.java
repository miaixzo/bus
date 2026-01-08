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
