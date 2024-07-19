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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MED_SMS_USG_ANTARES_3D_VOLUME;

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
        
            case PrivateTag.VoiSizeRatioX:
            case PrivateTag.VoiSizeRatioY:
            case PrivateTag.VoiSizeRatioZ:
            case PrivateTag.VrOrientationA:
            case PrivateTag.MprOrientationA:
            case PrivateTag.VrOffsetVector:
            case PrivateTag.BlendingRatio:
            case PrivateTag.QualityFactor:
            case PrivateTag.VolumeRateHz:
            case PrivateTag.VoiPositionOffsetX:
            case PrivateTag.VoiPositionOffsetY:
            case PrivateTag.VoiPositionOffsetZ:
            case PrivateTag.MprCenterLocation:
            case PrivateTag.SliceSpacing:
            case PrivateTag.VoiPivotX:
            case PrivateTag.VoiPivotY:
            case PrivateTag.VoiPivotZ:
            case PrivateTag._0039_xxF4_:
                return VR.FD;
            case PrivateTag.PrivateCreatorVersionOfBookmark:
            case PrivateTag._0039_xxF5_:
                return VR.LO;
            case PrivateTag._0039_xxF6_:
                return VR.LT;
            case PrivateTag.ZoomLevelMpr:
            case PrivateTag.ZoomLevelVolume:
                return VR.SS;
            case PrivateTag.BCutPlaneEnable:
            case PrivateTag.BMprColorMapIndex:
            case PrivateTag.BMprDynamicRangeDb:
            case PrivateTag.BMprGrayMapIndex:
            case PrivateTag.BVolumeRenderMode:
            case PrivateTag.BVrBrightness:
            case PrivateTag.BVrContrast:
            case PrivateTag.BVrColorMapIndex:
            case PrivateTag.BVrDynamicRangeDb:
            case PrivateTag.BVrGrayMapIndex:
            case PrivateTag.BVrThresholdHigh:
            case PrivateTag.BVrThresholdLow:
            case PrivateTag.BPreProcessFilterMix:
            case PrivateTag.CCutPlaneEnable:
            case PrivateTag.CFrontClipMode:
            case PrivateTag.CMprColorMapIndex:
            case PrivateTag.CMprColorFlowPriorityIndex:
            case PrivateTag.CVolumeRenderMode:
            case PrivateTag.CVrColorMapIndex:
            case PrivateTag.CVrColorFlowPriorityIndex:
            case PrivateTag.CVrOpacity:
            case PrivateTag.CVrThresholdHigh:
            case PrivateTag.CVrThresholdLow:
            case PrivateTag.VoiMode:
            case PrivateTag.VoiRotationOffsetDeg:
            case PrivateTag.VoiSyncPlane:
            case PrivateTag.VoiViewMode:
            case PrivateTag.FusionBlendMode:
            case PrivateTag.RendererType:
            case PrivateTag.SliceMode:
            case PrivateTag.ActiveQuad:
            case PrivateTag.ScreenMode:
            case PrivateTag.CutPlaneSide:
            case PrivateTag.WireframeMode:
            case PrivateTag.CrossmarkMode:
            case PrivateTag.MprDisplayType:
            case PrivateTag.VolumeDisplayType:
            case PrivateTag.LastReset:
            case PrivateTag.LastNonFullScreenMode:
            case PrivateTag.MprToolIndex:
            case PrivateTag.VoiToolIndex:
            case PrivateTag.ToolLoopMode:
            case PrivateTag.VolumeArbMode:
            case PrivateTag.MprZoomEn:
            case PrivateTag.IsVolumeZoomEn:
            case PrivateTag.IsAutoRotateEn:
            case PrivateTag.AutoRotateAxis:
            case PrivateTag.AutoRotateRangeIndex:
            case PrivateTag.AutoRotateSpeedIndex:
            case PrivateTag.CVrBrightness:
            case PrivateTag.CFlowStateIndex:
            case PrivateTag.BSubmodeIndex:
            case PrivateTag.CSubmodeIndex:
            case PrivateTag.CutPlane:
            case PrivateTag.BookmarkChunkId:
            case PrivateTag.SequenceMinChunkId:
            case PrivateTag.SequenceMaxChunkId:
            case PrivateTag.VrToolIndex:
            case PrivateTag.ShadingPercent:
            case PrivateTag.VolumeType:
            case PrivateTag.VrQuadDisplayType:
            case PrivateTag.SliceRangeType:
            case PrivateTag.SliceMPRPlane:
            case PrivateTag.SliceLayout:
            case PrivateTag.ThinVrMode:
            case PrivateTag.ThinVrThickness:
            case PrivateTag.CTopVoiQuad:
            case PrivateTag._0039_xxEA_:
            case PrivateTag._0039_xxED_:
            case PrivateTag._0039_xxEE_:
            case PrivateTag._0039_xxEF_:
            case PrivateTag._0039_xxF0_:
            case PrivateTag._0039_xxF1_:
            case PrivateTag._0039_xxF2_:
            case PrivateTag._0039_xxF3_:
                return VR.US;
        }
        return VR.UN;
    }
}
