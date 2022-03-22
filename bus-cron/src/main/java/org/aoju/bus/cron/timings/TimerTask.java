package org.aoju.bus.cron.timings;

/**
 * 延迟任务
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since Java 17+
 */
public class TimerTask {

    /**
     * 延迟时间
     */
    private final long delayMs;

    /**
     * 任务
     */
    private final Runnable task;
    /**
     * 任务描述
     */
    public String desc;
    /**
     * 时间槽
     */
    protected TimerTaskList timerTaskList;
    /**
     * 下一个节点
     */
    protected TimerTask next;
    /**
     * 上一个节点
     */
    protected TimerTask prev;

    /**
     * 构造
     *
     * @param task    任务
     * @param delayMs 延迟毫秒数（以当前时间为准）
     */
    public TimerTask(Runnable task, long delayMs) {
        this.delayMs = System.currentTimeMillis() + delayMs;
        this.task = task;
    }

    /**
     * 获取任务
     *
     * @return 任务
     */
    public Runnable getTask() {
        return task;
    }

    /**
     * 获取延迟时间点，即创建时间+延迟时长（单位毫秒）
     *
     * @return 延迟时间点
     */
    public long getDelayMs() {
        return delayMs;
    }

    @Override
    public String toString() {
        return desc;
    }

}
