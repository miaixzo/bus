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
package org.miaixz.bus.http.socket;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.http.Httpd;
import org.miaixz.bus.http.Request;

/**
 * 到web socket的非阻塞接口。使用{@linkplain WebSocket.Factory factory} 通常是{@link Httpd} 在正常操作时，每个web套接字将通过一系列状态进行处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface WebSocket {

    /**
     * @return 返回初始化此web套接字的请求
     */
    Request request();

    /**
     * @return 所有排队等待发送到服务器的消息的大小(以字节为单位)。这还不包括帧开销 它也不包括任何由操作系统或网络中介体缓冲的字节。如果队列中没有消息等待，则此方法返回0
     *         如果在web套接字被取消后可能返回一个非零值;这表示未传输排队消息
     */
    long queueSize();

    /**
     * 尝试将{@code text}编码为UTF-8并将其作为文本(类型为{@code 0x1})消息的数据发送 如果消息被加入队列，此方法将返回true。将溢出传出消息缓冲区的消息将被拒绝，
     * 并触发此web套接字的{@linkplain #close graceful shutdown}。此方法在这种情况下返回false， 在此web套接字关闭、关闭或取消的任何其他情况下也返回false
     *
     * @param text 文本信息
     * @return the true/false
     */
    boolean send(String text);

    /**
     * 尝试将{@code bytes}作为二进制(类型为{@code 0x2})消息的数据发送 如果消息被加入队列，此方法将返回true。将溢出传出消息缓冲区(16 MiB)的消息将被拒绝，
     * 并触发此web套接字的{@linkplain #close graceful shutdown}。此方法在这种情况下返回false， 在此web套接字关闭、关闭或取消的任何其他情况下也返回false
     *
     * @param bytes 缓存流
     * @return the true/false
     */
    boolean send(ByteString bytes);

    /**
     * 尝试启动此web套接字的正常关闭。任何已加入队列的消息将在发送关闭消息之前发送， 但是随后对{@link #send}的调用将返回false，它们的消息将不被加入队列
     *
     * @param code   RFC 6455第7.4节定义的状态码
     * @param reason 关闭或{@code null}的原因
     * @return the true/false
     * @throws IllegalArgumentException 如果状态码无效
     */
    boolean close(int code, String reason);

    /**
     * 立即并强烈地释放这个web套接字持有的资源，丢弃任何排队的消息 如果web套接字已经关闭或取消，则此操作不执行任何操作
     */
    void cancel();

    interface Factory {

        /**
         * 创建一个新的web套接字并立即返回它。创建web套接字将启动一个异步进程来连接套接字 成功或失败，{@code listener}将被通知。当返回的web套接字不再使用时，调用者必须关闭或取消它
         *
         * @param request  当前网络请求
         * @param listener 监听器
         * @return the web socket
         */
        WebSocket newWebSocket(Request request, WebSocketListener listener);
    }

}
