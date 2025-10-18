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
package org.miaixz.bus.vortex.registry;

import org.miaixz.bus.vortex.Registry;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * An abstract, generic, thread-safe base class for creating in-memory registries.
 * <p>
 * This class provides the core functionality for a key-value store, using a {@link ConcurrentHashMap} for thread-safe
 * operations. It is designed to be extended by concrete registry implementations (e.g., {@link AssetsRegistry}), which
 * must provide a key generation strategy.
 *
 * @param <T> The type of objects to be stored in the registry.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRegistry<T> implements Registry<T>, InitializingBean {

    /**
     * The underlying thread-safe map that stores the registered items.
     */
    private final Map<String, T> registry = new ConcurrentHashMap<>();

    /**
     * The function used to generate a unique key for each item stored in the registry.
     */
    protected Function<T, String> keyGenerator;

    /**
     * Sets the key generation strategy for this registry. This method must be called by subclasses in their constructor
     * to define how items are indexed.
     *
     * @param keyGenerator A {@link Function} that takes an item of type {@code T} and returns its unique string key.
     */
    protected void setKeyGenerator(Function<T, String> keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    @Override
    public void register(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set. Call setKeyGenerator in the constructor.");
        }
        register(keyGenerator.apply(item), item);
    }

    @Override
    public void register(String key, T item) {
        this.registry.put(key, item);
    }

    @Override
    public void destroy(String key) {
        this.registry.remove(key);
    }

    @Override
    public void destroy(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set.");
        }
        destroy(keyGenerator.apply(item));
    }

    @Override
    public void update(T item) {
        if (keyGenerator == null) {
            throw new IllegalStateException("Key generator has not been set.");
        }
        update(keyGenerator.apply(item), item);
    }

    @Override
    public void update(String key, T item) {
        this.registry.put(key, item);
    }

    @Override
    public void refresh() {
        this.registry.clear();
        init();
    }

    @Override
    public T get(String key) {
        return this.registry.get(key);
    }

    @Override
    public Collection<T> getAll() {
        return this.registry.values();
    }

    /**
     * Integrates with the Spring lifecycle. After all bean properties are set, this method is called, which in turn
     * triggers the initial {@link #refresh()} of the registry.
     */
    @Override
    public void afterPropertiesSet() {
        refresh();
    }

}
