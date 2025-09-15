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
import org.aspectj.lang.annotation.*;
import org.miaixz.bus.logger.Logger;
import org.springframework.core.annotation.Order;

/**
 * AOP aspect for dynamic datasource switching.
 * <p>
 * This aspect intercepts methods annotated with {@link DataSource} and manages the datasource context before and after
 * method execution.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(-1)
@Aspect
public class AspectjJdbcProxy {

    /**
     * Advice that runs before a method execution with {@link DataSource} annotation.
     * <p>
     * Sets the datasource context based on the value specified in the annotation. If no datasource is specified, the
     * default datasource will be used.
     * </p>
     *
     * @param joinPoint  the join point representing the method execution
     * @param dataSource the datasource annotation on the method
     */
    @Before("@annotation(dataSource)")
    public void before(JoinPoint joinPoint, DataSource dataSource) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String dataSourceName = dataSource.value();
        if (dataSourceName.isEmpty()) {
            Logger.debug("==>     Method: [{}.{}] No datasource specified, will use default datasource", className,
                    methodName);
            return;
        }
        Logger.info("==>     Method: [{}.{}] starts execution, switching to datasource: [{}]", className, methodName,
                dataSourceName);

        DataSourceHolder.setKey(dataSourceName);
    }

    /**
     * Advice that runs after a method execution with {@link DataSource} annotation.
     * <p>
     * Cleans up the datasource context based on the configuration in the annotation. If {@link DataSource#clear()} is
     * true, the datasource context will be cleared. Otherwise, the datasource context will be maintained for subsequent
     * operations.
     * </p>
     *
     * @param joinPoint  the join point representing the method execution
     * @param dataSource the datasource annotation on the method
     */
    @After("@annotation(dataSource)")
    public void after(JoinPoint joinPoint, DataSource dataSource) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String dataSourceName = dataSource.value();
        if (dataSourceName.isEmpty()) {
            Logger.debug("==>     Method: [{}.{}] execution completed, no datasource switched", className, methodName);
            return;
        }
        if (dataSource.clear()) {
            Logger.info("==>     Method: [{}.{}] execution completed, clearing datasource setting: [{}]", className,
                    methodName, dataSourceName);
            DataSourceHolder.remove();
        } else {
            Logger.info("==>     Method: [{}.{}] execution completed, keeping datasource setting: [{}]", className,
                    methodName, dataSourceName);
        }
    }

}