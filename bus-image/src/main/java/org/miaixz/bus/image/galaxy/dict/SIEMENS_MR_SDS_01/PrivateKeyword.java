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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_SDS_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.SiemensMRSDSSequence:
                return "SiemensMRSDSSequence";

            case PrivateTag.UsedPatientWeight:
                return "UsedPatientWeight";

            case PrivateTag.SARWholeBody:
                return "SARWholeBody";

            case PrivateTag.MRProtocol:
                return "MRProtocol";

            case PrivateTag.SliceArrayConcatenations:
                return "SliceArrayConcatenations";

            case PrivateTag.RelTablePosition:
                return "RelTablePosition";

            case PrivateTag.CoilForGradient:
                return "CoilForGradient";

            case PrivateTag.LongModelName:
                return "LongModelName";

            case PrivateTag.GradientMode:
                return "GradientMode";

            case PrivateTag.PATModeText:
                return "PATModeText";

            case PrivateTag.SWCorrectionFactor:
                return "SWCorrectionFactor";

            case PrivateTag.RFPowerErrorIndicator:
                return "RFPowerErrorIndicator";

            case PrivateTag.PositivePCSDirections:
                return "PositivePCSDirections";

            case PrivateTag.ProtocolChangeHistory:
                return "ProtocolChangeHistory";

            case PrivateTag.DataFileName:
                return "DataFileName";

            case PrivateTag.Stimlim:
                return "Stimlim";

            case PrivateTag.MRProtocolVersion:
                return "MRProtocolVersion";

            case PrivateTag.PhaseGradientAmplitude:
                return "PhaseGradientAmplitude";

            case PrivateTag.ReadoutOS:
                return "ReadoutOS";

            case PrivateTag.tpulsmax:
                return "tpulsmax";

            case PrivateTag.NumberOfPrescans:
                return "NumberOfPrescans";

            case PrivateTag.MeasurementIndex:
                return "MeasurementIndex";

            case PrivateTag.dBdtThreshold:
                return "dBdtThreshold";

            case PrivateTag.SelectionGradientAmplitude:
                return "SelectionGradientAmplitude";

            case PrivateTag.RFSWDMostCriticalAspect:
                return "RFSWDMostCriticalAspect";

            case PrivateTag.MRPhoenixProtocol:
                return "MRPhoenixProtocol";

            case PrivateTag.CoilString:
                return "CoilString";

            case PrivateTag.SliceResolution:
                return "SliceResolution";

            case PrivateTag.Stimmaxonline:
                return "Stimmaxonline";

            case PrivateTag.OperationModeFlag:
                return "OperationModeFlag";

            case PrivateTag.AutoAlignMatrix:
                return "AutoAlignMatrix";

            case PrivateTag.CoilTuningReflection:
                return "CoilTuningReflection";

            case PrivateTag.RepresentativeImage:
                return "RepresentativeImage";

            case PrivateTag.SequenceFileOwner:
                return "SequenceFileOwner";

            case PrivateTag.RFWatchdogMask:
                return "RFWatchdogMask";

            case PrivateTag.PostProcProtocol:
                return "PostProcProtocol";

            case PrivateTag.TablePositionOrigin:
                return "TablePositionOrigin";

            case PrivateTag.MiscSequenceParam:
                return "MiscSequenceParam";

            case PrivateTag.Isocentered:
                return "Isocentered";

            case PrivateTag.CoilID:
                return "CoilID";

            case PrivateTag.PatReinPattern:
                return "PatReinPattern";

            case PrivateTag.SED:
                return "SED";

            case PrivateTag.SARMostCriticalAspect:
                return "SARMostCriticalAspect";

            case PrivateTag.StimmOnMode:
                return "StimmOnMode";

            case PrivateTag.GradientDelayTime:
                return "GradientDelayTime";

            case PrivateTag.ReadoutGradientAmplitude:
                return "ReadoutGradientAmplitude";

            case PrivateTag.AbsTablePosition:
                return "AbsTablePosition";

            case PrivateTag.RFSWDOperationMode:
                return "RFSWDOperationMode";

            case PrivateTag.CoilForGradient2:
                return "CoilForGradient2";

            case PrivateTag.StimFactor:
                return "StimFactor";

            case PrivateTag.Stimmaxgesnormonline:
                return "Stimmaxgesnormonline";

            case PrivateTag.dBdtmax:
                return "dBdtmax";

            case PrivateTag.TransmitterCalibration:
                return "TransmitterCalibration";

            case PrivateTag.MREVAProtocol:
                return "MREVAProtocol";

            case PrivateTag.dBdtLimit:
                return "dBdtLimit";

            case PrivateTag.VFModelInfo:
                return "VFModelInfo";

            case PrivateTag.PhaseSliceOversampling:
                return "PhaseSliceOversampling";

            case PrivateTag.VFSettings:
                return "VFSettings";

            case PrivateTag.AutoAlignData:
                return "AutoAlignData";

            case PrivateTag.FMRIModelParameters:
                return "FMRIModelParameters";

            case PrivateTag.FMRIModelInfo:
                return "FMRIModelInfo";

            case PrivateTag.FMRIExternalParameters:
                return "FMRIExternalParameters";

            case PrivateTag.FMRIExternalInfo:
                return "FMRIExternalInfo";

            case PrivateTag.B1RMS:
                return "B1RMS";

            case PrivateTag.B1RMSSupervision:
                return "B1RMSSupervision";

            case PrivateTag.TalesReferencePower:
                return "TalesReferencePower";

            case PrivateTag.SafetyStandard:
                return "SafetyStandard";

            case PrivateTag.DICOMImageFlavor:
                return "DICOMImageFlavor";

            case PrivateTag.DICOMAcquisitionContrast:
                return "DICOMAcquisitionContrast";

            case PrivateTag.RFEchoTrainLength4MF:
                return "RFEchoTrainLength4MF";

            case PrivateTag.GradientEchoTrainLength4MF:
                return "GradientEchoTrainLength4MF";

            case PrivateTag.VersionInfo:
                return "VersionInfo";

            case PrivateTag.Laterality4MF:
                return "Laterality4MF";
        }
        return "";
    }

}
