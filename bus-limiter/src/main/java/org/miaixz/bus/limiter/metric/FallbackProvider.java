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
 * FALLBACK 模式处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FallbackProvider implements Provider {

    private final Map<String, Method> map = new ConcurrentHashMap<>();

    @Override
    public StrategyMode get() {
        return StrategyMode.FALLBACK;
    }

    @Override
    public Object process(Object bean, Method method, Object[] args) {
        // 合成方法名称
        String fallbackMethodName = StringKit.format("{}Fallback", method.getName());

        Method fallbackMethod;
        // 缓存操作
        if (map.containsKey(fallbackMethodName)) {
            fallbackMethod = map.get(fallbackMethodName);
        } else {
            fallbackMethod = MethodKit.getMethod(bean.getClass(), fallbackMethodName, method.getParameterTypes());
            map.put(fallbackMethodName, fallbackMethod);
        }

        if (ObjectKit.isNull(fallbackMethod)) {
            throw new RuntimeException(StringKit.format("Can't find fallback method [{}] in bean [{}]",
                    fallbackMethodName, bean.getClass().getName()));
        }

        return MethodKit.invoke(bean, fallbackMethod, args);
    }

}
