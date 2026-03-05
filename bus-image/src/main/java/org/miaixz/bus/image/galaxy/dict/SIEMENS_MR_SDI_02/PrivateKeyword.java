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
