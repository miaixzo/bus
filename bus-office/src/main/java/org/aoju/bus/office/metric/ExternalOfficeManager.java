/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.office.metric;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Protocol;
import org.aoju.bus.office.bridge.LocalOfficeBridgeFactory;
import org.aoju.bus.office.builtin.MadeInOffice;
import org.aoju.bus.office.magic.UnoUrl;

import java.io.File;

/**
 * 连接到外部office流程的{@link OfficeManager}实现.
 * 外部office流程需要手动启动，例如从命令行with
 * 由于此实现不管理Office流程，因此不支持在流程意外退出时自动重新启动该流程.
 * 但是，如果外部进程是手动重新启动的，那么它将自动重新连接到外部进程
 *
 * @author Kimi Liu
 * @version 6.3.1
 * @since JDK 1.8+
 */
public final class ExternalOfficeManager extends AbstractOfficeManager {

    private final LocalOfficeBridgeFactory localOffice;

    /**
     * 使用指定的参数构造类的新实例.
     *
     * @param unoUrl office URL.
     * @param config 管理器配置.
     */
    private ExternalOfficeManager(
            final UnoUrl unoUrl, final ExternalOfficeBuilder config) {
        super(config);

        localOffice = new LocalOfficeBridgeFactory(unoUrl);
    }

    /**
     * 创建一个新的生成器实例.
     *
     * @return 一个新的生成器实例.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 使用默认配置创建一个新的 {@link ExternalOfficeManager}.
     *
     * @return 带有默认配置的 {@link ExternalOfficeManager}.
     */
    public static ExternalOfficeManager make() {
        return builder().build();
    }

    /**
     * 使用默认配置创建一个新的{@link ExternalOfficeManager}
     * 然后，创建的管理器将是{@link InstalledOfficeHolder}类的唯一实例
     * 注意，如果该类已经持有{@code OfficeManager}实例， 则现有管理器的所有者负责停止它
     *
     * @return 带有默认配置的{@link ExternalOfficeManager}.
     */
    public static ExternalOfficeManager install() {
        return builder().install().build();
    }

    private void connect() throws InstrumentException {

        try {
            final ExternalOfficeBuilder mconfig = (ExternalOfficeBuilder) config;
            new ConnectRetryable(localOffice)
                    .execute(mconfig.getRetryInterval(), mconfig.getConnectTimeout());

        } catch (Exception ex) {
            throw new InstrumentException("Could not establish connection to external office process", ex);
        }
    }

    @Override
    public void execute(final MadeInOffice task) throws InstrumentException {
        synchronized (localOffice) {
            if (!isRunning()) {
                connect();
            }
            task.execute(localOffice);
        }
    }

    @Override
    public boolean isRunning() {
        return localOffice.isConnected();
    }

    @Override
    public void start() throws InstrumentException {
        if (((ExternalOfficeBuilder) config).isConnectOnStart()) {
            synchronized (localOffice) {
                connect();

                makeTempDir();
            }
        }
    }

    @Override
    public void stop() {
        synchronized (localOffice) {
            if (isRunning()) {
                try {
                    localOffice.disconnect();
                } finally {
                    deleteTempDir();
                }
            }
        }
    }

    /**
     * 构造{@link ExternalOfficeManager}的生成器.
     *
     * @see ExternalOfficeManager
     */
    public static final class Builder extends AbstractOfficeManagerBuilder<Builder> {

        private Protocol connectionProtocol = Protocol.SOCKET;
        private int portNumber = org.aoju.bus.office.Builder.DEFAULT_PORT_NUMBER;
        private String pipeName = org.aoju.bus.office.Builder.DEFAULT_PIPE_NAME;
        private boolean connectOnStart = true;
        private long connectTimeout = org.aoju.bus.office.Builder.DEFAULT_CONNECT_TIMEOUT;
        private long retryInterval = org.aoju.bus.office.Builder.DEFAULT_RETRY_INTERVAL;

        // 私有，因此只有LocalOfficeManager可以初始化此生成器的实例.
        private Builder() {
            super();
        }

        @Override
        public ExternalOfficeManager build() {
            if (null == workingDir) {
                workingDir = new File(System.getProperty("java.io.tmpdir"));
            }

            // 验证office目录
            org.aoju.bus.office.Builder.validateOfficeWorkingDirectory(workingDir);

            // 建立office管理器
            final ExternalOfficeManager manager =
                    new ExternalOfficeManager(
                            connectionProtocol == Protocol.SOCKET
                                    ? new UnoUrl(portNumber)
                                    : null != pipeName ? new UnoUrl(pipeName) : new UnoUrl(org.aoju.bus.office.Builder.DEFAULT_PORT_NUMBER),
                            new ExternalOfficeBuilder(
                                    workingDir, connectOnStart, connectTimeout, retryInterval));
            if (install) {
                InstalledOfficeHolder.setInstance(manager);
            }
            return manager;
        }

        /**
         * 指定连接协议.
         *
         * @param connectionProtocol 使用的新协议.
         * @return 当前实例信息.
         */
        public Builder connectionProtocol(final Protocol connectionProtocol) {
            this.connectionProtocol = connectionProtocol;
            return this;
        }

        /**
         * 指定将用于与office通信的管道名称.
         *
         * @param pipeName 要使用的管道名称.
         * @return 当前实例信息.
         */
        public Builder pipeName(final String pipeName) {
            this.pipeName = pipeName;
            return this;
        }

        /**
         * 指定将用于与office通信的端口号.
         *
         * @param portNumber 要使用的端口号.
         * @return 当前实例信息.
         */
        public Builder portNumber(final int portNumber) {
            this.portNumber = portNumber;
            return this;
        }

        /**
         * 指定是否必须在{@link #start()}上尝试连接?如果为false，
         * 则仅在第一次执行{@link MadeInOffice}时尝试连接
         * <p>
         * 默认:true
         *
         * @param connectOnStart{@code true}连接开始，{@code false}连接结束.
         * @return 当前实例信息.
         */
        public Builder connectOnStart(final boolean connectOnStart) {
            this.connectOnStart = connectOnStart;
            return this;
        }

        /**
         * 指定超时(以毫秒为单位)，超时后连接尝试将失败.
         * 默认: 120000毫秒 (2分钟)
         *
         * @param connectTimeout 进程超时，以毫秒为单位.
         * @return 当前实例信息.
         */
        public Builder connectTimeout(final long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * 指定连接到office时每次尝试之间的延迟(以毫秒为单位).
         * 默认:250毫秒 (0.25 秒)
         *
         * @param retryInterval 重试间隔，以毫秒为单位.
         * @return 当前实例信息.
         */
        public Builder retryInterval(final long retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }
    }

}
