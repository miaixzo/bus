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
 * @author Kimi Liu
 * @since Java 17+
 */
public class ItemPointer implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852263859002L;

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
