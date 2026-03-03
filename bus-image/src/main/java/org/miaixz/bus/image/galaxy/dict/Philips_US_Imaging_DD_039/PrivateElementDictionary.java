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
package org.miaixz.bus.image.galaxy.dict.Philips_US_Imaging_DD_039;

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
            case PrivateTag._200D_xx60_:
            case PrivateTag._200D_xx61_:
                return VR.IS;

            case PrivateTag._200D_xx01_:
            case PrivateTag._200D_xx02_:
            case PrivateTag._200D_xx03_:
            case PrivateTag._200D_xx04_:
            case PrivateTag._200D_xx05_:
            case PrivateTag._200D_xx06_:
            case PrivateTag._200D_xx07_:
            case PrivateTag._200D_xx08_:
            case PrivateTag._200D_xx09_:
            case PrivateTag._200D_xx0A_:
            case PrivateTag._200D_xx0B_:
            case PrivateTag._200D_xx0C_:
            case PrivateTag._200D_xx0D_:
            case PrivateTag._200D_xx10_:
            case PrivateTag._200D_xx11_:
            case PrivateTag._200D_xx12_:
            case PrivateTag._200D_xx13_:
            case PrivateTag._200D_xx14_:
            case PrivateTag._200D_xx15_:
                return VR.LO;
        }
        return VR.UN;
    }

}
