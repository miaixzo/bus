package org.aoju.bus.cron;

import org.aoju.bus.cron.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业执行管理器<br>
 * 负责管理作业的启动、停止等
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class TaskExecutorManager {

    protected Scheduler scheduler;
    /**
     * 执行器列表
     */
    private List<TaskExecutor> executors = new ArrayList<>();

    public TaskExecutorManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 启动 TaskExecutor
     *
     * @param task {@link Task}
     * @return {@link TaskExecutor}
     */
    public TaskExecutor spawnExecutor(Task task) {
        final TaskExecutor executor = new TaskExecutor(this.scheduler, task);
        synchronized (this.executors) {
            this.executors.add(executor);
        }
        // 子线程是否为deamon线程取决于父线程，因此此处无需显示调用
        // executor.setDaemon(this.scheduler.daemon);
//		executor.start();
        this.scheduler.threadExecutor.execute(executor);
        return executor;
    }

    /**
     * 执行器执行完毕调用此方法，将执行器从执行器列表移除
     *
     * @param executor 执行器 {@link TaskExecutor}
     * @return this
     */
    public TaskExecutorManager notifyExecutorCompleted(TaskExecutor executor) {
        synchronized (executors) {
            executors.remove(executor);
        }
        return this;
    }

}
