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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
                case PrivateTag.NumberOfImagesInMosaic:
            return "NumberOfImagesInMosaic";
        case PrivateTag.SliceNormalVector:
            return "SliceNormalVector";
        case PrivateTag.SliceMeasurementDuration:
            return "SliceMeasurementDuration";
        case PrivateTag.TimeAfterStart:
            return "TimeAfterStart";
        case PrivateTag.BValue:
            return "BValue";
        case PrivateTag.ICEDims:
            return "ICEDims";
        case PrivateTag.RFSWDDataType:
            return "RFSWDDataType";
        case PrivateTag.MoCoQMeasure:
            return "MoCoQMeasure";
        case PrivateTag.PhaseEncodingDirectionPositive:
            return "PhaseEncodingDirectionPositive";
        case PrivateTag.PixelFile:
            return "PixelFile";
        case PrivateTag.FMRIStimulInfo:
            return "FMRIStimulInfo";
        case PrivateTag.VoxelInPlaneRot:
            return "VoxelInPlaneRot";
        case PrivateTag.DiffusionDirectionality4MF:
            return "DiffusionDirectionality4MF";
        case PrivateTag.VoxelThickness:
            return "VoxelThickness";
        case PrivateTag.BMatrix:
            return "BMatrix";
        case PrivateTag.MultistepIndex:
            return "MultistepIndex";
        case PrivateTag.CompAdjustedParam:
            return "CompAdjustedParam";
        case PrivateTag.CompAlgorithm:
            return "CompAlgorithm";
        case PrivateTag.VoxelNormalCor:
            return "VoxelNormalCor";
        case PrivateTag.FlowEncodingDirectionString:
            return "FlowEncodingDirectionString";
        case PrivateTag.VoxelNormalSag:
            return "VoxelNormalSag";
        case PrivateTag.VoxelPositionSag:
            return "VoxelPositionSag";
        case PrivateTag.VoxelNormalTra:
            return "VoxelNormalTra";
        case PrivateTag.VoxelPositionTra:
            return "VoxelPositionTra";
        case PrivateTag.UsedChannelMask:
            return "UsedChannelMask";
        case PrivateTag.RepetitionTimeEffective:
            return "RepetitionTimeEffective";
        case PrivateTag.CSIImageOrientationPatient:
            return "CSIImageOrientationPatient";
        case PrivateTag.CSISliceLocation:
            return "CSISliceLocation";
        case PrivateTag.EchoColumnPosition:
            return "EchoColumnPosition";
        case PrivateTag.FlowVENC:
            return "FlowVENC";
        case PrivateTag.MeasuredFourierLines:
            return "MeasuredFourierLines";
        case PrivateTag.LQAlgorithm:
            return "LQAlgorithm";
        case PrivateTag.VoxelPositionCor:
            return "VoxelPositionCor";
        case PrivateTag.Filter2:
            return "Filter2";
        case PrivateTag.FMRIStimulLevel:
            return "FMRIStimulLevel";
        case PrivateTag.VoxelReadoutFOV:
            return "VoxelReadoutFOV";
        case PrivateTag.NormalizeManipulated:
            return "NormalizeManipulated";
        case PrivateTag.RBMoCoRot:
            return "RBMoCoRot";
        case PrivateTag.CompManualAdjusted:
            return "CompManualAdjusted";
        case PrivateTag.SpectrumTextRegionLabel:
            return "SpectrumTextRegionLabel";
        case PrivateTag.VoxelPhaseFOV:
            return "VoxelPhaseFOV";
        case PrivateTag.GSWDDataType:
            return "GSWDDataType";
        case PrivateTag.RealDwellTime:
            return "RealDwellTime";
        case PrivateTag.CompJobID:
            return "CompJobID";
        case PrivateTag.CompBlended:
            return "CompBlended";
        case PrivateTag.ImaAbsTablePosition:
            return "ImaAbsTablePosition";
        case PrivateTag.DiffusionGradientDirection:
            return "DiffusionGradientDirection";
        case PrivateTag.FlowEncodingDirection:
            return "FlowEncodingDirection";
        case PrivateTag.EchoPartitionPosition:
            return "EchoPartitionPosition";
        case PrivateTag.EchoLinePosition:
            return "EchoLinePosition";
        case PrivateTag.CompAutoParam:
            return "CompAutoParam";
        case PrivateTag.OriginalImageNumber:
            return "OriginalImageNumber";
        case PrivateTag.OriginalSeriesNumber:
            return "OriginalSeriesNumber";
        case PrivateTag.Actual3DImaPartNumber:
            return "Actual3DImaPartNumber";
        case PrivateTag.ImaCoilString:
            return "ImaCoilString";
        case PrivateTag.CSIPixelSpacing:
            return "CSIPixelSpacing";
        case PrivateTag.SequenceMask:
            return "SequenceMask";
        case PrivateTag.ImageGroup:
            return "ImageGroup";
        case PrivateTag.BandwidthPerPixelPhaseEncode:
            return "BandwidthPerPixelPhaseEncode";
        case PrivateTag.NonPlanarImage:
            return "NonPlanarImage";
        case PrivateTag.PixelFileName:
            return "PixelFileName";
        case PrivateTag.ImaPATModeText:
            return "ImaPATModeText";
        case PrivateTag.CSIImagePositionPatient:
            return "CSIImagePositionPatient";
        case PrivateTag.AcquisitionMatrixText:
            return "AcquisitionMatrixText";
        case PrivateTag.ImaRelTablePosition:
            return "ImaRelTablePosition";
        case PrivateTag.RBMoCoTrans:
            return "RBMoCoTrans";
        case PrivateTag.SlicePositionPCS:
            return "SlicePositionPCS";
        case PrivateTag.CSISliceThickness:
            return "CSISliceThickness";
        case PrivateTag.ProtocolSliceNumber:
            return "ProtocolSliceNumber";
        case PrivateTag.Filter1:
            return "Filter1";
        case PrivateTag.TransmittingCoil:
            return "TransmittingCoil";
        case PrivateTag.NumberOfAveragesN4:
            return "NumberOfAveragesN4";
        case PrivateTag.MosaicRefAcqTimes:
            return "MosaicRefAcqTimes";
        case PrivateTag.AutoInlineImageFilterEnabled:
            return "AutoInlineImageFilterEnabled";
        case PrivateTag.QCData:
            return "QCData";
        case PrivateTag.ExamLandmarks:
            return "ExamLandmarks";
        case PrivateTag.ExamDataRole:
            return "ExamDataRole";
        case PrivateTag.MRDiffusion:
            return "MRDiffusion";
        case PrivateTag.RealWorldValueMapping:
            return "RealWorldValueMapping";
        case PrivateTag.DataSetInfo:
            return "DataSetInfo";
        case PrivateTag.UsedChannelString:
            return "UsedChannelString";
        case PrivateTag.PhaseContrastN4:
            return "PhaseContrastN4";
        case PrivateTag.MRVelocityEncoding:
            return "MRVelocityEncoding";
        case PrivateTag.VelocityEncodingDirectionN4:
            return "VelocityEncodingDirectionN4";
        case PrivateTag.ImageType4MF:
            return "ImageType4MF";
        case PrivateTag.ImageHistory:
            return "ImageHistory";
        case PrivateTag.SequenceInfo:
            return "SequenceInfo";
        case PrivateTag.ImageTypeVisible:
            return "ImageTypeVisible";
        case PrivateTag.DistortionCorrectionType:
            return "DistortionCorrectionType";
        case PrivateTag.ImageFilterType:
            return "ImageFilterType";
        case PrivateTag.SiemensMRSDISequence:
            return "SiemensMRSDISequence";
        }
        return "";
    }

}
