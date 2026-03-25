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
package org.miaixz.bus.image.galaxy.dict.SPI_P_XSB_VISUB_Release_1;

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

            case PrivateTag._0029_xx02_:
            case PrivateTag._0029_xx0F_:
            case PrivateTag._0029_xx12_:
            case PrivateTag._0029_xx1F_:
            case PrivateTag._0029_xx22_:
            case PrivateTag._0029_xx2F_:
            case PrivateTag._0029_xx32_:
            case PrivateTag._0029_xx3F_:
                return VR.CS;

            case PrivateTag._0029_xx01_:
            case PrivateTag._0029_xx11_:
            case PrivateTag._0029_xx21_:
            case PrivateTag._0029_xx31_:
                return VR.DS;

            case PrivateTag._0019_xx20_:
            case PrivateTag._0019_xx50_:
            case PrivateTag._0029_xx00_:
            case PrivateTag._0029_xx10_:
            case PrivateTag._0029_xx20_:
            case PrivateTag._0029_xx30_:
                return VR.LO;
        }
        return VR.UN;
    }

}
