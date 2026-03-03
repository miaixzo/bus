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
package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;

/**
 * An annotation that marks a method to be executed as a scheduled task. This allows for flexible scheduling of tasks
 * using cron expressions, fixed delays, or fixed rates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Scheduled {

    /**
     * A cron-like expression for scheduling the task. The specific syntax may depend on the underlying scheduling
     * framework (e.g., Quartz).
     *
     * @return The cron expression.
     */
    String cron() default Normal.EMPTY;

    /**
     * The fixed delay in milliseconds between the completion of the last execution and the start of the next. The value
     * can be a numeric string or a placeholder to be resolved from configuration.
     *
     * @return The fixed delay as a string.
     */
    String fixedDelay() default Normal.EMPTY;

    /**
     * The fixed period in milliseconds between the start of one execution and the start of the next. The value can be a
     * numeric string or a placeholder to be resolved from configuration.
     *
     * @return The fixed rate as a string.
     */
    String fixedRate() default Normal.EMPTY;

    /**
     * The initial delay in milliseconds before the first execution of the task. The value can be a numeric string or a
     * placeholder to be resolved from configuration.
     *
     * @return The initial delay as a string.
     */
    String initialDelay() default Normal.EMPTY;

    /**
     * If set to {@code true}, the annotated method will be executed once when the application starts. This is useful
     * for running initialization tasks.
     *
     * @return {@code true} to execute on application startup, {@code false} otherwise.
     */
    boolean onApplicationStart() default false;

    /**
     * Specifies whether the {@code onApplicationStart} execution should be asynchronous. If {@code true}, the task will
     * run in a separate thread, allowing the application startup to proceed without waiting for it to complete. This
     * setting only applies if {@link #onApplicationStart()} is {@code true}.
     *
     * @return {@code true} for asynchronous execution, {@code false} for synchronous.
     */
    boolean async() default false;

}
