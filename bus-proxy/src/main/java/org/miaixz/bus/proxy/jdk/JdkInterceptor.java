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
package org.miaixz.bus.proxy.jdk;

import java.io.Serial;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.miaixz.bus.core.xyz.ModifierKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.proxy.Aspect;
import org.miaixz.bus.proxy.invoker.Interceptor;

/**
 * JDK实现的动态代理切面
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkInterceptor extends Interceptor implements InvocationHandler {

    @Serial
    private static final long serialVersionUID = 2852250138577L;

    /**
     * 构造
     *
     * @param target 被代理对象
     * @param aspect 切面实现
     */
    public JdkInterceptor(final Object target, final Aspect aspect) {
        super(target, aspect);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object target = this.target;
        final Aspect aspect = this.aspect;
        Object result = null;

        // 开始前回调
        if (aspect.before(target, method, args)) {
            ReflectKit.setAccessible(method);

            try {
                result = method.invoke(ModifierKit.isStatic(method) ? null : target, args);
            } catch (final InvocationTargetException e) {
                // 异常回调（只捕获业务代码导致的异常，而非反射导致的异常）
                if (aspect.afterException(target, method, args, e.getTargetException())) {
                    throw e;
                }
            }

            // 结束执行回调
            if (aspect.after(target, method, args, result)) {
                return result;
            }
        }
        return null;
    }

}
