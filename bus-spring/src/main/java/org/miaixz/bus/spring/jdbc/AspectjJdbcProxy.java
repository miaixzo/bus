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
package org.miaixz.bus.spring.jdbc;

import java.lang.reflect.Method;
import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Applies annotation-driven datasource routing around Spring-managed method invocations.
 * <p>
 * The aspect runs before transaction advice so datasource selection occurs before a transaction obtains its connection.
 *
 * @author Kimi Liu
 */
@Order(-1)
@Aspect
public class AspectjJdbcProxy {

    /**
     * Application-context-scoped datasource routing state.
     */
    private final DataSourceHolder dataSourceHolder;

    /**
     * Creates the datasource routing aspect.
     *
     * @param dataSourceHolder datasource routing state
     */
    public AspectjJdbcProxy(DataSourceHolder dataSourceHolder) {
        this.dataSourceHolder = Objects.requireNonNull(dataSourceHolder, "dataSourceHolder");
    }

    /**
     * Runs an annotated invocation inside a nested datasource scope.
     * <p>
     * Method annotations take precedence over class annotations. Every invocation restores the exact parent key on
     * normal return or exception.
     *
     * @param joinPoint intercepted invocation
     * @return invocation result
     * @throws Throwable when the invocation fails
     */
    @Around("@annotation(org.miaixz.bus.spring.jdbc.DataSource) || @within(org.miaixz.bus.spring.jdbc.DataSource)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        DataSource dataSource = resolveAnnotation(joinPoint);
        if (dataSource == null || StringKit.isEmpty(dataSource.value())) {
            return joinPoint.proceed();
        }
        String requestedKey = dataSource.value().trim();
        Object target = joinPoint.getTarget();
        String className = target == null ? joinPoint.getSignature().getDeclaringTypeName()
                : target.getClass().getSimpleName();
        Logger.info(
                true,
                "Spring",
                "Datasource scope entered: class={}, method={}, datasource={}",
                className,
                joinPoint.getSignature().getName(),
                requestedKey);
        try (DataSourceHolder.Scope ignored = this.dataSourceHolder.scope(requestedKey)) {
            return joinPoint.proceed();
        } finally {
            Logger.debug(
                    false,
                    "Spring",
                    "Datasource scope restored: class={}, method={}, parent={}",
                    className,
                    joinPoint.getSignature().getName(),
                    this.dataSourceHolder.getCurrentKey());
        }
    }

    /**
     * Resolves the effective datasource annotation, preferring the implementation method over its class.
     *
     * @param joinPoint intercepted invocation
     * @return effective datasource annotation, or {@code null}
     */
    private static DataSource resolveAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object target = joinPoint.getTarget();
        if (target != null) {
            try {
                method = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException ignored) {
                // The signature method remains authoritative for non-public implementation methods.
            }
        }
        DataSource methodAnnotation = method.getAnnotation(DataSource.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return target == null ? null : target.getClass().getAnnotation(DataSource.class);
    }

}
