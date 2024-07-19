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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SYNGO_RT;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "SIEMENS MED SYNGO RT";

    /** (300B,xx10) VR=CS VM=1 Plan Type */
    public static final int PlanType = 0x300B0010;

    /** (300B,xx11) VR=LT VM=1 Delivery Warning Dose Comment */
    public static final int DeliveryWarningDoseComment = 0x300B0011;

    /** (300B,xx12) VR=LO VM=1 Imager Organ Program */
    public static final int ImagerOrganProgram = 0x300B0012;

    /** (300B,xx13) VR=DS VM=1 XA Image SID */
    public static final int XAImageSID = 0x300B0013;

    /** (300B,xx14) VR=DS VM=1 XA Image Receptor Angle */
    public static final int XAImageReceptorAngle = 0x300B0014;

    /** (300B,xx15) VR=CS VM=1 Target Prescription Dose Type */
    public static final int TargetPrescriptionDoseType = 0x300B0015;

    /** (300B,xx16) VR=IS VM=1 Referenced Target ROI Number */
    public static final int ReferencedTargetROINumber = 0x300B0016;

    /** (300B,xx17) VR=UT VM=1 RT Private Data */
    public static final int RTPrivateData = 0x300B0017;

    /** (300B,xx18) VR=SQ VM=1 Referenced Siemens Non Image Sequence */
    public static final int ReferencedSiemensNonImageSequence = 0x300B0018;

    /** (300B,xx19) VR=CS VM=1 Verification Method */
    public static final int VerificationMethod = 0x300B0019;

    /** (300B,xx20) VR=SQ VM=1 Alternative Treatment Machine Name Sequence */
    public static final int AlternativeTreatmentMachineNameSequence = 0x300B0020;

    /** (300B,xx24) VR=DS VM=1 Imager Angular Angle */
    public static final int ImagerAngularAngle = 0x300B0024;

    /** (300B,xx25) VR=DS VM=1 Imager Isocentric Angle */
    public static final int ImagerIsocentricAngle = 0x300B0025;

    /** (300B,xx26) VR=DS VM=1 Imager Vertical Position */
    public static final int ImagerVerticalPosition = 0x300B0026;

    /** (300B,xx27) VR=DS VM=1 Imager Longitudinal Position */
    public static final int ImagerLongitudinalPosition = 0x300B0027;

    /** (300B,xx28) VR=DS VM=1 Imager Lateral Position */
    public static final int ImagerLateralPosition = 0x300B0028;

    /** (300B,xx29) VR=CS VM=1 Imager Angular Rotation Direction */
    public static final int ImagerAngularRotationDirection = 0x300B0029;

    /** (300B,xx2A) VR=CS VM=1 Imager Isocentric Rotation Direction */
    public static final int ImagerIsocentricRotationDirection = 0x300B002A;

    /** (300B,xx2B) VR=DS VM=1 Requested Scanning Spot Size */
    public static final int RequestedScanningSpotSize = 0x300B002B;

    /** (300B,xx2C) VR=DS VM=1 Imager Orbital Angle */
    public static final int ImagerOrbitalAngle = 0x300B002C;

    /** (300B,xx2E) VR=CS VM=1 Imaging Technique */
    public static final int ImagingTechnique = 0x300B002E;

    /** (300B,xx2F) VR=CS VM=1 Imager Orbital Rotation Direction */
    public static final int ImagerOrbitalRotationDirection = 0x300B002F;

    /** (300B,xx30) VR=LO VM=1 Patient Barcode */
    public static final int PatientBarcode = 0x300B0030;

    /** (300B,xx31) VR=SH VM=3 Beam Modifier Type */
    public static final int BeamModifierType = 0x300B0031;

    /** (300B,xx32) VR=FL VM=3 Source to Range Shifter Distance */
    public static final int SourceToRangeShifterDistance = 0x300B0032;

    /** (300B,xx33) VR=SH VM=3 Treatment Room Name */
    public static final int TreatmentRoomName = 0x300B0033;

    /** (300B,xx60) VR=IS VM=1-n Ordered Referenced Beam Numbers */
    public static final int OrderedReferencedBeamNumbers = 0x300B0060;

    /** (300B,xx66) VR=LT VM=1 Fraction Delivery Notes */
    public static final int FractionDeliveryNotes = 0x300B0066;

    /** (300B,xx67) VR=LO VM=1 Record ID */
    public static final int RecordID = 0x300B0067;

    /** (300B,xx76) VR=CS VM=1 Fraction Group Code */
    public static final int FractionGroupCode = 0x300B0076;

    /** (300B,xx77) VR=IS VM=1 Referenced Treatment Beam Number */
    public static final int ReferencedTreatmentBeamNumber = 0x300B0077;

    /** (300B,xx78) VR=DS VM=1 Fraction Dose to Dose Reference */
    public static final int FractionDoseToDoseReference = 0x300B0078;

    /** (300B,xx80) VR=SQ VM=1 Referenced Target ROI Sequence */
    public static final int ReferencedTargetROISequence = 0x300B0080;

    /** (300B,xx82) VR=DS VM=1 Target ROI Lateral Expansion Margin */
    public static final int TargetROILateralExpansionMargin = 0x300B0082;

    /** (300B,xx83) VR=DS VM=1 Target ROI Distal Expansion Margin */
    public static final int TargetROIDistalExpansionMargin = 0x300B0083;

    /** (300B,xx84) VR=DS VM=1 Target ROI Proximal Expansion Margin */
    public static final int TargetROIProximalExpansionMargin = 0x300B0084;

    /** (300B,xx85) VR=DS VM=2 Scan Grid Lateral Distance */
    public static final int ScanGridLateralDistance = 0x300B0085;

    /** (300B,xx86) VR=DS VM=1 Scan Grid Longitudinal Distance */
    public static final int ScanGridLongitudinalDistance = 0x300B0086;

    /** (300B,xx87) VR=DS VM=1 Beam Weight */
    public static final int BeamWeight = 0x300B0087;

    /** (300B,xx89) VR=DS VM=3 Point on Plane */
    public static final int PointOnPlane = 0x300B0089;

    /** (300B,xx8A) VR=DS VM=3 Norm Vector of Plane */
    public static final int NormVectorOfPlane = 0x300B008A;

    /** (300B,xx8B) VR=DS VM=1 Plane Thickness */
    public static final int PlaneThickness = 0x300B008B;

    /** (300B,xx90) VR=SQ VM=1 Treatment Approval Sequence */
    public static final int TreatmentApprovalSequence = 0x300B0090;

    /** (300B,xx98) VR=DS VM=1 Treatment Room Temperature */
    public static final int TreatmentRoomTemperature = 0x300B0098;

    /** (300B,xx99) VR=DS VM=1 Treatment Room Air Pressure */
    public static final int TreatmentRoomAirPressure = 0x300B0099;

    /** (300B,xx9B) VR=SQ VM=1 Split Plane Sequence */
    public static final int SplitPlaneSequence = 0x300B009B;

    /** (300B,xx9C) VR=IS VM=1 Display Color */
    public static final int DisplayColor = 0x300B009C;

    /** (300B,xx9D) VR=SH VM=1 Beam Group Name */
    public static final int BeamGroupName = 0x300B009D;

    /** (300B,xxA0) VR=DT VM=1 Expire DateTime */
    public static final int ExpireDateTime = 0x300B00A0;

    /** (300B,xxA1) VR=OB VM=1 Checksum Encryption Code */
    public static final int ChecksumEncryptionCode = 0x300B00A1;

    /** (300B,xxA2) VR=CS VM=1 Setup Type */
    public static final int SetupType = 0x300B00A2;

    /** (300B,xxA3) VR=IS VM=1 Number of Therapy Detectors */
    public static final int NumberofTherapyDetectors = 0x300B00A3;

    /** (300B,xxA4) VR=SQ VM=1 Therapy Detector Sequence */
    public static final int TherapyDetectorSequence = 0x300B00A4;

    /** (300B,xxA5) VR=IS VM=1 Therapy Detector Number */
    public static final int TherapyDetectorNumber = 0x300B00A5;

    /** (300B,xxA6) VR=SH VM=1 Therapy Detector Setup ID */
    public static final int TherapyDetectorSetupID = 0x300B00A6;

    /** (300B,xxA7) VR=SQ VM=1 Therapy Detector Settings Sequence */
    public static final int TherapyDetectorSettingsSequence = 0x300B00A7;

    /** (300B,xxA8) VR=IS VM=1 Referenced Therapy Detector Number */
    public static final int ReferencedTherapyDetectorNumber = 0x300B00A8;

    /** (300B,xxA9) VR=DS VM=1 Therapy Detector Position */
    public static final int TherapyDetectorPosition = 0x300B00A9;

    /** (300B,xxB0) VR=IS VM=1 Scan Spot Resumption Index */
    public static final int ScanSpotResumptionIndex = 0x300B00B0;

    /** (300B,xxB1) VR=OW VM=1 Phantom Detector Measurements */
    public static final int PhantomDetectorMeasurements = 0x300B00B1;

    /** (300B,xxB2) VR=UT VM=1 Treatment Events */
    public static final int TreatmentEvents = 0x300B00B2;

    /** (300B,xxC0) VR=SQ VM=1 Dose Optimization Constraint Sequence */
    public static final int DoseOptimizationConstraintSequence = 0x300B00C0;

    /** (300B,xxC8) VR=DS VM=1 Target Maximum Dose Constraint */
    public static final int TargetMaximumDoseConstraint = 0x300B00C8;

    /** (300B,xxC9) VR=DS VM=1 Target Maximum Dose Constraint Weight */
    public static final int TargetMaximumDoseConstraintWeight = 0x300B00C9;

    /** (300B,xxCA) VR=DS VM=1 Target Minimum Dose Constraint */
    public static final int TargetMinimumDoseConstraint = 0x300B00CA;

    /** (300B,xxCB) VR=DS VM=1 Target Minimum Dose Constraint Weight */
    public static final int TargetMinimumDoseConstraintWeight = 0x300B00CB;

    /** (300B,xxCC) VR=DS VM=1 Organ At Risk Maximum Dose Constraint */
    public static final int OrganAtRiskMaximumDoseConstraint = 0x300B00CC;

    /** (300B,xxCD) VR=DS VM=1 Organ At Risk Maximum Dose Constraint Weight */
    public static final int OrganAtRiskMaximumDoseConstraintWeight = 0x300B00CD;

    /** (300B,xxCE) VR=SQ VM=1 DVH Constraint Sequence */
    public static final int DVHConstraintSequence = 0x300B00CE;

    /** (300B,xxCF) VR=DS VM=1 DVH Constraint Volume Limit */
    public static final int DVHConstraintVolumeLimit = 0x300B00CF;

    /** (300B,xxD0) VR=CS VM=1 DVH Constraint Volume Units */
    public static final int DVHConstraintVolumeUnits = 0x300B00D0;

    /** (300B,xxD1) VR=DS VM=1 DVH Constraint Dose Limit */
    public static final int DVHConstraintDoseLimit = 0x300B00D1;

    /** (300B,xxD3) VR=CS VM=1 DVH Constraint Direction */
    public static final int DVHConstraintDirection = 0x300B00D3;

    /** (300B,xxD4) VR=DS VM=1 DVH Constraint Weight */
    public static final int DVHConstraintWeight = 0x300B00D4;

    /** (300B,xxD5) VR=CS VM=1 Optimized Dose Type */
    public static final int OptimizedDoseType = 0x300B00D5;

    /** (300B,xxD6) VR=UT VM=1 Dose Calculation and Optimization Parameters */
    public static final int DoseCalculationandOptimizationParameters = 0x300B00D6;

    /** (300B,xxD7) VR=DS VM=1 Applied Renormalization Factor */
    public static final int AppliedRenormalizationFactor = 0x300B00D7;

    /** (300B,xxD8) VR=CS VM=1 Dose Constraint Status */
    public static final int DoseConstraintStatus = 0x300B00D8;

    /** (300B,xxD9) VR=DS VM=1 Organ At Risk Minimum Dose Constraint */
    public static final int OrganAtRiskMinimumDoseConstraint = 0x300B00D9;

    /** (300B,xxDA) VR=DS VM=1 Organ At Risk Minimum Dose Constraint Weight */
    public static final int OrganAtRiskMinimumDoseConstraintWeight = 0x300B00DA;

    /** (300B,xxE0) VR=SQ VM=1 Base Data Group Sequence */
    public static final int BaseDataGroupSequence = 0x300B00E0;

    /** (300B,xxE1) VR=CS VM=1 Physics Base Data Group ID */
    public static final int PhysicsBaseDataGroupID = 0x300B00E1;

    /** (300B,xxE2) VR=CS VM=1 Biological Base Data Group ID */
    public static final int BiologicalBaseDataGroupID = 0x300B00E2;

    /** (300B,xxE3) VR=CS VM=1 Imaging Base Data Group ID */
    public static final int ImagingBaseDataGroupID = 0x300B00E3;

    /** (300B,xxE4) VR=CS VM=1 Dosimetric Base Data Group ID */
    public static final int DosimetricBaseDataGroupID = 0x300B00E4;

    /** (300B,xxE5) VR=OB VM=1 Configuration Baseline */
    public static final int ConfigurationBaseline = 0x300B00E5;

    /** (300B,xxE6) VR=OB VM=1 Configuration Objects */
    public static final int ConfigurationObjects = 0x300B00E6;

    /** (300B,xxE7) VR=CS VM=1 Confidence Measure Units */
    public static final int ConfidenceMeasureUnits = 0x300B00E7;

    /** (300B,xxE8) VR=DS VM=1 Confidence Measure Value */
    public static final int ConfidenceMeasureValue = 0x300B00E8;

    /** (300B,xxE9) VR=LO VM=1 Imager Organ Program Name */
    public static final int ImagerOrganProgramName = 0x300B00E9;

    /** (300B,xxEB) VR=CS VM=1 Body Region */
    public static final int BodyRegion = 0x300B00EB;

    /** (300B,xxEC) VR=CS VM=1 Prefinalized Status */
    public static final int PrefinalizedStatus = 0x300B00EC;

    /** (300B,xxED) VR=OB VM=1 Dosimetric Checksum Encryption Code */
    public static final int DosimetricChecksumEncryptionCode = 0x300B00ED;

    /** (300B,xxEE) VR=SQ VM=1 Referenced Report Sequence */
    public static final int ReferencedReportSequence = 0x300B00EE;

    /** (300B,xxEF) VR=CS VM=1 Maintain Checksum Compatibility */
    public static final int MaintainChecksumCompatibility = 0x300B00EF;

    /** (300B,xxF0) VR=DS VM=1 Dose Statistical Uncertainty */
    public static final int DoseStatisticalUncertainty = 0x300B00F0;

    /** (300B,xxF1) VR=CS VM=1 Interpreted Radiation Type */
    public static final int InterpretedRadiationType = 0x300B00F1;

}
