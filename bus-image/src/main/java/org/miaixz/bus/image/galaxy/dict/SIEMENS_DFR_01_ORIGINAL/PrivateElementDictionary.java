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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_DFR_01_ORIGINAL;

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

            case PrivateTag._0017_xx45_:
            case PrivateTag._0017_xx46_:
                return VR.DS;

            case PrivateTag._0017_xx25_:
            case PrivateTag._0017_xx27_:
            case PrivateTag._0017_xx30_:
            case PrivateTag._0017_xx41_:
            case PrivateTag._0017_xx48_:
            case PrivateTag._0017_xx4A_:
            case PrivateTag._0017_xx71_:
            case PrivateTag._0017_xx7B_:
            case PrivateTag._0017_xxA0_:
                return VR.IS;

            case PrivateTag._0017_xx49_:
            case PrivateTag._0017_xxC1_:
            case PrivateTag._0017_xxC2_:
                return VR.LO;

            case PrivateTag._0017_xx47_:
                return VR.SH;

            case PrivateTag._0017_xx52_:
                return VR.UL;

            case PrivateTag._0017_xx11_:
            case PrivateTag._0017_xx12_:
            case PrivateTag._0017_xx14_:
            case PrivateTag._0017_xx15_:
            case PrivateTag._0017_xx16_:
            case PrivateTag._0017_xx18_:
            case PrivateTag._0017_xx21_:
            case PrivateTag._0017_xx22_:
            case PrivateTag._0017_xx23_:
            case PrivateTag._0017_xx24_:
            case PrivateTag._0017_xx26_:
            case PrivateTag._0017_xx2A_:
            case PrivateTag._0017_xx31_:
            case PrivateTag._0017_xx32_:
            case PrivateTag._0017_xx33_:
            case PrivateTag._0017_xx37_:
            case PrivateTag._0017_xx38_:
            case PrivateTag._0017_xx43_:
            case PrivateTag._0017_xx44_:
            case PrivateTag._0017_xx51_:
            case PrivateTag._0017_xx61_:
            case PrivateTag._0017_xx62_:
            case PrivateTag._0017_xx72_:
            case PrivateTag._0017_xx73_:
            case PrivateTag._0017_xx74_:
            case PrivateTag._0017_xx79_:
            case PrivateTag._0017_xx7A_:
                return VR.US;
        }
        return VR.UN;
    }

}
