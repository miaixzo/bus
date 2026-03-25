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
package org.miaixz.bus.image.galaxy.dict.GEMS_3D_INTVL_01;

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

            case PrivateTag.MarkerType:
            case PrivateTag.MarkerVisibleState:
            case PrivateTag.MarkerPointVisibleState:
            case PrivateTag.CutPlaneActivationFlag:
            case PrivateTag.BMCSetting:
            case PrivateTag.BackViewSetting:
            case PrivateTag.SubVolumeVisibility:
            case PrivateTag.ThreeDLandmarksVisibility:
            case PrivateTag.AblationPointVisibility:
                return VR.CS;

            case PrivateTag.MarkerSize:
            case PrivateTag.MarkerPointPosition:
            case PrivateTag.MarkerPointSize:
            case PrivateTag.VolumeManualRegistration:
            case PrivateTag.CutPlaneNormalValue:
            case PrivateTag.VolumeScalingFactor:
            case PrivateTag.ROIToTableTopDistance:
            case PrivateTag.VolumeTablePosition:
            case PrivateTag.Zoom:
                return VR.FL;

            case PrivateTag.MarkerPointOrder:
            case PrivateTag.VolumesThreshold:
            case PrivateTag.CutPlanePositionValue:
            case PrivateTag.DRRThreshold:
            case PrivateTag.RenderingMode:
            case PrivateTag.ThreeDObjectOpacity:
            case PrivateTag.InvertImage:
            case PrivateTag.EnhanceFull:
            case PrivateTag.Roam:
            case PrivateTag.WindowLevel:
            case PrivateTag.WindowWidth:
                return VR.IS;

            case PrivateTag.MarkerLabel:
            case PrivateTag.MarkerDescription:
                return VR.LO;

            case PrivateTag.MarkerID:
            case PrivateTag.MarkerPointID:
                return VR.SH;

            case PrivateTag.XRayMarkerSequence:
            case PrivateTag.MarkerPointsSequence:
                return VR.SQ;

            case PrivateTag.MarkerColorCIELabValue:
            case PrivateTag.MarkerPointColorCIELabValue:
                return VR.US;
        }
        return VR.UN;
    }

}
