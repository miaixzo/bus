/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
