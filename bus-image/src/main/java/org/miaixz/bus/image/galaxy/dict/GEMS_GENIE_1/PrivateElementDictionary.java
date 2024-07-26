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
package org.miaixz.bus.image.galaxy.dict.GEMS_GENIE_1;

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

        case PrivateTag.eNTEGRADataObjectType:
            return VR.CS;
        case PrivateTag.PatientCreationDate:
            return VR.DA;
        case PrivateTag.AcqZoomRetired:
        case PrivateTag.AcqPanRetired:
            return VR.DS;
        case PrivateTag.DistancePrescribed:
        case PrivateTag.RotationalContinuousSpeed:
        case PrivateTag.RadiusOfRotation:
        case PrivateTag.PhaseDurationRetired:
        case PrivateTag.ViewXAdjustment:
        case PrivateTag.ViewYAdjustment:
        case PrivateTag.PixelScale:
        case PrivateTag.PixelOffset:
        case PrivateTag.RotationAngle:
        case PrivateTag.ThresholdCenter:
        case PrivateTag.ThresholdWidth:
        case PrivateTag.Period:
        case PrivateTag.ElapsedTime:
        case PrivateTag.FOV:
        case PrivateTag.LinearFOV:
        case PrivateTag.SpatialOffset:
        case PrivateTag.SpatialOrientation:
        case PrivateTag.MotionThreshold:
        case PrivateTag.ReconPanAPOffset:
        case PrivateTag.ReconPanLROffset:
        case PrivateTag.ReconArea:
        case PrivateTag.RefTransPixelVolume:
        case PrivateTag.InterpolationDistance:
        case PrivateTag.InterpolationCenterX:
        case PrivateTag.InterpolationCenterY:
        case PrivateTag.RfmtrTransRefmm:
        case PrivateTag.DigitalFOV:
        case PrivateTag.Fscalar:
        case PrivateTag.TransmissionScanTime:
        case PrivateTag.TransmissionMaskWidth:
        case PrivateTag.CopperAttenuatorThickness:
        case PrivateTag.DetAngSeparation:
        case PrivateTag.TomoViewOffset:
        case PrivateTag.AcceptedBeatsTime:
        case PrivateTag.Threshold:
        case PrivateTag.LinearDepth:
        case PrivateTag.DoubleData:
        case PrivateTag.StartAngle:
        case PrivateTag.SegStart:
        case PrivateTag.SegEnd:
        case PrivateTag.TxtX:
        case PrivateTag.TxtY:
        case PrivateTag.ROIArea:
        case PrivateTag.Seeds:
        case PrivateTag.ShapeTilt:
        case PrivateTag.ShapeCtrlPts:
            return VR.FD;
        case PrivateTag.StudyName:
        case PrivateTag.SeriesObjectName:
        case PrivateTag.TriggerHistoryUID:
        case PrivateTag.SeriesComments:
        case PrivateTag._0009_xx30_:
        case PrivateTag.RadioNuclideName:
        case PrivateTag.DatasetObjectName:
        case PrivateTag.DatasetName:
        case PrivateTag.CompletionTime:
        case PrivateTag.PictureObjectName:
        case PrivateTag.AcquisitionParentUID:
        case PrivateTag.ProcessingParentUID:
        case PrivateTag.EnergyCorrectName:
        case PrivateTag.SpatialCorrectName:
        case PrivateTag.TuningCalibName:
        case PrivateTag.UniformityCorrectName:
        case PrivateTag.ViewingObjectName:
        case PrivateTag.WhereObjectName:
        case PrivateTag.ReferenceDatasetUID:
        case PrivateTag.UnifDateTime:
        case PrivateTag.OriginalSOPInstanceUID:
        case PrivateTag.Name:
        case PrivateTag.ProtocolDataUID:
        case PrivateTag.RelevantDataUID:
        case PrivateTag.BulkData:
        case PrivateTag.SOPClassUID:
        case PrivateTag.SOPInstanceUID:
        case PrivateTag.Legend:
        case PrivateTag.XUnits:
        case PrivateTag.YUnits:
        case PrivateTag.StyleColour:
        case PrivateTag.StylePColour:
        case PrivateTag.SegStyleColour:
        case PrivateTag.SegName:
        case PrivateTag.TxtText:
        case PrivateTag.TxtName:
        case PrivateTag.ROIName:
        case PrivateTag.DerivedFromImageUID:
        case PrivateTag.CurveName:
        case PrivateTag.CurveUID:
        case PrivateTag.SoftwareVersion:
        case PrivateTag.DetectorName:
        case PrivateTag.NormalColor:
        case PrivateTag.Label:
        case PrivateTag.DatasetROIMapping:
            return VR.LO;
        case PrivateTag._0009_xx45_:
        case PrivateTag.AcquisitionSpecificCorrectName:
        case PrivateTag.StudyComments:
        case PrivateTag.BulkDataFormat:
        case PrivateTag.IntDataFormat:
        case PrivateTag.DoubleDataFormat:
        case PrivateTag.StringDataFormat:
        case PrivateTag.Description:
        case PrivateTag.NameFont:
            return VR.LT;
        case PrivateTag.StringData:
        case PrivateTag.TriggerData:
            return VR.OB;
        case PrivateTag.PatientObjectName:
            return VR.PN;
        case PrivateTag._0009_xx01_:
        case PrivateTag.UserOrientation:
        case PrivateTag.StarcamReferenceDataset:
        case PrivateTag.PreFilterParam:
        case PrivateTag.PreFilterParam2:
        case PrivateTag.BackProjFilterParam:
        case PrivateTag.BackProjFilterParam2:
        case PrivateTag.AttenuationCoef:
        case PrivateTag.AttenuationThreshold:
        case PrivateTag.Date:
        case PrivateTag.Time:
        case PrivateTag.StartDate:
        case PrivateTag.CompletionDate:
            return VR.SH;
        case PrivateTag.StudyFlags:
        case PrivateTag.StudyType:
        case PrivateTag.SeriesFlags:
        case PrivateTag.InitiationType:
        case PrivateTag.InitiationDelay:
        case PrivateTag.InitiationCountRate:
        case PrivateTag.NumberEnergySets:
        case PrivateTag.NumberDetectors:
        case PrivateTag.NumberRRWindows:
        case PrivateTag.NumberMGTimeSlots:
        case PrivateTag.NumberViewSets:
        case PrivateTag.TrackBeatAverage:
        case PrivateTag.TableDirection:
        case PrivateTag.GantryMotionTypeRetired:
        case PrivateTag.GantryLocusType:
        case PrivateTag.StartingHeartRate:
        case PrivateTag.RRWindowWidth:
        case PrivateTag.RRWindowOffset:
        case PrivateTag.PercentCycleImaged:
        case PrivateTag.PatientFlags:
        case PrivateTag.NumViewsAcquiredRetired:
        case PrivateTag.SeriesType:
        case PrivateTag.EffectiveSeriesDuration:
        case PrivateTag.NumBeats:
        case PrivateTag.DatasetModified:
        case PrivateTag.DatasetType:
        case PrivateTag.DetectorNumber:
        case PrivateTag.EnergyNumber:
        case PrivateTag.RRIntervalWindowNumber:
        case PrivateTag.MGBinNumber:
        case PrivateTag.DetectorCountZone:
        case PrivateTag.NumEnergyWindows:
        case PrivateTag.EnergyOffset:
        case PrivateTag.EnergyRange:
        case PrivateTag.EnergyWidthRetired:
        case PrivateTag.ImageOrientation:
        case PrivateTag.UseFOVMask:
        case PrivateTag.FOVMaskYCutoffAngle:
        case PrivateTag.FOVMaskCutoffAngle:
        case PrivateTag.TableOrientation:
        case PrivateTag.ROITopLeft:
        case PrivateTag.ROIBottomRight:
        case PrivateTag.UniformityMean:
        case PrivateTag.PixelOverflowFlag:
        case PrivateTag.OverflowLevel:
        case PrivateTag.ByteOrder:
        case PrivateTag.CompressionType:
        case PrivateTag.PictureFormat:
        case PrivateTag.EnergyPeakRetired:
        case PrivateTag.FOVShape:
        case PrivateTag.DatasetFlags:
        case PrivateTag.OrientationAngle:
        case PrivateTag.WindowInverseFlag:
        case PrivateTag.InterpolationType:
        case PrivateTag.ImageSize:
        case PrivateTag.ReferenceFrameNumber:
        case PrivateTag.CursorLength:
        case PrivateTag.NumberOfCursors:
        case PrivateTag.CursorCoordinates:
        case PrivateTag.ReconOptionsFlag:
        case PrivateTag.ReconType:
        case PrivateTag.PreFilterType:
        case PrivateTag.BackProjFilterType:
        case PrivateTag.ReconArc:
        case PrivateTag.StartView:
        case PrivateTag.AttenuationType:
        case PrivateTag.DualEnergyProcessing:
        case PrivateTag.RefSliceWidth:
        case PrivateTag.QuantFilterFlag:
        case PrivateTag.HeadConversion:
        case PrivateTag.SliceWidthPixels:
        case PrivateTag.RfmtrTransRef:
        case PrivateTag.TwoLineTransRef:
        case PrivateTag.ThreeDZero:
        case PrivateTag.ThreeDZeroLength:
        case PrivateTag.ThreeDZeroIn:
        case PrivateTag.SourceTranslator:
        case PrivateTag.RALFlags:
        case PrivateTag.OriginalImageNumber:
        case PrivateTag.AutoTrackPeak:
        case PrivateTag.AutoTrackWidth:
        case PrivateTag.AxialAcceptanceAngle:
        case PrivateTag.ThetaAcceptanceValue:
        case PrivateTag.SeriesAcceptedBeats:
        case PrivateTag.SeriesRejectedBeats:
        case PrivateTag.FrameTerminationCondition:
        case PrivateTag.FrameTerminationValue:
        case PrivateTag.NumECTPhases:
        case PrivateTag.NumWBScans:
        case PrivateTag.ECTPhaseNum:
        case PrivateTag.WBScanNum:
        case PrivateTag.CombHeadNumber:
        case PrivateTag.Modified:
        case PrivateTag.IntData:
        case PrivateTag.Cid:
        case PrivateTag.Srid:
        case PrivateTag.CurveType:
        case PrivateTag.GraphType:
        case PrivateTag.Edit:
        case PrivateTag.Suspend:
        case PrivateTag.StyleLine:
        case PrivateTag.StyleFill:
        case PrivateTag.StyleWidth:
        case PrivateTag.StylePoint:
        case PrivateTag.StylePSize:
        case PrivateTag.Segments:
        case PrivateTag.SegType:
        case PrivateTag.SegStyleLine:
        case PrivateTag.SegStyleFill:
        case PrivateTag.SegStyleWidth:
        case PrivateTag.SegStylePoint:
        case PrivateTag.SegStylePColour:
        case PrivateTag.SegStylePSize:
        case PrivateTag.SegAllowDirInt:
        case PrivateTag.TextAnnots:
        case PrivateTag.DerivedFromImages:
        case PrivateTag.FillPattern:
        case PrivateTag.LineStyle:
        case PrivateTag.LineDashLength:
        case PrivateTag.LineThickness:
        case PrivateTag.Interactivity:
        case PrivateTag.NamePos:
        case PrivateTag.NameDisplay:
        case PrivateTag.BpSeg:
        case PrivateTag.SeedSpace:
        case PrivateTag.Shape:
        case PrivateTag.ShapePtsSpace:
        case PrivateTag.ShapeCtrlPtsCounts:
        case PrivateTag.ShapeCtrlPSpace:
        case PrivateTag.ROIFlags:
        case PrivateTag.FrameNumber:
        case PrivateTag.ID:
            return VR.SL;
        case PrivateTag.eNTEGRAFrameSequence:
        case PrivateTag._0019_xx5F_:
        case PrivateTag.eNTEGRAEnergyWindowInformationSequence:
        case PrivateTag.eNTEGRAEnergyWindowRangeSequence:
        case PrivateTag.eNTEGRADetectorInformationSequence:
        case PrivateTag.eNTEGRARotationInformationSequence:
        case PrivateTag.eNTEGRAGatedInformationSequence:
        case PrivateTag.eNTEGRADataInformationSequence:
        case PrivateTag.SDODoubleDataSequence:
        case PrivateTag._0055_xx65_:
            return VR.SQ;
        case PrivateTag.PatientCreationTime:
            return VR.TM;
        case PrivateTag.DatasetUID:
        case PrivateTag._0009_xx46_:
        case PrivateTag.MotionCurveUID:
            return VR.UI;
        case PrivateTag.PrecedingBeat:
        case PrivateTag.ProtocolDataFlags:
        case PrivateTag.ProtocolName:
        case PrivateTag.AllocateTriggerBuffer:
        case PrivateTag.NumberOfTriggers:
        case PrivateTag.TriggerSize:
        case PrivateTag.TriggerDataSize:
        case PrivateTag.CurveFlags:
            return VR.UL;
        case PrivateTag.BpSegPairs:
            return VR.US;
        }
        return VR.UN;
    }
}
