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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SMS_AX__ORIGINAL_IMAGE_INFO_1_0;

/**
 * @author Kimi Liu
 * @since Java 21+
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
