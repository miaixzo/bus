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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Private_ICS_Release_1_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0029_xx00_:
                return "_0029_xx00_";

            case PrivateTag._0029_xx05_:
                return "_0029_xx05_";

            case PrivateTag._0029_xx06_:
                return "_0029_xx06_";

            case PrivateTag._0029_xx20_:
                return "_0029_xx20_";

            case PrivateTag._0029_xx21_:
                return "_0029_xx21_";

            case PrivateTag._0029_xxC0_:
                return "_0029_xxC0_";

            case PrivateTag._0029_xxC1_:
                return "_0029_xxC1_";

            case PrivateTag._0029_xxCB_:
                return "_0029_xxCB_";

            case PrivateTag._0029_xxCC_:
                return "_0029_xxCC_";

            case PrivateTag._0029_xxCD_:
                return "_0029_xxCD_";

            case PrivateTag._0029_xxD0_:
                return "_0029_xxD0_";

            case PrivateTag._0029_xxD1_:
                return "_0029_xxD1_";

            case PrivateTag._0029_xxD2_:
                return "_0029_xxD2_";

            case PrivateTag._0029_xxD3_:
                return "_0029_xxD3_";

            case PrivateTag._0029_xxD4_:
                return "_0029_xxD4_";

            case PrivateTag._0029_xxD5_:
                return "_0029_xxD5_";

            case PrivateTag._0029_xxD6_:
                return "_0029_xxD6_";
        }
        return "";
    }

}
