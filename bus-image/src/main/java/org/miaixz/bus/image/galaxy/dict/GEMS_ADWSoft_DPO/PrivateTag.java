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
package org.miaixz.bus.image.galaxy.dict.GEMS_ADWSoft_DPO;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_ADWSoft_DPO";

    /** (0039,xx80) VR=IS VM=1 Private Entity Number */
    public static final int PrivateEntityNumber = 0x00390080;

    /** (0039,xx85) VR=DA VM=1 Private Entity Date */
    public static final int PrivateEntityDate = 0x00390085;

    /** (0039,xx90) VR=TM VM=1 Private Entity Time */
    public static final int PrivateEntityTime = 0x00390090;

    /** (0039,xx95) VR=LO VM=1 Private Entity Launch Command */
    public static final int PrivateEntityLaunchCommand = 0x00390095;

    /** (0039,xxAA) VR=CS VM=1 Private Entity Type */
    public static final int PrivateEntityType = 0x003900AA;

}
