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
package org.miaixz.bus.http.metric;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.Connection;
import org.miaixz.bus.http.accord.ConnectionPool;
import org.miaixz.bus.http.socket.Handshake;

/**
 * 用于度量事件的侦听器。扩展这个类来监视应用程序的HTTP调用的数量、大小和持续时间 所有事件方法必须快速执行，不需要外部锁定，不能抛出异常，不能尝试更改事件参数， 也不能重入客户机。任何对文件或网络的IO写入都应该异步进行
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class EventListener {

    public static final EventListener NONE = new EventListener() {

    };

    public static EventListener.Factory factory(EventListener listener) {
        return call -> listener;
    }

    /**
     * 在调用进入队列或由客户端执行时立即调用。在线程或流限制的情况下，这个调用可能在处理请求 开始之前就已经执行了 对于单个{@link NewCall}只调用一次。不同路由或重定向的重试将在
     * 单个callStart和{@link #callEnd}/{@link #callFailed}对的范围内处理。
     *
     * @param call 调用信息
     */
    public void callStart(NewCall call) {

    }

    /**
     * 仅在DNS查找之前调用。看到{@link DnsX #查找(String)}
     *
     * @param call       调用信息
     * @param domainName 主机名
     */
    public void dnsStart(NewCall call, String domainName) {

    }

    /**
     * 在DNS查找后立即调用. 此方法在{@link #dnsStart}之后调用
     *
     * @param call            调用信息
     * @param domainName      主机名
     * @param inetAddressList IP地址信息
     */
    public void dnsEnd(NewCall call, String domainName, List<InetAddress> inetAddressList) {

    }

    /**
     * 仅在初始化套接字连接之前调用. 如果不能重用{@link ConnectionPool}中的现有连接，则将调用此方法.
     *
     * @param call              调用信息
     * @param inetSocketAddress 网络套接字信息
     * @param proxy             代理
     */
    public void connectStart(NewCall call, InetSocketAddress inetSocketAddress, Proxy proxy) {

    }

    /**
     * 在启动TLS连接之前调用.
     *
     * @param call 调用信息
     */
    public void secureConnectStart(NewCall call) {

    }

    /**
     * 尝试TLS连接后立即调用. 此方法在{@link #secureConnectStart}之后调用.
     *
     * @param call      调用信息
     * @param handshake 网络握手信息
     */
    public void secureConnectEnd(NewCall call, Handshake handshake) {

    }

    /**
     * 在尝试套接字连接后立即调用.
     *
     * @param call              调用信息
     * @param inetSocketAddress 网络套接字信息
     * @param proxy             代理
     * @param protocol          协议
     */
    public void connectEnd(NewCall call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol) {

    }

    /**
     * 连接尝试失败时调用。如果有进一步的路由可用并且启用了故障恢复，则此故障不是终端.
     *
     * @param call              调用信息
     * @param inetSocketAddress 网络套接字信息
     * @param proxy             代理
     * @param protocol          协议
     * @param ioe               异常
     */
    public void connectFailed(NewCall call, InetSocketAddress inetSocketAddress, Proxy proxy, Protocol protocol,
            IOException ioe) {

    }

    /**
     * 为{@code call}获取连接后调用.
     *
     * @param call       调用信息
     * @param connection 连接信息
     */
    public void connectionAcquired(NewCall call, Connection connection) {

    }

    /**
     * 在为{@code call}释放连接后调用. 这个方法总是在{@link #connectionAcquired(NewCall, Connection)}之后调用。
     *
     * @param call       调用信息
     * @param connection 连接信息
     */
    public void connectionReleased(NewCall call, Connection connection) {

    }

    /**
     * 仅在发送请求头之前调用. 连接是隐式的，通常与最后一个{@link #connectionAcquired(NewCall, Connection)}事件相关
     *
     * @param call 调用信息
     */
    public void requestHeadersStart(NewCall call) {

    }

    /**
     * 发送请求头后立即调用. 这个方法总是在{@link #requestHeadersStart(NewCall)}之后调用
     *
     * @param call    调用信息
     * @param request 通过网络发送的请求
     */
    public void requestHeadersEnd(NewCall call, Request request) {

    }

    /**
     * 仅在发送请求主体之前调用。只有在请求允许并有一个请求体要发送时才会被调用吗. 连接是隐式的，通常与最后一个{@link #connectionAcquired(NewCall, Connection)}事件相关
     *
     * @param call 调用信息
     */
    public void requestBodyStart(NewCall call) {

    }

    /**
     * 在发送请求主体后立即调用 此方法总是在{@link #requestBodyStart(NewCall)}之后调用
     *
     * @param call      调用信息
     * @param byteCount 字节流长度信息
     */
    public void requestBodyEnd(NewCall call, long byteCount) {

    }

    /**
     * 当写入请求失败时调用 这个方法在{@link #requestHeadersStart}或{@link #requestBodyStart}之后被调用
     */
    public void requestFailed(NewCall call, IOException ioe) {
    }

    /**
     * 仅在接收响应标头之前调用. 连接是隐式的，通常与最后一个{@link #connectionAcquired(NewCall, Connection)}事件相关
     * 对于单个{@link NewCall}可以调用多次。例如，如果对{@link NewCall#request()}的响应是重定向到另一个地址
     *
     * @param call 调用信息
     */
    public void responseHeadersStart(NewCall call) {

    }

    /**
     * 在接收响应标头后立即调用 这个方法总是在{@link #responseHeadersStart}之后调用
     *
     * @param call     调用信息
     * @param response 通过网络接收到的响应
     */
    public void responseHeadersEnd(NewCall call, Response response) {

    }

    /**
     * 仅在接收响应主体之前调用. 连接是隐式的，通常与最后一个{@link #connectionAcquired(NewCall, Connection)}事件相关
     * 对于单个{@link NewCall}通常只会调用一次，例外情况是一组有限的情况，包括故障恢复
     *
     * @param call 调用信息
     */
    public void responseBodyStart(NewCall call) {

    }

    /**
     * 在接收到响应体并完成读取后立即调用. 只会在有响应体的请求时调用，例如，不会在websocket升级时调用 此方法总是在{@link #requestBodyStart(NewCall)}之后调用
     *
     * @param call      调用信息
     * @param byteCount 字节流长度信息
     */
    public void responseBodyEnd(NewCall call, long byteCount) {

    }

    /**
     * 当读取响应失败时调用 这个方法在{@link #responseHeadersStart}或{@link #responseBodyStart}之后被调用
     */
    public void responseFailed(NewCall call, IOException ioe) {
    }

    /**
     * 在调用完全结束后立即调用。这包括调用方延迟消耗响应体. 此方法总是在{@link #callStart(NewCall)}之后调用
     *
     * @param call 调用信息
     */
    public void callEnd(NewCall call) {

    }

    /**
     * 永久失败时调用. 此方法总是在{@link #callStart(NewCall)}之后调用
     *
     * @param call 调用信息
     * @param ioe  异常
     */
    public void callFailed(NewCall call, IOException ioe) {

    }

    public interface Factory {

        /**
         * 为特定的{@link NewCall}创建{@link EventListener}的实例。 返回的{@link EventListener}实例将在{@code call}的生命周期中使用
         * 此方法在创建{@code call}之后调用。查看{@link Httpd # newCall(请求)} 对实现来说，在这个方法的{@code call}实例上发出任何变化操作都是错误的
         *
         * @param call 调用信息
         * @return 监听器
         */
        EventListener create(NewCall call);
    }

}
