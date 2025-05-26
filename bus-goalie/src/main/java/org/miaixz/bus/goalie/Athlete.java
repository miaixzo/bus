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
package org.miaixz.bus.goalie;

import org.miaixz.bus.logger.Logger;

import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * 服务端类，负责启动和管理基于 Reactor Netty 的 HTTP 服务器
 *
 * @author Justubborn
 * @since Java 17+
 */
public class Athlete {

    /**
     * Reactor Netty 的 HTTP 服务器实例，用于处理 HTTP 请求
     */
    private final HttpServer httpServer;

    /**
     * 可释放的服务器实例，表示已绑定的服务器资源
     */
    private DisposableServer disposableServer;

    /**
     * 构造器，初始化 Athlete 实例
     *
     * @param httpServer Reactor Netty 的 HTTP 服务器实例
     */
    public Athlete(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    /**
     * 初始化并启动 HTTP 服务器 将 httpServer 绑定到指定端口，并记录启动成功的日志
     */
    private void init() {
        disposableServer = httpServer.bindNow();
        Logger.info("reactor server start on port:{} success", disposableServer.port());
    }

    /**
     * 停止并销毁 HTTP 服务器 释放服务器资源，并记录停止成功的日志
     */
    private void destroy() {
        disposableServer.disposeNow();
        Logger.info("reactor server stop on port:{} success", disposableServer.port());
    }

}