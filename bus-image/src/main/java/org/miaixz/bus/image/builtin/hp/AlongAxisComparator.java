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

import java.util.Objects;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.geometry.Vector3;

/**
 * Sorts images along the normal axis derived from Image Orientation Patient.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AlongAxisComparator implements HpComparator {

    /**
     * The sign value.
     */
    private final int sign;

    /**
     * Creates a new instance.
     *
     * @param sortingDirection the sorting direction.
     */
    public AlongAxisComparator(SortingDirection sortingDirection) {
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
        try {
            double v1 = projection(first);
            double v2 = projection(second);
            return Double.compare(v1, v2) * sign;
        } catch (NullPointerException | IllegalArgumentException | IndexOutOfBoundsException exception) {
            return 0;
        }
    }

    /**
     * Executes the projection operation.
     *
     * @param attributes the attributes.
     * @return the operation result.
     */
    private double projection(Attributes attributes) {
        double[] position = attributes.getDoubles(Tag.ImagePositionPatient);
        double[] orientation = attributes.getDoubles(Tag.ImageOrientationPatient);
        if (position == null || orientation == null || position.length < 3 || orientation.length < 6) {
            throw new IllegalArgumentException("Missing image position or orientation");
        }
        Vector3 ipp = new Vector3(position[0], position[1], position[2]);
        Vector3 row = new Vector3(orientation[0], orientation[1], orientation[2]);
        Vector3 column = new Vector3(orientation[3], orientation[4], orientation[5]);
        return ipp.dot(row.cross(column));
    }

}
