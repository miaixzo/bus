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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SMS_USG_S2000_3D_VOLUME;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {
    
        switch (tag & 0xFFFF00FF) {
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
