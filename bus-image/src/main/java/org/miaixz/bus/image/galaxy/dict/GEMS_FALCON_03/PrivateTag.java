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
package org.miaixz.bus.image.galaxy.dict.GEMS_FALCON_03;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_FALCON_03";

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
