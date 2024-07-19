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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_MR_IMAGING_DD_001_1;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.ImageAngulationAP:
            return "ImageAngulationAP";
        case PrivateTag.ImageAngulationFH:
            return "ImageAngulationFH";
        case PrivateTag.ImageAngulationRL:
            return "ImageAngulationRL";
        case PrivateTag.ImageDisplayOrientation:
            return "ImageDisplayOrientation";
        case PrivateTag.SynergyReconstructionType:
            return "SynergyReconstructionType";
        case PrivateTag.ImageOffcenterAP:
            return "ImageOffcenterAP";
        case PrivateTag.ImageOffcenterFH:
            return "ImageOffcenterFH";
        case PrivateTag.ImageOffCentreRL:
            return "ImageOffCentreRL";
        case PrivateTag.MaxFP:
            return "MaxFP";
        case PrivateTag.MinFP:
            return "MinFP";
        case PrivateTag.ScaleIntercept:
            return "ScaleIntercept";
        case PrivateTag.ScaleSlope:
            return "ScaleSlope";
        case PrivateTag.WindowCenter:
            return "WindowCenter";
        case PrivateTag.WindowWidth:
            return "WindowWidth";
        case PrivateTag.ImageType:
            return "ImageType";
        case PrivateTag.CardiacGating:
            return "CardiacGating";
        case PrivateTag.DevelopmentMode:
            return "DevelopmentMode";
        case PrivateTag.Diffusion:
            return "Diffusion";
        case PrivateTag.FatSaturation:
            return "FatSaturation";
        case PrivateTag.FlowCompensation:
            return "FlowCompensation";
        case PrivateTag.FourierInterpolation:
            return "FourierInterpolation";
        case PrivateTag.HardcopyProtocol:
            return "HardcopyProtocol";
        case PrivateTag.InverseReconstructed:
            return "InverseReconstructed";
        case PrivateTag.LabelSyntax:
            return "LabelSyntax";
        case PrivateTag.MagnetizationPrepared:
            return "MagnetizationPrepared";
        case PrivateTag.MagnetizationTransferContrast:
            return "MagnetizationTransferContrast";
        case PrivateTag.MeasurementScanResolution:
            return "MeasurementScanResolution";
        case PrivateTag.MIPProtocol:
            return "MIPProtocol";
        case PrivateTag.MPRProtocol:
            return "MPRProtocol";
        case PrivateTag.NumberOfChemicalShift:
            return "NumberOfChemicalShift";
        case PrivateTag.NumberOfMixes:
            return "NumberOfMixes";
        case PrivateTag.NumberOfReferences:
            return "NumberOfReferences";
        case PrivateTag.NumberOfSlabs:
            return "NumberOfSlabs";
        case PrivateTag.NumberOfVolumes:
            return "NumberOfVolumes";
        case PrivateTag.OverSamplingPhase:
            return "OverSamplingPhase";
        case PrivateTag.PackageMode:
            return "PackageMode";
        case PrivateTag.PartialFourierFrequency:
            return "PartialFourierFrequency";
        case PrivateTag.PartialFourierPhase:
            return "PartialFourierPhase";
        case PrivateTag.PatientReferenceID:
            return "PatientReferenceID";
        case PrivateTag.PercentScanComplete:
            return "PercentScanComplete";
        case PrivateTag.PhaseEncodeReordering:
            return "PhaseEncodeReordering";
        case PrivateTag.PlanScanSurveyNumberOfImages:
            return "PlanScanSurveyNumberOfImages";
        case PrivateTag.PPGPPUGating:
            return "PPGPPUGating";
        case PrivateTag.SpatialPresaturation:
            return "SpatialPresaturation";
        case PrivateTag.RepetitionTime:
            return "RepetitionTime";
        case PrivateTag.RespiratoryGating:
            return "RespiratoryGating";
        case PrivateTag.SampleRepresentation:
            return "SampleRepresentation";
        case PrivateTag.AcquisitionDuration:
            return "AcquisitionDuration";
        case PrivateTag.SegmentedKSpace:
            return "SegmentedKSpace";
        case PrivateTag.DataType:
            return "DataType";
        case PrivateTag.IsCardiac:
            return "IsCardiac";
        case PrivateTag.IsSpectro:
            return "IsSpectro";
        case PrivateTag.Spoiled:
            return "Spoiled";
        case PrivateTag.SteadyState:
            return "SteadyState";
        case PrivateTag.SubAnatomy:
            return "SubAnatomy";
        case PrivateTag.TimeReversedSteadyState:
            return "TimeReversedSteadyState";
        case PrivateTag.TiltOptimizedNonsaturatedExcitation:
            return "TiltOptimizedNonsaturatedExcitation";
        case PrivateTag.NumberOfRRIntervalRanges:
            return "NumberOfRRIntervalRanges";
        case PrivateTag.RRIntervalsDistribution:
            return "RRIntervalsDistribution";
        case PrivateTag.PlanScanAcquisitionNumber:
            return "PlanScanAcquisitionNumber";
        case PrivateTag.ReferencedAcquisitionNumber:
            return "ReferencedAcquisitionNumber";
        case PrivateTag.ReferencedChemicalShiftNumber:
            return "ReferencedChemicalShiftNumber";
        case PrivateTag.ReferenceDynamicScanNumber:
            return "ReferenceDynamicScanNumber";
        case PrivateTag.ReferencedEchoNumber:
            return "ReferencedEchoNumber";
        case PrivateTag.ReferencedEntity:
            return "ReferencedEntity";
        case PrivateTag.ReferencedImageType:
            return "ReferencedImageType";
        case PrivateTag.SlabFOVRL:
            return "SlabFOVRL";
        case PrivateTag.SlabOffcentreAP:
            return "SlabOffcentreAP";
        case PrivateTag.SlabOffcentreFH:
            return "SlabOffcentreFH";
        case PrivateTag.SlabOffcentreRL:
            return "SlabOffcentreRL";
        case PrivateTag.SlabType:
            return "SlabType";
        case PrivateTag.SlabViewAxis:
            return "SlabViewAxis";
        case PrivateTag.VolumeAngulationAP:
            return "VolumeAngulationAP";
        case PrivateTag.VolumeAngulationFH:
            return "VolumeAngulationFH";
        case PrivateTag.VolumeAngulationRL:
            return "VolumeAngulationRL";
        case PrivateTag.VolumeFOVAP:
            return "VolumeFOVAP";
        case PrivateTag.VolumeFOVFH:
            return "VolumeFOVFH";
        case PrivateTag.VolumeFOVRL:
            return "VolumeFOVRL";
        case PrivateTag.VolumeOffcentreAP:
            return "VolumeOffcentreAP";
        case PrivateTag.VolumeOffcentreFH:
            return "VolumeOffcentreFH";
        case PrivateTag.VolumeOffcentreRL:
            return "VolumeOffcentreRL";
        case PrivateTag.VolumeType:
            return "VolumeType";
        case PrivateTag.VolumeViewAxis:
            return "VolumeViewAxis";
        case PrivateTag.StudyOrigin:
            return "StudyOrigin";
        case PrivateTag.StudySequenceNumber:
            return "StudySequenceNumber";
        case PrivateTag.PrepulseType:
            return "PrepulseType";
        case PrivateTag.fMRIStatusIndication:
            return "fMRIStatusIndication";
        case PrivateTag.ReferencePhaseNumber:
            return "ReferencePhaseNumber";
        case PrivateTag.ReferenceReconstructionNumber:
            return "ReferenceReconstructionNumber";
        case PrivateTag.ReferenceScanningSequence:
            return "ReferenceScanningSequence";
        case PrivateTag.ReferenceSliceNumber:
            return "ReferenceSliceNumber";
        case PrivateTag.ReferenceType:
            return "ReferenceType";
        case PrivateTag.SlabAngulationAP:
            return "SlabAngulationAP";
        case PrivateTag.SlabAngulationFH:
            return "SlabAngulationFH";
        case PrivateTag.SlabAngulationRL:
            return "SlabAngulationRL";
        case PrivateTag.SlabFOVAP:
            return "SlabFOVAP";
        case PrivateTag.SlabFOVFH:
            return "SlabFOVFH";
        case PrivateTag.ScanningSequence:
            return "ScanningSequence";
        case PrivateTag.AcquisitionType:
            return "AcquisitionType";
        case PrivateTag.HardcopyProtocolEV:
            return "HardcopyProtocolEV";
        case PrivateTag.StackAngulationAP:
            return "StackAngulationAP";
        case PrivateTag.StackAngulationFH:
            return "StackAngulationFH";
        case PrivateTag.StackAngulationRL:
            return "StackAngulationRL";
        case PrivateTag.StackFOVAP:
            return "StackFOVAP";
        case PrivateTag.StackFOVFH:
            return "StackFOVFH";
        case PrivateTag.StackFOVRL:
            return "StackFOVRL";
        case PrivateTag.StackOffcentreAP:
            return "StackOffcentreAP";
        case PrivateTag.StackOffcentreFH:
            return "StackOffcentreFH";
        case PrivateTag.StackOffcentreRL:
            return "StackOffcentreRL";
        case PrivateTag.StackPreparationDirection:
            return "StackPreparationDirection";
        case PrivateTag.StackSliceDistance:
            return "StackSliceDistance";
        case PrivateTag.SeriesPlanScan:
            return "SeriesPlanScan";
        case PrivateTag.StackViewAxis:
            return "StackViewAxis";
        case PrivateTag.SeriesSlab:
            return "SeriesSlab";
        case PrivateTag.SeriesReference:
            return "SeriesReference";
        case PrivateTag.SeriesVolume:
            return "SeriesVolume";
        case PrivateTag.NumberOfGeometry:
            return "NumberOfGeometry";
        case PrivateTag.SeriesGeometry:
            return "SeriesGeometry";
        case PrivateTag.SpectralSelectiveExcitationPulse:
            return "SpectralSelectiveExcitationPulse";
        case PrivateTag.DynamicScanBeginTime:
            return "DynamicScanBeginTime";
        case PrivateTag.SyncraScanType:
            return "SyncraScanType";
        case PrivateTag.IsCOCA:
            return "IsCOCA";
        case PrivateTag.StackCoilID:
            return "StackCoilID";
        case PrivateTag.StackCBBCoil1:
            return "StackCBBCoil1";
        case PrivateTag.StackCBBCoil2:
            return "StackCBBCoil2";
        case PrivateTag.StackChannelCombi:
            return "StackChannelCombi";
        case PrivateTag.StackCoilConnection:
            return "StackCoilConnection";
        case PrivateTag.InversionTime:
            return "InversionTime";
        case PrivateTag.GeometryCorrection:
            return "GeometryCorrection";
        case PrivateTag.DiffusionDirectionRL:
            return "DiffusionDirectionRL";
        case PrivateTag.DiffusionDirectionAP:
            return "DiffusionDirectionAP";
        case PrivateTag.DiffusionDirectionFH:
            return "DiffusionDirectionFH";
        case PrivateTag.ScanSequence:
            return "ScanSequence";
        }
        return "";
    }

}
