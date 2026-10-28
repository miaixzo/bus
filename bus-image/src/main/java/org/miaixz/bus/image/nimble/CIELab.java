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

import java.awt.Color;
import java.util.Objects;

import org.miaixz.bus.image.nimble.opencv.lut.colormap.InterpolationSpace;

/**
 * Represents the CIELab type.
 *
 * @author Kimi Liu
 */
public class CIELab {

    /**
     * Creates a color conversion helper instance for compatibility with existing callers.
     */
    public CIELab() {
    }

    /**
     * Converts DICOM encoded L*a*b* values to RGB color space.
     *
     * @param lab integer array of 3 DICOM encoded L*a*b* values, must not be null and length 3
     * @return RGB values as int array [r, g, b] in range [0, 255], or empty array if input is invalid
     * @see <a href=
     *      "http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.10.7.html#sect_C.10.7.1.1">DICOM
     *      L*a*b* Encoding</a>
     */
    public static int[] dicomLab2rgb(int[] lab) {
        if (lab == null || lab.length != 3) {
            return new int[0];
        }
        // Convert DICOM encoding to normalized L*a*b* values
        double l = lab[0] * 100.0 / 65535.0;
        double a = lab[1] * 255.0 / 65535.0 - DICOM_AB_OFFSET;
        double b = lab[2] * 255.0 / 65535.0 - DICOM_AB_OFFSET;

        double[] rgb = InterpolationSpace.labToRgb(l, a, b);
        return new int[] { (int) Math.round(Math.clamp(rgb[0], 0.0, 1.0) * RGB_MAX_VALUE),
                (int) Math.round(Math.clamp(rgb[1], 0.0, 1.0) * RGB_MAX_VALUE),
                (int) Math.round(Math.clamp(rgb[2], 0.0, 1.0) * RGB_MAX_VALUE) };
    }

    /**
     * Converts RGB color to DICOM encoded L*a*b* values.
     *
     * @param color RGB color, must not be null
     * @return DICOM encoded L*a*b* values as int array [l, a, b]
     * @throws NullPointerException if color is null
     * @see <a href=
     *      "http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.10.7.html#sect_C.10.7.1.1">DICOM
     *      L*a*b* Encoding</a>
     */
    public static int[] rgbToDicomLab(Color color) {
        Objects.requireNonNull(color, "Color cannot be null");
        return rgbToDicomLab(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Converts RGB values to DICOM encoded L*a*b* values.
     *
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @return DICOM encoded L*a*b* values as int array [l, a, b]
     * @see <a href=
     *      "http://dicom.nema.org/medical/dicom/current/output/chtml/part03/sect_C.10.7.html#sect_C.10.7.1.1">DICOM
     *      L*a*b* Encoding</a>
     */
    public static int[] rgbToDicomLab(int r, int g, int b) {
        double[] lab = InterpolationSpace.rgbToLab(r / RGB_MAX_VALUE, g / RGB_MAX_VALUE, b / RGB_MAX_VALUE);

        return new int[] { Math.clamp((int) Math.round(lab[0] * DICOM_L_SCALE), RGB_MIN, DICOM_MAX),
                Math.clamp((int) Math.round((lab[1] + DICOM_AB_OFFSET) * DICOM_AB_SCALE), RGB_MIN, DICOM_MAX),
                Math.clamp((int) Math.round((lab[2] + DICOM_AB_OFFSET) * DICOM_AB_SCALE), RGB_MIN, DICOM_MAX) };
    }

    /**
     * Scale converting CIELAB lightness to the unsigned 16-bit DICOM encoding.
     */
    private static final double DICOM_L_SCALE = 65535.0 / 100.0;

    /**
     * Scale converting CIELAB chroma components to the unsigned 16-bit DICOM encoding.
     */
    private static final double DICOM_AB_SCALE = 65535.0 / 255.0;

    /**
     * Offset converting signed CIELAB chroma components to unsigned DICOM values.
     */
    private static final double DICOM_AB_OFFSET = 128.0;

    /**
     * Maximum intensity of an 8-bit RGB component.
     */
    private static final double RGB_MAX_VALUE = 255.0;

    /**
     * Minimum encoded RGB or DICOM color component.
     */
    private static final int RGB_MIN = 0;

    /**
     * Maximum unsigned 16-bit DICOM color component.
     */
    private static final int DICOM_MAX = 65535;

}
