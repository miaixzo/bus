/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.limiter.metric;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.limiter.Builder;
import org.miaixz.bus.limiter.Provider;
import org.miaixz.bus.limiter.Supplier;
import org.miaixz.bus.limiter.magic.StrategyMode;
import org.miaixz.bus.limiter.magic.annotation.Limiting;

/**
 * Implements the {@link Provider} interface for handling the REQUEST_LIMIT strategy mode. This provider manages request
 * limiting based on user identifiers and configured {@link Limiting} annotations. It uses a {@link ResourceManager} for
 * each user to track and enforce limits.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RequestProvider implements Provider {

    /**
     * An {@link ExecutorService} used for asynchronously cleaning up expired {@link ResourceManager} instances. It uses
     * a fixed thread pool with one thread.
     */
    private final ExecutorService cleaner = ThreadKit.newFixedExecutor(1, 5, "L-", false);

    /**
     * A concurrent hash map to store {@link ResourceManager} instances, keyed by a serializable user identifier. Each
     * {@link ResourceManager} manages the limiting rules for a specific user.
     */
    private final Map<Serializable, ResourceManager> map = new ConcurrentHashMap<>();

    /**
     * The default {@link Supplier} implementation that provides a unique object ID as the user identifier. This can be
     * overridden by calling {@link #setMarkSupplier(Supplier)}.
     */
    private Supplier supplier = new Supplier() {

        /**
         * Generates a unique object ID as the user identifier.
         *
         * @return A {@link Serializable} unique object ID.
         */
        @Override
        public Serializable get() {
            return ID.objectId();
        }

    };

    /**
     * Sets a new user identifier provider. This allows customizing how user identifiers are obtained for request
     * limiting.
     *
     * @param supplier The new {@link Supplier} to be used for providing user identifiers.
     */
    public void setMarkSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    /**
     * Returns the strategy mode supported by this provider, which is {@link StrategyMode#REQUEST_LIMIT}.
     *
     * @return The {@link StrategyMode#REQUEST_LIMIT} enum value.
     */
    @Override
    public StrategyMode get() {
        return StrategyMode.REQUEST_LIMIT;
    }

    /**
     * Processes the method invocation by applying request limiting rules. It retrieves the current user identifier,
     * obtains or creates a {@link ResourceManager} for that user, and then checks if the request is allowed based on
     * the {@link Limiting} annotation configured for the method. If the request is blocked, the
     * {@link Supplier#intercept(Object, Method, Object[])} method is called.
     *
     * @param bean   The target object on which the method is invoked.
     * @param method The {@link Method} being invoked.
     * @param args   The arguments passed to the method invocation.
     * @return The result of the method invocation if allowed, or the result of the interception if blocked.
     */
    @Override
    public Object process(Object bean, Method method, Object[] args) {
        // Get the current user identifier
        Serializable mark = supplier.get();
        ResourceManager resourceManager = map.get(mark);

        // Cache operation: create a new ResourceManager if one doesn't exist for the current user
        if (Objects.isNull(resourceManager)) {
            resourceManager = new ResourceManager();
            map.put(mark, resourceManager);
        }

        // Get method configuration parameters
        String name = Builder.resolveMethodName(method);
        Limiting limiting = (Limiting) MethodManager.getAnnoInfo(name).getRight();
        if (!resourceManager.entry(name, limiting)) {
            // Intercept the method if limiting is triggered
            return supplier.intercept(bean, method, args);
        }

        // Allow execution
        return MethodKit.invoke(bean, method, args);
    }

    /**
     * Initiates an asynchronous cleanup of existing {@link ResourceManager} instances. This method submits a task to
     * the {@link #cleaner} executor service to perform the cleanup.
     */
    private void clears() {
        cleaner.submit(this::clear);
    }

    /**
     * Cleans up {@link ResourceManager} instances that are marked for clearance. It iterates through the map of
     * resource managers and removes those that indicate they are clear. This method limits the number of resources
     * cleaned in a single pass to a maximum of 10.
     */
    private void clear() {
        int count = 0;
        for (Serializable mark : map.keySet()) {
            count++;
            ResourceManager resourceManager = map.get(mark);
            if (Objects.isNull(resourceManager))
                continue;
            if (resourceManager.isClear())
                map.remove(mark);
            if (count > 9)
                return;
        }
    }

}
