package org.aoju.bus.cron;

import org.aoju.bus.cron.task.Task;

/**
 * 作业执行器<br>
 * 执行具体的作业，执行完毕销毁
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class TaskExecutor implements Runnable {

    private Scheduler scheduler;
    private Task task;

    public TaskExecutor(Scheduler scheduler, Task task) {
        this.scheduler = scheduler;
        this.task = task;
    }

    /**
     * 获得任务对象
     *
     * @return 任务对象
     */
    public Task getTask() {
        return task;
    }

    @Override
    public void run() {
        try {
            scheduler.listenerManager.notifyTaskStart(this);
            task.execute();
            scheduler.listenerManager.notifyTaskSucceeded(this);
        } catch (Exception e) {
            scheduler.listenerManager.notifyTaskFailed(this, e);
        } finally {
            scheduler.taskExecutorManager.notifyExecutorCompleted(this);
        }
    }
}
