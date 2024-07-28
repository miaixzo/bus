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
package org.miaixz.bus.image.galaxy.dict.Philips_Imaging_DD_001;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.ChemicalShift:
            return "ChemicalShift";
        case PrivateTag.ChemicalShiftNumberMR:
            return "ChemicalShiftNumberMR";
        case PrivateTag.DiffusionBFactor:
            return "DiffusionBFactor";
        case PrivateTag.DiffusionDirection:
            return "DiffusionDirection";
        case PrivateTag.GraphicAnnotationParentID:
            return "GraphicAnnotationParentID";
        case PrivateTag.ImageEnhanced:
            return "ImageEnhanced";
        case PrivateTag.ImageTypeEDES:
            return "ImageTypeEDES";
        case PrivateTag.PhaseNumber:
            return "PhaseNumber";
        case PrivateTag.ImagePrepulseDelay:
            return "ImagePrepulseDelay";
        case PrivateTag.SliceNumber:
            return "SliceNumber";
        case PrivateTag.SliceOrientation:
            return "SliceOrientation";
        case PrivateTag.ArrhythmiaRejection:
            return "ArrhythmiaRejection";
        case PrivateTag.CardiacCycled:
            return "CardiacCycled";
        case PrivateTag.CardiacGateWidth:
            return "CardiacGateWidth";
        case PrivateTag.CardiacSync:
            return "CardiacSync";
        case PrivateTag.DiffusionEchoTime:
            return "DiffusionEchoTime";
        case PrivateTag.DynamicSeries:
            return "DynamicSeries";
        case PrivateTag.EPIFactor:
            return "EPIFactor";
        case PrivateTag.NumberOfEchoes:
            return "NumberOfEchoes";
        case PrivateTag.NumberOfLocations:
            return "NumberOfLocations";
        case PrivateTag.NumberOfPCDirections:
            return "NumberOfPCDirections";
        case PrivateTag.NumberOfPhases:
            return "NumberOfPhases";
        case PrivateTag.NumberOfSlices:
            return "NumberOfSlices";
        case PrivateTag.PartialMatrixScanned:
            return "PartialMatrixScanned";
        case PrivateTag.PCVelocity:
            return "PCVelocity";
        case PrivateTag.PrepulseDelay:
            return "PrepulseDelay";
        case PrivateTag.PrepulseType:
            return "PrepulseType";
        case PrivateTag.ReconstructionNumber:
            return "ReconstructionNumber";
        case PrivateTag.ReformatAccuracy:
            return "ReformatAccuracy";
        case PrivateTag.RespirationSync:
            return "RespirationSync";
        case PrivateTag.ScanningTechnique:
            return "ScanningTechnique";
        case PrivateTag.SPIR:
            return "SPIR";
        case PrivateTag.WaterFatShift:
            return "WaterFatShift";
        case PrivateTag.FlipAngle:
            return "FlipAngle";
        case PrivateTag.SeriesIsInteractive:
            return "SeriesIsInteractive";
        case PrivateTag.EchoTimeDisplay:
            return "EchoTimeDisplay";
        case PrivateTag.PresentationStateSubtractionActive:
            return "PresentationStateSubtractionActive";
        case PrivateTag._2001_xx29_:
            return "_2001_xx29_";
        case PrivateTag._2001_xx2B_:
            return "_2001_xx2B_";
        case PrivateTag.NumberOfSlicesInStack:
            return "NumberOfSlicesInStack";
        case PrivateTag.StackRadialAngle:
            return "StackRadialAngle";
        case PrivateTag.StackRadialAxis:
            return "StackRadialAxis";
        case PrivateTag.StackSliceNumber:
            return "StackSliceNumber";
        case PrivateTag.StackType:
            return "StackType";
        case PrivateTag._2001_xx39_:
            return "_2001_xx39_";
        case PrivateTag.ContourFillColor:
            return "ContourFillColor";
        case PrivateTag.DisplayedAreaZoomInterpolationMeth:
            return "DisplayedAreaZoomInterpolationMeth";
        case PrivateTag.EllipsDisplShutMajorAxFrstEndPnt:
            return "EllipsDisplShutMajorAxFrstEndPnt";
        case PrivateTag.EllipsDisplShutMajorAxScndEndPnt:
            return "EllipsDisplShutMajorAxScndEndPnt";
        case PrivateTag.EllipsDisplShutOtherAxFrstEndPnt:
            return "EllipsDisplShutOtherAxFrstEndPnt";
        case PrivateTag.GraphicLineStyle:
            return "GraphicLineStyle";
        case PrivateTag.GraphicLineWidth:
            return "GraphicLineWidth";
        case PrivateTag.GraphicAnnotationID:
            return "GraphicAnnotationID";
        case PrivateTag.InterpolationMethod:
            return "InterpolationMethod";
        case PrivateTag.PolyLineBeginPointStyle:
            return "PolyLineBeginPointStyle";
        case PrivateTag.PolyLineEndPointStyle:
            return "PolyLineEndPointStyle";
        case PrivateTag.WindowSmoothingTaste:
            return "WindowSmoothingTaste";
        case PrivateTag.GraphicMarkerType:
            return "GraphicMarkerType";
        case PrivateTag.OverlayPlaneID:
            return "OverlayPlaneID";
        case PrivateTag.ImagePresentationStateUID:
            return "ImagePresentationStateUID";
        case PrivateTag.PresentationGLTrafoInvert:
            return "PresentationGLTrafoInvert";
        case PrivateTag.ContourFillTransparency:
            return "ContourFillTransparency";
        case PrivateTag.GraphicLineColor:
            return "GraphicLineColor";
        case PrivateTag.GraphicType:
            return "GraphicType";
        case PrivateTag.ContrastTransferTaste:
            return "ContrastTransferTaste";
        case PrivateTag.GraphicAnnotationModel:
            return "GraphicAnnotationModel";
        case PrivateTag.MeasurementTextUnits:
            return "MeasurementTextUnits";
        case PrivateTag.MeasurementTextType:
            return "MeasurementTextType";
        case PrivateTag.StackSequence:
            return "StackSequence";
        case PrivateTag.NumberOfStacks:
            return "NumberOfStacks";
        case PrivateTag.SeriesTransmitted:
            return "SeriesTransmitted";
        case PrivateTag.SeriesCommitted:
            return "SeriesCommitted";
        case PrivateTag.ExaminationSource:
            return "ExaminationSource";
        case PrivateTag.TextType:
            return "TextType";
        case PrivateTag.GraphicOverlayPlane:
            return "GraphicOverlayPlane";
        case PrivateTag.LinearPresentationGLTrafoShapeSub:
            return "LinearPresentationGLTrafoShapeSub";
        case PrivateTag.LinearModalityGLTrafo:
            return "LinearModalityGLTrafo";
        case PrivateTag.DisplayShutter:
            return "DisplayShutter";
        case PrivateTag.SpatialTransformation:
            return "SpatialTransformation";
        case PrivateTag._2001_xx6B_:
            return "_2001_xx6B_";
        case PrivateTag.TextFont:
            return "TextFont";
        case PrivateTag.SeriesType:
            return "SeriesType";
        case PrivateTag.GraphicConstraint:
            return "GraphicConstraint";
        case PrivateTag.EllipsDisplShutOtherAxScndEndPnt:
            return "EllipsDisplShutOtherAxScndEndPnt";
        case PrivateTag._2001_xx74_:
            return "_2001_xx74_";
        case PrivateTag._2001_xx75_:
            return "_2001_xx75_";
        case PrivateTag.NumberOfFrames:
            return "NumberOfFrames";
        case PrivateTag.GLTrafoType:
            return "GLTrafoType";
        case PrivateTag.WindowRoundingFactor:
            return "WindowRoundingFactor";
        case PrivateTag.AcquisitionNumber:
            return "AcquisitionNumber";
        case PrivateTag.FrameNumber:
            return "FrameNumber";
        case PrivateTag._2001_xx80_:
            return "_2001_xx80_";
        case PrivateTag.NumberOfDynamicScans:
            return "NumberOfDynamicScans";
        case PrivateTag.EchoTrainLength:
            return "EchoTrainLength";
        case PrivateTag.ImagingFrequency:
            return "ImagingFrequency";
        case PrivateTag.InversionTime:
            return "InversionTime";
        case PrivateTag.MagneticFieldStrength:
            return "MagneticFieldStrength";
        case PrivateTag.NrOfPhaseEncodingSteps:
            return "NrOfPhaseEncodingSteps";
        case PrivateTag.ImagedNucleus:
            return "ImagedNucleus";
        case PrivateTag.NumberOfAverages:
            return "NumberOfAverages";
        case PrivateTag.PhaseFOVPercent:
            return "PhaseFOVPercent";
        case PrivateTag.SamplingPercent:
            return "SamplingPercent";
        case PrivateTag.TransmittingCoil:
            return "TransmittingCoil";
        case PrivateTag.TextForegroundColor:
            return "TextForegroundColor";
        case PrivateTag.TextBackgroundColor:
            return "TextBackgroundColor";
        case PrivateTag.TextShadowColor:
            return "TextShadowColor";
        case PrivateTag.TextStyle:
            return "TextStyle";
        case PrivateTag._2001_xx9A_:
            return "_2001_xx9A_";
        case PrivateTag.GraphicNumber:
            return "GraphicNumber";
        case PrivateTag.GraphicAnnotationLabel:
            return "GraphicAnnotationLabel";
        case PrivateTag.PixelProcessingKernelSize:
            return "PixelProcessingKernelSize";
        case PrivateTag.IsRawImage:
            return "IsRawImage";
        case PrivateTag.TextColorForeground:
            return "TextColorForeground";
        case PrivateTag.TextColorBackground:
            return "TextColorBackground";
        case PrivateTag.TextColorShadow:
            return "TextColorShadow";
        case PrivateTag.ExamCardName:
            return "ExamCardName";
        case PrivateTag.DerivationDescription:
            return "DerivationDescription";
        case PrivateTag._2001_xxDA_:
            return "_2001_xxDA_";
        case PrivateTag.ProspectiveMotionCorrection:
            return "ProspectiveMotionCorrection";
        case PrivateTag.RetrospectiveMotionCorrection:
            return "RetrospectiveMotionCorrection";
        }
        return "";
    }

}
