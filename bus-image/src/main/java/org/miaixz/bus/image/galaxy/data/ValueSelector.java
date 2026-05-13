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
package org.miaixz.bus.image.galaxy.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the ValueSelector type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ValueSelector implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852275858733L;

    /**
     * The attribute selector value.
     */
    private final AttributeSelector attributeSelector;

    /**
     * The value index value.
     */
    private final int valueIndex;

    /**
     * The text value.
     */
    private String text;

    /**
     * Creates a new instance.
     *
     * @param tag            the tag.
     * @param privateCreator the private creator.
     * @param index          the index.
     * @param itemPointers   the item pointers.
     */
    public ValueSelector(int tag, String privateCreator, int index, ItemPointer... itemPointers) {
        this(new AttributeSelector(tag, privateCreator, itemPointers), index);
    }

    /**
     * Creates a new instance.
     *
     * @param attributeSelector the attribute selector.
     * @param index             the index.
     */
    public ValueSelector(AttributeSelector attributeSelector, int index) {
        this.attributeSelector = Objects.requireNonNull(attributeSelector);
        this.valueIndex = index;
    }

    /**
     * Executes the value of operation.
     *
     * @param s the s.
     * @return the operation result.
     */
    public static ValueSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new ValueSelector(AttributeSelector.valueOf(s), AttributeSelector.selectNumber(s, fromIndex) - 1);
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    /**
     * Executes the tag operation.
     *
     * @return the operation result.
     */
    public int tag() {
        return attributeSelector.tag();
    }

    /**
     * Executes the private creator operation.
     *
     * @return the operation result.
     */
    public String privateCreator() {
        return attributeSelector.privateCreator();
    }

    /**
     * Executes the level operation.
     *
     * @return the operation result.
     */
    public int level() {
        return attributeSelector.level();
    }

    /**
     * Executes the item pointer operation.
     *
     * @param index the index.
     * @return the operation result.
     */
    public ItemPointer itemPointer(int index) {
        return attributeSelector.itemPointer(index);
    }

    /**
     * Executes the value index operation.
     *
     * @return the operation result.
     */
    public int valueIndex() {
        return valueIndex;
    }

    /**
     * Executes the select string value operation.
     *
     * @param attrs  the attrs.
     * @param defVal the def val.
     * @return the operation result.
     */
    public String selectStringValue(Attributes attrs, String defVal) {
        return attributeSelector.selectStringValue(attrs, valueIndex, defVal);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        if (text == null)
            text = attributeSelector.toStringBuilder().append("/Value[@number=\"").append(valueIndex + 1).append("\"]")
                    .toString();
        return text;
    }

    /**
     * Returns the hash code.
     *
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Compares this instance with another object for equality.
     *
     * @param object the object.
     * @return true if the condition is met; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ValueSelector))
            return false;

        return toString().equals(object.toString());
    }

}
