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
package org.miaixz.bus.image.galaxy.dict.Philips_MR_Imaging_DD_005;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "Philips MR Imaging DD 005";

    /** (2005,xx00) VR=CS VM=1 Volume View Enabled */
    public static final int VolumeViewEnabled = 0x20050000;

    /** (2005,xx01) VR=UL VM=1 Number of Study Reference */
    public static final int NumberOfStudyReference = 0x20050001;

    /** (2005,xx02) VR=SQ VM=1 SPS Code */
    public static final int SPSCode = 0x20050002;

    /** (2005,xx03) VR=UL VM=1 Number of SPS Codes */
    public static final int NumberOfSPSCodes = 0x20050003;

    /** (2005,xx04) VR=SS VM=1 ? */
    public static final int _2005_xx04_ = 0x20050004;

    /** (2005,xx06) VR=SS VM=1 Number of PS Specific Character Sets */
    public static final int NumberOfPSSpecificCharacterSets = 0x20050006;

    /** (2005,xx07) VR=SS VM=1 Number of Specific Character Set */
    public static final int NumberOfSpecificCharacterSet = 0x20050007;

    /** (2005,xx09) VR=DS VM=1 Rescale Intercept Original */
    public static final int RescaleInterceptOriginal = 0x20050009;

    /** (2005,xx0A) VR=DS VM=1 Rescale Slope Original */
    public static final int RescaleSlopeOriginal = 0x2005000A;

    /** (2005,xx0B) VR=LO VM=1 Rescale Type Original */
    public static final int RescaleTypeOriginal = 0x2005000B;

    /** (2005,xx0E) VR=SQ VM=1 Private Shared Sequence */
    public static final int PrivateSharedSequence = 0x2005000E;

    /** (2005,xx0F) VR=SQ VM=1 Private Per-Frame Sequence */
    public static final int PrivatePerFrameSequence = 0x2005000F;

    /** (2005,xx10) VR=IS VM=1 MF Conv Treat Spectro Mix Number */
    public static final int MFConvTreatSpectroMixNumber = 0x20050010;

    /** (2005,xx11) VR=UI VM=1 MF Private Referenced SOP Instance UID */
    public static final int MFPrivateReferencedSOPInstanceUID = 0x20050011;

    /** (2005,xx12) VR=IS VM=1 Diffusion B Value Number */
    public static final int DiffusionBValueNumber = 0x20050012;

    /** (2005,xx13) VR=IS VM=1 Gradient Orientation Number */
    public static final int GradientOrientationNumber = 0x20050013;

    /** (2005,xx14) VR=SL VM=1 Number of Diffusion B Values */
    public static final int NumberOfDiffusionBValues = 0x20050014;

    /** (2005,xx15) VR=SL VM=1 Number of Diffusion Gradient Orientations */
    public static final int NumberOfDiffusionGradientOrientations = 0x20050015;

    /** (2005,xx16) VR=CS VM=1 Plan Mode */
    public static final int PlanMode = 0x20050016;

    /** (2005,xx17) VR=FD VM=3 Diffusion B Matrix */
    public static final int DiffusionBMatrix = 0x20050017;

    /** (2005,xx18) VR=CS VM=3 Operating Mode Type */
    public static final int OperatingModeType = 0x20050018;

    /** (2005,xx19) VR=CS VM=3 Operating Mode */
    public static final int OperatingMode = 0x20050019;

    /** (2005,xx1A) VR=CS VM=1 Fat Saturation Technique */
    public static final int FatSaturationTechnique = 0x2005001A;

    /** (2005,xx1B) VR=IS VM=1 Version Number Deleted Images */
    public static final int VersionNumberDeletedImages = 0x2005001B;

    /** (2005,xx1C) VR=IS VM=1 Version Number Deleted Spectra */
    public static final int VersionNumberDeletedSpectra = 0x2005001C;

    /** (2005,xx1D) VR=IS VM=1 Version Number Deleted Blobsets */
    public static final int VersionNumberDeletedBlobsets = 0x2005001D;

    /** (2005,xx1E) VR=UL VM=1 LUT1 Offset */
    public static final int LUT1Offset = 0x2005001E;

    /** (2005,xx1F) VR=UL VM=1 LUT1 Range */
    public static final int LUT1Range = 0x2005001F;

    /** (2005,xx20) VR=UL VM=1 LUT1 Begin Color */
    public static final int LUT1BeginColor = 0x20050020;

    /** (2005,xx21) VR=UL VM=1 LUT1 End Color */
    public static final int LUT1EndColor = 0x20050021;

    /** (2005,xx22) VR=UL VM=1 LUT2 Offset */
    public static final int LUT2Offset = 0x20050022;

    /** (2005,xx23) VR=UL VM=1 LUT2 Range */
    public static final int LUT2Range = 0x20050023;

    /** (2005,xx24) VR=UL VM=1 LUT2 Begin Color */
    public static final int LUT2BeginColor = 0x20050024;

    /** (2005,xx25) VR=UL VM=1 LUT2 End Color */
    public static final int LUT2EndColor = 0x20050025;

    /** (2005,xx26) VR=CS VM=1 Viewing Hardcopy Only */
    public static final int ViewingHardcopyOnly = 0x20050026;

    /** (2005,xx27) VR=CS VM=1  */
    public static final int _2005_xx27_ = 0x20050027;

    /** (2005,xx28) VR=SL VM=1 Number of Label Types */
    public static final int NumberOfLabelTypes = 0x20050028;

    /** (2005,xx29) VR=CS VM=1 Label Type */
    public static final int LabelType = 0x20050029;

    /** (2005,xx2A) VR=CS VM=1 Exam Print Status */
    public static final int ExamPrintStatus = 0x2005002A;

    /** (2005,xx2B) VR=CS VM=1 Exam Export Status */
    public static final int ExamExportStatus = 0x2005002B;

    /** (2005,xx2C) VR=CS VM=1 Exam Storage Commit Status */
    public static final int ExamStorageCommitStatus = 0x2005002C;

    /** (2005,xx2D) VR=CS VM=1 Exam Media Write Status */
    public static final int ExamMediaWriteStatus = 0x2005002D;

    /** (2005,xx2E) VR=FL VM=1 dBdt */
    public static final int DBdt = 0x2005002E;

    /** (2005,xx2F) VR=FL VM=1 Proton SAR */
    public static final int ProtonSAR = 0x2005002F;

    /** (2005,xx30) VR=FL VM=1 Non Proton SAR */
    public static final int NonProtonSAR = 0x20050030;

    /** (2005,xx31) VR=FL VM=1 Local SAR */
    public static final int LocalSAR = 0x20050031;

    /** (2005,xx32) VR=CS VM=1 Safety Override Mode */
    public static final int SafetyOverrideMode = 0x20050032;

    /** (2005,xx33) VR=DT VM=1 EV DVD Job In Params Datetime */
    public static final int EVDVDJobInParamsDatetime = 0x20050033;

    /** (2005,xx34) VR=DT VM=1 DVD Job In Params Volume Label */
    public static final int DVDJobInParamsVolumeLabel = 0x20050034;

    /** (2005,xx35) VR=CS VM=1 Spectro Examcard */
    public static final int SpectroExamcard = 0x20050035;

    /** (2005,xx36) VR=UI VM=1 Referenced Series Instance UID */
    public static final int ReferencedSeriesInstanceUID = 0x20050036;

    /** (2005,xx37) VR=CS VM=1 Color LUT Type */
    public static final int ColorLUTType = 0x20050037;

    /** (2005,xx38) VR=LT VM=1 ? */
    public static final int _2005_xx38_ = 0x20050038;

    /** (2005,xx39) VR=LT VM=1 ? */
    public static final int _2005_xx39_ = 0x20050039;

    /** (2005,xx3A) VR=LT VM=1 Data Dictionary Contents Version */
    public static final int DataDictionaryContentsVersion = 0x2005003A;

    /** (2005,xx3B) VR=CS VM=1 Is Coil Survey */
    public static final int IsCoilSurvey = 0x2005003B;

    /** (2005,xx3C) VR=FL VM=1 Stack Table Position Longitudinal */
    public static final int StackTablePosLong = 0x2005003C;

    /** (2005,xx3D) VR=FL VM=1 Stack Table Position Lateral */
    public static final int StackTablePosLat = 0x2005003D;

    /** (2005,xx3E) VR=FL VM=1 Stack Posterior Coil Position */
    public static final int StackPosteriorCoilPos = 0x2005003E;

    /** (2005,xx3F) VR=CS VM=1 Active Implantable Medical Device Limits Applied */
    public static final int AIMDLimitsApplied = 0x2005003F;

    /** (2005,xx40) VR=FL VM=1 Active Implantable Medical Device Head SAR Limit */
    public static final int AIMDHeadSARLimit = 0x20050040;

    /** (2005,xx41) VR=FL VM=1 Active Implantable Medical Device Whole Body SAR Limit */
    public static final int AIMDWholeBodySARLimit = 0x20050041;

    /** (2005,xx42) VR=FL VM=1 Active Implantable Medical Device B1 RMS Limit */
    public static final int AIMDB1RMSLimit = 0x20050042;

    /** (2005,xx43) VR=FL VM=1 Active Implantable Medical Device dbDt Limit */
    public static final int AIMDdbDtLimit = 0x20050043;

    /** (2005,xx44) VR=IS VM=1 TFE Factor */
    public static final int TFEFactor = 0x20050044;

    /** (2005,xx45) VR=CS VM=1 Attenuation Correction */
    public static final int AttenuationCorrection = 0x20050045;

    /** (2005,xx46) VR=FL VM=1 FWHM Shim */
    public static final int FWHMShim = 0x20050046;

    /** (2005,xx47) VR=FL VM=1 Power Optimization */
    public static final int PowerOptimization = 0x20050047;

    /** (2005,xx48) VR=FL VM=1 Coil Q */
    public static final int CoilQ = 0x20050048;

    /** (2005,xx49) VR=FL VM=1 Receiver Gain */
    public static final int ReceiverGain = 0x20050049;

    /** (2005,xx4A) VR=FL VM=1 Data Window Duration */
    public static final int DataWindowDuration = 0x2005004A;

    /** (2005,xx4B) VR=FL VM=1 Mixing Time */
    public static final int MixingTime = 0x2005004B;

    /** (2005,xx4C) VR=FL VM=1 First Echo Time */
    public static final int FirstEchoTime = 0x2005004C;

    /** (2005,xx4D) VR=CS VM=1 Is B0 Series */
    public static final int IsB0Series = 0x2005004D;

    /** (2005,xx4E) VR=CS VM=1 Is B1 Series */
    public static final int IsB1Series = 0x2005004E;

    /** (2005,xx4F) VR=CS VM=1 Volume Select */
    public static final int VolumeSelect = 0x2005004F;

    /** (2005,xx50) VR=SS VM=1 Number of Patient Other IDs */
    public static final int NumberOfPatientOtherIDs = 0x20050050;

    /** (2005,xx51) VR=IS VM=1 Original Series Number */
    public static final int OriginalSeriesNumber = 0x20050051;

    /** (2005,xx52) VR=UI VM=1 Original Series Instance UID */
    public static final int OriginalSeriesInstanceUID = 0x20050052;

    /** (2005,xx53) VR=CS VM=1 Split Series Job Params */
    public static final int SplitSeriesJobParams = 0x20050053;

    /** (2005,xx54) VR=SS VM=1 Preferred Dimension for Splitting */
    public static final int PreferredDimensionForSplitting = 0x20050054;

    /** (2005,xx55) VR=FD VM=3 Velocity Encoding Direction */
    public static final int VelocityEncodingDirection = 0x20050055;

    /** (2005,xx56) VR=SS VM=1 Contrast/Bolus Number of Injections */
    public static final int ContrastBolusNumberOfInjections = 0x20050056;

    /** (2005,xx57) VR=LT VM=1 Contrast/Bolus Agent Code */
    public static final int ContrastBolusAgentCode = 0x20050057;

    /** (2005,xx58) VR=LT VM=1 Contrast/Bolus Administration Route Code */
    public static final int ContrastBolusAdminRouteCode = 0x20050058;

    /** (2005,xx59) VR=DS VM=1 Contrast/Bolus Volume */
    public static final int ContrastBolusVolume = 0x20050059;

    /** (2005,xx5A) VR=DS VM=1 Contrast/Bolus Ingredient Concentration */
    public static final int ContrastBolusIngredientConcentration = 0x2005005A;

    /** (2005,xx5B) VR=IS VM=1 Contrast/Bolus Dynamic Number */
    public static final int ContrastBolusDynamicNumber = 0x2005005B;

    /** (2005,xx5C) VR=SQ VM=1 Contrast/Bolus Sequence */
    public static final int ContrastBolusSequence = 0x2005005C;

    /** (2005,xx5D) VR=IS VM=1 Contrast/Bolus ID */
    public static final int ContrastBolusID = 0x2005005D;

    /** (2005,xx60) VR=CS VM=1 LUT to RGB Job Params */
    public static final int LUTToRGBJobParams = 0x20050060;

    /** (2005,xx90) VR=SQ VM=1 Original VOI LUT Sequence */
    public static final int OriginalVOILUTSequence = 0x20050090;

    /** (2005,xx91) VR=SQ VM=1 Original Modality LUT Sequence */
    public static final int OriginalModalityLUTSequence = 0x20050091;

    /** (2005,xx92) VR=FL VM=1 Specific Energy Dose */
    public static final int SpecificEnergyDose = 0x20050092;

}
