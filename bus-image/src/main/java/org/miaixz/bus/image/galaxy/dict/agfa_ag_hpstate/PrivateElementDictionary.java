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
package org.miaixz.bus.image.galaxy.dict.agfa_ag_hpstate;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
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

            case PrivateTag._0019_xxA1_:
            case PrivateTag._0019_xxA2_:
            case PrivateTag._0019_xxA3_:
            case PrivateTag._0019_xxA4_:
            case PrivateTag._0071_xx20_:
            case PrivateTag._0073_xx80_:
                return VR.FL;

            case PrivateTag._0071_xx21_:
            case PrivateTag._0071_xx22_:
            case PrivateTag._0071_xx23_:
            case PrivateTag._0071_xx24_:
            case PrivateTag._0071_xx2B_:
            case PrivateTag._0071_xx2C_:
            case PrivateTag._0071_xx2D_:
            case PrivateTag._0087_xx05_:
            case PrivateTag._0087_xx06_:
            case PrivateTag._0087_xx07_:
            case PrivateTag._0087_xx08_:
                return VR.FD;

            case PrivateTag._0075_xx10_:
            case PrivateTag._0087_xx01_:
            case PrivateTag._0087_xx02_:
                return VR.LO;

            case PrivateTag._0011_xx11_:
            case PrivateTag._0073_xx23_:
                return VR.SH;

            case PrivateTag._0087_xx03_:
            case PrivateTag._0087_xx04_:
                return VR.SL;

            case PrivateTag._0019_xxA0_:
            case PrivateTag._0071_xx18_:
            case PrivateTag._0071_xx19_:
            case PrivateTag._0071_xx1A_:
            case PrivateTag._0071_xx1C_:
            case PrivateTag._0071_xx1E_:
            case PrivateTag._0073_xx24_:
            case PrivateTag._0073_xx28_:
                return VR.SQ;
        }
        return VR.UN;
    }

}
