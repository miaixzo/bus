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
package org.miaixz.bus.image.nimble.opencv.seg;

import java.awt.Color;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.opencv.lut.ColorLut;

/**
 * Represents the visual and descriptive attributes of a segmentation region. This class encapsulates display properties
 * such as color, opacity, visibility, and metadata like labels and pixel counts for regions in medical imaging.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RegionAttributes implements Comparable<RegionAttributes> {

    /**
     * The default line thickness value.
     */
    private static final float DEFAULT_LINE_THICKNESS = 1.0f;

    /**
     * The default opacity value.
     */
    private static final float DEFAULT_OPACITY = 1.0f;

    /**
     * The min opacity value.
     */
    private static final float MIN_OPACITY = 0.0f;

    /**
     * The max opacity value.
     */
    private static final float MAX_OPACITY = 1.0f;

    /**
     * The uninitialized pixel count value.
     */
    private static final long UNINITIALIZED_PIXEL_COUNT = -1L;

    /**
     * The label separators value.
     */
    private static final String[] LABEL_SEPARATORS = { " ", "_", "-" };

    /**
     * The min prefix length value.
     */
    private static final int MIN_PREFIX_LENGTH = 3;

    /**
     * The collator value.
     */
    private static final Collator COLLATOR = Collator.getInstance();

    /**
     * The ID value.
     */
    private final int id;

    /**
     * The label value.
     */
    private String label;

    /**
     * The description value.
     */
    private String description;

    /**
     * The type value.
     */
    private String type;

    /**
     * The color value.
     */
    private Color color;

    /**
     * The filled value.
     */
    private boolean filled = true;

    /**
     * The line thickness value.
     */
    private float lineThickness = DEFAULT_LINE_THICKNESS;

    /**
     * The visible value.
     */
    private boolean visible = true;

    /**
     * The interior opacity value.
     */
    private float interiorOpacity = DEFAULT_OPACITY;

    /**
     * The number of pixels value.
     */
    protected long numberOfPixels = UNINITIALIZED_PIXEL_COUNT;

    /**
     * Creates a new instance.
     *
     * @param id    the ID.
     * @param label the label.
     */
    public RegionAttributes(int id, String label) {
        this(id, label, null);
    }

    /**
     * Creates a new instance.
     *
     * @param id    the ID.
     * @param label the label.
     * @param color the color.
     */
    public RegionAttributes(int id, String label, Color color) {
        this.id = id;
        setLabel(label);
        this.color = color;
    }

    /**
     * Returns the ID.
     *
     * @return the ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the label.
     *
     * @return the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param label the label.
     */
    public void setLabel(String label) {
        if (!StringKit.hasText(label)) {
            throw new IllegalArgumentException("Label cannot be null or empty");
        }
        this.label = label;
    }

    /**
     * Returns the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the type.
     *
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the color.
     *
     * @return the color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color.
     *
     * @param color the color.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Checks whether the filled condition is true.
     *
     * @return true if the filled condition is true; otherwise false.
     */
    public boolean isFilled() {
        return filled;
    }

    /**
     * Sets the filled.
     *
     * @param filled the filled.
     */
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    /**
     * Returns the line thickness.
     *
     * @return the line thickness.
     */
    public float getLineThickness() {
        return lineThickness;
    }

    /**
     * Sets the line thickness.
     *
     * @param lineThickness the line thickness.
     */
    public void setLineThickness(float lineThickness) {
        if (lineThickness < 0) {
            throw new IllegalArgumentException("Line thickness cannot be negative");
        }
        this.lineThickness = lineThickness;
    }

    /**
     * Checks whether the visible condition is true.
     *
     * @return true if the visible condition is true; otherwise false.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Sets the visible.
     *
     * @param visible the visible.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns the interior opacity.
     *
     * @return the interior opacity.
     */
    public float getInteriorOpacity() {
        return interiorOpacity;
    }

    /**
     * Sets the interior opacity.
     *
     * @param interiorOpacity the interior opacity.
     */
    public void setInteriorOpacity(float interiorOpacity) {
        this.interiorOpacity = clampOpacity(interiorOpacity);
    }

    /**
     * Returns the number of pixels.
     *
     * @return the number of pixels.
     */
    public long getNumberOfPixels() {
        return numberOfPixels;
    }

    /**
     * Returns the prefix.
     *
     * @return the prefix.
     */
    public String getPrefix() {
        if (label == null) {
            return "";
        }

        return findFirstSeparator(label).map(index -> label.substring(0, index)).orElse(label);
    }

    /**
     * Finds the first separator.
     *
     * @param text the text.
     * @return the operation result.
     */
    private java.util.Optional<Integer> findFirstSeparator(String text) {
        return java.util.Arrays.stream(LABEL_SEPARATORS).mapToInt(text::indexOf)
                .filter(index -> index > MIN_PREFIX_LENGTH).min().stream().boxed().findFirst();
    }

    /**
     * Adds the pixels.
     *
     * @param region the region.
     */
    public void addPixels(Region region) {
        if (region == null) {
            return;
        }

        long regionPixels = region.getNumberOfPixels();
        if (regionPixels < 0) {
            return;
        }
        if (isPixelCountUninitialized()) {
            resetPixelCount();
        }
        this.numberOfPixels += regionPixels;
    }

    /**
     * Resets the pixel count.
     */
    public void resetPixelCount() {
        this.numberOfPixels = 0;
    }

    /**
     * Checks whether the pixel count uninitialized condition is true.
     *
     * @return true if the pixel count uninitialized condition is true; otherwise false.
     */
    private boolean isPixelCountUninitialized() {
        return numberOfPixels < 0;
    }

    /**
     * Executes the group regions operation.
     *
     * @param regions the regions.
     * @return the operation result.
     */
    public static <E extends RegionAttributes> Map<String, List<E>> groupRegions(Collection<E> regions) {
        if (regions == null || regions.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<E>> groupedRegions = new HashMap<>();
        for (E region : regions) {
            String prefix = region.getPrefix();
            groupedRegions.computeIfAbsent(prefix, k -> new ArrayList<>()).add(region);
        }
        // Sort each group
        groupedRegions.values().forEach(Collections::sort);
        return groupedRegions;
    }

    /**
     * Returns the color.
     *
     * @param colorRgb  the color RGB.
     * @param contourID the contour ID.
     * @return the color.
     */
    public static Color getColor(int[] colorRgb, int contourID) {
        return getColor(colorRgb, contourID, DEFAULT_OPACITY);
    }

    /**
     * Returns the color.
     *
     * @param colorRgb  the color RGB.
     * @param contourID the contour ID.
     * @param opacity   the opacity.
     * @return the color.
     */
    public static Color getColor(int[] colorRgb, int contourID, float opacity) {
        int alphaValue = Math.round(clampOpacity(opacity) * 255f);

        if (isValidColorArray(colorRgb)) {
            return new Color(colorRgb[0], colorRgb[1], colorRgb[2], alphaValue);
        }

        return generateColorFromLut(contourID, alphaValue);
    }

    /**
     * Executes the clamp opacity operation.
     *
     * @param opacity the opacity.
     * @return the operation result.
     */
    private static float clampOpacity(float opacity) {
        return Math.max(MIN_OPACITY, Math.min(MAX_OPACITY, opacity));
    }

    /**
     * Checks whether the valid color array condition is true.
     *
     * @param colorRgb the color RGB.
     * @return true if the valid color array condition is true; otherwise false.
     */
    private static boolean isValidColorArray(int[] colorRgb) {
        return colorRgb != null && colorRgb.length >= 3;
    }

    /**
     * Generates the color from LUT.
     *
     * @param contourID  the contour ID.
     * @param alphaValue the alpha value.
     * @return the operation result.
     */
    private static Color generateColorFromLut(int contourID, int alphaValue) {
        byte[][] lut = ColorLut.MULTICOLOR.getByteLut().lutTable();
        int lutIndex = Math.abs(contourID) % lut[0].length;

        return new Color(Byte.toUnsignedInt(lut[0][lutIndex]), Byte.toUnsignedInt(lut[1][lutIndex]),
                Byte.toUnsignedInt(lut[2][lutIndex]), alphaValue);
    }

    /**
     * Executes the compare to operation.
     *
     * @param other the other.
     * @return the operation result.
     */
    @Override
    public int compareTo(RegionAttributes other) {
        if (other == null) {
            return 1;
        }
        return COLLATOR.compare(this.label, other.label);
    }

    /**
     * Executes the equals operation.
     *
     * @param obj the obj.
     * @return true if the equals condition is true; otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        return this == obj
                || (obj instanceof RegionAttributes other && id == other.id && Objects.equals(label, other.label));
    }

    /**
     * Checks whether the hash code condition is true.
     *
     * @return true if the hash code condition is true; otherwise false.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, label);
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return "RegionAttributes{id=%d, label='%s', visible=%s, pixels=%d}"
                .formatted(id, label, visible, numberOfPixels);
    }

}
