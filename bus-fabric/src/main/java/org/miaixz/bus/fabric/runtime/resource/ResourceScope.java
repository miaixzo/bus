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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
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
    private final ArrayDeque<AutoCloseable> resources;

    /**
     * Identity set preventing duplicate registration and release.
     */
    private final Set<AutoCloseable> identities;

    /**
     * Cancellation shared with the owning lifecycle.
     */
    private final Cancellation cancellation;

    /**
     * Callback removing this scope from shared cancellation.
     */
    private final Runnable unregisterCancellation;

    /**
     * Closed state.
     */
    private final AtomicBoolean closed;

    /**
     * Creates an empty resource scope.
     *
     * @param cancellation shared cancellation
     */
    private ResourceScope(final Cancellation cancellation) {
        this.resources = new ArrayDeque<>();
        this.identities = Collections.newSetFromMap(new IdentityHashMap<>());
        this.cancellation = Assert.notNull(cancellation, () -> new ValidateException("Cancellation must not be null"));
        this.closed = new AtomicBoolean();
        this.unregisterCancellation = this.cancellation.onCancel(this::close);
    }

    /**
     * Creates a resource scope.
     *
     * @return resource scope
     */
    public static ResourceScope create() {
        return create(Cancellation.create());
    }

    /**
     * Creates a resource scope sharing an existing cancellation.
     *
     * @param cancellation shared cancellation
     * @return resource scope
     */
    public static ResourceScope create(final Cancellation cancellation) {
        return new ResourceScope(cancellation);
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
            } catch (final Throwable e) {
                throw new InternalException("Unable to close rejected resource", e);
            }
            throw new StatefulException("Resource scope is closed");
        }
        if (!identities.add(current)) {
            return current;
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
        if (!identities.remove(current)) {
            return false;
        }
        final Iterator<AutoCloseable> iterator = resources.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == current) {
                iterator.remove();
                break;
            }
        }
        return true;
    }

    /**
     * Returns registered resource count.
     *
     * @return resource count
     */
    public synchronized int size() {
        return identities.size();
    }

    /**
     * Records cancellation and releases all resources.
     *
     * @param cause cancellation cause
     * @return true when this invocation first cancelled the shared scope
     */
    public boolean cancel(final Throwable cause) {
        final Throwable current = Assert
                .notNull(cause, () -> new ValidateException("Cancellation cause must not be null"));
        if (unregisterCancellation != null) {
            unregisterCancellation.run();
        }
        final boolean changed = cancellation.cancel(current);
        close();
        return changed;
    }

    /**
     * Closes all registered resources.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        if (unregisterCancellation != null) {
            unregisterCancellation.run();
        }
        final List<AutoCloseable> closing = new ArrayList<>();
        synchronized (this) {
            AutoCloseable resource;
            while ((resource = resources.pollLast()) != null) {
                closing.add(resource);
            }
            identities.clear();
        }
        Throwable failure = null;
        for (final AutoCloseable resource : closing) {
            try {
                resource.close();
            } catch (final Throwable e) {
                if (failure == null) {
                    failure = e;
                } else if (failure != e) {
                    failure.addSuppressed(e);
                }
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
