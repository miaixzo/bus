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
package org.miaixz.bus.core.center.date;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒表封装 此工具用于存储一组任务的耗时时间，并一次性打印对比。 比如：我们可以记录多段代码耗时时间，然后一次性打印（StopWatch提供了一个prettyString()函数用于按照指定格式打印出耗时）
 *
 * <p>
 * 此工具来自：https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/util/StopWatch.java
 * </p>
 * 使用方法如下：
 *
 * <pre>{@code
 * StopWatch stopWatch = StopWatch.of("任务名称");
 *
 * // 任务1
 * stopWatch.start("任务一");
 * Thread.sleep(1000);
 * stopWatch.stop();
 *
 * // 任务2
 * stopWatch.start("任务二");
 * Thread.sleep(2000);
 * stopWatch.stop();
 *
 * // 打印出耗时
 * Console.log(stopWatch.prettyPrint());
 *
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StopWatch {

    /**
     * 秒表唯一标识，用于多个秒表对象的区分
     */
    private final String id;
    private List<TaskInfo> taskList;
    /**
     * 任务名称
     */
    private String currentTaskName;
    /**
     * 开始时间
     */
    private long startTimeNanos;
    /**
     * 最后一次任务对象
     */
    private TaskInfo lastTaskInfo;
    /**
     * 总任务数
     */
    private int taskCount;
    /**
     * 总运行时间
     */
    private long totalTimeNanos;

    /**
     * 构造，不启动任何任务
     */
    public StopWatch() {
        this(Normal.EMPTY);
    }

    /**
     * 构造，不启动任何任务
     *
     * @param id 用于标识秒表的唯一ID
     */
    public StopWatch(final String id) {
        this(id, true);
    }

    /**
     * 构造，不启动任何任务
     *
     * @param id           用于标识秒表的唯一ID
     * @param keepTaskList 是否在停止后保留任务，{@code false} 表示停止运行后不保留任务
     */
    public StopWatch(final String id, final boolean keepTaskList) {
        this.id = id;
        if (keepTaskList) {
            this.taskList = new ArrayList<>();
        }
    }

    /**
     * 创建计时任务（秒表）
     *
     * @return StopWatch
     */
    public static StopWatch of() {
        return new StopWatch();
    }

    /**
     * 创建计时任务（秒表）
     *
     * @param id 用于标识秒表的唯一ID
     * @return StopWatch
     */
    public static StopWatch of(final String id) {
        return new StopWatch(id);
    }

    /**
     * 获取StopWatch 的ID，用于多个秒表对象的区分
     *
     * @return the ID 默认为空字符串
     * @see #StopWatch(String)
     */
    public String getId() {
        return this.id;
    }

    /**
     * 设置是否在停止后保留任务，{@code false} 表示停止运行后不保留任务
     *
     * @param keepTaskList 是否在停止后保留任务
     */
    public void setKeepTaskList(final boolean keepTaskList) {
        if (keepTaskList) {
            if (null == this.taskList) {
                this.taskList = new ArrayList<>();
            }
        } else {
            this.taskList = null;
        }
    }

    /**
     * 开始默认的新任务
     *
     * @throws IllegalStateException 前一个任务没有结束
     */
    public void start() throws IllegalStateException {
        start(Normal.EMPTY);
    }

    /**
     * 开始指定名称的新任务
     *
     * @param taskName 新开始的任务名称
     * @throws IllegalStateException 前一个任务没有结束
     */
    public void start(final String taskName) throws IllegalStateException {
        if (null != this.currentTaskName) {
            throw new IllegalStateException("Can't start StopWatch: it's already running");
        }
        this.currentTaskName = taskName;
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * 停止当前任务
     *
     * @throws IllegalStateException 任务没有开始
     */
    public void stop() throws IllegalStateException {
        if (null == this.currentTaskName) {
            throw new IllegalStateException("Can't stop StopWatch: it's not running");
        }

        final long lastTime = System.nanoTime() - this.startTimeNanos;
        this.totalTimeNanos += lastTime;
        this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
        if (null != this.taskList) {
            this.taskList.add(this.lastTaskInfo);
        }
        ++this.taskCount;
        this.currentTaskName = null;
    }

    /**
     * 检查是否有正在运行的任务
     *
     * @return 是否有正在运行的任务
     * @see #currentTaskName()
     */
    public boolean isRunning() {
        return (this.currentTaskName != null);
    }

    /**
     * 获取当前任务名，{@code null} 表示无任务
     *
     * @return 当前任务名，{@code null} 表示无任务
     * @see #isRunning()
     */
    public String currentTaskName() {
        return this.currentTaskName;
    }

    /**
     * 获取最后任务的花费时间（纳秒）
     *
     * @return 任务的花费时间（纳秒）
     * @throws IllegalStateException 无任务
     */
    public long getLastTaskTimeNanos() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task interval");
        }
        return this.lastTaskInfo.getTimeNanos();
    }

    /**
     * 获取最后任务的花费时间（毫秒）
     *
     * @return 任务的花费时间（毫秒）
     * @throws IllegalStateException 无任务
     */
    public long getLastTaskTimeMillis() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task interval");
        }
        return this.lastTaskInfo.getTimeMillis();
    }

    /**
     * 获取最后的任务名
     *
     * @return 任务名
     * @throws IllegalStateException 无任务
     */
    public String getLastTaskName() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task name");
        }
        return this.lastTaskInfo.getTaskName();
    }

    /**
     * 获取最后的任务对象
     *
     * @return {@link TaskInfo} 任务对象，包括任务名和花费时间
     * @throws IllegalStateException 无任务
     */
    public TaskInfo getLastTaskInfo() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task info");
        }
        return this.lastTaskInfo;
    }

    /**
     * 获取所有任务的总花费时间
     *
     * @param unit 时间单位，{@code null}表示默认{@link TimeUnit#NANOSECONDS}
     * @return 花费时间
     */
    public long getTotal(final TimeUnit unit) {
        return unit.convert(this.totalTimeNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * 获取所有任务的总花费时间（纳秒）
     *
     * @return 所有任务的总花费时间（纳秒）
     * @see #getTotalTimeMillis()
     * @see #getTotalTimeSeconds()
     */
    public long getTotalTimeNanos() {
        return this.totalTimeNanos;
    }

    /**
     * 获取所有任务的总花费时间（毫秒）
     *
     * @return 所有任务的总花费时间（毫秒）
     * @see #getTotalTimeNanos()
     * @see #getTotalTimeSeconds()
     */
    public long getTotalTimeMillis() {
        return getTotal(TimeUnit.MILLISECONDS);
    }

    /**
     * 获取所有任务的总花费时间（秒）
     *
     * @return 所有任务的总花费时间（秒）
     * @see #getTotalTimeNanos()
     * @see #getTotalTimeMillis()
     */
    public double getTotalTimeSeconds() {
        return DateKit.nanosToSeconds(this.totalTimeNanos);
    }

    /**
     * 获取任务数
     *
     * @return 任务数
     */
    public int getTaskCount() {
        return this.taskCount;
    }

    /**
     * 获取任务列表
     *
     * @return 任务列表
     */
    public TaskInfo[] getTaskInfo() {
        if (null == this.taskList) {
            throw new UnsupportedOperationException("Task info is not being kept!");
        }
        return this.taskList.toArray(new TaskInfo[0]);
    }

    /**
     * 获取任务信息，类似于：
     * 
     * <pre>
     *     StopWatch '[data]': running time = [total] ns
     * </pre>
     *
     * @return 任务信息
     */
    public String shortSummary() {
        return shortSummary(null);
    }

    /**
     * 获取任务信息，类似于：
     * 
     * <pre>
     *     StopWatch '[data]': running time = [total] [unit]
     * </pre>
     *
     * @param unit 时间单位，{@code null}则默认为{@link TimeUnit#NANOSECONDS}
     * @return 任务信息
     */
    public String shortSummary(TimeUnit unit) {
        if (null == unit) {
            unit = TimeUnit.NANOSECONDS;
        }
        return StringKit.format("StopWatch '{}': running time = {} {}", this.id, getTotal(unit),
                DateKit.getShortName(unit));
    }

    /**
     * 生成所有任务的一个任务花费时间表，单位纳秒
     *
     * @return 任务时间表
     */
    public String prettyPrint() {
        return prettyPrint(null);
    }

    /**
     * 生成所有任务的一个任务花费时间表
     *
     * @param unit 时间单位，{@code null}则默认{@link TimeUnit#NANOSECONDS} 纳秒
     * @return 任务时间表
     */
    public String prettyPrint(TimeUnit unit) {
        if (null == unit) {
            unit = TimeUnit.NANOSECONDS;
        }

        final StringBuilder sb = new StringBuilder(shortSummary(unit));
        sb.append(FileKit.getLineSeparator());
        if (null == this.taskList) {
            sb.append("No task info kept");
        } else {
            sb.append("---------------------------------------------").append(FileKit.getLineSeparator());
            sb.append(DateKit.getShortName(unit)).append("          %     Task name")
                    .append(FileKit.getLineSeparator());
            sb.append("---------------------------------------------").append(FileKit.getLineSeparator());

            final NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setGroupingUsed(false);

            final NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setGroupingUsed(false);

            for (final TaskInfo task : getTaskInfo()) {
                final String taskTimeStr = nf.format(task.getTime(unit));
                sb.append(taskTimeStr);
                if (taskTimeStr.length() < 11) {
                    sb.append(StringKit.repeat(Symbol.C_SPACE, 11 - taskTimeStr.length()));
                }

                final String percentStr = pf.format((double) task.getTimeNanos() / getTotalTimeNanos());
                if (percentStr.length() < 4) {
                    sb.append(StringKit.repeat(Symbol.C_SPACE, 4 - percentStr.length()));
                }
                sb.append(percentStr).append("   ");
                sb.append(task.getTaskName()).append(FileKit.getLineSeparator());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(shortSummary());
        if (null != this.taskList) {
            for (final TaskInfo task : this.taskList) {
                sb.append("; [").append(task.getTaskName()).append("] took ").append(task.getTimeNanos()).append(" ns");
                final long percent = Math.round(100.0 * task.getTimeNanos() / getTotalTimeNanos());
                sb.append(" = ").append(percent).append(Symbol.PERCENT);
            }
        } else {
            sb.append("; no task info kept");
        }
        return sb.toString();
    }

    /**
     * 存放任务名称和花费时间对象
     */
    public static final class TaskInfo {

        private final String taskName;
        private final long timeNanos;

        /**
         * 构造
         *
         * @param taskName  任务名称
         * @param timeNanos 花费时间（纳秒）
         */
        TaskInfo(final String taskName, final long timeNanos) {
            this.taskName = taskName;
            this.timeNanos = timeNanos;
        }

        /**
         * 获取任务名
         *
         * @return 任务名
         */
        public String getTaskName() {
            return this.taskName;
        }

        /**
         * 获取指定单位的任务花费时间
         *
         * @param unit 单位
         * @return 任务花费时间
         */
        public long getTime(final TimeUnit unit) {
            return unit.convert(this.timeNanos, TimeUnit.NANOSECONDS);
        }

        /**
         * 获取任务花费时间（单位：纳秒）
         *
         * @return 任务花费时间（单位：纳秒）
         * @see #getTimeMillis()
         * @see #getTimeSeconds()
         */
        public long getTimeNanos() {
            return this.timeNanos;
        }

        /**
         * 获取任务花费时间（单位：毫秒）
         *
         * @return 任务花费时间（单位：毫秒）
         * @see #getTimeNanos()
         * @see #getTimeSeconds()
         */
        public long getTimeMillis() {
            return getTime(TimeUnit.MILLISECONDS);
        }

        /**
         * 获取任务花费时间（单位：秒）
         *
         * @return 任务花费时间（单位：秒）
         * @see #getTimeMillis()
         * @see #getTimeNanos()
         */
        public double getTimeSeconds() {
            return DateKit.nanosToSeconds(this.timeNanos);
        }
    }

}
