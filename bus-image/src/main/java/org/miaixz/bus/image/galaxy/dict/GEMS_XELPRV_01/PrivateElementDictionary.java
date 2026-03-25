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
package org.miaixz.bus.image.galaxy.dict.GEMS_XELPRV_01;

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

            case PrivateTag._0033_xx08_:
                return VR.CS;

            case PrivateTag._0033_xx17_:
                return VR.DA;

            case PrivateTag._0033_xx1E_:
                return VR.FD;

            case PrivateTag._0033_xx11_:
            case PrivateTag._0033_xx1A_:
            case PrivateTag._0033_xx1B_:
                return VR.LO;

            case PrivateTag._0033_xx24_:
                return VR.LT;

            case PrivateTag._0033_xx1C_:
            case PrivateTag._0033_xx1F_:
            case PrivateTag._0033_xx20_:
            case PrivateTag._0033_xx21_:
            case PrivateTag._0033_xx22_:
            case PrivateTag._0033_xx23_:
                return VR.OB;

            case PrivateTag._0033_xx10_:
                return VR.SL;

            case PrivateTag._0033_xx70_:
                return VR.SQ;

            case PrivateTag._0033_xx18_:
                return VR.TM;

            case PrivateTag._0033_xx16_:
            case PrivateTag._0033_xx71_:
            case PrivateTag._0033_xx72_:
                return VR.UI;

            case PrivateTag._0033_xx19_:
            case PrivateTag._0033_xx1D_:
                return VR.UL;
        }
        return VR.UN;
    }

}
