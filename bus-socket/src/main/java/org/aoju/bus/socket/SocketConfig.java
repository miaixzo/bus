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

import org.aoju.bus.core.utils.IoUtils;

/**
 * Socket通讯配置
 *
 * @author Kimi Liu
 * @version 3.0.0
 * @since JDK 1.8
 */
public class SocketConfig {

    /**
     * CPU核心数
     */
    private static int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 共享线程池大小，此线程池用于接收和处理用户连接
     */
    private int threadPoolSize = CPU_COUNT;

    /**
     * 读取超时时长，小于等于0表示默认
     */
    private long readTimeout;
    /**
     * 写出超时时长，小于等于0表示默认
     */
    private long writeTimeout;

    /**
     * 读取缓存大小
     */
    private int readBufferSize = IoUtils.DEFAULT_BUFFER_SIZE;
    /**
     * 写出缓存大小
     */
    private int writeBufferSize = IoUtils.DEFAULT_BUFFER_SIZE;

    /**
     * 获取共享线程池大小，此线程池用于接收和处理用户连接
     *
     * @return 共享线程池大小，此线程池用于接收和处理用户连接
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * 设置共享线程池大小，此线程池用于接收和处理用户连接
     *
     * @param threadPoolSize 共享线程池大小，此线程池用于接收和处理用户连接
     */
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * 获取读取超时时长，小于等于0表示默认
     *
     * @return 读取超时时长，小于等于0表示默认
     */
    public long getReadTimeout() {
        return readTimeout;
    }

    /**
     * 设置读取超时时长，小于等于0表示默认
     *
     * @param readTimeout 读取超时时长，小于等于0表示默认
     */
    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * 获取写出超时时长，小于等于0表示默认
     *
     * @return 写出超时时长，小于等于0表示默认
     */
    public long getWriteTimeout() {
        return writeTimeout;
    }

    /**
     * 设置写出超时时长，小于等于0表示默认
     *
     * @param writeTimeout 写出超时时长，小于等于0表示默认
     */
    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /**
     * 获取读取缓存大小
     *
     * @return 读取缓存大小
     */
    public int getReadBufferSize() {
        return readBufferSize;
    }

    /**
     * 设置读取缓存大小
     *
     * @param readBufferSize 读取缓存大小
     */
    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    /**
     * 获取写出缓存大小
     *
     * @return 写出缓存大小
     */
    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    /**
     * 设置写出缓存大小
     *
     * @param writeBufferSize 写出缓存大小
     */
    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

}
