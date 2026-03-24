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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_DATAMAPPING_ATTRIBUTES;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR DATAMAPPING ATTRIBUTES";

    /** (0011,xx01) VR=ST VM=1 Reprocessing Info */
    public static final int ReprocessingInfo = 0x00110001;

    /** (0011,xx02) VR=CS VM=1-n Data Role Type */
    public static final int DataRoleType = 0x00110002;

    /** (0011,xx03) VR=ST VM=1 Data Role Name */
    public static final int DataRoleName = 0x00110003;

    /** (0011,xx04) VR=SL VM=1 Rescan Name */
    public static final int RescanName = 0x00110004;

    /** (0011,xx05) VR=FD VM=1 ? */
    public static final int _0011_xx05_ = 0x00110005;

    /** (0011,xx06) VR=ST VM=1 Cardiac Type Name */
    public static final int CardiacTypeName = 0x00110006;

    /** (0011,xx07) VR=ST VM=1 Cardiac Type Name L2 */
    public static final int CardiacTypeNameL2 = 0x00110007;

    /** (0011,xx08) VR=ST VM=1 Misc Indicator */
    public static final int MiscIndicator = 0x00110008;

    /** (0011,xx09) VR=SL VM=1 ? */
    public static final int _0011_xx09_ = 0x00110009;

    /** (0011,xx0A) VR=SL VM=1 ? */
    public static final int _0011_xx0A_ = 0x0011000A;

    /** (0011,xx0B) VR=DS VM=1 ? */
    public static final int _0011_xx0B_ = 0x0011000B;

    /** (0011,xx0C) VR=ST VM=1 Split Bagging Name */
    public static final int SplitBaggingName = 0x0011000C;

    /** (0011,xx0D) VR=ST VM=1 Split Sub Bagging Name */
    public static final int SplitSubBaggingName = 0x0011000D;

    /** (0011,xx0E) VR=ST VM=1 Stage Sub Bagging Name */
    public static final int StageSubBaggingName = 0x0011000E;

    /** (0011,xx0F) VR=ST VM=1 Is Internal Data Role */
    public static final int IsInternalDataRole = 0x0011000F;

}
