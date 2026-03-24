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
package org.miaixz.bus.image.galaxy.dict.Siemens__Thorax_Multix_FD_Lab_Settings;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0019_xx00_:
                return "_0019_xx00_";

            case PrivateTag._0019_xx01_:
                return "_0019_xx01_";

            case PrivateTag.TotalDoseAreaProduct:
                return "TotalDoseAreaProduct";

            case PrivateTag._0019_xx03_:
                return "_0019_xx03_";

            case PrivateTag._0019_xx04_:
                return "_0019_xx04_";

            case PrivateTag._0019_xx05_:
                return "_0019_xx05_";

            case PrivateTag.TableObjectDistance:
                return "TableObjectDistance";

            case PrivateTag.TableDetectorDistance:
                return "TableDetectorDistance";

            case PrivateTag.OrthoStepDistance:
                return "OrthoStepDistance";

            case PrivateTag.AutoWindowFlag:
                return "AutoWindowFlag";

            case PrivateTag.AutoWindowCenter:
                return "AutoWindowCenter";

            case PrivateTag.AutoWindowWidth:
                return "AutoWindowWidth";

            case PrivateTag.FilterID:
                return "FilterID";

            case PrivateTag.AnatomicCorrectView:
                return "AnatomicCorrectView";

            case PrivateTag.AutoWindowShift:
                return "AutoWindowShift";

            case PrivateTag.AutoWindowExpansion:
                return "AutoWindowExpansion";

            case PrivateTag.SystemType:
                return "SystemType";

            case PrivateTag.AnatomicSortNumber:
                return "AnatomicSortNumber";

            case PrivateTag.AcquisitionSortNumber:
                return "AcquisitionSortNumber";
        }
        return "";
    }

}
