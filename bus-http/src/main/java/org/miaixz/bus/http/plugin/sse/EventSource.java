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

import org.miaixz.bus.http.Request;

/**
 * 服务器推送事件（Server-Sent Events, SSE）源接口，定义了与事件源交互的基本操作。 实现此接口的类负责管理 SSE 连接，包括获取原始请求和取消连接。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface EventSource {

    /**
     * 返回发起事件源的原始请求。
     *
     * @return 原始的 HTTP 请求
     */
    Request request();

    /**
     * 立即且强制性地释放事件源持有的资源。如果事件源已被关闭或取消，则此方法无效。
     */
    void cancel();

    /**
     * 事件源工厂接口，用于创建新的 {@link EventSource} 实例。
     */
    interface Factory {
        /**
         * 创建并立即返回一个新的事件源。创建事件源会启动一个异步过程来连接服务器。 连接成功或失败时，将通知监听器。调用者必须在不再使用返回的事件源时取消它。
         *
         * @param request  用于发起事件源的 HTTP 请求
         * @param listener 事件源监听器，用于接收连接状态和事件数据
         * @return 新创建的 {@link EventSource} 实例
         */
        EventSource newEventSource(Request request, EventSourceListener listener);
    }

}