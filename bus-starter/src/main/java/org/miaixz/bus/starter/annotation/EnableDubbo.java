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
 * @since Java 17+
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
