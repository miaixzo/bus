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
package org.miaixz.bus.image.galaxy.dict.GEIIS;

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

            case PrivateTag._004B_xx13_:
            case PrivateTag._0009_xx12_:
                return VR.IS;

            case PrivateTag.AssigningAuthorityForPatientID:
                return VR.LO;

            case PrivateTag._004B_xx15_:
                return VR.LT;

            case PrivateTag.GEPrivateImageThumbnailSequence:
                return VR.SQ;

            case PrivateTag.OriginalStudyInstanceUID:
            case PrivateTag.OriginalSeriesInstanceUID:
            case PrivateTag.OriginalSOPInstanceUID:
                return VR.UI;

            case PrivateTag.ShiftCount:
            case PrivateTag.Offset:
            case PrivateTag.ActualFrameNumber:
            case PrivateTag.CompressionType:
            case PrivateTag.MultiframeOffsets:
            case PrivateTag.MultiResolutionLevels:
            case PrivateTag.SubbandRows:
            case PrivateTag.SubbandColumns:
            case PrivateTag.SubbandBytecounts:
                return VR.UL;
        }
        return VR.UN;
    }

}
