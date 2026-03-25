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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Release_1_1;

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

            case PrivateTag.ZoomEnableStatus:
            case PrivateTag.ZoomSelectStatus:
            case PrivateTag.MagnifyingGlassEnableStatus:
            case PrivateTag.MagnifyingGlassSelectStatus:
                return VR.CS;

            case PrivateTag.ZoomRectangle:
            case PrivateTag.ZoomFactor:
            case PrivateTag.MagnifyingGlassRectangle:
            case PrivateTag.MagnifyingGlassFactor:
                return VR.DS;

            case PrivateTag._0009_xxC0_:
            case PrivateTag._0009_xxC1_:
            case PrivateTag.ZoomID:
            case PrivateTag.MagnifyingGlassID:
                return VR.LT;

            case PrivateTag.SampleBitsAllocated:
            case PrivateTag.SampleBitsStored:
            case PrivateTag.SampleHighBit:
            case PrivateTag.SampleRepresentation:
            case PrivateTag.SampleBitsAllocated2:
            case PrivateTag.SampleBitsStored2:
            case PrivateTag.SampleHighBit2:
            case PrivateTag.SampleRepresentation2:
            case PrivateTag.ZoomFunction:
            case PrivateTag.MagnifyingGlassFunction:
                return VR.US;
        }
        return VR.UN;
    }

}
