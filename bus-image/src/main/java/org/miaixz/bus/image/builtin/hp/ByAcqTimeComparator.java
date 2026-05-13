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
package org.miaixz.bus.image.builtin.hp;

import java.util.Date;
import java.util.Objects;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * Sorts images by acquisition/content time.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ByAcqTimeComparator implements HpComparator {

    /**
     * The sign value.
     */
    private final int sign;

    /**
     * Creates a new instance.
     *
     * @param sortingDirection the sorting direction.
     */
    public ByAcqTimeComparator(SortingDirection sortingDirection) {
        this.sign = Objects.requireNonNull(sortingDirection, "sortingDirection").getSign();
    }

    /**
     * Compares two values.
     *
     * @param first       the first.
     * @param firstFrame  the first frame.
     * @param second      the second.
     * @param secondFrame the second frame.
     * @return the operation result.
     */
    @Override
    public int compare(Attributes first, int firstFrame, Attributes second, int secondFrame) {
        Date t1 = toAcqTime(first);
        Date t2 = toAcqTime(second);
        if (t1 == null || t2 == null) {
            return 0;
        }
        return t1.compareTo(t2) * sign;
    }

    /**
     * Converts this value to acq time.
     *
     * @param attributes the attributes.
     * @return the operation result.
     */
    private Date toAcqTime(Attributes attributes) {
        Date time = attributes.getDate(Tag.AcquisitionDateTime);
        if (time == null) {
            time = attributes.getDate(Tag.AcquisitionDate);
        }
        if (time == null) {
            time = attributes.getDate(Tag.ContentDate);
        }
        return time;
    }

}
