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
package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;

/**
 * A documentation annotation used to indicate that a field or method is protected by a specific lock. This helps in
 * code maintenance, peer review, and automated analysis tools by making the concurrency policy explicit and helping to
 * identify potential thread-safety issues.
 * <p>
 * This annotation is conceptually similar to the {@code @GuardedBy} annotation found in various static analysis tools
 * and concurrency libraries (e.g., JCIP - Java Concurrency in Practice).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
public @interface GuardedBy {

    /**
     * Specifies the lock that protects the annotated element. The value should be a string that identifies the lock
     * object. Common conventions include:
     * <ul>
     * <li>{@code "this"}: The intrinsic lock of the instance.</li>
     * <li>{@code "ClassName.class"}: The intrinsic lock of the class object.</li>
     * <li>{@code "fieldName"}: The lock object referenced by the specified field.</li>
     * </ul>
     *
     * @return A string identifying the lock that provides the guard.
     */
    String value() default Normal.EMPTY;

}
