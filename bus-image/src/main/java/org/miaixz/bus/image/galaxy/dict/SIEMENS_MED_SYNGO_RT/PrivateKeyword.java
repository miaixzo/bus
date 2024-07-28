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
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.PlanType:
            return "PlanType";
        case PrivateTag.DeliveryWarningDoseComment:
            return "DeliveryWarningDoseComment";
        case PrivateTag.ImagerOrganProgram:
            return "ImagerOrganProgram";
        case PrivateTag.XAImageSID:
            return "XAImageSID";
        case PrivateTag.XAImageReceptorAngle:
            return "XAImageReceptorAngle";
        case PrivateTag.TargetPrescriptionDoseType:
            return "TargetPrescriptionDoseType";
        case PrivateTag.ReferencedTargetROINumber:
            return "ReferencedTargetROINumber";
        case PrivateTag.RTPrivateData:
            return "RTPrivateData";
        case PrivateTag.ReferencedSiemensNonImageSequence:
            return "ReferencedSiemensNonImageSequence";
        case PrivateTag.VerificationMethod:
            return "VerificationMethod";
        case PrivateTag.AlternativeTreatmentMachineNameSequence:
            return "AlternativeTreatmentMachineNameSequence";
        case PrivateTag.ImagerAngularAngle:
            return "ImagerAngularAngle";
        case PrivateTag.ImagerIsocentricAngle:
            return "ImagerIsocentricAngle";
        case PrivateTag.ImagerVerticalPosition:
            return "ImagerVerticalPosition";
        case PrivateTag.ImagerLongitudinalPosition:
            return "ImagerLongitudinalPosition";
        case PrivateTag.ImagerLateralPosition:
            return "ImagerLateralPosition";
        case PrivateTag.ImagerAngularRotationDirection:
            return "ImagerAngularRotationDirection";
        case PrivateTag.ImagerIsocentricRotationDirection:
            return "ImagerIsocentricRotationDirection";
        case PrivateTag.RequestedScanningSpotSize:
            return "RequestedScanningSpotSize";
        case PrivateTag.ImagerOrbitalAngle:
            return "ImagerOrbitalAngle";
        case PrivateTag.ImagingTechnique:
            return "ImagingTechnique";
        case PrivateTag.ImagerOrbitalRotationDirection:
            return "ImagerOrbitalRotationDirection";
        case PrivateTag.PatientBarcode:
            return "PatientBarcode";
        case PrivateTag.BeamModifierType:
            return "BeamModifierType";
        case PrivateTag.SourceToRangeShifterDistance:
            return "SourceToRangeShifterDistance";
        case PrivateTag.TreatmentRoomName:
            return "TreatmentRoomName";
        case PrivateTag.OrderedReferencedBeamNumbers:
            return "OrderedReferencedBeamNumbers";
        case PrivateTag.FractionDeliveryNotes:
            return "FractionDeliveryNotes";
        case PrivateTag.RecordID:
            return "RecordID";
        case PrivateTag.FractionGroupCode:
            return "FractionGroupCode";
        case PrivateTag.ReferencedTreatmentBeamNumber:
            return "ReferencedTreatmentBeamNumber";
        case PrivateTag.FractionDoseToDoseReference:
            return "FractionDoseToDoseReference";
        case PrivateTag.ReferencedTargetROISequence:
            return "ReferencedTargetROISequence";
        case PrivateTag.TargetROILateralExpansionMargin:
            return "TargetROILateralExpansionMargin";
        case PrivateTag.TargetROIDistalExpansionMargin:
            return "TargetROIDistalExpansionMargin";
        case PrivateTag.TargetROIProximalExpansionMargin:
            return "TargetROIProximalExpansionMargin";
        case PrivateTag.ScanGridLateralDistance:
            return "ScanGridLateralDistance";
        case PrivateTag.ScanGridLongitudinalDistance:
            return "ScanGridLongitudinalDistance";
        case PrivateTag.BeamWeight:
            return "BeamWeight";
        case PrivateTag.PointOnPlane:
            return "PointOnPlane";
        case PrivateTag.NormVectorOfPlane:
            return "NormVectorOfPlane";
        case PrivateTag.PlaneThickness:
            return "PlaneThickness";
        case PrivateTag.TreatmentApprovalSequence:
            return "TreatmentApprovalSequence";
        case PrivateTag.TreatmentRoomTemperature:
            return "TreatmentRoomTemperature";
        case PrivateTag.TreatmentRoomAirPressure:
            return "TreatmentRoomAirPressure";
        case PrivateTag.SplitPlaneSequence:
            return "SplitPlaneSequence";
        case PrivateTag.DisplayColor:
            return "DisplayColor";
        case PrivateTag.BeamGroupName:
            return "BeamGroupName";
        case PrivateTag.ExpireDateTime:
            return "ExpireDateTime";
        case PrivateTag.ChecksumEncryptionCode:
            return "ChecksumEncryptionCode";
        case PrivateTag.SetupType:
            return "SetupType";
        case PrivateTag.NumberofTherapyDetectors:
            return "NumberofTherapyDetectors";
        case PrivateTag.TherapyDetectorSequence:
            return "TherapyDetectorSequence";
        case PrivateTag.TherapyDetectorNumber:
            return "TherapyDetectorNumber";
        case PrivateTag.TherapyDetectorSetupID:
            return "TherapyDetectorSetupID";
        case PrivateTag.TherapyDetectorSettingsSequence:
            return "TherapyDetectorSettingsSequence";
        case PrivateTag.ReferencedTherapyDetectorNumber:
            return "ReferencedTherapyDetectorNumber";
        case PrivateTag.TherapyDetectorPosition:
            return "TherapyDetectorPosition";
        case PrivateTag.ScanSpotResumptionIndex:
            return "ScanSpotResumptionIndex";
        case PrivateTag.PhantomDetectorMeasurements:
            return "PhantomDetectorMeasurements";
        case PrivateTag.TreatmentEvents:
            return "TreatmentEvents";
        case PrivateTag.DoseOptimizationConstraintSequence:
            return "DoseOptimizationConstraintSequence";
        case PrivateTag.TargetMaximumDoseConstraint:
            return "TargetMaximumDoseConstraint";
        case PrivateTag.TargetMaximumDoseConstraintWeight:
            return "TargetMaximumDoseConstraintWeight";
        case PrivateTag.TargetMinimumDoseConstraint:
            return "TargetMinimumDoseConstraint";
        case PrivateTag.TargetMinimumDoseConstraintWeight:
            return "TargetMinimumDoseConstraintWeight";
        case PrivateTag.OrganAtRiskMaximumDoseConstraint:
            return "OrganAtRiskMaximumDoseConstraint";
        case PrivateTag.OrganAtRiskMaximumDoseConstraintWeight:
            return "OrganAtRiskMaximumDoseConstraintWeight";
        case PrivateTag.DVHConstraintSequence:
            return "DVHConstraintSequence";
        case PrivateTag.DVHConstraintVolumeLimit:
            return "DVHConstraintVolumeLimit";
        case PrivateTag.DVHConstraintVolumeUnits:
            return "DVHConstraintVolumeUnits";
        case PrivateTag.DVHConstraintDoseLimit:
            return "DVHConstraintDoseLimit";
        case PrivateTag.DVHConstraintDirection:
            return "DVHConstraintDirection";
        case PrivateTag.DVHConstraintWeight:
            return "DVHConstraintWeight";
        case PrivateTag.OptimizedDoseType:
            return "OptimizedDoseType";
        case PrivateTag.DoseCalculationandOptimizationParameters:
            return "DoseCalculationandOptimizationParameters";
        case PrivateTag.AppliedRenormalizationFactor:
            return "AppliedRenormalizationFactor";
        case PrivateTag.DoseConstraintStatus:
            return "DoseConstraintStatus";
        case PrivateTag.OrganAtRiskMinimumDoseConstraint:
            return "OrganAtRiskMinimumDoseConstraint";
        case PrivateTag.OrganAtRiskMinimumDoseConstraintWeight:
            return "OrganAtRiskMinimumDoseConstraintWeight";
        case PrivateTag.BaseDataGroupSequence:
            return "BaseDataGroupSequence";
        case PrivateTag.PhysicsBaseDataGroupID:
            return "PhysicsBaseDataGroupID";
        case PrivateTag.BiologicalBaseDataGroupID:
            return "BiologicalBaseDataGroupID";
        case PrivateTag.ImagingBaseDataGroupID:
            return "ImagingBaseDataGroupID";
        case PrivateTag.DosimetricBaseDataGroupID:
            return "DosimetricBaseDataGroupID";
        case PrivateTag.ConfigurationBaseline:
            return "ConfigurationBaseline";
        case PrivateTag.ConfigurationObjects:
            return "ConfigurationObjects";
        case PrivateTag.ConfidenceMeasureUnits:
            return "ConfidenceMeasureUnits";
        case PrivateTag.ConfidenceMeasureValue:
            return "ConfidenceMeasureValue";
        case PrivateTag.ImagerOrganProgramName:
            return "ImagerOrganProgramName";
        case PrivateTag.BodyRegion:
            return "BodyRegion";
        case PrivateTag.PrefinalizedStatus:
            return "PrefinalizedStatus";
        case PrivateTag.DosimetricChecksumEncryptionCode:
            return "DosimetricChecksumEncryptionCode";
        case PrivateTag.ReferencedReportSequence:
            return "ReferencedReportSequence";
        case PrivateTag.MaintainChecksumCompatibility:
            return "MaintainChecksumCompatibility";
        case PrivateTag.DoseStatisticalUncertainty:
            return "DoseStatisticalUncertainty";
        case PrivateTag.InterpretedRadiationType:
            return "InterpretedRadiationType";
        }
        return "";
    }

}
