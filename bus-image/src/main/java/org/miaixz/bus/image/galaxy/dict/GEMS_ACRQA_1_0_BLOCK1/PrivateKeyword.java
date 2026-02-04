/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.GEMS_ACRQA_1_0_BLOCK1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.CRExposureMenuCode:
                return "CRExposureMenuCode";

            case PrivateTag.CRExposureMenuString:
                return "CRExposureMenuString";

            case PrivateTag.CREDRMode:
                return "CREDRMode";

            case PrivateTag.CRLatitude:
                return "CRLatitude";

            case PrivateTag.CRGroupNumber:
                return "CRGroupNumber";

            case PrivateTag.CRImageSerialNumber:
                return "CRImageSerialNumber";

            case PrivateTag.CRBarCodeNumber:
                return "CRBarCodeNumber";

            case PrivateTag.CRFilmOutputExposure:
                return "CRFilmOutputExposure";

            case PrivateTag.CRFilmFormat:
                return "CRFilmFormat";

            case PrivateTag.CRSShiftString:
                return "CRSShiftString";
        }
        return "";
    }

}
