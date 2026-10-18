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
package org.miaixz.bus.mapper.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Orders mapped Java properties in newly created tables only. Rules are merged from ancestors to descendants. Does not
 * reorder existing tables, CRUD columns, composite keys, or indexes. Excluded superclasses contribute no rules.
 * Property references are checked after merging; replaced rules do not constrain the final mapped properties.
 * Declaration errors such as duplicates and mutually exclusive modes are always rejected.
 *
 * @author Kimi Liu
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ColumnOrder {

    /**
     * Complete property order, replacing inherited and default rules. Must cover all mapped properties. Mutually
     * exclusive with nonempty {@link #first()} and {@link #last()}.
     *
     * @return the complete property order
     */
    String[] value() default {};

    /**
     * Properties moved to the front, in the specified order. Other inherited rules remain effective.
     *
     * @return the leading properties
     */
    String[] first() default {};

    /**
     * Properties moved to the end, in the specified order. A nonempty list replaces the entire default or inherited
     * trailing list. An empty list preserves preceding trailing rules; properties explicitly moved to the front are
     * removed from that list.
     *
     * @return the trailing properties
     */
    String[] last() default {};

}
