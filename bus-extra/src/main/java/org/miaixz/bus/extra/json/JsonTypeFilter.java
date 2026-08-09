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
 */
@FunctionalInterface
public interface JsonTypeFilter {

    /**
     * Shared filter that accepts every requested target class.
     */
    JsonTypeFilter ALWAYS = type -> true;

    /**
     * Determines whether a target class may be deserialized.
     *
     * @param type requested target class
     * @return {@code true} when the type is accepted; {@code false} otherwise
     */
    boolean accept(Class<?> type);

    /**
     * Returns the shared filter that accepts every target class.
     *
     * @return always-accept type filter
     */
    static JsonTypeFilter always() {
        return ALWAYS;
    }

}
