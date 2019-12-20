/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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

import java.nio.channels.AsynchronousSocketChannel;

/**
 * 网络监控器,提供通讯层面监控功能的接口
 * <p>
 * 并未单独提供配置监控服务的接口,用户在使用时仅需在MessageProcessor实现类中同时实现当前NetMonitor接口即可
 * 在注册消息处理器时,若服务监测到该处理器同时实现了NetMonitor接口,则该监视器便会生效
 * </p>
 * <h2>示例：</h2>
 * <pre>
 *     public class MessageProcessorImpl implements MessageProcessor,NetMonitor{
 *
 *     }
 * </pre>
 *
 * <b>注意:</b>
 * <p>
 * 实现本接口时要关注acceptMonitor接口的返回值,如无特殊需求直接返回true,若返回false会拒绝本次连接
 * </p>
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public interface NetMonitor<T> {

    /**
     * <p>
     * 监控已接收到的连接
     * </p>
     *
     * @param channel 当前已经建立连接的通道对象
     * @return true:接受该连接,false:拒绝该连接
     */
    boolean acceptMonitor(AsynchronousSocketChannel channel);

    /**
     * 监控触发本次读回调Session的已读数据字节数
     *
     * @param session  当前执行read的AioSession对象
     * @param readSize 已读数据长度
     */
    void readMonitor(AioSession<T> session, int readSize);

    /**
     * 监控触发本次写回调session的已写数据字节数
     *
     * @param session   本次执行write回调的AIOSession对象
     * @param writeSize 本次输出的数据长度
     */
    void writeMonitor(AioSession<T> session, int writeSize);

}
