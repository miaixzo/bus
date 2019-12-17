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
package org.aoju.bus.office.verbose;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.office.metric.AbstractOfficePool;
import org.aoju.bus.office.metric.InstalledOfficeHolder;
import org.aoju.bus.office.metric.OfficeManager;

import java.io.File;
import java.util.stream.IntStream;

/**
 * {@link OfficeManager}池实现，不依赖于office安装来处理转换
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public final class OnlineOffice extends AbstractOfficePool {

    private final int poolSize;
    private final String urlConnection;

    private OnlineOffice(
            final int poolSize,
            final String urlConnection,
            final OnlineOfficePoolConfig config) {
        super(poolSize, config);

        this.poolSize = poolSize;
        this.urlConnection = urlConnection;
    }

    /**
     * 创建一个新的生成器实例.
     *
     * @return 新的生成器实例.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 使用默认配置创建一个新的{@link OnlineOffice}.
     *
     * @param urlConnection 指向LibreOfficeOnline服务器的URL.
     * @return 带有默认配置的{@link OnlineOffice}.
     */
    public static OnlineOffice make(final String urlConnection) {
        return builder().urlConnection(urlConnection).build();
    }

    /**
     * 使用默认配置创建一个新的{@link OnlineOffice}.
     * 然后，创建的管理器将是{@link InstalledOfficeHolder}类的唯一实例.
     * 注意，如果{@code InstalledOfficeManagerHolder}类已经持有{@code OfficeManager}实例，
     * 则现有管理器的所有者负责停止它.
     *
     * @param urlConnection 指向LibreOfficeOnline服务器的URL.
     * @return {@link OnlineOffice} 使用默认配置.
     */
    public static OnlineOffice install(final String urlConnection) {
        return builder().urlConnection(urlConnection).install().build();
    }

    @Override
    protected OnlineOfficePoolEntry[] createPoolEntries() {

        return IntStream.range(0, poolSize)
                .mapToObj(
                        idx ->
                                new OnlineOfficePoolEntry(
                                        urlConnection, (OnlineOfficeEntryConfig) config))
                .toArray(OnlineOfficePoolEntry[]::new);
    }

    /**
     * 用于构造{@link OnlineOffice}的生成器.
     *
     * @see OnlineOffice
     */
    public static final class Builder extends AbstractOfficeManagerPoolBuilder<Builder> {

        /**
         * 池的最大大小.
         */
        public static final int MAX_POOL_SIZE = 1000;
        /**
         * 池的默认大小.
         */
        private int poolSize = 1;
        private String urlConnection;

        private Builder() {
            super();
        }

        @Override
        public OnlineOffice build() {

            Assert.notEmpty(urlConnection, "The URL connection is missing");

            if (workingDir == null) {
                workingDir = new File(System.getProperty("java.io.tmpdir"));
            }

            final OnlineOfficePoolConfig config = new OnlineOfficePoolConfig(workingDir);
            config.setTaskExecutionTimeout(taskExecutionTimeout);
            config.setTaskQueueTimeout(taskQueueTimeout);

            final OnlineOffice manager =
                    new OnlineOffice(poolSize, urlConnection, config);
            if (install) {
                InstalledOfficeHolder.setInstance(manager);
            }
            return manager;
        }

        /**
         * 指定管理器的池大小.
         *
         * @param poolSize 池的大小.
         * @return 构造器实例.
         */
        public Builder poolSize(final int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        /**
         * 指定管理器的URL连接.
         *
         * @param urlConnection URL连接.
         * @return 构造器实例.
         */
        public Builder urlConnection(final String urlConnection) {
            this.urlConnection = urlConnection;
            return this;
        }

    }

}
