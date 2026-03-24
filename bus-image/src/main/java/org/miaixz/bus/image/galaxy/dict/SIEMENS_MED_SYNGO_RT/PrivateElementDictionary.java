/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SYNGO_RT;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

            case PrivateTag.PlanType:
            case PrivateTag.TargetPrescriptionDoseType:
            case PrivateTag.VerificationMethod:
            case PrivateTag.ImagerAngularRotationDirection:
            case PrivateTag.ImagerIsocentricRotationDirection:
            case PrivateTag.ImagingTechnique:
            case PrivateTag.ImagerOrbitalRotationDirection:
            case PrivateTag.FractionGroupCode:
            case PrivateTag.SetupType:
            case PrivateTag.DVHConstraintVolumeUnits:
            case PrivateTag.DVHConstraintDirection:
            case PrivateTag.OptimizedDoseType:
            case PrivateTag.DoseConstraintStatus:
            case PrivateTag.PhysicsBaseDataGroupID:
            case PrivateTag.BiologicalBaseDataGroupID:
            case PrivateTag.ImagingBaseDataGroupID:
            case PrivateTag.DosimetricBaseDataGroupID:
            case PrivateTag.ConfidenceMeasureUnits:
            case PrivateTag.BodyRegion:
            case PrivateTag.PrefinalizedStatus:
            case PrivateTag.MaintainChecksumCompatibility:
            case PrivateTag.InterpretedRadiationType:
                return VR.CS;

            case PrivateTag.XAImageSID:
            case PrivateTag.XAImageReceptorAngle:
            case PrivateTag.ImagerAngularAngle:
            case PrivateTag.ImagerIsocentricAngle:
            case PrivateTag.ImagerVerticalPosition:
            case PrivateTag.ImagerLongitudinalPosition:
            case PrivateTag.ImagerLateralPosition:
            case PrivateTag.RequestedScanningSpotSize:
            case PrivateTag.ImagerOrbitalAngle:
            case PrivateTag.FractionDoseToDoseReference:
            case PrivateTag.TargetROILateralExpansionMargin:
            case PrivateTag.TargetROIDistalExpansionMargin:
            case PrivateTag.TargetROIProximalExpansionMargin:
            case PrivateTag.ScanGridLateralDistance:
            case PrivateTag.ScanGridLongitudinalDistance:
            case PrivateTag.BeamWeight:
            case PrivateTag.PointOnPlane:
            case PrivateTag.NormVectorOfPlane:
            case PrivateTag.PlaneThickness:
            case PrivateTag.TreatmentRoomTemperature:
            case PrivateTag.TreatmentRoomAirPressure:
            case PrivateTag.TherapyDetectorPosition:
            case PrivateTag.TargetMaximumDoseConstraint:
            case PrivateTag.TargetMaximumDoseConstraintWeight:
            case PrivateTag.TargetMinimumDoseConstraint:
            case PrivateTag.TargetMinimumDoseConstraintWeight:
            case PrivateTag.OrganAtRiskMaximumDoseConstraint:
            case PrivateTag.OrganAtRiskMaximumDoseConstraintWeight:
            case PrivateTag.DVHConstraintVolumeLimit:
            case PrivateTag.DVHConstraintDoseLimit:
            case PrivateTag.DVHConstraintWeight:
            case PrivateTag.AppliedRenormalizationFactor:
            case PrivateTag.OrganAtRiskMinimumDoseConstraint:
            case PrivateTag.OrganAtRiskMinimumDoseConstraintWeight:
            case PrivateTag.ConfidenceMeasureValue:
            case PrivateTag.DoseStatisticalUncertainty:
                return VR.DS;

            case PrivateTag.ExpireDateTime:
                return VR.DT;

            case PrivateTag.SourceToRangeShifterDistance:
                return VR.FL;

            case PrivateTag.ReferencedTargetROINumber:
            case PrivateTag.OrderedReferencedBeamNumbers:
            case PrivateTag.ReferencedTreatmentBeamNumber:
            case PrivateTag.DisplayColor:
            case PrivateTag.NumberofTherapyDetectors:
            case PrivateTag.TherapyDetectorNumber:
            case PrivateTag.ReferencedTherapyDetectorNumber:
            case PrivateTag.ScanSpotResumptionIndex:
                return VR.IS;

            case PrivateTag.ImagerOrganProgram:
            case PrivateTag.PatientBarcode:
            case PrivateTag.RecordID:
            case PrivateTag.ImagerOrganProgramName:
                return VR.LO;

            case PrivateTag.DeliveryWarningDoseComment:
            case PrivateTag.FractionDeliveryNotes:
                return VR.LT;

            case PrivateTag.ChecksumEncryptionCode:
            case PrivateTag.ConfigurationBaseline:
            case PrivateTag.ConfigurationObjects:
            case PrivateTag.DosimetricChecksumEncryptionCode:
                return VR.OB;

            case PrivateTag.PhantomDetectorMeasurements:
                return VR.OW;

            case PrivateTag.BeamModifierType:
            case PrivateTag.TreatmentRoomName:
            case PrivateTag.BeamGroupName:
            case PrivateTag.TherapyDetectorSetupID:
                return VR.SH;

            case PrivateTag.ReferencedSiemensNonImageSequence:
            case PrivateTag.AlternativeTreatmentMachineNameSequence:
            case PrivateTag.ReferencedTargetROISequence:
            case PrivateTag.TreatmentApprovalSequence:
            case PrivateTag.SplitPlaneSequence:
            case PrivateTag.TherapyDetectorSequence:
            case PrivateTag.TherapyDetectorSettingsSequence:
            case PrivateTag.DoseOptimizationConstraintSequence:
            case PrivateTag.DVHConstraintSequence:
            case PrivateTag.BaseDataGroupSequence:
            case PrivateTag.ReferencedReportSequence:
                return VR.SQ;

            case PrivateTag.RTPrivateData:
            case PrivateTag.TreatmentEvents:
            case PrivateTag.DoseCalculationandOptimizationParameters:
                return VR.UT;
        }
        return VR.UN;
    }

}
