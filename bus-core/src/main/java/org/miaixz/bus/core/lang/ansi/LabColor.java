/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang.ansi;

import java.awt.*;
import java.awt.color.ColorSpace;

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents a color stored in LAB format.
 * <ul>
 * <li>L: Luminance (lightness)</li>
 * <li>a: Positive values indicate red, negative values indicate green</li>
 * <li>b: Positive values indicate yellow, negative values indicate blue</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LabColor {

    /**
     * The CIE XYZ color space instance.
     */
    private static final ColorSpace XYZ_COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    /**
     * L: Luminance (lightness) component of the LAB color.
     */
    private final double l;
    /**
     * A: Green-red component of the LAB color. Positive values indicate red, negative values indicate green.
     */
    private final double a;
    /**
     * B: Blue-yellow component of the LAB color. Positive values indicate yellow, negative values indicate blue.
     */
    private final double b;

    /**
     * Constructs a {@code LabColor} from an RGB integer value.
     *
     * @param rgb The RGB color as an integer.
     */
    public LabColor(final Integer rgb) {
        this((rgb != null) ? new Color(rgb) : null);
    }

    /**
     * Constructs a {@code LabColor} from a {@link Color} object..
     *
     * @param color The {@link Color} object. Must not be null.
     * @throws IllegalArgumentException if the provided color is null.
     */
    public LabColor(final Color color) {
        Assert.notNull(color, "Color must not be null");
        final float[] lab = fromXyz(color.getColorComponents(XYZ_COLOR_SPACE, null));
        this.l = lab[0];
        this.a = lab[1];
        this.b = lab[2];
    }

    /**
     * Converts XYZ color components to LAB color components. The conversion formula is based on the following: L = 116
     * * f(y) - 16 a = 500 * [f(x/Xn) - f(y/Yn)] b = 200 * [f(y/Yn) - f(z/Zn)] Where Xn, Yn, Zn are the CIE XYZ
     * tristimulus values of the reference white point (D65 illuminant is often used). In this implementation, Xn, Yn,
     * Zn are implicitly handled by the constants used in the f() function.
     *
     * @param x The X component of the XYZ color.
     * @param y The Y component of the XYZ color.
     * @param z The Z component of the XYZ color.
     * @return A float array containing the L, a, and b components of the LAB color.
     */
    private static float[] fromXyz(final float x, final float y, final float z) {
        // Assuming reference white point values are normalized, e.g., Xn=0.95047, Yn=1.0, Zn=1.08883 for D65
        // The constants 0.982 and 1.183 in the original comment suggest a specific white point,
        // but the code uses a simplified f(t) without explicit division by Xn, Yn, Zn.
        final double l = (f(y) - 16.0) * 116.0;
        final double a = (f(x) - f(y)) * 500.0;
        final double b = (f(y) - f(z)) * 200.0;
        return new float[] { (float) l, (float) a, (float) b };
    }

    /**
     * Helper function for XYZ to LAB conversion. Applies a cubic root transformation for values above a certain
     * threshold, and a linear transformation for values below it.
     *
     * @param t The input value (X, Y, or Z component).
     * @return The transformed value.
     */
    private static double f(final double t) {
        // The threshold 216.0 / 24389.0 is (6/29)^3
        // The linear part (1.0 / 3.0) * Math.pow(29.0 / 6.0, 2) * t + (4.0 / 29.0) is for values below the threshold.
        return (t > (216.0 / 24389.0)) ? Math.cbrt(t) : (1.0 / 3.0) * Math.pow(29.0 / 6.0, 2) * t + (4.0 / 29.0);
    }

    /**
     * Calculates the color difference (distance) between this LAB color and another LAB color using the CIE94 color
     * difference formula.
     *
     * @param other The other {@code LabColor} to compare with.
     * @return The color difference (distance) between the two colors.
     * @see <a href="https://en.wikipedia.org/wiki/Color_difference#CIE94">CIE94 Color Difference</a>
     */
    public double getDistance(final LabColor other) {
        final double c1 = Math.sqrt(this.a * this.a + this.b * this.b);
        final double deltaC = c1 - Math.sqrt(other.a * other.a + other.b * other.b);
        final double deltaA = this.a - other.a;
        final double deltaB = this.b - other.b;
        final double deltaH = Math.sqrt(Math.max(0.0, deltaA * deltaA + deltaB * deltaB - deltaC * deltaC));
        return Math.sqrt(
                Math.max(
                        0.0,
                        Math.pow((this.l - other.l), 2) + Math.pow(deltaC / (1 + 0.045 * c1), 2)
                                + Math.pow(deltaH / (1 + 0.015 * c1), 2.0)));
    }

    /**
     * Converts an array of XYZ color components to LAB color components.
     *
     * @param xyz A float array containing the X, Y, and Z components of the XYZ color.
     * @return A float array containing the L, a, and b components of the LAB color.
     */
    private float[] fromXyz(final float[] xyz) {
        return fromXyz(xyz[0], xyz[1], xyz[2]);
    }

}
