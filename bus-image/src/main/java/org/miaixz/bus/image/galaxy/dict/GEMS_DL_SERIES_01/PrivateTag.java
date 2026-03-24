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
package org.miaixz.bus.image.galaxy.dict.GEMS_DL_SERIES_01;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_DL_SERIES_01";

    /** (0015,xx85) VR=LO VM=1 Series File Name */
    public static final int SeriesFileName = 0x00150085;

    /** (0015,xx87) VR=IS VM=1 Number of Images */
    public static final int NumberOfImages = 0x00150087;

    /** (0015,xx8C) VR=CS VM=1 Sent Flag */
    public static final int SentFlag = 0x0015008C;

    /** (0015,xx8D) VR=US VM=1 Item Locked */
    public static final int ItemLocked = 0x0015008D;

    /** (0019,xx4C) VR=CS VM=1 Internal Label */
    public static final int InternalLabel = 0x0019004C;

    /** (0019,xx4D) VR=CS VM=1 Browser Hide */
    public static final int BrowserHide = 0x0019004D;

}
