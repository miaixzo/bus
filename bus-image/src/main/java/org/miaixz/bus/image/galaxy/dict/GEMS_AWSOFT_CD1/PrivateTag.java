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
package org.miaixz.bus.image.galaxy.dict.GEMS_AWSOFT_CD1;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_AWSOFT_CD1";

    /** (0039,xx65) VR=UI VM=1 Reference to Study UID */
    public static final int ReferenceToStudyUID = 0x00390065;

    /** (0039,xx70) VR=UI VM=1 Reference to Series UID */
    public static final int ReferenceToSeriesUID = 0x00390070;

    /** (0039,xx75) VR=IS VM=1 Reference to Original Instance Number */
    public static final int ReferenceToOriginalInstance = 0x00390075;

    /** (0039,xx80) VR=IS VM=1 DPO Number */
    public static final int DPONumber = 0x00390080;

    /** (0039,xx85) VR=DA VM=1 DPO Date */
    public static final int DPODate = 0x00390085;

    /** (0039,xx90) VR=TM VM=1 DPO Time */
    public static final int DPOTime = 0x00390090;

    /** (0039,xx95) VR=LO VM=1 DPO Invocation String */
    public static final int DPOInvocationString = 0x00390095;

    /** (0039,xxAA) VR=CS VM=1 DPO Type */
    public static final int DPOType = 0x003900AA;

    /** (0039,xxFF) VR=OB VM=1 DPO Data */
    public static final int DPOData = 0x003900FF;

}
