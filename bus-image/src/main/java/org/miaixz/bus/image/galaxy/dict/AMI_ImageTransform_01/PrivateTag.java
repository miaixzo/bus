/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.AMI_ImageTransform_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "AMI ImageTransform_01";

    /** (3107,xx10) VR=DS VM=1-n Transformation Matrix */
    public static final int TransformationMatrix = 0x31070010;

    /** (3107,xx20) VR=DS VM=1 Center Offset */
    public static final int CenterOffset = 0x31070020;

    /** (3107,xx30) VR=DS VM=1 Magnification */
    public static final int Magnification = 0x31070030;

    /** (3107,xx40) VR=CS VM=1 Magnification Type */
    public static final int MagnificationType = 0x31070040;

    /** (3107,xx50) VR=DS VM=1 Displayed Area */
    public static final int DisplayedArea = 0x31070050;

    /** (3107,xx60) VR=DS VM=1 Calibration Factor */
    public static final int CalibrationFactor = 0x31070060;

}
