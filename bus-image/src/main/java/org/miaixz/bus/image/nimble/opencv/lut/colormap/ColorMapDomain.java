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
package org.miaixz.bus.image.nimble.opencv.lut.colormap;

import java.util.Objects;

/**
 * Value axis of a color map: what stop positions mean and the default range they cover.
 *
 * @param kind      interpretation of stop positions
 * @param unit      physical unit for {@link DomainKind#ABSOLUTE} and {@link DomainKind#FIXED}, else null
 * @param min       lower domain bound
 * @param max       upper domain bound
 * @param reference what 100 percent means for {@link DomainKind#PERCENT}, else null
 * @author Kimi Liu
 */
public record ColorMapDomain(DomainKind kind, String unit, double min, double max, String reference) {

    /**
     * Standard unitless domain spanning zero through one.
     */
    public static final ColorMapDomain RELATIVE = new ColorMapDomain(DomainKind.RELATIVE, null, 0.0, 1.0, null);

    /**
     * Percent-domain reference representing the maximum observed value.
     */
    public static final String REFERENCE_MAX = "max";

    /**
     * Percent-domain reference representing a display window.
     */
    public static final String REFERENCE_WINDOW = "window";

    /**
     * Percent-domain reference representing a prescribed value.
     */
    public static final String REFERENCE_PRESCRIPTION = "prescription";

    /**
     * Validates the domain kind, finite ordered bounds, and kind-specific constraints.
     *
     * @throws NullPointerException     if the domain kind is null
     * @throws IllegalArgumentException if the bounds or kind-specific values are invalid
     */
    public ColorMapDomain {
        Objects.requireNonNull(kind, "Domain kind cannot be null");
        if (!Double.isFinite(min) || !Double.isFinite(max) || min >= max) {
            throw new IllegalArgumentException("Domain range must be finite with min < max");
        }
        if (kind == DomainKind.RELATIVE && (min != 0.0 || max != 1.0)) {
            throw new IllegalArgumentException("A relative domain spans [0, 1]");
        }
        if (kind == DomainKind.PERCENT && reference == null) {
            throw new IllegalArgumentException("A percent domain needs a reference");
        }
    }

    /**
     * Creates an absolute-value domain.
     *
     * @param unit physical unit
     * @param min  lower bound
     * @param max  upper bound
     * @return absolute domain
     */
    public static ColorMapDomain absolute(String unit, double min, double max) {
        return new ColorMapDomain(DomainKind.ABSOLUTE, unit, min, max, null);
    }

    /**
     * Creates a fixed-value domain that should not be rescaled automatically.
     *
     * @param unit physical unit
     * @param min  lower bound
     * @param max  upper bound
     * @return fixed domain
     */
    public static ColorMapDomain fixed(String unit, double min, double max) {
        return new ColorMapDomain(DomainKind.FIXED, unit, min, max, null);
    }

    /**
     * Creates a percentage domain relative to a named reference.
     *
     * @param reference percentage reference name
     * @param min       lower percentage bound
     * @param max       upper percentage bound
     * @return percentage domain
     */
    public static ColorMapDomain percent(String reference, double min, double max) {
        return new ColorMapDomain(DomainKind.PERCENT, null, min, max, reference);
    }

    /**
     * Returns the width of the domain.
     *
     * @return {@code max - min}
     */
    public double span() {
        return max - min;
    }

    /**
     * Fraction of the range at {@code value}, not clamped.
     */
    public double normalize(double value) {
        return (value - min) / span();
    }

    /**
     * Converts a relative fraction to a domain value without clamping.
     *
     * @param fraction relative fraction
     * @return corresponding domain value
     */
    public double denormalize(double fraction) {
        return min + fraction * span();
    }

    /**
     * Tests whether this is the standard relative domain.
     *
     * @return {@code true} for {@link DomainKind#RELATIVE}
     */
    public boolean isRelative() {
        return kind == DomainKind.RELATIVE;
    }

}
