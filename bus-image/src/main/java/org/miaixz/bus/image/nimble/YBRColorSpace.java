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
import java.io.Serial;

/**
 * Represents the YBRColorSpace type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class YBRColorSpace extends ColorSpace {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852287982516L;

    /**
     * The cs rgb value.
     */
    private final ColorSpace csRGB;

    /**
     * The ybr value.
     */
    private final YBR ybr;

    /**
     * Creates a new instance.
     *
     * @param csRGB the cs rgb.
     * @param ybr   the ybr.
     */
    public YBRColorSpace(ColorSpace csRGB, YBR ybr) {
        super(TYPE_YCbCr, 3);
        this.csRGB = csRGB;
        this.ybr = ybr;
    }

    /**
     * Converts this value to rgb.
     *
     * @param ybr the ybr.
     * @return the operation result.
     */
    @Override
    public float[] toRGB(float[] ybr) {
        return this.ybr.toRGB(ybr);
    }

    /**
     * Executes the from rgb operation.
     *
     * @param rgb the rgb.
     * @return the operation result.
     */
    @Override
    public float[] fromRGB(float[] rgb) {
        return this.ybr.fromRGB(rgb);
    }

    /**
     * Converts this value to ciexyz.
     *
     * @param colorvalue the colorvalue.
     * @return the operation result.
     */
    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        return csRGB.toCIEXYZ(toRGB(colorvalue));
    }

    /**
     * Executes the from ciexyz operation.
     *
     * @param xyzvalue the xyzvalue.
     * @return the operation result.
     */
    @Override
    public float[] fromCIEXYZ(float[] xyzvalue) {
        return fromRGB(csRGB.fromCIEXYZ(xyzvalue));
    }

}
