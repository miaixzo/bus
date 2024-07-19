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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_SDS_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MR SDS 01";

    /** (0021,xxFE) VR=SQ VM=1 Siemens MR SDS Sequence */
    public static final int SiemensMRSDSSequence = 0x002100FE;

    /** (0021,xx01) VR=IS VM=1 Used Patient Weight */
    public static final int UsedPatientWeight = 0x00210001;

    /** (0021,xx02) VR=DS VM=3 SAR Whole Body */
    public static final int SARWholeBody = 0x00210002;

    /** (0021,xx03) VR=OB VM=1 MR Protocol */
    public static final int MRProtocol = 0x00210003;

    /** (0021,xx04) VR=DS VM=1 Slice Array Concatenations */
    public static final int SliceArrayConcatenations = 0x00210004;

    /** (0021,xx05) VR=IS VM=3 Rel Table Position */
    public static final int RelTablePosition = 0x00210005;

    /** (0021,xx06) VR=LO VM=1 Coil For Gradient */
    public static final int CoilForGradient = 0x00210006;

    /** (0021,xx07) VR=LO VM=1 Long Model Name */
    public static final int LongModelName = 0x00210007;

    /** (0021,xx08) VR=SH VM=1 Gradient Mode */
    public static final int GradientMode = 0x00210008;

    /** (0021,xx09) VR=LO VM=1 PAT Mode Text */
    public static final int PATModeText = 0x00210009;

    /** (0021,xx0A) VR=DS VM=1 SW Correction Factor */
    public static final int SWCorrectionFactor = 0x0021000A;

    /** (0021,xx0B) VR=DS VM=1 RF Power Error Indicator */
    public static final int RFPowerErrorIndicator = 0x0021000B;

    /** (0021,xx0C) VR=SH VM=1 Positive PCS Directions */
    public static final int PositivePCSDirections = 0x0021000C;

    /** (0021,xx0D) VR=US VM=1 Protocol Change History */
    public static final int ProtocolChangeHistory = 0x0021000D;

    /** (0021,xx0E) VR=LO VM=1 Data File Name */
    public static final int DataFileName = 0x0021000E;

    /** (0021,xx0F) VR=DS VM=3 Stimlim */
    public static final int Stimlim = 0x0021000F;

    /** (0021,xx10) VR=IS VM=1 MR Protocol Version */
    public static final int MRProtocolVersion = 0x00210010;

    /** (0021,xx11) VR=DS VM=1 Phase Gradient Amplitude */
    public static final int PhaseGradientAmplitude = 0x00210011;

    /** (0021,xx12) VR=FD VM=1 Readout OS */
    public static final int ReadoutOS = 0x00210012;

    /** (0021,xx13) VR=DS VM=1 tpuls max */
    public static final int tpulsmax = 0x00210013;

    /** (0021,xx14) VR=IS VM=1 Number of Prescans */
    public static final int NumberOfPrescans = 0x00210014;

    /** (0021,xx15) VR=FL VM=1 Measurement Index */
    public static final int MeasurementIndex = 0x00210015;

    /** (0021,xx16) VR=DS VM=1 dBdt Threshold */
    public static final int dBdtThreshold = 0x00210016;

    /** (0021,xx17) VR=DS VM=1 Selection Gradient Amplitude */
    public static final int SelectionGradientAmplitude = 0x00210017;

    /** (0021,xx18) VR=SH VM=1 RF SWD Most Critical Aspect */
    public static final int RFSWDMostCriticalAspect = 0x00210018;

    /** (0021,xx19) VR=OB VM=1 MR Phoenix Protocol */
    public static final int MRPhoenixProtocol = 0x00210019;

    /** (0021,xx1A) VR=LO VM=1 Coil String */
    public static final int CoilString = 0x0021001A;

    /** (0021,xx1B) VR=DS VM=1 Slice Resolution */
    public static final int SliceResolution = 0x0021001B;

    /** (0021,xx1C) VR=DS VM=3 Stim max online */
    public static final int Stimmaxonline = 0x0021001C;

    /** (0021,xx1D) VR=IS VM=1 Operation Mode Flag */
    public static final int OperationModeFlag = 0x0021001D;

    /** (0021,xx1E) VR=FL VM=16 Auto Align Matrix */
    public static final int AutoAlignMatrix = 0x0021001E;

    /** (0021,xx1F) VR=DS VM=2 Coil Tuning Reflection */
    public static final int CoilTuningReflection = 0x0021001F;

    /** (0021,xx20) VR=UI VM=1 Representative Image */
    public static final int RepresentativeImage = 0x00210020;

    /** (0021,xx22) VR=SH VM=1 Sequence File Owner */
    public static final int SequenceFileOwner = 0x00210022;

    /** (0021,xx23) VR=IS VM=1 RF Watchdog Mask */
    public static final int RFWatchdogMask = 0x00210023;

    /** (0021,xx24) VR=LO VM=1 Post Proc Protocol */
    public static final int PostProcProtocol = 0x00210024;

    /** (0021,xx25) VR=SL VM=3 Table Position Origin */
    public static final int TablePositionOrigin = 0x00210025;

    /** (0021,xx26) VR=IS VM=32 Misc Sequence Param */
    public static final int MiscSequenceParam = 0x00210026;

    /** (0021,xx27) VR=US VM=1 Isocentered */
    public static final int Isocentered = 0x00210027;

    /** (0021,xx2A) VR=IS VM=1-n Coil ID */
    public static final int CoilID = 0x0021002A;

    /** (0021,xx2B) VR=ST VM=1 Pat Rein Pattern */
    public static final int PatReinPattern = 0x0021002B;

    /** (0021,xx2C) VR=DS VM=3 SED */
    public static final int SED = 0x0021002C;

    /** (0021,xx2D) VR=DS VM=3 SAR Most Critical Aspect */
    public static final int SARMostCriticalAspect = 0x0021002D;

    /** (0021,xx2E) VR=IS VM=1 Stimm on mode */
    public static final int StimmOnMode = 0x0021002E;

    /** (0021,xx2F) VR=DS VM=3 Gradient Delay Time */
    public static final int GradientDelayTime = 0x0021002F;

    /** (0021,xx30) VR=DS VM=1 Readout Gradient Amplitude */
    public static final int ReadoutGradientAmplitude = 0x00210030;

    /** (0021,xx31) VR=IS VM=1 Abs Table Position */
    public static final int AbsTablePosition = 0x00210031;

    /** (0021,xx32) VR=SS VM=1 RF SWD Operation Mode */
    public static final int RFSWDOperationMode = 0x00210032;

    /** (0021,xx33) VR=SH VM=1 Coil for Gradient2 */
    public static final int CoilForGradient2 = 0x00210033;

    /** (0021,xx34) VR=DS VM=1 Stim Factor */
    public static final int StimFactor = 0x00210034;

    /** (0021,xx35) VR=DS VM=1 Stim max ges norm online */
    public static final int Stimmaxgesnormonline = 0x00210035;

    /** (0021,xx36) VR=DS VM=1 dBdt max */
    public static final int dBdtmax = 0x00210036;

    /** (0021,xx38) VR=DS VM=1 Transmitter Calibration */
    public static final int TransmitterCalibration = 0x00210038;

    /** (0021,xx39) VR=OB VM=1 MR EVA Protocol */
    public static final int MREVAProtocol = 0x00210039;

    /** (0021,xx3B) VR=DS VM=1 dBdt Limit */
    public static final int dBdtLimit = 0x0021003B;

    /** (0021,xx3C) VR=OB VM=1 VF Model Info */
    public static final int VFModelInfo = 0x0021003C;

    /** (0021,xx3D) VR=CS VM=1 Phase Slice Oversampling */
    public static final int PhaseSliceOversampling = 0x0021003D;

    /** (0021,xx3E) VR=OB VM=1 VF Settings */
    public static final int VFSettings = 0x0021003E;

    /** (0021,xx3F) VR=UT VM=1 Auto Align Data */
    public static final int AutoAlignData = 0x0021003F;

    /** (0021,xx40) VR=UT VM=1 FMRI Model Parameters */
    public static final int FMRIModelParameters = 0x00210040;

    /** (0021,xx41) VR=UT VM=1 FMRI Model Info */
    public static final int FMRIModelInfo = 0x00210041;

    /** (0021,xx42) VR=UT VM=1 FMRI External Parameters */
    public static final int FMRIExternalParameters = 0x00210042;

    /** (0021,xx43) VR=UT VM=1 FMRI External Info */
    public static final int FMRIExternalInfo = 0x00210043;

    /** (0021,xx44) VR=DS VM=2 B1 RMS */
    public static final int B1RMS = 0x00210044;

    /** (0021,xx45) VR=CS VM=1 B1 RMS Supervision */
    public static final int B1RMSSupervision = 0x00210045;

    /** (0021,xx46) VR=DS VM=1 Tales Reference Power */
    public static final int TalesReferencePower = 0x00210046;

    /** (0021,xx47) VR=CS VM=1 Safety Standard */
    public static final int SafetyStandard = 0x00210047;

    /** (0021,xx48) VR=CS VM=1 DICOM Image Flavor */
    public static final int DICOMImageFlavor = 0x00210048;

    /** (0021,xx49) VR=CS VM=1 DICOM Acquisition Contrast */
    public static final int DICOMAcquisitionContrast = 0x00210049;

    /** (0021,xx50) VR=US VM=1 RF Echo Train Length 4MF */
    public static final int RFEchoTrainLength4MF = 0x00210050;

    /** (0021,xx51) VR=US VM=1 Gradient Echo Train Length 4MF */
    public static final int GradientEchoTrainLength4MF = 0x00210051;

    /** (0021,xx52) VR=LO VM=1 Version Info */
    public static final int VersionInfo = 0x00210052;

    /** (0021,xx53) VR=CS VM=1 Laterality 4MF */
    public static final int Laterality4MF = 0x00210053;

}
