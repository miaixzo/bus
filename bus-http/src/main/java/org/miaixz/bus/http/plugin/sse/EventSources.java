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
package org.miaixz.bus.http.plugin.sse;

import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.metric.sse.RealEventSource;
import org.miaixz.bus.http.metric.sse.ServerSentEventReader;

/**
 * 服务器推送事件（Server-Sent Events, SSE）的工具类，提供创建事件源工厂和处理事件响应的静态方法。 用于简化 SSE 连接的创建和事件流解析。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class EventSources {

    /**
     * 使用提供的调用工厂创建事件源工厂。
     *
     * @param callFactory 用于创建 HTTP 调用的工厂
     * @return 事件源工厂，用于生成 {@link EventSource} 实例
     */
    public static EventSource.Factory createFactory(NewCall.Factory callFactory) {
        return new FactoryImpl(callFactory);
    }

    /**
     * 处理服务器推送事件的响应，通知监听器处理事件或失败。 验证响应状态和内容类型，解析事件流，并触发监听器的相应回调。
     *
     * @param response 服务器返回的 HTTP 响应
     * @param listener 事件监听器，用于接收事件和状态更新
     */
    public static void processResponse(Response response, EventSourceListener listener) {
        try (Response ignored = response) {
            if (!response.isSuccessful()) {
                listener.onFailure(null, null, response);
                return;
            }

            ResponseBody body = response.body();
            if (body == null) {
                listener.onFailure(null, new IllegalStateException("Response body is null"), response);
                return;
            }

            if (!isEventStream(body)) {
                listener.onFailure(null, new IllegalStateException("Invalid content-type: " + body.contentType()),
                        response);
                return;
            }

            // 用空响应体替换原始响应体，防止回调访问实际数据
            Response modifiedResponse = response.newBuilder().body(Builder.EMPTY_RESPONSE).build();

            ServerSentEventReader reader = new ServerSentEventReader(body.source(),
                    new ServerSentEventReader.Callback() {
                        @Override
                        public void onEvent(String id, String type, String data) {
                            listener.onEvent(null, id, type, data);
                        }

                        @Override
                        public void onRetryChange(long timeMs) {
                            // 忽略，不执行自动重试
                        }
                    });

            try {
                listener.onOpen(null, modifiedResponse);
                while (reader.processNextEvent()) {
                    // 持续处理事件
                }
            } catch (Exception e) {
                listener.onFailure(null, e, modifiedResponse);
                return;
            }
            listener.onClosed(null);
        }
    }

    /**
     * 检查响应体是否为服务器推送事件流（Content-Type: text/event-stream）。
     *
     * @param body 响应体
     * @return 如果是事件流返回 true，否则返回 false
     */
    private static boolean isEventStream(ResponseBody body) {
        if (body.contentType() == null) {
            return false;
        }
        return "text".equals(body.contentType().type()) && "event-stream".equals(body.contentType().subtype());
    }

    /**
     * 事件源工厂实现类，负责创建 {@link RealEventSource} 实例并发起连接。
     */
    private static class FactoryImpl implements EventSource.Factory {
        /**
         * 用于创建 HTTP 调用的工厂
         */
        private final NewCall.Factory callFactory;

        /**
         * 构造工厂实例。
         *
         * @param callFactory 用于创建 HTTP 调用的工厂
         */
        FactoryImpl(NewCall.Factory callFactory) {
            this.callFactory = callFactory;
        }

        /**
         * 创建新的 {@link EventSource} 实例并启动异步连接。
         *
         * @param request  用于发起事件源的 HTTP 请求
         * @param listener 事件监听器，用于接收事件和状态更新
         * @return 新创建的 {@link EventSource} 实例
         */
        @Override
        public EventSource newEventSource(Request request, EventSourceListener listener) {
            RealEventSource eventSource = new RealEventSource(request, listener);
            eventSource.connect(callFactory);
            return eventSource;
        }
    }

}