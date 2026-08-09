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

import java.util.Objects;

/**
 * Immutable, framework-independent options for JSON serialization.
 *
 * @param dateFormat     optional legacy date format; blank means provider default
 * @param writeNulls     whether null-valued properties should be written
 * @param propertyFilter annotation-aware callback controlling property inclusion
 * @author Kimi Liu
 */
public record JsonWriteOptions(String dateFormat, boolean writeNulls, JsonPropertyFilter propertyFilter) {

    /**
     * Creates validated write options.
     *
     * @param dateFormat     optional legacy date format
     * @param writeNulls     whether null-valued properties should be written
     * @param propertyFilter caller-supplied callback; automatically decorated with mandatory Bus annotation rules
     */
    public JsonWriteOptions {
        dateFormat = dateFormat == null || dateFormat.isBlank() ? null : dateFormat;
        propertyFilter = JsonAnnotationFilter.of(Objects.requireNonNull(propertyFilter, "propertyFilter"));
    }

    /**
     * Creates compatibility options that preserve provider defaults, apply Bus annotation rules, and impose no
     * additional caller property restrictions.
     *
     * @return default write options
     */
    public static JsonWriteOptions defaults() {
        return new JsonWriteOptions(null, true, JsonPropertyFilter.always());
    }

    /**
     * Determines whether the caller configured a property rule in addition to mandatory annotation filtering.
     *
     * @return {@code true} when an additional caller filter is configured
     */
    public boolean hasPropertyFilter() {
        return propertyFilter instanceof JsonAnnotationFilter annotationFilter && annotationFilter.hasDelegateFilter();
    }

}
