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
package org.miaixz.bus.http.accord;

import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.Route;
import org.miaixz.bus.http.socket.Handshake;

import java.net.Socket;

/**
 * HTTP、HTTPS或HTTPS+HTTP/2连接的套接字和流。
 * 可以用于多个HTTP请求/响应交换。连接可以直接到源服务器，也可以通过代理
 *
 * <p>
 * 通常，此类的实例由HTTP客户机自动创建、连接和执行。  应用程序可以使用这个类
 * 作为{@linkplain ConnectionPool connection pool}的成员来监视HTTP连接.
 * 不要将这个类与错误命名的{@code HttpURLConnection}混淆，后者与其说是一个连接，
 * 不如说是一个请求/响应交换
 * 在协商到远程主机的安全连接时，在选择包括哪些选项时需要进行权衡。更新的TLS选项非常有用
 * 当最大并发流限制降低时，一些分配将被取消。尝试在这些分配上创建新流将失败
 * </p>
 *
 *   <ul>
 *     <li>服务器名称指示(SNI)允许一个IP地址为多个域名协商安全连接
 *     <li>应用层协议协商(ALPN)允许使用HTTPS端口(443)协商HTTP/2.
 *   </ul>
 *
 * <p>
 * 注意，一个分配可能在它的流完成之前被释放。这是为了使调用者更容易地进行簿记:
 * 一旦找到终端流，就释放分配。但仅在其数据流耗尽后才完成流
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Connection {

    /**
     * @return 返回此连接使用的路由
     */
    Route route();

    /**
     * @return 此连接使用的套接字。如果此连接是HTTPS，
     * 则返回{@linkplain javax.net.ssl.SSLSocket SSL套接字}。
     * 如果这是一个HTTP/2连接，则套接字可能由多个并发调用共享
     */
    Socket socket();

    /**
     * @return 用于建立此连接的TLS握手，如果连接不是HTTPS则返回null
     */
    Handshake handshake();

    /**
     * @return 此连接协商的协议，如果没有协商协议，则返回{@link Protocol #HTTP_1_1}。
     * 此方法返回{@link Protocol#HTTP_1_1}，即使远程对等方使用{@link Protocol#HTTP_1_0}
     */
    Protocol protocol();

}
