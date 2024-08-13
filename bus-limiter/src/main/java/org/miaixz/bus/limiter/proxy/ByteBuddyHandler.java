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
 * 规则拦截处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteBuddyHandler implements InvocationHandler {

    private final ByteBuddyProxy byteBuddyProxy;
    private Map<String, Method> methodCache = new HashMap<>();

    public ByteBuddyHandler(ByteBuddyProxy byteBuddyProxy) {
        this.byteBuddyProxy = byteBuddyProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取方法标记
        String name = Builder.resolveMethodName(method);
        // 此对象内是否有缓存方法
        Method realMethod;
        if (methodCache.containsKey(name)) {
            realMethod = methodCache.get(name);
        } else {
            // 获取bean的方法且进行缓存到此对象
            realMethod = MethodKit.getMethod(byteBuddyProxy.bean.getClass(), method.getName(),
                    method.getParameterTypes());
            methodCache.put(name, realMethod);
        }

        if (MethodManager.contain(name)) {
            // 获取方法缓存的策略和注解信息
            StrategyMode strategyMode = MethodManager.getAnnoInfo(name).getLeft();
            Annotation anno = MethodManager.getAnnoInfo(name).getRight();

            // 判断注解类型
            if (anno instanceof Downgrade) {
                Registry.register((Downgrade) anno, name);
            } else if (anno instanceof Hotspot) {
                Registry.register((Hotspot) anno, name);
            } else if (anno instanceof Limiting) {
                Registry.register((Limiting) anno, name);
            } else {
                throw new RuntimeException("annotation type error");
            }
            // 通过
            return Sentinel.process(byteBuddyProxy.bean, realMethod, args, name, strategyMode);
        } else {
            return MethodKit.invoke(byteBuddyProxy.bean, realMethod, args);
        }
    }

}
