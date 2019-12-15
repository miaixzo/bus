package org.aoju.bus.office.metric;

/**
 * 这个接口提供了{@link OfficeProcessPoolEntry}的配置
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface OfficeManagerEntryConfig {

    /**
     * 获取允许处理任务的最大时间。如果任务的处理时间长于此超时，则此任务将中止并处理下一个任务.
     * 默认:2分钟
     *
     * @return 任务执行超时，以毫秒为单位.
     */
    long getTaskExecutionTimeout();

    /**
     * 设置允许处理任务的最大时间。如果任务的处理时间长于此超时，则此任务将中止并处理下一个任务.
     *
     * @param taskExecutionTimeout 新的任务执行超时.
     */
    void setTaskExecutionTimeout(final long taskExecutionTimeout);

}
