package org.aoju.bus.office.metric;

import org.aoju.bus.office.Builder;
import org.aoju.bus.office.process.ProcessManager;

import java.io.File;

/**
 * 当需要某个office实例执行转换时，该类提供{@link AbstractOfficePool}的配置
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class OfficeProcessManagerPoolConfig extends OfficeProcessManagerEntryConfig
        implements OfficeManagerPoolConfig {

    private long taskQueueTimeout = Builder.DEFAULT_TASK_QUEUE_TIMEOUT;

    /**
     * 使用指定的值创建配置.
     *
     * @param officeHome     office安装的主目录.
     * @param workingDir     要设置为office的工作目录.
     * @param processManager 用于处理创建的流程的流程管理器.
     */
    public OfficeProcessManagerPoolConfig(
            final File officeHome, final File workingDir, final ProcessManager processManager) {
        super(officeHome, workingDir, processManager);
    }

    @Override
    public long getTaskQueueTimeout() {
        return taskQueueTimeout;
    }

    @Override
    public void setTaskQueueTimeout(final long taskQueueTimeout) {
        this.taskQueueTimeout = taskQueueTimeout;
    }

}
