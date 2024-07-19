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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_SDI_02;

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
        
            case PrivateTag.DiffusionDirectionality4MF:
            case PrivateTag.PhaseContrastN4:
            case PrivateTag.ImageType4MF:
            case PrivateTag.ImageTypeVisible:
            case PrivateTag.DistortionCorrectionType:
            case PrivateTag.ImageFilterType:
                return VR.CS;
            case PrivateTag.SliceMeasurementDuration:
            case PrivateTag.TimeAfterStart:
            case PrivateTag.VoxelInPlaneRot:
            case PrivateTag.VoxelThickness:
            case PrivateTag.VoxelNormalCor:
            case PrivateTag.VoxelNormalSag:
            case PrivateTag.VoxelPositionSag:
            case PrivateTag.VoxelNormalTra:
            case PrivateTag.VoxelPositionTra:
            case PrivateTag.RepetitionTimeEffective:
            case PrivateTag.CSIImageOrientationPatient:
            case PrivateTag.CSISliceLocation:
            case PrivateTag.VoxelPositionCor:
            case PrivateTag.VoxelReadoutFOV:
            case PrivateTag.VoxelPhaseFOV:
            case PrivateTag.CSIPixelSpacing:
            case PrivateTag.CSIImagePositionPatient:
            case PrivateTag.CSISliceThickness:
            case PrivateTag.NumberOfAveragesN4:
                return VR.DS;
            case PrivateTag.SliceNormalVector:
            case PrivateTag.BMatrix:
            case PrivateTag.FlowVENC:
            case PrivateTag.FMRIStimulLevel:
            case PrivateTag.RBMoCoRot:
            case PrivateTag.DiffusionGradientDirection:
            case PrivateTag.BandwidthPerPixelPhaseEncode:
            case PrivateTag.RBMoCoTrans:
            case PrivateTag.SlicePositionPCS:
            case PrivateTag.MosaicRefAcqTimes:
            case PrivateTag.QCData:
            case PrivateTag.VelocityEncodingDirectionN4:
                return VR.FD;
            case PrivateTag.BValue:
            case PrivateTag.PhaseEncodingDirectionPositive:
            case PrivateTag.FMRIStimulInfo:
            case PrivateTag.MultistepIndex:
            case PrivateTag.CompAlgorithm:
            case PrivateTag.EchoColumnPosition:
            case PrivateTag.MeasuredFourierLines:
            case PrivateTag.Filter2:
            case PrivateTag.NormalizeManipulated:
            case PrivateTag.CompManualAdjusted:
            case PrivateTag.RealDwellTime:
            case PrivateTag.CompBlended:
            case PrivateTag.FlowEncodingDirection:
            case PrivateTag.EchoPartitionPosition:
            case PrivateTag.EchoLinePosition:
            case PrivateTag.OriginalImageNumber:
            case PrivateTag.OriginalSeriesNumber:
            case PrivateTag.Actual3DImaPartNumber:
            case PrivateTag.ImaRelTablePosition:
            case PrivateTag.ProtocolSliceNumber:
            case PrivateTag.Filter1:
            case PrivateTag.AutoInlineImageFilterEnabled:
                return VR.IS;
            case PrivateTag.ICEDims:
            case PrivateTag.ImaCoilString:
            case PrivateTag.ImaPATModeText:
            case PrivateTag.ImageHistory:
            case PrivateTag.SequenceInfo:
                return VR.LO;
            case PrivateTag.CompAdjustedParam:
            case PrivateTag.CompJobID:
            case PrivateTag.CompAutoParam:
            case PrivateTag.ExamLandmarks:
                return VR.LT;
            case PrivateTag.PixelFile:
            case PrivateTag.PixelFileName:
            case PrivateTag.MRDiffusion:
            case PrivateTag.RealWorldValueMapping:
            case PrivateTag.DataSetInfo:
                return VR.OB;
            case PrivateTag.RFSWDDataType:
            case PrivateTag.FlowEncodingDirectionString:
            case PrivateTag.LQAlgorithm:
            case PrivateTag.SpectrumTextRegionLabel:
            case PrivateTag.GSWDDataType:
            case PrivateTag.AcquisitionMatrixText:
            case PrivateTag.TransmittingCoil:
                return VR.SH;
            case PrivateTag.ImaAbsTablePosition:
                return VR.SL;
            case PrivateTag.SiemensMRSDISequence:
                return VR.SQ;
            case PrivateTag.ExamDataRole:
                return VR.ST;
            case PrivateTag.UsedChannelMask:
            case PrivateTag.SequenceMask:
                return VR.UL;
            case PrivateTag.NumberOfImagesInMosaic:
            case PrivateTag.MoCoQMeasure:
            case PrivateTag.ImageGroup:
            case PrivateTag.NonPlanarImage:
                return VR.US;
            case PrivateTag.UsedChannelString:
            case PrivateTag.MRVelocityEncoding:
                return VR.UT;
        }
        return VR.UN;
    }
}
