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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_FRAME_SET;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO FRAME SET";

    /** (0029,xx10) VR=SQ VM=1 Image Frame Sequence */
    public static final int ImageFrameSequence = 0x00290010;

    /** (0029,xx12) VR=CS VM=1 Type of Progression */
    public static final int TypeOfProgression = 0x00290012;

    /** (0029,xx14) VR=IS VM=1 Representation Level */
    public static final int RepresentationLevel = 0x00290014;

    /**
     * (0029,xx16) VR=SQ VM=1 Representation Information Sequenc
     */
    public static final int RepresentationInformationSequence = 0x00290016;

    /** (0029,xx18) VR=IS VM=1 Number of Representations */
    public static final int NumberOfRepresentations = 0x00290018;

    /** (0029,xx20) VR=IS VM=1 Representation Pixel Offse */
    public static final int RepresentationPixelOffset = 0x00290020;

}
