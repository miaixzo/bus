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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_DISPLAY;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MED DISPLAY";

    /** (0029,xx04) VR=CS VM=1 Photometric Interpretation */
    public static final int PhotometricInterpretation = 0x00290004;

    /** (0029,xx10) VR=US VM=1 Rows of Submatrix */
    public static final int RowsOfSubmatrix = 0x00290010;

    /** (0029,xx11) VR=US VM=1 Columns of Submatrix */
    public static final int ColumnsOfSubmatrix = 0x00290011;

    /** (0029,xx20) VR=US VM=1 ? */
    public static final int _0029_xx20_ = 0x00290020;

    /** (0029,xx21) VR=US VM=1 ? */
    public static final int _0029_xx21_ = 0x00290021;

    /** (0029,xx50) VR=US VM=1 Origin of Submatrix */
    public static final int OriginOfSubmatrix = 0x00290050;

    /** (0029,xx80) VR=US VM=1 ? */
    public static final int _0029_xx80_ = 0x00290080;

    /** (0029,xx99) VR=LO VM=1 Shutter Type */
    public static final int ShutterType = 0x00290099;

    /** (0029,xxA0) VR=US VM=1 Rows of Rectangular Shutter */
    public static final int RowsOfRectangularShutter = 0x002900A0;

    /** (0029,xxA1) VR=US VM=1 Columns of Rectangular Shutter */
    public static final int ColumnsOfRectangularShutter = 0x002900A1;

    /** (0029,xxA2) VR=US VM=1 Origin of Rectangular Shutter */
    public static final int OriginOfRectangularShutter = 0x002900A2;

    /** (0029,xxB0) VR=US VM=1 Radius of Circular Shutter */
    public static final int RadiusOfCircularShutter = 0x002900B0;

    /** (0029,xxB2) VR=US VM=1 Origin of Circular Shutter */
    public static final int OriginOfCircularShutter = 0x002900B2;

    /** (0029,xxC1) VR=US VM=1 Contour of Irregular Shutter */
    public static final int ContourOfIrregularShutter = 0x002900C1;

}
