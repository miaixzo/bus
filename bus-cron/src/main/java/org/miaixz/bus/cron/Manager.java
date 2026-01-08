/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.cron.crontab.CronCrontab;

/**
 * Manages the execution of jobs, including starting and stopping them.
 * <p>
 * This class keeps track of currently running jobs, adding them to a list upon startup and removing them upon
 * completion.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Manager implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852287209701L;

    /**
     * List of running task executors.
     */
    private final List<Executor> executors = new ArrayList<>();

    /**
     * List of task launchers.
     */
    protected final List<Launcher> launchers = new ArrayList<>();
    /**
     * The scheduler that this manager belongs to.
     */
    protected Scheduler scheduler;

    /**
     * Constructs a new Manager.
     *
     * @param scheduler The {@link Scheduler} that this manager belongs to.
     */
    public Manager(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Gets all currently executing task executors.
     *
     * @return A list of task executors.
     */
    public List<Executor> getExecutors() {
        return ListKit.view(this.executors);
    }

    /**
     * Gets an unmodifiable list of all launchers.
     *
     * @return A list of launchers.
     */
    public List<Launcher> getLaunchers() {
        return ListKit.view(this.launchers);
    }

    /**
     * Spawns a TaskExecutor, effectively starting a job.
     *
     * @param crontab The {@link CronCrontab} to execute.
     * @return The newly created {@link Executor}.
     */
    public Executor spawnExecutor(final CronCrontab crontab) {
        final Executor executor = new Executor(this.scheduler, crontab);
        synchronized (this.executors) {
            this.executors.add(executor);
        }
        this.scheduler.threadExecutor.execute(executor);
        return executor;
    }

    /**
     * Spawns a TaskLauncher.
     *
     * @param millis The timestamp in milliseconds for the trigger event.
     * @return The newly created {@link Launcher}.
     */
    protected Launcher spawnLauncher(final long millis) {
        final Launcher launcher = new Launcher(this.scheduler, millis);
        synchronized (this.launchers) {
            this.launchers.add(launcher);
        }
        this.scheduler.threadExecutor.execute(launcher);
        return launcher;
    }

    /**
     * This method is called when an executor has finished its execution. It removes the executor from the list of
     * running executors. This method is intended to be called by an {@link Executor} instance to notify the manager of
     * its completion.
     *
     * @param executor The {@link Executor} that has completed.
     */
    public void notifyExecutorCompleted(final Executor executor) {
        synchronized (executors) {
            executors.remove(executor);
        }
    }

    /**
     * Called when a launcher has completed its task, removing it from the list of launchers.
     *
     * @param launcher The {@link Launcher} that has completed.
     */
    protected void notifyLauncherCompleted(final Launcher launcher) {
        synchronized (launchers) {
            launchers.remove(launcher);
        }
    }

}
