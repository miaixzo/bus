/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphO;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.limiter.magic.StrategyMode;
import org.miaixz.bus.limiter.metric.StrategyManager;
import org.miaixz.bus.logger.Logger;

import java.lang.reflect.Method;

/**
 * 管控执行
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sentinel {

    /**
     * 执行对应方法及相关规则
     *
     * @param bean         对象信息
     * @param method       执行方法
     * @param args         参数
     * @param name         方法名称
     * @param strategyMode 规则
     * @return the object
     */
    public static Object process(Object bean, Method method, Object[] args, String name, StrategyMode strategyMode) {
        // 进行各种策略的处理
        switch (strategyMode) {
        case FALLBACK:
            // 允许进入则直接调用
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
                // 进行回调fallback方法
                return StrategyManager.get(strategyMode).process(bean, method, args);
            }
        case HOT_METHOD:
            // 参数转换
            String convertParam = Builder.md5Hex(JsonKit.toJsonString(args));
            Entry entry = null;
            try {
                // 判断是否进行限流
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
