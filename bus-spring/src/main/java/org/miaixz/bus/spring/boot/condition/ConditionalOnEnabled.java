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
package org.miaixz.bus.spring.boot.condition;

import java.lang.annotation.*;

import org.springframework.context.annotation.Conditional;

import org.miaixz.bus.core.lang.Normal;

/**
 * Activates a Spring Boot feature when its explicit enable annotation is present or its configuration property is true.
 * <p>
 * The explicit annotation has the highest priority. Consequently, an {@code @EnableXxx} annotation still activates its
 * feature when the corresponding {@code bus.xxx.enabled} property is absent or explicitly set to {@code false}. The
 * property is evaluated only as the secondary activation source.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Conditional(EnabledCondition.class)
public @interface ConditionalOnEnabled {

    /**
     * Returns the explicit annotation that activates the feature.
     *
     * @return feature enable annotation
     */
    Class<? extends Annotation> annotation();

    /**
     * Returns the property prefix whose {@code enabled} member provides secondary activation.
     *
     * @return feature property prefix
     */
    String prefix();

    /**
     * Returns the property name appended to {@link #prefix()} for secondary activation.
     *
     * @return feature property name
     */
    String name() default Normal.ENABLED;

    /**
     * Returns whether a missing secondary property activates the feature when the explicit annotation is absent.
     *
     * @return {@code true} when a missing property should match
     */
    boolean matchIfMissing() default false;

}
