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
 * A marker annotation that indicates a class or method is thread-safe.
 * <p>
 * This annotation serves as a form of documentation to communicate the author's intent regarding the concurrency
 * properties of a component.
 * <ul>
 * <li>When applied to a <strong>class</strong>, it signifies that instances of the class are designed to be safely used
 * by multiple threads concurrently.</li>
 * <li>When applied to a <strong>method</strong>, it signifies that the specific method is thread-safe, even if the
 * class as a whole is not.</li>
 * </ul>
 * <p>
 * <strong>Note:</strong> This annotation is a marker and does not, by itself, enforce thread safety. Its primary
 * purpose is for documentation and to be leveraged by static analysis tools. The actual thread safety of the component
 * must be verified through code review and testing.
 * <p>
 * It may also be used by frameworks that perform reflection-based instantiation, where thread-safe components might be
 * handled differently.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface ThreadSafe {

}
