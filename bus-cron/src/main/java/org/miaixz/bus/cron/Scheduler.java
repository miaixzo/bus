/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.cron;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.lang.thread.ExecutorBuilder;
import org.miaixz.bus.core.lang.thread.ThreadFactoryBuilder;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cron.crontab.Crontab;
import org.miaixz.bus.cron.crontab.InvokeCrontab;
import org.miaixz.bus.cron.crontab.RunnableCrontab;
import org.miaixz.bus.cron.listener.TaskListener;
import org.miaixz.bus.cron.listener.TaskListenerManager;
import org.miaixz.bus.cron.pattern.CronPattern;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.Setting;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 任务调度器
 * <p>
 * 调度器启动流程：
 * <pre>启动Timer = 启动TaskLauncher = 启动TaskExecutor</pre>
 * 调度器关闭流程:
 * <pre>关闭Timer = 关闭所有运行中的TaskLauncher = 关闭所有运行中的TaskExecutor</pre>
 * 其中：
 * <pre>Launcher ：定时器每分钟调用一次（如果{@link Scheduler#isMatchSecond()}为{@code true}每秒调用一次），
 * 负责检查<strong>Repertoire</strong>是否有匹配到此时间运行的Task
 * </pre>
 *
 * <pre>
 * Executor ：TaskLauncher匹配成功后，触发TaskExecutor执行具体的作业，执行完毕销毁
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Scheduler implements Serializable {

    private static final long serialVersionUID = -1L;

    private final Lock lock = new ReentrantLock();

    /**
     * 定时任务配置
     */
    protected Configure config = new Configure();
    /**
     * 是否为守护线程
     */
    protected boolean daemon;
    /**
     * 定时任务表
     */
    protected Repertoire repertoire = new Repertoire();
    /**
     * 启动器管理器
     */
    protected Supervisor supervisor;
    /**
     * 执行器管理器
     */
    protected Manager manager;
    /**
     * 监听管理器列表
     */
    protected TaskListenerManager listenerManager = new TaskListenerManager();
    /**
     * 线程池，用于执行TaskLauncher和TaskExecutor
     */
    protected ExecutorService threadExecutor;
    /**
     * 是否已经启动
     */
    private boolean started = false;
    /**
     * 定时器
     */
    private CronTimer timer;

    /**
     * 获得时区，默认为 {@link TimeZone#getDefault()}
     *
     * @return 时区
     */
    public TimeZone getTimeZone() {
        return this.config.getTimeZone();
    }

    /**
     * 设置时区
     *
     * @param timeZone 时区
     * @return this
     */
    public Scheduler setTimeZone(final TimeZone timeZone) {
        this.config.setTimeZone(timeZone);
        return this;
    }

    /**
     * 设置自定义线程池
     * 自定义线程池时须考虑方法执行的线程是否为守护线程
     *
     * @param threadExecutor 自定义线程池
     * @return this
     * @throws CrontabException 定时任务已经启动抛出此异常
     */
    public Scheduler setThreadExecutor(final ExecutorService threadExecutor) throws CrontabException {
        lock.lock();
        try {
            checkStarted();
            this.threadExecutor = threadExecutor;
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * 是否为守护线程
     *
     * @return 是否为守护线程
     */
    public boolean isDaemon() {
        return this.daemon;
    }

    /**
     * 设置是否为守护线程
     * 如果为true，则在调用{@link #stop()}方法后执行的定时任务立即结束，否则等待执行完毕才结束。默认非守护线程
     * 如果用户调用{@link #setThreadExecutor(ExecutorService)}自定义线程池则此参数无效
     *
     * @param on {@code true}为守护线程，否则非守护线程
     * @return this
     * @throws CrontabException 定时任务已经启动抛出此异常
     */
    public Scheduler setDaemon(final boolean on) throws CrontabException {
        lock.lock();
        try {
            checkStarted();
            this.daemon = on;
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * 是否支持秒匹配
     *
     * @return {@code true}使用，{@code false}不使用
     */
    public boolean isMatchSecond() {
        return this.config.isMatchSecond();
    }

    /**
     * 设置是否支持秒匹配，默认不使用
     *
     * @param isMatchSecond {@code true}支持，{@code false}不支持
     * @return this
     */
    public Scheduler setMatchSecond(final boolean isMatchSecond) {
        this.config.setMatchSecond(isMatchSecond);
        return this;
    }

    /**
     * 增加监听器
     *
     * @param listener {@link TaskListener}
     * @return this
     */
    public Scheduler addListener(final TaskListener listener) {
        this.listenerManager.addListener(listener);
        return this;
    }

    /**
     * 移除监听器
     *
     * @param listener {@link TaskListener}
     * @return this
     */
    public Scheduler removeListener(final TaskListener listener) {
        this.listenerManager.removeListener(listener);
        return this;
    }

    /**
     * 批量加入配置文件中的定时任务
     * 配置文件格式为： xxx.xxx.xxx.Class.method = * * * * *
     *
     * @param cronSetting 定时任务设置文件
     * @return this
     */
    public Scheduler schedule(final Setting cronSetting) {
        if (MapKit.isNotEmpty(cronSetting)) {
            String group;
            for (final Entry<String, LinkedHashMap<String, String>> groupedEntry : cronSetting.getGroupedMap().entrySet()) {
                group = groupedEntry.getKey();
                for (final Entry<String, String> entry : groupedEntry.getValue().entrySet()) {
                    String jobClass = entry.getKey();
                    if (StringKit.isNotBlank(group)) {
                        jobClass = group + Symbol.C_DOT + jobClass;
                    }
                    final String pattern = entry.getValue();
                    Logger.debug("Load job: {} {}", pattern, jobClass);
                    try {
                        // 自定义ID避免重复从配置文件加载
                        schedule("id_" + jobClass, pattern, new InvokeCrontab(jobClass));
                    } catch (final Exception e) {
                        throw new CrontabException("Schedule [{}] [{}] error!", pattern, jobClass);
                    }
                }
            }
        }
        return this;
    }

    /**
     * 新增Task，使用随机UUID
     *
     * @param pattern {@link CronPattern}对应的String表达式
     * @param task    {@link Runnable}
     * @return ID
     */
    public String schedule(final String pattern, final Runnable task) {
        return schedule(pattern, new RunnableCrontab(task));
    }

    /**
     * 新增Task，使用随机UUID
     *
     * @param pattern {@link CronPattern}对应的String表达式
     * @param crontab {@link Crontab}
     * @return ID
     */
    public String schedule(final String pattern, final Crontab crontab) {
        final String id = ID.objectId();
        schedule(id, pattern, crontab);
        return id;
    }

    /**
     * 新增Task，如果任务ID已经存在，抛出异常
     *
     * @param id      ID，为每一个Task定义一个ID
     * @param pattern {@link CronPattern}对应的String表达式
     * @param task    {@link Runnable}
     * @return this
     */
    public Scheduler schedule(final String id, final String pattern, final Runnable task) {
        return schedule(id, new CronPattern(pattern), new RunnableCrontab(task));
    }

    /**
     * 新增Task，如果任务ID已经存在，抛出异常
     *
     * @param id      ID，为每一个Task定义一个ID
     * @param pattern {@link CronPattern}对应的String表达式
     * @param crontab {@link Crontab}
     * @return this
     */
    public Scheduler schedule(final String id, final String pattern, final Crontab crontab) {
        return schedule(id, new CronPattern(pattern), crontab);
    }

    /**
     * 新增Task，如果任务ID已经存在，抛出异常
     *
     * @param id      ID，为每一个Task定义一个ID
     * @param pattern {@link CronPattern}
     * @param crontab {@link Crontab}
     * @return this
     */
    public Scheduler schedule(final String id, final CronPattern pattern, final Crontab crontab) {
        repertoire.add(id, pattern, crontab);
        return this;
    }

    /**
     * 移除Task
     *
     * @param id Task的ID
     * @return this
     */
    public Scheduler deschedule(final String id) {
        descheduleWithStatus(id);
        return this;
    }

    /**
     * 移除Task，并返回是否移除成功
     *
     * @param id Task的ID
     * @return 是否移除成功，{@code false}表示未找到对应ID的任务
     */
    public boolean descheduleWithStatus(final String id) {
        return this.repertoire.remove(id);
    }

    /**
     * 更新Task执行的时间规则
     *
     * @param id      Task的ID
     * @param pattern {@link CronPattern}
     * @return this
     */
    public Scheduler updatePattern(final String id, final CronPattern pattern) {
        this.repertoire.updatePattern(id, pattern);
        return this;
    }

    /**
     * 获取定时任务表，注意此方法返回非复制对象，对返回对象的修改将影响已有定时任务
     *
     * @return 定时任务表{@link Repertoire}
     */
    public Repertoire getTaskTable() {
        return this.repertoire;
    }

    /**
     * 获得指定id的{@link CronPattern}
     *
     * @param id ID
     * @return {@link CronPattern}
     */
    public CronPattern getPattern(final String id) {
        return this.repertoire.getPattern(id);
    }

    /**
     * 获得指定id的{@link Crontab}
     *
     * @param id ID
     * @return {@link Crontab}
     */
    public Crontab getTask(final String id) {
        return this.repertoire.getTask(id);
    }

    /**
     * 是否无任务
     *
     * @return true表示无任务
     */
    public boolean isEmpty() {
        return this.repertoire.isEmpty();
    }

    /**
     * 当前任务数
     *
     * @return 当前任务数
     */
    public int size() {
        return this.repertoire.size();
    }

    /**
     * 清空任务表
     *
     * @return this
     */
    public Scheduler clear() {
        this.repertoire = new Repertoire();
        return this;
    }

    /**
     * @return 是否已经启动
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * 启动
     *
     * @param isDaemon 是否以守护线程方式启动，如果为true，则在调用{@link #stop()}方法后执行的定时任务立即结束，否则等待执行完毕才结束。
     * @return this
     */
    public Scheduler start(final boolean isDaemon) {
        this.daemon = isDaemon;
        return start();
    }

    /**
     * 启动
     *
     * @return this
     */
    public Scheduler start() {
        lock.lock();
        try {
            checkStarted();

            if (null == this.threadExecutor) {
                // 无界线程池，确保每一个需要执行的线程都可以及时运行，同时复用已有线程避免线程重复创建
                this.threadExecutor = ExecutorBuilder.of().useSynchronousQueue().setThreadFactory(//
                        ThreadFactoryBuilder.of().setNamePrefix("x-cron-").setDaemon(this.daemon).build()//
                ).build();
            }
            this.supervisor = new Supervisor(this);
            this.manager = new Manager(this);

            // Start CronTimer
            timer = new CronTimer(this);
            timer.setDaemon(this.daemon);
            timer.start();
            this.started = true;
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * 停止定时任务
     * 此方法调用后会将定时器进程立即结束，如果为守护线程模式，则正在执行的作业也会自动结束，否则作业线程将在执行完成后结束。
     * 此方法并不会清除任务表中的任务，请调用{@link #clear()} 方法清空任务或者使用{@link #stop(boolean)}方法可选是否清空
     *
     * @return this
     */
    public Scheduler stop() {
        return stop(false);
    }

    /**
     * 停止定时任务
     * 此方法调用后会将定时器进程立即结束，如果为守护线程模式，则正在执行的作业也会自动结束，否则作业线程将在执行完成后结束。
     *
     * @param clearTasks 是否清除所有任务
     * @return this
     */
    public Scheduler stop(final boolean clearTasks) {
        lock.lock();
        try {
            if (!started) {
                throw new IllegalStateException("Scheduler not started !");
            }

            // 停止CronTimer
            this.timer.stopTimer();
            this.timer = null;

            //停止线程池
            this.threadExecutor.shutdown();
            this.threadExecutor = null;

            //可选是否清空任务表
            if (clearTasks) {
                clear();
            }

            // 修改标志
            started = false;
        } finally {
            lock.unlock();
        }
        return this;
    }

    /**
     * 检查定时任务是否已经启动
     *
     * @throws CrontabException 已经启动则抛出此异常
     */
    private void checkStarted() throws CrontabException {
        if (this.started) {
            throw new CrontabException("Scheduler already started!");
        }
    }

}
