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
package org.miaixz.bus.image.galaxy.dict.Applicare_Print_Version_5_1;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Applicare/Print/Version 5.1";

    /** (4101,xx01) VR=UL VM=1 Mask State */
    public static final int MaskState = 0x41010001;

    /** (4101,xx02) VR=SQ VM=1 Annotations */
    public static final int Annotations = 0x41010002;

    /** (4101,xx03) VR=LO VM=1 Font */
    public static final int Font = 0x41010003;

    /** (4101,xx04) VR=UL VM=1 Font Size */
    public static final int FontSize = 0x41010004;

    /** (4101,xx05) VR=FD VM=1 Font Relative Size */
    public static final int FontRelativeSize = 0x41010005;

    /** (4101,xx06) VR=US VM=1 Overlay */
    public static final int Overlay = 0x41010006;

    /** (4101,xx07) VR=US VM=1 Pixel Rep */
    public static final int PixelRep = 0x41010007;

    /** (4101,xx08) VR=US VM=1 Annotation Level */
    public static final int AnnotationLevel = 0x41010008;

    /** (4101,xx09) VR=US VM=1 Show Caliper */
    public static final int ShowCaliper = 0x41010009;

}
