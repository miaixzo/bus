package org.aoju.bus.office.metric;

/**
 * 这个类提供了{@link AbstractOfficePool}的配置
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface OfficeManagerPoolConfig extends OfficeManagerConfig {

    /**
     * 获取转换队列中任务的最大生存时间。如果等待时间长于此超时，则任务将从队列中删除.
     * 默认:30秒
     *
     * @return 任务队列超时，以毫秒为单位.
     */
    long getTaskQueueTimeout();

    /**
     * 设置转换队列中任务的最大生存时间。如果等待时间长于此超时，则任务将从队列中删除.
     *
     * @param taskQueueTimeout 任务队列超时，以毫秒为单位.
     */
    void setTaskQueueTimeout(final long taskQueueTimeout);

}
