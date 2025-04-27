/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_SYNGO_ADVANCED_PRESENTATION;

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

        case PrivateTag.PresentationName:
        case PrivateTag.PresentationType:
        case PrivateTag.NavigationName:
        case PrivateTag.AutoNavigationDirection:
        case PrivateTag.AutoNavigationMode:
        case PrivateTag.AutoNavigationStrategy:
        case PrivateTag.AutoNavigationRealtimeFlag:
        case PrivateTag.VolumeNavigationScrollUnit:
        case PrivateTag.MeasurementGraphicIsVisibleFlag:
        case PrivateTag.MeasurementEvaluationStringValue:
        case PrivateTag.MeasurementLineShowCenterFlag:
        case PrivateTag.MeasurementAngleShowArrowTipFlag:
        case PrivateTag.RendererVertexIsCharacteristicFlag:
        case PrivateTag.RendererThicknessUsageFlag:
        case PrivateTag.RendererDirectionalLightTwoSideUsageFlag:
        case PrivateTag.RendererIsCameraRequiredFlag:
        case PrivateTag.RendererDoDepthTestFlag:
        case PrivateTag.RendererDirectionalLightUsageFlag:
        case PrivateTag.SegmentationDisplayIsSelectedFlag:
        case PrivateTag.ECGGraphicType:
        case PrivateTag.CameraRotationCenterUsageFlag:
        case PrivateTag.CameraRotationAxisUsageFlag:
        case PrivateTag.MeasurementEvaluationDataRoleItem:
        case PrivateTag.MeasurementEvaluationID:
        case PrivateTag.FusionLUTIsActiveFlag:
        case PrivateTag.LUTInvertedFlag:
        case PrivateTag.MeasurementLinkEvaluationTextFlag:
        case PrivateTag.TaskDataType:
        case PrivateTag.ClipPlaneUseThicknessFlag:
        case PrivateTag.ClipPlaneEnableClip:
        case PrivateTag.ClipPlaneEnabledFlag:
        case PrivateTag.ClipPlaneShowGraphicsFlag:
        case PrivateTag.CropBoxEnabledFlag:
        case PrivateTag.CropBoxShowGraphicsFlag:
        case PrivateTag.CurvedCameraPointOfInterestUsageFlag:
        case PrivateTag.CurvedCameraRotationAxisMode:
        case PrivateTag.MeasurementIsCircleFlag:
            return VR.CS;
        case PrivateTag.AutoNavigationFrameRate:
        case PrivateTag.AutoNavigationRealtimeSpeed:
        case PrivateTag.VolumeNavigationMinimumPixelSpacing:
        case PrivateTag.VolumeNavigationStepSize:
        case PrivateTag.VolumeNavigationJumpSize:
        case PrivateTag.ChannelAlphaValue:
        case PrivateTag.CameraZoom:
        case PrivateTag.CameraPosition:
        case PrivateTag.CameraOrientation:
        case PrivateTag.CameraFarClipPlane:
        case PrivateTag.CameraNearClipPlane:
        case PrivateTag.CameraThickness:
        case PrivateTag.CameraViewPortSize:
        case PrivateTag.CameraAspectRatio:
        case PrivateTag.CameraFieldOfView:
        case PrivateTag.CameraImagePlaneDistance:
        case PrivateTag.CameraImageMaximumHeight:
        case PrivateTag.CameraImageMinimumHeight:
        case PrivateTag.ParallelShiftIntervalMM:
        case PrivateTag.ParallelShiftBoundingBoxMinimum:
        case PrivateTag.ParallelShiftBoundingBoxMaximum:
        case PrivateTag.RendererThreshold:
        case PrivateTag.RendererMaterial:
        case PrivateTag.RendererDirectionalLightColor:
        case PrivateTag.RendererDirectionalLightDirection:
        case PrivateTag.RendererPWLVertexColor:
        case PrivateTag.RendererSamplingDistance:
        case PrivateTag.RendererInitialSamplingDistance:
        case PrivateTag.SegmentationDisplayColor:
        case PrivateTag.SegmentationVolumeSize:
        case PrivateTag.SegmentationVolumeStorageDataType:
        case PrivateTag.CameraRotationAxis:
        case PrivateTag.CameraRotationCenter:
        case PrivateTag.CameraParallelEpiped:
        case PrivateTag.CameraMaxZoomInFactor:
        case PrivateTag.CameraMinZoomInFactor:
        case PrivateTag.MeasurementSurfaceNormal:
        case PrivateTag.MeasurementEvaluationValue:
        case PrivateTag.MeasurementEvaluationLongestDistance:
        case PrivateTag.MeasurementEvaluationCentroid:
        case PrivateTag.ImageRotationFractional:
        case PrivateTag.MeasurementEvaluationTextPositionVector:
        case PrivateTag.ClipPlaneCenter:
        case PrivateTag.ClipPlaneNormal:
        case PrivateTag.ClipPlaneScale:
        case PrivateTag.ClipPlaneThickness:
        case PrivateTag.ClipPlaneHandleRatio:
        case PrivateTag.ClipPlaneBoundingPoints:
        case PrivateTag.ClipPlaneMotionMatrix:
        case PrivateTag.ClipPlaneShiftVelocity:
        case PrivateTag.ClipPlaneRotateVelocity:
        case PrivateTag.CropBoxSize:
        case PrivateTag.CropBoxAbsoluteOrigin:
        case PrivateTag.CurvedCameraCoordinates:
        case PrivateTag.CurvedCameraPointOfInterest:
        case PrivateTag.CurvedCameraThickness:
        case PrivateTag.CurvedCameraExtrusionLength:
        case PrivateTag.CurvedCameraRollRotationAxis:
        case PrivateTag.CurvedCameraViewPortHeight:
        case PrivateTag.CurvedCameraCutDirection:
        case PrivateTag.CurvedCameraPanVector:
            return VR.DS;
        case PrivateTag.MeasurementEvaluationDecimalValue:
        case PrivateTag.SegmentationVolumeModelMatrix:
        case PrivateTag.MeasurementEllipsoidModelMatrix:
        case PrivateTag.MeasurementDataPoints:
        case PrivateTag.MeasurementDataAngles:
        case PrivateTag.MeasurementDataSliceThickness:
        case PrivateTag.MeasurementDataBoundingBox:
        case PrivateTag.MeasurementAlpha:
            return VR.FL;
        case PrivateTag.DisplayedAreaBottomRightHandCornerFractional:
        case PrivateTag.DisplayedAreaTopLeftHandCornerFractional:
            return VR.FD;
        case PrivateTag.IndexNavigationCurrentIndex:
        case PrivateTag.IndexAutoNavigationSkippingDegree:
        case PrivateTag.ReferencedRegistrationNumber:
        case PrivateTag.MeasurementApplicationNumber:
        case PrivateTag.MeasurementEvaluationIntegerValue:
        case PrivateTag.RendererPWLVertexIndex:
        case PrivateTag.MeasurementDiameter:
        case PrivateTag.SoftcopyVOILUTViewingIndex:
        case PrivateTag.TaskDataSize:
            return VR.IS;
        case PrivateTag.MeasurementApplicationName:
        case PrivateTag.MeasurementType:
        case PrivateTag.CameraProjectionType:
        case PrivateTag.SegmentationDisplayParameterType:
        case PrivateTag.SegmentationDisplayVisibility:
        case PrivateTag.SegmentationVersionIdentifier:
        case PrivateTag.RegistrationCreationAlgorithmName:
        case PrivateTag.PresentationStateGroupIdentifier:
        case PrivateTag.MeasurementEvaluationDataRoleID:
        case PrivateTag.MeasurementAlgorithmType:
        case PrivateTag.MeasurementDataSlice:
        case PrivateTag.MeasurementText:
        case PrivateTag.PresetName:
        case PrivateTag.PresentationModuleType:
        case PrivateTag.ImageTextViewName:
        case PrivateTag.ImageTextLineNames:
        case PrivateTag.ImageTextLineValues:
        case PrivateTag._0029_xxB5_:
        case PrivateTag.TaskDataVersion:
        case PrivateTag.ClinicalFindingID:
        case PrivateTag.MeasurementApplicationTypeID:
            return VR.LO;
        case PrivateTag.SegmentationAdditionalInformationBlob:
        case PrivateTag.MeasurementBitmap:
        case PrivateTag.ImageTextAlphaBlendingLineValue:
        case PrivateTag.TaskData:
            return VR.OB;
        case PrivateTag.OverlayHiddenDisplayAttributes:
            return VR.SL;
        case PrivateTag.AdvancedPresentationSequence:
        case PrivateTag.TimePointSequence:
        case PrivateTag.BaseImageSequence:
        case PrivateTag.OverlayImageSequence:
        case PrivateTag.RegistrationInstanceSequence:
        case PrivateTag.RealWorldValueMappingInstanceSequence:
        case PrivateTag.MeasurementSequence:
        case PrivateTag.SegmentationSequence:
        case PrivateTag.NavigationSequence:
        case PrivateTag.MeasurementDataSequence:
        case PrivateTag.CameraHomeSettingsSequence:
        case PrivateTag.RendererPWLTransferFunctionSequence:
        case PrivateTag.RendererThicknessSequence:
        case PrivateTag.RendererSliceSpacingSequence:
        case PrivateTag.SegmentationDisplayDataSequence:
        case PrivateTag.SegmentationDisplayParameterSequence:
        case PrivateTag.MeasurementEvaluationDataRoleSequence:
        case PrivateTag.MeasurementEvaluationSequence:
        case PrivateTag.MeasurementReferencedFramesSequence:
        case PrivateTag.FusionLUTSequence:
        case PrivateTag.PresentationModuleSequence:
        case PrivateTag.PresentationStateSequence:
        case PrivateTag.ImageTextViewContentSequence:
        case PrivateTag.TaskDataSequence:
        case PrivateTag.ClipPlaneSequence:
        case PrivateTag.ImageSequence:
            return VR.SQ;
        case PrivateTag.MeasurementApplicationNumberPrefixText:
        case PrivateTag.HashCodeValue:
        case PrivateTag.TaskDataDescription:
            return VR.ST;
        case PrivateTag.MeasurementUID:
        case PrivateTag.SegmentationUID:
        case PrivateTag.RealWorldValueMappingUID:
        case PrivateTag.MeasurementFrameOfReferenceUID:
        case PrivateTag.MeasurementUid:
        case PrivateTag.ReferencedSyngoUID:
        case PrivateTag.ClinicalFindingUID:
        case PrivateTag.SegmentationDisplayDataUID:
        case PrivateTag.RegistrationReferencedFrames:
        case PrivateTag.RegistrationReferencedRegistrations:
        case PrivateTag.SyngoUID:
        case PrivateTag.PresentationVersionIdentifier:
            return VR.UI;
        case PrivateTag.TemporarySmallestImagePixelValue:
        case PrivateTag.TemporaryLargestImagePixelValue:
        case PrivateTag.CurrentFrameNumber:
            return VR.US;
        }
        return VR.UN;
    }
}
