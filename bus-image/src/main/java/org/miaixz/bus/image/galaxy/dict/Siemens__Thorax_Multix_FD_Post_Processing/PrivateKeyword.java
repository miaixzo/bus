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
package org.miaixz.bus.image.galaxy.dict.Siemens__Thorax_Multix_FD_Post_Processing;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0021_xx00_:
                return "_0021_xx00_";

            case PrivateTag._0021_xx01_:
                return "_0021_xx01_";

            case PrivateTag._0021_xx02_:
                return "_0021_xx02_";

            case PrivateTag._0021_xx03_:
                return "_0021_xx03_";

            case PrivateTag._0021_xx04_:
                return "_0021_xx04_";

            case PrivateTag._0021_xx05_:
                return "_0021_xx05_";

            case PrivateTag._0021_xx06_:
                return "_0021_xx06_";

            case PrivateTag._0021_xx07_:
                return "_0021_xx07_";

            case PrivateTag.AutoWindowFlag:
                return "AutoWindowFlag";

            case PrivateTag.AutoWindowCenter:
                return "AutoWindowCenter";

            case PrivateTag.AutoWindowWidth:
                return "AutoWindowWidth";

            case PrivateTag.FilterID:
                return "FilterID";

            case PrivateTag._0021_xx0C_:
                return "_0021_xx0C_";

            case PrivateTag._0021_xx0D_:
                return "_0021_xx0D_";

            case PrivateTag.DoseControlValue:
                return "DoseControlValue";

            case PrivateTag._0021_xx0F_:
                return "_0021_xx0F_";

            case PrivateTag._0021_xx10_:
                return "_0021_xx10_";

            case PrivateTag._0021_xx11_:
                return "_0021_xx11_";

            case PrivateTag._0021_xx12_:
                return "_0021_xx12_";

            case PrivateTag._0021_xx13_:
                return "_0021_xx13_";

            case PrivateTag.AnatomicCorrectView:
                return "AnatomicCorrectView";

            case PrivateTag.AutoWindowShift:
                return "AutoWindowShift";

            case PrivateTag.AutoWindowExpansion:
                return "AutoWindowExpansion";

            case PrivateTag.SystemType:
                return "SystemType";

            case PrivateTag.DetectorType:
                return "DetectorType";

            case PrivateTag.AnatomicSortNumber:
                return "AnatomicSortNumber";

            case PrivateTag.AcquisitionSortNumber:
                return "AcquisitionSortNumber";
        }
        return "";
    }

}
