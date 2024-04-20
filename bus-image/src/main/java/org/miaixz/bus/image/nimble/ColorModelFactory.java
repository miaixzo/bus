/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.nimble;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ColorModelFactory {

    public static ColorModel createMonochromeColorModel(int bits, int dataType) {
        return new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                new int[]{bits},
                false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE,
                dataType);
    }

    public static ColorModel createPaletteColorModel(int bits, int dataType,
                                                     Attributes ds) {
        return new PaletteColorModel(bits, dataType, createRGBColorSpace(ds), ds);
    }

    public static ColorModel createRGBColorModel(int bits, int dataType,
                                                 Attributes ds) {
        return new ComponentColorModel(
                createRGBColorSpace(ds),
                new int[]{bits, bits, bits},
                false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE,
                dataType);
    }


    public static ColorModel createYBRFullColorModel(int bits, int dataType,
                                                     Attributes ds) {
        return new ComponentColorModel(
                new YBRColorSpace(createRGBColorSpace(ds), YBR.FULL),
                new int[]{bits, bits, bits},
                false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE,
                dataType);
    }

    public static ColorModel createYBRColorModel(int bits,
                                                 int dataType,
                                                 Attributes ds,
                                                 YBR ybr,
                                                 ColorSubsampling subsampling) {
        return new SampledColorModel(new YBRColorSpace(createRGBColorSpace(ds), ybr), subsampling);
    }

    private static ColorSpace createRGBColorSpace(Attributes ds) {
        return createRGBColorSpace(ds.getSafeBytes(Tag.ICCProfile));
    }

    private static ColorSpace createRGBColorSpace(byte[] iccProfile) {
        if (null != iccProfile && iccProfile.length > 0)
            return new ICC_ColorSpace(ICC_Profile.getInstance(iccProfile));

        return ColorSpace.getInstance(ColorSpace.CS_sRGB);
    }

}
