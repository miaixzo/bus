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
package org.miaixz.bus.starter.validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.Context;
import org.springframework.core.DefaultParameterNameDiscoverer;

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
     * @param joinPoint Spring AOP 切点
     * @return 返回执行结果
     * @throws Throwable 异常
     */
    public Object access(JoinPoint joinPoint) throws Throwable {
        // 获取方法和参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] arguments = joinPoint.getArgs();

        // 如果是接口，尝试获取实现类的方法
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(method.getName(),
                        method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                Logger.info(
                        "Cannot find the specified method in the implementation class, skipping validation, method: {}",
                        method.getName());
                return joinPoint.getArgs(); // 跳过验证
            }
        }

        // 获取参数注解和名称
        Annotation[][] annotations = method.getParameterAnnotations();
        String[] names = new DefaultParameterNameDiscoverer().getParameterNames(method);
        if (names == null || names.length == 0) {
            names = new String[arguments.length]; // 分配默认名称
            for (int i = 0; i < names.length; i++) {
                names[i] = Normal.EMPTY + i;
            }
        }

        // 执行验证
        for (int i = 0; i < arguments.length; i++) {
            Builder.on(arguments[i], annotations[i], Context.newInstance(), names[i]);
        }

        // 继续执行原方法
        return proceed(joinPoint, arguments);
    }

    /**
     * 模拟 ProxyChain 的 proceed 方法
     *
     * @param joinPoint Spring AOP 切点
     * @param arguments 方法参数
     * @return 原方法执行结果
     * @throws Throwable 异常
     */
    private Object proceed(JoinPoint joinPoint, Object[] arguments) throws Throwable {
        // 如果是 ProceedingJoinPoint，执行 proceed
        if (joinPoint instanceof org.aspectj.lang.ProceedingJoinPoint) {
            return ((org.aspectj.lang.ProceedingJoinPoint) joinPoint).proceed(arguments);
        }
        // 否则，跳过验证直接返回参数（根据业务需求调整）
        return arguments;
    }

}