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
package org.miaixz.bus.sensitive.magic.annotation;

import org.miaixz.bus.core.lang.Normal;

import java.lang.annotation.*;

/**
 * An annotation to desensitize or filter key-value pairs within a field that contains a JSON string.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NShield {

    /**
     * An array of {@link Shield} annotations to specify desensitization rules for individual keys within the JSON.
     *
     * @return The array of shield rules.
     */
    Shield[] value() default {};

    /**
     * Specifies the class type of the JSON object. This can be used for more advanced filtering or processing based on
     * the object's structure.
     *
     * @return The class type of the JSON object.
     */
    Class<?> type();

    /**
     * A comma-separated list of property names to include from the JSON object. If specified, only these properties
     * will be retained in the output.
     *
     * @return The properties to include.
     */
    String include() default Normal.EMPTY;

    /**
     * A comma-separated list of property names to filter out (exclude) from the JSON object.
     *
     * @return The properties to filter.
     */
    String filter() default Normal.EMPTY;

}
