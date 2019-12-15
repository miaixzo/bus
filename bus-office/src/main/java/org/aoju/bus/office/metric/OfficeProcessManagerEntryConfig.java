package org.aoju.bus.office.metric;

import org.aoju.bus.office.Builder;
import org.aoju.bus.office.process.ProcessManager;

import java.io.File;

/**
 * 当需要office实例来执行转换时，该类保存{@link OfficeProcessPoolEntry}的配置
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class OfficeProcessManagerEntryConfig extends OfficeProcessManagerConfig
        implements OfficeManagerEntryConfig {

    private long taskExecutionTimeout = Builder.DEFAULT_TASK_EXECUTION_TIMEOUT;

    /**
     * 使用默认值创建配置.
     */
    public OfficeProcessManagerEntryConfig() {
        super();
    }

    /**
     * 使用指定的值创建配置.
     *
     * @param officeHome     office安装的主目录.
     * @param workingDir     要设置为office的工作目录.
     * @param processManager 用于处理创建的流程的流程管理器.
     */
    public OfficeProcessManagerEntryConfig(
            final File officeHome, final File workingDir, final ProcessManager processManager) {
        super(officeHome, workingDir, processManager);
    }

    @Override
    public long getTaskExecutionTimeout() {
        return taskExecutionTimeout;
    }

    @Override
    public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
    }

}
