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
