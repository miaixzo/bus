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
package org.miaixz.bus.image.galaxy.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import org.miaixz.bus.image.Tag;

/**
 * Represents a selector for DICOM attributes, allowing selection based on tag, private creator, and item pointers. This
 * class is used to identify specific attributes within a complex DICOM dataset, especially those nested within
 * sequences.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AttributeSelector implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852259019856L;

    /**
     * The minimum string length for an item pointer, used for parsing.
     */
    private static final int MIN_ITEM_POINTER_STR_LEN = 30;

    /**
     * The DICOM tag of the attribute to select.
     */
    private final int tag;
    /**
     * The private creator of the attribute, if applicable.
     */
    private final String privateCreator;
    /**
     * A list of {@link ItemPointer} objects that specify the path to the attribute within nested sequences.
     */
    private final List<ItemPointer> itemPointers;
    /**
     * A cached string representation of this attribute selector.
     */
    private String str;

    /**
     * Constructs an {@code AttributeSelector} with the specified tag and no private creator or item pointers.
     * 
     * @param tag The DICOM tag of the attribute.
     */
    public AttributeSelector(int tag) {
        this(tag, null, Collections.emptyList());
    }

    /**
     * Constructs an {@code AttributeSelector} with the specified tag and private creator, and no item pointers.
     * 
     * @param tag            The DICOM tag of the attribute.
     * @param privateCreator The private creator of the attribute.
     */
    public AttributeSelector(int tag, String privateCreator) {
        this(tag, privateCreator, Collections.emptyList());
    }

    /**
     * Constructs an {@code AttributeSelector} with the specified tag, private creator, and item pointers.
     * 
     * @param tag            The DICOM tag of the attribute.
     * @param privateCreator The private creator of the attribute.
     * @param itemPointers   An array of {@link ItemPointer} objects specifying the path to the attribute.
     */
    public AttributeSelector(int tag, String privateCreator, ItemPointer... itemPointers) {
        this(tag, privateCreator, Arrays.asList(itemPointers));
    }

    /**
     * Constructs an {@code AttributeSelector} with the specified tag, private creator, and a list of item pointers.
     * 
     * @param tag            The DICOM tag of the attribute.
     * @param privateCreator The private creator of the attribute.
     * @param itemPointers   A list of {@link ItemPointer} objects specifying the path to the attribute.
     */
    public AttributeSelector(int tag, String privateCreator, List<ItemPointer> itemPointers) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.itemPointers = itemPointers;
    }

    /**
     * Creates an {@code AttributeSelector} instance from its string representation. The string representation is
     * typically generated by the {@link #toString()} method.
     * 
     * @param s The string representation of the attribute selector.
     * @return An {@code AttributeSelector} instance.
     * @throws IllegalArgumentException If the string cannot be parsed into a valid {@code AttributeSelector}.
     */
    public static AttributeSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new AttributeSelector(selectTag(s, fromIndex), selectPrivateCreator(s, fromIndex),
                    itemPointersOf(s, fromIndex));
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    /**
     * Extracts the DICOM tag from a string representation of an attribute selector.
     * 
     * @param s         The string representation.
     * @param fromIndex The starting index for parsing the tag.
     * @return The DICOM tag as an integer.
     */
    static int selectTag(String s, int fromIndex) {
        String tagStr = select("@tag=", s, fromIndex);
        return Integer.parseInt(tagStr, 16);
    }

    /**
     * Extracts the private creator from a string representation of an attribute selector.
     * 
     * @param s         The string representation.
     * @param fromIndex The starting index for parsing the private creator.
     * @return The private creator string, or {@code null} if not found.
     */
    static String selectPrivateCreator(String s, int fromIndex) {
        return select("@privateCreator=", s, fromIndex);
    }

    /**
     * Extracts the item number from a string representation of an attribute selector.
     * 
     * @param s         The string representation.
     * @param fromIndex The starting index for parsing the item number.
     * @return The item number as an integer, or 0 if not found.
     */
    static int selectNumber(String s, int fromIndex) {
        String no = select("@number=", s, fromIndex);
        return no != null ? Integer.parseInt(no) : 0;
    }

    /**
     * Parses a list of {@link ItemPointer} objects from a string representation.
     * 
     * @param s        The string representation of the attribute selector.
     * @param endIndex The ending index for parsing item pointers.
     * @return A list of {@link ItemPointer} objects.
     */
    private static List<ItemPointer> itemPointersOf(String s, int endIndex) {
        if (endIndex == 0)
            return Collections.emptyList();

        ArrayList<ItemPointer> list = new ArrayList<>();
        int fromIndex = 0;
        while (fromIndex < endIndex) {
            list.add(
                    new ItemPointer(selectPrivateCreator(s, fromIndex), selectTag(s, fromIndex),
                            selectNumber(s, fromIndex) - 1));
            fromIndex = s.indexOf("DicomAttribute", fromIndex + MIN_ITEM_POINTER_STR_LEN);
        }
        list.trimToSize();
        return list;
    }

    /**
     * Helper method to extract a value associated with a key from a string.
     * 
     * @param key       The key to search for (e.g., "@tag=").
     * @param s         The string to parse.
     * @param fromIndex The starting index for the search.
     * @return The extracted value string, or {@code null} if the key is not found.
     */
    private static String select(String key, String s, int fromIndex) {
        int pos = s.indexOf(key, fromIndex);
        if (pos < 0)
            return null;

        int quotePos = pos + key.length();
        int beginIndex = quotePos + 1;
        return s.substring(beginIndex, s.indexOf(s.charAt(quotePos), beginIndex));
    }

    /**
     * Returns the DICOM tag of this attribute selector.
     * 
     * @return The DICOM tag.
     */
    public int tag() {
        return tag;
    }

    /**
     * Returns the private creator of this attribute selector.
     * 
     * @return The private creator string, or {@code null} if not present.
     */
    public String privateCreator() {
        return privateCreator;
    }

    /**
     * Returns the nesting level of this attribute selector, which is the number of item pointers.
     * 
     * @return The nesting level.
     */
    public int level() {
        return itemPointers.size();
    }

    /**
     * Returns the {@link ItemPointer} at the specified index.
     * 
     * @param index The index of the item pointer.
     * @return The {@link ItemPointer} at the given index.
     */
    public ItemPointer itemPointer(int index) {
        return itemPointers.get(index);
    }

    /**
     * Selects a string value from the given {@link Attributes} based on this attribute selector. If the attribute is
     * not found or is {@code null}, the default value is returned.
     * 
     * @param attrs      The {@link Attributes} object to search within.
     * @param valueIndex The index of the value to retrieve (for multi-valued attributes).
     * @param defVal     The default value to return if the attribute is not found.
     * @return The selected string value, or the default value.
     */
    public String selectStringValue(Attributes attrs, int valueIndex, String defVal) {
        Attributes item = attrs.getNestedDataset(itemPointers);
        return item != null ? item.getString(privateCreator, tag, valueIndex, defVal) : defVal;
    }

    /**
     * Returns a string representation of this attribute selector. The string representation can be used to reconstruct
     * the attribute selector using {@link #valueOf(String)}.
     * 
     * @return A string representation of the attribute selector.
     */
    @Override
    public String toString() {
        if (str == null)
            str = toStringBuilder().toString();
        return str;
    }

    /**
     * Compares this {@code AttributeSelector} to the specified object. The result is {@code true} if and only if the
     * argument is not {@code null} and is an {@code AttributeSelector} object that represents the same tag, private
     * creator, and item pointers.
     * 
     * @param o The object to compare this {@code AttributeSelector} against.
     * @return {@code true} if the given object represents an {@code AttributeSelector} equivalent to this selector,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AttributeSelector that = (AttributeSelector) o;
        return tag == that.tag && Objects.equals(privateCreator, that.privateCreator)
                && itemPointers.equals(that.itemPointers);
    }

    /**
     * Returns a hash code for this {@code AttributeSelector}.
     * 
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(tag, privateCreator, itemPointers);
    }

    /**
     * Builds a {@link StringBuilder} containing the string representation of this attribute selector. This method is
     * used internally by {@link #toString()}.
     * 
     * @return A {@link StringBuilder} with the attribute selector's string representation.
     */
    StringBuilder toStringBuilder() {
        StringBuilder sb = new StringBuilder(32);
        for (ItemPointer ip : itemPointers) {
            appendTo(ip.sequenceTag, ip.privateCreator, "\"]/Item", sb);
            if (ip.itemIndex >= 0)
                sb.append("[@number=\"").append(ip.itemIndex + 1).append("\"]");
            sb.append('/');
        }
        return appendTo(tag, privateCreator, "\"]", sb);
    }

    /**
     * Appends the string representation of a DICOM tag and private creator to a {@link StringBuilder}. This is a helper
     * method for {@link #toStringBuilder()}.
     * 
     * @param tag            The DICOM tag.
     * @param privateCreator The private creator string.
     * @param suffix         The suffix to append after the tag and private creator.
     * @param sb             The {@link StringBuilder} to append to.
     * @return The modified {@link StringBuilder}.
     */
    private StringBuilder appendTo(int tag, String privateCreator, String suffix, StringBuilder sb) {
        sb.append("DicomAttribute[@tag=\"").append(Tag.toHexString(tag));
        if (privateCreator != null)
            sb.append("\" and @privateCreator=\"").append(privateCreator);
        return sb.append(suffix);
    }

    /**
     * Checks if this attribute selector matches the given item pointers, private creator, and tag.
     * 
     * @param itemPointers   The list of {@link ItemPointer} objects to compare against.
     * @param privateCreator The private creator string to compare against.
     * @param tag            The DICOM tag to compare against.
     * @return {@code true} if the selector matches, {@code false} otherwise.
     */
    public boolean matches(List<ItemPointer> itemPointers, String privateCreator, int tag) {
        int level;
        if (tag != this.tag || !Objects.equals(privateCreator, this.privateCreator)
                || (itemPointers.size() != (level = level()))) {
            return false;
        }
        for (int i = 0; i < level; i++) {
            ItemPointer itemPointer = itemPointers.get(i);
            ItemPointer other = itemPointer(i);
            if (!(itemPointer.itemIndex < 0 || other.itemIndex < 0 ? itemPointer.equalsIgnoreItemIndex(other)
                    : itemPointer.equals(other))) {
                return false;
            }
        }
        return true;
    }

}
