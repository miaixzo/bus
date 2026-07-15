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
package org.miaixz.bus.fabric.runtime.resource;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.logger.Logger;

/**
 * Owns closeable resources and releases them in reverse registration order.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ResourceScope implements AutoCloseable {

    /**
     * Registered resources.
     */
    private final ConcurrentLinkedDeque<AutoCloseable> resources;

    /**
     * Closed state.
     */
    private final AtomicBoolean closed;

    /**
     * Creates an empty resource scope.
     */
    private ResourceScope() {
        this.resources = new ConcurrentLinkedDeque<>();
        this.closed = new AtomicBoolean();
    }

    /**
     * Creates a resource scope.
     *
     * @return resource scope
     */
    public static ResourceScope create() {
        return new ResourceScope();
    }

    /**
     * Adds a closeable resource.
     *
     * @param resource resource
     * @param <T>      resource type
     * @return original resource
     */
    public synchronized <T extends AutoCloseable> T add(final T resource) {
        final T current = Assert.notNull(resource, () -> new ValidateException("Resource must not be null"));
        if (closed.get()) {
            try {
                current.close();
            } catch (final Exception e) {
                throw new InternalException("Unable to close rejected resource", e);
            }
            throw new StatefulException("Resource scope is closed");
        }
        resources.addLast(current);
        return current;
    }

    /**
     * Removes a resource without closing it.
     *
     * @param resource resource
     * @return true when removed
     */
    public synchronized boolean remove(final AutoCloseable resource) {
        final AutoCloseable current = Assert
                .notNull(resource, () -> new ValidateException("Resource must not be null"));
        return resources.remove(current);
    }

    /**
     * Returns registered resource count.
     *
     * @return resource count
     */
    public synchronized int size() {
        return resources.size();
    }

    /**
     * Closes all registered resources.
     */
    @Override
    public synchronized void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        Exception failure = null;
        AutoCloseable resource;
        while ((resource = resources.pollLast()) != null) {
            try {
                resource.close();
            } catch (final Exception e) {
                failure = ObjectKit.defaultIfNull(failure, e);
            }
        }
        if (failure != null) {
            throw new InternalException("Unable to close resource scope", failure);
        }
    }

    /**
     * Records a leak and closes the scope.
     *
     * @param owner owner object
     */
    public void leak(final Object owner) {
        final Object current = Assert.notNull(owner, () -> new ValidateException("Leak owner must not be null"));
        Logger.debug(false, "Fabric", "Resource scope leak detected: owner={}, resources={}", current, size());
        close();
    }

}
