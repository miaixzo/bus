/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.magic;

import org.aoju.bus.http.Httpd;
import org.aoju.bus.http.NewCall;
import org.aoju.bus.http.Request;

import java.io.IOException;

/**
 * 请求调用者
 *
 * @author Kimi Liu
 * @version 6.3.2
 * @since JDK 1.8+
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

    public NewCall buildCall(AbsCallback absCallback) {
        request = createRequest(absCallback);
        newCall = httpd.newCall(request);
        return newCall;
    }

    private Request createRequest(AbsCallback absCallback) {
        return httpRequest.createRequest(absCallback);
    }

    public HttpResponse execute() throws Exception {
        buildCall(null);
        try {
            HttpResponse rsp = new HttpResponse(newCall.execute());
            if (rsp.isSuccessful()) {
                HttpxStat.onReqSuccess();
            } else {
                HttpxStat.onReqFailure(newCall.request().url().toString(), null);
            }
            return rsp;
        } catch (Exception e) {
            HttpxStat.onReqFailure(newCall.request().url().toString(), e);
            throw e;
        }
    }

    public void executeAsync(AbsCallback absCallback) {
        buildCall(absCallback);
        execute(this, absCallback);
    }

    private void execute(final RequestCall requestCall, AbsCallback absCallback) {
        final AbsCallback finalAbsCallback = absCallback;
        final String id = requestCall.getHttpRequest().getId();
        requestCall.getNewCall().enqueue(new org.aoju.bus.http.Callback() {
            @Override
            public void onFailure(NewCall newCall, final IOException e) {
                HttpxStat.onReqFailure(newCall.request().url().toString(), e);
                if (null != finalAbsCallback) {
                    finalAbsCallback.onFailure(newCall, e, id);
                }
            }

            @Override
            public void onResponse(final NewCall newCall, final org.aoju.bus.http.Response response) {
                HttpxStat.onReqSuccess();
                if (null != finalAbsCallback) {
                    finalAbsCallback.onResponse(newCall, new HttpResponse(response), id);
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
