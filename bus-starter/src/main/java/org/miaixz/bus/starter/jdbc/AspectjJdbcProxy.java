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
package org.miaixz.bus.starter.jdbc;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.miaixz.bus.core.xyz.StringKit;
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
     * {@code true}, the data source context is switched back to the default data source. Otherwise, it is maintained
     * for subsequent operations within the same thread.
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
                    "[{}.{}] execution completed, clearing datasource setting: [{}], switching back to default",
                    className,
                    methodName,
                    dataSourceName);

            // Switch back to the default data source instead of just clearing
            String defaultDataSource = DataSourceHolder.getDefault();
            if (StringKit.isEmpty(defaultDataSource)) {
                DataSourceHolder.setKey(defaultDataSource);
                Logger.debug(false, "AOP", "Switched back to default datasource: [{}]", defaultDataSource);
            } else {
                // If no default data source is configured, clear the context (fallback to original behavior)
                DataSourceHolder.remove();
                Logger.debug(false, "AOP", "No default datasource configured, context cleared");
            }
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
