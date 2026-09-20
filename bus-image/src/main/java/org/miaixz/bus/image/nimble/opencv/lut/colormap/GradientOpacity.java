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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Opacity factor as a function of the normalized gradient magnitude of the volume, {@code 0} in homogeneous regions and
 * {@code 1} across a full-range edge. Multiplies the alpha of a volume preset, so surfaces show and uniform tissue
 * fades: the classic edge emphasis of volume rendering.
 *
 * @param points sorted by magnitude; a magnitude below the first or above the last point clamps
 * @author Kimi Liu
 */
public record GradientOpacity(List<Point> points) {

    /**
     * Magnitude above which a two-point emphasis curve is fully opaque.
     */
    public static final double EDGE_MAGNITUDE = 0.3;

    public GradientOpacity {
        Objects.requireNonNull(points, "Points cannot be null");
        if (points.isEmpty()) {
            throw new IllegalArgumentException("At least one point is needed");
        }
        var sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingDouble(Point::magnitude));
        points = List.copyOf(sorted);
    }

    /**
     * Two-point curve for an emphasis in {@code [0, 1]}: homogeneous regions keep {@code 1 -
     * emphasis} of their opacity, edges keep all of it.
     */
    public static GradientOpacity edgeEmphasis(float emphasis) {
        float kept = 1f - Math.max(0f, Math.min(1f, emphasis));
        return new GradientOpacity(List.of(new Point(0.0, kept), new Point(EDGE_MAGNITUDE, 1f)));
    }

    /**
     * The emphasis a two-point curve was built with: what homogeneous regions lose.
     */
    public float emphasis() {
        return 1f - points.get(0).factor();
    }

    public float factorAt(double magnitude) {
        Point first = points.get(0);
        if (magnitude <= first.magnitude()) {
            return first.factor();
        }
        for (int i = 1; i < points.size(); i++) {
            Point p = points.get(i);
            if (magnitude <= p.magnitude()) {
                Point q = points.get(i - 1);
                double span = p.magnitude() - q.magnitude();
                float t = span <= 0 ? 1f : (float) ((magnitude - q.magnitude()) / span);
                return q.factor() + (p.factor() - q.factor()) * t;
            }
        }
        return points.get(points.size() - 1).factor();
    }

    /**
     * {@code n} factors sampled evenly over the magnitude range, for a shader uniform.
     */
    public float[] table(int n) {
        if (n < 2) {
            throw new IllegalArgumentException("At least 2 samples are needed: " + n);
        }
        float[] table = new float[n];
        for (int i = 0; i < n; i++) {
            table[i] = factorAt((double) i / (n - 1));
        }
        return table;
    }

    /**
     * Defines one opacity factor control point.
     *
     * @param magnitude the normalized gradient magnitude.
     * @param factor    the opacity factor.
     * @author Kimi Liu
     */
    public record Point(double magnitude, float factor) {

        public Point {
            if (!(magnitude >= 0.0 && magnitude <= 1.0)) {
                throw new IllegalArgumentException("Magnitude must be in [0, 1]: " + magnitude);
            }
            if (Float.isNaN(factor) || factor < 0f || factor > 1f) {
                throw new IllegalArgumentException("Factor must be in [0, 1]: " + factor);
            }
        }
    }
}
