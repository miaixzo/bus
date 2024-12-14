/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.io.IOException;

import org.miaixz.bus.http.*;

/**
 * 请求调用者
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RequestCall {

    private Httpd httpd;
    private HttpRequest httpRequest;
    private Request request;
    private NewCall newCall;

    public RequestCall(HttpRequest request, Httpd httpd) {
        this.httpRequest = request;
        this.httpd = httpd;
    }

    public NewCall buildCall(Callback callback) {
        request = createRequest(callback);
        newCall = httpd.newCall(request);
        return newCall;
    }

    private Request createRequest(Callback callback) {
        return httpRequest.createRequest(callback);
    }

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

    public void executeAsync(Callback callback) {
        buildCall(callback);
        execute(this, callback);
    }

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
            public void onResponse(final NewCall newCall, final Response response) {
                HttpzState.onReqSuccess();
                if (null != callback) {
                    callback.onResponse(newCall, response, id);
                }
            }
        });

    }

    public NewCall getNewCall() {
        return newCall;
    }

    public Request getRequest() {
        return request;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void cancel() {
        if (null != newCall) {
            newCall.cancel();
        }
    }

}
