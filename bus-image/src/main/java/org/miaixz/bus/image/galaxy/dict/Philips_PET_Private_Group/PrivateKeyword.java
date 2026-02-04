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
package org.miaixz.bus.image.galaxy.dict.Philips_PET_Private_Group;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.PrivateData:
                return "PrivateData";

            case PrivateTag.SUVFactor:
                return "SUVFactor";

            case PrivateTag.OriginalFileName:
                return "OriginalFileName";

            case PrivateTag._7053_xx04_:
                return "_7053_xx04_";

            case PrivateTag.WorklistInfoFileName:
                return "WorklistInfoFileName";

            case PrivateTag._7053_xx06_:
                return "_7053_xx06_";

            case PrivateTag._7053_xx07_:
                return "_7053_xx07_";

            case PrivateTag._7053_xx08_:
                return "_7053_xx08_";

            case PrivateTag.ActivityConcentrationScaleFactor:
                return "ActivityConcentrationScaleFactor";

            case PrivateTag._7053_xx0F_:
                return "_7053_xx0F_";

            case PrivateTag._7053_xx10_:
                return "_7053_xx10_";

            case PrivateTag._7053_xx11_:
                return "_7053_xx11_";

            case PrivateTag._7053_xx12_:
                return "_7053_xx12_";

            case PrivateTag._7053_xx13_:
                return "_7053_xx13_";

            case PrivateTag._7053_xx14_:
                return "_7053_xx14_";

            case PrivateTag._7053_xx15_:
                return "_7053_xx15_";

            case PrivateTag._7053_xx16_:
                return "_7053_xx16_";

            case PrivateTag._7053_xx17_:
                return "_7053_xx17_";

            case PrivateTag._7053_xx18_:
                return "_7053_xx18_";

            case PrivateTag._7053_xxC2_:
                return "_7053_xxC2_";
        }
        return "";
    }

}
