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
package org.aoju.bus.socket.origin;

import org.aoju.bus.socket.origin.aio.AioSession;

import java.nio.ByteBuffer;

/**
 * 消息解码器
 *
 * @param <T> 解码后的目标类型
 * @author Kimi Liu
 * @version 3.2.5
 * @since JDK 1.8
 */
public interface MsgDecoder<T> {

    /**
     * 对于从Socket流中获取到的数据采用当前MsgDecoder的实现类协议进行解析。
     *
     * @param session    本次需要解码的session
     * @param readBuffer 待处理的读buffer
     * @return 本次解码成功后封装的业务消息对象, 返回null则表示解码未完成
     */
    T decode(AioSession session, ByteBuffer readBuffer);

}
