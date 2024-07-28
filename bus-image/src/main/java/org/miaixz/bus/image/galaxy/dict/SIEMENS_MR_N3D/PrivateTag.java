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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_N3D;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR N3D";

    /** (0021,xx30) VR=SQ VM=1 Background Color DR Sequence */
    public static final int BackgroundColorDRSequence = 0x00210030;

    /** (0021,xx31) VR=DS VM=3 Background Color */
    public static final int BackgroundColor = 0x00210031;

    /** (0021,xx36) VR=SQ VM=1 Field Map DR Sequence */
    public static final int FieldMapDRSequence = 0x00210036;

    /** (0021,xx37) VR=CS VM=1 Visible */
    public static final int Visible = 0x00210037;

    /** (0021,xx38) VR=DS VM=3 Tinting Color */
    public static final int TintingColor = 0x00210038;

    /** (0021,xx39) VR=CS VM=1 Tinting Enabled */
    public static final int TintingEnabled = 0x00210039;

    /** (0021,xx3A) VR=LO VM=1 Volume ID */
    public static final int VolumeID = 0x0021003A;

    /** (0021,xx3B) VR=LO VM=1 Volume ID As Bound */
    public static final int VolumeIDAsBound = 0x0021003B;

    /** (0021,xx41) VR=SQ VM=1 Floating MPR Color LUT DR Sequence */
    public static final int FloatingMPRColorLUTDRSequence = 0x00210041;

    /** (0021,xx42) VR=DS VM=1 RGBA LUT */
    public static final int RGBALUT = 0x00210042;

    /** (0021,xx43) VR=DS VM=1 Blend Factor */
    public static final int BlendFactor = 0x00210043;

    /** (0021,xx44) VR=SQ VM=1 RGBA LUT Data Sequence */
    public static final int RGBALUTDataSequence = 0x00210044;

    /** (0021,xx45) VR=OB VM=1 Color LUT */
    public static final int ColorLUT = 0x00210045;

    /** (0021,xx4A) VR=SQ VM=1 Ortho MPR ColorLUT DR Sequence */
    public static final int OrthoMPRColorLUTDRSequence = 0x0021004A;

    /** (0021,xx4B) VR=SQ VM=1 VRT Color LUT DR Sequence */
    public static final int VRTColorLUTDRSequence = 0x0021004B;

    /** (0021,xx4D) VR=SQ VM=1 Pwl Transfer Function Data Sequence */
    public static final int PwlTransferFunctionDataSequence = 0x0021004D;

    /** (0021,xx4C) VR=SQ VM=1 Pwl Transfer Function Sequence */
    public static final int PwlTransferFunctionSequence = 0x0021004C;

    /** (0021,xx4E) VR=DS VM=1 Pwl Vertex Index */
    public static final int PwlVertexIndex = 0x0021004E;

    /** (0021,xx50) VR=SQ VM=1 Pwl Vertex Sequence */
    public static final int PwlVertexSequence = 0x00210050;

    /** (0021,xx51) VR=SQ VM=1 Floating MPR Render DR Sequence */
    public static final int FloatingMPRRenderDRSequence = 0x00210051;

    /** (0021,xx52) VR=CS VM=1 Primary Show Hide */
    public static final int PrimaryShowHide = 0x00210052;

    /** (0021,xx56) VR=CS VM=1 Alpha Dependent Fieldmap */
    public static final int AlphaDependentFieldmap = 0x00210056;

    /** (0021,xx57) VR=LO VM=1 Volume Filter */
    public static final int VolumeFilter = 0x00210057;

    /** (0021,xx5A) VR=SQ VM=1 Ortho MPR Render DR Sequence */
    public static final int OrthoMPRRenderDRSequence = 0x0021005A;

    /** (0021,xx5B) VR=SQ VM=1 VRT Render DR Sequence */
    public static final int VRTRenderDRSequence = 0x0021005B;

    /** (0021,xx53) VR=CS VM=1 Secondary Show Hide */
    public static final int SecondaryShowHide = 0x00210053;

    /** (0021,xx54) VR=LO VM=1 Primary Shading Index */
    public static final int PrimaryShadingIndex = 0x00210054;

    /** (0021,xx55) VR=LO VM=1 Secondary Shading Index */
    public static final int SecondaryShadingIndex = 0x00210055;

    /** (0021,xx58) VR=DS VM=3 Bounding Box Color */
    public static final int BoundingBoxColor = 0x00210058;

    /** (0021,xx59) VR=CS VM=1 Scene Interaction On */
    public static final int SceneInteractionOn = 0x00210059;

    /** (0021,xx60) VR=SQ VM=6 Clip Plane DR Sequence */
    public static final int ClipPlaneDRSequence = 0x00210060;

    /** (0021,xx61) VR=DS VM=3 Plane Center */
    public static final int PlaneCenter = 0x00210061;

    /** (0021,xx62) VR=DS VM=3 Plane Normal */
    public static final int PlaneNormal = 0x00210062;

    /** (0021,xx63) VR=DS VM=2 Plane Scale */
    public static final int PlaneScale = 0x00210063;

    /** (0021,xx64) VR=CS VM=1 Plane Enable GL Clip */
    public static final int PlaneEnableGLClip = 0x00210064;

    /** (0021,xx65) VR=DS VM=1 Plane Handle Ratio */
    public static final int PlaneHandleRatio = 0x00210065;

    /** (0021,xx66) VR=DS VM=24 Plane Bounding Points */
    public static final int PlaneBoundingPoints = 0x00210066;

    /** (0021,xx67) VR=DS VM=16 Plane Motion Matrix */
    public static final int PlaneMotionMatrix = 0x00210067;

    /** (0021,xx68) VR=DS VM=1 Plane Shift Velocity */
    public static final int PlaneShiftVelocity = 0x00210068;

    /** (0021,xx69) VR=CS VM=1 Plane Enabled */
    public static final int PlaneEnabled = 0x00210069;

    /** (0021,xx6A) VR=DS VM=1 Plane Rotate Velocity */
    public static final int PlaneRotateVelocity = 0x0021006A;

    /** (0021,xx6B) VR=CS VM=1 Plane Show Graphics */
    public static final int PlaneShowGraphics = 0x0021006B;

    /** (0021,xx73) VR=CS VM=1 Plane Single Selection Mode */
    public static final int PlaneSingleSelectionMode = 0x00210073;

    /** (0021,xx74) VR=CS VM=1 Plane Alignment */
    public static final int PlaneAlignment = 0x00210074;

    /** (0021,xx75) VR=CS VM=1 Plane Selected */
    public static final int PlaneSelected = 0x00210075;

    /** (0021,xx70) VR=SQ VM=1 Split Plane DR Sequence */
    public static final int SplitPlaneDRSequence = 0x00210070;

    /** (0021,xx71) VR=SQ VM=3 Floating MPR DR Sequence */
    public static final int FloatingMPRDRSequence = 0x00210071;

    /** (0021,xx6E) VR=CS VM=1 Plane MPR Locked */
    public static final int PlaneMPRLocked = 0x0021006E;

    /** (0021,xx6F) VR=CS VM=1 Plane Scaling Disabled */
    public static final int PlaneScalingDisabled = 0x0021006F;

    /** (0021,xx72) VR=SQ VM=3 Ortho MPR DR Sequence */
    public static final int OrthoMPRDRSequence = 0x00210072;

    /** (0021,xx6C) VR=DS VM=1 Offset */
    public static final int Offset = 0x0021006C;

    /** (0021,xx6D) VR=CS VM=1 Ortho MPR At Bounding Box */
    public static final int OrthoMPRAtBoundingBox = 0x0021006D;

    /** (0021,xx76) VR=SQ VM=1 Clustering DR Sequence */
    public static final int ClusteringDRSequence = 0x00210076;

    /** (0021,xx77) VR=DS VM=1 Cluster Size */
    public static final int ClusterSize = 0x00210077;

    /** (0021,xx78) VR=CS VM=1 Clustering Enabled */
    public static final int ClusteringEnabled = 0x00210078;

    /** (0021,xx79) VR=LO VM=1 Cluster Mask Vol ID */
    public static final int ClusterMaskVolID = 0x00210079;

    /** (0021,xx80) VR=SQ VM=1 Head Masking DR Sequence */
    public static final int HeadMaskingDRSequence = 0x00210080;

    /** (0021,xx81) VR=DS VM=2 Masking Range */
    public static final int MaskingRange = 0x00210081;

    /** (0021,xx82) VR=CS VM=1 Mask Enabled */
    public static final int MaskEnabled = 0x00210082;

    /** (0021,xx83) VR=SQ VM=1 Brain Masking DR Sequence */
    public static final int BrainMaskingDRSequence = 0x00210083;

    /** (0021,xx84) VR=SQ VM=1 Masking Status DR Sequence */
    public static final int MaskingStatusDRSequence = 0x00210084;

    /** (0021,xx85) VR=SQ VM=1 VRT Masking DR Sequence */
    public static final int VRTMaskingDRSequence = 0x00210085;

    /** (0021,xx86) VR=SQ VM=1 Ortho MPR Masking DR Sequence */
    public static final int OrthoMPRMaskingDRSequence = 0x00210086;

    /** (0021,xx87) VR=SQ VM=1 Floating MPR Masking DR Sequence */
    public static final int FloatingMPRMaskingDRSequence = 0x00210087;

    /** (0021,xx88) VR=SQ VM=1 Align DR Sequence */
    public static final int AlignDRSequence = 0x00210088;

    /** (0021,xx89) VR=DS VM=16 Registration Matrix */
    public static final int RegistrationMatrix = 0x00210089;

    /** (0021,xx90) VR=SQ VM=1 Functional Evaluation DR Sequence */
    public static final int FunctionalEvaluationDRSequence = 0x00210090;

    /** (0021,xx91) VR=DS VM=1-n Frame Acquition Numbers */
    public static final int FrameAcquitionNumbers = 0x00210091;

    /** (0021,xx92) VR=CS VM=1 Show Cursor */
    public static final int ShowCursor = 0x00210092;

    /** (0021,xx93) VR=DS VM=1 Current Frame */
    public static final int CurrentFrame = 0x00210093;

    /** (0021,xx94) VR=DS VM=1-n Plot Area */
    public static final int PlotArea = 0x00210094;

    /** (0021,xx95) VR=DS VM=2 Plot Text Position */
    public static final int PlotTextPosition = 0x00210095;

    /** (0021,xx96) VR=DS VM=1-n Base Line Points */
    public static final int BaseLinePoints = 0x00210096;

    /** (0021,xx97) VR=DS VM=1-n Active Points */
    public static final int ActivePoints = 0x00210097;

    /** (0021,xx98) VR=CS VM=1 Show Label */
    public static final int ShowLabel = 0x00210098;

    /** (0021,xx99) VR=CS VM=1 Mean Plot */
    public static final int MeanPlot = 0x00210099;

    /** (0021,xx9A) VR=CS VM=1 Motion Plot */
    public static final int MotionPlot = 0x0021009A;

    /** (0021,xx9B) VR=CS VM=1 Activate Normallized Curve */
    public static final int ActivateNormallizedCurve = 0x0021009B;

    /** (0021,xx9C) VR=DS VM=1 Plot Size */
    public static final int PlotSize = 0x0021009C;

    /** (0021,xxA0) VR=SQ VM=4 PlotDR Sequence */
    public static final int PlotDRSequence = 0x002100A0;

    /** (0021,xxA1) VR=LO VM=1 Title */
    public static final int Title = 0x002100A1;

    /** (0021,xxA2) VR=CS VM=1 Auto Scale */
    public static final int AutoScale = 0x002100A2;

    /** (0021,xxA3) VR=DS VM=1 Fixed Scale */
    public static final int FixedScale = 0x002100A3;

    /** (0021,xxA5) VR=LO VM=1 Label X */
    public static final int LabelX = 0x002100A5;

    /** (0021,xxA6) VR=LO VM=1 Label Y */
    public static final int LabelY = 0x002100A6;

    /** (0021,xxA7) VR=CS VM=1 Legend */
    public static final int Legend = 0x002100A7;

    /** (0021,xxA8) VR=CS VM=1 Scroll Bar X */
    public static final int ScrollBarX = 0x002100A8;

    /** (0021,xxA9) VR=CS VM=1 Scroll Bar Y */
    public static final int ScrollBarY = 0x002100A9;

    /** (0021,xxAA) VR=LO VM=1 Connect Scroll X */
    public static final int ConnectScrollX = 0x002100AA;

    /** (0021,xxAB) VR=LO VM=1 Plot ID */
    public static final int PlotID = 0x002100AB;

    /** (0021,xxAC) VR=DS VM=1 Plot Position */
    public static final int PlotPosition = 0x002100AC;

    /** (0021,xxB0) VR=SQ VM=1 Curve DR Sequence */
    public static final int CurveDRSequence = 0x002100B0;

    /** (0021,xxB1) VR=LO VM=1 Curve ID */
    public static final int CurveID = 0x002100B1;

    /** (0021,xxB2) VR=LO VM=1 Plot Type */
    public static final int PlotType = 0x002100B2;

    /** (0021,xxB3) VR=DS VM=1-n Curve Values */
    public static final int CurveValues = 0x002100B3;

    /** (0021,xxB4) VR=DS VM=3 Line Color */
    public static final int LineColor = 0x002100B4;

    /** (0021,xxB5) VR=DS VM=3 Marker Color */
    public static final int MarkerColor = 0x002100B5;

    /** (0021,xxB6) VR=CS VM=1 Line Filled */
    public static final int LineFilled = 0x002100B6;

    /** (0021,xxB7) VR=LO VM=1 Label */
    public static final int Label = 0x002100B7;

    /** (0021,xxB8) VR=CS VM=1 Show Marker */
    public static final int ShowMarker = 0x002100B8;

    /** (0021,xxB9) VR=LO VM=1 Marker Shape */
    public static final int MarkerShape = 0x002100B9;

    /** (0021,xxBA) VR=LO VM=1 Smoothing Algo */
    public static final int SmoothingAlgo = 0x002100BA;

    /** (0021,xxBB) VR=DS VM=1 Marker Size */
    public static final int MarkerSize = 0x002100BB;

    /** (0021,xxBC) VR=LO VM=1 Line Style */
    public static final int LineStyle = 0x002100BC;

    /** (0021,xxBD) VR=LO VM=1 Line Pattern */
    public static final int LinePattern = 0x002100BD;

    /** (0021,xxBE) VR=DS VM=1 Line Width */
    public static final int LineWidth = 0x002100BE;

    /** (0021,xxC0) VR=SQ VM=1 VRT Filter DR Sequence */
    public static final int VRTFilterDRSequence = 0x002100C0;

    /** (0021,xxC1) VR=LO VM=1 Filter Type */
    public static final int FilterType = 0x002100C1;

    /** (0021,xxC2) VR=CS VM=1 Current Active Plane */
    public static final int CurrentActivePlane = 0x002100C2;

}
