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
package org.miaixz.bus.cron.listener;

import org.miaixz.bus.cron.Executor;

/**
 * A simple no-op implementation of {@link TaskListener}. Extend this class and override only the methods you need.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SimpleTaskListener implements TaskListener {

    /**
     * Triggered when a task is about to start.
     * <p>
     * Description inherited from parent interface.
     *
     * @param executor The {@link Executor} for the task that is starting.
     */
    @Override
    public void onStart(final Executor executor) {
        // Do nothing
    }

    /**
     * Triggered when a task has completed successfully.
     * <p>
     * Description inherited from parent interface.
     *
     * @param executor The {@link Executor} for the task that has succeeded.
     */
    @Override
    public void onSucceeded(final Executor executor) {
        // Do nothing
    }

    /**
     * Triggered when a task fails to execute.
     * <p>
     * Description inherited from parent interface.
     *
     * @param executor  The {@link Executor} for the task that has failed.
     * @param exception The exception that caused the failure.
     */
    @Override
    public void onFailed(final Executor executor, final Throwable exception) {
        // Do nothing
    }

}
