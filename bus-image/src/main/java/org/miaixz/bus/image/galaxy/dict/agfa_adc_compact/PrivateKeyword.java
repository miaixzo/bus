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
package org.miaixz.bus.image.galaxy.dict.agfa_adc_compact;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.DataStreamFromCassette:
                return "DataStreamFromCassette";

            case PrivateTag.SetOfDestinationIds:
                return "SetOfDestinationIds";

            case PrivateTag.SetOfProcessingCodes:
                return "SetOfProcessingCodes";

            case PrivateTag.NumberOfSeriesInStudy:
                return "NumberOfSeriesInStudy";

            case PrivateTag.SessionNumber:
                return "SessionNumber";

            case PrivateTag.IDStationName:
                return "IDStationName";

            case PrivateTag.NumberOfImagesInSeries:
                return "NumberOfImagesInSeries";

            case PrivateTag.BreakCondition:
                return "BreakCondition";

            case PrivateTag.WaitOrHoldFlag:
                return "WaitOrHoldFlag";

            case PrivateTag.ScanResFlag:
                return "ScanResFlag";

            case PrivateTag.OperationCode:
                return "OperationCode";

            case PrivateTag.ImageQuality:
                return "ImageQuality";
        }
        return "";
    }

}
