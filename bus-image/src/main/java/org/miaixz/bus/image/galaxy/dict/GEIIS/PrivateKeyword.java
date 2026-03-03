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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag._004B_xx13_:
                return "_004B_xx13_";

            case PrivateTag._004B_xx15_:
                return "_004B_xx15_";

            case PrivateTag.GEPrivateImageThumbnailSequence:
                return "GEPrivateImageThumbnailSequence";

            case PrivateTag._0009_xx12_:
                return "_0009_xx12_";

            case PrivateTag.ShiftCount:
                return "ShiftCount";

            case PrivateTag.Offset:
                return "Offset";

            case PrivateTag.ActualFrameNumber:
                return "ActualFrameNumber";

            case PrivateTag.AssigningAuthorityForPatientID:
                return "AssigningAuthorityForPatientID";

            case PrivateTag.OriginalStudyInstanceUID:
                return "OriginalStudyInstanceUID";

            case PrivateTag.OriginalSeriesInstanceUID:
                return "OriginalSeriesInstanceUID";

            case PrivateTag.OriginalSOPInstanceUID:
                return "OriginalSOPInstanceUID";

            case PrivateTag.CompressionType:
                return "CompressionType";

            case PrivateTag.MultiframeOffsets:
                return "MultiframeOffsets";

            case PrivateTag.MultiResolutionLevels:
                return "MultiResolutionLevels";

            case PrivateTag.SubbandRows:
                return "SubbandRows";

            case PrivateTag.SubbandColumns:
                return "SubbandColumns";

            case PrivateTag.SubbandBytecounts:
                return "SubbandBytecounts";
        }
        return "";
    }

}
