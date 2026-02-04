/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.jdbc;

import org.miaixz.bus.core.lang.Normal;

import java.lang.annotation.*;

/**
 * Annotation for supporting multiple data sources.
 * <p>
 * This annotation can be applied to methods, types, or parameters to specify which data source should be used for the
 * execution of a method. It is typically used in conjunction with an Aspect-Oriented Programming (AOP) setup to switch
 * the data source context dynamically.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER })
public @interface DataSource {

    /**
     * The name of the data source. If left empty, the default data source will be used.
     *
     * @return The data source name.
     */
    String value() default Normal.EMPTY;

    /**
     * Whether to clear the data source setting after the method execution.
     * <p>
     * If set to {@code true}, the data source context will be reset to the default data source after the annotated
     * method completes.
     * </p>
     *
     * @return {@code true} to clear the data source setting, {@code false} otherwise.
     */
    boolean clear() default true;

}
