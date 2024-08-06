/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;

import org.miaixz.bus.image.galaxy.data.Attributes;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ColorModelFactory {

    public static ColorModel createMonochromeColorModel(int bits, int dataType) {
        return new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] { bits }, false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE, dataType);
    }

    public static ColorModel createPaletteColorModel(int bits, int dataType, ColorSpace cspace, Attributes ds) {
        return new PaletteColorModel(bits, dataType, cspace, ds);
    }

    public static ColorModel createRGBColorModel(int bits, int dataType, ColorSpace cspace) {
        return new ComponentColorModel(cspace, new int[] { bits, bits, bits }, false, false, Transparency.OPAQUE,
                dataType);
    }

    public static ColorModel createYBRFullColorModel(int bits, int dataType, ColorSpace cspace) {
        return new ComponentColorModel(cspace, new int[] { bits, bits, bits }, false, false, Transparency.OPAQUE,
                dataType);
    }

    public static ColorModel createYBRColorModel(int bits, int dataType, ColorSpace cspace,
            ColorSubsampling subsampling) {
        return new SampledColorModel(cspace, subsampling);
    }

}
