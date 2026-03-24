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
package org.miaixz.bus.image.galaxy.dict.GEMS_IMPS_01;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.LowerRangeOfPixels:
                return "LowerRangeOfPixels";

            case PrivateTag.LowerRangeOfPixels1:
                return "LowerRangeOfPixels1";

            case PrivateTag.UpperRangeOfPixels1:
                return "UpperRangeOfPixels1";

            case PrivateTag.LowerRangeOfPixels2:
                return "LowerRangeOfPixels2";

            case PrivateTag.UpperRangeOfPixels2:
                return "UpperRangeOfPixels2";

            case PrivateTag.LengthOfTotalHeaderInBytes:
                return "LengthOfTotalHeaderInBytes";

            case PrivateTag.VersionOfHeaderStructure:
                return "VersionOfHeaderStructure";

            case PrivateTag.AdvantageCompOverflow:
                return "AdvantageCompOverflow";

            case PrivateTag.AdvantageCompUnderflow:
                return "AdvantageCompUnderflow";
        }
        return "";
    }

}
