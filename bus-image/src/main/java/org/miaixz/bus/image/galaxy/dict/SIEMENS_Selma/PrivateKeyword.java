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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_Selma;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0019_xx06_:
                return "_0019_xx06_";

            case PrivateTag._0019_xx07_:
                return "_0019_xx07_";

            case PrivateTag._0019_xx08_:
                return "_0019_xx08_";

            case PrivateTag._0019_xx26_:
                return "_0019_xx26_";

            case PrivateTag._0019_xx29_:
                return "_0019_xx29_";

            case PrivateTag._0019_xx30_:
                return "_0019_xx30_";

            case PrivateTag._0019_xx31_:
                return "_0019_xx31_";

            case PrivateTag._0019_xx32_:
                return "_0019_xx32_";

            case PrivateTag._0019_xx33_:
                return "_0019_xx33_";

            case PrivateTag._0019_xx34_:
                return "_0019_xx34_";

            case PrivateTag._0019_xx35_:
                return "_0019_xx35_";
        }
        return "";
    }

}
