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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_NM__Private;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.CurrentSegment:
                return "CurrentSegment";

            case PrivateTag.NumberOfSegments:
                return "NumberOfSegments";

            case PrivateTag.SegmentStartPosition:
                return "SegmentStartPosition";

            case PrivateTag.SegmentStopPosition:
                return "SegmentStopPosition";

            case PrivateTag.RelativeCOROffsetXDirection:
                return "RelativeCOROffsetXDirection";

            case PrivateTag.RelativeCOROffsetZDirection:
                return "RelativeCOROffsetZDirection";

            case PrivateTag.CurrentRotationNumber:
                return "CurrentRotationNumber";

            case PrivateTag.NumberOfRotations:
                return "NumberOfRotations";

            case PrivateTag.AlignmentTranslations:
                return "AlignmentTranslations";

            case PrivateTag.AlignmentRotations:
                return "AlignmentRotations";

            case PrivateTag.AlignmentTimestamp:
                return "AlignmentTimestamp";

            case PrivateTag.RelatedXraySeriesInstanceUID:
                return "RelatedXraySeriesInstanceUID";

            case PrivateTag._7051_xx25_:
                return "_7051_xx25_";

            case PrivateTag._7051_xx26_:
                return "_7051_xx26_";

            case PrivateTag._7051_xx27_:
                return "_7051_xx27_";

            case PrivateTag._7051_xx28_:
                return "_7051_xx28_";

            case PrivateTag._7051_xx29_:
                return "_7051_xx29_";
        }
        return "";
    }

}
