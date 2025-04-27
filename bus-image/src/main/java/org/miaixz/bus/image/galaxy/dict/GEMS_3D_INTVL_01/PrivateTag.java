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
package org.miaixz.bus.image.galaxy.dict.GEMS_3D_INTVL_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_3D_INTVL_01";

    /** (0023,xx01) VR=SQ VM=1 X-Ray Marker Sequence */
    public static final int XRayMarkerSequence = 0x00230001;

    /** (0023,xx02) VR=SH VM=1 Marker ID */
    public static final int MarkerID = 0x00230002;

    /** (0023,xx03) VR=CS VM=1 Marker Type */
    public static final int MarkerType = 0x00230003;

    /** (0023,xx04) VR=FL VM=1 Marker Size */
    public static final int MarkerSize = 0x00230004;

    /** (0023,xx05) VR=US VM=3 Marker Color CIELab Value */
    public static final int MarkerColorCIELabValue = 0x00230005;

    /** (0023,xx06) VR=LO VM=1 Marker Label */
    public static final int MarkerLabel = 0x00230006;

    /** (0023,xx07) VR=CS VM=1 Marker Visible State */
    public static final int MarkerVisibleState = 0x00230007;

    /** (0023,xx08) VR=LO VM=1 Marker Description */
    public static final int MarkerDescription = 0x00230008;

    /** (0023,xx10) VR=SQ VM=1 Marker Points Sequence */
    public static final int MarkerPointsSequence = 0x00230010;

    /** (0023,xx11) VR=SH VM=1 Marker Point ID */
    public static final int MarkerPointID = 0x00230011;

    /** (0023,xx12) VR=FL VM=3 Marker Point Position */
    public static final int MarkerPointPosition = 0x00230012;

    /** (0023,xx13) VR=FL VM=1 Marker Point Size */
    public static final int MarkerPointSize = 0x00230013;

    /** (0023,xx14) VR=US VM=3 Marker Point Color CIELab Value */
    public static final int MarkerPointColorCIELabValue = 0x00230014;

    /** (0023,xx16) VR=CS VM=1 Marker Point Visible State */
    public static final int MarkerPointVisibleState = 0x00230016;

    /** (0023,xx17) VR=IS VM=1 Marker Point Order */
    public static final int MarkerPointOrder = 0x00230017;

    /** (0023,xx18) VR=FL VM=3 Volume Manual Registration */
    public static final int VolumeManualRegistration = 0x00230018;

    /** (0023,xx20) VR=IS VM=1-n Volumes Threshold */
    public static final int VolumesThreshold = 0x00230020;

    /** (0023,xx25) VR=CS VM=1 Cut Plane Activation Flag */
    public static final int CutPlaneActivationFlag = 0x00230025;

    /** (0023,xx26) VR=IS VM=1 Cut Plane Position Value */
    public static final int CutPlanePositionValue = 0x00230026;

    /** (0023,xx27) VR=FL VM=3 Cut Plane Normal Value */
    public static final int CutPlaneNormalValue = 0x00230027;

    /** (0023,xx28) VR=FL VM=1 Volume Scaling Factor */
    public static final int VolumeScalingFactor = 0x00230028;

    /** (0023,xx29) VR=FL VM=1 ROI to Table Top Distance */
    public static final int ROIToTableTopDistance = 0x00230029;

    /** (0023,xx30) VR=IS VM=1-n DRR Threshold */
    public static final int DRRThreshold = 0x00230030;

    /** (0023,xx31) VR=FL VM=3 Volume Table Position */
    public static final int VolumeTablePosition = 0x00230031;

    /** (0023,xx32) VR=IS VM=1 Rendering Mode */
    public static final int RenderingMode = 0x00230032;

    /** (0023,xx33) VR=IS VM=1 3D Object Opacity */
    public static final int ThreeDObjectOpacity = 0x00230033;

    /** (0023,xx34) VR=IS VM=1 Invert Image */
    public static final int InvertImage = 0x00230034;

    /** (0023,xx35) VR=IS VM=1 Enhance Full */
    public static final int EnhanceFull = 0x00230035;

    /** (0023,xx36) VR=FL VM=1 Zoom */
    public static final int Zoom = 0x00230036;

    /** (0023,xx37) VR=IS VM=2 Roam */
    public static final int Roam = 0x00230037;

    /** (0023,xx38) VR=IS VM=1 Window Level */
    public static final int WindowLevel = 0x00230038;

    /** (0023,xx39) VR=IS VM=1 Window Width */
    public static final int WindowWidth = 0x00230039;

    /** (0023,xx40) VR=CS VM=1 BMC Setting */
    public static final int BMCSetting = 0x00230040;

    /** (0023,xx41) VR=CS VM=1 Back View Setting */
    public static final int BackViewSetting = 0x00230041;

    /** (0023,xx42) VR=CS VM=1-n Sub Volume Visibility */
    public static final int SubVolumeVisibility = 0x00230042;

    /** (0023,xx43) VR=CS VM=1 3D Landmarks Visibility */
    public static final int ThreeDLandmarksVisibility = 0x00230043;

    /** (0023,xx44) VR=CS VM=1 Ablation Point Visibility */
    public static final int AblationPointVisibility = 0x00230044;

}
