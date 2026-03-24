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
package org.miaixz.bus.image.galaxy.dict.PHILIPS_IMAGING_DD_001_1;

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

            case PrivateTag.DiffusionDirection:
            case PrivateTag.ImageEnhanced:
            case PrivateTag.ImageTypeEDES:
            case PrivateTag.ImageOrientation:
            case PrivateTag.ArrhythmiaRejection:
            case PrivateTag.CardiacCycled:
            case PrivateTag.CardiacSync:
            case PrivateTag.DynamicSeries:
            case PrivateTag.PartialMatrixScanned:
            case PrivateTag.PrepulseType:
            case PrivateTag.ReformatAccuracy:
            case PrivateTag.RespirationSync:
            case PrivateTag.SPIR:
            case PrivateTag.SeriesIsInteractive:
            case PrivateTag.PresentationStateSubtractionActive:
            case PrivateTag._2001_xx2B_:
            case PrivateTag.StackRadialAxis:
            case PrivateTag.StackType:
            case PrivateTag.DisplayedAreaZoomInterpolationMeth:
            case PrivateTag.GraphicLineStyle:
            case PrivateTag.InterpolationMethod:
            case PrivateTag.PolyLineBeginPointStyle:
            case PrivateTag.PolyLineEndPointStyle:
            case PrivateTag.WindowSmoothingTaste:
            case PrivateTag.PresentationGLTrafoInvert:
            case PrivateTag.GraphicType:
            case PrivateTag.SeriesTransmitted:
            case PrivateTag.SeriesCommitted:
            case PrivateTag.ExaminationSource:
            case PrivateTag.LinearPresentationGLTrafoShapeSub:
            case PrivateTag.GraphicConstraint:
            case PrivateTag.GLTrafoType:
            case PrivateTag.IsRawImage:
            case PrivateTag._2001_xxDA_:
                return VR.CS;

            case PrivateTag.FlipAngle:
            case PrivateTag._2001_xx74_:
            case PrivateTag._2001_xx75_:
            case PrivateTag.ImagingFrequency:
            case PrivateTag.InversionTime:
            case PrivateTag.MagneticFieldStrength:
            case PrivateTag.NumberOfAverages:
            case PrivateTag.PhaseFOVPercent:
            case PrivateTag.SamplingPercent:
                return VR.DS;

            case PrivateTag.ChemicalShift:
            case PrivateTag.DiffusionBFactor:
            case PrivateTag.ImagePrepulseDelay:
            case PrivateTag.DiffusionEchoTime:
            case PrivateTag.PCVelocity:
            case PrivateTag.PrepulseDelay:
            case PrivateTag.WaterFatShift:
            case PrivateTag._2001_xx29_:
            case PrivateTag.StackRadialAngle:
            case PrivateTag._2001_xx39_:
            case PrivateTag.GraphicLineWidth:
            case PrivateTag.ContourFillTransparency:
            case PrivateTag.WindowRoundingFactor:
            case PrivateTag.ProspectiveMotionCorrection:
            case PrivateTag.RetrospectiveMotionCorrection:
                return VR.FL;

            case PrivateTag.ChemicalShiftNumberMR:
            case PrivateTag.PhaseNumber:
            case PrivateTag.ImagePlaneNumber:
            case PrivateTag.ReconstructionNumber:
            case PrivateTag.EllipsDisplShutMajorAxFrstEndPnt:
            case PrivateTag.EllipsDisplShutMajorAxScndEndPnt:
            case PrivateTag.EllipsDisplShutOtherAxFrstEndPnt:
            case PrivateTag.OverlayPlaneID:
            case PrivateTag.EllipsDisplShutOtherAxScndEndPnt:
            case PrivateTag.AcquisitionNumber:
            case PrivateTag.NumberOfDynamicScans:
            case PrivateTag.EchoTrainLength:
            case PrivateTag.NrOfPhaseEncodingSteps:
                return VR.IS;

            case PrivateTag.ScanningTechnique:
            case PrivateTag.GraphicMarkerType:
            case PrivateTag.TextFont:
            case PrivateTag._2001_xx80_:
            case PrivateTag.TextForegroundColor:
            case PrivateTag.TextBackgroundColor:
            case PrivateTag.TextShadowColor:
            case PrivateTag.TextStyle:
            case PrivateTag.GraphicAnnotationLabel:
            case PrivateTag.ExamCardName:
                return VR.LO;

            case PrivateTag.EchoTimeDisplay:
            case PrivateTag.TextType:
            case PrivateTag.SeriesType:
            case PrivateTag.ImagedNucleus:
            case PrivateTag.TransmittingCoil:
                return VR.SH;

            case PrivateTag.EPIFactor:
            case PrivateTag.NumberOfEchoes:
            case PrivateTag.NumberOfPhases:
            case PrivateTag.NumberOfSlices:
            case PrivateTag.NumberOfStacks:
                return VR.SL;

            case PrivateTag.StackSequence:
            case PrivateTag.GraphicOverlayPlane:
            case PrivateTag.LinearModalityGLTrafo:
            case PrivateTag.DisplayShutter:
            case PrivateTag.SpatialTransformation:
            case PrivateTag._2001_xx6B_:
            case PrivateTag._2001_xx9A_:
                return VR.SQ;

            case PrivateTag.GraphicAnnotationParentID:
            case PrivateTag.CardiacGateWidth:
            case PrivateTag.NumberOfLocations:
            case PrivateTag.NumberOfPCDirections:
            case PrivateTag.NumberOfSlicesInStack:
            case PrivateTag.StackSliceNumber:
            case PrivateTag.GraphicAnnotationID:
                return VR.SS;

            case PrivateTag.GraphicAnnotationModel:
            case PrivateTag.MeasurementTextUnits:
            case PrivateTag.MeasurementTextType:
            case PrivateTag.DerivationDescription:
                return VR.ST;

            case PrivateTag.ImagePresentationStateUID:
                return VR.UI;

            case PrivateTag.ContourFillColor:
            case PrivateTag.GraphicLineColor:
            case PrivateTag.ContrastTransferTaste:
            case PrivateTag.NumberOfFrames:
            case PrivateTag.GraphicNumber:
            case PrivateTag.TextColorForeground:
            case PrivateTag.TextColorBackground:
            case PrivateTag.TextColorShadow:
                return VR.UL;

            case PrivateTag.FrameNumber:
            case PrivateTag.PixelProcessingKernelSize:
                return VR.US;
        }
        return VR.UN;
    }

}
