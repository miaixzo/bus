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
package org.miaixz.bus.image.nimble.mpr;

import java.util.Locale;

import org.miaixz.bus.image.nimble.geometry.Vector3;

/**
 * Patient-space axis mapping for canonical MPR planes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum AxisDirection {

    /**
     * The axial value.
     */
    AXIAL("axial", Vector3.UNIT_X, Vector3.UNIT_Y, new Vector3(0.0, 0.0, -1.0), true),
    /**
     * The coronal value.
     */
    CORONAL("coronal", Vector3.UNIT_X, new Vector3(0.0, 0.0, -1.0), Vector3.UNIT_Y, false),
    /**
     * The sagittal value.
     */
    SAGITTAL("sagittal", Vector3.UNIT_Y, new Vector3(0.0, 0.0, -1.0), Vector3.UNIT_X, false);

    /**
     * The label value.
     */
    private final String label;

    /**
     * The axis x value.
     */
    private final Vector3 axisX;

    /**
     * The axis y value.
     */
    private final Vector3 axisY;

    /**
     * The axis z value.
     */
    private final Vector3 axisZ;

    /**
     * The inverted direction value.
     */
    private final boolean invertedDirection;

    /**
     * Creates a new instance.
     *
     * @param label             the label.
     * @param axisX             the axis x.
     * @param axisY             the axis y.
     * @param axisZ             the axis z.
     * @param invertedDirection the inverted direction.
     */
    AxisDirection(String label, Vector3 axisX, Vector3 axisY, Vector3 axisZ, boolean invertedDirection) {
        this.label = label;
        this.axisX = axisX;
        this.axisY = axisY;
        this.axisZ = axisZ;
        this.invertedDirection = invertedDirection;
    }

    /**
     * Gets the label.
     *
     * @return the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the axis x.
     *
     * @return the axis x.
     */
    public Vector3 getAxisX() {
        return axisX;
    }

    /**
     * Gets the axis y.
     *
     * @return the axis y.
     */
    public Vector3 getAxisY() {
        return axisY;
    }

    /**
     * Gets the axis z.
     *
     * @return the axis z.
     */
    public Vector3 getAxisZ() {
        return axisZ;
    }

    /**
     * Determines whether inverted direction.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isInvertedDirection() {
        return invertedDirection;
    }

    /**
     * Creates a value from the supplied input.
     *
     * @param plane the plane.
     * @return the operation result.
     */
    public static AxisDirection of(String plane) {
        if (plane == null || plane.isBlank()) {
            throw new IllegalArgumentException("plane cannot be blank");
        }
        return AxisDirection.valueOf(plane.trim().toUpperCase(Locale.ROOT));
    }

}
