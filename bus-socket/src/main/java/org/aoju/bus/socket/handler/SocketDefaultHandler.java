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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.socket.handler;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.logger.level.Level;
import org.aoju.bus.socket.SocketRequest;
import org.aoju.bus.socket.SocketResponse;

/**
 * 默认拦截器
 *
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public class SocketDefaultHandler extends AbstractSocketHandler {

    @Override
    public final void doHandle(SocketRequest request, SocketResponse response) {

    }

    /**
     * 握手成功
     *
     * @param request  请求
     * @param response 响应
     */
    public void onHandShark(SocketRequest request, SocketResponse response) {
        Logger.get().log(Level.FATAL, "handShark success");
    }

    /**
     * 握手成功
     *
     * @param request  请求
     * @param response 响应
     */
    public void onClose(SocketRequest request, SocketResponse response) {
        Logger.get().log(Level.FATAL, "close connection");
    }

    /**
     * 处理字符串请求消息
     *
     * @param request  请求
     * @param response 响应
     * @param data     数据
     */
    public void handleTextMessage(SocketRequest request, SocketResponse response, String data) {

    }

    /**
     * 处理二进制请求消息
     *
     * @param request  请求
     * @param response 响应
     * @param data     数据
     */
    public void handleBinaryMessage(SocketRequest request, SocketResponse response, byte[] data) {

    }

    /**
     * 处理错误请求消息
     *
     * @param throwable 异常信息
     */
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

}
