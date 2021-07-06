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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.Httpv;
import org.aoju.bus.http.NewCall;
import org.aoju.bus.http.Results;
import org.aoju.bus.http.Results.State;
import org.aoju.bus.http.magic.RealResult;
import org.aoju.bus.http.metric.Cancelable;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 同步 Http 请求任务
 *
 * @author Kimi Liu
 * @version 6.2.5
 * @since JDK 1.8+
 */
public class SyncHttp extends CoverHttp<SyncHttp> {

    public SyncHttp(Httpv client, String url) {
        super(client, url);
    }

    /**
     * 发起 GET 请求（Rest：获取资源，幂等）
     *
     * @return 请求结果
     */
    public Results get() {
        return request(Http.GET);
    }

    /**
     * 发起 HEAD 请求（Rest：读取资源头信息，幂等）
     *
     * @return 请求结果
     */
    public Results head() {
        return request(Http.HEAD);
    }

    /**
     * 发起 POST 请求（Rest：创建资源，非幂等）
     *
     * @return 请求结果
     */
    public Results post() {
        return request(Http.POST);
    }

    /**
     * 发起 PUT 请求（Rest：更新资源，幂等）
     *
     * @return 请求结果
     */
    public Results put() {
        return request(Http.PUT);
    }

    /**
     * 发起 PATCH 请求（Rest：更新资源，部分更新，幂等）
     *
     * @return HttpCall
     */
    public Results patch() {
        return request(Http.PATCH);
    }

    /**
     * 发起 DELETE 请求（Rest：删除资源，幂等）
     *
     * @return 请求结果
     */
    public Results delete() {
        return request(Http.DELETE);
    }

    /**
     * 发起 HTTP 请求
     *
     * @param method 请求方法
     * @return 请求结果
     */
    public Results request(String method) {
        if (null == method || method.isEmpty()) {
            throw new IllegalArgumentException("Request method method cannot be empty!");
        }
        RealResult result = new RealResult(this, httpv.executor());
        SyncHttpCall httpCall = new SyncHttpCall();
        // 注册标签任务
        registeTagTask(httpCall);
        CountDownLatch latch = new CountDownLatch(1);
        httpv.preprocess(this, () -> {
            synchronized (httpCall) {
                if (httpCall.canceled) {
                    result.exception(State.CANCELED, null);
                    latch.countDown();
                    return;
                }
                httpCall.call = prepareCall(method);
            }
            try {
                result.response(httpCall.call.execute());
                httpCall.done = true;
            } catch (IOException e) {
                result.exception(toState(e), e);
            } finally {
                latch.countDown();
            }
        }, skipPreproc, skipSerialPreproc);
        boolean timeout = false;
        if (null == result.getState()) {
            timeout = !timeoutAwait(latch);
        }
        // 移除标签任务
        removeTagTask();
        if (timeout) {
            httpCall.cancel();
            return timeoutResult();
        }
        IOException e = result.getError();
        State state = result.getState();
        if (null != e && state != State.CANCELED
                && !nothrow) {
            throw new InstrumentException("Abnormal execution", e);
        }
        return result;
    }


    static class SyncHttpCall implements Cancelable {

        NewCall call;
        boolean done = false;
        boolean canceled = false;

        @Override
        public synchronized boolean cancel() {
            if (done) {
                return false;
            }
            if (null != call) {
                call.cancel();
            }
            canceled = true;
            return true;
        }

    }

}
