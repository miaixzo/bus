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
package org.miaixz.bus.image.galaxy.dict.GEMS_FUNCTOOL_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.GroupName:
                return "GroupName";

            case PrivateTag.FunctionName:
                return "FunctionName";

            case PrivateTag.Bias:
                return "Bias";

            case PrivateTag.Scale:
                return "Scale";

            case PrivateTag.ParameterCount:
                return "ParameterCount";

            case PrivateTag.Parameters:
                return "Parameters";

            case PrivateTag.Version:
                return "Version";

            case PrivateTag.ColorRampIndex:
                return "ColorRampIndex";

            case PrivateTag.WindowWidth:
                return "WindowWidth";

            case PrivateTag.WindowLevel:
                return "WindowLevel";

            case PrivateTag.BValue:
                return "BValue";

            case PrivateTag.WizardStateDataSize:
                return "WizardStateDataSize";

            case PrivateTag.WizardState:
                return "WizardState";

            case PrivateTag._0051_xx0E_:
                return "_0051_xx0E_";
        }
        return "";
    }

}
