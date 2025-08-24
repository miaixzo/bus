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

import org.miaixz.bus.http.Response;

/**
 * 服务器推送事件（Server-Sent Events, SSE）监听器抽象类，定义了处理事件源生命周期和事件的回调方法。 子类可重写这些方法以响应事件源的打开、事件接收、关闭或失败等状态。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class EventSourceListener {

    /**
     * 当事件源被远程服务器接受并可以开始传输事件时调用。
     *
     * @param eventSource 事件源实例
     * @param response    服务器返回的 HTTP 响应
     */
    public void onOpen(EventSource eventSource, Response response) {

    }

    /**
     * 当接收到新的服务器推送事件时调用。
     *
     * @param eventSource 事件源实例
     * @param id          事件 ID，可能为 null
     * @param type        事件类型，可能为 null
     * @param data        事件数据
     */
    public void onEvent(EventSource eventSource, String id, String type, String data) {

    }

    /**
     * 当事件源正常关闭时调用。 此后不会再次调用该监听器的任何方法。
     *
     * @param eventSource 事件源实例
     */
    public void onClosed(EventSource eventSource) {

    }

    /**
     * 当事件源因网络读写错误而关闭时调用。可能丢失部分传入事件。 此后不会再次调用该监听器的任何方法。
     *
     * @param eventSource 事件源实例
     * @param throwable   发生的异常，可能为 null
     * @param response    服务器返回的 HTTP 响应，可能为 null
     */
    public void onFailure(EventSource eventSource, Throwable throwable, Response response) {

    }

}