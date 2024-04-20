/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.galaxy.data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ValueSelector implements Serializable {

    private final AttributesSelector attributesSelector;
    private final int valueIndex;
    private String text;

    public ValueSelector(int tag, String privateCreator, int index, ItemPointer... itemPointers) {
        this(new AttributesSelector(tag, privateCreator, itemPointers), index);
    }

    public ValueSelector(AttributesSelector attributesSelector, int index) {
        this.attributesSelector = Objects.requireNonNull(attributesSelector);
        this.valueIndex = index;
    }

    public static ValueSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new ValueSelector(AttributesSelector.valueOf(s),
                    AttributesSelector.selectNumber(s, fromIndex) - 1);
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    public int tag() {
        return attributesSelector.tag();
    }

    public String privateCreator() {
        return attributesSelector.privateCreator();
    }

    public int level() {
        return attributesSelector.level();
    }

    public ItemPointer itemPointer(int index) {
        return attributesSelector.itemPointer(index);
    }

    public int valueIndex() {
        return valueIndex;
    }

    public String selectStringValue(Attributes attrs, String defVal) {
        return attributesSelector.selectStringValue(attrs, valueIndex, defVal);
    }

    @Override
    public String toString() {
        if (null == text)
            text = attributesSelector.toStringBuilder()
                    .append("/Value[@number=\"")
                    .append(valueIndex + 1)
                    .append("\"]")
                    .toString();
        return text;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ValueSelector))
            return false;

        return toString().equals(object.toString());
    }

}
