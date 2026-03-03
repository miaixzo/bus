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
package org.miaixz.bus.image.galaxy.dict.Philips_US_Imaging_DD_042;

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

            case PrivateTag._200D_xx16_:
                return VR.FD;

            case PrivateTag._200D_xx15_:
                return VR.IS;

            case PrivateTag._200D_xx20_:
            case PrivateTag._200D_xx30_:
            case PrivateTag._200D_xx31_:
            case PrivateTag._200D_xx40_:
            case PrivateTag._200D_xx50_:
            case PrivateTag._200D_xx51_:
            case PrivateTag._200D_xx52_:
            case PrivateTag._200D_xx53_:
            case PrivateTag._200D_xx54_:
            case PrivateTag._200D_xx55_:
            case PrivateTag._200D_xx56_:
            case PrivateTag._200D_xx57_:
            case PrivateTag._200D_xx58_:
            case PrivateTag._200D_xx59_:
            case PrivateTag._200D_xx5A_:
            case PrivateTag._200D_xx5B_:
            case PrivateTag._200D_xx5C_:
            case PrivateTag._200D_xx5D_:
            case PrivateTag._200D_xx5E_:
            case PrivateTag._200D_xx5F_:
            case PrivateTag._200D_xx60_:
            case PrivateTag._200D_xx70_:
            case PrivateTag._200D_xx71_:
            case PrivateTag._200D_xx72_:
            case PrivateTag._200D_xx73_:
            case PrivateTag._200D_xx74_:
            case PrivateTag._200D_xx75_:
            case PrivateTag._200D_xx76_:
            case PrivateTag._200D_xx77_:
            case PrivateTag._200D_xx78_:
            case PrivateTag._200D_xx8C_:
                return VR.LO;
        }
        return VR.UN;
    }

}
