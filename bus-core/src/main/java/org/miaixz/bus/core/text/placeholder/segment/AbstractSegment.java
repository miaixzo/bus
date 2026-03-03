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
package org.miaixz.bus.core.text.placeholder.segment;

/**
 * Abstract base class for placeholder segments in string templates.
 * <p>
 * For example: {@literal "???" -> "???", "{}" -> "{}", "{name}" -> "name"}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractSegment implements StringSegment {

    /**
     * The placeholder variable, e.g., {@literal "???" -> "???", "{}" -> "{}", "{name}" -> "name"}
     */
    private final String placeholder;

    /**
     * Constructs an {@code AbstractSegment} with the given placeholder.
     *
     * @param placeholder The placeholder string.
     */
    protected AbstractSegment(final String placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * Retrieves the text value of the segment.
     *
     * @return The text value, which is the placeholder itself.
     */
    @Override
    public String getText() {
        return placeholder;
    }

    /**
     * Retrieves the placeholder string.
     *
     * @return The placeholder string.
     */
    public String getPlaceholder() {
        return placeholder;
    }

}
