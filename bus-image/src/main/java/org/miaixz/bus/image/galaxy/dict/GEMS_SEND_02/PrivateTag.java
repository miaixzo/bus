/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.GEMS_SEND_02;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_SEND_02";

    /** (0045,xx55) VR=DS VM=8 A_Coefficients used in Multiresolution Algorithm */
    public static final int ACoefficients = 0x00450055;

    /** (0045,xx62) VR=IS VM=1 User Window Center */
    public static final int UserWindowCenter = 0x00450062;

    /** (0045,xx63) VR=IS VM=1 User Window Width */
    public static final int UserWindowWidth = 0x00450063;

    /** (0045,xx65) VR=IS VM=1 Requested Detector Entrance Dose */
    public static final int RequestedDetectorEntranceDose = 0x00450065;

    /** (0045,xx67) VR=DS VM=3 VOI LUT Assymmetry Parameter Beta */
    public static final int VOILUTAssymmetryParameterBeta = 0x00450067;

    /** (0045,xx69) VR=IS VM=1 Collimator Rotation */
    public static final int CollimatorRotation = 0x00450069;

    /** (0045,xx72) VR=IS VM=1 Collimator Width */
    public static final int CollimatorWidth = 0x00450072;

    /** (0045,xx73) VR=IS VM=1 Collimator Height */
    public static final int CollimatorHeight = 0x00450073;

}
