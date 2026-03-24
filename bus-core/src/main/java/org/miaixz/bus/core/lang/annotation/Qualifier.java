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
 * A meta-annotation that identifies a custom annotation as a "qualifier" annotation.
 * <p>
 * In dependency injection frameworks, qualifier annotations are used to resolve ambiguity when multiple beans of the
 * same type are available for injection. By creating a custom annotation and marking it with {@code @Qualifier}, you
 * can provide a distinct identifier for a specific bean implementation.
 * <p>
 * For example:
 * 
 * <pre>
 *   {@code @Qualifier}
 *   {@code @Retention(RetentionPolicy.RUNTIME)}
 *   public @interface Offline {}
 * </pre>
 * 
 * This {@code @Offline} annotation can then be used at an injection point to request a specific "offline"
 * implementation of a service. This concept is central to standards like JSR-330 ({@code jakarta.inject.Qualifier}).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Qualifier {

}
