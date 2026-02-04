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
package org.miaixz.bus.image.galaxy.dict.Philips_RAD_Imaging_DD_001;

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

            case PrivateTag._200B_xx52_:
                return VR.CS;

            case PrivateTag._200B_xx2B_:
            case PrivateTag._200B_xx47_:
                return VR.DA;

            case PrivateTag._200B_xx28_:
            case PrivateTag._200B_xx29_:
                return VR.DS;

            case PrivateTag._200B_xx27_:
            case PrivateTag._200B_xx4F_:
                return VR.DT;

            case PrivateTag._200B_xx05_:
                return VR.IS;

            case PrivateTag._200B_xx11_:
            case PrivateTag._200B_xx2D_:
            case PrivateTag._200B_xx3B_:
                return VR.LO;

            case PrivateTag._200B_xx00_:
                return VR.PN;

            case PrivateTag._200B_xx40_:
            case PrivateTag._200B_xx41_:
            case PrivateTag._200B_xx48_:
            case PrivateTag._200B_xx4C_:
            case PrivateTag._200B_xx4D_:
                return VR.SH;

            case PrivateTag._200B_xx2C_:
                return VR.TM;

            case PrivateTag._200B_xx42_:
            case PrivateTag._200B_xx43_:
                return VR.UI;

            case PrivateTag._200B_xx2A_:
                return VR.UL;

            case PrivateTag._200B_xx01_:
            case PrivateTag._200B_xx02_:
                return VR.US;
        }
        return VR.UN;
    }

}
