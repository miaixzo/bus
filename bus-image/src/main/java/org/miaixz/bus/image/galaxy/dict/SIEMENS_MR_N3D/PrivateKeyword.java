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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.BackgroundColorDRSequence:
                return "BackgroundColorDRSequence";

            case PrivateTag.BackgroundColor:
                return "BackgroundColor";

            case PrivateTag.FieldMapDRSequence:
                return "FieldMapDRSequence";

            case PrivateTag.Visible:
                return "Visible";

            case PrivateTag.TintingColor:
                return "TintingColor";

            case PrivateTag.TintingEnabled:
                return "TintingEnabled";

            case PrivateTag.VolumeID:
                return "VolumeID";

            case PrivateTag.VolumeIDAsBound:
                return "VolumeIDAsBound";

            case PrivateTag.FloatingMPRColorLUTDRSequence:
                return "FloatingMPRColorLUTDRSequence";

            case PrivateTag.RGBALUT:
                return "RGBALUT";

            case PrivateTag.BlendFactor:
                return "BlendFactor";

            case PrivateTag.RGBALUTDataSequence:
                return "RGBALUTDataSequence";

            case PrivateTag.ColorLUT:
                return "ColorLUT";

            case PrivateTag.OrthoMPRColorLUTDRSequence:
                return "OrthoMPRColorLUTDRSequence";

            case PrivateTag.VRTColorLUTDRSequence:
                return "VRTColorLUTDRSequence";

            case PrivateTag.PwlTransferFunctionDataSequence:
                return "PwlTransferFunctionDataSequence";

            case PrivateTag.PwlTransferFunctionSequence:
                return "PwlTransferFunctionSequence";

            case PrivateTag.PwlVertexIndex:
                return "PwlVertexIndex";

            case PrivateTag.PwlVertexSequence:
                return "PwlVertexSequence";

            case PrivateTag.FloatingMPRRenderDRSequence:
                return "FloatingMPRRenderDRSequence";

            case PrivateTag.PrimaryShowHide:
                return "PrimaryShowHide";

            case PrivateTag.AlphaDependentFieldmap:
                return "AlphaDependentFieldmap";

            case PrivateTag.VolumeFilter:
                return "VolumeFilter";

            case PrivateTag.OrthoMPRRenderDRSequence:
                return "OrthoMPRRenderDRSequence";

            case PrivateTag.VRTRenderDRSequence:
                return "VRTRenderDRSequence";

            case PrivateTag.SecondaryShowHide:
                return "SecondaryShowHide";

            case PrivateTag.PrimaryShadingIndex:
                return "PrimaryShadingIndex";

            case PrivateTag.SecondaryShadingIndex:
                return "SecondaryShadingIndex";

            case PrivateTag.BoundingBoxColor:
                return "BoundingBoxColor";

            case PrivateTag.SceneInteractionOn:
                return "SceneInteractionOn";

            case PrivateTag.ClipPlaneDRSequence:
                return "ClipPlaneDRSequence";

            case PrivateTag.PlaneCenter:
                return "PlaneCenter";

            case PrivateTag.PlaneNormal:
                return "PlaneNormal";

            case PrivateTag.PlaneScale:
                return "PlaneScale";

            case PrivateTag.PlaneEnableGLClip:
                return "PlaneEnableGLClip";

            case PrivateTag.PlaneHandleRatio:
                return "PlaneHandleRatio";

            case PrivateTag.PlaneBoundingPoints:
                return "PlaneBoundingPoints";

            case PrivateTag.PlaneMotionMatrix:
                return "PlaneMotionMatrix";

            case PrivateTag.PlaneShiftVelocity:
                return "PlaneShiftVelocity";

            case PrivateTag.PlaneEnabled:
                return "PlaneEnabled";

            case PrivateTag.PlaneRotateVelocity:
                return "PlaneRotateVelocity";

            case PrivateTag.PlaneShowGraphics:
                return "PlaneShowGraphics";

            case PrivateTag.PlaneSingleSelectionMode:
                return "PlaneSingleSelectionMode";

            case PrivateTag.PlaneAlignment:
                return "PlaneAlignment";

            case PrivateTag.PlaneSelected:
                return "PlaneSelected";

            case PrivateTag.SplitPlaneDRSequence:
                return "SplitPlaneDRSequence";

            case PrivateTag.FloatingMPRDRSequence:
                return "FloatingMPRDRSequence";

            case PrivateTag.PlaneMPRLocked:
                return "PlaneMPRLocked";

            case PrivateTag.PlaneScalingDisabled:
                return "PlaneScalingDisabled";

            case PrivateTag.OrthoMPRDRSequence:
                return "OrthoMPRDRSequence";

            case PrivateTag.Offset:
                return "Offset";

            case PrivateTag.OrthoMPRAtBoundingBox:
                return "OrthoMPRAtBoundingBox";

            case PrivateTag.ClusteringDRSequence:
                return "ClusteringDRSequence";

            case PrivateTag.ClusterSize:
                return "ClusterSize";

            case PrivateTag.ClusteringEnabled:
                return "ClusteringEnabled";

            case PrivateTag.ClusterMaskVolID:
                return "ClusterMaskVolID";

            case PrivateTag.HeadMaskingDRSequence:
                return "HeadMaskingDRSequence";

            case PrivateTag.MaskingRange:
                return "MaskingRange";

            case PrivateTag.MaskEnabled:
                return "MaskEnabled";

            case PrivateTag.BrainMaskingDRSequence:
                return "BrainMaskingDRSequence";

            case PrivateTag.MaskingStatusDRSequence:
                return "MaskingStatusDRSequence";

            case PrivateTag.VRTMaskingDRSequence:
                return "VRTMaskingDRSequence";

            case PrivateTag.OrthoMPRMaskingDRSequence:
                return "OrthoMPRMaskingDRSequence";

            case PrivateTag.FloatingMPRMaskingDRSequence:
                return "FloatingMPRMaskingDRSequence";

            case PrivateTag.AlignDRSequence:
                return "AlignDRSequence";

            case PrivateTag.RegistrationMatrix:
                return "RegistrationMatrix";

            case PrivateTag.FunctionalEvaluationDRSequence:
                return "FunctionalEvaluationDRSequence";

            case PrivateTag.FrameAcquitionNumbers:
                return "FrameAcquitionNumbers";

            case PrivateTag.ShowCursor:
                return "ShowCursor";

            case PrivateTag.CurrentFrame:
                return "CurrentFrame";

            case PrivateTag.PlotArea:
                return "PlotArea";

            case PrivateTag.PlotTextPosition:
                return "PlotTextPosition";

            case PrivateTag.BaseLinePoints:
                return "BaseLinePoints";

            case PrivateTag.ActivePoints:
                return "ActivePoints";

            case PrivateTag.ShowLabel:
                return "ShowLabel";

            case PrivateTag.MeanPlot:
                return "MeanPlot";

            case PrivateTag.MotionPlot:
                return "MotionPlot";

            case PrivateTag.ActivateNormallizedCurve:
                return "ActivateNormallizedCurve";

            case PrivateTag.PlotSize:
                return "PlotSize";

            case PrivateTag.PlotDRSequence:
                return "PlotDRSequence";

            case PrivateTag.Title:
                return "Title";

            case PrivateTag.AutoScale:
                return "AutoScale";

            case PrivateTag.FixedScale:
                return "FixedScale";

            case PrivateTag.LabelX:
                return "LabelX";

            case PrivateTag.LabelY:
                return "LabelY";

            case PrivateTag.Legend:
                return "Legend";

            case PrivateTag.ScrollBarX:
                return "ScrollBarX";

            case PrivateTag.ScrollBarY:
                return "ScrollBarY";

            case PrivateTag.ConnectScrollX:
                return "ConnectScrollX";

            case PrivateTag.PlotID:
                return "PlotID";

            case PrivateTag.PlotPosition:
                return "PlotPosition";

            case PrivateTag.CurveDRSequence:
                return "CurveDRSequence";

            case PrivateTag.CurveID:
                return "CurveID";

            case PrivateTag.PlotType:
                return "PlotType";

            case PrivateTag.CurveValues:
                return "CurveValues";

            case PrivateTag.LineColor:
                return "LineColor";

            case PrivateTag.MarkerColor:
                return "MarkerColor";

            case PrivateTag.LineFilled:
                return "LineFilled";

            case PrivateTag.Label:
                return "Label";

            case PrivateTag.ShowMarker:
                return "ShowMarker";

            case PrivateTag.MarkerShape:
                return "MarkerShape";

            case PrivateTag.SmoothingAlgo:
                return "SmoothingAlgo";

            case PrivateTag.MarkerSize:
                return "MarkerSize";

            case PrivateTag.LineStyle:
                return "LineStyle";

            case PrivateTag.LinePattern:
                return "LinePattern";

            case PrivateTag.LineWidth:
                return "LineWidth";

            case PrivateTag.VRTFilterDRSequence:
                return "VRTFilterDRSequence";

            case PrivateTag.FilterType:
                return "FilterType";

            case PrivateTag.CurrentActivePlane:
                return "CurrentActivePlane";
        }
        return "";
    }

}
