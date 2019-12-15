package org.aoju.bus.office.verbose;

import org.aoju.bus.office.Builder;
import org.aoju.bus.office.metric.AbstractOfficePool;
import org.aoju.bus.office.metric.OfficeManagerPoolConfig;

import java.io.File;

/**
 * 当不需要任何office实例来执行转换时，
 * 该类提供{@link AbstractOfficePool}的配置.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class OnlineOfficePoolConfig extends OnlineOfficeEntryConfig
        implements OfficeManagerPoolConfig {

    private long taskQueueTimeout = Builder.DEFAULT_TASK_QUEUE_TIMEOUT;
    private File workingDir;

    /**
     * 使用指定的值创建配置.
     *
     * @param workingDir 要设置为office的工作目录.
     */
    public OnlineOfficePoolConfig(final File workingDir) {
        super();

        this.workingDir = workingDir;
    }

    @Override
    public long getTaskQueueTimeout() {
        return taskQueueTimeout;
    }

    @Override
    public void setTaskQueueTimeout(final long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
    }

    @Override
    public File getWorkingDir() {
        return workingDir;
    }

    @Override
    public void setWorkingDir(final File workingDir) {
        this.workingDir = workingDir;
    }

}
