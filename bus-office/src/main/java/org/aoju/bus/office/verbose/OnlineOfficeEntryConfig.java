package org.aoju.bus.office.verbose;

import org.aoju.bus.office.Builder;
import org.aoju.bus.office.metric.OfficeManagerEntryConfig;

/**
 * 当不需要office实例来执行转换时，该类保存{@link OnlineOfficePoolEntry}的配置.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class OnlineOfficeEntryConfig implements OfficeManagerEntryConfig {

    private long taskExecutionTimeout = Builder.DEFAULT_TASK_EXECUTION_TIMEOUT;

    @Override
    public long getTaskExecutionTimeout() {
        return taskExecutionTimeout;
    }

    @Override
    public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
        this.taskExecutionTimeout = taskExecutionTimeout;
    }

}
