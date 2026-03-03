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
package org.miaixz.bus.image.galaxy.dict.SPI_P_PCR_Release_2;

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

            case PrivateTag._0019_xx40_:
            case PrivateTag._0019_xxA3_:
            case PrivateTag._0019_xxA4_:
            case PrivateTag._0019_xxA5_:
            case PrivateTag._0019_xxA6_:
            case PrivateTag._0019_xxA7_:
            case PrivateTag._0019_xxA8_:
            case PrivateTag._0019_xxA9_:
            case PrivateTag._0019_xxAA_:
            case PrivateTag._0019_xxAB_:
            case PrivateTag._0019_xxAC_:
            case PrivateTag._0019_xxAD_:
            case PrivateTag._0019_xxAE_:
            case PrivateTag._0019_xxB5_:
            case PrivateTag._0019_xxB6_:
            case PrivateTag._0019_xxB8_:
                return VR.DS;

            case PrivateTag._0019_xx20_:
            case PrivateTag._0019_xxB2_:
            case PrivateTag._0019_xxB3_:
            case PrivateTag._0019_xxB4_:
                return VR.IS;

            case PrivateTag._0019_xx21_:
            case PrivateTag._0019_xx60_:
            case PrivateTag._0019_xx90_:
                return VR.LO;

            case PrivateTag._0019_xxA1_:
            case PrivateTag._0019_xxAF_:
            case PrivateTag._0019_xxB0_:
            case PrivateTag._0019_xxB1_:
            case PrivateTag._0019_xxB7_:
            case PrivateTag._0019_xxB9_:
            case PrivateTag._0019_xxBA_:
                return VR.ST;

            case PrivateTag._0019_xx10_:
            case PrivateTag._0019_xx30_:
            case PrivateTag._0019_xx80_:
                return VR.US;
        }
        return VR.UN;
    }

}
