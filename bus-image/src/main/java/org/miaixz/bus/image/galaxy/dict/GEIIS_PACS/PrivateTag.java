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
package org.miaixz.bus.image.galaxy.dict.GEIIS_PACS;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEIIS PACS";

    /** (0903,xx10) VR=US VM=1 Reject Image Flag */
    public static final int RejectImageFlag = 0x09030010;

    /** (0903,xx11) VR=US VM=1 Significant Flag */
    public static final int SignificantFlag = 0x09030011;

    /** (0903,xx12) VR=US VM=1 Confidential Flag */
    public static final int ConfidentialFlag = 0x09030012;

    /** (0903,xx20) VR=CS VM=1 ? */
    public static final int _0903_xx20_ = 0x09030020;

    /** (0907,xx21) VR=US VM=1 Prefetch Algorithm */
    public static final int PrefetchAlgorithm = 0x09070021;

    /** (0907,xx22) VR=US VM=1 Limit Recent Studies */
    public static final int LimitRecentStudies = 0x09070022;

    /** (0907,xx23) VR=US VM=1 Limit Oldest Studies */
    public static final int LimitOldestStudies = 0x09070023;

    /** (0907,xx24) VR=US VM=1 Limit Recent Months */
    public static final int LimitRecentMonths = 0x09070024;

    /** (0907,xx31) VR=UI VM=1-n Exclude Study UIDs */
    public static final int ExcludeStudyUIDs = 0x09070031;

}
