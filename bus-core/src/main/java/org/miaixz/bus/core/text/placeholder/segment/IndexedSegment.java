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
 * Represents an indexed placeholder segment in a string template.
 * <p>
 * For example, "{1}".
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IndexedSegment extends NamedSegment {

    /**
     * The index value of the placeholder.
     */
    private final int index;

    /**
     * Constructs an {@code IndexedSegment} with the given index string and whole placeholder text.
     *
     * @param idxStr           The string representation of the index variable.
     * @param wholePlaceholder The complete text of the placeholder, including delimiters.
     */
    public IndexedSegment(final String idxStr, final String wholePlaceholder) {
        super(idxStr, wholePlaceholder);
        this.index = Integer.parseInt(idxStr);
    }

    /**
     * Retrieves the integer index of the placeholder.
     *
     * @return The integer index.
     */
    public int getIndex() {
        return index;
    }

}
