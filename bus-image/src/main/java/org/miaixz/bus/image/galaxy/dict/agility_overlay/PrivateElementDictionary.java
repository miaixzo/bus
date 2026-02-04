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
package org.miaixz.bus.image.galaxy.dict.agility_overlay;

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

            case PrivateTag._0071_xx08_:
            case PrivateTag._0071_xx0A_:
            case PrivateTag._0071_xx12_:
            case PrivateTag._0071_xx18_:
            case PrivateTag._0071_xx50_:
            case PrivateTag._0071_xx51_:
            case PrivateTag._0071_xx53_:
            case PrivateTag._0071_xx54_:
            case PrivateTag._0071_xx55_:
            case PrivateTag._0071_xx56_:
            case PrivateTag._0071_xx57_:
            case PrivateTag._0071_xx5D_:
                return VR.CS;

            case PrivateTag._0071_xx10_:
            case PrivateTag._0071_xx59_:
            case PrivateTag._0071_xx5A_:
            case PrivateTag._0071_xx5B_:
            case PrivateTag._0071_xx5C_:
                return VR.FL;

            case PrivateTag._0071_xx15_:
            case PrivateTag._0071_xx60_:
                return VR.FD;

            case PrivateTag._0071_xx52_:
                return VR.LT;

            case PrivateTag._0071_xx06_:
            case PrivateTag._0071_xx2D_:
                return VR.SQ;

            case PrivateTag._0071_xx01_:
            case PrivateTag._0071_xx02_:
            case PrivateTag._0071_xx03_:
            case PrivateTag._0071_xx05_:
            case PrivateTag._0071_xx07_:
            case PrivateTag._0071_xx09_:
            case PrivateTag._0071_xx11_:
            case PrivateTag._0071_xx16_:
            case PrivateTag._0071_xx17_:
            case PrivateTag._0071_xx19_:
            case PrivateTag._0071_xx1B_:
            case PrivateTag._0071_xx1C_:
            case PrivateTag._0071_xx1D_:
            case PrivateTag._0071_xx22_:
            case PrivateTag._0071_xx2C_:
                return VR.ST;

            case PrivateTag._0071_xx13_:
            case PrivateTag._0071_xx14_:
            case PrivateTag._0071_xx2E_:
                return VR.US;
        }
        return VR.UN;
    }

}
