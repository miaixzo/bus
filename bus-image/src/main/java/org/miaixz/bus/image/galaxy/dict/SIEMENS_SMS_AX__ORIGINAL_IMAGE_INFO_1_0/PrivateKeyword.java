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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__ORIGINAL_IMAGE_INFO_1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.ViewNative:
            return "ViewNative";
        case PrivateTag.OriginalSeriesNumber:
            return "OriginalSeriesNumber";
        case PrivateTag.OriginalImageNumber:
            return "OriginalImageNumber";
        case PrivateTag.WinCenter:
            return "WinCenter";
        case PrivateTag.WinWidth:
            return "WinWidth";
        case PrivateTag.WinBrightness:
            return "WinBrightness";
        case PrivateTag.WinContrast:
            return "WinContrast";
        case PrivateTag.OriginalFrameNumber:
            return "OriginalFrameNumber";
        case PrivateTag.OriginalMaskFrameNumber:
            return "OriginalMaskFrameNumber";
        case PrivateTag.Opac:
            return "Opac";
        case PrivateTag.OriginalNumberofFrames:
            return "OriginalNumberofFrames";
        case PrivateTag.OriginalSceneDuration:
            return "OriginalSceneDuration";
        case PrivateTag.IdentifierLOID:
            return "IdentifierLOID";
        case PrivateTag.OriginalSceneVFRInfo:
            return "OriginalSceneVFRInfo";
        case PrivateTag.OriginalFrameECGPosition:
            return "OriginalFrameECGPosition";
        case PrivateTag.OriginalECG1stFrameOffset:
            return "OriginalECG1stFrameOffset";
        case PrivateTag.ZoomFlag:
            return "ZoomFlag";
        case PrivateTag.FlexiblePixelShift:
            return "FlexiblePixelShift";
        case PrivateTag.NumberOfMaskFrames:
            return "NumberOfMaskFrames";
        case PrivateTag.NumberOfFillFrames:
            return "NumberOfFillFrames";
        case PrivateTag.SeriesNumber:
            return "SeriesNumber";
        case PrivateTag.ImageNumber:
            return "ImageNumber";
        case PrivateTag.ReadyProcessingStatus:
            return "ReadyProcessingStatus";
        }
        return "";
    }

}
