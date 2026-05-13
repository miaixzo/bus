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
package org.miaixz.bus.image.nimble;

import java.awt.color.ColorSpace;

/**
 * Represents the CIELabColorSpace type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CIELabColorSpace extends ColorSpace {

    /**
     * The d value.
     */
    private static final double D = 4.0 / 29.0;

    /**
     * The delta value.
     */
    private static final double DELTA = 6.0 / 29.0;

    /**
     * The delta 3 value.
     */
    private static final double DELTA_3 = 216.0 / 24389.0;

    /**
     * The three delta 2 value.
     */
    private static final double THREE_DELTA_2 = 108.0 / 841.0;

    /**
     * The ciexyz value.
     */
    private final ColorSpace CIEXYZ;

    /**
     * Creates a new instance.
     *
     * @param ciexyz the ciexyz.
     */
    CIELabColorSpace(ColorSpace ciexyz) {
        super(ColorSpace.TYPE_Lab, 3);
        CIEXYZ = ciexyz;
    }

    /**
     * Gets the instance.
     *
     * @return the instance.
     */
    public static CIELabColorSpace getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Executes the f operation.
     *
     * @param t the t.
     * @return the operation result.
     */
    private static double f(double t) {
        return t > DELTA_3 ? Math.cbrt(t) : t / THREE_DELTA_2 + D;
    }

    /**
     * Executes the f inv operation.
     *
     * @param t the t.
     * @return the operation result.
     */
    private static double fInv(double t) {
        return t > DELTA ? t * t * t : THREE_DELTA_2 * (t - D);
    }

    /**
     * Gets the max value.
     *
     * @param component the component.
     * @return the max value.
     */
    @Override
    public float getMaxValue(int component) {
        if ((component < 0) || (component > 2)) {
            throw new IllegalArgumentException("Component index out of range: " + component);
        }
        return component == 0 ? 100.0f : 127.0f;
    }

    /**
     * Gets the min value.
     *
     * @param component the component.
     * @return the min value.
     */
    @Override
    public float getMinValue(int component) {
        if ((component < 0) || (component > 2)) {
            throw new IllegalArgumentException("Component index out of range: " + component);
        }
        return component == 0 ? 0.0f : -128.0f;
    }

    /**
     * Executes the from ciexyz operation.
     *
     * @param colorvalue the colorvalue.
     * @return the operation result.
     */
    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        double l = f(colorvalue[1]);
        double L = 116.0 * l - 16.0;
        double a = 500.0 * (f(colorvalue[0]) - l);
        double b = 200.0 * (l - f(colorvalue[2]));
        return new float[] { (float) L, (float) a, (float) b };
    }

    /**
     * Executes the from rgb operation.
     *
     * @param rgbvalue the rgbvalue.
     * @return the operation result.
     */
    @Override
    public float[] fromRGB(float[] rgbvalue) {
        float[] xyz = CIEXYZ.fromRGB(rgbvalue);
        return fromCIEXYZ(xyz);
    }

    /**
     * Converts this value to ciexyz.
     *
     * @param colorvalue the colorvalue.
     * @return the operation result.
     */
    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        double l = (colorvalue[0] + 16.0) / 116.0;
        double X = fInv(l + colorvalue[1] / 500.0);
        double Y = fInv(l);
        double Z = fInv(l - colorvalue[2] / 200.0);
        return new float[] { (float) X, (float) Y, (float) Z };
    }

    /**
     * Converts this value to rgb.
     *
     * @param colorvalue the colorvalue.
     * @return the operation result.
     */
    @Override
    public float[] toRGB(float[] colorvalue) {
        float[] xyz = toCIEXYZ(colorvalue);
        return CIEXYZ.toRGB(xyz);
    }

    /**
     * Reads the resolve.
     *
     * @return the operation result.
     */
    private Object readResolve() {
        return getInstance();
    }

    /**
     * Represents the LazyHolder type.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class LazyHolder {

        /**
         * The instance value.
         */
        static final CIELabColorSpace INSTANCE = new CIELabColorSpace(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ));

    }

}
