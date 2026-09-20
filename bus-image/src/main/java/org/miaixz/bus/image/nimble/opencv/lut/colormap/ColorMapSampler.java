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

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates a {@link ColorMap} at domain values. Builds the color, alpha and material curves once, so create one
 * sampler per map and reuse it when compiling tables.
 *
 * @author Kimi Liu
 */
public final class ColorMapSampler {

    private final ColorMap map;
    private final boolean step;
    private final boolean cyclic;
    private final double first;
    private final double last;
    private final double[] colorPositions;
    private final Rgba[] colors;
    private final double[] alphaPositions;
    private final float[] alphas;
    private final double[] materialPositions;
    private final Material[] materials;

    ColorMapSampler(ColorMap map) {
        this.map = map;
        this.step = map.interpolation().isStep();
        this.cyclic = map.type() == ColorMapType.CYCLIC;
        this.first = map.firstPosition();
        this.last = map.lastPosition();

        var colorStops = new ArrayList<ColorStop>();
        var alphaStops = new ArrayList<ColorStop>();
        var materialStops = new ArrayList<ColorStop>();
        for (ColorStop stop : map.stops()) {
            if (stop.hasColor()) {
                colorStops.add(stop);
            }
            if (stop.hasAlpha()) {
                alphaStops.add(stop);
            }
            if (stop.hasMaterial()) {
                materialStops.add(stop);
            }
        }
        this.colorPositions = positions(colorStops);
        this.colors = colorStops.stream().map(s -> Rgba.of(s.color())).toArray(Rgba[]::new);
        this.alphaPositions = positions(alphaStops);
        this.alphas = new float[alphaStops.size()];
        for (int i = 0; i < alphas.length; i++) {
            alphas[i] = alphaStops.get(i).alpha();
        }
        this.materialPositions = positions(materialStops);
        this.materials = materialStops.stream().map(ColorStop::material).toArray(Material[]::new);
    }

    private static double[] positions(List<ColorStop> stops) {
        return stops.stream().mapToDouble(ColorStop::position).toArray();
    }

    private static double fraction(double[] positions, int i, double v) {
        return (v - positions[i]) / (positions[i + 1] - positions[i]);
    }

    // Index of the last position <= v, so duplicates resolve to the later stop; -1 when v is below
    // all.
    private static int segment(double[] positions, double v) {
        int lo = 0;
        int hi = positions.length - 1;
        int found = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (positions[mid] <= v) {
                found = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return found;
    }

    public ColorMap map() {
        return map;
    }

    /**
     * * Color and alpha at a domain value; NaN and out-of-range values follow the map's outside colors.
     */
    public Rgba sample(double value) {
        if (Double.isNaN(value)) {
            Rgba nan = map.outside().nan();
            return nan != null ? nan : Rgba.TRANSPARENT;
        }
        if (cyclic) {
            double v = wrap(value);
            return colorAt(v).withAlpha(alphaAt(v));
        }
        if (value < first && map.outside().low() != null) {
            return map.outside().low();
        }
        if (value > last && map.outside().high() != null) {
            return map.outside().high();
        }
        return colorAt(value).withAlpha(alphaAt(value));
    }

    /**
     * Shading coefficients at a domain value; {@link Material#DEFAULT} when no stop defines any.
     */
    public Material material(double value) {
        if (materials.length == 0 || Double.isNaN(value)) {
            return Material.DEFAULT;
        }
        double v = cyclic ? wrap(value) : value;
        int i = segment(materialPositions, v);
        int n = materials.length;
        if (isAcrossSeam(i, n)) {
            return step ? materials[n - 1] : materials[n - 1].mix(materials[0], seam(materialPositions, v));
        }
        if (i < 0) {
            return materials[0];
        }
        if (step || i == n - 1) {
            return materials[i];
        }
        return materials[i].mix(materials[i + 1], fraction(materialPositions, i, v));
    }

    private double wrap(double value) {
        ColorMapDomain d = map.domain();
        double offset = (value - d.min()) % d.span();
        if (offset < 0) {
            offset += d.span();
        }
        return d.min() + offset;
    }

    private Rgba colorAt(double v) {
        if (colors.length == 0) {
            return Rgba.WHITE;
        }
        int i = segment(colorPositions, v);
        int n = colors.length;
        if (isAcrossSeam(i, n)) {
            return step ? colors[n - 1] : map.space().mix(colors[n - 1], colors[0], seam(colorPositions, v));
        }
        if (i < 0) {
            return colors[0];
        }
        if (step || i == n - 1) {
            return colors[i];
        }
        return map.space().mix(colors[i], colors[i + 1], fraction(colorPositions, i, v));
    }

    private float alphaAt(double v) {
        if (alphas.length == 0) {
            return 1f;
        }
        int i = segment(alphaPositions, v);
        int n = alphas.length;
        if (isAcrossSeam(i, n)) {
            float t = step ? 0f : (float) seam(alphaPositions, v);
            return alphas[n - 1] + (alphas[0] - alphas[n - 1]) * t;
        }
        if (i < 0) {
            return alphas[0];
        }
        if (step || i == n - 1) {
            return alphas[i];
        }
        float t = (float) fraction(alphaPositions, i, v);
        return alphas[i] + (alphas[i + 1] - alphas[i]) * t;
    }

    // A cyclic curve continues from its last stop to its first one across the domain end
    private boolean isAcrossSeam(int segmentIndex, int stopCount) {
        return cyclic && stopCount > 1 && (segmentIndex < 0 || segmentIndex == stopCount - 1);
    }

    private double seam(double[] positions, double v) {
        double span = map.domain().span();
        double lastPosition = positions[positions.length - 1];
        double gap = positions[0] + span - lastPosition;
        double offset = v >= lastPosition ? v - lastPosition : v + span - lastPosition;
        return gap > 0 ? offset / gap : 0.0;
    }
}
