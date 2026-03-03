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
package org.miaixz.bus.image.galaxy.dict.GEMS_STDY_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_STDY_01";

    /** (0023,xx01) VR=SL VM=1 Number Of Series In Study */
    public static final int NumberOfSeriesInStudy = 0x00230001;

    /** (0023,xx02) VR=SL VM=1 Number Of Unarchived Series */
    public static final int NumberOfUnarchivedSeries = 0x00230002;

    /** (0023,xx10) VR=SS VM=1 Reference Image Field */
    public static final int ReferenceImageField = 0x00230010;

    /** (0023,xx50) VR=SS VM=1 Summary Image */
    public static final int SummaryImage = 0x00230050;

    /** (0023,xx70) VR=FD VM=1 Start Time Secs In First Axial */
    public static final int StartTimeSecsInFirstAxial = 0x00230070;

    /** (0023,xx74) VR=SL VM=1 Number Of Updates To Header */
    public static final int NumberOfUpdatesToHeader = 0x00230074;

    /**
     * (0023,xx7D) VR=SS VM=1 Indicates If Study Has Complete Info
     */
    public static final int IndicatesIfStudyHasCompleteInfo = 0x0023007D;

    /** (0023,xx80) VR=SQ VM=1 Has MPPS Related Tags */
    public static final int HasMPPSRelatedTags = 0x00230080;

}
