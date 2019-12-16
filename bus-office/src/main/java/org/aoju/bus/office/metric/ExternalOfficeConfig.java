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
package org.aoju.bus.office.metric;

import org.aoju.bus.office.builtin.MadeInOffice;

import java.io.File;

/**
 * 这个类包含{@link ExternalOfficeManager}的配置.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class ExternalOfficeConfig implements OfficeManagerConfig {

    /**
     * 获取是否在启动时尝试连接.
     */
    private final boolean connectOnStart;
    /**
     * 获取连接尝试失败后的超时时间.
     */
    private final long connectTimeout;
    /**
     * 获取连接到office时每次尝试之间的延迟(以毫秒为单位).
     */
    private final long retryInterval;
    /**
     * office工作目录.
     */
    private File workingDir;

    /**
     * 使用指定的值创建配置.
     *
     * @param workingDir     工作目录.
     * @param connectOnStart 如果尝试连接，则仅在第一次执行{@link MadeInOffice}时尝试连接
     * @param connectTimeout 超时之后，连接尝试将失败.
     * @param retryInterval  次尝试连接之间的超时.
     */
    public ExternalOfficeConfig(
            final File workingDir,
            final boolean connectOnStart,
            final long connectTimeout,
            final long retryInterval) {

        this.workingDir =
                workingDir == null ? new File(System.getProperty("java.io.tmpdir")) : workingDir;
        this.connectOnStart = connectOnStart;
        this.connectTimeout = connectTimeout;
        this.retryInterval = retryInterval;
    }

    @Override
    public File getWorkingDir() {
        return this.workingDir;
    }

    @Override
    public void setWorkingDir(final File workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * 获取是否在启动时尝试连接.
     *
     * @return {@code true}如果应该在启动时尝试连接，则{@code false}否则忽略
     */
    public boolean isConnectOnStart() {
        return connectOnStart;
    }

    /**
     * 获取连接尝试失败后的超时时间.
     *
     * @return 连接超时.
     */
    public long getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 获取连接到office时每次尝试之间的延迟(以毫秒为单位).
     *
     * @return 重试间隔.
     */
    public long getRetryInterval() {
        return retryInterval;
    }

}
