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

/**
 * Defines the YBR values.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum YBR {

    /**
     * The full value.
     */
    FULL {

        @Override
        public float[] toRGB(float[] ybr) {
            return convert(ybr, FROM_YBR_FULL);
        }

        @Override
        public float[] fromRGB(float[] rgb) {
            return convert(rgb, TO_YBR_FULL);
        }
    },
    /**
     * The partial value.
     */
    PARTIAL {

        @Override
        public float[] toRGB(float[] ybr) {
            return convert(ybr, FROM_YBR_PARTIAL);
        }

        @Override
        public float[] fromRGB(float[] rgb) {
            return convert(rgb, TO_YBR_PARTIAL);
        }
    };

    /**
     * The from ybr full value.
     */
    private static final double[] FROM_YBR_FULL = { 1.0, -3.681999032610751E-5, 1.4019875769352639, -0.7009753784724688,
            1.0, -0.34411328131331737, -0.7141038211151132, 0.5291085512142153, 1.0, 1.7719781167370596,
            -1.345834129159976E-4, -0.8859217666620718, };

    /**
     * The from ybr partial value.
     */
    private static final double[] FROM_YBR_PARTIAL = { 1.1644154634373545, -9.503599204778129E-5, 1.5960018776303868,
            -0.8707293872840042, 1.1644154634373545, -0.39172456367367336, -0.8130133682767554, 0.5295929995103797,
            1.1644154634373545, 2.017290682233469, -1.3527300480981362E-4, -1.0813536710791642, };

    /**
     * The to ybr full value.
     */
    private static final double[] TO_YBR_FULL = { 0.2990, 0.5870, 0.1140, 0.0, -0.1687, -0.3313, 0.5, 0.5, 0.5, -0.4187,
            -0.0813, 0.5 };

    /**
     * The to ybr partial value.
     */
    private static final double[] TO_YBR_PARTIAL = { 0.2568, 0.5041, 0.0979, 0.0625, -0.1482, -0.2910, 0.4392, 0.5,
            0.4392, -0.3678, -0.0714, 0.5 };

    /**
     * Executes the convert operation.
     *
     * @param in the in.
     * @param a  the a.
     * @return the operation result.
     */
    private static float[] convert(float[] in, double[] a) {
        return new float[] { (float) Math.max(0.0, Math.min(1.0, a[0] * in[0] + a[1] * in[1] + a[2] * in[2] + a[3])),
                (float) Math.max(0.0, Math.min(1.0, a[4] * in[0] + a[5] * in[1] + a[6] * in[2] + a[7])),
                (float) Math.max(0.0, Math.min(1.0, a[8] * in[0] + a[9] * in[1] + a[10] * in[2] + a[11])) };
    }

    /**
     * Converts this value to rgb.
     *
     * @param ybr the ybr.
     * @return the operation result.
     */
    public abstract float[] toRGB(float[] ybr);

    /**
     * Executes the from rgb operation.
     *
     * @param rgb the rgb.
     * @return the operation result.
     */
    public abstract float[] fromRGB(float[] rgb);

}
