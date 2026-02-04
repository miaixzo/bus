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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_MR_SPECTRO_1;

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

            case PrivateTag._0019_xx07_:
            case PrivateTag._0019_xx08_:
            case PrivateTag._0019_xx09_:
            case PrivateTag._0019_xx10_:
            case PrivateTag._0019_xx12_:
            case PrivateTag._0019_xx13_:
            case PrivateTag._0019_xx16_:
            case PrivateTag._0019_xx17_:
            case PrivateTag._0019_xx20_:
            case PrivateTag._0019_xx21_:
            case PrivateTag._0019_xx22_:
            case PrivateTag._0019_xx23_:
            case PrivateTag._0019_xx24_:
            case PrivateTag._0019_xx25_:
            case PrivateTag._0019_xx26_:
            case PrivateTag._0019_xx27_:
            case PrivateTag._0019_xx28_:
            case PrivateTag._0019_xx29_:
            case PrivateTag._0019_xx42_:
            case PrivateTag._0019_xx43_:
            case PrivateTag._0019_xx47_:
            case PrivateTag._0019_xx48_:
            case PrivateTag._0019_xx71_:
            case PrivateTag._0019_xx80_:
                return VR.IS;

            case PrivateTag._0019_xx41_:
                return VR.LT;

            case PrivateTag._0019_xx01_:
            case PrivateTag._0019_xx02_:
            case PrivateTag._0019_xx03_:
            case PrivateTag._0019_xx04_:
            case PrivateTag._0019_xx05_:
            case PrivateTag._0019_xx06_:
            case PrivateTag._0019_xx14_:
            case PrivateTag._0019_xx15_:
            case PrivateTag._0019_xx31_:
            case PrivateTag._0019_xx32_:
            case PrivateTag._0019_xx45_:
            case PrivateTag._0019_xx46_:
            case PrivateTag._0019_xx49_:
            case PrivateTag._0019_xx60_:
            case PrivateTag._0019_xx61_:
            case PrivateTag._0019_xx72_:
            case PrivateTag._0019_xx73_:
            case PrivateTag._0019_xx74_:
            case PrivateTag._0019_xx76_:
            case PrivateTag._0019_xx77_:
            case PrivateTag._0019_xx78_:
            case PrivateTag._0019_xx79_:
                return VR.US;
        }
        return VR.UN;
    }

}
