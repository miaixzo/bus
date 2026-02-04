/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.GEMS_SENO_02;

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

            case PrivateTag.Track:
            case PrivateTag.AES:
            case PrivateTag.SenographType:
            case PrivateTag.CorrectionType:
            case PrivateTag.AcquisitionType:
            case PrivateTag.BreastLaterality:
            case PrivateTag.WindowingType:
                return VR.CS;

            case PrivateTag.Angulation:
            case PrivateTag.CompressionThickness:
            case PrivateTag.CompressionForce:
            case PrivateTag.RealMagnificationFactor:
            case PrivateTag.DisplayedMagnificationFactor:
            case PrivateTag.IntegrationTime:
            case PrivateTag.ROIOriginXY:
            case PrivateTag.CCDTemperature:
            case PrivateTag.ReceptorSizeCmXY:
            case PrivateTag.PixelPitchMicrons:
            case PrivateTag.QuantumGain:
            case PrivateTag.ElectronEDURatio:
            case PrivateTag.ElectronicGain:
            case PrivateTag.MeanOfRawGrayLevels:
            case PrivateTag.MeanOfOffsetGrayLevels:
            case PrivateTag.MeanOfCorrectedGrayLevels:
            case PrivateTag.MeanOfRegionGrayLevels:
            case PrivateTag.MeanOfLogRegionGrayLevels:
            case PrivateTag.StandardDeviationOfRawGrayLevels:
            case PrivateTag.StandardDeviationOfCorrectedGrayLevels:
            case PrivateTag.StandardDeviationOfRegionGrayLevels:
            case PrivateTag.StandardDeviationOfLogRegionGrayLevels:
            case PrivateTag.WindowingParameters:
            case PrivateTag.ReferenceLandmarkAX3DCoordinates:
            case PrivateTag.ReferenceLandmarkAY3DCoordinates:
            case PrivateTag.ReferenceLandmarkAZ3DCoordinates:
            case PrivateTag.ReferenceLandmarkBX3DCoordinates:
            case PrivateTag.ReferenceLandmarkBY3DCoordinates:
            case PrivateTag.ReferenceLandmarkBZ3DCoordinates:
            case PrivateTag.XRaySourceXLocation:
            case PrivateTag.XRaySourceYLocation:
            case PrivateTag.XRaySourceZLocation:
            case PrivateTag.RadiologicalThickness:
            case PrivateTag.Exponent:
            case PrivateTag.NoiseReductionSensitivity:
            case PrivateTag.NoiseReductionThreshold:
            case PrivateTag.Mu:
            case PrivateTag.ImageCropPoint:
            case PrivateTag.SignalAverageFactor:
            case PrivateTag.OrganDoseForSourceImages:
            case PrivateTag.EntranceDoseInmGyForSourceImages:
            case PrivateTag.OrganDoseIndGyForCompleteDBTSequence:
            case PrivateTag.EntranceDoseIndGyForCompleteDBTSequence:
            case PrivateTag.ReplacementImage:
            case PrivateTag.CumulativeOrganDoseIndGy:
            case PrivateTag.CumulativeEntranceDoseInmGy:
                return VR.DS;

            case PrivateTag.ReceptorSizePixelsXY:
            case PrivateTag.PixelDepthBits:
            case PrivateTag.BinningFactorXY:
            case PrivateTag.SetNumber:
            case PrivateTag.CrosshairCursorXCoordinates:
            case PrivateTag.CrosshairCursorYCoordinates:
            case PrivateTag.ReferenceLandmarkAXImageCoordinates:
            case PrivateTag.ReferenceLandmarkAYImageCoordinates:
            case PrivateTag.ReferenceLandmarkBXImageCoordinates:
            case PrivateTag.ReferenceLandmarkBYImageCoordinates:
            case PrivateTag.RawDiagnosticLow:
            case PrivateTag.RawDiagnosticHigh:
            case PrivateTag.ACoefficients:
            case PrivateTag.Threshold:
            case PrivateTag.BreastROIX:
            case PrivateTag.BreastROIY:
            case PrivateTag.UserWindowCenter:
            case PrivateTag.UserWindowWidth:
            case PrivateTag.SegmentationThreshold:
            case PrivateTag.DetectorEntranceDose:
            case PrivateTag.AsymmetricalCollimationInformation:
                return VR.IS;

            case PrivateTag.DigitalSenographConfiguration:
            case PrivateTag.ClinicalView:
            case PrivateTag.PaddleProperties:
                return VR.LO;

            case PrivateTag.SystemSeriesDescription:
            case PrivateTag.ReconstructionParameters:
                return VR.LT;

            case PrivateTag.IDSDataBuffer:
            case PrivateTag.MAOBuffer:
            case PrivateTag.VignettePixelData:
            case PrivateTag.STXBuffer:
                return VR.OB;

            case PrivateTag.PremiumViewBeta:
                return VR.SH;

            case PrivateTag.ReplacemeImageSequence:
                return VR.SQ;

            case PrivateTag.Screen:
                return VR.ST;

            case PrivateTag.FallbackInstanceUID:
            case PrivateTag.FallbackSeriesUID:
            case PrivateTag.SOPInstanceUIDForLossyCompression:
                return VR.UI;

            case PrivateTag.VignetteRows:
            case PrivateTag.VignetteColumns:
            case PrivateTag.VignetteBitsAllocated:
            case PrivateTag.VignetteBitsStored:
            case PrivateTag.VignetteHighBit:
            case PrivateTag.VignettePixelRepresentation:
                return VR.US;
        }
        return VR.UN;
    }

}
