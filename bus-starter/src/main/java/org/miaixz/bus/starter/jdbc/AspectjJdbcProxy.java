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
package org.miaixz.bus.starter.jdbc;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.miaixz.bus.logger.Logger;
import org.springframework.core.annotation.Order;

/**
 * AOP aspect for dynamic data source switching.
 * <p>
 * This aspect intercepts methods annotated with {@link DataSource} and manages the data source context before and after
 * method execution. The {@code @Order(-1)} annotation ensures that this aspect runs before the transaction aspect, so
 * data source switching occurs before transaction management begins.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(-1)
@Aspect
public class AspectjJdbcProxy {

    /**
     * Advice that executes before a method annotated with {@link DataSource}.
     * <p>
     * It sets the data source context based on the value specified in the annotation. If no data source name is
     * provided, it defaults to the primary data source.
     * </p>
     *
     * @param joinPoint  The join point representing the method execution.
     * @param dataSource The {@link DataSource} annotation instance on the method.
     */
    @Before("@annotation(dataSource)")
    public void before(JoinPoint joinPoint, DataSource dataSource) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String dataSourceName = dataSource.value();
        if (dataSourceName.isEmpty()) {
            Logger.debug(
                    true,
                    "AOP",
                    "[{}.{}] No datasource specified, will use default datasource",
                    className,
                    methodName);
            return;
        }
        Logger.info(
                true,
                "AOP",
                "[{}.{}] starts execution, switching to datasource: [{}]",
                className,
                methodName,
                dataSourceName);

        DataSourceHolder.setKey(dataSourceName);
    }

    /**
     * Advice that executes after a method annotated with {@link DataSource}.
     * <p>
     * It cleans up the data source context based on the annotation's configuration. If {@link DataSource#clear()} is
     * {@code true}, the data source context is cleared. Otherwise, it is maintained for subsequent operations within
     * the same thread.
     * </p>
     *
     * @param joinPoint  The join point representing the method execution.
     * @param dataSource The {@link DataSource} annotation instance on the method.
     */
    @After("@annotation(dataSource)")
    public void after(JoinPoint joinPoint, DataSource dataSource) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String dataSourceName = dataSource.value();
        if (dataSourceName.isEmpty()) {
            Logger.debug(false, "AOP", "[{}.{}] execution completed, no datasource switched", className, methodName);
            return;
        }
        if (dataSource.clear()) {
            Logger.info(
                    false,
                    "AOP",
                    "[{}.{}] execution completed, clearing datasource setting: [{}]",
                    className,
                    methodName,
                    dataSourceName);
            DataSourceHolder.remove();
        } else {
            Logger.info(
                    false,
                    "AOP",
                    "[{}.{}] execution completed, keeping datasource setting: [{}]",
                    className,
                    methodName,
                    dataSourceName);
        }
    }

}
