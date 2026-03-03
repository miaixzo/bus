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
