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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_MRS_05;

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

            case PrivateTag.SeriesProtocolInstance:
            case PrivateTag.SpectroResultType:
            case PrivateTag.SpectroResultExtendType:
            case PrivateTag.PostProcProtocol:
            case PrivateTag.RescanLevel:
            case PrivateTag.SyncData:
            case PrivateTag.PostProcDetailedProtocol:
            case PrivateTag.SpectroResultExtendTypeDetailed:
                return VR.CS;

            case PrivateTag.TransmitterReferenceAmplitude:
            case PrivateTag.CSIGridshiftVector:
            case PrivateTag.MixingTime:
                return VR.FD;

            case PrivateTag.VoxelNumber:
                return VR.IS;

            case PrivateTag.SpectroAlgoResult:
            case PrivateTag.SpectroDisplayParams:
                return VR.OF;

            case PrivateTag.APRSequence:
                return VR.SQ;

            case PrivateTag.HammingFilterWidth:
                return VR.US;
        }
        return VR.UN;
    }

}
