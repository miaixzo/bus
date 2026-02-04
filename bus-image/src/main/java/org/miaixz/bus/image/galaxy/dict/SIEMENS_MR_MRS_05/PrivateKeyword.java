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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_MRS_05;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.TransmitterReferenceAmplitude:
                return "TransmitterReferenceAmplitude";

            case PrivateTag.HammingFilterWidth:
                return "HammingFilterWidth";

            case PrivateTag.CSIGridshiftVector:
                return "CSIGridshiftVector";

            case PrivateTag.MixingTime:
                return "MixingTime";

            case PrivateTag.SeriesProtocolInstance:
                return "SeriesProtocolInstance";

            case PrivateTag.SpectroResultType:
                return "SpectroResultType";

            case PrivateTag.SpectroResultExtendType:
                return "SpectroResultExtendType";

            case PrivateTag.PostProcProtocol:
                return "PostProcProtocol";

            case PrivateTag.RescanLevel:
                return "RescanLevel";

            case PrivateTag.SpectroAlgoResult:
                return "SpectroAlgoResult";

            case PrivateTag.SpectroDisplayParams:
                return "SpectroDisplayParams";

            case PrivateTag.VoxelNumber:
                return "VoxelNumber";

            case PrivateTag.APRSequence:
                return "APRSequence";

            case PrivateTag.SyncData:
                return "SyncData";

            case PrivateTag.PostProcDetailedProtocol:
                return "PostProcDetailedProtocol";

            case PrivateTag.SpectroResultExtendTypeDetailed:
                return "SpectroResultExtendTypeDetailed";
        }
        return "";
    }

}
