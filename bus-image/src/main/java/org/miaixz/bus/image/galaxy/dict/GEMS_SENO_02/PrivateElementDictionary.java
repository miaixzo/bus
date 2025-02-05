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
