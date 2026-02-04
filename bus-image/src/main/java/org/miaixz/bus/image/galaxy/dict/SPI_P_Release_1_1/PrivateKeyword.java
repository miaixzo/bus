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
package org.miaixz.bus.image.galaxy.dict.SPI_P_Release_1_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._0009_xxC0_:
                return "_0009_xxC0_";

            case PrivateTag._0009_xxC1_:
                return "_0009_xxC1_";

            case PrivateTag.PhysiologicalDataType:
                return "PhysiologicalDataType";

            case PrivateTag.PhysiologicalDataChannelAndKind:
                return "PhysiologicalDataChannelAndKind";

            case PrivateTag.SampleBitsAllocated:
                return "SampleBitsAllocated";

            case PrivateTag.SampleBitsStored:
                return "SampleBitsStored";

            case PrivateTag.SampleHighBit:
                return "SampleHighBit";

            case PrivateTag.SampleRepresentation:
                return "SampleRepresentation";

            case PrivateTag.SmallestSampleValue:
                return "SmallestSampleValue";

            case PrivateTag.LargestSampleValue:
                return "LargestSampleValue";

            case PrivateTag.NumberOfSamples:
                return "NumberOfSamples";

            case PrivateTag.SampleData:
                return "SampleData";

            case PrivateTag.SampleRate:
                return "SampleRate";

            case PrivateTag.PhysiologicalDataType2:
                return "PhysiologicalDataType2";

            case PrivateTag.PhysiologicalDataChannelAndKind2:
                return "PhysiologicalDataChannelAndKind2";

            case PrivateTag.SampleBitsAllocated2:
                return "SampleBitsAllocated2";

            case PrivateTag.SampleBitsStored2:
                return "SampleBitsStored2";

            case PrivateTag.SampleHighBit2:
                return "SampleHighBit2";

            case PrivateTag.SampleRepresentation2:
                return "SampleRepresentation2";

            case PrivateTag.SmallestSampleValue2:
                return "SmallestSampleValue2";

            case PrivateTag.LargestSampleValue2:
                return "LargestSampleValue2";

            case PrivateTag.NumberOfSamples2:
                return "NumberOfSamples2";

            case PrivateTag.SampleData2:
                return "SampleData2";

            case PrivateTag.SampleRate2:
                return "SampleRate2";

            case PrivateTag.ZoomID:
                return "ZoomID";

            case PrivateTag.ZoomRectangle:
                return "ZoomRectangle";

            case PrivateTag.ZoomFactor:
                return "ZoomFactor";

            case PrivateTag.ZoomFunction:
                return "ZoomFunction";

            case PrivateTag.ZoomEnableStatus:
                return "ZoomEnableStatus";

            case PrivateTag.ZoomSelectStatus:
                return "ZoomSelectStatus";

            case PrivateTag.MagnifyingGlassID:
                return "MagnifyingGlassID";

            case PrivateTag.MagnifyingGlassRectangle:
                return "MagnifyingGlassRectangle";

            case PrivateTag.MagnifyingGlassFactor:
                return "MagnifyingGlassFactor";

            case PrivateTag.MagnifyingGlassFunction:
                return "MagnifyingGlassFunction";

            case PrivateTag.MagnifyingGlassEnableStatus:
                return "MagnifyingGlassEnableStatus";

            case PrivateTag.MagnifyingGlassSelectStatus:
                return "MagnifyingGlassSelectStatus";
        }
        return "";
    }

}
