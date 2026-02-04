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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_DATAMAPPING_ATTRIBUTES;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.ReprocessingInfo:
                return "ReprocessingInfo";

            case PrivateTag.DataRoleType:
                return "DataRoleType";

            case PrivateTag.DataRoleName:
                return "DataRoleName";

            case PrivateTag.RescanName:
                return "RescanName";

            case PrivateTag._0011_xx05_:
                return "_0011_xx05_";

            case PrivateTag.CardiacTypeName:
                return "CardiacTypeName";

            case PrivateTag.CardiacTypeNameL2:
                return "CardiacTypeNameL2";

            case PrivateTag.MiscIndicator:
                return "MiscIndicator";

            case PrivateTag._0011_xx09_:
                return "_0011_xx09_";

            case PrivateTag._0011_xx0A_:
                return "_0011_xx0A_";

            case PrivateTag._0011_xx0B_:
                return "_0011_xx0B_";

            case PrivateTag.SplitBaggingName:
                return "SplitBaggingName";

            case PrivateTag.SplitSubBaggingName:
                return "SplitSubBaggingName";

            case PrivateTag.StageSubBaggingName:
                return "StageSubBaggingName";

            case PrivateTag.IsInternalDataRole:
                return "IsInternalDataRole";
        }
        return "";
    }

}
