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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_ADVANCED_PRESENTATION;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS SYNGO ADVANCED PRESENTATION";

    /** (0029,xx00) VR=CS VM=1 Presentation Nam */
    public static final int PresentationName = 0x00290000;

    /** (0029,xx01) VR=CS VM=1 Presentation Typ */
    public static final int PresentationType = 0x00290001;

    /** (0029,xx02) VR=SQ VM=1 Advanced Presentation Sequenc */
    public static final int AdvancedPresentationSequence = 0x00290002;

    /** (0029,xx03) VR=SQ VM=1 Time Point Sequenc */
    public static final int TimePointSequence = 0x00290003;

    /** (0029,xx04) VR=SQ VM=1 Base Image Sequenc */
    public static final int BaseImageSequence = 0x00290004;

    /** (0029,xx05) VR=SQ VM=1 Overlay Image Sequenc */
    public static final int OverlayImageSequence = 0x00290005;

    /** (0029,xx06) VR=SQ VM=1 Registration Instance Sequenc */
    public static final int RegistrationInstanceSequence = 0x00290006;

    /**
     * (0029,xx07) VR=SQ VM=1 Real World Value Mapping Instance Sequenc
     */
    public static final int RealWorldValueMappingInstanceSequence = 0x00290007;

    /** (0029,xx08) VR=SQ VM=1 Measurement Sequenc */
    public static final int MeasurementSequence = 0x00290008;

    /** (0029,xx09) VR=UI VM=1 Measurement UI */
    public static final int MeasurementUID = 0x00290009;

    /** (0029,xx10) VR=SQ VM=1 Segmentation Sequence */
    public static final int SegmentationSequence = 0x00290010;

    /** (0029,xx11) VR=UI VM=1 Segmentation UI */
    public static final int SegmentationUID = 0x00290011;

    /** (0029,xx12) VR=SQ VM=1 Navigation Sequenc */
    public static final int NavigationSequence = 0x00290012;

    /** (0029,xx13) VR=CS VM=1 Navigation Nam */
    public static final int NavigationName = 0x00290013;

    /** (0029,xx14) VR=CS VM=1 Auto Navigation Directio */
    public static final int AutoNavigationDirection = 0x00290014;

    /** (0029,xx15) VR=DS VM=1 Auto Navigation Frame Rat */
    public static final int AutoNavigationFrameRate = 0x00290015;

    /** (0029,xx16) VR=CS VM=1 Auto Navigation Mod */
    public static final int AutoNavigationMode = 0x00290016;

    /** (0029,xx17) VR=DS VM=1 Auto Navigation Realtime Spee */
    public static final int AutoNavigationRealtimeSpeed = 0x00290017;

    /** (0029,xx18) VR=CS VM=1 Auto Navigation Strateg */
    public static final int AutoNavigationStrategy = 0x00290018;

    /** (0029,xx19) VR=CS VM=1 Auto Navigation Realtime Fla */
    public static final int AutoNavigationRealtimeFlag = 0x00290019;

    /** (0029,xx20) VR=IS VM=1 Index Navigation Current Inde */
    public static final int IndexNavigationCurrentIndex = 0x00290020;

    /**
     * (0029,xx21) VR=IS VM=1 Index Auto Navigation Skipping Degre
     */
    public static final int IndexAutoNavigationSkippingDegree = 0x00290021;

    /**
     * (0029,xx22) VR=DS VM=1 Volume Navigation Minimum Pixel Spacin
     */
    public static final int VolumeNavigationMinimumPixelSpacing = 0x00290022;

    /** (0029,xx23) VR=CS VM=1 Volume Navigation Scroll Uni */
    public static final int VolumeNavigationScrollUnit = 0x00290023;

    /** (0029,xx24) VR=DS VM=1 Volume Navigation Step Siz */
    public static final int VolumeNavigationStepSize = 0x00290024;

    /** (0029,xx25) VR=DS VM=1 Volume Navigation Jump Siz */
    public static final int VolumeNavigationJumpSize = 0x00290025;

    /** (0029,xx26) VR=IS VM=1 Referenced Registration Numbe */
    public static final int ReferencedRegistrationNumber = 0x00290026;

    /** (0029,xx27) VR=UI VM=1 Real World Value Mapping UI */
    public static final int RealWorldValueMappingUID = 0x00290027;

    /** (0029,xx28) VR=DS VM=1 Channel Alpha Value */
    public static final int ChannelAlphaValue = 0x00290028;

    /** (0029,xx30) VR=LO VM=1 Measurement Application Name */
    public static final int MeasurementApplicationName = 0x00290030;

    /** (0029,xx31) VR=SQ VM=1 Measurement Data Sequenc */
    public static final int MeasurementDataSequence = 0x00290031;

    /** (0029,xx32) VR=LO VM=1 Measurement Typ */
    public static final int MeasurementType = 0x00290032;

    /** (0029,xx33) VR=UI VM=1 Measurement Frame of Reference UI */
    public static final int MeasurementFrameOfReferenceUID = 0x00290033;

    /** (0029,xx34) VR=UI VM=1 Measurement UID */
    public static final int MeasurementUid = 0x00290034;

    /** (0029,xx35) VR=IS VM=1 Measurement Application Numbe */
    public static final int MeasurementApplicationNumber = 0x00290035;

    /**
     * (0029,xx36) VR=ST VM=1 Measurement Application Number Prefix Tex
     */
    public static final int MeasurementApplicationNumberPrefixText = 0x00290036;

    /** (0029,xx37) VR=CS VM=1 Measurement Graphic Is Visible Flag */
    public static final int MeasurementGraphicIsVisibleFlag = 0x00290037;

    /** (0029,xx38) VR=UI VM=4 Referenced Syngo UID */
    public static final int ReferencedSyngoUID = 0x00290038;

    /** (0029,xx39) VR=UI VM=1 Clinical Finding UID */
    public static final int ClinicalFindingUID = 0x00290039;

    /**
     * (0029,xx3A) VR=CS VM=1 Measurement Evaluation String Value
     */
    public static final int MeasurementEvaluationStringValue = 0x0029003A;

    /**
     * (0029,xx3B) VR=IS VM=1 Measurement Evaluation Integer Value
     */
    public static final int MeasurementEvaluationIntegerValue = 0x0029003B;

    /**
     * (0029,xx3C) VR=FL VM=1 Measurement Evaluation Decimal Value
     */
    public static final int MeasurementEvaluationDecimalValue = 0x0029003C;

    /** (0029,xx3D) VR=CS VM=1 Measurement Line Show Center Flag */
    public static final int MeasurementLineShowCenterFlag = 0x0029003D;

    /**
     * (0029,xx3E) VR=CS VM=1 Measurement Angle Show ArrowTip Flag
     */
    public static final int MeasurementAngleShowArrowTipFlag = 0x0029003E;

    /** (0029,xx3F) VR=SQ VM=1 Camera Home Settings Sequence */
    public static final int CameraHomeSettingsSequence = 0x0029003F;

    /** (0029,xx40) VR=DS VM=1 Camera Zoom */
    public static final int CameraZoom = 0x00290040;

    /** (0029,xx41) VR=DS VM=3 Camera Positio */
    public static final int CameraPosition = 0x00290041;

    /** (0029,xx42) VR=DS VM=4 Camera Orientatio */
    public static final int CameraOrientation = 0x00290042;

    /** (0029,xx43) VR=DS VM=1 Camera Far Clip Plan */
    public static final int CameraFarClipPlane = 0x00290043;

    /** (0029,xx44) VR=DS VM=1 Camera Near Clip Plan */
    public static final int CameraNearClipPlane = 0x00290044;

    /** (0029,xx45) VR=DS VM=1 Camera Thicknes */
    public static final int CameraThickness = 0x00290045;

    /** (0029,xx46) VR=DS VM=1 Camera ViewPort Siz */
    public static final int CameraViewPortSize = 0x00290046;

    /** (0029,xx47) VR=DS VM=1 Camera Aspect Rati */
    public static final int CameraAspectRatio = 0x00290047;

    /** (0029,xx48) VR=LO VM=1 Camera Projection Typ */
    public static final int CameraProjectionType = 0x00290048;

    /** (0029,xx49) VR=DS VM=1 Camera Field of Vie */
    public static final int CameraFieldOfView = 0x00290049;

    /** (0029,xx4A) VR=DS VM=1 Camera Image Plane Distanc */
    public static final int CameraImagePlaneDistance = 0x0029004A;

    /** (0029,xx4B) VR=DS VM=1 Camera Image Maximum Heigh */
    public static final int CameraImageMaximumHeight = 0x0029004B;

    /** (0029,xx4C) VR=DS VM=1 Camera Image Minimum Heigh */
    public static final int CameraImageMinimumHeight = 0x0029004C;

    /** (0029,xx4D) VR=DS VM=1 Parallel Shift Interval M */
    public static final int ParallelShiftIntervalMM = 0x0029004D;

    /** (0029,xx4E) VR=DS VM=3 Parallel Shift BoundingBox Minimu */
    public static final int ParallelShiftBoundingBoxMinimum = 0x0029004E;

    /** (0029,xx4F) VR=DS VM=3 Parallel Shift BoundingBox Maximu */
    public static final int ParallelShiftBoundingBoxMaximum = 0x0029004F;

    /**
     * (0029,xx50) VR=CS VM=1 Renderer Vertex Is Characteristic Flag
     */
    public static final int RendererVertexIsCharacteristicFlag = 0x00290050;

    /** (0029,xx51) VR=CS VM=1 Renderer Thickness Usage Fla */
    public static final int RendererThicknessUsageFlag = 0x00290051;

    /** (0029,xx52) VR=DS VM=4 Renderer Threshol */
    public static final int RendererThreshold = 0x00290052;

    /** (0029,xx53) VR=DS VM=4 Renderer Materia */
    public static final int RendererMaterial = 0x00290053;

    /** (0029,xx54) VR=DS VM=4 Renderer DirectionalLight Colo */
    public static final int RendererDirectionalLightColor = 0x00290054;

    /**
     * (0029,xx55) VR=DS VM=3 Renderer DirectionalLight Directio
     */
    public static final int RendererDirectionalLightDirection = 0x00290055;

    /**
     * (0029,xx56) VR=CS VM=1 Renderer DirectionalLight TwoSide Usage Fla
     */
    public static final int RendererDirectionalLightTwoSideUsageFlag = 0x00290056;

    /**
     * (0029,xx57) VR=SQ VM=1 Renderer PWL TransferFunction Sequenc
     */
    public static final int RendererPWLTransferFunctionSequence = 0x00290057;

    /** (0029,xx58) VR=IS VM=0-n Renderer PWL Vertex Inde */
    public static final int RendererPWLVertexIndex = 0x00290058;

    /** (0029,xx59) VR=DS VM=0-n Renderer PWL Vertex Colo */
    public static final int RendererPWLVertexColor = 0x00290059;

    /** (0029,xx5A) VR=CS VM=1 Renderer Is Camera Required Fla */
    public static final int RendererIsCameraRequiredFlag = 0x0029005A;

    /** (0029,xx5B) VR=CS VM=1 Renderer Do Depth Test Flag */
    public static final int RendererDoDepthTestFlag = 0x0029005B;

    /**
     * (0029,xx5C) VR=CS VM=1 Renderer Directional Light Usage Fla
     */
    public static final int RendererDirectionalLightUsageFlag = 0x0029005C;

    /** (0029,xx5D) VR=SQ VM=1 Renderer Thickness Sequenc */
    public static final int RendererThicknessSequence = 0x0029005D;

    /** (0029,xx5E) VR=SQ VM=0-n Renderer Slice Spacing Sequence */
    public static final int RendererSliceSpacingSequence = 0x0029005E;

    /** (0029,xx5F) VR=DS VM=1 Renderer Sampling Distance */
    public static final int RendererSamplingDistance = 0x0029005F;

    /** (0029,xx60) VR=DS VM=1 Renderer Initial Sampling Distance */
    public static final int RendererInitialSamplingDistance = 0x00290060;

    /** (0029,xx61) VR=SQ VM=1 Segmentation Display Data Sequenc */
    public static final int SegmentationDisplayDataSequence = 0x00290061;

    /** (0029,xx62) VR=UI VM=0-n Segmentation Display Data UI */
    public static final int SegmentationDisplayDataUID = 0x00290062;

    /**
     * (0029,xx63) VR=SQ VM=1 Segmentation Display Parameter Sequenc
     */
    public static final int SegmentationDisplayParameterSequence = 0x00290063;

    /** (0029,xx64) VR=LO VM=1 Segmentation Display Parameter Typ */
    public static final int SegmentationDisplayParameterType = 0x00290064;

    /** (0029,xx65) VR=LO VM=1 Segmentation Display Visibilit */
    public static final int SegmentationDisplayVisibility = 0x00290065;

    /** (0029,xx66) VR=DS VM=4 Segmentation Display Colo */
    public static final int SegmentationDisplayColor = 0x00290066;

    /**
     * (0029,xx67) VR=CS VM=1 Segmentation Display is Selected Flag
     */
    public static final int SegmentationDisplayIsSelectedFlag = 0x00290067;

    /**
     * (0029,xx68) VR=OB VM=1 Segmentation Additional Information Blob
     */
    public static final int SegmentationAdditionalInformationBlob = 0x00290068;

    /** (0029,xx69) VR=ST VM=1 Hash Code Value */
    public static final int HashCodeValue = 0x00290069;

    /** (0029,xx6A) VR=LO VM=1-n Segmentation Version Identifier */
    public static final int SegmentationVersionIdentifier = 0x0029006A;

    /** (0029,xx70) VR=DS VM=3 Segmentation Volume Size */
    public static final int SegmentationVolumeSize = 0x00290070;

    /** (0029,xx71) VR=UI VM=1-n Registration Referenced Frame */
    public static final int RegistrationReferencedFrames = 0x00290071;

    /**
     * (0029,xx72) VR=UI VM=1-n Registration Referenced Registrations
     */
    public static final int RegistrationReferencedRegistrations = 0x00290072;

    /**
     * (0029,xx73) VR=LO VM=1 Registration Creation Algorithm Name
     */
    public static final int RegistrationCreationAlgorithmName = 0x00290073;

    /** (0029,xx74) VR=CS VM=1 ECG Graphic Type */
    public static final int ECGGraphicType = 0x00290074;

    /**
     * (0029,xx7A) VR=DS VM=1 Segmentation Volume Storage Data Type
     */
    public static final int SegmentationVolumeStorageDataType = 0x0029007A;

    /** (0029,xx7B) VR=FL VM=16 Segmentation Volume Model Matrix */
    public static final int SegmentationVolumeModelMatrix = 0x0029007B;

    /** (0029,xx80) VR=DS VM=3 Camera Rotation Axis */
    public static final int CameraRotationAxis = 0x00290080;

    /** (0029,xx81) VR=SL VM=0-n Overlay Hidden Display Attributes */
    public static final int OverlayHiddenDisplayAttributes = 0x00290081;

    /**
     * (0029,xx82) VR=LO VM=1 Presentation State Group Identifier
     */
    public static final int PresentationStateGroupIdentifier = 0x00290082;

    /**
     * (0029,xx83) VR=US VM=1 Temporary Smallest Image Pixel Value
     */
    public static final int TemporarySmallestImagePixelValue = 0x00290083;

    /** (0029,xx84) VR=DS VM=3 Camera Rotation Cente */
    public static final int CameraRotationCenter = 0x00290084;

    /** (0029,xx85) VR=CS VM=1 Camera Rotation Center Usage Fla */
    public static final int CameraRotationCenterUsageFlag = 0x00290085;

    /** (0029,xx86) VR=DS VM=12 Camera Parallel Epipe */
    public static final int CameraParallelEpiped = 0x00290086;

    /** (0029,xx87) VR=DS VM=1 Camera Max Zoom In Factor */
    public static final int CameraMaxZoomInFactor = 0x00290087;

    /** (0029,xx88) VR=DS VM=1 Camera Min Zoom In Factor */
    public static final int CameraMinZoomInFactor = 0x00290088;

    /** (0029,xx89) VR=US VM=1 Temporary Largest Image Pixel Value */
    public static final int TemporaryLargestImagePixelValue = 0x00290089;

    /** (0029,xx8A) VR=CS VM=1 Camera Rotation Axis Usage Flag */
    public static final int CameraRotationAxisUsageFlag = 0x0029008A;

    /** (0029,xx8B) VR=DS VM=3 Measurement Surface Normal */
    public static final int MeasurementSurfaceNormal = 0x0029008B;

    /** (0029,xx8C) VR=FL VM=16 Measurement Ellipsoid Model Matrix */
    public static final int MeasurementEllipsoidModelMatrix = 0x0029008C;

    /** (0029,xx8D) VR=LO VM=1 Measurement Evaluation DataRole ID */
    public static final int MeasurementEvaluationDataRoleID = 0x0029008D;

    /** (0029,xx8E) VR=LO VM=1 Measurement Algorithm Type */
    public static final int MeasurementAlgorithmType = 0x0029008E;

    /**
     * (0029,xx91) VR=SQ VM=1 Measurement Evaluation DataRole Sequenc
     */
    public static final int MeasurementEvaluationDataRoleSequence = 0x00290091;

    /**
     * (0029,xx92) VR=CS VM=1 Measurement Evaluation DataRole Ite
     */
    public static final int MeasurementEvaluationDataRoleItem = 0x00290092;

    /** (0029,xx93) VR=SQ VM=1 Measurement Evaluation Sequenc */
    public static final int MeasurementEvaluationSequence = 0x00290093;

    /** (0029,xx94) VR=DS VM=1 Measurement Evaluation Valu */
    public static final int MeasurementEvaluationValue = 0x00290094;

    /** (0029,xx95) VR=CS VM=1 Measurement Evaluation I */
    public static final int MeasurementEvaluationID = 0x00290095;

    /** (0029,xx96) VR=FL VM=0-n Measurement Data Point */
    public static final int MeasurementDataPoints = 0x00290096;

    /** (0029,xx97) VR=FL VM=0-n Measurement Data Angle */
    public static final int MeasurementDataAngles = 0x00290097;

    /** (0029,xx98) VR=LO VM=1 Measurement Data Slic */
    public static final int MeasurementDataSlice = 0x00290098;

    /** (0029,xx99) VR=FL VM=1 Measurement Data Slice Thicknes */
    public static final int MeasurementDataSliceThickness = 0x00290099;

    /**
     * (0029,xx9A) VR=SQ VM=1 Measurement Referenced Frames Sequenc
     */
    public static final int MeasurementReferencedFramesSequence = 0x0029009A;

    /**
     * (0029,xx9B) VR=DS VM=0-n Measurement Evaluation Longest Distanc
     */
    public static final int MeasurementEvaluationLongestDistance = 0x0029009B;

    /** (0029,xx9C) VR=DS VM=0-n Measurement Evaluation Centroi */
    public static final int MeasurementEvaluationCentroid = 0x0029009C;

    /** (0029,xx9D) VR=FL VM=6 Measurement Data Bounding Box */
    public static final int MeasurementDataBoundingBox = 0x0029009D;

    /** (0029,xx9E) VR=LO VM=1 Measurement Text */
    public static final int MeasurementText = 0x0029009E;

    /** (0029,xx9F) VR=IS VM=1 Measurement Diameter */
    public static final int MeasurementDiameter = 0x0029009F;

    /** (0029,xxA0) VR=DS VM=1 Image Rotation Fractiona */
    public static final int ImageRotationFractional = 0x002900A0;

    /** (0029,xxA1) VR=LO VM=1 Preset Name */
    public static final int PresetName = 0x002900A1;

    /** (0029,xxA2) VR=SQ VM=1 Fusion LUT Sequence */
    public static final int FusionLUTSequence = 0x002900A2;

    /** (0029,xxA3) VR=CS VM=1 Fusion LUT Is Active Flag */
    public static final int FusionLUTIsActiveFlag = 0x002900A3;

    /** (0029,xxA5) VR=UI VM=1 Syngo UID */
    public static final int SyngoUID = 0x002900A5;

    /** (0029,xxA6) VR=UI VM=1 Presentation Version Identifie */
    public static final int PresentationVersionIdentifier = 0x002900A6;

    /** (0029,xxA7) VR=SQ VM=1 Presentation Module Sequenc */
    public static final int PresentationModuleSequence = 0x002900A7;

    /** (0029,xxA8) VR=LO VM=1 Presentation Module Typ */
    public static final int PresentationModuleType = 0x002900A8;

    /** (0029,xxA9) VR=SQ VM=1 Presentation State Sequenc */
    public static final int PresentationStateSequence = 0x002900A9;

    /** (0029,xxAA) VR=CS VM=1 LUT Inverted Flag */
    public static final int LUTInvertedFlag = 0x002900AA;

    /** (0029,xxAB) VR=IS VM=1 Softcopy VOI LUT Viewing Index */
    public static final int SoftcopyVOILUTViewingIndex = 0x002900AB;

    /**
     * (0029,xxAC) VR=FD VM=2 Displayed Area Bottom Right Hand Corner Fractional
     */
    public static final int DisplayedAreaBottomRightHandCornerFractional = 0x002900AC;

    /**
     * (0029,xxAD) VR=FD VM=2 Displayed Area Top Left Hand Corner Fractional
     */
    public static final int DisplayedAreaTopLeftHandCornerFractional = 0x002900AD;

    /** (0029,xxAE) VR=FL VM=1 Measurement Alpha */
    public static final int MeasurementAlpha = 0x002900AE;

    /** (0029,xxAF) VR=OB VM=1 Measurement Bitmap */
    public static final int MeasurementBitmap = 0x002900AF;

    /** (0029,xxB0) VR=US VM=1 Current Frame Number */
    public static final int CurrentFrameNumber = 0x002900B0;

    /** (0029,xxB1) VR=LO VM=1 Image Text View Name */
    public static final int ImageTextViewName = 0x002900B1;

    /** (0029,xxB2) VR=SQ VM=1 Image Text View Content Sequence */
    public static final int ImageTextViewContentSequence = 0x002900B2;

    /** (0029,xxB3) VR=LO VM=1-n Image Text Line Names */
    public static final int ImageTextLineNames = 0x002900B3;

    /** (0029,xxB4) VR=LO VM=1-n ImageText Line Values */
    public static final int ImageTextLineValues = 0x002900B4;

    /** (0029,xxB5) VR=LO VM=1 ? */
    public static final int _0029_xxB5_ = 0x002900B5;

    /**
     * (0029,xxB6) VR=CS VM=1 Measurement Link Evaluation Text Flag
     */
    public static final int MeasurementLinkEvaluationTextFlag = 0x002900B6;

    /**
     * (0029,xxB7) VR=DS VM=3 Measurement Evaluation Text Position Vector
     */
    public static final int MeasurementEvaluationTextPositionVector = 0x002900B7;

    /**
     * (0029,xxB8) VR=OB VM=1 Image Text Alpha Blending Line Value
     */
    public static final int ImageTextAlphaBlendingLineValue = 0x002900B8;

    /** (0029,xxC1) VR=SQ VM=1 Task Data Sequence */
    public static final int TaskDataSequence = 0x002900C1;

    /** (0029,xxC2) VR=CS VM=1 Task Data Type */
    public static final int TaskDataType = 0x002900C2;

    /** (0029,xxC3) VR=LO VM=1 Task Data Version */
    public static final int TaskDataVersion = 0x002900C3;

    /** (0029,xxC4) VR=ST VM=1 Task Data Description */
    public static final int TaskDataDescription = 0x002900C4;

    /** (0029,xxC5) VR=OB VM=1 Task Data */
    public static final int TaskData = 0x002900C5;

    /** (0029,xxC6) VR=IS VM=1 Task Data Size */
    public static final int TaskDataSize = 0x002900C6;

    /** (0029,xxC9) VR=SQ VM=1 Clip Plane Sequence */
    public static final int ClipPlaneSequence = 0x002900C9;

    /** (0029,xxCA) VR=DS VM=3 Clip Plane Center */
    public static final int ClipPlaneCenter = 0x002900CA;

    /** (0029,xxCB) VR=DS VM=3 Clip Plane Normal */
    public static final int ClipPlaneNormal = 0x002900CB;

    /** (0029,xxCC) VR=DS VM=2 Clip Plane Scale */
    public static final int ClipPlaneScale = 0x002900CC;

    /** (0029,xxCD) VR=CS VM=1 Clip Plane Use Thickness Flag */
    public static final int ClipPlaneUseThicknessFlag = 0x002900CD;

    /** (0029,xxCE) VR=DS VM=1 Clip Plane Thickness */
    public static final int ClipPlaneThickness = 0x002900CE;

    /** (0029,xxCF) VR=SQ VM=1 Image Sequence */
    public static final int ImageSequence = 0x002900CF;

    /** (0029,xxD0) VR=CS VM=1 Clip Plane Enable Clip */
    public static final int ClipPlaneEnableClip = 0x002900D0;

    /** (0029,xxD1) VR=DS VM=1 Clip Plane Handle Ratio */
    public static final int ClipPlaneHandleRatio = 0x002900D1;

    /** (0029,xxD2) VR=DS VM=24 Clip Plane Bounding Points */
    public static final int ClipPlaneBoundingPoints = 0x002900D2;

    /** (0029,xxD3) VR=DS VM=16 Clip Plane Motion Matrix */
    public static final int ClipPlaneMotionMatrix = 0x002900D3;

    /** (0029,xxD4) VR=DS VM=1 Clip Plane Shift Velocity */
    public static final int ClipPlaneShiftVelocity = 0x002900D4;

    /** (0029,xxD5) VR=CS VM=1 Clip Plane Enabled Flag */
    public static final int ClipPlaneEnabledFlag = 0x002900D5;

    /** (0029,xxD6) VR=DS VM=1 Clip Plane Rotate Velocity */
    public static final int ClipPlaneRotateVelocity = 0x002900D6;

    /** (0029,xxD7) VR=CS VM=1 Clip Plane Show Graphics Flag */
    public static final int ClipPlaneShowGraphicsFlag = 0x002900D7;

    /** (0029,xxE0) VR=DS VM=3 Crop Box Size */
    public static final int CropBoxSize = 0x002900E0;

    /** (0029,xxE1) VR=CS VM=1 Crop Box Enabled Flag */
    public static final int CropBoxEnabledFlag = 0x002900E1;

    /** (0029,xxE2) VR=DS VM=3 Crop Box Absolute Origin */
    public static final int CropBoxAbsoluteOrigin = 0x002900E2;

    /** (0029,xxE3) VR=CS VM=1 Crop Box Show Graphics Flag */
    public static final int CropBoxShowGraphicsFlag = 0x002900E3;

    /** (0029,xxF1) VR=DS VM=0-n Curved Camera Coordinates */
    public static final int CurvedCameraCoordinates = 0x002900F1;

    /** (0029,xxF2) VR=DS VM=1 Curved Camera Point of Interest */
    public static final int CurvedCameraPointOfInterest = 0x002900F2;

    /**
     * (0029,xxF3) VR=CS VM=1 Curved Camera Point of Interest Usage Flag
     */
    public static final int CurvedCameraPointOfInterestUsageFlag = 0x002900F3;

    /** (0029,xxF4) VR=DS VM=1 Curved Camera Thickness */
    public static final int CurvedCameraThickness = 0x002900F4;

    /** (0029,xxF5) VR=DS VM=1 Curved Camera Extrusion Length */
    public static final int CurvedCameraExtrusionLength = 0x002900F5;

    /** (0029,xxF6) VR=CS VM=1 Curved Camera Rotation Axis Mode */
    public static final int CurvedCameraRotationAxisMode = 0x002900F6;

    /** (0029,xxF7) VR=DS VM=1 Curved Camera Roll Rotation Axis */
    public static final int CurvedCameraRollRotationAxis = 0x002900F7;

    /** (0029,xxF8) VR=DS VM=1 Curved Camera View Port Height */
    public static final int CurvedCameraViewPortHeight = 0x002900F8;

    /** (0029,xxF9) VR=DS VM=1 Curved Camera Cut Direction */
    public static final int CurvedCameraCutDirection = 0x002900F9;

    /** (0029,xxFA) VR=DS VM=1 Curved Camera Pan Vector */
    public static final int CurvedCameraPanVector = 0x002900FA;

    /** (0029,xxFB) VR=LO VM=1 Clinical Finding ID */
    public static final int ClinicalFindingID = 0x002900FB;

    /** (0029,xxFC) VR=CS VM=1 Measurement Is Circle Flag */
    public static final int MeasurementIsCircleFlag = 0x002900FC;

    /** (0029,xxFD) VR=LO VM=1 Measurement Application Type ID */
    public static final int MeasurementApplicationTypeID = 0x002900FD;

}
