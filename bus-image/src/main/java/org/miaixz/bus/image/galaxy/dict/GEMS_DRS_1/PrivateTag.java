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
package org.miaixz.bus.image.galaxy.dict.GEMS_DRS_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_DRS_1";

    /** (0037,xx10) VR=LO VM=1 ReferringDepartment */
    public static final int ReferringDepartment = 0x00370010;

    /** (0037,xx20) VR=US VM=1 ScreenNumber */
    public static final int ScreenNumber = 0x00370020;

    /** (0037,xx40) VR=SH VM=1 LeftOrientation */
    public static final int LeftOrientation = 0x00370040;

    /** (0037,xx42) VR=SH VM=1 RightOrientation */
    public static final int RightOrientation = 0x00370042;

    /** (0037,xx50) VR=CS VM=1 Inversion */
    public static final int Inversion = 0x00370050;

    /** (0037,xx60) VR=US VM=1 DSA */
    public static final int DSA = 0x00370060;

}
