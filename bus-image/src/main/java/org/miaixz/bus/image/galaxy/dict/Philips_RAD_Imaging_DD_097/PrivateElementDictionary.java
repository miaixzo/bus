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
package org.miaixz.bus.image.galaxy.dict.Philips_RAD_Imaging_DD_097;

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

            case PrivateTag._200B_xx01_:
            case PrivateTag._200B_xx75_:
            case PrivateTag._200B_xx86_:
            case PrivateTag._200B_xx88_:
                return VR.CS;

            case PrivateTag._200B_xx90_:
                return VR.DS;

            case PrivateTag._200B_xx72_:
            case PrivateTag._200B_xx9A_:
            case PrivateTag._200B_xx9B_:
                return VR.FD;

            case PrivateTag._200B_xx74_:
            case PrivateTag._200B_xx78_:
            case PrivateTag._200B_xx79_:
            case PrivateTag._200B_xx7A_:
            case PrivateTag._200B_xx7D_:
            case PrivateTag._200B_xx85_:
                return VR.IS;

            case PrivateTag._200B_xx81_:
            case PrivateTag._200B_xx82_:
                return VR.LO;

            case PrivateTag._200B_xx60_:
            case PrivateTag._200B_xx63_:
            case PrivateTag._200B_xx89_:
            case PrivateTag._200B_xxA0_:
                return VR.LT;

            case PrivateTag._200B_xx76_:
            case PrivateTag._200B_xx96_:
            case PrivateTag._200B_xx99_:
                return VR.SH;

            case PrivateTag._200B_xx02_:
            case PrivateTag._200B_xx50_:
            case PrivateTag._200B_xx51_:
            case PrivateTag._200B_xx52_:
            case PrivateTag._200B_xx53_:
            case PrivateTag._200B_xx65_:
            case PrivateTag._200B_xx73_:
                return VR.SS;

            case PrivateTag._200B_xx00_:
            case PrivateTag._200B_xx54_:
                return VR.ST;

            case PrivateTag._200B_xx7E_:
                return VR.UI;

            case PrivateTag._200B_xx6E_:
            case PrivateTag._200B_xx7B_:
            case PrivateTag._200B_xx7C_:
                return VR.US;
        }
        return VR.UN;
    }

}
