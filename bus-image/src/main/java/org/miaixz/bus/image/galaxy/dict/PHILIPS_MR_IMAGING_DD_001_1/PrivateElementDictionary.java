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

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
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

        case PrivateTag.ImageDisplayOrientation:
        case PrivateTag.SynergyReconstructionType:
        case PrivateTag.ImageType:
        case PrivateTag.CardiacGating:
        case PrivateTag.DevelopmentMode:
        case PrivateTag.Diffusion:
        case PrivateTag.FatSaturation:
        case PrivateTag.FlowCompensation:
        case PrivateTag.FourierInterpolation:
        case PrivateTag.InverseReconstructed:
        case PrivateTag.MagnetizationPrepared:
        case PrivateTag.MagnetizationTransferContrast:
        case PrivateTag.OverSamplingPhase:
        case PrivateTag.PackageMode:
        case PrivateTag.PartialFourierFrequency:
        case PrivateTag.PartialFourierPhase:
        case PrivateTag.PhaseEncodeReordering:
        case PrivateTag.PPGPPUGating:
        case PrivateTag.SpatialPresaturation:
        case PrivateTag.RespiratoryGating:
        case PrivateTag.SampleRepresentation:
        case PrivateTag.SegmentedKSpace:
        case PrivateTag.DataType:
        case PrivateTag.IsCardiac:
        case PrivateTag.IsSpectro:
        case PrivateTag.Spoiled:
        case PrivateTag.SteadyState:
        case PrivateTag.TimeReversedSteadyState:
        case PrivateTag.TiltOptimizedNonsaturatedExcitation:
        case PrivateTag.ReferencedEntity:
        case PrivateTag.ReferencedImageType:
        case PrivateTag.SlabType:
        case PrivateTag.SlabViewAxis:
        case PrivateTag.VolumeType:
        case PrivateTag.VolumeViewAxis:
        case PrivateTag.StudyOrigin:
        case PrivateTag.PrepulseType:
        case PrivateTag.ReferenceScanningSequence:
        case PrivateTag.ReferenceType:
        case PrivateTag.ScanningSequence:
        case PrivateTag.AcquisitionType:
        case PrivateTag.StackPreparationDirection:
        case PrivateTag.StackViewAxis:
        case PrivateTag.SpectralSelectiveExcitationPulse:
        case PrivateTag.SyncraScanType:
        case PrivateTag.IsCOCA:
        case PrivateTag.StackCoilConnection:
        case PrivateTag.GeometryCorrection:
        case PrivateTag.ScanSequence:
            return VR.CS;
        case PrivateTag.WindowCenter:
        case PrivateTag.WindowWidth:
        case PrivateTag.InversionTime:
            return VR.DS;
        case PrivateTag.ImageAngulationAP:
        case PrivateTag.ImageAngulationFH:
        case PrivateTag.ImageAngulationRL:
        case PrivateTag.ImageOffcenterAP:
        case PrivateTag.ImageOffcenterFH:
        case PrivateTag.ImageOffCentreRL:
        case PrivateTag.MaxFP:
        case PrivateTag.MinFP:
        case PrivateTag.ScaleIntercept:
        case PrivateTag.ScaleSlope:
        case PrivateTag.RepetitionTime:
        case PrivateTag.AcquisitionDuration:
        case PrivateTag.SlabFOVRL:
        case PrivateTag.SlabOffcentreAP:
        case PrivateTag.SlabOffcentreFH:
        case PrivateTag.SlabOffcentreRL:
        case PrivateTag.VolumeAngulationAP:
        case PrivateTag.VolumeAngulationFH:
        case PrivateTag.VolumeAngulationRL:
        case PrivateTag.VolumeFOVAP:
        case PrivateTag.VolumeFOVFH:
        case PrivateTag.VolumeFOVRL:
        case PrivateTag.VolumeOffcentreAP:
        case PrivateTag.VolumeOffcentreFH:
        case PrivateTag.VolumeOffcentreRL:
        case PrivateTag.SlabAngulationAP:
        case PrivateTag.SlabAngulationFH:
        case PrivateTag.SlabAngulationRL:
        case PrivateTag.SlabFOVAP:
        case PrivateTag.SlabFOVFH:
        case PrivateTag.StackAngulationAP:
        case PrivateTag.StackAngulationFH:
        case PrivateTag.StackAngulationRL:
        case PrivateTag.StackFOVAP:
        case PrivateTag.StackFOVFH:
        case PrivateTag.StackFOVRL:
        case PrivateTag.StackOffcentreAP:
        case PrivateTag.StackOffcentreFH:
        case PrivateTag.StackOffcentreRL:
        case PrivateTag.StackSliceDistance:
        case PrivateTag.DynamicScanBeginTime:
        case PrivateTag.DiffusionDirectionRL:
        case PrivateTag.DiffusionDirectionAP:
        case PrivateTag.DiffusionDirectionFH:
            return VR.FL;
        case PrivateTag.NumberOfReferences:
        case PrivateTag.PatientReferenceID:
        case PrivateTag.PlanScanSurveyNumberOfImages:
        case PrivateTag.ReferencedAcquisitionNumber:
        case PrivateTag.ReferencedChemicalShiftNumber:
        case PrivateTag.ReferenceDynamicScanNumber:
        case PrivateTag.ReferencedEchoNumber:
        case PrivateTag.StudySequenceNumber:
        case PrivateTag.ReferencePhaseNumber:
        case PrivateTag.ReferenceReconstructionNumber:
        case PrivateTag.ReferenceSliceNumber:
        case PrivateTag.StackCoilID:
        case PrivateTag.StackCBBCoil1:
        case PrivateTag.StackCBBCoil2:
        case PrivateTag.StackChannelCombi:
            return VR.IS;
        case PrivateTag.HardcopyProtocol:
        case PrivateTag.HardcopyProtocolEV:
            return VR.LO;
        case PrivateTag.MIPProtocol:
        case PrivateTag.MPRProtocol:
        case PrivateTag.SubAnatomy:
            return VR.SH;
        case PrivateTag.NumberOfChemicalShift:
        case PrivateTag.RRIntervalsDistribution:
        case PrivateTag.PlanScanAcquisitionNumber:
            return VR.SL;
        case PrivateTag.SeriesPlanScan:
        case PrivateTag.SeriesSlab:
        case PrivateTag.SeriesReference:
        case PrivateTag.SeriesVolume:
        case PrivateTag.SeriesGeometry:
            return VR.SQ;
        case PrivateTag.LabelSyntax:
        case PrivateTag.MeasurementScanResolution:
        case PrivateTag.NumberOfMixes:
        case PrivateTag.NumberOfSlabs:
        case PrivateTag.NumberOfVolumes:
        case PrivateTag.PercentScanComplete:
        case PrivateTag.NumberOfRRIntervalRanges:
        case PrivateTag.fMRIStatusIndication:
        case PrivateTag.NumberOfGeometry:
            return VR.SS;
        }
        return VR.UN;
    }
}
