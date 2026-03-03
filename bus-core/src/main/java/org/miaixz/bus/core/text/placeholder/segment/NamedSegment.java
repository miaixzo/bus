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
 * Represents a named placeholder segment in a string template.
 * <p>
 * For example, "{name}", "#{data}".
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NamedSegment extends AbstractSegment {

    /**
     * The complete text of the placeholder, e.g., {@literal "{name}" -> "{name}"}
     */
    private final String wholePlaceholder;

    /**
     * Constructs a {@code NamedSegment} with the given name and whole placeholder text.
     * 
     * @param name             The name of the placeholder variable.
     * @param wholePlaceholder The complete text of the placeholder, including delimiters.
     */
    public NamedSegment(final String name, final String wholePlaceholder) {
        super(name);
        this.wholePlaceholder = wholePlaceholder;
    }

    /**
     * Retrieves the complete text of the placeholder.
     * 
     * @return The complete text of the placeholder.
     */
    @Override
    public String getText() {
        return wholePlaceholder;
    }

}
