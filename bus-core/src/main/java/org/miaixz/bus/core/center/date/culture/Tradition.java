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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.Almanac;
import org.miaixz.bus.core.center.date.Culture;

/**
 * An abstract base class for traditional cultural elements, such as festivals or holidays.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class Tradition implements Culture {

    /**
     * Constructs a traditional culture item.
     */
    protected Tradition() {
    }

    /**
     * Returns the display name of this traditional culture item.
     *
     * @return The name of the traditional element.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Compares this object with another based on its textual representation.
     *
     * @param o The object to compare with.
     * @return {@code true} if the objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Almanac && toString().equals(o.toString());
    }

    /**
     * Normalizes an index to be within the valid range [0, size-1] using the modulo operator.
     *
     * @param index The index to normalize.
     * @param size  The size of the cycle or range.
     * @return The normalized 0-based index.
     */
    protected int indexOf(int index, int size) {
        return Math.floorMod(index, size);
    }

}
