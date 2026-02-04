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
package org.miaixz.bus.image.galaxy.dict.Philips_US_Imaging_DD_043;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag._200D_xx1F_:
            case PrivateTag._200D_xx23_:
            case PrivateTag._200D_xx24_:
            case PrivateTag._200D_xx25_:
            case PrivateTag._200D_xx26_:
            case PrivateTag._200D_xx27_:
            case PrivateTag._200D_xx28_:
            case PrivateTag._200D_xx29_:
            case PrivateTag._200D_xx2A_:
            case PrivateTag._200D_xx2B_:
            case PrivateTag._200D_xx2C_:
            case PrivateTag._200D_xx2D_:
            case PrivateTag._200D_xx2E_:
            case PrivateTag._200D_xx2F_:
            case PrivateTag._200D_xx30_:
            case PrivateTag._200D_xx31_:
            case PrivateTag._200D_xx32_:
            case PrivateTag._200D_xx33_:
            case PrivateTag._200D_xx34_:
            case PrivateTag._200D_xx37_:
            case PrivateTag._200D_xx38_:
            case PrivateTag._200D_xx39_:
                return VR.FD;

            case PrivateTag._200D_xx09_:
            case PrivateTag._200D_xx0A_:
            case PrivateTag._200D_xx0B_:
            case PrivateTag._200D_xx0C_:
            case PrivateTag._200D_xx0D_:
            case PrivateTag._200D_xx0E_:
            case PrivateTag._200D_xx0F_:
            case PrivateTag._200D_xx10_:
            case PrivateTag._200D_xx11_:
            case PrivateTag._200D_xx17_:
            case PrivateTag._200D_xx1A_:
            case PrivateTag._200D_xx1B_:
            case PrivateTag._200D_xx1E_:
            case PrivateTag._200D_xx21_:
            case PrivateTag._200D_xx35_:
            case PrivateTag._200D_xx36_:
            case PrivateTag._200D_xx40_:
            case PrivateTag._200D_xx41_:
            case PrivateTag._200D_xx42_:
                return VR.IS;

            case PrivateTag._200D_xx05_:
                return VR.SH;
        }
        return VR.UN;
    }

}
