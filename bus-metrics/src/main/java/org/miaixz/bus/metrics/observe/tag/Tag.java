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
package org.miaixz.bus.metrics.observe.tag;

/**
 * An immutable key=value metric tag.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public record Tag(String key, String value) {

    /**
     * Compact constructor — validates key and normalises null value to empty string.
     *
     * @throws IllegalArgumentException if {@code key} is null or blank
     */
    public Tag {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Tag key must not be blank");
        }
        if (value == null) {
            value = "";
        }
    }

    /**
     * Factory method for concise tag construction.
     *
     * @param key   tag key; must not be blank
     * @param value tag value; {@code null} is normalised to {@code ""}
     * @return a new immutable Tag
     */
    public static Tag of(String key, String value) {
        return new Tag(key, value);
    }

    /** Returns {@code key="value"} suitable for use in Prometheus label strings. */
    @Override
    public String toString() {
        return key + "=\"" + value + "\"";
    }

}
