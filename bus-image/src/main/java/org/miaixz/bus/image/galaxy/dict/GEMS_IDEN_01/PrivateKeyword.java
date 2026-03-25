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

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.FullFidelity:
                return "FullFidelity";

            case PrivateTag.SuiteId:
                return "SuiteId";

            case PrivateTag.ProductId:
                return "ProductId";

            case PrivateTag._0009_xx17_:
                return "_0009_xx17_";

            case PrivateTag._0009_xx1A_:
                return "_0009_xx1A_";

            case PrivateTag._0009_xx20_:
                return "_0009_xx20_";

            case PrivateTag.ImageActualDate:
                return "ImageActualDate";

            case PrivateTag._0009_xx2F_:
                return "_0009_xx2F_";

            case PrivateTag.ServiceId:
                return "ServiceId";

            case PrivateTag.MobileLocationNumber:
                return "MobileLocationNumber";

            case PrivateTag._0009_xxE2_:
                return "_0009_xxE2_";

            case PrivateTag.EquipmentUID:
                return "EquipmentUID";

            case PrivateTag.GenesisVersionNow:
                return "GenesisVersionNow";

            case PrivateTag.ExamRecordChecksum:
                return "ExamRecordChecksum";

            case PrivateTag.SeriesSuiteID:
                return "SeriesSuiteID";

            case PrivateTag.ActualSeriesDataTimeStamp:
                return "ActualSeriesDataTimeStamp";
        }
        return "";
    }

}
