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
package org.miaixz.bus.image.galaxy.dict.Philips_Imaging_DD_001;

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

        case PrivateTag.DiffusionDirection:
        case PrivateTag.ImageEnhanced:
        case PrivateTag.ImageTypeEDES:
        case PrivateTag.SliceOrientation:
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
        case PrivateTag.SliceNumber:
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
