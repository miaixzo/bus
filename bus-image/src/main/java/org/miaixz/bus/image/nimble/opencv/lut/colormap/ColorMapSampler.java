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

    /**
     * Color map evaluated by this sampler.
     */
    private final ColorMap map;

    /**
     * Whether the map uses step interpolation.
     */
    private final boolean step;

    /**
     * Whether values wrap around the domain boundary.
     */
    private final boolean cyclic;

    /**
     * Position of the first stop.
     */
    private final double first;

    /**
     * Position of the last stop.
     */
    private final double last;

    /**
     * Ordered positions of stops that define colors.
     */
    private final double[] colorPositions;

    /**
     * Colors corresponding to {@link #colorPositions}.
     */
    private final Rgba[] colors;

    /**
     * Ordered positions of stops that define opacity.
     */
    private final double[] alphaPositions;

    /**
     * Opacity values corresponding to {@link #alphaPositions}.
     */
    private final float[] alphas;

    /**
     * Ordered positions of stops that define materials.
     */
    private final double[] materialPositions;

    /**
     * Materials corresponding to {@link #materialPositions}.
     */
    private final Material[] materials;

    /**
     * Creates a sampler and separates the map stops into color, opacity, and material curves.
     *
     * @param map color map to evaluate
     */
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

    /**
     * Extracts the positions from an ordered stop list.
     *
     * @param stops stops whose positions are required
     * @return stop positions in the same order
     */
    private static double[] positions(List<ColorStop> stops) {
        return stops.stream().mapToDouble(ColorStop::position).toArray();
    }

    /**
     * Computes the interpolation fraction within one adjacent-position interval.
     *
     * @param positions ordered positions
     * @param i         index of the interval's lower position
     * @param v         value being interpolated
     * @return relative fraction between positions {@code i} and {@code i + 1}
     */
    private static double fraction(double[] positions, int i, double v) {
        return (v - positions[i]) / (positions[i + 1] - positions[i]);
    }

    /**
     * Finds the last position not greater than a value, so duplicate positions resolve to the later stop.
     *
     * @param positions ordered positions to search
     * @param v         value to locate
     * @return matching segment index, or {@code -1} when the value precedes every position
     */
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

    /**
     * Returns the sampled color map.
     *
     * @return sampled color map
     */
    public ColorMap map() {
        return map;
    }

    /**
     * Returns the color and opacity at a domain value. NaN and out-of-range values follow the map's outside colors.
     *
     * @param value domain value to sample
     * @return sampled color and opacity
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
     * Returns shading coefficients at a domain value.
     *
     * @param value domain value to sample
     * @return sampled material, or {@link Material#DEFAULT} when no stop defines one
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

    /**
     * Wraps a value into the map domain.
     *
     * @param value value to wrap
     * @return equivalent value within the domain
     */
    private double wrap(double value) {
        ColorMapDomain d = map.domain();
        double offset = (value - d.min()) % d.span();
        if (offset < 0) {
            offset += d.span();
        }
        return d.min() + offset;
    }

    /**
     * Evaluates the color curve at an in-domain value.
     *
     * @param v domain value
     * @return interpolated or stepped color
     */
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

    /**
     * Evaluates the opacity curve at an in-domain value.
     *
     * @param v domain value
     * @return interpolated or stepped opacity
     */
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

    /**
     * Tests whether a segment crosses the cyclic boundary from the last stop to the first.
     *
     * @param segmentIndex current segment index
     * @param stopCount    number of stops in the curve
     * @return {@code true} when interpolation crosses the domain seam
     */
    private boolean isAcrossSeam(int segmentIndex, int stopCount) {
        return cyclic && stopCount > 1 && (segmentIndex < 0 || segmentIndex == stopCount - 1);
    }

    /**
     * Computes the interpolation fraction across the cyclic domain seam.
     *
     * @param positions positions for the sampled curve
     * @param v         wrapped domain value
     * @return interpolation fraction from the last stop to the first
     */
    private double seam(double[] positions, double v) {
        double span = map.domain().span();
        double lastPosition = positions[positions.length - 1];
        double gap = positions[0] + span - lastPosition;
        double offset = v >= lastPosition ? v - lastPosition : v + span - lastPosition;
        return gap > 0 ? offset / gap : 0.0;
    }
}
