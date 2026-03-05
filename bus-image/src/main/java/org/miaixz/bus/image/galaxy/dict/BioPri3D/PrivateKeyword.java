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
package org.miaixz.bus.image.galaxy.dict.BioPri3D;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0011_xx20_:
                return "_0011_xx20_";

            case PrivateTag._0011_xx24_:
                return "_0011_xx24_";

            case PrivateTag._0011_xx30_:
                return "_0011_xx30_";

            case PrivateTag._0011_xx31_:
                return "_0011_xx31_";

            case PrivateTag._0011_xx32_:
                return "_0011_xx32_";

            case PrivateTag._0011_xx39_:
                return "_0011_xx39_";

            case PrivateTag._0011_xx3A_:
                return "_0011_xx3A_";

            case PrivateTag._0011_xxD0_:
                return "_0011_xxD0_";

            case PrivateTag._0011_xxE0_:
                return "_0011_xxE0_";

            case PrivateTag._0011_xxE1_:
                return "_0011_xxE1_";

            case PrivateTag._0011_xxE2_:
                return "_0011_xxE2_";

            case PrivateTag._0011_xxE3_:
                return "_0011_xxE3_";

            case PrivateTag._0011_xxE4_:
                return "_0011_xxE4_";

            case PrivateTag._0011_xxE5_:
                return "_0011_xxE5_";

            case PrivateTag._0063_xx0C_:
                return "_0063_xx0C_";

            case PrivateTag._0063_xx35_:
                return "_0063_xx35_";

            case PrivateTag._0063_xx20_:
                return "_0063_xx20_";

            case PrivateTag._0063_xx21_:
                return "_0063_xx21_";
        }
        return "";
    }

}
