/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.socket;

import org.aoju.bus.core.lang.exception.CommonException;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;

/**
 * Socket相关工具类
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class SocketUtils {

    /**
     * 获取远程端的地址信息，包括host和端口
     * null表示channel为null或者远程主机未连接
     *
     * @param channel {@link AsynchronousSocketChannel}
     * @return 远程端的地址信息，包括host和端口，null表示channel为null或者远程主机未连接
     */
    public static SocketAddress getRemoteAddress(AsynchronousSocketChannel channel) {
        try {
            return (null == channel) ? null : channel.getRemoteAddress();
        } catch (ClosedChannelException e) {
            // Channel未打开或已关闭，返回null表示未连接
            return null;
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 远程主机是否处于连接状态
     * 通过判断远程地址获取成功与否判断
     *
     * @param channel {@link AsynchronousSocketChannel}
     * @return 远程主机是否处于连接状态
     */
    public static boolean isConnected(AsynchronousSocketChannel channel) {
        return null != getRemoteAddress(channel);
    }

}
