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
package org.miaixz.bus.image.galaxy.dict.mitra_markup_1_0;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.Markup1:
                return "Markup1";

            case PrivateTag.Markup2:
                return "Markup2";

            case PrivateTag.Markup3:
                return "Markup3";

            case PrivateTag.Markup4:
                return "Markup4";

            case PrivateTag.Markup5:
                return "Markup5";

            case PrivateTag.Markup6:
                return "Markup6";

            case PrivateTag.Markup7:
                return "Markup7";

            case PrivateTag.Markup8:
                return "Markup8";

            case PrivateTag.Markup9:
                return "Markup9";

            case PrivateTag.Markup10:
                return "Markup10";

            case PrivateTag.Markup11:
                return "Markup11";

            case PrivateTag.Markup12:
                return "Markup12";

            case PrivateTag.Markup13:
                return "Markup13";

            case PrivateTag.Markup14:
                return "Markup14";

            case PrivateTag.Markup15:
                return "Markup15";
        }
        return "";
    }

}
