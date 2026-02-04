/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ValueSelector implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852275858733L;

    private final AttributeSelector attributeSelector;
    private final int valueIndex;
    private String text;

    public ValueSelector(int tag, String privateCreator, int index, ItemPointer... itemPointers) {
        this(new AttributeSelector(tag, privateCreator, itemPointers), index);
    }

    public ValueSelector(AttributeSelector attributeSelector, int index) {
        this.attributeSelector = Objects.requireNonNull(attributeSelector);
        this.valueIndex = index;
    }

    public static ValueSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new ValueSelector(AttributeSelector.valueOf(s), AttributeSelector.selectNumber(s, fromIndex) - 1);
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    public int tag() {
        return attributeSelector.tag();
    }

    public String privateCreator() {
        return attributeSelector.privateCreator();
    }

    public int level() {
        return attributeSelector.level();
    }

    public ItemPointer itemPointer(int index) {
        return attributeSelector.itemPointer(index);
    }

    public int valueIndex() {
        return valueIndex;
    }

    public String selectStringValue(Attributes attrs, String defVal) {
        return attributeSelector.selectStringValue(attrs, valueIndex, defVal);
    }

    @Override
    public String toString() {
        if (text == null)
            text = attributeSelector.toStringBuilder().append("/Value[@number=\"").append(valueIndex + 1).append("\"]")
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
