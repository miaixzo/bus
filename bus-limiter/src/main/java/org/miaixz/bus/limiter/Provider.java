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
package org.miaixz.bus.limiter;

import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.limiter.magic.StrategyMode;

/**
 * Defines the contract for a limiter provider, which is responsible for executing limiting rules. This interface
 * extends {@link org.miaixz.bus.core.Provider} and specifies methods for retrieving the limiting strategy and
 * processing method invocations under limiting rules.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider extends org.miaixz.bus.core.Provider {

    /**
     * Retrieves the limiting strategy associated with this provider.
     *
     * @return The {@link StrategyMode} representing the limiting strategy.
     */
    StrategyMode get();

    /**
     * Processes a method invocation according to the limiting rules defined by this provider. This method is typically
     * called before the actual method execution to apply rate limiting, circuit breaking, or other control mechanisms.
     *
     * @param bean   The target object on which the method is invoked.
     * @param method The {@link Method} being invoked.
     * @param args   The arguments passed to the method invocation.
     * @return The result of the method invocation after applying limiting rules, or a fallback value.
     */
    Object process(Object bean, Method method, Object[] args);

    /**
     * Returns the type of this provider, which is {@link EnumValue.Povider#LIMITER}.
     *
     * @return The provider type.
     */
    @Override
    default Object type() {
        return EnumValue.Povider.LIMITER;
    }

}
