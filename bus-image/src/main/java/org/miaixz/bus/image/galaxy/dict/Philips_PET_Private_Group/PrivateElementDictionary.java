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
package org.miaixz.bus.image.galaxy.dict.Philips_PET_Private_Group;

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

            case PrivateTag.SUVFactor:
            case PrivateTag.ActivityConcentrationScaleFactor:
                return VR.DS;

            case PrivateTag.WorklistInfoFileName:
                return VR.LO;

            case PrivateTag._7053_xx04_:
            case PrivateTag._7053_xx06_:
                return VR.OB;

            case PrivateTag._7053_xx07_:
            case PrivateTag._7053_xx08_:
            case PrivateTag._7053_xx12_:
                return VR.SQ;

            case PrivateTag._7053_xx13_:
            case PrivateTag._7053_xx14_:
            case PrivateTag._7053_xx15_:
            case PrivateTag._7053_xx16_:
            case PrivateTag._7053_xx17_:
            case PrivateTag._7053_xx18_:
                return VR.SS;

            case PrivateTag.OriginalFileName:
                return VR.ST;

            case PrivateTag._7053_xxC2_:
                return VR.UI;

            case PrivateTag._7053_xx0F_:
                return VR.UL;

            case PrivateTag.PrivateData:
            case PrivateTag._7053_xx10_:
            case PrivateTag._7053_xx11_:
                return VR.US;
        }
        return VR.UN;
    }

}
