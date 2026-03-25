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

/**
 * Marks an element (such as a field, parameter, or method) to be explicitly included in a specific processing flow.
 * <p>
 * This annotation is the semantic opposite of {@code @Ignore}. In many frameworks or tools, a "default-exclude" or
 * "include-on-demand" strategy might be in place. In such cases, {@code @Include} is used to specify which elements
 * should be processed, thereby overriding the default behavior.
 *
 * <p>
 * This annotation is retained at runtime ({@link RetentionPolicy#RUNTIME}) so that it can be read via reflection by
 * frameworks, libraries, or tools to implement dynamic, annotation-based inclusion logic. Common use cases include:
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
public @interface Include {

}
