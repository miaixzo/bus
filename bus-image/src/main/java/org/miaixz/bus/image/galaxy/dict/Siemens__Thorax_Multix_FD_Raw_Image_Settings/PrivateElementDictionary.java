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
package org.miaixz.bus.image.galaxy.dict.Siemens__Thorax_Multix_FD_Raw_Image_Settings;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

        case PrivateTag.GammaLUTParameter2:
        case PrivateTag._0025_xx36_:
        case PrivateTag._0025_xx37_:
            return VR.DS;
        case PrivateTag._0025_xx03_:
        case PrivateTag._0025_xx04_:
        case PrivateTag._0025_xx05_:
        case PrivateTag._0025_xx06_:
        case PrivateTag._0025_xx07_:
        case PrivateTag._0025_xx08_:
        case PrivateTag._0025_xx09_:
        case PrivateTag._0025_xx0A_:
        case PrivateTag.HarmonizationGain:
        case PrivateTag.EdgeEnhancementGain:
            return VR.FL;
        case PrivateTag._0025_xx17_:
        case PrivateTag.GammaLUTName:
            return VR.LO;
        case PrivateTag.InternalValue:
            return VR.LT;
        case PrivateTag.RawImageAmplification:
        case PrivateTag.GammaLUT:
        case PrivateTag.HarmonizationKernel:
        case PrivateTag.EdgeEnhancementKernel:
        case PrivateTag._0025_xx11_:
        case PrivateTag._0025_xx12_:
        case PrivateTag._0025_xx13_:
        case PrivateTag._0025_xx14_:
        case PrivateTag._0025_xx15_:
        case PrivateTag._0025_xx16_:
        case PrivateTag.GammaLUTParameter1:
        case PrivateTag.GammaLUTParameter3:
        case PrivateTag.GammaLUTParameter4:
            return VR.SS;
        case PrivateTag._0025_xx02_:
        case PrivateTag._0025_xx0B_:
        case PrivateTag.AutoGain:
        case PrivateTag.OrthoSubsampling:
        case PrivateTag.ImageCropUpperLeft:
        case PrivateTag.ImageCropUpperRight:
        case PrivateTag.ImageCropLowerLeft:
        case PrivateTag.ImageCropLowerRight:
        case PrivateTag.ManualCropping:
            return VR.US;
        }
        return VR.UN;
    }
}
