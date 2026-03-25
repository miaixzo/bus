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
package org.miaixz.bus.image.galaxy.dict.GEMS_IDI_01;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_IDI_01";

    /** (0073,xx20) VR=DS VM=1 Height Map Plane Distance */
    public static final int HeightMapPlaneDistance = 0x00730020;

    /** (0073,xx21) VR=DS VM=1 Height Map Plane Offset */
    public static final int HeightMapPlaneOffset = 0x00730021;

    /** (0073,xx30) VR=OW VM=1 Height Map Plane Indices */
    public static final int HeightMapPlaneIndices = 0x00730030;

    /** (0073,xx31) VR=OW VM=1 X Map Plane Indices */
    public static final int XMapPlaneIndices = 0x00730031;

    /** (0073,xx32) VR=OW VM=1 Y Map Plane Indices */
    public static final int YMapPlaneIndices = 0x00730032;

    /**
     * (0073,xx40) VR=DS VM=1 Central Projection Detector Secondary Angle
     */
    public static final int CentralProjectionDetectorSecondaryAngle = 0x00730040;

    /** (0073,xx50) VR=DS VM=2 Detector Active Dimensions */
    public static final int DetectorActiveDimensions = 0x00730050;

}
