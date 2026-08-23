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
package org.miaixz.bus.core.instance;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Instance factory class.
 *
 * @author Kimi Liu
 */
public class InstanceFactory implements Instance {

    /**
     * Process-wide singleton slots.
     */
    private final Map<String, Slot> singletonMap = new ConcurrentHashMap<>();

    /**
     * Thread-local map object.
     */
    private final ThreadLocal<Map<String, Object>> mapThreadLocal = new ThreadLocal<>();

    /**
     * Creates an instance factory with independent thread-local storage.
     */
    public InstanceFactory() {
        // No initialization required.
    }

    /**
     * Gets the singleton instance of the factory.
     *
     * @return The instance factory.
     */
    public static InstanceFactory getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Gets a singleton instance using a static method.
     *
     * @param <T>   The generic type.
     * @param clazz The class information.
     * @return The singleton instance.
     */
    public static <T> T singletion(Class<T> clazz) {
        return getInstance().singleton(clazz);
    }

    /**
     * Gets a singleton instance from a specific group using a static method.
     *
     * @param <T>       The generic type.
     * @param clazz     The class information.
     * @param groupName The group name.
     * @return The singleton instance.
     */
    public static <T> T singletion(Class<T> clazz, final String groupName) {
        return getInstance().singleton(clazz, groupName);
    }

    /**
     * Singleton method.
     */
    @Override
    public <T> T singleton(Class<T> clazz, String groupName) {
        this.notNull(clazz);
        Assert.notEmpty(groupName, "id");
        final String key = clazz.getName() + Symbol.MINUS + groupName;
        return singleton(key, () -> this.multiple(clazz));
    }

    /**
     * Singleton method.
     */
    @Override
    public <T> T singleton(Class<T> clazz) {
        this.notNull(clazz);
        return singleton(clazz.getName(), () -> this.multiple(clazz));
    }

    /**
     * Gets or creates a process-wide singleton for a custom key.
     *
     * @param key      The singleton key.
     * @param supplier The singleton supplier.
     * @param <T>      The singleton type.
     * @return The resolved singleton, or {@code null} when the supplier returns {@code null}.
     */
    public <T> T singleton(final String key, final SupplierX<T> supplier) {
        while (true) {
            final Slot existing = singletonMap.get(key);
            if (null != existing) {
                return (T) existing.await(key);
            }

            final Slot created = new Slot();
            if (null != singletonMap.putIfAbsent(key, created)) {
                continue;
            }

            try {
                final T value = supplier.get();
                if (null == value) {
                    if (created.complete(null)) {
                        singletonMap.remove(key, created);
                        return null;
                    }
                    return (T) created.await(key);
                }
                created.complete(value);
                return (T) created.await(key);
            } catch (final RuntimeException | Error cause) {
                if (created.fail(cause)) {
                    singletonMap.remove(key, created);
                    throw cause;
                }
                return (T) created.await(key);
            }
        }
    }

    /**
     * Puts an existing process-wide singleton.
     *
     * @param key   The singleton key.
     * @param value The singleton value.
     */
    public void put(final String key, final Object value) {
        Assert.notNull(value, "Bean object must be not null !");
        while (true) {
            final Slot existing = singletonMap.get(key);
            if (null == existing) {
                if (null == singletonMap.putIfAbsent(key, Slot.completed(value))) {
                    return;
                }
            } else if (existing.complete(value)) {
                return;
            } else if (singletonMap.replace(key, existing, Slot.completed(value))) {
                return;
            }
        }
    }

    /**
     * Tests whether a completed process-wide singleton exists.
     *
     * @param key The singleton key.
     * @return {@code true} when a completed singleton exists.
     */
    public boolean exists(final String key) {
        final Slot slot = singletonMap.get(key);
        return null != slot && slot.isReady();
    }

    /**
     * Gets the classes of all completed process-wide singletons.
     *
     * @return The singleton classes.
     */
    public Set<Class<?>> getExistClass() {
        return singletonMap.values().stream().map(Slot::value).filter(value -> null != value).map(Object::getClass)
                .collect(Collectors.toSet());
    }

    /**
     * Removes a process-wide singleton.
     *
     * @param key The singleton key.
     */
    public void remove(final String key) {
        singletonMap.remove(key);
    }

    /**
     * Clears all process-wide singletons.
     */
    public void destroy() {
        singletonMap.clear();
    }

    /**
     * Threadlocal method.
     */
    @Override
    public <T> T threadLocal(Class<T> clazz) {
        this.notNull(clazz);

        // 1. Check if the map exists.
        Map<String, Object> map = mapThreadLocal.get();
        if (ObjectKit.isNull(map)) {
            map = new ConcurrentHashMap<>();
        }

        // 2. Get the object.
        T instance = this.getSingleton(clazz, map);

        // 3. Update the thread-local.
        mapThreadLocal.set(map);

        return instance;
    }

    /**
     * Multiple method.
     */
    @Override
    public <T> T multiple(Class<T> clazz) {
        this.notNull(clazz);

        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Threadsafe method.
     */
    @Override
    public <T> T threadSafe(Class<T> clazz) {
        if (clazz.isAnnotationPresent(ThreadSafe.class)) {
            return this.singleton(clazz);
        }
        return this.multiple(clazz);
    }

    /**
     * Gets the singleton object.
     *
     * @param <T>         The generic type.
     * @param clazz       The class type.
     * @param instanceMap The instance map.
     * @return The singleton object.
     */
    private <T> T getSingleton(final Class<T> clazz, final Map<String, Object> instanceMap) {
        this.notNull(clazz);

        final String fullClassName = clazz.getName();
        T instance = (T) instanceMap.get(fullClassName);
        if (ObjectKit.isNull(instance)) {
            instance = this.multiple(clazz);
            instanceMap.put(fullClassName, instance);
        }
        return instance;
    }

    /**
     * Asserts that the class is not null.
     *
     * @param clazz The class information.
     */
    private void notNull(final Class<?> clazz) {
        Assert.notNull(clazz, "class");
    }

    /**
     * One singleton slot shared by its creator and concurrent waiters.
     */
    private static final class Slot {

        /**
         * Thread creating the value, cleared after completion to avoid retaining the thread.
         */
        private volatile Thread owner;

        /**
         * Creation result shared by concurrent callers.
         */
        private final CompletableFuture<Object> completion;

        /**
         * Creates an incomplete slot owned by the current thread.
         */
        private Slot() {
            this.owner = Thread.currentThread();
            this.completion = new CompletableFuture<>();
        }

        /**
         * Creates a completed slot for an explicitly supplied value.
         *
         * @param value The singleton value.
         * @return A completed slot.
         */
        private static Slot completed(final Object value) {
            final Slot slot = new Slot();
            slot.complete(value);
            return slot;
        }

        /**
         * Waits for creation, rejecting a circular dependency owned by the current thread.
         *
         * @param key The singleton key used in a circular dependency error.
         * @return The created value.
         */
        private Object await(final String key) {
            if (!completion.isDone() && owner == Thread.currentThread()) {
                throw new IllegalStateException("Circular instance dependency: " + key);
            }
            try {
                return completion.join();
            } catch (final CompletionException failure) {
                final Throwable cause = failure.getCause();
                if (cause instanceof RuntimeException runtime) {
                    throw runtime;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw failure;
            }
        }

        /**
         * Completes creation successfully.
         *
         * @param value The created value.
         * @return {@code true} when this invocation completed the slot.
         */
        private boolean complete(final Object value) {
            owner = null;
            return completion.complete(value);
        }

        /**
         * Completes creation exceptionally.
         *
         * @param cause The creation failure.
         * @return {@code true} when this invocation completed the slot.
         */
        private boolean fail(final Throwable cause) {
            owner = null;
            return completion.completeExceptionally(cause);
        }

        /**
         * Tests whether this slot contains a successfully created value.
         *
         * @return {@code true} when creation completed with a non-null value.
         */
        private boolean isReady() {
            return null != value();
        }

        /**
         * Returns the completed value without waiting.
         *
         * @return The completed value, or {@code null} while incomplete or failed.
         */
        private Object value() {
            if (!completion.isDone() || completion.isCompletedExceptionally() || completion.isCancelled()) {
                return null;
            }
            return completion.getNow(null);
        }
    }

    /**
     * Static inner class for singleton implementation.
     *
     * @author Kimi Liu
     */
    private static final class Holder {

        /**
         * The singleton instance of the factory.
         */
        private static final InstanceFactory INSTANCE = new InstanceFactory();

    }

}
