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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Private_ICS_Release_1_2;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0029_xx00_:
                return "_0029_xx00_";

            case PrivateTag._0029_xx01_:
                return "_0029_xx01_";

            case PrivateTag._0029_xx02_:
                return "_0029_xx02_";

            case PrivateTag._0029_xx03_:
                return "_0029_xx03_";

            case PrivateTag._0029_xx04_:
                return "_0029_xx04_";

            case PrivateTag._0029_xx05_:
                return "_0029_xx05_";

            case PrivateTag._0029_xx30_:
                return "_0029_xx30_";

            case PrivateTag._0029_xxA0_:
                return "_0029_xxA0_";

            case PrivateTag._0029_xxA1_:
                return "_0029_xxA1_";

            case PrivateTag._0029_xxA2_:
                return "_0029_xxA2_";

            case PrivateTag._0029_xxA3_:
                return "_0029_xxA3_";

            case PrivateTag._0029_xxA5_:
                return "_0029_xxA5_";

            case PrivateTag._0029_xxA6_:
                return "_0029_xxA6_";

            case PrivateTag._0029_xxD9_:
                return "_0029_xxD9_";
        }
        return "";
    }

}
