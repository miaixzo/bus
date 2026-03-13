/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.tempus;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the creation and lifecycle of {@link Launcher} instances. Each time the cron timer ticks, a new
 * {@link Launcher} is spawned by this supervisor to check for tasks that should be executed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Supervisor implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852287618105L;
    /**
     * A list of currently active launchers.
     */
    protected final List<Launcher> launchers = new ArrayList<>();
    /**
     * The scheduler that this supervisor belongs to.
     */
    protected Scheduler scheduler;

    /**
     * Constructs a new Supervisor.
     *
     * @param scheduler The {@link Scheduler}.
     */
    public Supervisor(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Spawns and executes a new {@link Launcher}.
     *
     * @param millis The timestamp for the trigger event in milliseconds.
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
     * Called by a {@link Launcher} to notify the supervisor that it has completed its work. The completed launcher is
     * then removed from the list of active launchers.
     *
     * @param launcher The {@link Launcher} that has completed.
     */
    protected void notifyLauncherCompleted(final Launcher launcher) {
        synchronized (launchers) {
            launchers.remove(launcher);
        }
    }

}
