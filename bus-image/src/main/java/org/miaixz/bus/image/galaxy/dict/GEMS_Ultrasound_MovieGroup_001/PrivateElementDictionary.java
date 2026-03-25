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
package org.miaixz.bus.image.galaxy.dict.GEMS_Ultrasound_MovieGroup_001;

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

            case PrivateTag._7FE1_xx51_:
                return VR.FL;

            case PrivateTag._7FE1_xx3C_:
            case PrivateTag._7FE1_xx48_:
            case PrivateTag._7FE1_xx52_:
            case PrivateTag._7FE1_xx77_:
            case PrivateTag._7FE1_xx87_:
            case PrivateTag._7FE1_xx88_:
                return VR.FD;

            case PrivateTag._7FE1_xx02_:
            case PrivateTag._7FE1_xx12_:
            case PrivateTag._7FE1_xx30_:
            case PrivateTag._7FE1_xx72_:
            case PrivateTag._7FE1_xx74_:
            case PrivateTag._7FE1_xx84_:
                return VR.LO;

            case PrivateTag._7FE1_xx57_:
                return VR.LT;

            case PrivateTag._7FE1_xx43_:
            case PrivateTag._7FE1_xx55_:
            case PrivateTag._7FE1_xx60_:
                return VR.OB;

            case PrivateTag._7FE1_xx61_:
            case PrivateTag._7FE1_xx69_:
                return VR.OW;

            case PrivateTag._7FE1_xx24_:
                return VR.SH;

            case PrivateTag._7FE1_xx54_:
            case PrivateTag._7FE1_xx79_:
            case PrivateTag._7FE1_xx86_:
                return VR.SL;

            case PrivateTag._7FE1_xx01_:
            case PrivateTag._7FE1_xx08_:
            case PrivateTag._7FE1_xx10_:
            case PrivateTag._7FE1_xx18_:
            case PrivateTag._7FE1_xx20_:
            case PrivateTag._7FE1_xx26_:
            case PrivateTag._7FE1_xx36_:
            case PrivateTag._7FE1_xx3A_:
            case PrivateTag._7FE1_xx62_:
            case PrivateTag._7FE1_xx70_:
            case PrivateTag._7FE1_xx73_:
            case PrivateTag._7FE1_xx75_:
            case PrivateTag._7FE1_xx83_:
            case PrivateTag._7FE1_xx85_:
                return VR.SQ;

            case PrivateTag._7FE1_xx03_:
            case PrivateTag._7FE1_xx32_:
            case PrivateTag._7FE1_xx37_:
            case PrivateTag._7FE1_xx49_:
            case PrivateTag._7FE1_xx53_:
            case PrivateTag._7FE1_xx71_:
                return VR.UL;
        }
        return VR.UN;
    }

}
