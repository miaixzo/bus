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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Tag;

import java.io.Serializable;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AttributesSelector implements Serializable {

    private static final int MIN_ITEM_POINTER_STR_LEN = 30;

    private final int tag;
    private final String privateCreator;
    private final List<ItemPointer> itemPointers;
    private String text;

    public AttributesSelector(int tag) {
        this(tag, null, Collections.EMPTY_LIST);
    }

    public AttributesSelector(int tag, String privateCreator) {
        this(tag, privateCreator, Collections.EMPTY_LIST);
    }

    public AttributesSelector(int tag, String privateCreator, ItemPointer... itemPointers) {
        this(tag, privateCreator, Arrays.asList(itemPointers));
    }

    public AttributesSelector(int tag, String privateCreator, List<ItemPointer> itemPointers) {
        this.tag = tag;
        this.privateCreator = privateCreator;
        this.itemPointers = itemPointers;
    }

    public static AttributesSelector valueOf(String s) {
        int fromIndex = s.lastIndexOf("DicomAttribute");
        try {
            return new AttributesSelector(
                    selectTag(s, fromIndex),
                    selectPrivateCreator(s, fromIndex),
                    itemPointersOf(s, fromIndex));
        } catch (Exception e) {
            throw new IllegalArgumentException(s);
        }
    }

    static int selectTag(String s, int fromIndex) {
        String tagStr = select("@tag=", s, fromIndex);
        return Integer.parseInt(tagStr, Normal._16);
    }

    static String selectPrivateCreator(String s, int fromIndex) {
        return select("@privateCreator=", s, fromIndex);
    }

    static int selectNumber(String s, int fromIndex) {
        String no = select("@number=", s, fromIndex);
        return null != no ? Integer.parseInt(no) : 0;
    }

    private static List<ItemPointer> itemPointersOf(String s, int endIndex) {
        if (endIndex == 0)
            return Collections.emptyList();

        ArrayList<ItemPointer> list = new ArrayList<>();
        int fromIndex = 0;
        while (fromIndex < endIndex) {
            list.add(new ItemPointer(
                    selectTag(s, fromIndex),
                    selectPrivateCreator(s, fromIndex),
                    selectNumber(s, fromIndex) - 1));
            fromIndex = s.indexOf("DicomAttribute",
                    fromIndex + MIN_ITEM_POINTER_STR_LEN);
        }
        list.trimToSize();
        return list;
    }

    private static String select(String key, String s, int fromIndex) {
        int pos = s.indexOf(key, fromIndex);
        if (pos < 0)
            return null;

        int quotePos = pos + key.length();
        int beginIndex = quotePos + 1;
        return s.substring(beginIndex, s.indexOf(s.charAt(quotePos), beginIndex));
    }

    public int tag() {
        return tag;
    }

    public String privateCreator() {
        return privateCreator;
    }

    public int level() {
        return itemPointers.size();
    }

    public ItemPointer itemPointer(int index) {
        return itemPointers.get(index);
    }

    public String selectStringValue(Attributes attrs, int valueIndex, String defVal) {
        Attributes item = attrs.getNestedDataset(itemPointers);
        return null != item ? item.getString(privateCreator, tag, valueIndex, defVal) : defVal;
    }

    @Override
    public String toString() {
        if (null == text)
            text = toStringBuilder().toString();
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        AttributesSelector that = (AttributesSelector) o;
        return tag == that.tag &&
                Objects.equals(privateCreator, that.privateCreator) &&
                itemPointers.equals(that.itemPointers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, privateCreator, itemPointers);
    }

    StringBuilder toStringBuilder() {
        StringBuilder sb = new StringBuilder(Normal._32);
        for (ItemPointer ip : itemPointers) {
            appendTo(ip.sequenceTag, ip.privateCreator, "\"]/Item", sb);
            if (ip.itemIndex >= 0)
                sb.append("[@number=\"").append(ip.itemIndex + 1).append("\"]");
            sb.append(Symbol.C_SLASH);
        }
        return appendTo(tag, privateCreator, "\"]", sb);
    }

    private StringBuilder appendTo(int tag, String privateCreator, String suffix, StringBuilder sb) {
        sb.append("DicomAttribute[@tag=\"").append(Tag.toHexString(tag));
        if (null != privateCreator)
            sb.append("\" and @privateCreator=\"").append(privateCreator);
        return sb.append(suffix);
    }

    public boolean matches(List<ItemPointer> itemPointers, String privateCreator, int tag) {
        int level;
        if (tag != this.tag || !Objects.equals(privateCreator, this.privateCreator)
                || (itemPointers.size() != (level = level()))) {
            return false;
        }
        for (int i = 0; i < level; i++) {
            ItemPointer itemPointer = itemPointers.get(i);
            ItemPointer other = itemPointer(i);
            if (itemPointer.itemIndex < 0 || other.itemIndex < 0
                    ? itemPointer.equalsIgnoreItemIndex(other)
                    : itemPointer.equals(other)) {
                return false;
            }
        }
        return true;
    }

}
