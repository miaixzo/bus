/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * StopWatch encapsulation. This tool is used to store the elapsed time of a group of tasks and print them for
 * comparison. For example, we can record the elapsed time of multiple code segments and then print them all at once
 * (StopWatch provides a prettyString() function to print the elapsed time in a specified format).
 *
 * <p>
 * This tool is inspired by:
 * https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/util/StopWatch.java
 * 
 * Usage example:
 *
 * <pre>{@code
 * StopWatch stopWatch = StopWatch.of("任务名称");
 *
 * // Task 1
 * stopWatch.start("任务一");
 * Thread.sleep(1000);
 * stopWatch.stop();
 *
 * // Task 2
 * stopWatch.start("任务二");
 * Thread.sleep(2000);
 * stopWatch.stop();
 *
 * // Print elapsed time
 * Console.log(stopWatch.prettyPrint());
 *
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StopWatch {

    /**
     * Unique ID for the StopWatch, used to distinguish between multiple StopWatch objects.
     */
    private final String id;
    /**
     * List of tasks.
     */
    private List<TaskInfo> taskList;
    /**
     * Current task name.
     */
    private String currentTaskName;
    /**
     * Start time of the current task in nanoseconds.
     */
    private long startTimeNanos;
    /**
     * Information about the last completed task.
     */
    private TaskInfo lastTaskInfo;
    /**
     * Total number of tasks.
     */
    private int taskCount;
    /**
     * Total elapsed time in nanoseconds.
     */
    private long totalTimeNanos;

    /**
     * Constructs a StopWatch without starting any task.
     */
    public StopWatch() {
        this(Normal.EMPTY);
    }

    /**
     * Constructs a StopWatch without starting any task.
     *
     * @param id A unique ID to identify the StopWatch.
     */
    public StopWatch(final String id) {
        this(id, true);
    }

    /**
     * Constructs a StopWatch without starting any task.
     *
     * @param id           A unique ID to identify the StopWatch.
     * @param keepTaskList Whether to keep the task list after stopping. {@code false} means tasks are not retained
     *                     after stopping.
     */
    public StopWatch(final String id, final boolean keepTaskList) {
        this.id = id;
        if (keepTaskList) {
            this.taskList = new ArrayList<>();
        }
    }

    /**
     * Creates a new StopWatch instance.
     *
     * @return A new StopWatch instance.
     */
    public static StopWatch of() {
        return new StopWatch();
    }

    /**
     * Creates a new StopWatch instance with a given ID.
     *
     * @param id A unique ID to identify the StopWatch.
     * @return A new StopWatch instance.
     */
    public static StopWatch of(final String id) {
        return new StopWatch(id);
    }

    /**
     * Gets the ID of this StopWatch, used to distinguish between multiple StopWatch objects.
     *
     * @return The ID. Defaults to an empty string if not specified.
     * @see #StopWatch(String)
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets whether to keep the task list after stopping. {@code false} means tasks are not retained after stopping.
     *
     * @param keepTaskList Whether to keep the task list.
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
     * Starts a new task with a default name (empty string).
     *
     * @throws IllegalStateException if a previous task has not been stopped.
     */
    public void start() throws IllegalStateException {
        start(Normal.EMPTY);
    }

    /**
     * Starts a new task with the specified name.
     *
     * @param taskName The name of the new task to start.
     * @throws IllegalStateException if a previous task has not been stopped.
     */
    public void start(final String taskName) throws IllegalStateException {
        if (null != this.currentTaskName) {
            throw new IllegalStateException("Can't start StopWatch: it's already running");
        }
        this.currentTaskName = taskName;
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * Stops the current running task.
     *
     * @throws IllegalStateException if no task is currently running.
     */
    public void stop() throws IllegalStateException {
        if (null == this.currentTaskName) {
            throw new IllegalStateException("Can't stop StopWatch: it's not running");
        }

        final long lastTime = Math.max(System.nanoTime() - this.startTimeNanos, 0);
        this.totalTimeNanos += lastTime;
        this.lastTaskInfo = new TaskInfo(this.currentTaskName, lastTime);
        if (null != this.taskList) {
            this.taskList.add(this.lastTaskInfo);
        }
        ++this.taskCount;
        this.currentTaskName = null;
    }

    /**
     * Checks if there is a task currently running.
     *
     * @return {@code true} if a task is running, {@code false} otherwise.
     * @see #currentTaskName()
     */
    public boolean isRunning() {
        return (this.currentTaskName != null);
    }

    /**
     * Gets the name of the current running task.
     *
     * @return The name of the current task, or {@code null} if no task is running.
     * @see #isRunning()
     */
    public String currentTaskName() {
        return this.currentTaskName;
    }

    /**
     * Gets the elapsed time of the last task in nanoseconds.
     *
     * @return The elapsed time of the last task in nanoseconds.
     * @throws IllegalStateException if no tasks have been run.
     */
    public long getLastTaskTimeNanos() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task interval");
        }
        return this.lastTaskInfo.getTimeNanos();
    }

    /**
     * Gets the elapsed time of the last task in milliseconds.
     *
     * @return The elapsed time of the last task in milliseconds.
     * @throws IllegalStateException if no tasks have been run.
     */
    public long getLastTaskTimeMillis() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task interval");
        }
        return this.lastTaskInfo.getTimeMillis();
    }

    /**
     * Gets the name of the last completed task.
     *
     * @return The name of the last task.
     * @throws IllegalStateException if no tasks have been run.
     */
    public String getLastTaskName() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task name");
        }
        return this.lastTaskInfo.getTaskName();
    }

    /**
     * Gets the last completed task information object.
     *
     * @return The {@link TaskInfo} object, including task name and elapsed time.
     * @throws IllegalStateException if no tasks have been run.
     */
    public TaskInfo getLastTaskInfo() throws IllegalStateException {
        if (this.lastTaskInfo == null) {
            throw new IllegalStateException("No tasks run: can't get last task info");
        }
        return this.lastTaskInfo;
    }

    /**
     * Gets the total elapsed time of all tasks.
     *
     * @param unit The time unit to return the total time in. {@code null} defaults to {@link TimeUnit#NANOSECONDS}.
     * @return The total elapsed time.
     */
    public long getTotal(final TimeUnit unit) {
        return unit.convert(this.totalTimeNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Gets the total elapsed time of all tasks in nanoseconds.
     *
     * @return The total elapsed time of all tasks in nanoseconds.
     * @see #getTotalTimeMillis()
     * @see #getTotalTimeSeconds()
     */
    public long getTotalTimeNanos() {
        return this.totalTimeNanos;
    }

    /**
     * Gets the total elapsed time of all tasks in milliseconds.
     *
     * @return The total elapsed time of all tasks in milliseconds.
     * @see #getTotalTimeNanos()
     * @see #getTotalTimeSeconds()
     */
    public long getTotalTimeMillis() {
        return getTotal(TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the total elapsed time of all tasks in seconds.
     *
     * @return The total elapsed time of all tasks in seconds.
     * @see #getTotalTimeNanos()
     * @see #getTotalTimeMillis()
     */
    public double getTotalTimeSeconds() {
        return DateKit.nanosToSeconds(this.totalTimeNanos);
    }

    /**
     * Gets the number of tasks recorded.
     *
     * @return The number of tasks.
     */
    public int getTaskCount() {
        return this.taskCount;
    }

    /**
     * Gets the list of task information.
     *
     * @return An array of {@link TaskInfo} objects.
     * @throws UnsupportedOperationException if task info is not being kept (i.e., {@code keepTaskList} was
     *                                       {@code false}).
     */
    public TaskInfo[] getTaskInfo() {
        if (null == this.taskList) {
            throw new UnsupportedOperationException("Task info is not being kept!");
        }
        return this.taskList.toArray(new TaskInfo[0]);
    }

    /**
     * Generates a short summary of the StopWatch, similar to:
     *
     * <pre>
     *     StopWatch '[id]': running time = [total] ns
     * </pre>
     *
     * @return A short summary string.
     */
    public String shortSummary() {
        return shortSummary(null);
    }

    /**
     * Generates a short summary of the StopWatch, similar to:
     *
     * <pre>
     *     StopWatch '[id]': running time = [total] [unit]
     * </pre>
     *
     * @param unit The time unit. {@code null} defaults to {@link TimeUnit#NANOSECONDS}.
     * @return A short summary string.
     */
    public String shortSummary(TimeUnit unit) {
        if (null == unit) {
            unit = TimeUnit.NANOSECONDS;
        }
        return StringKit
                .format("StopWatch '{}': running time = {} {}", this.id, getTotal(unit), DateKit.getShortName(unit));
    }

    /**
     * Generates a formatted table of all task elapsed times, in nanoseconds.
     *
     * @return A pretty-printed table of task times.
     */
    public String prettyPrint() {
        return prettyPrint(null);
    }

    /**
     * Generates a formatted table of all task elapsed times.
     *
     * @param unit The time unit. {@code null} defaults to {@link TimeUnit#NANOSECONDS}.
     * @return A pretty-printed table of task times.
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

    /**
     * Returns a string representation of the StopWatch, including a summary and details of each task.
     *
     * @return A string representation of the object.
     */
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
     * Represents information about a single task, including its name and elapsed time.
     */
    public static final class TaskInfo {

        private final String taskName;
        private final long timeNanos;

        /**
         * Constructs a TaskInfo object.
         *
         * @param taskName  The name of the task.
         * @param timeNanos The elapsed time of the task in nanoseconds.
         */
        TaskInfo(final String taskName, final long timeNanos) {
            this.taskName = taskName;
            this.timeNanos = timeNanos;
        }

        /**
         * Gets the name of the task.
         *
         * @return The task name.
         */
        public String getTaskName() {
            return this.taskName;
        }

        /**
         * Gets the elapsed time of the task in the specified unit.
         *
         * @param unit The time unit.
         * @return The elapsed time of the task.
         */
        public long getTime(final TimeUnit unit) {
            return unit.convert(this.timeNanos, TimeUnit.NANOSECONDS);
        }

        /**
         * Gets the elapsed time of the task in nanoseconds.
         *
         * @return The elapsed time of the task in nanoseconds.
         * @see #getTimeMillis()
         * @see #getTimeSeconds()
         */
        public long getTimeNanos() {
            return this.timeNanos;
        }

        /**
         * Gets the elapsed time of the task in milliseconds.
         *
         * @return The elapsed time of the task in milliseconds.
         * @see #getTimeNanos()
         * @see #getTimeSeconds()
         */
        public long getTimeMillis() {
            return getTime(TimeUnit.MILLISECONDS);
        }

        /**
         * Gets the elapsed time of the task in seconds.
         *
         * @return The elapsed time of the task in seconds.
         * @see #getTimeMillis()
         * @see #getTimeNanos()
         */
        public double getTimeSeconds() {
            return DateKit.nanosToSeconds(this.timeNanos);
        }
    }

}
