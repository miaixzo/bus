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
package org.miaixz.bus.image.galaxy.dict.GEMS_ACQU_01;

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
        
            case PrivateTag._0009_xx24_:
            case PrivateTag.CellNumberAtTheta:
            case PrivateTag.CellSpacing:
            case PrivateTag.HorizontalFrameOfReference:
            case PrivateTag.FirstScanLocation:
            case PrivateTag.LastScanLocation:
            case PrivateTag.DisplayFieldOfView:
            case PrivateTag._0019_xx20_:
            case PrivateTag._0019_xx22_:
            case PrivateTag.TableSpeed:
            case PrivateTag.MidScanTime:
            case PrivateTag.RotationSpeed:
            case PrivateTag.XrayOnPosition:
            case PrivateTag.XrayOffPosition:
            case PrivateTag.AngleOfFirstView:
            case PrivateTag.TriggerFrequency:
            case PrivateTag.InterscanDelay:
            case PrivateTag.ZChannelAvgOverViews:
            case PrivateTag.AvgOfLeftRefChannelsOverViews:
            case PrivateTag.MaxLeftChannelOverViews:
            case PrivateTag.AvgOfRightRefChannelsOverViews:
            case PrivateTag.MaxRightChannelOverViews:
            case PrivateTag.SecondEcho:
            case PrivateTag.TableDelta:
            case PrivateTag._0019_xx83_:
            case PrivateTag.PeakSAR:
            case PrivateTag.CardiacRepetitionTime:
            case PrivateTag.DelayAfterTrigger:
            case PrivateTag.PauseTime:
            case PrivateTag.AutoPrescanCenterFrequency:
            case PrivateTag.ReceiveBandwidth:
            case PrivateTag.UserData0:
            case PrivateTag.UserData1:
            case PrivateTag.UserData2:
            case PrivateTag.UserData3:
            case PrivateTag.UserData4:
            case PrivateTag.UserData5:
            case PrivateTag.UserData6:
            case PrivateTag.UserData7:
            case PrivateTag.UserData8:
            case PrivateTag.UserData9:
            case PrivateTag.UserData10:
            case PrivateTag.UserData11:
            case PrivateTag.UserData12:
            case PrivateTag.UserData13:
            case PrivateTag.UserData14:
            case PrivateTag.UserData15:
            case PrivateTag.UserData16:
            case PrivateTag.UserData17:
            case PrivateTag.UserData18:
            case PrivateTag.UserData19:
            case PrivateTag.UserData20:
            case PrivateTag.UserData21:
            case PrivateTag.UserData22:
            case PrivateTag.ProjectionAngle:
            case PrivateTag.ConcatenatedSatOrDTIDiffusionDirection:
            case PrivateTag.BackProjectorCoefficient:
            case PrivateTag.DynamicZAlphaValue:
            case PrivateTag.UserData23:
            case PrivateTag.UserData24:
            case PrivateTag._0019_xxE1_:
            case PrivateTag.VelocityEncodeScale:
            case PrivateTag._0019_xxE8_:
            case PrivateTag._0019_xxE9_:
            case PrivateTag._0019_xxEB_:
            case PrivateTag.TransmitGain:
                return VR.DS;
            case PrivateTag.PulseSequenceDate:
                return VR.DT;
            case PrivateTag.AcquisitionDuration:
                return VR.FL;
            case PrivateTag._0009_xxFB_:
            case PrivateTag._0019_xx3A_:
            case PrivateTag._0019_xxE5_:
                return VR.IS;
            case PrivateTag.FirstScanRAS:
            case PrivateTag.LastScanRAS:
            case PrivateTag.PulseSequenceName:
            case PrivateTag.InternalPulseSequenceName:
                return VR.LO;
            case PrivateTag._0019_xx01_:
            case PrivateTag._0019_xx05_:
            case PrivateTag._0019_xx3B_:
            case PrivateTag._0019_xxE3_:
            case PrivateTag._0019_xxE4_:
            case PrivateTag._0019_xxF1_:
            case PrivateTag._0019_xxF3_:
            case PrivateTag._0019_xxF4_:
                return VR.LT;
            case PrivateTag.RawDataType:
            case PrivateTag.ProjectionAlgorithmName:
                return VR.SH;
            case PrivateTag.NumberOfCellsInDetector:
            case PrivateTag.TubeAzimuth:
            case PrivateTag.NumberOfTriggers:
            case PrivateTag.DataSizeForScanData:
            case PrivateTag.NumberOfChannels1To512:
            case PrivateTag.IncrementBetweenChannels:
            case PrivateTag.StartingView:
            case PrivateTag.NumberOfViews:
            case PrivateTag.IncrementBetweenViews:
            case PrivateTag.SliceOffsetOnFrequencyAxis:
            case PrivateTag.BitmapDefiningCVs:
            case PrivateTag.RawDataRunNumber:
                return VR.SL;
            case PrivateTag.SeriesContrast:
            case PrivateTag.LastPseq:
            case PrivateTag.StartNumberForBaseline:
            case PrivateTag.EndNumberForBaseline:
            case PrivateTag.StartNumberForEnhancedScans:
            case PrivateTag.EndNumberForEnhancedScans:
            case PrivateTag.SeriesPlane:
            case PrivateTag.MidScanFlag:
            case PrivateTag.ScanFOVType:
            case PrivateTag.StatReconFlag:
            case PrivateTag.ComputeType:
            case PrivateTag.SegmentNumber:
            case PrivateTag.TotalSegmentsRequired:
            case PrivateTag.ViewCompressionFactor:
            case PrivateTag.TotalNumberOfRefChannels:
            case PrivateTag.ReconPostProcessingFlag:
            case PrivateTag.CTWaterNumber:
            case PrivateTag.CTBoneNumber:
            case PrivateTag.DependantOnNumberOfViewsProcessed:
            case PrivateTag.FieldOfViewInDetectorCells:
            case PrivateTag.ValueOfBackProjectionButton:
            case PrivateTag.SetIfFatqEstimatesWereUsed:
            case PrivateTag.NumberOfEchos:
            case PrivateTag.Contiguous:
            case PrivateTag.MonitorSAR:
            case PrivateTag.ImagesPerCardiacCycle:
            case PrivateTag.ActualReceiveGainAnalog:
            case PrivateTag.ActualReceiveGainDigital:
            case PrivateTag.SwapPhaseFrequency:
            case PrivateTag.PauseInterval:
            case PrivateTag.AutoPrescanTransmitGain:
            case PrivateTag.AutoPrescanAnalogReceiverGain:
            case PrivateTag.AutoPrescanDigitalReceiverGain:
            case PrivateTag.CenterFrequencyMethod:
            case PrivateTag.PulseSequenceMode:
            case PrivateTag.TransmittingCoil:
            case PrivateTag.SurfaceCoilType:
            case PrivateTag.ExtremityCoilFlag:
            case PrivateTag.SATFatWaterBone:
            case PrivateTag.SaturationPlanes:
            case PrivateTag.SurfaceCoilIntensityCorrectionFlag:
            case PrivateTag.SATLocationR:
            case PrivateTag.SATLocationL:
            case PrivateTag.SATLocationA:
            case PrivateTag.SATLocationP:
            case PrivateTag.SATLocationH:
            case PrivateTag.SATLocationF:
            case PrivateTag.SATThicknessRL:
            case PrivateTag.SATThicknessAP:
            case PrivateTag.SATThicknessHF:
            case PrivateTag.PhaseContrastFlowAxis:
            case PrivateTag.VelocityEncoding:
            case PrivateTag.ThicknessDisclaimer:
            case PrivateTag.PrescanType:
            case PrivateTag.PrescanStatus:
            case PrivateTag.ProjectionAlgorithm:
            case PrivateTag.FractionalEcho:
            case PrivateTag.PrepPulse:
            case PrivateTag.CardiacPhases:
            case PrivateTag.VariableEchoFlag:
            case PrivateTag.ReferenceChannelUsed:
            case PrivateTag.PrimarySpeedCorrectionUsed:
            case PrivateTag.OverrangeCorrectionUsed:
            case PrivateTag.FastPhases:
                return VR.SS;
            case PrivateTag.CalibratedFieldStrength:
                return VR.UL;
            case PrivateTag._0009_xx25_:
            case PrivateTag._0009_xx3E_:
            case PrivateTag._0009_xx3F_:
            case PrivateTag._0009_xx42_:
            case PrivateTag._0009_xx43_:
            case PrivateTag._0009_xxF8_:
            case PrivateTag._0019_xx0E_:
            case PrivateTag._0019_xx2D_:
            case PrivateTag._0019_xx48_:
            case PrivateTag._0019_xx49_:
            case PrivateTag._0019_xx5D_:
            case PrivateTag._0019_xx82_:
            case PrivateTag._0019_xx86_:
            case PrivateTag._0019_xx99_:
            case PrivateTag._0019_xxD4_:
            case PrivateTag._0019_xxE6_:
            case PrivateTag._0019_xxEC_:
                return VR.US;
        }
        return VR.UN;
    }
}
