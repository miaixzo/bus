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
package org.miaixz.bus.cron.crontab;

/**
 * Interface for a scheduled job to be executed.
 * <p>
 * Job execution is asynchronous. This means that executions of different jobs, or even multiple executions of the same
 * job, are independent of each other. If a previously scheduled execution of a job has not yet completed, a new
 * execution will start at its scheduled time without waiting for the previous one to finish. To ensure mutual
 * exclusion, you must implement your own locking mechanism within the job.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface Crontab {

    /**
     * Executes the scheduled task.
     * <p>
     * Implementations should handle any exceptions that may occur during execution. By default, exceptions are caught
     * and passed to registered listeners. If no listeners are configured, exceptions will be silently ignored. It is
     * therefore recommended to handle exceptions within the method body.
     * </p>
     */
    void execute();

}
