package org.aoju.bus.cron.listener;

import org.aoju.bus.cron.TaskExecutor;

/**
 * 定时任务监听接口<br>
 * 通过实现此接口，实现对定时任务的各个环节做监听
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface TaskListener {
    /**
     * 定时任务启动时触发
     *
     * @param executor {@link TaskExecutor}
     */
    public void onStart(TaskExecutor executor);

    /**
     * 任务成功结束时触发
     *
     * @param executor {@link TaskExecutor}
     */
    public void onSucceeded(TaskExecutor executor);

    /**
     * 任务启动失败时触发
     *
     * @param executor  {@link TaskExecutor}
     * @param exception 异常
     */
    public void onFailed(TaskExecutor executor, Throwable exception);
}
