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
package org.miaixz.bus.limiter;

import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.limiter.magic.StrategyMode;
import org.miaixz.bus.limiter.metric.StrategyManager;
import org.miaixz.bus.logger.Logger;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * Sentinel execution class for applying various limiting and protection strategies. This class integrates with Alibaba
 * Sentinel to enforce flow control, hotspot protection, and fallback mechanisms based on configured rules.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sentinel {

    /**
     * Executes the given method with the specified limiting strategy. This method acts as an entry point for applying
     * different protection strategies (fallback, hotspot, request limit) before or during method invocation.
     *
     * @param bean         The target object on which the method is to be invoked.
     * @param method       The {@link Method} to be executed.
     * @param args         The arguments to be passed to the method.
     * @param name         The resource name associated with the method for Sentinel rules.
     * @param strategyMode The {@link StrategyMode} to apply (FALLBACK, HOT_METHOD, REQUEST_LIMIT).
     * @return The result of the method invocation, or a fallback value if a strategy is triggered.
     * @throws InternalException if an unsupported {@link StrategyMode} is provided.
     */
    public static Object process(Object bean, Method method, Object[] args, String name, StrategyMode strategyMode) {
        // Process various strategies
        switch (strategyMode) {
            case FALLBACK:
                // If allowed to enter, call directly
                if (SphO.entry(name)) {
                    try {
                        return MethodKit.invoke(bean, method, args);
                    } finally {
                        SphO.exit();
                    }
                } else {
                    if (Holder.load().isLogger()) {
                        Logger.info("Trigger fallback strategy for [{}], args: [{}]", name, JsonKit.toJsonString(args));
                    }
                    // Call the fallback method
                    return StrategyManager.get(strategyMode).process(bean, method, args);
                }
            case HOT_METHOD:
                // Parameter conversion
                String convertParam = Builder.md5Hex(JsonKit.toJsonString(args));
                Entry entry = null;
                try {
                    // Determine if flow control is needed
                    entry = SphU.entry(name, EntryType.IN, 1, convertParam);
                    return MethodKit.invoke(bean, method, args);
                } catch (BlockException e) {
                    if (Holder.load().isLogger()) {
                        Logger.info(" Trigger hotspot strategy for [{}], args: [{}]", name, JsonKit.toJsonString(args));
                    }
                    return StrategyManager.get(strategyMode).process(bean, method, args);
                } finally {
                    if (entry != null) {
                        entry.exit(1, convertParam);
                    }
                }
            case REQUEST_LIMIT:
                if (Holder.load().isLogger()) {
                    Logger.info("Trigger requestLimit strategy for [{}], args: [{}]", name, JsonKit.toJsonString(args));
                }
                return StrategyManager.get(strategyMode).process(bean, method, args);

            default:
                throw new InternalException("Strategy error!");
        }
    }

}
