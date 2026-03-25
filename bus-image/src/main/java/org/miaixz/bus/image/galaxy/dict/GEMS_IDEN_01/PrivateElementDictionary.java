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
package org.miaixz.bus.image.galaxy.dict.GEMS_IDEN_01;

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

            case PrivateTag.FullFidelity:
                return VR.LO;

            case PrivateTag._0009_xx17_:
            case PrivateTag._0009_xxE2_:
                return VR.LT;

            case PrivateTag.SuiteId:
            case PrivateTag.ProductId:
            case PrivateTag._0009_xx2F_:
            case PrivateTag.ServiceId:
            case PrivateTag.MobileLocationNumber:
            case PrivateTag.GenesisVersionNow:
                return VR.SH;

            case PrivateTag.ImageActualDate:
            case PrivateTag.ActualSeriesDataTimeStamp:
                return VR.SL;

            case PrivateTag.EquipmentUID:
                return VR.UI;

            case PrivateTag.ExamRecordChecksum:
            case PrivateTag.SeriesSuiteID:
                return VR.UL;

            case PrivateTag._0009_xx1A_:
            case PrivateTag._0009_xx20_:
                return VR.US;
        }
        return VR.UN;
    }

}
