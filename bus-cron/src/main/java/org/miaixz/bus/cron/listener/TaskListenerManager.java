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
import org.miaixz.bus.logger.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages and dispatches events to a collection of {@link TaskListener} instances. This class provides a centralized
 * way to handle task lifecycle events.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TaskListenerManager implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852287805158L;

    private final List<TaskListener> listeners = new ArrayList<>();

    /**
     * Adds a {@link TaskListener} to the manager. The listener will be notified of task execution events.
     *
     * @param listener The {@link TaskListener} to add.
     * @return this {@link TaskListenerManager} instance for chaining.
     */
    public TaskListenerManager addListener(final TaskListener listener) {
        synchronized (listeners) {
            this.listeners.add(listener);
        }
        return this;
    }

    /**
     * Removes a {@link TaskListener} from the manager. The listener will no longer receive task execution events.
     *
     * @param listener The {@link TaskListener} to remove.
     * @return this {@link TaskListenerManager} instance for chaining.
     */
    public TaskListenerManager removeListener(final TaskListener listener) {
        synchronized (listeners) {
            this.listeners.remove(listener);
        }
        return this;
    }

    /**
     * Notifies all registered listeners that a task is about to start.
     *
     * @param executor The {@link Executor} for the task that is starting.
     */
    public void notifyTaskStart(final Executor executor) {
        synchronized (listeners) {
            for (final TaskListener listener : listeners) {
                if (null != listener) {
                    listener.onStart(executor);
                }
            }
        }
    }

    /**
     * Notifies all registered listeners that a task has completed successfully.
     *
     * @param executor The {@link Executor} for the task that has succeeded.
     */
    public void notifyTaskSucceeded(final Executor executor) {
        synchronized (listeners) {
            for (final TaskListener listener : listeners) {
                listener.onSucceeded(executor);
            }
        }
    }

    /**
     * Notifies all registered listeners that a task has failed. If no listeners are registered, the exception is logged
     * as an error.
     *
     * @param executor  The {@link Executor} for the task that has failed.
     * @param exception The exception that caused the failure.
     */
    public void notifyTaskFailed(final Executor executor, final Throwable exception) {
        synchronized (listeners) {
            if (!listeners.isEmpty()) {
                for (final TaskListener listener : listeners) {
                    listener.onFailed(executor, exception);
                }
            } else {
                Logger.error(exception, exception.getMessage());
            }
        }
    }

}
