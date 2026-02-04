/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.nimble.opencv.seg;

import java.awt.*;
import java.text.Collator;
import java.util.*;
import java.util.List;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.image.nimble.opencv.lut.ColorLut;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegionAttributes implements Comparable<RegionAttributes> {

    private final int id;
    protected long numberOfPixels;
    private String label;
    private String description;
    private String type;
    private Color color;
    private boolean filled;
    private float lineThickness;
    private boolean visible;
    private float interiorOpacity;

    public RegionAttributes(int id, String label) {
        this(id, label, null);
    }

    public RegionAttributes(int id, String label, Color color) {
        if (StringKit.hasText(label)) {
            setLabel(label);
        } else {
            throw new IllegalArgumentException("Label cannot be null or empty");
        }
        this.id = id;
        this.description = null;
        this.type = null;
        this.color = color;
        this.filled = true;
        this.lineThickness = 1.0f;
        this.visible = true;
        this.interiorOpacity = 1.0f;
        this.numberOfPixels = -1;
    }

    public static <E extends RegionAttributes> Map<String, List<E>> groupRegions(Collection<E> regions) {
        Map<String, List<E>> map = new HashMap<>();
        for (E region : regions) {
            String prefix = region.getPrefix();
            List<E> list = map.computeIfAbsent(prefix, l -> new ArrayList<>());
            int index = Collections.binarySearch(list, region, RegionAttributes::compareTo);
            if (index < 0) {
                list.add(-(index + 1), region);
            } else {
                list.add(index, region);
            }
        }
        return map;
    }

    public static Color getColor(int[] colorRgb, int contourID) {
        return getColor(colorRgb, contourID, 1.0f);
    }

    public static Color getColor(int[] colorRgb, int contourID, float opacity) {
        int opacityInt = (int) (opacity * 255f);
        Color rgbColor;
        if (colorRgb == null || colorRgb.length < 3) {
            byte[][] lut = ColorLut.MULTICOLOR.getByteLut().lutTable();
            int lutIndex = contourID % 256;
            rgbColor = new Color(lut[0][lutIndex] & 0xFF, lut[1][lutIndex] & 0xFF, lut[2][lutIndex] & 0xFF, opacityInt);
        } else {
            rgbColor = new Color(colorRgb[0], colorRgb[1], colorRgb[2], opacityInt);
        }
        return rgbColor;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public float getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(float lineThickness) {
        this.lineThickness = lineThickness;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getInteriorOpacity() {
        return interiorOpacity;
    }

    /**
     * @param interiorOpacity the opacity of the interior of the contour. The value is between 0.0 and 1.0.
     */
    public void setInteriorOpacity(float interiorOpacity) {
        this.interiorOpacity = Math.max(0.0f, Math.min(interiorOpacity, 1.0f));
    }

    public long getNumberOfPixels() {
        return numberOfPixels;
    }

    public String getPrefix() {
        return getLabel().split("[ _-]")[0];
    }

    public void addPixels(Region region) {
        if (region == null || region.getNumberOfPixels() < 0) {
            return;
        }
        if (numberOfPixels < 0) {
            resetPixelCount();
        }
        this.numberOfPixels += region.getNumberOfPixels();
    }

    public void resetPixelCount() {
        this.numberOfPixels = 0;
    }

    @Override
    public int compareTo(RegionAttributes o) {
        return Collator.getInstance(Locale.getDefault()).compare(getLabel(), o.getLabel());
    }

}
