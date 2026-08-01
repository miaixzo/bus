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
 * Framework-independent callback used to decide whether a property is included during JSON serialization.
 * Implementations must be side-effect free because a provider may invoke the callback more than once.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@FunctionalInterface
public interface JsonPropertyFilter {

    /**
     * Shared filter that includes every property.
     */
    JsonPropertyFilter INCLUDE_ALL = (source, name, value) -> true;

    /**
     * Determines whether a property should be serialized.
     *
     * @param source owning object, or {@code null} when the JSON framework cannot expose it
     * @param name   serialized property name
     * @param value  current property value, or {@code null}
     * @return {@code true} to include the property; {@code false} to omit it
     */
    boolean include(Object source, String name, Object value);

    /**
     * Returns the shared filter that includes every property.
     *
     * @return include-all property filter
     */
    static JsonPropertyFilter includeAll() {
        return INCLUDE_ALL;
    }

}
