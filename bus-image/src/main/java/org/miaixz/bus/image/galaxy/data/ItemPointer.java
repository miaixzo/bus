/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ItemPointer implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852231731983L;

    public final int sequenceTag;
    public final String privateCreator;
    public final int itemIndex;

    public ItemPointer(int sequenceTag) {
        this(null, sequenceTag, -1);
    }

    public ItemPointer(int sequenceTag, int itemIndex) {
        this(null, sequenceTag, itemIndex);
    }

    public ItemPointer(String privateCreator, int sequenceTag) {
        this(privateCreator, sequenceTag, -1);
    }

    public ItemPointer(String privateCreator, int sequenceTag, int itemIndex) {
        this.sequenceTag = sequenceTag;
        this.privateCreator = privateCreator;
        this.itemIndex = itemIndex;
    }

    public boolean equalsIgnoreItemIndex(ItemPointer that) {
        return sequenceTag == that.sequenceTag && Objects.equals(privateCreator, that.privateCreator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemPointer that = (ItemPointer) o;
        return sequenceTag == that.sequenceTag && itemIndex == that.itemIndex
                && Objects.equals(privateCreator, that.privateCreator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceTag, privateCreator, itemIndex);
    }

    @Override
    public String toString() {
        return "ItemPointer{" + "sequenceTag=" + sequenceTag + ", privateCreator='" + privateCreator + '\''
                + ", itemIndex=" + itemIndex + '}';
    }

}
