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
package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.*;

/**
 * Marks a constructor, method, or field as an injection point for dependencies. This annotation is a core part of a
 * dependency injection (DI) framework, indicating where the container should provide instances of required components.
 * It is conceptually similar to {@code jakarta.inject.Inject}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD })
public @interface Inject {

    /**
     * Specifies whether the dependency is optional. If set to {@code true}, the injector will skip the injection for
     * this member if a suitable binding is not found, without generating an error.
     * <p>
     * When applied to a field, if no dependency is found, the field retains its default assigned value.
     * <p>
     * When applied to a method, the method will only be invoked if bindings are found for all of its parameters. If any
     * parameter cannot be resolved, the method will not be called.
     *
     * @return {@code true} if the injection is optional, {@code false} otherwise.
     */
    boolean optional() default false;

}
