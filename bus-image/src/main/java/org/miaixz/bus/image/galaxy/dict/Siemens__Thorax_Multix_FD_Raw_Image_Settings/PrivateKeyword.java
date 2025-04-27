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
package org.miaixz.bus.image.galaxy.dict.Siemens__Thorax_Multix_FD_Raw_Image_Settings;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.RawImageAmplification:
            return "RawImageAmplification";
        case PrivateTag.GammaLUT:
            return "GammaLUT";
        case PrivateTag._0025_xx02_:
            return "_0025_xx02_";
        case PrivateTag._0025_xx03_:
            return "_0025_xx03_";
        case PrivateTag._0025_xx04_:
            return "_0025_xx04_";
        case PrivateTag._0025_xx05_:
            return "_0025_xx05_";
        case PrivateTag._0025_xx06_:
            return "_0025_xx06_";
        case PrivateTag._0025_xx07_:
            return "_0025_xx07_";
        case PrivateTag._0025_xx08_:
            return "_0025_xx08_";
        case PrivateTag._0025_xx09_:
            return "_0025_xx09_";
        case PrivateTag._0025_xx0A_:
            return "_0025_xx0A_";
        case PrivateTag._0025_xx0B_:
            return "_0025_xx0B_";
        case PrivateTag.HarmonizationKernel:
            return "HarmonizationKernel";
        case PrivateTag.HarmonizationGain:
            return "HarmonizationGain";
        case PrivateTag.EdgeEnhancementKernel:
            return "EdgeEnhancementKernel";
        case PrivateTag.EdgeEnhancementGain:
            return "EdgeEnhancementGain";
        case PrivateTag.InternalValue:
            return "InternalValue";
        case PrivateTag._0025_xx11_:
            return "_0025_xx11_";
        case PrivateTag._0025_xx12_:
            return "_0025_xx12_";
        case PrivateTag._0025_xx13_:
            return "_0025_xx13_";
        case PrivateTag._0025_xx14_:
            return "_0025_xx14_";
        case PrivateTag._0025_xx15_:
            return "_0025_xx15_";
        case PrivateTag._0025_xx16_:
            return "_0025_xx16_";
        case PrivateTag._0025_xx17_:
            return "_0025_xx17_";
        case PrivateTag.AutoGain:
            return "AutoGain";
        case PrivateTag.OrthoSubsampling:
            return "OrthoSubsampling";
        case PrivateTag.ImageCropUpperLeft:
            return "ImageCropUpperLeft";
        case PrivateTag.ImageCropUpperRight:
            return "ImageCropUpperRight";
        case PrivateTag.ImageCropLowerLeft:
            return "ImageCropLowerLeft";
        case PrivateTag.ImageCropLowerRight:
            return "ImageCropLowerRight";
        case PrivateTag.ManualCropping:
            return "ManualCropping";
        case PrivateTag.GammaLUTParameter1:
            return "GammaLUTParameter1";
        case PrivateTag.GammaLUTParameter2:
            return "GammaLUTParameter2";
        case PrivateTag.GammaLUTParameter3:
            return "GammaLUTParameter3";
        case PrivateTag.GammaLUTParameter4:
            return "GammaLUTParameter4";
        case PrivateTag.GammaLUTName:
            return "GammaLUTName";
        case PrivateTag._0025_xx36_:
            return "_0025_xx36_";
        case PrivateTag._0025_xx37_:
            return "_0025_xx37_";
        }
        return "";
    }

}
