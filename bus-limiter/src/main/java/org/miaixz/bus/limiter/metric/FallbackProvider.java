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
package org.miaixz.bus.limiter.metric;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.limiter.Provider;
import org.miaixz.bus.limiter.magic.StrategyMode;

/**
 * Implements the {@link Provider} interface for handling the FALLBACK strategy mode. This provider is responsible for
 * invoking a fallback method when the primary method execution is blocked or fails due to limiting rules.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FallbackProvider implements Provider {

    /**
     * A concurrent hash map to cache fallback methods for performance. The key is the generated fallback method name
     * (e.g., "originalMethodNameFallback"), and the value is the {@link Method} object for the fallback method.
     */
    private final Map<String, Method> map = new ConcurrentHashMap<>();

    /**
     * Returns the strategy mode supported by this provider, which is {@link StrategyMode#FALLBACK}.
     *
     * @return The {@link StrategyMode#FALLBACK} enum value.
     */
    @Override
    public StrategyMode get() {
        return StrategyMode.FALLBACK;
    }

    /**
     * Processes the method invocation by attempting to find and invoke a fallback method. The fallback method is
     * expected to have the same parameters as the original method and its name should be the original method name
     * appended with "Fallback".
     *
     * @param bean   The target object on which the original method was invoked.
     * @param method The original {@link Method} that was attempted to be executed.
     * @param args   The arguments passed to the original method invocation.
     * @return The result of the fallback method invocation.
     * @throws RuntimeException if a suitable fallback method cannot be found for the given bean and method.
     */
    @Override
    public Object process(Object bean, Method method, Object[] args) {
        // Synthesize the fallback method name
        String fallbackMethodName = StringKit.format("{}Fallback", method.getName());

        Method fallbackMethod;
        // Cache operation
        if (map.containsKey(fallbackMethodName)) {
            fallbackMethod = map.get(fallbackMethodName);
        } else {
            fallbackMethod = MethodKit.getMethod(bean.getClass(), fallbackMethodName, method.getParameterTypes());
            map.put(fallbackMethodName, fallbackMethod);
        }

        if (ObjectKit.isNull(fallbackMethod)) {
            throw new RuntimeException(StringKit.format(
                    "Can't find fallback method [{}] in bean [{}]",
                    fallbackMethodName,
                    bean.getClass().getName()));
        }

        return MethodKit.invoke(bean, fallbackMethod, args);
    }

}
