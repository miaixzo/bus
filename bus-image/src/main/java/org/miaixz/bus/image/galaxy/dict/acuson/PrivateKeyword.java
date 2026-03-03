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
package org.miaixz.bus.image.galaxy.dict.acuson;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0009_xx00_:
                return "_0009_xx00_";

            case PrivateTag._0009_xx01_:
                return "_0009_xx01_";

            case PrivateTag._0009_xx02_:
                return "_0009_xx02_";

            case PrivateTag._0009_xx03_:
                return "_0009_xx03_";

            case PrivateTag._0009_xx04_:
                return "_0009_xx04_";

            case PrivateTag._0009_xx05_:
                return "_0009_xx05_";

            case PrivateTag._0009_xx06_:
                return "_0009_xx06_";

            case PrivateTag._0009_xx07_:
                return "_0009_xx07_";

            case PrivateTag._0009_xx08_:
                return "_0009_xx08_";

            case PrivateTag._0009_xx09_:
                return "_0009_xx09_";

            case PrivateTag._0009_xx0a_:
                return "_0009_xx0a_";

            case PrivateTag._0009_xx0b_:
                return "_0009_xx0b_";

            case PrivateTag._0009_xx0c_:
                return "_0009_xx0c_";

            case PrivateTag._0009_xx0d_:
                return "_0009_xx0d_";

            case PrivateTag._0009_xx0e_:
                return "_0009_xx0e_";

            case PrivateTag._0009_xx0f_:
                return "_0009_xx0f_";

            case PrivateTag._0009_xx10_:
                return "_0009_xx10_";

            case PrivateTag._0009_xx11_:
                return "_0009_xx11_";

            case PrivateTag._0009_xx12_:
                return "_0009_xx12_";

            case PrivateTag._0009_xx13_:
                return "_0009_xx13_";

            case PrivateTag._0009_xx14_:
                return "_0009_xx14_";

            case PrivateTag._0009_xx15_:
                return "_0009_xx15_";
        }
        return "";
    }

}
