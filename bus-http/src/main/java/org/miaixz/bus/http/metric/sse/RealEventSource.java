/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.metric.sse;

import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.plugin.sse.EventSource;
import org.miaixz.bus.http.plugin.sse.EventSourceListener;

import java.io.IOException;

/**
 * 实现服务器推送事件（Server-Sent Events, SSE）的核心类，负责与服务器建立连接并处理事件流。 实现 {@link EventSource} 接口以提供请求和取消功能，实现
 * {@link ServerSentEventReader.Callback} 接口以处理事件数据，实现 {@link Callback} 接口以处理 HTTP 响应。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RealEventSource implements EventSource, ServerSentEventReader.Callback, Callback {

    /**
     * 发起事件源的原始 HTTP 请求
     */
    private final Request request;

    /**
     * 监听事件源生命周期和事件的监听器
     */
    private final EventSourceListener listener;

    /**
     * 用于执行 HTTP 请求的调用对象，可能为 null
     */
    private NewCall call;

    /**
     * 标记事件源是否已被取消
     */
    private boolean canceled;

    /**
     * 构造一个新的 {@code RealEventSource} 实例。
     *
     * @param request  发起事件源的 HTTP 请求
     * @param listener 事件源监听器，用于接收事件和状态更新
     */
    public RealEventSource(Request request, EventSourceListener listener) {
        this.request = request;
        this.listener = listener;
        this.call = null;
        this.canceled = false;
    }

    /**
     * 使用指定的调用工厂建立事件源连接。 创建一个新的 HTTP 调用并异步执行，触发 {@link Callback} 的回调方法。
     *
     * @param callFactory 用于创建 HTTP 调用的工厂
     */
    public void connect(NewCall.Factory callFactory) {
        call = callFactory.newCall(request);
        call.enqueue(this);
    }

    /**
     * 处理 HTTP 响应，调用 {@link #processResponse(Response)} 方法进行具体处理。
     *
     * @param call     发起请求的调用对象
     * @param response 服务器返回的 HTTP 响应
     */
    @Override
    public void onResponse(NewCall call, Response response) {
        processResponse(response);
    }

    /**
     * 处理服务器推送事件的响应，解析事件流并触发监听器回调。 验证响应状态和内容类型，处理事件流数据，并在完成后关闭连接。
     *
     * @param response 服务器返回的 HTTP 响应
     */
    public void processResponse(Response response) {
        try (Response ignored = response) {
            if (!response.isSuccessful()) {
                listener.onFailure(this, null, response);
                return;
            }

            ResponseBody body = response.body();
            if (body == null) {
                listener.onFailure(this, new IllegalStateException("Response body is null"), response);
                return;
            }

            if (!isEventStream(body)) {
                listener.onFailure(
                        this,
                        new IllegalStateException("Invalid content-type: " + body.contentType()),
                        response);
                return;
            }

            // 这是一个长期连接的响应，取消整个调用的超时
            if (call instanceof RealCall) {
                ((RealCall) call).timeoutEarlyExit();
            }

            // 用空响应体替换原始响应体，防止回调访问实际数据
            Response modifiedResponse = response.newBuilder().body(Builder.EMPTY_RESPONSE).build();

            ServerSentEventReader reader = new ServerSentEventReader(body.source(), this);
            try {
                listener.onOpen(this, modifiedResponse);
                while (reader.processNextEvent()) {
                    // 持续处理事件
                }
            } catch (Exception e) {
                listener.onFailure(this, e, modifiedResponse);
                return;
            }
            listener.onClosed(this);
        }
    }

    /**
     * 检查响应体是否为服务器推送事件流（Content-Type: text/event-stream）。
     *
     * @param body 响应体
     * @return 如果是事件流返回 true，否则返回 false
     */
    private boolean isEventStream(ResponseBody body) {
        if (body.contentType() == null) {
            return false;
        }
        return "text".equals(body.contentType().type()) && "event-stream".equals(body.contentType().subtype());
    }

    /**
     * 处理 HTTP 请求失败的情况，通知监听器。
     *
     * @param call 发起请求的调用对象
     * @param e    发生的 I/O 异常
     */
    @Override
    public void onFailure(NewCall call, IOException e) {
        listener.onFailure(this, e, null);
    }

    /**
     * 返回发起事件源的原始请求。
     *
     * @return 原始 HTTP 请求
     */
    @Override
    public Request request() {
        return request;
    }

    /**
     * 取消事件源连接，仅在未取消且调用对象存在时执行。
     */
    @Override
    public void cancel() {
        if (call != null && !canceled) {
            canceled = true;
            call.cancel();
        }
    }

    /**
     * 处理接收到的事件数据，通知监听器。
     *
     * @param id   事件 ID，可能为 null
     * @param type 事件类型，可能为 null
     * @param data 事件数据
     */
    @Override
    public void onEvent(String id, String type, String data) {
        listener.onEvent(this, id, type, data);
    }

    /**
     * 处理重试时间的变化（retry 字段），当前实现忽略自动重试。
     *
     * @param timeMs 重试时间（毫秒）
     */
    @Override
    public void onRetryChange(long timeMs) {
        // 忽略，不执行自动重试
    }

}
