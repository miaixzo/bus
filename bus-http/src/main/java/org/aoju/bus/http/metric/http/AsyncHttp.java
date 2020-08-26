/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.*;
import org.aoju.bus.http.Results.State;
import org.aoju.bus.http.magic.RealResult;
import org.aoju.bus.http.metric.TaskExecutor;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 异步 Http 请求任务
 *
 * @author Kimi Liu
 * @version 6.0.8
 * @since JDK 1.8+
 */
public class AsyncHttp extends CoverHttp<AsyncHttp> {

    private OnBack<Results> onResponse;
    private OnBack<IOException> onException;
    private OnBack<State> onComplete;
    private boolean rOnIO;
    private boolean eOnIO;
    private boolean cOnIO;

    public AsyncHttp(Httpv htttpv, String url) {
        super(htttpv, url);
    }

    /**
     * 设置请求执行异常后的回调函数，设置后，相关异常将不再向上抛出
     *
     * @param onException 请求异常回调
     * @return AsyncHttp 实例
     */
    public AsyncHttp setOnException(OnBack<IOException> onException) {
        this.onException = onException;
        eOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * 设置请求执行完成后的回调函数，无论成功|失败|异常 都会被执行
     *
     * @param onComplete 请求完成回调
     * @return AsyncHttp 实例
     */
    public AsyncHttp setOnComplete(OnBack<State> onComplete) {
        this.onComplete = onComplete;
        cOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * 设置请求得到响应后的回调函数
     *
     * @param onResponse 请求响应回调
     * @return AsyncHttp 实例
     */
    public AsyncHttp setOnResponse(OnBack<Results> onResponse) {
        this.onResponse = onResponse;
        rOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * 发起 GET 请求（Rest：读取资源，幂等）
     *
     * @return GiveCall
     */
    public GiveCall get() {
        return request(Http.GET);
    }

    /**
     * 发起 HEAD 请求（Rest：读取资源头信息，幂等）
     *
     * @return GiveCall
     */
    public GiveCall head() {
        return request(Http.HEAD);
    }

    /**
     * 发起 POST 请求（Rest：创建资源，非幂等）
     *
     * @return GiveCall
     */
    public GiveCall post() {
        return request(Http.POST);
    }

    /**
     * 发起 PUT 请求（Rest：更新资源，幂等）
     *
     * @return GiveCall
     */
    public GiveCall put() {
        return request(Http.PUT);
    }

    /**
     * 发起 PATCH 请求（Rest：更新资源，部分更新，幂等）
     *
     * @return GiveCall
     */
    public GiveCall patch() {
        return request(Http.PATCH);
    }

    /**
     * 发起 DELETE 请求（Rest：删除资源，幂等）
     *
     * @return GiveCall
     */
    public GiveCall delete() {
        return request(Http.DELETE);
    }

    /**
     * 发起 HTTP 请求
     *
     * @param method 请求方法
     * @return GiveCall
     */
    public GiveCall request(String method) {
        if (method == null || method.isEmpty()) {
            throw new IllegalArgumentException("Request method method cannot be empty!");
        }
        PreGiveCall call = new PreGiveCall();
        registeTagTask(call);
        httpClient.preprocess(this, () -> {
            synchronized (call) {
                if (call.canceled) {
                    removeTagTask();
                } else {
                    call.setCall(executeCall(prepareCall(method)));
                }
            }
        }, skipPreproc, skipSerialPreproc);
        return call;
    }

    private GiveCall executeCall(NewCall call) {
        OkGiveCall httpCall = new OkGiveCall(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(NewCall call, IOException error) {
                State state = toState(error);
                Results result = new RealResult(AsyncHttp.this, state, error);
                onCallback(httpCall, result, () -> {
                    TaskExecutor executor = httpClient.executor();
                    executor.executeOnComplete(AsyncHttp.this, onComplete, state, cOnIO);
                    if (!executor.executeOnException(AsyncHttp.this, onException, error, eOnIO)
                            && !nothrow) {
                        throw new InstrumentException(error.getMessage(), error);
                    }
                });
            }

            @Override
            public void onResponse(NewCall call, Response response) {
                TaskExecutor executor = httpClient.executor();
                Results result = new RealResult(AsyncHttp.this, response, executor);
                onCallback(httpCall, result, () -> {
                    executor.executeOnComplete(AsyncHttp.this, onComplete, State.RESPONSED, cOnIO);
                    executor.executeOnResponse(AsyncHttp.this, onResponse, result, rOnIO);
                });
            }

        });
        return httpCall;
    }

    private void onCallback(OkGiveCall httpCall, Results result, Runnable runnable) {
        synchronized (httpCall) {
            removeTagTask();
            if (httpCall.isCanceled() || result.getState() == State.CANCELED) {
                httpCall.setResult(new RealResult(AsyncHttp.this, State.CANCELED));
                return;
            }
            httpCall.setResult(result);
            runnable.run();
        }
    }

    class PreGiveCall implements GiveCall {

        GiveCall call;
        boolean canceled = false;
        CountDownLatch latch = new CountDownLatch(1);

        @Override
        public synchronized boolean cancel() {
            canceled = call == null || call.cancel();
            latch.countDown();
            return canceled;
        }

        @Override
        public boolean isDone() {
            if (call != null) {
                return call.isDone();
            }
            return canceled;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        void setCall(GiveCall call) {
            this.call = call;
            latch.countDown();
        }

        @Override
        public Results getResult() {
            if (!timeoutAwait(latch)) {
                cancel();
                return timeoutResult();
            }
            if (canceled || call == null) {
                return new RealResult(AsyncHttp.this, State.CANCELED);
            }
            return call.getResult();
        }

    }

    class OkGiveCall implements GiveCall {

        NewCall call;
        Results result;
        CountDownLatch latch = new CountDownLatch(1);

        OkGiveCall(NewCall call) {
            this.call = call;
        }

        @Override
        public synchronized boolean cancel() {
            if (result == null) {
                call.cancel();
                return true;
            }
            return false;
        }

        @Override
        public boolean isDone() {
            return result != null;
        }

        @Override
        public boolean isCanceled() {
            return call.isCanceled();
        }

        @Override
        public Results getResult() {
            if (result == null) {
                if (!timeoutAwait(latch)) {
                    cancel();
                    return timeoutResult();
                }
            }
            return result;
        }

        void setResult(Results result) {
            this.result = result;
            latch.countDown();
        }

    }

}
