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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_VA0__GEN;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.TotalMeasurementTimeNominal:
                return "TotalMeasurementTimeNominal";

            case PrivateTag.TotalMeasurementTimeCurrent:
                return "TotalMeasurementTimeCurrent";

            case PrivateTag.StartDelayTime:
                return "StartDelayTime";

            case PrivateTag.DwellTime:
                return "DwellTime";

            case PrivateTag.NumberOfPhases:
                return "NumberOfPhases";

            case PrivateTag.SequenceControlMask:
                return "SequenceControlMask";

            case PrivateTag.MeasurementStatusMask:
                return "MeasurementStatusMask";

            case PrivateTag.NumberOfFourierLinesNominal:
                return "NumberOfFourierLinesNominal";

            case PrivateTag.NumberOfFourierLinesCurrent:
                return "NumberOfFourierLinesCurrent";

            case PrivateTag.NumberOfFourierLinesAfterZero:
                return "NumberOfFourierLinesAfterZero";

            case PrivateTag.FirstMeasuredFourierLine:
                return "FirstMeasuredFourierLine";

            case PrivateTag.AcquisitionColumns:
                return "AcquisitionColumns";

            case PrivateTag.ReconstructionColumns:
                return "ReconstructionColumns";

            case PrivateTag.ArrayCoilElementNumber:
                return "ArrayCoilElementNumber";

            case PrivateTag.ArrayCoilElementSelectMask:
                return "ArrayCoilElementSelectMask";

            case PrivateTag.ArrayCoilElementDataMask:
                return "ArrayCoilElementDataMask";

            case PrivateTag.ArrayCoilElementToADCConnect:
                return "ArrayCoilElementToADCConnect";

            case PrivateTag.ArrayCoilElementNoiseLevel:
                return "ArrayCoilElementNoiseLevel";

            case PrivateTag.ArrayCoilADCPairNumber:
                return "ArrayCoilADCPairNumber";

            case PrivateTag.ArrayCoilCombinationMask:
                return "ArrayCoilCombinationMask";

            case PrivateTag.NumberOfAveragesCurrent:
                return "NumberOfAveragesCurrent";

            case PrivateTag.FlipAngle:
                return "FlipAngle";

            case PrivateTag.NumberOfPrescans:
                return "NumberOfPrescans";

            case PrivateTag.FilterTypeForRawData:
                return "FilterTypeForRawData";

            case PrivateTag.FilterParameterForRawData:
                return "FilterParameterForRawData";

            case PrivateTag.FilterTypeForImageData:
                return "FilterTypeForImageData";

            case PrivateTag.FilterParameterForImageData:
                return "FilterParameterForImageData";

            case PrivateTag.FilterTypeForPhaseCorrection:
                return "FilterTypeForPhaseCorrection";

            case PrivateTag.FilterParameterForPhaseCorrection:
                return "FilterParameterForPhaseCorrection";

            case PrivateTag.NormalizationFilterTypeForImageData:
                return "NormalizationFilterTypeForImageData";

            case PrivateTag.NormalizationFilterParameterForImageData:
                return "NormalizationFilterParameterForImageData";

            case PrivateTag.NumberOfSaturationRegions:
                return "NumberOfSaturationRegions";

            case PrivateTag.SaturationPhaseEncodingVectorSagittalComponent:
                return "SaturationPhaseEncodingVectorSagittalComponent";

            case PrivateTag.SaturationReadoutVectorSagittalComponent:
                return "SaturationReadoutVectorSagittalComponent";

            case PrivateTag.EPIStimulationMonitorMode:
                return "EPIStimulationMonitorMode";

            case PrivateTag.ImageRotationAngle:
                return "ImageRotationAngle";

            case PrivateTag.CoilIDMask:
                return "CoilIDMask";

            case PrivateTag.CoilClassMask:
                return "CoilClassMask";

            case PrivateTag.CoilPosition:
                return "CoilPosition";

            case PrivateTag.EPIReconstructionPhase:
                return "EPIReconstructionPhase";

            case PrivateTag.EPIReconstructionSlope:
                return "EPIReconstructionSlope";

            case PrivateTag.PhaseCorrectionRowsSequence:
                return "PhaseCorrectionRowsSequence";

            case PrivateTag.PhaseCorrectionColumnsSequence:
                return "PhaseCorrectionColumnsSequence";

            case PrivateTag.PhaseCorrectionRowsReconstruction:
                return "PhaseCorrectionRowsReconstruction";

            case PrivateTag.PhaseCorrectionColumnsReconstruction:
                return "PhaseCorrectionColumnsReconstruction";

            case PrivateTag.NumberOf3DRawPartitionsNominal:
                return "NumberOf3DRawPartitionsNominal";

            case PrivateTag.NumberOf3DRawPartitionsCurrent:
                return "NumberOf3DRawPartitionsCurrent";

            case PrivateTag.NumberOf3DImagePartitions:
                return "NumberOf3DImagePartitions";

            case PrivateTag.Actual3DImagePartitionNumber:
                return "Actual3DImagePartitionNumber";

            case PrivateTag.SlabThickness:
                return "SlabThickness";

            case PrivateTag.NumberOfSlicesNominal:
                return "NumberOfSlicesNominal";

            case PrivateTag.NumberOfSlicesCurrent:
                return "NumberOfSlicesCurrent";

            case PrivateTag.CurrentSliceNumber:
                return "CurrentSliceNumber";

            case PrivateTag.CurrentGroupNumber:
                return "CurrentGroupNumber";

            case PrivateTag.CurrentSliceDistanceFactor:
                return "CurrentSliceDistanceFactor";

            case PrivateTag.MIPStartRow:
                return "MIPStartRow";

            case PrivateTag.MIPStopRow:
                return "MIPStopRow";

            case PrivateTag.MIPStartColumn:
                return "MIPStartColumn";

            case PrivateTag.MIPStopColumn:
                return "MIPStopColumn";

            case PrivateTag.MIPStartSlice:
                return "MIPStartSlice";

            case PrivateTag.MIPStopSlice:
                return "MIPStopSlice";

            case PrivateTag.OrderofSlices:
                return "OrderofSlices";

            case PrivateTag.SignalMask:
                return "SignalMask";

            case PrivateTag.DelayAfterTrigger:
                return "DelayAfterTrigger";

            case PrivateTag.RRInterval:
                return "RRInterval";

            case PrivateTag.NumberOfTriggerPulses:
                return "NumberOfTriggerPulses";

            case PrivateTag.RepetitionTimeEffective:
                return "RepetitionTimeEffective";

            case PrivateTag.GatePhase:
                return "GatePhase";

            case PrivateTag.GateThreshold:
                return "GateThreshold";

            case PrivateTag.GatedRatio:
                return "GatedRatio";

            case PrivateTag.NumberOfInterpolatedImages:
                return "NumberOfInterpolatedImages";

            case PrivateTag.NumberOfEchoes:
                return "NumberOfEchoes";

            case PrivateTag.SecondEchoTime:
                return "SecondEchoTime";

            case PrivateTag.SecondRepetitionTime:
                return "SecondRepetitionTime";

            case PrivateTag.CardiacCode:
                return "CardiacCode";

            case PrivateTag.SaturationPhaseEncodingVectorTransverseComponent:
                return "SaturationPhaseEncodingVectorTransverseComponent";

            case PrivateTag.SaturationReadoutVectorTransverseComponent:
                return "SaturationReadoutVectorTransverseComponent";

            case PrivateTag.EPIChangeValueOfMagnitude:
                return "EPIChangeValueOfMagnitude";

            case PrivateTag.EPIChangeValueOfXComponent:
                return "EPIChangeValueOfXComponent";

            case PrivateTag.EPIChangeValueOfYComponent:
                return "EPIChangeValueOfYComponent";

            case PrivateTag.EPIChangeValueOfZComponent:
                return "EPIChangeValueOfZComponent";
        }
        return "";
    }

}
