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
package org.miaixz.bus.metrics.guard;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.nimble.MetricRegistration;

/**
 * Transactional group of registrations released in reverse order.
 *
 * @author Kimi Liu
 */
public final class RegistrationGroup implements MetricRegistration {

    /**
     * Registrations in reverse release order.
     */
    private final Deque<MetricRegistration> registrations = new ArrayDeque<>();
    /**
     * Whether construction completed successfully.
     */
    private boolean committed;
    /**
     * Whether all owned registrations have been released.
     */
    private boolean closed;

    /**
     * Creates an empty registration group.
     */
    public RegistrationGroup() {
        // No initialization required.
    }

    /**
     * Adds a registration to this transaction.
     *
     * @param registration registration to own
     * @param <T>          registration type
     * @return the same registration
     * @throws IllegalStateException if this group is already committed or closed
     */
    public synchronized <T extends MetricRegistration> T add(T registration) {
        T checked = Objects.requireNonNull(registration, "Metric registration must not be null");
        if (closed) {
            checked.close();
            throw new IllegalStateException("Registration group is already closed");
        }
        if (committed) {
            checked.close();
            throw new IllegalStateException("Registration group is already committed");
        }
        registrations.push(checked);
        return checked;
    }

    /**
     * Marks registration construction successful. The group remains the owner until {@link #close()}.
     *
     * @throws IllegalStateException if this group is already closed
     */
    public synchronized void commit() {
        if (closed) {
            throw new IllegalStateException("Registration group is already closed");
        }
        committed = true;
    }

    /**
     * Returns whether construction was committed.
     *
     * @return commit state
     */
    public synchronized boolean isCommitted() {
        return committed;
    }

    /**
     * Rolls back or releases the group in reverse registration order.
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        int failures = 0;
        RuntimeException first = null;
        while (!registrations.isEmpty()) {
            try {
                registrations.pop().close();
            } catch (RuntimeException exception) {
                failures++;
                if (first == null) {
                    first = exception;
                } else {
                    first.addSuppressed(exception);
                }
            }
        }
        if (failures > 0) {
            Logger.warn(
                    false,
                    "Metrics",
                    "Metric registration group close completed with failures: failureCount={}, firstError={}",
                    failures,
                    first == null ? null : first.getMessage());
        }
    }

}
