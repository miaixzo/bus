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
package org.miaixz.bus.core.lang.ansi;

import java.awt.*;
import java.util.Map;

/**
 * Abstract base class for mapping ANSI colors to Lab colors. This class provides functionality to find the closest
 * {@link AnsiElement} for a given {@link LabColor} or {@link Color}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AnsiLabMapping {

    /**
     * Constructs a new AnsiLabMapping. Utility class constructor for static access.
     */
    AnsiLabMapping() {
    }

    /**
     * A map storing the correspondence between {@link AnsiElement} and {@link LabColor}. Subclasses are expected to
     * populate this map.
     */
    protected Map<AnsiElement, LabColor> ansiLabMap;

    /**
     * Looks up the {@link AnsiElement} that is closest to the given {@link Color}. The input {@link Color} is first
     * converted to a {@link LabColor} for comparison.
     *
     * @param color The {@link Color} to find the closest {@link AnsiElement} for.
     * @return The {@link AnsiElement} that is closest to the given color.
     */
    public AnsiElement lookupClosest(final Color color) {
        return lookupClosest(new LabColor(color));
    }

    /**
     * Looks up the {@link AnsiElement} that is closest to the given {@link LabColor}. The "closest" color is determined
     * by calculating the color difference (distance) between the input color and each color in the {@link #ansiLabMap}.
     *
     * @param color The {@link LabColor} to find the closest {@link AnsiElement} for.
     * @return The {@link AnsiElement} that is closest to the given color.
     */
    public AnsiElement lookupClosest(final LabColor color) {
        AnsiElement closest = null;
        double closestDistance = Float.MAX_VALUE;
        for (final Map.Entry<AnsiElement, LabColor> entry : ansiLabMap.entrySet()) {
            final double candidateDistance = color.getDistance(entry.getValue());
            if (closest == null || candidateDistance < closestDistance) {
                closestDistance = candidateDistance;
                closest = entry.getKey();
            }
        }
        return closest;
    }

}
