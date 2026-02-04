/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
