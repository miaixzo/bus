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
package org.miaixz.bus.image.galaxy.dict.BioPri3D;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * Represents the PrivateElementDictionary type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateElementDictionary extends ElementDictionary {

    /**
     * The private creator value.
     */
    public static final String PrivateCreator = "";

    /**
     * Creates a new instance.
     */
    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    /**
     * Executes the keyword of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    /**
     * Executes the vr of operation.
     *
     * @param tag the tag.
     * @return the operation result.
     */
    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag._0011_xx39_:
            case PrivateTag._0011_xxE5_:
                return VR.CS;

            case PrivateTag._0011_xx24_:
            case PrivateTag._0063_xx0C_:
                return VR.DS;

            case PrivateTag._0011_xx30_:
                return VR.LO;

            case PrivateTag._0011_xxD0_:
                return VR.OB;

            case PrivateTag._0011_xx32_:
                return VR.SL;

            case PrivateTag._0063_xx35_:
                return VR.SQ;

            case PrivateTag._0011_xx20_:
            case PrivateTag._0011_xx31_:
            case PrivateTag._0011_xx3A_:
            case PrivateTag._0011_xxE0_:
            case PrivateTag._0011_xxE1_:
            case PrivateTag._0011_xxE2_:
            case PrivateTag._0063_xx21_:
                return VR.UL;

            case PrivateTag._0011_xxE3_:
            case PrivateTag._0011_xxE4_:
            case PrivateTag._0063_xx20_:
                return VR.US;
        }
        return VR.UN;
    }

}
