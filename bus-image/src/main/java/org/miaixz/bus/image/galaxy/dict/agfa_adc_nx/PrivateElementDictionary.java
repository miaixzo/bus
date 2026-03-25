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
package org.miaixz.bus.image.galaxy.dict.agfa_adc_nx;

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

            case PrivateTag._0019_xx07_:
            case PrivateTag._0019_xx28_:
            case PrivateTag.CassetteOrientation:
            case PrivateTag._0019_xxFD_:
            case PrivateTag._0019_xxFE_:
                return VR.CS;

            case PrivateTag.PlateSensitivity:
            case PrivateTag.PlateErasability:
                return VR.DS;

            case PrivateTag._0019_xx21_:
                return VR.FL;

            case PrivateTag._0019_xxF8_:
            case PrivateTag._0019_xxFA_:
            case PrivateTag._0019_xxFC_:
                return VR.IS;

            case PrivateTag.UserDefinedField1:
            case PrivateTag.UserDefinedField2:
            case PrivateTag.UserDefinedField3:
            case PrivateTag.UserDefinedField4:
            case PrivateTag.UserDefinedField5:
                return VR.LO;

            case PrivateTag._0019_xx09_:
                return VR.SQ;
        }
        return VR.UN;
    }

}
