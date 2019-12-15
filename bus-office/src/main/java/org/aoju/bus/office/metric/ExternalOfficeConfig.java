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
