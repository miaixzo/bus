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
