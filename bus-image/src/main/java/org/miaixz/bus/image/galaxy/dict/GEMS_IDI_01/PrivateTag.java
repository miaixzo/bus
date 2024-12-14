/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.GEMS_IDI_01;

/**
 * @author Kimi Liu
 * @since Java 17+
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
