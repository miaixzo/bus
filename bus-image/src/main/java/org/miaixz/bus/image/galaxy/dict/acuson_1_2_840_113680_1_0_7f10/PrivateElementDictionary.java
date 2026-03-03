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
package org.miaixz.bus.image.galaxy.dict.acuson_1_2_840_113680_1_0_7f10;

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

            case PrivateTag._7fdf_xx20_:
            case PrivateTag._7fdf_xx22_:
            case PrivateTag._7fdf_xx33_:
            case PrivateTag._7fdf_xx34_:
            case PrivateTag._7fdf_xx37_:
                return VR.FL;

            case PrivateTag._7fdf_xx62_:
            case PrivateTag._7fdf_xx63_:
            case PrivateTag._7fdf_xx64_:
            case PrivateTag._7fdf_xx73_:
            case PrivateTag._7fdf_xx8a_:
            case PrivateTag._7fdf_xx88_:
                return VR.FD;

            case PrivateTag._7fdf_xx00_:
            case PrivateTag._7fdf_xx0d_:
            case PrivateTag._7fdf_xx31_:
            case PrivateTag._7fdf_xx32_:
            case PrivateTag._7fdf_xx35_:
            case PrivateTag._7fdf_xx36_:
            case PrivateTag._7fdf_xx38_:
            case PrivateTag._7fdf_xx52_:
            case PrivateTag._7fdf_xx54_:
            case PrivateTag._7fdf_xxf5_:
                return VR.IS;

            case PrivateTag._7fdf_xx65_:
            case PrivateTag._7fdf_xx66_:
            case PrivateTag._7fdf_xx72_:
            case PrivateTag._7fdf_xx75_:
            case PrivateTag._7fdf_xx7a_:
            case PrivateTag._7fdf_xx7b_:
            case PrivateTag._7fdf_xx7c_:
            case PrivateTag._7fdf_xx7d_:
                return VR.LO;

            case PrivateTag._7fdf_xx25_:
            case PrivateTag._7fdf_xx81_:
            case PrivateTag._7fdf_xx85_:
            case PrivateTag._7fdf_xx89_:
            case PrivateTag._7fdf_xx8d_:
            case PrivateTag._7fdf_xx86_:
                return VR.OB;

            case PrivateTag._7fdf_xx10_:
            case PrivateTag._7fdf_xx50_:
                return VR.SH;

            case PrivateTag._7fdf_xx0b_:
            case PrivateTag._7fdf_xx0c_:
            case PrivateTag._7fdf_xx27_:
            case PrivateTag._7fdf_xx60_:
            case PrivateTag._7fdf_xx61_:
            case PrivateTag._7fdf_xx67_:
            case PrivateTag._7fdf_xx76_:
            case PrivateTag._7fdf_xx7e_:
            case PrivateTag._7fdf_xx7f_:
                return VR.SL;

            case PrivateTag._7fdf_xx30_:
                return VR.SQ;

            case PrivateTag._7fdf_xx24_:
            case PrivateTag._7fdf_xx26_:
            case PrivateTag._7fdf_xx28_:
            case PrivateTag._7fdf_xx29_:
            case PrivateTag._7fdf_xx2a_:
            case PrivateTag._7fdf_xx2b_:
            case PrivateTag._7fdf_xx2c_:
            case PrivateTag._7fdf_xx68_:
            case PrivateTag._7fdf_xx69_:
            case PrivateTag._7fdf_xx6a_:
            case PrivateTag._7fdf_xx6b_:
            case PrivateTag._7fdf_xx6c_:
            case PrivateTag._7fdf_xx74_:
            case PrivateTag._7fdf_xx77_:
            case PrivateTag._7fdf_xx78_:
            case PrivateTag._7fdf_xx79_:
            case PrivateTag._7fdf_xx80_:
            case PrivateTag._7fdf_xx82_:
            case PrivateTag._7fdf_xx83_:
            case PrivateTag._7fdf_xx8b_:
            case PrivateTag._7fdf_xx8c_:
            case PrivateTag._7fdf_xxf1_:
                return VR.UL;

            case PrivateTag._7fdf_xx02_:
            case PrivateTag._7fdf_xx01_:
                return VR.US;
        }
        return VR.UN;
    }

}
