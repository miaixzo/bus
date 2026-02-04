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
package org.miaixz.bus.image.galaxy.dict.agfa_adc_nx;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0019_xx07_:
                return "_0019_xx07_";

            case PrivateTag._0019_xx09_:
                return "_0019_xx09_";

            case PrivateTag._0019_xx21_:
                return "_0019_xx21_";

            case PrivateTag._0019_xx28_:
                return "_0019_xx28_";

            case PrivateTag.UserDefinedField1:
                return "UserDefinedField1";

            case PrivateTag.UserDefinedField2:
                return "UserDefinedField2";

            case PrivateTag.UserDefinedField3:
                return "UserDefinedField3";

            case PrivateTag.UserDefinedField4:
                return "UserDefinedField4";

            case PrivateTag.UserDefinedField5:
                return "UserDefinedField5";

            case PrivateTag.CassetteOrientation:
                return "CassetteOrientation";

            case PrivateTag.PlateSensitivity:
                return "PlateSensitivity";

            case PrivateTag.PlateErasability:
                return "PlateErasability";

            case PrivateTag._0019_xxF8_:
                return "_0019_xxF8_";

            case PrivateTag._0019_xxFA_:
                return "_0019_xxFA_";

            case PrivateTag._0019_xxFC_:
                return "_0019_xxFC_";

            case PrivateTag._0019_xxFD_:
                return "_0019_xxFD_";

            case PrivateTag._0019_xxFE_:
                return "_0019_xxFE_";
        }
        return "";
    }

}
