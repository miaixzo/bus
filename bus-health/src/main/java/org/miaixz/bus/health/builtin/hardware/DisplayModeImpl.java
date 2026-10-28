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

import java.util.Locale;
import java.util.Objects;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * Default immutable {@link DisplayMode} implementation.
 *
 * @author Kimi Liu
 */
@Immutable
public final class DisplayModeImpl implements DisplayMode {

    /**
     * The logical width.
     */
    private final int width;

    /**
     * The logical height.
     */
    private final int height;

    /**
     * The native pixel width.
     */
    private final int pixelWidth;

    /**
     * The native pixel height.
     */
    private final int pixelHeight;

    /**
     * The refresh rate in hertz.
     */
    private final double refreshRate;

    /**
     * The clockwise rotation in degrees.
     */
    private final int rotation;

    /**
     * The desktop x coordinate.
     */
    private final int x;

    /**
     * The desktop y coordinate.
     */
    private final int y;

    /**
     * Constructs a display mode. All sizes use the display's current orientation.
     *
     * @param x           the x coordinate of the display's top-left corner
     * @param y           the y coordinate of the display's top-left corner
     * @param width       the logical width
     * @param height      the logical height
     * @param pixelWidth  the native pixel width
     * @param pixelHeight the native pixel height
     * @param refreshRate the refresh rate in hertz, or 0 if unknown
     * @param rotation    the clockwise rotation, normalized to the nearest quarter turn
     */
    public DisplayModeImpl(int x, int y, int width, int height, int pixelWidth, int pixelHeight, double refreshRate,
            int rotation) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        this.refreshRate = refreshRate > Normal._0 ? refreshRate : Normal._0;
        this.rotation = normalizeRotation(rotation);
    }

    /**
     * Normalizes a rotation to the nearest quarter turn in the range 0 through 270 degrees.
     *
     * @param degrees the clockwise rotation in degrees
     * @return 0, 90, 180, or 270
     */
    static int normalizeRotation(int degrees) {
        int quarterTurns = Math.round(degrees / (float) Normal._90) % Normal._4;
        return (quarterTurns < Normal._0 ? quarterTurns + Normal._4 : quarterTurns) * Normal._90;
    }

    /**
     * Returns the stored logical width in desktop coordinates.
     *
     * @return the logical width in the current display orientation
     */
    @Override
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the stored logical height in desktop coordinates.
     *
     * @return the logical height in the current display orientation
     */
    @Override
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the stored native pixel width driven to the display.
     *
     * @return the native pixel width in the current display orientation
     */
    @Override
    public int getPixelWidth() {
        return this.pixelWidth;
    }

    /**
     * Returns the stored native pixel height driven to the display.
     *
     * @return the native pixel height in the current display orientation
     */
    @Override
    public int getPixelHeight() {
        return this.pixelHeight;
    }

    /**
     * Returns the stored refresh rate after non-positive constructor values have been normalized to zero.
     *
     * @return the refresh rate in hertz, or zero when unknown or not fixed
     */
    @Override
    public double getRefreshRate() {
        return this.refreshRate;
    }

    /**
     * Returns the stored clockwise rotation normalized to the nearest quarter turn.
     *
     * @return the rotation in degrees: 0, 90, 180, or 270
     */
    @Override
    public int getRotation() {
        return this.rotation;
    }

    /**
     * Returns the horizontal desktop position of the display's top-left corner.
     *
     * @return the x coordinate in logical desktop units
     */
    @Override
    public int getX() {
        return this.x;
    }

    /**
     * Returns the vertical desktop position of the display's top-left corner.
     *
     * @return the y coordinate in logical desktop units
     */
    @Override
    public int getY() {
        return this.y;
    }

    /**
     * Compares this mode with another {@code DisplayModeImpl} using all stored mode fields.
     *
     * @param object the object to compare with this mode
     * @return {@code true} when both objects have equal dimensions, refresh rate, rotation, and desktop position
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DisplayModeImpl)) {
            return false;
        }
        DisplayModeImpl other = (DisplayModeImpl) object;
        return this.width == other.width && this.height == other.height && this.pixelWidth == other.pixelWidth
                && this.pixelHeight == other.pixelHeight && Double.compare(this.refreshRate, other.refreshRate) == 0
                && this.rotation == other.rotation && this.x == other.x && this.y == other.y;
    }

    /**
     * Computes a hash code from the dimensions, refresh rate, rotation, and desktop position used by equality.
     *
     * @return the hash code for all stored mode fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                this.width,
                this.height,
                this.pixelWidth,
                this.pixelHeight,
                this.refreshRate,
                this.rotation,
                this.x,
                this.y);
    }

    /**
     * Formats the logical dimensions and desktop position, including different native dimensions, a known refresh
     * rate, and nonzero rotation when present.
     *
     * @return a human-readable description of this display mode
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.width).append('x').append(this.height);
        if (this.pixelWidth != this.width || this.pixelHeight != this.height) {
            builder.append(" (").append(this.pixelWidth).append('x').append(this.pixelHeight).append(" pixels)");
        }
        if (this.refreshRate > Normal._0) {
            builder.append(String.format(Locale.ROOT, " @ %.2f Hz", this.refreshRate));
        }
        if (this.rotation != Normal._0) {
            builder.append(", rotated ").append(this.rotation).append(" degrees");
        }
        builder.append(" at (").append(this.x).append(',').append(this.y).append(')');
        return builder.toString();
    }

}
