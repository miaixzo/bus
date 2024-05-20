/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.starter.validate;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;
import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 自动进行参数处理实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AutoValidateAdvice {

    /**
     * 自动进行参数处理
     *
     * @param proxyChain 切面
     * @return 返回执行结果
     * @throws Throwable 异常
     */
    public Object access(ProxyChain proxyChain) throws Throwable {
        Object[] agruements = proxyChain.getArguments();
        Method method = proxyChain.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = proxyChain.getProxy().getClass().getDeclaredMethod(method.getName(),
                        method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                Logger.info("无法在实现类中找到指定的方法,所以无法实现校验器验证,method：" + method.getName());
                return proxyChain.proceed(agruements);
            }
        }
        Annotation[][] annotations = method.getParameterAnnotations();
        Object[] names = proxyChain.getNames();
        for (int i = 0; i < agruements.length; i++) {
            Builder.on(agruements[i], annotations[i], Context.newInstance(), StringKit.toString(names[i]));
        }
        return proxyChain.proceed(agruements);
    }

}
