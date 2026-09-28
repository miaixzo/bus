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
package org.miaixz.bus.health.builtin.hardware;

import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * Describes the mode in which a display is currently driven, including its logical and native pixel sizes, refresh
 * rate, rotation, and desktop position.
 * <p>
 * All sizes use the display's current orientation. The logical size is the area occupied in desktop coordinates, while
 * the pixel size is the native resolution sent to the panel. These values can differ when desktop scaling is active.
 *
 * @author Kimi Liu
 */
@Immutable
public interface DisplayMode {

    /**
     * Gets the logical width of the display on the desktop.
     *
     * @return the logical width
     */
    int getWidth();

    /**
     * Gets the logical height of the display on the desktop.
     *
     * @return the logical height
     */
    int getHeight();

    /**
     * Gets the width of the resolution driven to the display.
     *
     * @return the native pixel width
     */
    int getPixelWidth();

    /**
     * Gets the height of the resolution driven to the display.
     *
     * @return the native pixel height
     */
    int getPixelHeight();

    /**
     * Gets the refresh rate.
     *
     * @return the refresh rate in hertz, or 0 when unknown or not fixed
     */
    double getRefreshRate();

    /**
     * Gets the clockwise rotation relative to the display's natural orientation.
     *
     * @return the rotation in degrees: 0, 90, 180, or 270
     */
    int getRotation();

    /**
     * Gets the horizontal position of the display's top-left corner on the desktop.
     *
     * @return the x coordinate in logical units
     */
    int getX();

    /**
     * Gets the vertical position of the display's top-left corner on the desktop.
     *
     * @return the y coordinate in logical units
     */
    int getY();

}
