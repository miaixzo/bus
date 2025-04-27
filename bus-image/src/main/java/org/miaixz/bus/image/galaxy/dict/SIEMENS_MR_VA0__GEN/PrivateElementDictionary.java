/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_VA0__GEN;

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

        case PrivateTag.FilterTypeForRawData:
        case PrivateTag.FilterTypeForImageData:
        case PrivateTag.FilterTypeForPhaseCorrection:
        case PrivateTag.NormalizationFilterTypeForImageData:
        case PrivateTag.OrderofSlices:
            return VR.CS;
        case PrivateTag.TotalMeasurementTimeNominal:
        case PrivateTag.TotalMeasurementTimeCurrent:
        case PrivateTag.StartDelayTime:
        case PrivateTag.DwellTime:
        case PrivateTag.ArrayCoilElementNoiseLevel:
        case PrivateTag.FlipAngle:
        case PrivateTag.FilterParameterForRawData:
        case PrivateTag.FilterParameterForImageData:
        case PrivateTag.FilterParameterForPhaseCorrection:
        case PrivateTag.NormalizationFilterParameterForImageData:
        case PrivateTag.SaturationPhaseEncodingVectorSagittalComponent:
        case PrivateTag.SaturationReadoutVectorSagittalComponent:
        case PrivateTag.ImageRotationAngle:
        case PrivateTag.CoilPosition:
        case PrivateTag.EPIReconstructionPhase:
        case PrivateTag.EPIReconstructionSlope:
        case PrivateTag.SlabThickness:
        case PrivateTag.CurrentSliceDistanceFactor:
        case PrivateTag.NumberOfTriggerPulses:
        case PrivateTag.RepetitionTimeEffective:
        case PrivateTag.GateThreshold:
        case PrivateTag.GatedRatio:
        case PrivateTag.SecondEchoTime:
        case PrivateTag.SecondRepetitionTime:
        case PrivateTag.SaturationPhaseEncodingVectorTransverseComponent:
        case PrivateTag.SaturationReadoutVectorTransverseComponent:
        case PrivateTag.EPIChangeValueOfMagnitude:
        case PrivateTag.EPIChangeValueOfXComponent:
        case PrivateTag.EPIChangeValueOfYComponent:
        case PrivateTag.EPIChangeValueOfZComponent:
            return VR.DS;
        case PrivateTag.NumberOfPhases:
        case PrivateTag.NumberOfFourierLinesNominal:
        case PrivateTag.NumberOfFourierLinesCurrent:
        case PrivateTag.NumberOfFourierLinesAfterZero:
        case PrivateTag.FirstMeasuredFourierLine:
        case PrivateTag.AcquisitionColumns:
        case PrivateTag.ReconstructionColumns:
        case PrivateTag.ArrayCoilElementNumber:
        case PrivateTag.ArrayCoilElementToADCConnect:
        case PrivateTag.ArrayCoilADCPairNumber:
        case PrivateTag.NumberOfAveragesCurrent:
        case PrivateTag.NumberOfPrescans:
        case PrivateTag.NumberOfSaturationRegions:
        case PrivateTag.EPIStimulationMonitorMode:
        case PrivateTag.PhaseCorrectionRowsSequence:
        case PrivateTag.PhaseCorrectionColumnsSequence:
        case PrivateTag.PhaseCorrectionRowsReconstruction:
        case PrivateTag.PhaseCorrectionColumnsReconstruction:
        case PrivateTag.NumberOf3DRawPartitionsNominal:
        case PrivateTag.NumberOf3DRawPartitionsCurrent:
        case PrivateTag.NumberOf3DImagePartitions:
        case PrivateTag.Actual3DImagePartitionNumber:
        case PrivateTag.NumberOfSlicesNominal:
        case PrivateTag.NumberOfSlicesCurrent:
        case PrivateTag.CurrentSliceNumber:
        case PrivateTag.CurrentGroupNumber:
        case PrivateTag.MIPStartRow:
        case PrivateTag.MIPStopRow:
        case PrivateTag.MIPStartColumn:
        case PrivateTag.MIPStopColumn:
        case PrivateTag.MIPStartSlice:
        case PrivateTag.MIPStopSlice:
        case PrivateTag.DelayAfterTrigger:
        case PrivateTag.RRInterval:
        case PrivateTag.NumberOfInterpolatedImages:
        case PrivateTag.NumberOfEchoes:
        case PrivateTag.CardiacCode:
            return VR.IS;
        case PrivateTag.GatePhase:
            return VR.LO;
        case PrivateTag.SequenceControlMask:
        case PrivateTag.MeasurementStatusMask:
        case PrivateTag.ArrayCoilElementSelectMask:
        case PrivateTag.ArrayCoilElementDataMask:
        case PrivateTag.ArrayCoilCombinationMask:
        case PrivateTag.CoilIDMask:
        case PrivateTag.CoilClassMask:
        case PrivateTag.SignalMask:
            return VR.UL;
        }
        return VR.UN;
    }
}
