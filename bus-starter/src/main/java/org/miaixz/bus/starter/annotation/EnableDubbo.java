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
package org.miaixz.bus.starter.annotation;

import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.miaixz.bus.starter.dubbo.DubboConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Enables Apache Dubbo support.
 * <p>
 * This is a convenience annotation that acts as a shortcut for {@code @EnableDubboConfig}, {@code @DubboComponentScan},
 * and {@code @Import(DubboConfiguration.class)}. It allows for the configuration and scanning of Dubbo components
 * within a Spring application.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@EnableDubboConfig
@DubboComponentScan
@Import(DubboConfiguration.class)
public @interface EnableDubbo {

    /**
     * Alias for {@link DubboComponentScan#basePackages()}.
     * <p>
     * Base packages to scan for annotated {@code @DubboService} classes.
     * </p>
     *
     * @return The base packages to scan.
     */
    @AliasFor(annotation = DubboComponentScan.class, attribute = "basePackages")
    String[] basePackages() default {};

    /**
     * Alias for {@link DubboComponentScan#basePackageClasses()}.
     * <p>
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan. The package of each class
     * specified will be scanned.
     * </p>
     *
     * @return Classes from the base packages to scan.
     */
    @AliasFor(annotation = DubboComponentScan.class, attribute = "basePackageClasses")
    Class<?>[] basePackageClasses() default {};

    /**
     * Alias for {@link EnableDubboConfig#multiple()}.
     * <p>
     * Indicates whether to allow binding to multiple Spring Beans.
     * </p>
     *
     * @return {@code true} if multiple bindings are allowed, {@code false} otherwise. Defaults to {@code true}.
     */
    @AliasFor(annotation = EnableDubboConfig.class, attribute = "multiple")
    boolean multiple() default true;

}
