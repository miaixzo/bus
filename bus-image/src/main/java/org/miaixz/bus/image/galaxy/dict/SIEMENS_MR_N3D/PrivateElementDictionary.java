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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_MR_N3D;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 21+
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

            case PrivateTag.Visible:
            case PrivateTag.TintingEnabled:
            case PrivateTag.PrimaryShowHide:
            case PrivateTag.AlphaDependentFieldmap:
            case PrivateTag.SecondaryShowHide:
            case PrivateTag.SceneInteractionOn:
            case PrivateTag.PlaneEnableGLClip:
            case PrivateTag.PlaneEnabled:
            case PrivateTag.PlaneShowGraphics:
            case PrivateTag.PlaneSingleSelectionMode:
            case PrivateTag.PlaneAlignment:
            case PrivateTag.PlaneSelected:
            case PrivateTag.PlaneMPRLocked:
            case PrivateTag.PlaneScalingDisabled:
            case PrivateTag.OrthoMPRAtBoundingBox:
            case PrivateTag.ClusteringEnabled:
            case PrivateTag.MaskEnabled:
            case PrivateTag.ShowCursor:
            case PrivateTag.ShowLabel:
            case PrivateTag.MeanPlot:
            case PrivateTag.MotionPlot:
            case PrivateTag.ActivateNormallizedCurve:
            case PrivateTag.AutoScale:
            case PrivateTag.Legend:
            case PrivateTag.ScrollBarX:
            case PrivateTag.ScrollBarY:
            case PrivateTag.LineFilled:
            case PrivateTag.ShowMarker:
            case PrivateTag.CurrentActivePlane:
                return VR.CS;

            case PrivateTag.BackgroundColor:
            case PrivateTag.TintingColor:
            case PrivateTag.RGBALUT:
            case PrivateTag.BlendFactor:
            case PrivateTag.PwlVertexIndex:
            case PrivateTag.BoundingBoxColor:
            case PrivateTag.PlaneCenter:
            case PrivateTag.PlaneNormal:
            case PrivateTag.PlaneScale:
            case PrivateTag.PlaneHandleRatio:
            case PrivateTag.PlaneBoundingPoints:
            case PrivateTag.PlaneMotionMatrix:
            case PrivateTag.PlaneShiftVelocity:
            case PrivateTag.PlaneRotateVelocity:
            case PrivateTag.Offset:
            case PrivateTag.ClusterSize:
            case PrivateTag.MaskingRange:
            case PrivateTag.RegistrationMatrix:
            case PrivateTag.FrameAcquitionNumbers:
            case PrivateTag.CurrentFrame:
            case PrivateTag.PlotArea:
            case PrivateTag.PlotTextPosition:
            case PrivateTag.BaseLinePoints:
            case PrivateTag.ActivePoints:
            case PrivateTag.PlotSize:
            case PrivateTag.FixedScale:
            case PrivateTag.PlotPosition:
            case PrivateTag.CurveValues:
            case PrivateTag.LineColor:
            case PrivateTag.MarkerColor:
            case PrivateTag.MarkerSize:
            case PrivateTag.LineWidth:
                return VR.DS;

            case PrivateTag.VolumeID:
            case PrivateTag.VolumeIDAsBound:
            case PrivateTag.VolumeFilter:
            case PrivateTag.PrimaryShadingIndex:
            case PrivateTag.SecondaryShadingIndex:
            case PrivateTag.ClusterMaskVolID:
            case PrivateTag.Title:
            case PrivateTag.LabelX:
            case PrivateTag.LabelY:
            case PrivateTag.ConnectScrollX:
            case PrivateTag.PlotID:
            case PrivateTag.CurveID:
            case PrivateTag.PlotType:
            case PrivateTag.Label:
            case PrivateTag.MarkerShape:
            case PrivateTag.SmoothingAlgo:
            case PrivateTag.LineStyle:
            case PrivateTag.LinePattern:
            case PrivateTag.FilterType:
                return VR.LO;

            case PrivateTag.ColorLUT:
                return VR.OB;

            case PrivateTag.BackgroundColorDRSequence:
            case PrivateTag.FieldMapDRSequence:
            case PrivateTag.FloatingMPRColorLUTDRSequence:
            case PrivateTag.RGBALUTDataSequence:
            case PrivateTag.OrthoMPRColorLUTDRSequence:
            case PrivateTag.VRTColorLUTDRSequence:
            case PrivateTag.PwlTransferFunctionDataSequence:
            case PrivateTag.PwlTransferFunctionSequence:
            case PrivateTag.PwlVertexSequence:
            case PrivateTag.FloatingMPRRenderDRSequence:
            case PrivateTag.OrthoMPRRenderDRSequence:
            case PrivateTag.VRTRenderDRSequence:
            case PrivateTag.ClipPlaneDRSequence:
            case PrivateTag.SplitPlaneDRSequence:
            case PrivateTag.FloatingMPRDRSequence:
            case PrivateTag.OrthoMPRDRSequence:
            case PrivateTag.ClusteringDRSequence:
            case PrivateTag.HeadMaskingDRSequence:
            case PrivateTag.BrainMaskingDRSequence:
            case PrivateTag.MaskingStatusDRSequence:
            case PrivateTag.VRTMaskingDRSequence:
            case PrivateTag.OrthoMPRMaskingDRSequence:
            case PrivateTag.FloatingMPRMaskingDRSequence:
            case PrivateTag.AlignDRSequence:
            case PrivateTag.FunctionalEvaluationDRSequence:
            case PrivateTag.PlotDRSequence:
            case PrivateTag.CurveDRSequence:
            case PrivateTag.VRTFilterDRSequence:
                return VR.SQ;
        }
        return VR.UN;
    }

}
