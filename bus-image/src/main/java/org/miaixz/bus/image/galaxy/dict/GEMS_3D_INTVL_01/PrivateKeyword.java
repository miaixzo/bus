/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.galaxy.dict.GEMS_3D_INTVL_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
            case PrivateTag.XRayMarkerSequence:
                return "XRayMarkerSequence";

            case PrivateTag.MarkerID:
                return "MarkerID";

            case PrivateTag.MarkerType:
                return "MarkerType";

            case PrivateTag.MarkerSize:
                return "MarkerSize";

            case PrivateTag.MarkerColorCIELabValue:
                return "MarkerColorCIELabValue";

            case PrivateTag.MarkerLabel:
                return "MarkerLabel";

            case PrivateTag.MarkerVisibleState:
                return "MarkerVisibleState";

            case PrivateTag.MarkerDescription:
                return "MarkerDescription";

            case PrivateTag.MarkerPointsSequence:
                return "MarkerPointsSequence";

            case PrivateTag.MarkerPointID:
                return "MarkerPointID";

            case PrivateTag.MarkerPointPosition:
                return "MarkerPointPosition";

            case PrivateTag.MarkerPointSize:
                return "MarkerPointSize";

            case PrivateTag.MarkerPointColorCIELabValue:
                return "MarkerPointColorCIELabValue";

            case PrivateTag.MarkerPointVisibleState:
                return "MarkerPointVisibleState";

            case PrivateTag.MarkerPointOrder:
                return "MarkerPointOrder";

            case PrivateTag.VolumeManualRegistration:
                return "VolumeManualRegistration";

            case PrivateTag.VolumesThreshold:
                return "VolumesThreshold";

            case PrivateTag.CutPlaneActivationFlag:
                return "CutPlaneActivationFlag";

            case PrivateTag.CutPlanePositionValue:
                return "CutPlanePositionValue";

            case PrivateTag.CutPlaneNormalValue:
                return "CutPlaneNormalValue";

            case PrivateTag.VolumeScalingFactor:
                return "VolumeScalingFactor";

            case PrivateTag.ROIToTableTopDistance:
                return "ROIToTableTopDistance";

            case PrivateTag.DRRThreshold:
                return "DRRThreshold";

            case PrivateTag.VolumeTablePosition:
                return "VolumeTablePosition";

            case PrivateTag.RenderingMode:
                return "RenderingMode";

            case PrivateTag.ThreeDObjectOpacity:
                return "ThreeDObjectOpacity";

            case PrivateTag.InvertImage:
                return "InvertImage";

            case PrivateTag.EnhanceFull:
                return "EnhanceFull";

            case PrivateTag.Zoom:
                return "Zoom";

            case PrivateTag.Roam:
                return "Roam";

            case PrivateTag.WindowLevel:
                return "WindowLevel";

            case PrivateTag.WindowWidth:
                return "WindowWidth";

            case PrivateTag.BMCSetting:
                return "BMCSetting";

            case PrivateTag.BackViewSetting:
                return "BackViewSetting";

            case PrivateTag.SubVolumeVisibility:
                return "SubVolumeVisibility";

            case PrivateTag.ThreeDLandmarksVisibility:
                return "ThreeDLandmarksVisibility";

            case PrivateTag.AblationPointVisibility:
                return "AblationPointVisibility";
        }
        return "";
    }

}
