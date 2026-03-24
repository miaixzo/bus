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
package org.miaixz.bus.tempus.listener;

import org.miaixz.bus.tempus.Executor;
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
 * @since Java 21+
 */
public class TaskListenerManager implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852287805158L;

    /**
     * The list of task listeners to notify of events.
     */
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
