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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Private_ICS_Release_1;

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

            case PrivateTag._0019_xx30_:
                return VR.DS;

            case PrivateTag._0029_xx4E_:
            case PrivateTag._0029_xx4F_:
            case PrivateTag._0029_xx50_:
            case PrivateTag._0029_xx51_:
                return VR.FD;

            case PrivateTag._0029_xx91_:
                return VR.IS;

            case PrivateTag._0019_xx31_:
            case PrivateTag._0029_xx67_:
            case PrivateTag._0029_xx6A_:
                return VR.LO;

            case PrivateTag._0029_xx0D_:
            case PrivateTag._0029_xx0E_:
            case PrivateTag._0029_xx0F_:
            case PrivateTag._0029_xx10_:
            case PrivateTag._0029_xx12_:
            case PrivateTag._0029_xx1B_:
            case PrivateTag._0029_xx1C_:
            case PrivateTag._0029_xx1D_:
            case PrivateTag._0029_xx1E_:
            case PrivateTag._0029_xx21_:
            case PrivateTag._0029_xx4C_:
            case PrivateTag._0029_xx4D_:
            case PrivateTag._0029_xx72_:
                return VR.SQ;

            case PrivateTag._0029_xx68_:
            case PrivateTag._0029_xx6B_:
                return VR.US;
        }
        return VR.UN;
    }

}
