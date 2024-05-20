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
package org.miaixz.bus.starter.jdbc;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * AOP切面切点
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(-1)
@Aspect
public class AspectjJdbcProxy {

    /**
     * 扫描所有含有@DataSource注解的类
     */
    @Pointcut("@annotation(org.miaixz.bus.starter.jdbc.DataSource)" +
            "||execution(* *(@org.miaixz.bus.starter.jdbc.DataSource (*), ..))")
    public void match() {

    }

    /**
     * 执行结果,使用around方式监控
     *
     * @param point 切点
     * @return 返回结果
     * @throws Throwable 异常
     */
    @Around("match()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取执行方法
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        // 获取方法的@DataSource注解
        DataSource dataSource = method.getAnnotation(DataSource.class);
        if (!StringKit.isEmptyIfString(dataSource.value())) {
            // 获取类级别的@DataSource注解
            dataSource = method.getDeclaringClass().getAnnotation(DataSource.class);
        }
        if (null != dataSource) {
            // 设置数据源key值
            DataSourceHolder.setKey(dataSource.value());
            Logger.info("Switch datasource to [{}] in method [{}]",
                    DataSourceHolder.getKey(), point.getSignature());
        }
        // 继续执行该方法
        Object object = point.proceed();
        // 恢复默认数据源
        DataSourceHolder.remove();
        Logger.info("Restore datasource to [{}] in method [{}]",
                DataSourceHolder.getKey(), point.getSignature());
        return object;
    }

}
