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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.miaixz.bus.core.lang.Normal;

/**
 * Marks a field as a logical status column, typically used for implementing soft deletes in a persistence layer. This
 * annotation allows frameworks to automatically handle filtering for active or inactive records.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Logical {

    /**
     * The value that represents a logically deleted or inactive state. For example, this could be "-1", "DELETED", or
     * even "null".
     *
     * @return The string representation of the deleted state value, defaulting to "-1".
     */
    String value() default Normal.EMPTY + Normal.__1;

    /**
     * The value that represents a valid or active state.
     *
     * @return The string representation of the active state value, defaulting to "1".
     */
    String valid() default Normal.EMPTY + Normal._1;

    /**
     * A hint for query generation, indicating whether to use an equals condition for active records. Using an equals
     * condition (e.g., {@code WHERE status = '1'}) is often more efficient for database indexes than a not-equals
     * condition (e.g., {@code WHERE status != '-1'}).
     *
     * @return {@code true} to suggest using an equals condition for queries, {@code false} otherwise.
     */
    boolean useEqualsCondition() default false;

}
