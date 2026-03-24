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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SMS_USG_ANTARES_3D_VOLUME;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.ReleaseVersion:
                return "ReleaseVersion";

            case PrivateTag.VolumeAcquisitionDuration:
                return "VolumeAcquisitionDuration";

            case PrivateTag.VolumeRawDataType:
                return "VolumeRawDataType";

            case PrivateTag.ScanType:
                return "ScanType";

            case PrivateTag.ZlateralMin:
                return "ZlateralMin";

            case PrivateTag.ZlateralSpan:
                return "ZlateralSpan";

            case PrivateTag.ZRadiusOfCurvature:
                return "ZRadiusOfCurvature";

            case PrivateTag.WobbleCorrection:
                return "WobbleCorrection";

            case PrivateTag.ScaleAlongWidth:
                return "ScaleAlongWidth";

            case PrivateTag.ScaleAlongHeight:
                return "ScaleAlongHeight";

            case PrivateTag.ScaleAlongDepth:
                return "ScaleAlongDepth";

            case PrivateTag.BufferSize:
                return "BufferSize";

            case PrivateTag.AcquisitionRate:
                return "AcquisitionRate";

            case PrivateTag.DepthMinCm:
                return "DepthMinCm";

            case PrivateTag.IsLeftRightFlippedEn:
                return "IsLeftRightFlippedEn";

            case PrivateTag.IsUpDownFlippedEn:
                return "IsUpDownFlippedEn";

            case PrivateTag.IsVolumeGeomAccurate:
                return "IsVolumeGeomAccurate";

            case PrivateTag.BByteMaskOffset:
                return "BByteMaskOffset";

            case PrivateTag.BByteMaskSize:
                return "BByteMaskSize";

            case PrivateTag.AcqPlaneRotationDeg:
                return "AcqPlaneRotationDeg";

            case PrivateTag.BeamAxialSpan:
                return "BeamAxialSpan";

            case PrivateTag.BeamLateralMin:
                return "BeamLateralMin";

            case PrivateTag.BeamLateralSpan:
                return "BeamLateralSpan";

            case PrivateTag.BeamAxialMin:
                return "BeamAxialMin";

            case PrivateTag.NumDisplaySamples:
                return "NumDisplaySamples";

            case PrivateTag.DVolumeWidth:
                return "DVolumeWidth";

            case PrivateTag.DVolumeDepth:
                return "DVolumeDepth";

            case PrivateTag.DVolumeHeight:
                return "DVolumeHeight";

            case PrivateTag.DVolumePosX:
                return "DVolumePosX";

            case PrivateTag.DVolumePosY:
                return "DVolumePosY";

            case PrivateTag.DVolumePosZ:
                return "DVolumePosZ";

            case PrivateTag.DBeamAxialMin:
                return "DBeamAxialMin";

            case PrivateTag.DBeamAxialSpan:
                return "DBeamAxialSpan";

            case PrivateTag.DBeamLateralSpan:
                return "DBeamLateralSpan";

            case PrivateTag.NumOfVolumesInSequence:
                return "NumOfVolumesInSequence";

            case PrivateTag.DByteMaskOffset:
                return "DByteMaskOffset";

            case PrivateTag.DByteMaskSize:
                return "DByteMaskSize";

            case PrivateTag.PrivateCreatorVersionOfBookmark:
                return "PrivateCreatorVersionOfBookmark";

            case PrivateTag.BCutPlaneEnable:
                return "BCutPlaneEnable";

            case PrivateTag.BMprColorMapIndex:
                return "BMprColorMapIndex";

            case PrivateTag.BMprDynamicRangeDb:
                return "BMprDynamicRangeDb";

            case PrivateTag.BMprGrayMapIndex:
                return "BMprGrayMapIndex";

            case PrivateTag.BVolumeRenderMode:
                return "BVolumeRenderMode";

            case PrivateTag.BVrBrightness:
                return "BVrBrightness";

            case PrivateTag.BVrContrast:
                return "BVrContrast";

            case PrivateTag.BVrColorMapIndex:
                return "BVrColorMapIndex";

            case PrivateTag.BVrDynamicRangeDb:
                return "BVrDynamicRangeDb";

            case PrivateTag.BVrGrayMapIndex:
                return "BVrGrayMapIndex";

            case PrivateTag.BVrThresholdHigh:
                return "BVrThresholdHigh";

            case PrivateTag.BVrThresholdLow:
                return "BVrThresholdLow";

            case PrivateTag.BPreProcessFilterMix:
                return "BPreProcessFilterMix";

            case PrivateTag.CCutPlaneEnable:
                return "CCutPlaneEnable";

            case PrivateTag.CFrontClipMode:
                return "CFrontClipMode";

            case PrivateTag.CMprColorMapIndex:
                return "CMprColorMapIndex";

            case PrivateTag.CMprColorFlowPriorityIndex:
                return "CMprColorFlowPriorityIndex";

            case PrivateTag.CVolumeRenderMode:
                return "CVolumeRenderMode";

            case PrivateTag.CVrColorMapIndex:
                return "CVrColorMapIndex";

            case PrivateTag.CVrColorFlowPriorityIndex:
                return "CVrColorFlowPriorityIndex";

            case PrivateTag.CVrOpacity:
                return "CVrOpacity";

            case PrivateTag.CVrThresholdHigh:
                return "CVrThresholdHigh";

            case PrivateTag.CVrThresholdLow:
                return "CVrThresholdLow";

            case PrivateTag.VoiMode:
                return "VoiMode";

            case PrivateTag.VoiRotationOffsetDeg:
                return "VoiRotationOffsetDeg";

            case PrivateTag.VoiSizeRatioX:
                return "VoiSizeRatioX";

            case PrivateTag.VoiSizeRatioY:
                return "VoiSizeRatioY";

            case PrivateTag.VoiSizeRatioZ:
                return "VoiSizeRatioZ";

            case PrivateTag.VoiSyncPlane:
                return "VoiSyncPlane";

            case PrivateTag.VoiViewMode:
                return "VoiViewMode";

            case PrivateTag.VrOrientationA:
                return "VrOrientationA";

            case PrivateTag.MprOrientationA:
                return "MprOrientationA";

            case PrivateTag.VrOffsetVector:
                return "VrOffsetVector";

            case PrivateTag.BlendingRatio:
                return "BlendingRatio";

            case PrivateTag.FusionBlendMode:
                return "FusionBlendMode";

            case PrivateTag.QualityFactor:
                return "QualityFactor";

            case PrivateTag.RendererType:
                return "RendererType";

            case PrivateTag.SliceMode:
                return "SliceMode";

            case PrivateTag.ActiveQuad:
                return "ActiveQuad";

            case PrivateTag.ScreenMode:
                return "ScreenMode";

            case PrivateTag.CutPlaneSide:
                return "CutPlaneSide";

            case PrivateTag.WireframeMode:
                return "WireframeMode";

            case PrivateTag.CrossmarkMode:
                return "CrossmarkMode";

            case PrivateTag.MprDisplayType:
                return "MprDisplayType";

            case PrivateTag.VolumeDisplayType:
                return "VolumeDisplayType";

            case PrivateTag.LastReset:
                return "LastReset";

            case PrivateTag.LastNonFullScreenMode:
                return "LastNonFullScreenMode";

            case PrivateTag.MprToolIndex:
                return "MprToolIndex";

            case PrivateTag.VoiToolIndex:
                return "VoiToolIndex";

            case PrivateTag.ToolLoopMode:
                return "ToolLoopMode";

            case PrivateTag.VolumeArbMode:
                return "VolumeArbMode";

            case PrivateTag.MprZoomEn:
                return "MprZoomEn";

            case PrivateTag.IsVolumeZoomEn:
                return "IsVolumeZoomEn";

            case PrivateTag.ZoomLevelMpr:
                return "ZoomLevelMpr";

            case PrivateTag.ZoomLevelVolume:
                return "ZoomLevelVolume";

            case PrivateTag.IsAutoRotateEn:
                return "IsAutoRotateEn";

            case PrivateTag.AutoRotateAxis:
                return "AutoRotateAxis";

            case PrivateTag.AutoRotateRangeIndex:
                return "AutoRotateRangeIndex";

            case PrivateTag.AutoRotateSpeedIndex:
                return "AutoRotateSpeedIndex";

            case PrivateTag.CVrBrightness:
                return "CVrBrightness";

            case PrivateTag.CFlowStateIndex:
                return "CFlowStateIndex";

            case PrivateTag.BSubmodeIndex:
                return "BSubmodeIndex";

            case PrivateTag.CSubmodeIndex:
                return "CSubmodeIndex";

            case PrivateTag.CutPlane:
                return "CutPlane";

            case PrivateTag.BookmarkChunkId:
                return "BookmarkChunkId";

            case PrivateTag.SequenceMinChunkId:
                return "SequenceMinChunkId";

            case PrivateTag.SequenceMaxChunkId:
                return "SequenceMaxChunkId";

            case PrivateTag.VolumeRateHz:
                return "VolumeRateHz";

            case PrivateTag.VoiPositionOffsetX:
                return "VoiPositionOffsetX";

            case PrivateTag.VoiPositionOffsetY:
                return "VoiPositionOffsetY";

            case PrivateTag.VoiPositionOffsetZ:
                return "VoiPositionOffsetZ";

            case PrivateTag.VrToolIndex:
                return "VrToolIndex";

            case PrivateTag.ShadingPercent:
                return "ShadingPercent";

            case PrivateTag.VolumeType:
                return "VolumeType";

            case PrivateTag.VrQuadDisplayType:
                return "VrQuadDisplayType";

            case PrivateTag.MprCenterLocation:
                return "MprCenterLocation";

            case PrivateTag.SliceRangeType:
                return "SliceRangeType";

            case PrivateTag.SliceMPRPlane:
                return "SliceMPRPlane";

            case PrivateTag.SliceLayout:
                return "SliceLayout";

            case PrivateTag.SliceSpacing:
                return "SliceSpacing";

            case PrivateTag.ThinVrMode:
                return "ThinVrMode";

            case PrivateTag.ThinVrThickness:
                return "ThinVrThickness";

            case PrivateTag.VoiPivotX:
                return "VoiPivotX";

            case PrivateTag.VoiPivotY:
                return "VoiPivotY";

            case PrivateTag.VoiPivotZ:
                return "VoiPivotZ";

            case PrivateTag.CTopVoiQuad:
                return "CTopVoiQuad";

            case PrivateTag._0039_xxEA_:
                return "_0039_xxEA_";

            case PrivateTag._0039_xxED_:
                return "_0039_xxED_";

            case PrivateTag._0039_xxEE_:
                return "_0039_xxEE_";

            case PrivateTag._0039_xxEF_:
                return "_0039_xxEF_";

            case PrivateTag._0039_xxF0_:
                return "_0039_xxF0_";

            case PrivateTag._0039_xxF1_:
                return "_0039_xxF1_";

            case PrivateTag._0039_xxF2_:
                return "_0039_xxF2_";

            case PrivateTag._0039_xxF3_:
                return "_0039_xxF3_";

            case PrivateTag._0039_xxF4_:
                return "_0039_xxF4_";

            case PrivateTag._0039_xxF5_:
                return "_0039_xxF5_";

            case PrivateTag._0039_xxF6_:
                return "_0039_xxF6_";
        }
        return "";
    }

}
