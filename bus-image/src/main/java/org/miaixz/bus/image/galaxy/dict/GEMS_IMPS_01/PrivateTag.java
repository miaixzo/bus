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
package org.miaixz.bus.image.galaxy.dict.GEMS_IMPS_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_IMPS_01";

    /** (0029,xx04) VR=SL VM=1 Lower Range Of Pixels */
    public static final int LowerRangeOfPixels = 0x00290004;

    /** (0029,xx15) VR=SL VM=1 Lower Range Of Pixels1 */
    public static final int LowerRangeOfPixels1 = 0x00290015;

    /** (0029,xx16) VR=SL VM=1 Upper Range Of Pixels1 */
    public static final int UpperRangeOfPixels1 = 0x00290016;

    /** (0029,xx17) VR=SL VM=1 Lower Range Of Pixels2 */
    public static final int LowerRangeOfPixels2 = 0x00290017;

    /** (0029,xx18) VR=SL VM=1 Upper Range Of Pixels2 */
    public static final int UpperRangeOfPixels2 = 0x00290018;

    /** (0029,xx1A) VR=SL VM=1 Length Of Total Header In Bytes */
    public static final int LengthOfTotalHeaderInBytes = 0x0029001A;

    /** (0029,xx26) VR=SS VM=1 Version Of Header Structure */
    public static final int VersionOfHeaderStructure = 0x00290026;

    /** (0029,xx34) VR=SL VM=1 Advantage Comp Overflow */
    public static final int AdvantageCompOverflow = 0x00290034;

    /** (0029,xx35) VR=SL VM=1 Advantage Comp Underflow */
    public static final int AdvantageCompUnderflow = 0x00290035;

}
