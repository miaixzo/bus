/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;

/**
 * Represents an image parameter with a tag, values, and optional parent sequence tags. This class is used to
 * encapsulate information about a specific data element within an image.
 *
 * @author Kimi Liu
 * @since Java 17+
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
