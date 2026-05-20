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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tempus.Executor;

/**
 * Manages and dispatches events to a collection of {@link TaskListener} instances. This class provides a centralized
 * way to handle task lifecycle events.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TaskListenerManager implements Serializable {

    /**
     * Constructs a new TaskListenerManager instance.
     */
    public TaskListenerManager() {
        // No initialization required.
    }

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
            Logger.debug(
                    false,
                    "Tempus",
                    "Task listener added: listenerType={}, listenerCount={}",
                    listener == null ? null : listener.getClass().getName(),
                    this.listeners.size());
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
            boolean removed = this.listeners.remove(listener);
            Logger.debug(
                    false,
                    "Tempus",
                    "Task listener removed: listenerType={}, removed={}, listenerCount={}",
                    listener == null ? null : listener.getClass().getName(),
                    removed,
                    this.listeners.size());
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
            Logger.debug(
                    true,
                    "Tempus",
                    "Task listener start notification started: taskId={}, listenerCount={}",
                    executor == null || executor.task() == null ? null : executor.task().getId(),
                    listeners.size());
            for (final TaskListener listener : listeners) {
                if (null != listener) {
                    listener.onStart(executor);
                }
            }
            Logger.debug(
                    false,
                    "Tempus",
                    "Task listener start notification completed: taskId={}, listenerCount={}",
                    executor == null || executor.task() == null ? null : executor.task().getId(),
                    listeners.size());
        }
    }

    /**
     * Notifies all registered listeners that a task has completed successfully.
     *
     * @param executor The {@link Executor} for the task that has succeeded.
     */
    public void notifyTaskSucceeded(final Executor executor) {
        synchronized (listeners) {
            Logger.debug(
                    true,
                    "Tempus",
                    "Task listener success notification started: taskId={}, listenerCount={}",
                    executor == null || executor.task() == null ? null : executor.task().getId(),
                    listeners.size());
            for (final TaskListener listener : listeners) {
                listener.onSucceeded(executor);
            }
            Logger.debug(
                    false,
                    "Tempus",
                    "Task listener success notification completed: taskId={}, listenerCount={}",
                    executor == null || executor.task() == null ? null : executor.task().getId(),
                    listeners.size());
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
                Logger.debug(
                        true,
                        "Tempus",
                        "Task listener failure notification started: taskId={}, listenerCount={}",
                        executor == null || executor.task() == null ? null : executor.task().getId(),
                        listeners.size());
                for (final TaskListener listener : listeners) {
                    listener.onFailed(executor, exception);
                }
                Logger.debug(
                        false,
                        "Tempus",
                        "Task listener failure notification completed: taskId={}, listenerCount={}",
                        executor == null || executor.task() == null ? null : executor.task().getId(),
                        listeners.size());
            } else {
                Logger.error(
                        false,
                        "Tempus",
                        exception,
                        "Task failed without registered listener: taskId={}, exception={}",
                        executor == null || executor.task() == null ? null : executor.task().getId(),
                        exception == null ? null : exception.getClass().getSimpleName());
            }
        }
    }

}
