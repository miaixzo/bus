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

    public static ColorModel createYBRColorModel(
            int bits,
            int dataType,
            ColorSpace cspace,
            ColorSubsampling subsampling) {
        return new SampledColorModel(cspace, subsampling);
    }

}
