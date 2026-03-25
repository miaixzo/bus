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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MED";

    /** (0009,xx10) VR=LO VM=1 Recognition Code */
    public static final int RecognitionCode = 0x00090010;

    /** (0009,xx30) VR=US VM=1 Byte Offset of Original Header */
    public static final int ByteOffsetOfOriginalHeader = 0x00090030;

    /** (0009,xx31) VR=UL VM=1 Length of Original Header */
    public static final int LengthOfOriginalHeader = 0x00090031;

    /** (0009,xx40) VR=US VM=1 Byte Offset of Pixelmatrix */
    public static final int ByteOffsetOfPixelmatrix = 0x00090040;

    /** (0009,xx41) VR=UL VM=1 Length of Pixelmatrix In Bytes */
    public static final int LengthOfPixelmatrixInBytes = 0x00090041;

    /** (0009,xx50) VR=LO VM=1 ? */
    public static final int _0009_xx50_ = 0x00090050;

    /** (0009,xx51) VR=LO VM=1 ? */
    public static final int _0009_xx51_ = 0x00090051;

    /** (0009,xxF5) VR=LO VM=1 PDM EFID Placeholder */
    public static final int PDMEFIDPlaceholder = 0x000900F5;

    /** (0009,xxF6) VR=LO VM=1 PDM Data Object Type Extension */
    public static final int PDMDataObjectTypeExtension = 0x000900F6;

    /** (0021,xx10) VR=DS VM=1 Zoom */
    public static final int Zoom = 0x00210010;

    /** (0021,xx11) VR=DS VM=2 Target */
    public static final int Target = 0x00210011;

    /** (0021,xx12) VR=IS VM=1 Tube Angle */
    public static final int TubeAngle = 0x00210012;

    /** (0021,xx20) VR=US VM=1 ROI Mask */
    public static final int ROIMask = 0x00210020;

    /** (7001,xx10) VR=LO VM=1 Dummy */
    public static final int Dummy = 0x70010010;

    /** (7003,xx10) VR=LO VM=1 Header */
    public static final int Header = 0x70030010;

}
