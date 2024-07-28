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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_SDI_02;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR SDI 02";

    /** (0021,xx01) VR=US VM=1 Number of Images in Mosaic */
    public static final int NumberOfImagesInMosaic = 0x00210001;

    /** (0021,xx02) VR=FD VM=3 Slice Normal Vector */
    public static final int SliceNormalVector = 0x00210002;

    /** (0021,xx03) VR=DS VM=1 Slice Measurement Duration */
    public static final int SliceMeasurementDuration = 0x00210003;

    /** (0021,xx04) VR=DS VM=1 Time After Start */
    public static final int TimeAfterStart = 0x00210004;

    /** (0021,xx05) VR=IS VM=1 B Value */
    public static final int BValue = 0x00210005;

    /** (0021,xx06) VR=LO VM=1 ICE Dims */
    public static final int ICEDims = 0x00210006;

    /** (0021,xx1A) VR=SH VM=1 RF SWD Data Type */
    public static final int RFSWDDataType = 0x0021001A;

    /** (0021,xx1B) VR=US VM=1 MoCoQ Measure */
    public static final int MoCoQMeasure = 0x0021001B;

    /** (0021,xx1C) VR=IS VM=1 Phase Encoding Direction Positive */
    public static final int PhaseEncodingDirectionPositive = 0x0021001C;

    /** (0021,xx1D) VR=OB VM=1 Pixel File */
    public static final int PixelFile = 0x0021001D;

    /** (0021,xx1F) VR=IS VM=1 FMRI Stimul Info */
    public static final int FMRIStimulInfo = 0x0021001F;

    /** (0021,xx20) VR=DS VM=1 Voxel in Plane Rot */
    public static final int VoxelInPlaneRot = 0x00210020;

    /** (0021,xx21) VR=CS VM=1 Diffusion Directionality 4MF */
    public static final int DiffusionDirectionality4MF = 0x00210021;

    /** (0021,xx22) VR=DS VM=1 Voxel Thickness */
    public static final int VoxelThickness = 0x00210022;

    /** (0021,xx23) VR=FD VM=6 B Matrix */
    public static final int BMatrix = 0x00210023;

    /** (0021,xx24) VR=IS VM=1 Multistep Index */
    public static final int MultistepIndex = 0x00210024;

    /** (0021,xx25) VR=LT VM=1 Comp Adjusted Param */
    public static final int CompAdjustedParam = 0x00210025;

    /** (0021,xx26) VR=IS VM=1 Comp Algorithm */
    public static final int CompAlgorithm = 0x00210026;

    /** (0021,xx27) VR=DS VM=1 Voxel NormalC or */
    public static final int VoxelNormalCor = 0x00210027;

    /** (0021,xx29) VR=SH VM=1 Flow Encoding Direction String */
    public static final int FlowEncodingDirectionString = 0x00210029;

    /** (0021,xx2A) VR=DS VM=1 Voxel Normal Sag */
    public static final int VoxelNormalSag = 0x0021002A;

    /** (0021,xx2B) VR=DS VM=1 Voxel Position Sag */
    public static final int VoxelPositionSag = 0x0021002B;

    /** (0021,xx2C) VR=DS VM=1 Voxel Normal Tra */
    public static final int VoxelNormalTra = 0x0021002C;

    /** (0021,xx2D) VR=DS VM=1 Voxel Position Tra */
    public static final int VoxelPositionTra = 0x0021002D;

    /** (0021,xx2E) VR=UL VM=1 Used Channel Mask */
    public static final int UsedChannelMask = 0x0021002E;

    /** (0021,xx2F) VR=DS VM=1 Repetition Time Effective */
    public static final int RepetitionTimeEffective = 0x0021002F;

    /** (0021,xx30) VR=DS VM=6 CSI Image Orientation Patient */
    public static final int CSIImageOrientationPatient = 0x00210030;

    /** (0021,xx32) VR=DS VM=1 CSI Slice Location */
    public static final int CSISliceLocation = 0x00210032;

    /** (0021,xx33) VR=IS VM=1 Echo Column Position */
    public static final int EchoColumnPosition = 0x00210033;

    /** (0021,xx34) VR=FD VM=1 Flow VENC */
    public static final int FlowVENC = 0x00210034;

    /** (0021,xx35) VR=IS VM=1 Measured Fourier Lines */
    public static final int MeasuredFourierLines = 0x00210035;

    /** (0021,xx36) VR=SH VM=1 LQ Algorithm */
    public static final int LQAlgorithm = 0x00210036;

    /** (0021,xx37) VR=DS VM=1 Voxel Position Cor */
    public static final int VoxelPositionCor = 0x00210037;

    /** (0021,xx38) VR=IS VM=1 Filter2 */
    public static final int Filter2 = 0x00210038;

    /** (0021,xx39) VR=FD VM=1 FMRI Stimul Level */
    public static final int FMRIStimulLevel = 0x00210039;

    /** (0021,xx3A) VR=DS VM=1 Voxel Readout FOV */
    public static final int VoxelReadoutFOV = 0x0021003A;

    /** (0021,xx3B) VR=IS VM=1 Normalize Manipulated */
    public static final int NormalizeManipulated = 0x0021003B;

    /** (0021,xx3C) VR=FD VM=3 RBMoCoRot */
    public static final int RBMoCoRot = 0x0021003C;

    /** (0021,xx3D) VR=IS VM=1 Comp Manual Adjusted */
    public static final int CompManualAdjusted = 0x0021003D;

    /** (0021,xx3F) VR=SH VM=1 Spectrum Text Region Label */
    public static final int SpectrumTextRegionLabel = 0x0021003F;

    /** (0021,xx40) VR=DS VM=1 Voxel Phase FOV */
    public static final int VoxelPhaseFOV = 0x00210040;

    /** (0021,xx41) VR=SH VM=1 GSWD Data Type */
    public static final int GSWDDataType = 0x00210041;

    /** (0021,xx42) VR=IS VM=1 Real Dwell Time */
    public static final int RealDwellTime = 0x00210042;

    /** (0021,xx43) VR=LT VM=1 Comp Job ID */
    public static final int CompJobID = 0x00210043;

    /** (0021,xx44) VR=IS VM=1 Comp Blended */
    public static final int CompBlended = 0x00210044;

    /** (0021,xx45) VR=SL VM=3 Ima Abs Table Position */
    public static final int ImaAbsTablePosition = 0x00210045;

    /** (0021,xx46) VR=FD VM=3 Diffusion Gradient Direction */
    public static final int DiffusionGradientDirection = 0x00210046;

    /** (0021,xx47) VR=IS VM=1 Flow Encoding Direction */
    public static final int FlowEncodingDirection = 0x00210047;

    /** (0021,xx48) VR=IS VM=1 Echo Partition Position */
    public static final int EchoPartitionPosition = 0x00210048;

    /** (0021,xx49) VR=IS VM=1 Echo Line Position */
    public static final int EchoLinePosition = 0x00210049;

    /** (0021,xx4B) VR=LT VM=1 Comp Auto Param */
    public static final int CompAutoParam = 0x0021004B;

    /** (0021,xx4C) VR=IS VM=1 Original Image Number */
    public static final int OriginalImageNumber = 0x0021004C;

    /** (0021,xx4D) VR=IS VM=1 Original Series Number */
    public static final int OriginalSeriesNumber = 0x0021004D;

    /** (0021,xx4E) VR=IS VM=1 Actual 3D Ima Part Number */
    public static final int Actual3DImaPartNumber = 0x0021004E;

    /** (0021,xx4F) VR=LO VM=1 Ima Coil String */
    public static final int ImaCoilString = 0x0021004F;

    /** (0021,xx50) VR=DS VM=2 CSI Pixel Spacing */
    public static final int CSIPixelSpacing = 0x00210050;

    /** (0021,xx51) VR=UL VM=1 Sequence Mask */
    public static final int SequenceMask = 0x00210051;

    /** (0021,xx52) VR=US VM=1 Image Group */
    public static final int ImageGroup = 0x00210052;

    /** (0021,xx53) VR=FD VM=1 Bandwidth Per Pixel Phase Encode */
    public static final int BandwidthPerPixelPhaseEncode = 0x00210053;

    /** (0021,xx54) VR=US VM=1 Non Planar Image */
    public static final int NonPlanarImage = 0x00210054;

    /** (0021,xx55) VR=OB VM=1 Pixel File Name */
    public static final int PixelFileName = 0x00210055;

    /** (0021,xx56) VR=LO VM=1 Ima PAT Mode Text */
    public static final int ImaPATModeText = 0x00210056;

    /** (0021,xx57) VR=DS VM=3 CSI Image Position Patient */
    public static final int CSIImagePositionPatient = 0x00210057;

    /** (0021,xx58) VR=SH VM=1 Acquisition Matrix Text */
    public static final int AcquisitionMatrixText = 0x00210058;

    /** (0021,xx59) VR=IS VM=3 Ima Rel Table Position */
    public static final int ImaRelTablePosition = 0x00210059;

    /** (0021,xx5A) VR=FD VM=3 RBMoCoTrans */
    public static final int RBMoCoTrans = 0x0021005A;

    /** (0021,xx5B) VR=FD VM=3 Slice Position PCS */
    public static final int SlicePositionPCS = 0x0021005B;

    /** (0021,xx5C) VR=DS VM=1 CSI Slice Thickness */
    public static final int CSISliceThickness = 0x0021005C;

    /** (0021,xx5E) VR=IS VM=1 Protocol Slice Number */
    public static final int ProtocolSliceNumber = 0x0021005E;

    /** (0021,xx5F) VR=IS VM=1 Filter1 */
    public static final int Filter1 = 0x0021005F;

    /** (0021,xx60) VR=SH VM=1 Transmitting Coil */
    public static final int TransmittingCoil = 0x00210060;

    /** (0021,xx61) VR=DS VM=1 Number of Averages N4 */
    public static final int NumberOfAveragesN4 = 0x00210061;

    /** (0021,xx62) VR=FD VM=1-n Mosaic Ref Acq Times */
    public static final int MosaicRefAcqTimes = 0x00210062;

    /** (0021,xx63) VR=IS VM=1 Auto Inline Image Filter Enabled */
    public static final int AutoInlineImageFilterEnabled = 0x00210063;

    /** (0021,xx65) VR=FD VM=1-n QC Data */
    public static final int QCData = 0x00210065;

    /** (0021,xx66) VR=LT VM=1 Exam Landmarks */
    public static final int ExamLandmarks = 0x00210066;

    /** (0021,xx67) VR=ST VM=1 Exam Data Role */
    public static final int ExamDataRole = 0x00210067;

    /** (0021,xx68) VR=OB VM=1 MR Diffusion */
    public static final int MRDiffusion = 0x00210068;

    /** (0021,xx69) VR=OB VM=1 Real World Value Mapping */
    public static final int RealWorldValueMapping = 0x00210069;

    /** (0021,xx70) VR=OB VM=1 Data Set Info */
    public static final int DataSetInfo = 0x00210070;

    /** (0021,xx71) VR=UT VM=1 Used Channel String */
    public static final int UsedChannelString = 0x00210071;

    /** (0021,xx72) VR=CS VM=1 Phase ContrastN4 */
    public static final int PhaseContrastN4 = 0x00210072;

    /** (0021,xx73) VR=UT VM=1 MR Velocity Encoding */
    public static final int MRVelocityEncoding = 0x00210073;

    /** (0021,xx74) VR=FD VM=3 Velocity Encoding Direction N4 */
    public static final int VelocityEncodingDirectionN4 = 0x00210074;

    /** (0021,xx75) VR=CS VM=1-n Image Type 4MF */
    public static final int ImageType4MF = 0x00210075;

    /** (0021,xx76) VR=LO VM=1-n Image History */
    public static final int ImageHistory = 0x00210076;

    /** (0021,xx77) VR=LO VM=1 SequenceI nfo */
    public static final int SequenceInfo = 0x00210077;

    /** (0021,xx78) VR=CS VM=1-n Image Type Visible */
    public static final int ImageTypeVisible = 0x00210078;

    /** (0021,xx79) VR=CS VM=1 Distortion Correction Type */
    public static final int DistortionCorrectionType = 0x00210079;

    /** (0021,xx80) VR=CS VM=1 Image Filter Type */
    public static final int ImageFilterType = 0x00210080;

    /** (0021,xxFE) VR=SQ VM=1 Siemens MR SDI Sequence */
    public static final int SiemensMRSDISequence = 0x002100FE;

}
