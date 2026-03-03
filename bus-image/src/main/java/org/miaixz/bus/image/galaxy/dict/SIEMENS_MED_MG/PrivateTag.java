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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_MG;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MED MG";

    /** (0029,xx10) VR=US VM=1 List of Group Numbers */
    public static final int ListOfGroupNumbers = 0x00290010;

    /** (0029,xx15) VR=LO VM=1 List of Shadow Owner Codes */
    public static final int ListOfShadowOwnerCodes = 0x00290015;

    /** (0029,xx20) VR=US VM=1 List of Element Numbers */
    public static final int ListOfElementNumbers = 0x00290020;

    /** (0029,xx30) VR=US VM=1 List of Total Display Length */
    public static final int ListOfTotalDisplayLength = 0x00290030;

    /** (0029,xx40) VR=LO VM=1-n List of Display Prefix */
    public static final int ListOfDisplayPrefix = 0x00290040;

    /** (0029,xx50) VR=LO VM=1-n List of Display Postfix */
    public static final int ListOfDisplayPostfix = 0x00290050;

    /** (0029,xx60) VR=US VM=1 List of Text Position */
    public static final int ListOfTextPosition = 0x00290060;

    /** (0029,xx70) VR=LO VM=1 List of Text Concatenation */
    public static final int ListOfTextConcatenation = 0x00290070;

}
