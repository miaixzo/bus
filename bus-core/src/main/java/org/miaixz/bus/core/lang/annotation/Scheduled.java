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
