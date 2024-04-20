/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.http.socket;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.http.Response;

/**
 * web socket 监听器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class WebSocketListener {

    /**
     * 当web socket被远程对等方接受并可以开始传输消息时调用
     *
     * @param webSocket 当前web socket
     * @param response  当前响应体
     */
    public void onOpen(WebSocket webSocket, Response response) {

    }

    /**
     * 当收到文本(类型为{@code 0x1})消息时调用
     *
     * @param webSocket 当前web socket
     * @param text      文本内容
     */
    public void onMessage(WebSocket webSocket, String text) {

    }

    /**
     * 当接收到二进制(类型为{@code 0x2})消息时调用
     *
     * @param webSocket 当前web socket
     * @param bytes     二进制内容
     */
    public void onMessage(WebSocket webSocket, ByteString bytes) {

    }

    /**
     * 当远程对等点指示不再传输传入消息时调用
     *
     * @param webSocket 当前web socket
     * @param code      状态码
     * @param reason    关闭终止原因
     */
    public void onClosing(WebSocket webSocket, int code, String reason) {

    }

    /**
     * 当两个对等点都指示不再传输任何消息且连接已成功释放时调用。不再调用此侦听器
     *
     * @param webSocket 当前web socket
     * @param code      状态码
     * @param reason    关闭终止原因
     */
    public void onClosed(WebSocket webSocket, int code, String reason) {

    }

    /**
     * 当web套接字由于从网络读取或写入错误而关闭时调用。发出和传入的消息可能都丢失了。不再调用此侦听器
     *
     * @param webSocket 当前web socket
     * @param throwable 线程信息
     * @param response  当前响应体
     */
    public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {

    }

}
