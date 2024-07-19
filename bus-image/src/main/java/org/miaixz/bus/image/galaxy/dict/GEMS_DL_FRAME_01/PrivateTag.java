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
package org.miaixz.bus.image.galaxy.dict.GEMS_DL_FRAME_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_DL_FRAME_01";

    /** (0025,xx02) VR=IS VM=1 Frame ID */
    public static final int FrameID = 0x00250002;

    /** (0025,xx03) VR=DS VM=1 Distance Source to Detector */
    public static final int DistanceSourceToDetector = 0x00250003;

    /** (0025,xx04) VR=DS VM=1 Distance Source to Patient */
    public static final int DistanceSourceToPatient = 0x00250004;

    /** (0025,xx05) VR=DS VM=1 Distance Source to Skin */
    public static final int DistanceSourceToSkin = 0x00250005;

    /** (0025,xx06) VR=DS VM=1 Positioner Primary Angle */
    public static final int PositionerPrimaryAngle = 0x00250006;

    /** (0025,xx07) VR=DS VM=1 Positioner Secondary Angle */
    public static final int PositionerSecondaryAngle = 0x00250007;

    /** (0025,xx08) VR=IS VM=1 Beam Orientation */
    public static final int BeamOrientation = 0x00250008;

    /** (0025,xx09) VR=DS VM=1 L Arm Angle */
    public static final int LArmAngle = 0x00250009;

    /** (0025,xx0A) VR=SQ VM=1 Frame Sequence */
    public static final int FrameSequence = 0x0025000A;

    /** (0025,xx10) VR=DS VM=1 Pivot Angle */
    public static final int PivotAngle = 0x00250010;

    /** (0025,xx1A) VR=DS VM=1 Arc Angle */
    public static final int ArcAngle = 0x0025001A;

    /** (0025,xx1B) VR=DS VM=1 Table Vertical Position */
    public static final int TableVerticalPosition = 0x0025001B;

    /** (0025,xx1C) VR=DS VM=1 Table Longitudinal Position */
    public static final int TableLongitudinalPosition = 0x0025001C;

    /** (0025,xx1D) VR=DS VM=1 Table Lateral Position */
    public static final int TableLateralPosition = 0x0025001D;

    /** (0025,xx1E) VR=IS VM=1 Beam Cover Area */
    public static final int BeamCoverArea = 0x0025001E;

    /** (0025,xx1F) VR=DS VM=1 kVP Actual */
    public static final int kVPActual = 0x0025001F;

    /** (0025,xx20) VR=DS VM=1 mAS Actual */
    public static final int mASActual = 0x00250020;

    /** (0025,xx21) VR=DS VM=1 PW Actual */
    public static final int PWActual = 0x00250021;

    /** (0025,xx22) VR=DS VM=1 Kvp Commanded */
    public static final int KvpCommanded = 0x00250022;

    /** (0025,xx23) VR=DS VM=1 Mas Commanded */
    public static final int MasCommanded = 0x00250023;

    /** (0025,xx24) VR=DS VM=1 Pw Commanded */
    public static final int PwCommanded = 0x00250024;

    /** (0025,xx25) VR=CS VM=1 Grid */
    public static final int Grid = 0x00250025;

    /** (0025,xx26) VR=DS VM=1 Sensor Feedback */
    public static final int SensorFeedback = 0x00250026;

    /** (0025,xx27) VR=DS VM=1 Target Entrance Dose */
    public static final int TargetEntranceDose = 0x00250027;

    /** (0025,xx28) VR=DS VM=1 Cnr Commanded */
    public static final int CnrCommanded = 0x00250028;

    /** (0025,xx29) VR=DS VM=1 Contrast Commanded */
    public static final int ContrastCommanded = 0x00250029;

    /** (0025,xx2A) VR=DS VM=1 EPT Actual */
    public static final int EPTActual = 0x0025002A;

    /** (0025,xx2B) VR=IS VM=1 Spectral Filter Znb */
    public static final int SpectralFilterZnb = 0x0025002B;

    /** (0025,xx2C) VR=DS VM=1 Spectral Filter Weight */
    public static final int SpectralFilterWeight = 0x0025002C;

    /** (0025,xx2D) VR=DS VM=1 Spectral Filter Density */
    public static final int SpectralFilterDensity = 0x0025002D;

    /** (0025,xx2E) VR=IS VM=1 Spectral Filter Thickness */
    public static final int SpectralFilterThickness = 0x0025002E;

    /** (0025,xx2F) VR=IS VM=1 Spectral Filter Status */
    public static final int SpectralFilterStatus = 0x0025002F;

    /** (0025,xx30) VR=IS VM=2 FOV Dimension */
    public static final int FOVDimension = 0x00250030;

    /** (0025,xx33) VR=IS VM=2 FOV Origin */
    public static final int FOVOrigin = 0x00250033;

    /** (0025,xx34) VR=IS VM=1 Collimator Left Vertical Edge */
    public static final int CollimatorLeftVerticalEdge = 0x00250034;

    /** (0025,xx35) VR=IS VM=1 Collimator Right Vertical Edge */
    public static final int CollimatorRightVerticalEdge = 0x00250035;

    /** (0025,xx36) VR=IS VM=1 Collimator Up Horizontal Edge */
    public static final int CollimatorUpHorizontalEdge = 0x00250036;

    /** (0025,xx37) VR=IS VM=1 Collimator Low Horizontal Edge */
    public static final int CollimatorLowHorizontalEdge = 0x00250037;

    /** (0025,xx38) VR=IS VM=1 Vertices Polygonal Collimator */
    public static final int VerticesPolygonalCollimator = 0x00250038;

    /** (0025,xx39) VR=IS VM=1 Contour Filter Distance */
    public static final int ContourFilterDistance = 0x00250039;

    /** (0025,xx3A) VR=UL VM=1 Contour Filter Angle */
    public static final int ContourFilterAngle = 0x0025003A;

    /** (0025,xx3B) VR=CS VM=1 Table Rotation Status */
    public static final int TableRotationStatus = 0x0025003B;

    /** (0025,xx3C) VR=CS VM=1 Internal Label Frame */
    public static final int InternalLabelFrame = 0x0025003C;

}
