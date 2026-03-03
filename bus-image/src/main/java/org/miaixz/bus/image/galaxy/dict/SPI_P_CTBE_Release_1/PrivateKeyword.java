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
package org.miaixz.bus.image.galaxy.dict.SPI_P_CTBE_Release_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0019_xx00_:
                return "_0019_xx00_";

            case PrivateTag._0019_xx02_:
                return "_0019_xx02_";

            case PrivateTag._0019_xx03_:
                return "_0019_xx03_";

            case PrivateTag._0019_xx04_:
                return "_0019_xx04_";

            case PrivateTag._0019_xx05_:
                return "_0019_xx05_";

            case PrivateTag._0019_xx0B_:
                return "_0019_xx0B_";

            case PrivateTag._0019_xx0C_:
                return "_0019_xx0C_";

            case PrivateTag._0019_xx14_:
                return "_0019_xx14_";

            case PrivateTag._0019_xx18_:
                return "_0019_xx18_";

            case PrivateTag._0019_xx19_:
                return "_0019_xx19_";

            case PrivateTag._0019_xx1A_:
                return "_0019_xx1A_";

            case PrivateTag._0019_xx1B_:
                return "_0019_xx1B_";

            case PrivateTag._0019_xx1C_:
                return "_0019_xx1C_";

            case PrivateTag._0019_xx1D_:
                return "_0019_xx1D_";
        }
        return "";
    }

}
