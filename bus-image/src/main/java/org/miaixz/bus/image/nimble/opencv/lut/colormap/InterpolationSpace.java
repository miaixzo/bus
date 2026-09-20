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

/**
 * Color space in which two stops are blended; alpha is never blended here. *
 *
 * @author Kimi Liu
 */
public enum InterpolationSpace {

    /**
     * Component-wise blend of sRGB values; cheapest, may dip through gray.
     */
    RGB,
    /**
     * Blend of hue (shortest arc), saturation and brightness; keeps ramps vivid.
     */
    HSL,
    /**
     * Blend in CIE L*a*b*; perceptually even ramps.
     */
    LAB;

    private static final double D65_X = 0.950456;
    private static final double D65_Y = 1.0;
    private static final double D65_Z = 1.088754;
    private static final double LAB_THRESHOLD = 8.85645167903563082e-3;
    private static final double LAB_INVERSE_THRESHOLD = 0.206896551724137931;
    private static final double LAB_ALPHA = 841.0 / 108.0;
    private static final double LAB_BETA = 4.0 / 29.0;
    private static final double GAMMA_THRESHOLD = 0.0031306684425005883;
    private static final double GAMMA_INVERSE_THRESHOLD = 0.0404482362771076;

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static Rgba mixHsb(Rgba from, Rgba to, float t) {
        float[] a = toHsb(from);
        float[] b = toHsb(to);
        // A gray has no hue: borrow the other end's so the ramp does not sweep the wheel.
        if (a[1] == 0f) {
            a[0] = b[0];
        } else if (b[1] == 0f) {
            b[0] = a[0];
        }
        float dh = b[0] - a[0];
        if (dh > 0.5f) {
            dh -= 1f;
        } else if (dh < -0.5f) {
            dh += 1f;
        }
        float h = a[0] + dh * t;
        h -= (float) Math.floor(h);
        return fromHsb(h, lerp(a[1], b[1], t), lerp(a[2], b[2], t));
    }

    private static float[] toHsb(Rgba c) {
        float max = Math.max(c.red(), Math.max(c.green(), c.blue()));
        float min = Math.min(c.red(), Math.min(c.green(), c.blue()));
        float delta = max - min;
        float s = max == 0f ? 0f : delta / max;
        float h = 0f;
        if (delta > 0f) {
            if (max == c.red()) {
                h = (c.green() - c.blue()) / delta;
            } else if (max == c.green()) {
                h = 2f + (c.blue() - c.red()) / delta;
            } else {
                h = 4f + (c.red() - c.green()) / delta;
            }
            h /= 6f;
            if (h < 0f) {
                h += 1f;
            }
        }
        return new float[] { h, s, max };
    }

    private static Rgba fromHsb(float h, float s, float v) {
        float sector = (h - (float) Math.floor(h)) * 6f;
        int i = (int) sector;
        float f = sector - i;
        float p = v * (1f - s);
        float q = v * (1f - s * f);
        float u = v * (1f - s * (1f - f));
        switch (i) {
            case 0:
                return new Rgba(v, u, p, 1f);

            case 1:
                return new Rgba(q, v, p, 1f);

            case 2:
                return new Rgba(p, v, u, 1f);

            case 3:
                return new Rgba(p, q, v, 1f);

            case 4:
                return new Rgba(u, p, v, 1f);

            default:
                return new Rgba(v, p, q, 1f);
        }
    }

    private static Rgba mixLab(Rgba from, Rgba to, float t) {
        double[] a = rgbToLab(from.red(), from.green(), from.blue());
        double[] b = rgbToLab(to.red(), to.green(), to.blue());
        double[] rgb = labToRgb(lerp(a[0], b[0], t), lerp(a[1], b[1], t), lerp(a[2], b[2], t));
        return new Rgba((float) rgb[0], (float) rgb[1], (float) rgb[2], 1f);
    }

    private static double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }

    /**
     * sRGB components in {@code [0, 1]} to CIE L*a*b* (D65), as {@code {L, a, b}} with L in {@code
     * [0, 100]}.
     */
    public static double[] rgbToLab(double r, double g, double b) {
        double lr = inverseGamma(r);
        double lg = inverseGamma(g);
        double lb = inverseGamma(b);
        double x = (0.4123955889674142161 * lr + 0.3575834307637148171 * lg + 0.1804926473817015735 * lb);
        double y = (0.2125862307855955516 * lr + 0.7151703037034108499 * lg + 0.07220049864333622685 * lb);
        double z = (0.01929721549174694484 * lr + 0.1191838645808485318 * lg + 0.9504971251315797660 * lb);
        double fx = labF(x / D65_X);
        double fy = labF(y / D65_Y);
        double fz = labF(z / D65_Z);
        return new double[] { 116.0 * fy - 16.0, 500.0 * (fx - fy), 200.0 * (fy - fz) };
    }

    /**
     * CIE L*a*b* (D65) to sRGB components in {@code [0, 1]}. An out-of-gamut color is shifted by its most negative
     * channel before gamma so its hue survives, then clamped.
     */
    public static double[] labToRgb(double l, double a, double b) {
        double fy = (l + 16.0) / 116.0;
        double fx = fy + a / 500.0;
        double fz = fy - b / 200.0;
        double x = D65_X * labInverse(fx);
        double y = D65_Y * labInverse(fy);
        double z = D65_Z * labInverse(fz);
        double r = 3.2406 * x - 1.5372 * y - 0.4986 * z;
        double g = -0.9689 * x + 1.8758 * y + 0.0415 * z;
        double bl = 0.0557 * x - 0.2040 * y + 1.0570 * z;
        double min = Math.min(r, Math.min(g, bl));
        if (min < 0) {
            r -= min;
            g -= min;
            bl -= min;
        }
        return new double[] { clamp01(gamma(r)), clamp01(gamma(g)), clamp01(gamma(bl)) };
    }

    private static double labF(double t) {
        return t > LAB_THRESHOLD ? Math.cbrt(t) : LAB_ALPHA * t + LAB_BETA;
    }

    private static double labInverse(double t) {
        return t > LAB_INVERSE_THRESHOLD ? t * t * t : (t - LAB_BETA) / LAB_ALPHA;
    }

    private static double gamma(double value) {
        return value <= GAMMA_THRESHOLD ? 12.92 * value : 1.055 * Math.pow(value, 1.0 / 2.4) - 0.055;
    }

    private static double inverseGamma(double value) {
        return value <= GAMMA_INVERSE_THRESHOLD ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /**
     * Color at fraction {@code t} between {@code from} (0) and {@code to} (1), opaque.
     */
    public Rgba mix(Rgba from, Rgba to, double t) {
        float f = (float) Math.max(0.0, Math.min(1.0, t));
        switch (this) {
            case HSL:
                return mixHsb(from, to, f);

            case LAB:
                return mixLab(from, to, f);

            default:
                return new Rgba(lerp(from.red(), to.red(), f), lerp(from.green(), to.green(), f),
                        lerp(from.blue(), to.blue(), f), 1f);
        }
    }
}
