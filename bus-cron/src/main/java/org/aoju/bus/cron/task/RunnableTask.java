package org.aoju.bus.cron.task;

/**
 * {@link Runnable} 的 {@link Task}包装
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class RunnableTask implements Task {
    private Runnable runnable;

    public RunnableTask(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void execute() {
        runnable.run();
    }
}
