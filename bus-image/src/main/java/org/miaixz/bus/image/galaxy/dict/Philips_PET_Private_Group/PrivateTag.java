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
package org.miaixz.bus.image.galaxy.dict.Philips_PET_Private_Group;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Philips PET Private Group";

    /** (0511,xx00) VR=US VM=1 Private Data */
    public static final int PrivateData = 0x05110000;

    /** (7053,xx00) VR=DS VM=1 SUV Factor */
    public static final int SUVFactor = 0x70530000;

    /** (7053,xx03) VR=ST VM=1 Original File Name */
    public static final int OriginalFileName = 0x70530003;

    /** (7053,xx04) VR=OB VM=1 ? */
    public static final int _7053_xx04_ = 0x70530004;

    /** (7053,xx05) VR=LO VM=1 Worklist Info File Name */
    public static final int WorklistInfoFileName = 0x70530005;

    /** (7053,xx06) VR=OB VM=1 ? */
    public static final int _7053_xx06_ = 0x70530006;

    /** (7053,xx07) VR=SQ VM=1 ? */
    public static final int _7053_xx07_ = 0x70530007;

    /** (7053,xx08) VR=SQ VM=1 ? */
    public static final int _7053_xx08_ = 0x70530008;

    /**
     * (7053,xx09) VR=DS VM=1 Activity Concentration Scale Factor
     */
    public static final int ActivityConcentrationScaleFactor = 0x70530009;

    /** (7053,xx0F) VR=UL VM=1 ? */
    public static final int _7053_xx0F_ = 0x7053000F;

    /** (7053,xx10) VR=US VM=1 ? */
    public static final int _7053_xx10_ = 0x70530010;

    /** (7053,xx11) VR=US VM=1 ? */
    public static final int _7053_xx11_ = 0x70530011;

    /** (7053,xx12) VR=SQ VM=1 ? */
    public static final int _7053_xx12_ = 0x70530012;

    /** (7053,xx13) VR=SS VM=1 ? */
    public static final int _7053_xx13_ = 0x70530013;

    /** (7053,xx14) VR=SS VM=1 ? */
    public static final int _7053_xx14_ = 0x70530014;

    /** (7053,xx15) VR=SS VM=1 ? */
    public static final int _7053_xx15_ = 0x70530015;

    /** (7053,xx16) VR=SS VM=1 ? */
    public static final int _7053_xx16_ = 0x70530016;

    /** (7053,xx17) VR=SS VM=1 ? */
    public static final int _7053_xx17_ = 0x70530017;

    /** (7053,xx18) VR=SS VM=1 ? */
    public static final int _7053_xx18_ = 0x70530018;

    /** (7053,xxC2) VR=UI VM=1 ? */
    public static final int _7053_xxC2_ = 0x705300C2;

}
