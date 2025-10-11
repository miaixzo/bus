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
package org.miaixz.bus.limiter.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.limiter.Builder;
import org.miaixz.bus.limiter.Registry;
import org.miaixz.bus.limiter.Sentinel;
import org.miaixz.bus.limiter.magic.StrategyMode;
import org.miaixz.bus.limiter.magic.annotation.Downgrade;
import org.miaixz.bus.limiter.magic.annotation.Hotspot;
import org.miaixz.bus.limiter.magic.annotation.Limiting;
import org.miaixz.bus.limiter.metric.MethodManager;

/**
 * An {@link InvocationHandler} implementation that intercepts method calls to apply limiting rules based on
 * annotations. This handler integrates with ByteBuddy for proxying and uses Sentinel for enforcing various limiting
 * strategies like downgrade, hotspot, and request limiting.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteBuddyHandler implements InvocationHandler {

    /**
     * The {@link ByteBuddyProxy} instance that holds the target bean for which method calls are being intercepted.
     */
    private final ByteBuddyProxy byteBuddyProxy;
    /**
     * A cache to store resolved {@link Method} objects, keyed by their unique string representation. This prevents
     * repeated reflection lookups for the same method.
     */
    private Map<String, Method> methodCache = new HashMap<>();

    /**
     * Constructs a new {@code ByteBuddyHandler} with the specified {@link ByteBuddyProxy}.
     *
     * @param byteBuddyProxy The {@link ByteBuddyProxy} instance containing the target object.
     */
    public ByteBuddyHandler(ByteBuddyProxy byteBuddyProxy) {
        this.byteBuddyProxy = byteBuddyProxy;
    }

    /**
     * Intercepts method invocations and applies limiting rules if the method is configured for it. It resolves the real
     * method, checks for registered limiting annotations, and then delegates to {@link Sentinel} to process the method
     * according to the defined strategy.
     *
     * @param proxy  The proxy instance that the method was invoked on.
     * @param method The {@link Method} instance corresponding to the interface method invoked on the proxy instance.
     * @param args   An array of objects containing the values of the arguments passed in the method invocation on the
     *               proxy instance. If an interface method has no arguments, this array will be empty. If the method is
     *               a static method, this array will be null.
     * @return The result of the method invocation, potentially modified by limiting strategies.
     * @throws Throwable        if an exception occurs during method invocation or limiting processing.
     * @throws RuntimeException if an unsupported annotation type is encountered.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Get the unique name for the method
        String name = Builder.resolveMethodName(method);
        // Check if the real method is already cached in this object
        Method realMethod;
        if (methodCache.containsKey(name)) {
            realMethod = methodCache.get(name);
        } else {
            // Get the actual method from the target bean and cache it
            realMethod = MethodKit
                    .getMethod(byteBuddyProxy.bean.getClass(), method.getName(), method.getParameterTypes());
            methodCache.put(name, realMethod);
        }

        // Check if the method is registered for limiting
        if (MethodManager.contain(name)) {
            // Retrieve the cached strategy and annotation information for the method
            StrategyMode strategyMode = MethodManager.getAnnoInfo(name).getLeft();
            Annotation anno = MethodManager.getAnnoInfo(name).getRight();

            // Register the rule with Sentinel based on the annotation type
            if (anno instanceof Downgrade) {
                Registry.register((Downgrade) anno, name);
            } else if (anno instanceof Hotspot) {
                Registry.register((Hotspot) anno, name);
            } else if (anno instanceof Limiting) {
                Registry.register((Limiting) anno, name);
            } else {
                throw new RuntimeException("annotation type error");
            }
            // Process the method invocation through Sentinel to apply limiting rules
            return Sentinel.process(byteBuddyProxy.bean, realMethod, args, name, strategyMode);
        } else {
            // If no limiting rules are configured, invoke the real method directly
            return MethodKit.invoke(byteBuddyProxy.bean, realMethod, args);
        }
    }

}
