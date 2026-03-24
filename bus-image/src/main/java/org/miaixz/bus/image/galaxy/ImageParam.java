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
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;

/**
 * Represents an image parameter with a tag, values, and optional parent sequence tags. This class is used to
 * encapsulate information about a specific data element within an image.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ImageParam {

    /**
     * The tag of the image parameter.
     */
    private final int tag;
    /**
     * The values associated with the image parameter.
     */
    private final String[] values;
    /**
     * Optional parent sequence tags for the image parameter.
     */
    private final int[] parentSeqTags;

    /**
     * Constructs an {@code ImageParam} with the specified tag and values. The parent sequence tags are set to
     * {@code null}.
     *
     * @param tag    The tag of the image parameter.
     * @param values The values associated with the image parameter.
     */
    public ImageParam(int tag, String... values) {
        this(null, tag, values);
    }

    /**
     * Constructs an {@code ImageParam} with the specified parent sequence tags, tag, and values.
     *
     * @param parentSeqTags Optional parent sequence tags for the image parameter.
     * @param tag           The tag of the image parameter.
     * @param values        The values associated with the image parameter.
     */
    public ImageParam(int[] parentSeqTags, int tag, String... values) {
        this.tag = tag;
        this.values = values;
        this.parentSeqTags = parentSeqTags;
    }

    /**
     * Retrieves the tag of the image parameter.
     *
     * @return The tag of the image parameter.
     */
    public int getTag() {
        return tag;
    }

    /**
     * Retrieves the values associated with the image parameter.
     *
     * @return An array of strings representing the values of the image parameter.
     */
    public String[] getValues() {
        return values;
    }

    /**
     * Retrieves the optional parent sequence tags for the image parameter.
     *
     * @return An array of integers representing the parent sequence tags, or {@code null} if not present.
     */
    public int[] getParentSeqTags() {
        return parentSeqTags;
    }

    /**
     * Retrieves the tag name of the image parameter from the {@link ElementDictionary}.
     *
     * @return The keyword (name) corresponding to the tag, or {@code null} if not found.
     */
    public String getTagName() {
        return ElementDictionary.keywordOf(tag, null);
    }

}
