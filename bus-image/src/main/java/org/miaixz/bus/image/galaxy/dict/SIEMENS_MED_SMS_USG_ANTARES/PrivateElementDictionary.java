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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SMS_USG_ANTARES;

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

            case PrivateTag._0019_xx03_:
            case PrivateTag._0019_xx21_:
            case PrivateTag._0019_xx22_:
            case PrivateTag._0019_xx43_:
            case PrivateTag._0019_xx54_:
            case PrivateTag._0019_xx60_:
            case PrivateTag._0019_xx61_:
            case PrivateTag._0019_xx62_:
            case PrivateTag._0019_xx63_:
            case PrivateTag._0019_xx80_:
            case PrivateTag._0019_xx81_:
                return VR.FD;

            case PrivateTag._0019_xx3B_:
            case PrivateTag._0019_xxA0_:
                return VR.LT;

            case PrivateTag._0019_xx00_:
            case PrivateTag._0019_xx0D_:
            case PrivateTag._0019_xx20_:
            case PrivateTag._0019_xx2E_:
            case PrivateTag._0019_xx40_:
            case PrivateTag._0019_xx42_:
            case PrivateTag._0019_xx66_:
            case PrivateTag._0019_xx87_:
                return VR.SH;

            case PrivateTag._0019_xx0C_:
            case PrivateTag._0019_xx0E_:
            case PrivateTag._0019_xx95_:
            case PrivateTag._0019_xx23_:
            case PrivateTag._0019_xx24_:
            case PrivateTag._0019_xx25_:
            case PrivateTag._0019_xx26_:
            case PrivateTag._0019_xx27_:
            case PrivateTag._0019_xx28_:
            case PrivateTag._0019_xx29_:
            case PrivateTag._0019_xx2A_:
            case PrivateTag._0019_xx2D_:
            case PrivateTag._0019_xx31_:
            case PrivateTag._0019_xx3A_:
            case PrivateTag._0019_xx41_:
            case PrivateTag._0019_xx44_:
            case PrivateTag._0019_xx46_:
            case PrivateTag._0019_xx47_:
            case PrivateTag._0019_xx48_:
            case PrivateTag._0019_xx49_:
            case PrivateTag._0019_xx65_:
            case PrivateTag._0019_xx67_:
            case PrivateTag._0019_xx69_:
            case PrivateTag._0019_xx6A_:
            case PrivateTag._0019_xx6C_:
            case PrivateTag._0019_xx72_:
            case PrivateTag._0019_xx82_:
            case PrivateTag._0019_xx83_:
            case PrivateTag._0019_xx86_:
            case PrivateTag._0019_xx88_:
                return VR.US;
        }
        return VR.UN;
    }

}
