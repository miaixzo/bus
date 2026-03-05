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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DFR_01_MANIPULATED;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0017_xx11_:
                return "_0017_xx11_";

            case PrivateTag._0017_xx12_:
                return "_0017_xx12_";

            case PrivateTag._0017_xx14_:
                return "_0017_xx14_";

            case PrivateTag._0017_xx15_:
                return "_0017_xx15_";

            case PrivateTag._0017_xx25_:
                return "_0017_xx25_";

            case PrivateTag._0017_xx27_:
                return "_0017_xx27_";

            case PrivateTag.EdgeEnhancement:
                return "EdgeEnhancement";

            case PrivateTag.Harmonization:
                return "Harmonization";

            case PrivateTag._0017_xx31_:
                return "_0017_xx31_";

            case PrivateTag._0017_xx32_:
                return "_0017_xx32_";

            case PrivateTag._0017_xx33_:
                return "_0017_xx33_";

            case PrivateTag._0017_xx35_:
                return "_0017_xx35_";

            case PrivateTag._0017_xx37_:
                return "_0017_xx37_";

            case PrivateTag._0017_xx38_:
                return "_0017_xx38_";

            case PrivateTag.Landmark:
                return "Landmark";

            case PrivateTag._0017_xx72_:
                return "_0017_xx72_";

            case PrivateTag._0017_xx73_:
                return "_0017_xx73_";

            case PrivateTag._0017_xx74_:
                return "_0017_xx74_";

            case PrivateTag.PixelShiftHorizontal:
                return "PixelShiftHorizontal";

            case PrivateTag.PixelShiftVertical:
                return "PixelShiftVertical";

            case PrivateTag._0017_xx79_:
                return "_0017_xx79_";

            case PrivateTag._0017_xx7A_:
                return "_0017_xx7A_";

            case PrivateTag._0017_xx80_:
                return "_0017_xx80_";

            case PrivateTag.LeftMarker:
                return "LeftMarker";

            case PrivateTag.RightMarker:
                return "RightMarker";

            case PrivateTag._0017_xxA1_:
                return "_0017_xxA1_";

            case PrivateTag.ImageNameExtension1:
                return "ImageNameExtension1";

            case PrivateTag.ImageNameExtension2:
                return "ImageNameExtension2";
        }
        return "";
    }

}
