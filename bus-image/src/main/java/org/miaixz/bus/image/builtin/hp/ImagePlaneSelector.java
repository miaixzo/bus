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

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.nimble.geometry.ImageOrientation;
import org.miaixz.bus.image.nimble.geometry.Vector3;

/**
 * Selects images by patient image plane.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class ImagePlaneSelector {

    /**
     * Creates a new instance.
     */
    private ImagePlaneSelector() {
    }

    /**
     * Executes the matches operation.
     *
     * @param attributes   the attributes.
     * @param expectedPlan the expected plan.
     * @return true if the condition is met; otherwise false.
     */
    public static boolean matches(Attributes attributes, ImageOrientation.Plan expectedPlan) {
        return planOf(attributes) == expectedPlan;
    }

    /**
     * Executes the plan of operation.
     *
     * @param attributes the attributes.
     * @return the operation result.
     */
    public static ImageOrientation.Plan planOf(Attributes attributes) {
        double[] orientation = attributes.getDoubles(Tag.ImageOrientationPatient);
        if (orientation == null || orientation.length < 6) {
            return ImageOrientation.Plan.UNKNOWN;
        }
        return ImageOrientation.getPlan(
                new Vector3(orientation[0], orientation[1], orientation[2]),
                new Vector3(orientation[3], orientation[4], orientation[5]));
    }

}
