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
package org.miaixz.bus.extra.json;

/**
 * Framework-independent allow-list callback for JSON deserialization target classes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface JsonTypeFilter {

    /**
     * Shared filter that allows every requested target class.
     */
    JsonTypeFilter ALLOW_ALL = type -> true;

    /**
     * Determines whether a target class may be deserialized.
     *
     * @param type requested target class
     * @return {@code true} when the type is allowed; {@code false} otherwise
     */
    boolean allow(Class<?> type);

    /**
     * Returns the shared filter that allows every target class.
     *
     * @return allow-all type filter
     */
    static JsonTypeFilter allowAll() {
        return ALLOW_ALL;
    }

}
