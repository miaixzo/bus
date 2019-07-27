package org.aoju.bus.cron.task;

/**
 * 定时作业接口，通过实现execute方法执行具体的任务<br>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface Task {

    /**
     * 执行作业
     */
    public void execute();
}
