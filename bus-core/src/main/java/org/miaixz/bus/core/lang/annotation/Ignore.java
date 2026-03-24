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
 * Marks a field, method, or parameter to be ignored during certain automated processes, such as bean copying,
 * serialization, or map conversion.
 * <p>
 * The behavior of this annotation depends on where it is applied:
 * <ul>
 * <li>When applied to a <strong>field</strong>, both read and write operations on that property are ignored.</li>
 * <li>When applied to a <strong>setter method</strong> (e.g., {@code setXxx}), the property will not be written
 * to.</li>
 * <li>When applied to a <strong>getter method</strong> (e.g., {@code getXxx}), the property will not be read from.</li>
 * <li>When applied to a <strong>parameter</strong>, it may be ignored by frameworks that process method
 * parameters.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface Ignore {

    /**
     * Reason description for ignoring (optional)
     *
     * @return Reason for ignoring
     */
    String value() default Normal.EMPTY;

}
