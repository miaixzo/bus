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

/**
 * Task launcher responsible for checking if the {@link Repertoire} has any tasks that match the current execution time.
 * The launcher's thread terminates after the check is complete.
 *
 * @param scheduler the scheduler managing task execution
 * @param millis    the current time in milliseconds to check for task matches
 * @author Kimi Liu
 * @since Java 21+
 */
public record Launcher(Scheduler scheduler, long millis) implements Runnable {

    /**
     * Executes the check for matching tasks and notifies the manager upon completion. The second-matching behavior is
     * determined by the scheduler's configuration, while the year is never matched.
     */
    @Override
    public void run() {
        // Execute tasks that match the given millisecond timestamp.
        this.scheduler.repertoire.execute(this.scheduler, this.millis);

        // Notify the manager that this launcher has completed its run.
        this.scheduler.manager.notifyLauncherCompleted(this);
    }

}
