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
package org.miaixz.bus.opencv.imgproc;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.MatOfFloat;
import org.miaixz.bus.opencv.core.MatOfInt;
import org.miaixz.bus.opencv.core.MatOfPoint;
import org.miaixz.bus.opencv.core.Point;
import org.miaixz.bus.opencv.core.Range;
import org.miaixz.bus.opencv.core.Rect;
import org.miaixz.bus.opencv.core.RotatedRect;
import org.miaixz.bus.opencv.core.Scalar;
import org.miaixz.bus.opencv.core.Size;
import org.miaixz.bus.opencv.core.TermCriteria;
import org.miaixz.bus.opencv.utils.Converters;

/**
 * Image filtering, color conversion, morphology, drawing, feature detection, and shape-analysis operations.
 */
public class Imgproc {

    /**
     * Creates a Java API container. The utility operations are available through static methods.
     */
    public Imgproc() {
        // No initialization required.
    }

    /**
     * Legacy border mode that fills samples outside the image with a constant value.
     */
    private static final int IPL_BORDER_CONSTANT = 0;

    /**
     * Legacy border mode that repeats the nearest boundary sample.
     */
    private static final int IPL_BORDER_REPLICATE = 1;

    /**
     * Legacy reflection border mode that repeats the boundary sample at the reflection.
     */
    private static final int IPL_BORDER_REFLECT = 2;

    /**
     * Legacy border mode that wraps coordinates to the opposite image boundary.
     */
    private static final int IPL_BORDER_WRAP = 3;

    /**
     * Legacy reflection border mode that excludes the boundary sample at the reflection.
     */
    private static final int IPL_BORDER_REFLECT_101 = 4;

    /**
     * Legacy border mode that leaves destination pixels outside the source unchanged where supported.
     */
    private static final int IPL_BORDER_TRANSPARENT = 5;

    /**
     * Choose the local threshold as the mean of the blockSize-by-blockSize neighborhood minus C.
     */
    public static final int ADAPTIVE_THRESH_MEAN_C = 0;

    /**
     * Choose the local threshold as the Gaussian-weighted sum of the blockSize-by-blockSize neighborhood minus C.
     */
    public static final int ADAPTIVE_THRESH_GAUSSIAN_C = 1;

    /**
     * F_a = 0, F_b = 0. Erases both images.
     */
    public static final int ALPHA_COMPOSITE_CLEAR = 0;

    /**
     * F_a = 1, F_b = 0. Keeps the overlay only (W3C `copy`).
     */
    public static final int ALPHA_COMPOSITE_SOURCE = 1;

    /**
     * F_a = 0, F_b = 1. Keeps the background only.
     */
    public static final int ALPHA_COMPOSITE_DEST = 2;

    /**
     * F_a = 1, F_b = 1 - alpha_s. Draws the overlay on top (W3C `source-over`).
     */
    public static final int ALPHA_COMPOSITE_OVER = 3;

    /**
     * F_a = 1 - alpha_b, F_b = 1. Draws the overlay underneath.
     */
    public static final int ALPHA_COMPOSITE_DEST_OVER = 4;

    /**
     * F_a = alpha_b, F_b = 0. Keeps the overlay where the background is opaque.
     */
    public static final int ALPHA_COMPOSITE_IN = 5;

    /**
     * F_a = 0, F_b = alpha_s. Keeps the background where the overlay is opaque.
     */
    public static final int ALPHA_COMPOSITE_DEST_IN = 6;

    /**
     * F_a = 1 - alpha_b, F_b = 0. Keeps the overlay where the background is transparent.
     */
    public static final int ALPHA_COMPOSITE_OUT = 7;

    /**
     * F_a = 0, F_b = 1 - alpha_s. Keeps the background where the overlay is transparent.
     */
    public static final int ALPHA_COMPOSITE_DEST_OUT = 8;

    /**
     * F_a = alpha_b, F_b = 1 - alpha_s. Draws the overlay on top, clipped to the background's shape.
     */
    public static final int ALPHA_COMPOSITE_ATOP = 9;

    /**
     * F_a = 1 - alpha_b, F_b = alpha_s. Draws the background on top, clipped to the overlay's shape.
     */
    public static final int ALPHA_COMPOSITE_DEST_ATOP = 10;

    /**
     * F_a = 1 - alpha_b, F_b = 1 - alpha_s. Keeps the non-overlapping parts of both.
     */
    public static final int ALPHA_COMPOSITE_XOR = 11;

    /**
     * F_a = 1, F_b = 1. Adds the two, clamped (W3C `lighter`, Cairo `OPERATOR_ADD`).
     */
    public static final int ALPHA_COMPOSITE_PLUS = 12;

    /**
     * Color-conversion code from BGR to BGRA.
     */
    public static final int COLOR_BGR2BGRA = 0;

    /**
     * Color-conversion code from RGB to RGBA.
     */
    public static final int COLOR_RGB2RGBA = COLOR_BGR2BGRA;

    /**
     * Color-conversion code from BGRA to BGR.
     */
    public static final int COLOR_BGRA2BGR = 1;

    /**
     * Color-conversion code from RGBA to RGB.
     */
    public static final int COLOR_RGBA2RGB = COLOR_BGRA2BGR;

    /**
     * Color-conversion code from BGR to RGBA.
     */
    public static final int COLOR_BGR2RGBA = 2;

    /**
     * Color-conversion code from RGB to BGRA.
     */
    public static final int COLOR_RGB2BGRA = COLOR_BGR2RGBA;

    /**
     * Color-conversion code from RGBA to BGR.
     */
    public static final int COLOR_RGBA2BGR = 3;

    /**
     * Color-conversion code from BGRA to RGB.
     */
    public static final int COLOR_BGRA2RGB = COLOR_RGBA2BGR;

    /**
     * Color-conversion code from BGR to RGB.
     */
    public static final int COLOR_BGR2RGB = 4;

    /**
     * Color-conversion code from RGB to BGR.
     */
    public static final int COLOR_RGB2BGR = COLOR_BGR2RGB;

    /**
     * Color-conversion code from BGRA to RGBA.
     */
    public static final int COLOR_BGRA2RGBA = 5;

    /**
     * Color-conversion code from RGBA to BGRA.
     */
    public static final int COLOR_RGBA2BGRA = COLOR_BGRA2RGBA;

    /**
     * Color-conversion code from BGR to GRAY.
     */
    public static final int COLOR_BGR2GRAY = 6;

    /**
     * Color-conversion code from RGB to GRAY.
     */
    public static final int COLOR_RGB2GRAY = 7;

    /**
     * Color-conversion code from GRAY to BGR.
     */
    public static final int COLOR_GRAY2BGR = 8;

    /**
     * Color-conversion code from GRAY to RGB.
     */
    public static final int COLOR_GRAY2RGB = COLOR_GRAY2BGR;

    /**
     * Color-conversion code from GRAY to BGRA.
     */
    public static final int COLOR_GRAY2BGRA = 9;

    /**
     * Color-conversion code from GRAY to RGBA.
     */
    public static final int COLOR_GRAY2RGBA = COLOR_GRAY2BGRA;

    /**
     * Color-conversion code from BGRA to GRAY.
     */
    public static final int COLOR_BGRA2GRAY = 10;

    /**
     * Color-conversion code from RGBA to GRAY.
     */
    public static final int COLOR_RGBA2GRAY = 11;

    /**
     * Color-conversion code from BGR to BGR565.
     */
    public static final int COLOR_BGR2BGR565 = 12;

    /**
     * Color-conversion code from RGB to BGR565.
     */
    public static final int COLOR_RGB2BGR565 = 13;

    /**
     * Color-conversion code from BGR565 to BGR.
     */
    public static final int COLOR_BGR5652BGR = 14;

    /**
     * Color-conversion code from BGR565 to RGB.
     */
    public static final int COLOR_BGR5652RGB = 15;

    /**
     * Color-conversion code from BGRA to BGR565.
     */
    public static final int COLOR_BGRA2BGR565 = 16;

    /**
     * Color-conversion code from RGBA to BGR565.
     */
    public static final int COLOR_RGBA2BGR565 = 17;

    /**
     * Color-conversion code from BGR565 to BGRA.
     */
    public static final int COLOR_BGR5652BGRA = 18;

    /**
     * Color-conversion code from BGR565 to RGBA.
     */
    public static final int COLOR_BGR5652RGBA = 19;

    /**
     * Color-conversion code from GRAY to BGR565.
     */
    public static final int COLOR_GRAY2BGR565 = 20;

    /**
     * Color-conversion code from BGR565 to GRAY.
     */
    public static final int COLOR_BGR5652GRAY = 21;

    /**
     * Color-conversion code from BGR to BGR555.
     */
    public static final int COLOR_BGR2BGR555 = 22;

    /**
     * Color-conversion code from RGB to BGR555.
     */
    public static final int COLOR_RGB2BGR555 = 23;

    /**
     * Color-conversion code from BGR555 to BGR.
     */
    public static final int COLOR_BGR5552BGR = 24;

    /**
     * Color-conversion code from BGR555 to RGB.
     */
    public static final int COLOR_BGR5552RGB = 25;

    /**
     * Color-conversion code from BGRA to BGR555.
     */
    public static final int COLOR_BGRA2BGR555 = 26;

    /**
     * Color-conversion code from RGBA to BGR555.
     */
    public static final int COLOR_RGBA2BGR555 = 27;

    /**
     * Color-conversion code from BGR555 to BGRA.
     */
    public static final int COLOR_BGR5552BGRA = 28;

    /**
     * Color-conversion code from BGR555 to RGBA.
     */
    public static final int COLOR_BGR5552RGBA = 29;

    /**
     * Color-conversion code from GRAY to BGR555.
     */
    public static final int COLOR_GRAY2BGR555 = 30;

    /**
     * Color-conversion code from BGR555 to GRAY.
     */
    public static final int COLOR_BGR5552GRAY = 31;

    /**
     * Color-conversion code from BGR to XYZ.
     */
    public static final int COLOR_BGR2XYZ = 32;

    /**
     * Color-conversion code from RGB to XYZ.
     */
    public static final int COLOR_RGB2XYZ = 33;

    /**
     * Color-conversion code from XYZ to BGR.
     */
    public static final int COLOR_XYZ2BGR = 34;

    /**
     * Color-conversion code from XYZ to RGB.
     */
    public static final int COLOR_XYZ2RGB = 35;

    /**
     * Color-conversion code from BGR to YCrCb.
     */
    public static final int COLOR_BGR2YCrCb = 36;

    /**
     * Color-conversion code from RGB to YCrCb.
     */
    public static final int COLOR_RGB2YCrCb = 37;

    /**
     * Color-conversion code from YCrCb to BGR.
     */
    public static final int COLOR_YCrCb2BGR = 38;

    /**
     * Color-conversion code from YCrCb to RGB.
     */
    public static final int COLOR_YCrCb2RGB = 39;

    /**
     * Color-conversion code from BGR to HSV.
     */
    public static final int COLOR_BGR2HSV = 40;

    /**
     * Color-conversion code from RGB to HSV.
     */
    public static final int COLOR_RGB2HSV = 41;

    /**
     * Color-conversion code from BGR to Lab.
     */
    public static final int COLOR_BGR2Lab = 44;

    /**
     * Color-conversion code from RGB to Lab.
     */
    public static final int COLOR_RGB2Lab = 45;

    /**
     * Color-conversion code from BGR to Luv.
     */
    public static final int COLOR_BGR2Luv = 50;

    /**
     * Color-conversion code from RGB to Luv.
     */
    public static final int COLOR_RGB2Luv = 51;

    /**
     * Color-conversion code from BGR to HLS.
     */
    public static final int COLOR_BGR2HLS = 52;

    /**
     * Color-conversion code from RGB to HLS.
     */
    public static final int COLOR_RGB2HLS = 53;

    /**
     * Color-conversion code from HSV to BGR.
     */
    public static final int COLOR_HSV2BGR = 54;

    /**
     * Color-conversion code from HSV to RGB.
     */
    public static final int COLOR_HSV2RGB = 55;

    /**
     * Color-conversion code from Lab to BGR.
     */
    public static final int COLOR_Lab2BGR = 56;

    /**
     * Color-conversion code from Lab to RGB.
     */
    public static final int COLOR_Lab2RGB = 57;

    /**
     * Color-conversion code from Luv to BGR.
     */
    public static final int COLOR_Luv2BGR = 58;

    /**
     * Color-conversion code from Luv to RGB.
     */
    public static final int COLOR_Luv2RGB = 59;

    /**
     * Color-conversion code from HLS to BGR.
     */
    public static final int COLOR_HLS2BGR = 60;

    /**
     * Color-conversion code from HLS to RGB.
     */
    public static final int COLOR_HLS2RGB = 61;

    /**
     * Color-conversion code from BGR to HSV_FULL.
     */
    public static final int COLOR_BGR2HSV_FULL = 66;

    /**
     * Color-conversion code from RGB to HSV_FULL.
     */
    public static final int COLOR_RGB2HSV_FULL = 67;

    /**
     * Color-conversion code from BGR to HLS_FULL.
     */
    public static final int COLOR_BGR2HLS_FULL = 68;

    /**
     * Color-conversion code from RGB to HLS_FULL.
     */
    public static final int COLOR_RGB2HLS_FULL = 69;

    /**
     * Color-conversion code from HSV to BGR_FULL.
     */
    public static final int COLOR_HSV2BGR_FULL = 70;

    /**
     * Color-conversion code from HSV to RGB_FULL.
     */
    public static final int COLOR_HSV2RGB_FULL = 71;

    /**
     * Color-conversion code from HLS to BGR_FULL.
     */
    public static final int COLOR_HLS2BGR_FULL = 72;

    /**
     * Color-conversion code from HLS to RGB_FULL.
     */
    public static final int COLOR_HLS2RGB_FULL = 73;

    /**
     * Color-conversion code from LBGR to Lab.
     */
    public static final int COLOR_LBGR2Lab = 74;

    /**
     * Color-conversion code from LRGB to Lab.
     */
    public static final int COLOR_LRGB2Lab = 75;

    /**
     * Color-conversion code from LBGR to Luv.
     */
    public static final int COLOR_LBGR2Luv = 76;

    /**
     * Color-conversion code from LRGB to Luv.
     */
    public static final int COLOR_LRGB2Luv = 77;

    /**
     * Color-conversion code from Lab to LBGR.
     */
    public static final int COLOR_Lab2LBGR = 78;

    /**
     * Color-conversion code from Lab to LRGB.
     */
    public static final int COLOR_Lab2LRGB = 79;

    /**
     * Color-conversion code from Luv to LBGR.
     */
    public static final int COLOR_Luv2LBGR = 80;

    /**
     * Color-conversion code from Luv to LRGB.
     */
    public static final int COLOR_Luv2LRGB = 81;

    /**
     * Color-conversion code from BGR to YUV.
     */
    public static final int COLOR_BGR2YUV = 82;

    /**
     * Color-conversion code from RGB to YUV.
     */
    public static final int COLOR_RGB2YUV = 83;

    /**
     * Color-conversion code from YUV to BGR.
     */
    public static final int COLOR_YUV2BGR = 84;

    /**
     * Color-conversion code from YUV to RGB.
     */
    public static final int COLOR_YUV2RGB = 85;

    /**
     * Color-conversion code from YUV to RGB with NV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_NV12 = 90;

    /**
     * Color-conversion code from YUV to BGR with NV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_NV12 = 91;

    /**
     * Color-conversion code from YUV to RGB with NV21 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_NV21 = 92;

    /**
     * Color-conversion code from YUV to BGR with NV21 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_NV21 = 93;

    /**
     * Color-conversion code from YUV4 to 0sp2RGB.
     */
    public static final int COLOR_YUV420sp2RGB = COLOR_YUV2RGB_NV21;

    /**
     * Color-conversion code from YUV4 to 0sp2BGR.
     */
    public static final int COLOR_YUV420sp2BGR = COLOR_YUV2BGR_NV21;

    /**
     * Color-conversion code from YUV to RGBA with NV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_NV12 = 94;

    /**
     * Color-conversion code from YUV to BGRA with NV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_NV12 = 95;

    /**
     * Color-conversion code from YUV to RGBA with NV21 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_NV21 = 96;

    /**
     * Color-conversion code from YUV to BGRA with NV21 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_NV21 = 97;

    /**
     * Color-conversion code from YUV4 to 0sp2RGBA.
     */
    public static final int COLOR_YUV420sp2RGBA = COLOR_YUV2RGBA_NV21;

    /**
     * Color-conversion code from YUV4 to 0sp2BGRA.
     */
    public static final int COLOR_YUV420sp2BGRA = COLOR_YUV2BGRA_NV21;

    /**
     * Color-conversion code from YUV to RGB with YV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_YV12 = 98;

    /**
     * Color-conversion code from YUV to BGR with YV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_YV12 = 99;

    /**
     * Color-conversion code from YUV to RGB with IYUV layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_IYUV = 100;

    /**
     * Color-conversion code from YUV to BGR with IYUV layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_IYUV = 101;

    /**
     * Color-conversion code from YUV to RGB with I420 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_I420 = COLOR_YUV2RGB_IYUV;

    /**
     * Color-conversion code from YUV to BGR with I420 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_I420 = COLOR_YUV2BGR_IYUV;

    /**
     * Color-conversion code from YUV4 to 0p2RGB.
     */
    public static final int COLOR_YUV420p2RGB = COLOR_YUV2RGB_YV12;

    /**
     * Color-conversion code from YUV4 to 0p2BGR.
     */
    public static final int COLOR_YUV420p2BGR = COLOR_YUV2BGR_YV12;

    /**
     * Color-conversion code from YUV to RGBA with YV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_YV12 = 102;

    /**
     * Color-conversion code from YUV to BGRA with YV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_YV12 = 103;

    /**
     * Color-conversion code from YUV to RGBA with IYUV layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_IYUV = 104;

    /**
     * Color-conversion code from YUV to BGRA with IYUV layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_IYUV = 105;

    /**
     * Color-conversion code from YUV to RGBA with I420 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_I420 = COLOR_YUV2RGBA_IYUV;

    /**
     * Color-conversion code from YUV to BGRA with I420 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_I420 = COLOR_YUV2BGRA_IYUV;

    /**
     * Color-conversion code from YUV4 to 0p2RGBA.
     */
    public static final int COLOR_YUV420p2RGBA = COLOR_YUV2RGBA_YV12;

    /**
     * Color-conversion code from YUV4 to 0p2BGRA.
     */
    public static final int COLOR_YUV420p2BGRA = COLOR_YUV2BGRA_YV12;

    /**
     * Color-conversion code from YUV to GRAY_420.
     */
    public static final int COLOR_YUV2GRAY_420 = 106;

    /**
     * Color-conversion code from YUV to GRAY with NV21 layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_NV21 = COLOR_YUV2GRAY_420;

    /**
     * Color-conversion code from YUV to GRAY with NV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_NV12 = COLOR_YUV2GRAY_420;

    /**
     * Color-conversion code from YUV to GRAY with YV12 layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_YV12 = COLOR_YUV2GRAY_420;

    /**
     * Color-conversion code from YUV to GRAY with IYUV layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_IYUV = COLOR_YUV2GRAY_420;

    /**
     * Color-conversion code from YUV to GRAY with I420 layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_I420 = COLOR_YUV2GRAY_420;

    /**
     * Color-conversion code from YUV4 to 0sp2GRAY.
     */
    public static final int COLOR_YUV420sp2GRAY = COLOR_YUV2GRAY_420;

    /**
     * Color-conversion code from YUV4 to 0p2GRAY.
     */
    public static final int COLOR_YUV420p2GRAY = COLOR_YUV2GRAY_420;

    /**
     * Color-conversion code from YUV to RGB with UYVY layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_UYVY = 107;

    /**
     * Color-conversion code from YUV to BGR with UYVY layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_UYVY = 108;

    /**
     * Color-conversion code from YUV to RGB with Y422 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_Y422 = COLOR_YUV2RGB_UYVY;

    /**
     * Color-conversion code from YUV to BGR with Y422 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_Y422 = COLOR_YUV2BGR_UYVY;

    /**
     * Color-conversion code from YUV to RGB with UYNV layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_UYNV = COLOR_YUV2RGB_UYVY;

    /**
     * Color-conversion code from YUV to BGR with UYNV layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_UYNV = COLOR_YUV2BGR_UYVY;

    /**
     * Color-conversion code from YUV to RGBA with UYVY layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_UYVY = 111;

    /**
     * Color-conversion code from YUV to BGRA with UYVY layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_UYVY = 112;

    /**
     * Color-conversion code from YUV to RGBA with Y422 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_Y422 = COLOR_YUV2RGBA_UYVY;

    /**
     * Color-conversion code from YUV to BGRA with Y422 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_Y422 = COLOR_YUV2BGRA_UYVY;

    /**
     * Color-conversion code from YUV to RGBA with UYNV layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_UYNV = COLOR_YUV2RGBA_UYVY;

    /**
     * Color-conversion code from YUV to BGRA with UYNV layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_UYNV = COLOR_YUV2BGRA_UYVY;

    /**
     * Color-conversion code from YUV to RGB with YUY2 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_YUY2 = 115;

    /**
     * Color-conversion code from YUV to BGR with YUY2 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_YUY2 = 116;

    /**
     * Color-conversion code from YUV to RGB with YVYU layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_YVYU = 117;

    /**
     * Color-conversion code from YUV to BGR with YVYU layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_YVYU = 118;

    /**
     * Color-conversion code from YUV to RGB with YUYV layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_YUYV = COLOR_YUV2RGB_YUY2;

    /**
     * Color-conversion code from YUV to BGR with YUYV layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_YUYV = COLOR_YUV2BGR_YUY2;

    /**
     * Color-conversion code from YUV to RGB with YUNV layout for the YUV components.
     */
    public static final int COLOR_YUV2RGB_YUNV = COLOR_YUV2RGB_YUY2;

    /**
     * Color-conversion code from YUV to BGR with YUNV layout for the YUV components.
     */
    public static final int COLOR_YUV2BGR_YUNV = COLOR_YUV2BGR_YUY2;

    /**
     * Color-conversion code from YUV to RGBA with YUY2 layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_YUY2 = 119;

    /**
     * Color-conversion code from YUV to BGRA with YUY2 layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_YUY2 = 120;

    /**
     * Color-conversion code from YUV to RGBA with YVYU layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_YVYU = 121;

    /**
     * Color-conversion code from YUV to BGRA with YVYU layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_YVYU = 122;

    /**
     * Color-conversion code from YUV to RGBA with YUYV layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_YUYV = COLOR_YUV2RGBA_YUY2;

    /**
     * Color-conversion code from YUV to BGRA with YUYV layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_YUYV = COLOR_YUV2BGRA_YUY2;

    /**
     * Color-conversion code from YUV to RGBA with YUNV layout for the YUV components.
     */
    public static final int COLOR_YUV2RGBA_YUNV = COLOR_YUV2RGBA_YUY2;

    /**
     * Color-conversion code from YUV to BGRA with YUNV layout for the YUV components.
     */
    public static final int COLOR_YUV2BGRA_YUNV = COLOR_YUV2BGRA_YUY2;

    /**
     * Color-conversion code from YUV to GRAY with UYVY layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_UYVY = 123;

    /**
     * Color-conversion code from YUV to GRAY with YUY2 layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_YUY2 = 124;

    /**
     * Color-conversion code from YUV to GRAY with Y422 layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_Y422 = COLOR_YUV2GRAY_UYVY;

    /**
     * Color-conversion code from YUV to GRAY with UYNV layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_UYNV = COLOR_YUV2GRAY_UYVY;

    /**
     * Color-conversion code from YUV to GRAY with YVYU layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_YVYU = COLOR_YUV2GRAY_YUY2;

    /**
     * Color-conversion code from YUV to GRAY with YUYV layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_YUYV = COLOR_YUV2GRAY_YUY2;

    /**
     * Color-conversion code from YUV to GRAY with YUNV layout for the YUV components.
     */
    public static final int COLOR_YUV2GRAY_YUNV = COLOR_YUV2GRAY_YUY2;

    /**
     * Color-conversion code from RGBA to mRGBA.
     */
    public static final int COLOR_RGBA2mRGBA = 125;

    /**
     * Color-conversion code from mRGBA to RGBA.
     */
    public static final int COLOR_mRGBA2RGBA = 126;

    /**
     * Color-conversion code from RGB to YUV with I420 layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_I420 = 127;

    /**
     * Color-conversion code from BGR to YUV with I420 layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_I420 = 128;

    /**
     * Color-conversion code from RGB to YUV with IYUV layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_IYUV = COLOR_RGB2YUV_I420;

    /**
     * Color-conversion code from BGR to YUV with IYUV layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_IYUV = COLOR_BGR2YUV_I420;

    /**
     * Color-conversion code from RGBA to YUV with I420 layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_I420 = 129;

    /**
     * Color-conversion code from BGRA to YUV with I420 layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_I420 = 130;

    /**
     * Color-conversion code from RGBA to YUV with IYUV layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_IYUV = COLOR_RGBA2YUV_I420;

    /**
     * Color-conversion code from BGRA to YUV with IYUV layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_IYUV = COLOR_BGRA2YUV_I420;

    /**
     * Color-conversion code from RGB to YUV with YV12 layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_YV12 = 131;

    /**
     * Color-conversion code from BGR to YUV with YV12 layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_YV12 = 132;

    /**
     * Color-conversion code from RGBA to YUV with YV12 layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_YV12 = 133;

    /**
     * Color-conversion code from BGRA to YUV with YV12 layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_YV12 = 134;

    /**
     * Color-conversion code from BayerBG to BGR.
     */
    public static final int COLOR_BayerBG2BGR = 46;

    /**
     * Color-conversion code from BayerGB to BGR.
     */
    public static final int COLOR_BayerGB2BGR = 47;

    /**
     * Color-conversion code from BayerRG to BGR.
     */
    public static final int COLOR_BayerRG2BGR = 48;

    /**
     * Color-conversion code from BayerGR to BGR.
     */
    public static final int COLOR_BayerGR2BGR = 49;

    /**
     * Color-conversion code from BayerRGGB to BGR.
     */
    public static final int COLOR_BayerRGGB2BGR = COLOR_BayerBG2BGR;

    /**
     * Color-conversion code from BayerGRBG to BGR.
     */
    public static final int COLOR_BayerGRBG2BGR = COLOR_BayerGB2BGR;

    /**
     * Color-conversion code from BayerBGGR to BGR.
     */
    public static final int COLOR_BayerBGGR2BGR = COLOR_BayerRG2BGR;

    /**
     * Color-conversion code from BayerGBRG to BGR.
     */
    public static final int COLOR_BayerGBRG2BGR = COLOR_BayerGR2BGR;

    /**
     * Color-conversion code from BayerRGGB to RGB.
     */
    public static final int COLOR_BayerRGGB2RGB = COLOR_BayerBGGR2BGR;

    /**
     * Color-conversion code from BayerGRBG to RGB.
     */
    public static final int COLOR_BayerGRBG2RGB = COLOR_BayerGBRG2BGR;

    /**
     * Color-conversion code from BayerBGGR to RGB.
     */
    public static final int COLOR_BayerBGGR2RGB = COLOR_BayerRGGB2BGR;

    /**
     * Color-conversion code from BayerGBRG to RGB.
     */
    public static final int COLOR_BayerGBRG2RGB = COLOR_BayerGRBG2BGR;

    /**
     * Color-conversion code from BayerBG to RGB.
     */
    public static final int COLOR_BayerBG2RGB = COLOR_BayerRG2BGR;

    /**
     * Color-conversion code from BayerGB to RGB.
     */
    public static final int COLOR_BayerGB2RGB = COLOR_BayerGR2BGR;

    /**
     * Color-conversion code from BayerRG to RGB.
     */
    public static final int COLOR_BayerRG2RGB = COLOR_BayerBG2BGR;

    /**
     * Color-conversion code from BayerGR to RGB.
     */
    public static final int COLOR_BayerGR2RGB = COLOR_BayerGB2BGR;

    /**
     * Color-conversion code from BayerBG to GRAY.
     */
    public static final int COLOR_BayerBG2GRAY = 86;

    /**
     * Color-conversion code from BayerGB to GRAY.
     */
    public static final int COLOR_BayerGB2GRAY = 87;

    /**
     * Color-conversion code from BayerRG to GRAY.
     */
    public static final int COLOR_BayerRG2GRAY = 88;

    /**
     * Color-conversion code from BayerGR to GRAY.
     */
    public static final int COLOR_BayerGR2GRAY = 89;

    /**
     * Color-conversion code from BayerRGGB to GRAY.
     */
    public static final int COLOR_BayerRGGB2GRAY = COLOR_BayerBG2GRAY;

    /**
     * Color-conversion code from BayerGRBG to GRAY.
     */
    public static final int COLOR_BayerGRBG2GRAY = COLOR_BayerGB2GRAY;

    /**
     * Color-conversion code from BayerBGGR to GRAY.
     */
    public static final int COLOR_BayerBGGR2GRAY = COLOR_BayerRG2GRAY;

    /**
     * Color-conversion code from BayerGBRG to GRAY.
     */
    public static final int COLOR_BayerGBRG2GRAY = COLOR_BayerGR2GRAY;

    /**
     * Color-conversion code from BayerBG to BGR using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerBG2BGR_VNG = 62;

    /**
     * Color-conversion code from BayerGB to BGR using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerGB2BGR_VNG = 63;

    /**
     * Color-conversion code from BayerRG to BGR using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerRG2BGR_VNG = 64;

    /**
     * Color-conversion code from BayerGR to BGR using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerGR2BGR_VNG = 65;

    /**
     * Color-conversion code from BayerRGGB to BGR using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerRGGB2BGR_VNG = COLOR_BayerBG2BGR_VNG;

    /**
     * Color-conversion code from BayerGRBG to BGR using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerGRBG2BGR_VNG = COLOR_BayerGB2BGR_VNG;

    /**
     * Color-conversion code from BayerBGGR to BGR using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerBGGR2BGR_VNG = COLOR_BayerRG2BGR_VNG;

    /**
     * Color-conversion code from BayerGBRG to BGR using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerGBRG2BGR_VNG = COLOR_BayerGR2BGR_VNG;

    /**
     * Color-conversion code from BayerRGGB to RGB using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerRGGB2RGB_VNG = COLOR_BayerBGGR2BGR_VNG;

    /**
     * Color-conversion code from BayerGRBG to RGB using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerGRBG2RGB_VNG = COLOR_BayerGBRG2BGR_VNG;

    /**
     * Color-conversion code from BayerBGGR to RGB using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerBGGR2RGB_VNG = COLOR_BayerRGGB2BGR_VNG;

    /**
     * Color-conversion code from BayerGBRG to RGB using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerGBRG2RGB_VNG = COLOR_BayerGRBG2BGR_VNG;

    /**
     * Color-conversion code from BayerBG to RGB using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerBG2RGB_VNG = COLOR_BayerRG2BGR_VNG;

    /**
     * Color-conversion code from BayerGB to RGB using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerGB2RGB_VNG = COLOR_BayerGR2BGR_VNG;

    /**
     * Color-conversion code from BayerRG to RGB using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerRG2RGB_VNG = COLOR_BayerBG2BGR_VNG;

    /**
     * Color-conversion code from BayerGR to RGB using variable-number-of-gradients demosaicing.
     */
    public static final int COLOR_BayerGR2RGB_VNG = COLOR_BayerGB2BGR_VNG;

    /**
     * Color-conversion code from BayerBG to BGR using edge-aware demosaicing.
     */
    public static final int COLOR_BayerBG2BGR_EA = 135;

    /**
     * Color-conversion code from BayerGB to BGR using edge-aware demosaicing.
     */
    public static final int COLOR_BayerGB2BGR_EA = 136;

    /**
     * Color-conversion code from BayerRG to BGR using edge-aware demosaicing.
     */
    public static final int COLOR_BayerRG2BGR_EA = 137;

    /**
     * Color-conversion code from BayerGR to BGR using edge-aware demosaicing.
     */
    public static final int COLOR_BayerGR2BGR_EA = 138;

    /**
     * Color-conversion code from BayerRGGB to BGR using edge-aware demosaicing.
     */
    public static final int COLOR_BayerRGGB2BGR_EA = COLOR_BayerBG2BGR_EA;

    /**
     * Color-conversion code from BayerGRBG to BGR using edge-aware demosaicing.
     */
    public static final int COLOR_BayerGRBG2BGR_EA = COLOR_BayerGB2BGR_EA;

    /**
     * Color-conversion code from BayerBGGR to BGR using edge-aware demosaicing.
     */
    public static final int COLOR_BayerBGGR2BGR_EA = COLOR_BayerRG2BGR_EA;

    /**
     * Color-conversion code from BayerGBRG to BGR using edge-aware demosaicing.
     */
    public static final int COLOR_BayerGBRG2BGR_EA = COLOR_BayerGR2BGR_EA;

    /**
     * Color-conversion code from BayerRGGB to RGB using edge-aware demosaicing.
     */
    public static final int COLOR_BayerRGGB2RGB_EA = COLOR_BayerBGGR2BGR_EA;

    /**
     * Color-conversion code from BayerGRBG to RGB using edge-aware demosaicing.
     */
    public static final int COLOR_BayerGRBG2RGB_EA = COLOR_BayerGBRG2BGR_EA;

    /**
     * Color-conversion code from BayerBGGR to RGB using edge-aware demosaicing.
     */
    public static final int COLOR_BayerBGGR2RGB_EA = COLOR_BayerRGGB2BGR_EA;

    /**
     * Color-conversion code from BayerGBRG to RGB using edge-aware demosaicing.
     */
    public static final int COLOR_BayerGBRG2RGB_EA = COLOR_BayerGRBG2BGR_EA;

    /**
     * Color-conversion code from BayerBG to RGB using edge-aware demosaicing.
     */
    public static final int COLOR_BayerBG2RGB_EA = COLOR_BayerRG2BGR_EA;

    /**
     * Color-conversion code from BayerGB to RGB using edge-aware demosaicing.
     */
    public static final int COLOR_BayerGB2RGB_EA = COLOR_BayerGR2BGR_EA;

    /**
     * Color-conversion code from BayerRG to RGB using edge-aware demosaicing.
     */
    public static final int COLOR_BayerRG2RGB_EA = COLOR_BayerBG2BGR_EA;

    /**
     * Color-conversion code from BayerGR to RGB using edge-aware demosaicing.
     */
    public static final int COLOR_BayerGR2RGB_EA = COLOR_BayerGB2BGR_EA;

    /**
     * Color-conversion code from BayerBG to BGRA.
     */
    public static final int COLOR_BayerBG2BGRA = 139;

    /**
     * Color-conversion code from BayerGB to BGRA.
     */
    public static final int COLOR_BayerGB2BGRA = 140;

    /**
     * Color-conversion code from BayerRG to BGRA.
     */
    public static final int COLOR_BayerRG2BGRA = 141;

    /**
     * Color-conversion code from BayerGR to BGRA.
     */
    public static final int COLOR_BayerGR2BGRA = 142;

    /**
     * Color-conversion code from BayerRGGB to BGRA.
     */
    public static final int COLOR_BayerRGGB2BGRA = COLOR_BayerBG2BGRA;

    /**
     * Color-conversion code from BayerGRBG to BGRA.
     */
    public static final int COLOR_BayerGRBG2BGRA = COLOR_BayerGB2BGRA;

    /**
     * Color-conversion code from BayerBGGR to BGRA.
     */
    public static final int COLOR_BayerBGGR2BGRA = COLOR_BayerRG2BGRA;

    /**
     * Color-conversion code from BayerGBRG to BGRA.
     */
    public static final int COLOR_BayerGBRG2BGRA = COLOR_BayerGR2BGRA;

    /**
     * Color-conversion code from BayerRGGB to RGBA.
     */
    public static final int COLOR_BayerRGGB2RGBA = COLOR_BayerBGGR2BGRA;

    /**
     * Color-conversion code from BayerGRBG to RGBA.
     */
    public static final int COLOR_BayerGRBG2RGBA = COLOR_BayerGBRG2BGRA;

    /**
     * Color-conversion code from BayerBGGR to RGBA.
     */
    public static final int COLOR_BayerBGGR2RGBA = COLOR_BayerRGGB2BGRA;

    /**
     * Color-conversion code from BayerGBRG to RGBA.
     */
    public static final int COLOR_BayerGBRG2RGBA = COLOR_BayerGRBG2BGRA;

    /**
     * Color-conversion code from BayerBG to RGBA.
     */
    public static final int COLOR_BayerBG2RGBA = COLOR_BayerRG2BGRA;

    /**
     * Color-conversion code from BayerGB to RGBA.
     */
    public static final int COLOR_BayerGB2RGBA = COLOR_BayerGR2BGRA;

    /**
     * Color-conversion code from BayerRG to RGBA.
     */
    public static final int COLOR_BayerRG2RGBA = COLOR_BayerBG2BGRA;

    /**
     * Color-conversion code from BayerGR to RGBA.
     */
    public static final int COLOR_BayerGR2RGBA = COLOR_BayerGB2BGRA;

    /**
     * Color-conversion code from RGB to YUV with UYVY layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_UYVY = 143;

    /**
     * Color-conversion code from BGR to YUV with UYVY layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_UYVY = 144;

    /**
     * Color-conversion code from RGB to YUV with Y422 layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_Y422 = COLOR_RGB2YUV_UYVY;

    /**
     * Color-conversion code from BGR to YUV with Y422 layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_Y422 = COLOR_BGR2YUV_UYVY;

    /**
     * Color-conversion code from RGB to YUV with UYNV layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_UYNV = COLOR_RGB2YUV_UYVY;

    /**
     * Color-conversion code from BGR to YUV with UYNV layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_UYNV = COLOR_BGR2YUV_UYVY;

    /**
     * Color-conversion code from RGBA to YUV with UYVY layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_UYVY = 145;

    /**
     * Color-conversion code from BGRA to YUV with UYVY layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_UYVY = 146;

    /**
     * Color-conversion code from RGBA to YUV with Y422 layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_Y422 = COLOR_RGBA2YUV_UYVY;

    /**
     * Color-conversion code from BGRA to YUV with Y422 layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_Y422 = COLOR_BGRA2YUV_UYVY;

    /**
     * Color-conversion code from RGBA to YUV with UYNV layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_UYNV = COLOR_RGBA2YUV_UYVY;

    /**
     * Color-conversion code from BGRA to YUV with UYNV layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_UYNV = COLOR_BGRA2YUV_UYVY;

    /**
     * Color-conversion code from RGB to YUV with YUY2 layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_YUY2 = 147;

    /**
     * Color-conversion code from BGR to YUV with YUY2 layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_YUY2 = 148;

    /**
     * Color-conversion code from RGB to YUV with YVYU layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_YVYU = 149;

    /**
     * Color-conversion code from BGR to YUV with YVYU layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_YVYU = 150;

    /**
     * Color-conversion code from RGB to YUV with YUYV layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_YUYV = COLOR_RGB2YUV_YUY2;

    /**
     * Color-conversion code from BGR to YUV with YUYV layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_YUYV = COLOR_BGR2YUV_YUY2;

    /**
     * Color-conversion code from RGB to YUV with YUNV layout for the YUV components.
     */
    public static final int COLOR_RGB2YUV_YUNV = COLOR_RGB2YUV_YUY2;

    /**
     * Color-conversion code from BGR to YUV with YUNV layout for the YUV components.
     */
    public static final int COLOR_BGR2YUV_YUNV = COLOR_BGR2YUV_YUY2;

    /**
     * Color-conversion code from RGBA to YUV with YUY2 layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_YUY2 = 151;

    /**
     * Color-conversion code from BGRA to YUV with YUY2 layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_YUY2 = 152;

    /**
     * Color-conversion code from RGBA to YUV with YVYU layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_YVYU = 153;

    /**
     * Color-conversion code from BGRA to YUV with YVYU layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_YVYU = 154;

    /**
     * Color-conversion code from RGBA to YUV with YUYV layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_YUYV = COLOR_RGBA2YUV_YUY2;

    /**
     * Color-conversion code from BGRA to YUV with YUYV layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_YUYV = COLOR_BGRA2YUV_YUY2;

    /**
     * Color-conversion code from RGBA to YUV with YUNV layout for the YUV components.
     */
    public static final int COLOR_RGBA2YUV_YUNV = COLOR_RGBA2YUV_YUY2;

    /**
     * Color-conversion code from BGRA to YUV with YUNV layout for the YUV components.
     */
    public static final int COLOR_BGRA2YUV_YUNV = COLOR_BGRA2YUV_YUY2;

    /**
     * Color-conversion code from BGR to Oklab.
     */
    public static final int COLOR_BGR2Oklab = 155;

    /**
     * Color-conversion code from RGB to Oklab.
     */
    public static final int COLOR_RGB2Oklab = 156;

    /**
     * Color-conversion code from Oklab to BGR.
     */
    public static final int COLOR_Oklab2BGR = 157;

    /**
     * Color-conversion code from Oklab to RGB.
     */
    public static final int COLOR_Oklab2RGB = 158;

    /**
     * Legacy upper-bound sentinel for color-conversion codes; retained at 155 for compatibility.
     */
    public static final int COLOR_COLORCVT_MAX = 155;

    /**
     * Color-map selector for the autumn palette.
     */
    public static final int COLORMAP_AUTUMN = 0;

    /**
     * Color-map selector for the bone palette.
     */
    public static final int COLORMAP_BONE = 1;

    /**
     * Color-map selector for the jet palette.
     */
    public static final int COLORMAP_JET = 2;

    /**
     * Color-map selector for the winter palette.
     */
    public static final int COLORMAP_WINTER = 3;

    /**
     * Color-map selector for the rainbow palette.
     */
    public static final int COLORMAP_RAINBOW = 4;

    /**
     * Color-map selector for the ocean palette.
     */
    public static final int COLORMAP_OCEAN = 5;

    /**
     * Color-map selector for the summer palette.
     */
    public static final int COLORMAP_SUMMER = 6;

    /**
     * Color-map selector for the spring palette.
     */
    public static final int COLORMAP_SPRING = 7;

    /**
     * Color-map selector for the cool palette.
     */
    public static final int COLORMAP_COOL = 8;

    /**
     * Color-map selector for the hsv palette.
     */
    public static final int COLORMAP_HSV = 9;

    /**
     * Color-map selector for the pink palette.
     */
    public static final int COLORMAP_PINK = 10;

    /**
     * Color-map selector for the hot palette.
     */
    public static final int COLORMAP_HOT = 11;

    /**
     * Color-map selector for the parula palette.
     */
    public static final int COLORMAP_PARULA = 12;

    /**
     * Color-map selector for the magma palette.
     */
    public static final int COLORMAP_MAGMA = 13;

    /**
     * Color-map selector for the inferno palette.
     */
    public static final int COLORMAP_INFERNO = 14;

    /**
     * Color-map selector for the plasma palette.
     */
    public static final int COLORMAP_PLASMA = 15;

    /**
     * Color-map selector for the viridis palette.
     */
    public static final int COLORMAP_VIRIDIS = 16;

    /**
     * Color-map selector for the cividis palette.
     */
    public static final int COLORMAP_CIVIDIS = 17;

    /**
     * Color-map selector for the twilight palette.
     */
    public static final int COLORMAP_TWILIGHT = 18;

    /**
     * Color-map selector for the twilight shifted palette.
     */
    public static final int COLORMAP_TWILIGHT_SHIFTED = 19;

    /**
     * Color-map selector for the turbo palette.
     */
    public static final int COLORMAP_TURBO = 20;

    /**
     * Color-map selector for the deepgreen palette.
     */
    public static final int COLORMAP_DEEPGREEN = 21;

    /**
     * Use Spaghetti for 8-connectivity and Spaghetti4C for 4-connectivity connected-component labeling.
     */
    public static final int CCL_DEFAULT = -1;

    /**
     * Use the SAUF connected-component labeling algorithm for either supported connectivity.
     */
    public static final int CCL_WU = 0;

    /**
     * Use BBDT for 8-connectivity and SAUF for 4-connectivity connected-component labeling.
     */
    public static final int CCL_GRANA = 1;

    /**
     * Use Spaghetti for 8-connectivity and Spaghetti4C for 4-connectivity connected-component labeling.
     */
    public static final int CCL_BOLELLI = 2;

    /**
     * Same as CCL_WU. It is preferable to use the flag with the name of the algorithm (CCL_SAUF) rather than the one
     * with the name of the first author (CCL_WU).
     */
    public static final int CCL_SAUF = 3;

    /**
     * Same as CCL_GRANA. It is preferable to use the flag with the name of the algorithm (CCL_BBDT) rather than the one
     * with the name of the first author (CCL_GRANA).
     */
    public static final int CCL_BBDT = 4;

    /**
     * Same as CCL_BOLELLI. It is preferable to use the flag with the name of the algorithm (CCL_SPAGHETTI) rather than
     * the one with the name of the first author (CCL_BOLELLI).
     */
    public static final int CCL_SPAGHETTI = 5;

    /**
     * The leftmost (x) coordinate which is the inclusive start of the bounding.
     */
    public static final int CC_STAT_LEFT = 0;

    /**
     * The topmost (y) coordinate which is the inclusive start of the bounding.
     */
    public static final int CC_STAT_TOP = 1;

    /**
     * The horizontal size of the bounding box.
     */
    public static final int CC_STAT_WIDTH = 2;

    /**
     * The vertical size of the bounding box.
     */
    public static final int CC_STAT_HEIGHT = 3;

    /**
     * The total area (in pixels) of the connected component.
     */
    public static final int CC_STAT_AREA = 4;

    /**
     * Max enumeration value. Used internally only for memory allocation.
     */
    public static final int CC_STAT_MAX = 5;

    /**
     * Legacy contour chain-code representation identifier.
     */
    public static final int CHAIN_CODE = 0;

    /**
     * Stores absolutely all the contour points. That is, any 2 subsequent points (x1,y1) and (x2,y2) of the contour
     * will be either horizontal, vertical or diagonal neighbors, that is, max(abs(x1-x2),abs(y2-y1))==1.
     */
    public static final int CHAIN_APPROX_NONE = 1;

    /**
     * Compresses horizontal, vertical, and diagonal segments and leaves only their end points. For example, an up-right
     * rectangular contour is encoded with 4 points.
     */
    public static final int CHAIN_APPROX_SIMPLE = 2;

    /**
     * Approximate contours with the Teh-Chin L1 chain approximation.
     */
    public static final int CHAIN_APPROX_TC89_L1 = 3;

    /**
     * Approximate contours with the Teh-Chin k-cosine chain approximation.
     */
    public static final int CHAIN_APPROX_TC89_KCOS = 4;

    /**
     * Retrieve contours using the link-runs representation.
     */
    public static final int LINK_RUNS = 5;

    /**
     * Each connected component of zeros in src (as well as all the non-zero pixels closest to the connected component)
     * will be assigned the same label.
     */
    public static final int DIST_LABEL_CCOMP = 0;

    /**
     * Each zero pixel (and all the non-zero pixels closest to it) gets its own label.
     */
    public static final int DIST_LABEL_PIXEL = 1;

    /**
     * Use a 3-by-3 neighborhood for approximate distance transformation.
     */
    public static final int DIST_MASK_3 = 3;

    /**
     * Use a 5-by-5 neighborhood for approximate distance transformation.
     */
    public static final int DIST_MASK_5 = 5;

    /**
     * Use the precise distance-transform algorithm for supported distance types.
     */
    public static final int DIST_MASK_PRECISE = 0;

    /**
     * If set, the difference between the current pixel and seed pixel is considered. Otherwise, the difference between
     * neighbor pixels is considered (that is, the range is floating).
     */
    public static final int FLOODFILL_FIXED_RANGE = 1 << 16;

    /**
     * If set, the function does not change the image ( newVal is ignored), and only fills the mask with the value
     * specified in bits 8-16 of flags as described above. This option only make sense in function variants that have
     * the mask parameter.
     */
    public static final int FLOODFILL_MASK_ONLY = 1 << 17;

    /**
     * An obvious background pixels.
     */
    public static final int GC_BGD = 0;

    /**
     * An obvious foreground (object) pixel.
     */
    public static final int GC_FGD = 1;

    /**
     * A possible background pixel.
     */
    public static final int GC_PR_BGD = 2;

    /**
     * A possible foreground pixel.
     */
    public static final int GC_PR_FGD = 3;

    /**
     * The function initializes the state and the mask using the provided rectangle. After that it runs iterCount
     * iterations of the algorithm.
     */
    public static final int GC_INIT_WITH_RECT = 0;

    /**
     * The function initializes the state using the provided mask. Note that GC_INIT_WITH_RECT and GC_INIT_WITH_MASK can
     * be combined. Then, all the pixels outside of the ROI are automatically initialized with GC_BGD .
     */
    public static final int GC_INIT_WITH_MASK = 1;

    /**
     * The value means that the algorithm should just resume.
     */
    public static final int GC_EVAL = 2;

    /**
     * The value means that the algorithm should just run the grabCut algorithm (a single iteration) with the fixed
     * model.
     */
    public static final int GC_EVAL_FREEZE_MODEL = 3;

    /**
     * Normal size sans-serif font.
     */
    public static final int FONT_HERSHEY_SIMPLEX = 0;

    /**
     * Small size sans-serif font.
     */
    public static final int FONT_HERSHEY_PLAIN = 1;

    /**
     * Normal size sans-serif font (more complex than FONT_HERSHEY_SIMPLEX).
     */
    public static final int FONT_HERSHEY_DUPLEX = 2;

    /**
     * Normal size serif font.
     */
    public static final int FONT_HERSHEY_COMPLEX = 3;

    /**
     * Normal size serif font (more complex than FONT_HERSHEY_COMPLEX).
     */
    public static final int FONT_HERSHEY_TRIPLEX = 4;

    /**
     * Smaller version of FONT_HERSHEY_COMPLEX.
     */
    public static final int FONT_HERSHEY_COMPLEX_SMALL = 5;

    /**
     * Hand-writing style font.
     */
    public static final int FONT_HERSHEY_SCRIPT_SIMPLEX = 6;

    /**
     * More complex variant of FONT_HERSHEY_SCRIPT_SIMPLEX.
     */
    public static final int FONT_HERSHEY_SCRIPT_COMPLEX = 7;

    /**
     * Flag for italic font.
     */
    public static final int FONT_ITALIC = 16;

    /**
     * Correlation d(H_1,H_2) = frac{sum_I (H_1(I) - bar{H_1}) (H_2(I) - bar{H_2})}{sqrt{sum_I(H_1(I) - bar{H_1})^2
     * sum_I(H_2(I) - bar{H_2})^2}} where bar{H_k} = frac{1}{N} sum _J H_k(J) and N is a total number of histogram bins.
     */
    public static final int HISTCMP_CORREL = 0;

    /**
     * Chi-Square d(H_1,H_2) = sum _I frac{left(H_1(I)-H_2(I)right)^2}{H_1(I)}.
     */
    public static final int HISTCMP_CHISQR = 1;

    /**
     * Intersection d(H_1,H_2) = sum _I min (H_1(I), H_2(I)).
     */
    public static final int HISTCMP_INTERSECT = 2;

    /**
     * Bhattacharyya distance (In fact, OpenCV computes Hellinger distance, which is related to Bhattacharyya
     * coefficient.) d(H_1,H_2) = sqrt{1 - frac{1}{sqrt{bar{H_1} bar{H_2} N^2}} sum_I sqrt{H_1(I) cdot H_2(I)}}.
     */
    public static final int HISTCMP_BHATTACHARYYA = 3;

    /**
     * Synonym for HISTCMP_BHATTACHARYYA.
     */
    public static final int HISTCMP_HELLINGER = HISTCMP_BHATTACHARYYA;

    /**
     * Compare histograms using twice the sum of squared bin differences divided by the sum of the two bin values.
     */
    public static final int HISTCMP_CHISQR_ALT = 4;

    /**
     * Kullback-Leibler divergence d(H_1,H_2) = sum _I H_1(I) log left(frac{H_1(I)}{H_2(I)}right).
     */
    public static final int HISTCMP_KL_DIV = 5;

    /**
     * Classical or standard Hough transform. Every line is represented by two floating-point numbers (rho, theta) ,
     * where rho is a distance between (0,0) point and the line, and theta is the angle between x-axis and the normal to
     * the line. Thus, the matrix must be (the created sequence will be) of CV_32FC2 type.
     */
    public static final int HOUGH_STANDARD = 0;

    /**
     * Probabilistic Hough transform (more efficient in case if the picture contains a few long linear segments). It
     * returns line segments rather than the whole line. Each segment is represented by starting and ending points, and
     * the matrix must be (the created sequence will be) of the CV_32SC4 type.
     */
    public static final int HOUGH_PROBABILISTIC = 1;

    /**
     * Multi-scale variant of the classical Hough transform. The lines are encoded the same way as HOUGH_STANDARD.
     */
    public static final int HOUGH_MULTI_SCALE = 2;

    /**
     * Use the gradient-based Hough circle detection method.
     */
    public static final int HOUGH_GRADIENT = 3;

    /**
     * Variation of HOUGH_GRADIENT to get better accuracy.
     */
    public static final int HOUGH_GRADIENT_ALT = 4;

    /**
     * Nearest neighbor interpolation.
     */
    public static final int INTER_NEAREST = 0;

    /**
     * Bilinear interpolation.
     */
    public static final int INTER_LINEAR = 1;

    /**
     * Bicubic interpolation.
     */
    public static final int INTER_CUBIC = 2;

    /**
     * Resampling using pixel area relation. It may be a preferred method for image decimation, as it gives moire'-free
     * results. But when the image is zoomed, it is similar to the INTER_NEAREST method.
     */
    public static final int INTER_AREA = 3;

    /**
     * Lanczos interpolation over 8x8 neighborhood.
     */
    public static final int INTER_LANCZOS4 = 4;

    /**
     * Bit exact bilinear interpolation.
     */
    public static final int INTER_LINEAR_EXACT = 5;

    /**
     * Bit exact nearest neighbor interpolation. This will produce same results as the nearest neighbor method in PIL,
     * scikit-image or Matlab.
     */
    public static final int INTER_NEAREST_EXACT = 6;

    /**
     * Bit mask that extracts the interpolation method from a combined warp option value.
     */
    public static final int INTER_MAX = 7;

    /**
     * Flag, fills all of the destination image pixels. If some of them correspond to outliers in the source image, they
     * are set to zero.
     */
    public static final int WARP_FILL_OUTLIERS = 8;

    /**
     * Flag, inverse transformation.
     */
    public static final int WARP_INVERSE_MAP = 16;

    /**
     * Interpret remap coordinates as offsets from each destination pixel coordinate.
     */
    public static final int WARP_RELATIVE_MAP = 32;

    /**
     * Number of fractional interpolation-coordinate bits.
     */
    public static final int INTER_BITS = 5;

    /**
     * Twice the number of fractional interpolation-coordinate bits.
     */
    public static final int INTER_BITS2 = INTER_BITS * 2;

    /**
     * Number of interpolation table positions along one axis.
     */
    public static final int INTER_TAB_SIZE = 1 << INTER_BITS;

    /**
     * Number of interpolation table positions for the two-dimensional coordinate pair.
     */
    public static final int INTER_TAB_SIZE2 = INTER_TAB_SIZE * INTER_TAB_SIZE;

    /**
     * No refinement applied.
     */
    public static final int LSD_REFINE_NONE = 0;

    /**
     * Standard refinement is applied. E.g. breaking arches into smaller straighter line approximations.
     */
    public static final int LSD_REFINE_STD = 1;

    /**
     * Advanced refinement. Number of false alarms is calculated, lines are.
     */
    public static final int LSD_REFINE_ADV = 2;

    /**
     * Drawing thickness value that fills the interior of a shape.
     */
    public static final int FILLED = -1;

    /**
     * 4-connected line.
     */
    public static final int LINE_4 = 4;

    /**
     * 8-connected line.
     */
    public static final int LINE_8 = 8;

    /**
     * Antialiased line.
     */
    public static final int LINE_AA = 16;

    /**
     * A crosshair marker shape.
     */
    public static final int MARKER_CROSS = 0;

    /**
     * A 45 degree tilted crosshair marker shape.
     */
    public static final int MARKER_TILTED_CROSS = 1;

    /**
     * A star marker shape, combination of cross and tilted cross.
     */
    public static final int MARKER_STAR = 2;

    /**
     * A diamond marker shape.
     */
    public static final int MARKER_DIAMOND = 3;

    /**
     * A square marker shape.
     */
    public static final int MARKER_SQUARE = 4;

    /**
     * An upwards pointing triangle marker shape.
     */
    public static final int MARKER_TRIANGLE_UP = 5;

    /**
     * A downwards pointing triangle marker shape.
     */
    public static final int MARKER_TRIANGLE_DOWN = 6;

    /**
     * A rectangular structuring element: E_{ij}=1.
     */
    public static final int MORPH_RECT = 0;

    /**
     * A cross-shaped structuring element:.
     */
    public static final int MORPH_CROSS = 1;

    /**
     * An elliptic structuring element, that is, a filled ellipse inscribed.
     */
    public static final int MORPH_ELLIPSE = 2;

    /**
     * A diamond structuring element defined by Manhattan distance.
     */
    public static final int MORPH_DIAMOND = 3;

    /**
     * See erode.
     */
    public static final int MORPH_ERODE = 0;

    /**
     * See dilate.
     */
    public static final int MORPH_DILATE = 1;

    /**
     * An opening operation.
     */
    public static final int MORPH_OPEN = 2;

    /**
     * A closing operation.
     */
    public static final int MORPH_CLOSE = 3;

    /**
     * A morphological gradient.
     */
    public static final int MORPH_GRADIENT = 4;

    /**
     * "top hat".
     */
    public static final int MORPH_TOPHAT = 5;

    /**
     * "black hat".
     */
    public static final int MORPH_BLACKHAT = 6;

    /**
     * "hit or miss".
     */
    public static final int MORPH_HITMISS = 7;

    /**
     * Put the text to the right from the origin.
     */
    public static final int PUT_TEXT_ALIGN_LEFT = 0;

    /**
     * Center the text at the origin; not implemented yet.
     */
    public static final int PUT_TEXT_ALIGN_CENTER = 1;

    /**
     * Put the text to the left of the origin.
     */
    public static final int PUT_TEXT_ALIGN_RIGHT = 2;

    /**
     * Alignment mask.
     */
    public static final int PUT_TEXT_ALIGN_MASK = 3;

    /**
     * Interpret the text origin relative to its top-left corner.
     */
    public static final int PUT_TEXT_ORIGIN_TL = 0;

    /**
     * Treat the target image as having bottom-left origin.
     */
    public static final int PUT_TEXT_ORIGIN_BL = 32;

    /**
     * Wrap text to the next line if it does not fit.
     */
    public static final int PUT_TEXT_WRAP = 128;

    /**
     * Retrieves only the extreme outer contours. It sets `hierarchy[i][2]=hierarchy[i][3]=-1` for all the contours.
     */
    public static final int RETR_EXTERNAL = 0;

    /**
     * Retrieves all of the contours without establishing any hierarchical relationships.
     */
    public static final int RETR_LIST = 1;

    /**
     * Retrieves all of the contours and organizes them into a two-level hierarchy. At the top level, there are external
     * boundaries of the components. At the second level, there are boundaries of the holes. If there is another contour
     * inside a hole of a connected component, it is still put at the top level.
     */
    public static final int RETR_CCOMP = 2;

    /**
     * Retrieves all of the contours and reconstructs a full hierarchy of nested contours.
     */
    public static final int RETR_TREE = 3;

    /**
     * Retrieve contours from a 32-bit integer label image using the flood-fill retrieval mode.
     */
    public static final int RETR_FLOODFILL = 4;

    /**
     * I_1(A,B) = sum _{i=1...7} left | frac{1}{m^A_i} - frac{1}{m^B_i} right |.
     */
    public static final int CONTOURS_MATCH_I1 = 1;

    /**
     * I_2(A,B) = sum _{i=1...7} left | m^A_i - m^B_i right |.
     */
    public static final int CONTOURS_MATCH_I2 = 2;

    /**
     * I_3(A,B) = max _{i=1...7} frac{ left| m^A_i - m^B_i right| }{ left| m^A_i right| }.
     */
    public static final int CONTOURS_MATCH_I3 = 3;

    /**
     * Select the Scharr derivative kernel instead of a regular Sobel kernel.
     */
    public static final int FILTER_SCHARR = -1;

    /**
     * Template-matching metric based on the sum of squared differences; smaller scores indicate a closer match.
     */
    public static final int TM_SQDIFF = 0;

    /**
     * &lt; R(x,y)= sum _{x',y'} (T(x',y')-I(x+x',y+y'))^2 with mask: R(x,y)= sum _{x',y'} left( (T(x',y')-I(x+x',y+y'))
     * cdot M(x',y') right)^2.
     */
    public static final int TM_SQDIFF_NORMED = 1;

    /**
     * &lt; R(x,y)= frac{sum_{x',y'} (T(x',y')-I(x+x',y+y'))^2}{sqrt{sum_{ x',y'}T(x',y')^2 cdot sum_{x',y'}
     * I(x+x',y+y')^2}} with mask: R(x,y)= frac{sum _{x',y'} left( (T(x',y')-I(x+x',y+y')) cdot M(x',y')
     * right)^2}{sqrt{sum_{x',y'} left( T(x',y') cdot M(x',y') right)^2 cdot sum_{x',y'} left( I(x+x',y+y') cdot
     * M(x',y') right)^2}}.
     */
    public static final int TM_CCORR = 2;

    /**
     * &lt; R(x,y)= sum _{x',y'} (T(x',y') cdot I(x+x',y+y')) with mask: R(x,y)= sum _{x',y'} (T(x',y') cdot
     * I(x+x',y+y') cdot M(x',y') ^2).
     */
    public static final int TM_CCORR_NORMED = 3;

    /**
     * &lt; R(x,y)= frac{sum_{x',y'} (T(x',y') cdot I(x+x',y+y'))}{sqrt{ sum_{x',y'}T(x',y')^2 cdot sum_{x',y'}
     * I(x+x',y+y')^2}} with mask: R(x,y)= frac{sum_{x',y'} (T(x',y') cdot I(x+x',y+y') cdot
     * M(x',y')^2)}{sqrt{sum_{x',y'} left( T(x',y') cdot M(x',y') right)^2 cdot sum_{x',y'} left( I(x+x',y+y') cdot
     * M(x',y') right)^2}}.
     */
    public static final int TM_CCOEFF = 4;

    /**
     * &lt; R(x,y)= sum _{x',y'} (T'(x',y') cdot I'(x+x',y+y')) where {l} T'(x',y')=T(x',y') - 1/(w cdot h) cdot sum _{
     * x'',y''} T(x'',y'') \\ I'(x+x',y+y')=I(x+x',y+y') - 1/(w cdot h) cdot sum _{x'',y''} I(x+x'',y+y'') with mask:
     * {l} T'(x',y')=M(x',y') cdot left( T(x',y') - frac{1}{sum _{x'',y''} M(x'',y'')} cdot sum _{x'',y''} (T(x'',y'')
     * cdot M(x'',y'')) right) \\ I'(x+x',y+y')=M(x',y') cdot left( I(x+x',y+y') - frac{1}{sum _{x'',y''} M(x'',y'')}
     * cdot sum _{x'',y''} (I(x+x'',y+y'') cdot M(x'',y'')) right).
     */
    public static final int TM_CCOEFF_NORMED = 5;

    /**
     * Write maxval when the source value is greater than thresh; write zero otherwise.
     */
    public static final int THRESH_BINARY = 0;

    /**
     * Write zero when the source value is greater than thresh; write maxval otherwise.
     */
    public static final int THRESH_BINARY_INV = 1;

    /**
     * Replace source values greater than thresh with thresh and preserve the remaining values.
     */
    public static final int THRESH_TRUNC = 2;

    /**
     * Preserve source values greater than thresh and replace the remaining values with zero.
     */
    public static final int THRESH_TOZERO = 3;

    /**
     * Replace source values greater than thresh with zero and preserve the remaining values.
     */
    public static final int THRESH_TOZERO_INV = 4;

    /**
     * Bit mask for extracting the basic threshold mode from combined threshold flags.
     */
    public static final int THRESH_MASK = 7;

    /**
     * Flag, use Otsu algorithm to choose the optimal threshold value.
     */
    public static final int THRESH_OTSU = 8;

    /**
     * Flag, use Triangle algorithm to choose the optimal threshold value.
     */
    public static final int THRESH_TRIANGLE = 16;

    /**
     * Flag, compute threshold only (useful for OTSU/TRIANGLE) but does not actually run thresholding.
     */
    public static final int THRESH_DRYRUN = 128;

    /**
     * Use an orthographic spherical projection.
     */
    public static final int PROJ_SPHERICAL_ORTHO = 0;

    /**
     * Use an equirectangular spherical projection.
     */
    public static final int PROJ_SPHERICAL_EQRECT = 1;

    /**
     * Remaps an image to/from polar space.
     */
    public static final int WARP_POLAR_LINEAR = 0;

    /**
     * Remaps an image to/from semilog-polar space.
     */
    public static final int WARP_POLAR_LOG = 256;

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @param ang_th      Gradient angle tolerance in degrees
     * @param log_eps     Detection threshold: -log10(NFA) &gt; log_eps. Used only when advance refinement is chosen
     * @param density_th  Minimal density of aligned region points in the enclosing rectangle
     * @param n_bins      Number of bins in pseudo-ordering of gradient modulus
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector(
            int refine,
            double scale,
            double sigma_scale,
            double quant,
            double ang_th,
            double log_eps,
            double density_th,
            int n_bins) {
        return LineSegmentDetector.__fromPtr__(
                createLineSegmentDetector_0(refine, scale, sigma_scale, quant, ang_th, log_eps, density_th, n_bins));
    }

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @param ang_th      Gradient angle tolerance in degrees
     * @param log_eps     Detection threshold: -log10(NFA) &gt; log_eps. Used only when advance refinement is chosen
     * @param density_th  Minimal density of aligned region points in the enclosing rectangle
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector(
            int refine,
            double scale,
            double sigma_scale,
            double quant,
            double ang_th,
            double log_eps,
            double density_th) {
        return LineSegmentDetector.__fromPtr__(
                createLineSegmentDetector_1(refine, scale, sigma_scale, quant, ang_th, log_eps, density_th));
    }

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @param ang_th      Gradient angle tolerance in degrees
     * @param log_eps     Detection threshold: -log10(NFA) &gt; log_eps. Used only when advance refinement is chosen
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector(
            int refine,
            double scale,
            double sigma_scale,
            double quant,
            double ang_th,
            double log_eps) {
        return LineSegmentDetector
                .__fromPtr__(createLineSegmentDetector_2(refine, scale, sigma_scale, quant, ang_th, log_eps));
    }

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @param ang_th      Gradient angle tolerance in degrees
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector(
            int refine,
            double scale,
            double sigma_scale,
            double quant,
            double ang_th) {
        return LineSegmentDetector.__fromPtr__(createLineSegmentDetector_3(refine, scale, sigma_scale, quant, ang_th));
    }

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector(
            int refine,
            double scale,
            double sigma_scale,
            double quant) {
        return LineSegmentDetector.__fromPtr__(createLineSegmentDetector_4(refine, scale, sigma_scale, quant));
    }

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector(int refine, double scale, double sigma_scale) {
        return LineSegmentDetector.__fromPtr__(createLineSegmentDetector_5(refine, scale, sigma_scale));
    }

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @param refine The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale  The scale of the image that will be used to find the lines. Range (0..1]
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector(int refine, double scale) {
        return LineSegmentDetector.__fromPtr__(createLineSegmentDetector_6(refine, scale));
    }

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @param refine The way found lines will be refined, see LineSegmentDetectorModes
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector(int refine) {
        return LineSegmentDetector.__fromPtr__(createLineSegmentDetector_7(refine));
    }

    /**
     * Creates a smart pointer to a LineSegmentDetector object and initializes it. The LineSegmentDetector algorithm is
     * defined using the standard values. Only advanced users may want to edit those, as to tailor it for their own
     * application.
     *
     * @return a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    public static LineSegmentDetector createLineSegmentDetector() {
        return LineSegmentDetector.__fromPtr__(createLineSegmentDetector_8());
    }

    /**
     * Returns Gaussian filter coefficients.
     *
     * @param ksize Aperture size. It should be odd ( {@code ksize} mod 2 = 1 ) and positive
     * @param sigma Gaussian standard deviation. If it is non-positive, it is computed from ksize as sigma =
     *              0.3*((ksize-1)*0.5 - 1) + 0.8
     * @param ktype Type of filter coefficients. It can be CV_32F or CV_64F
     * @return the normalized one-dimensional Gaussian coefficient column
     */
    public static Mat getGaussianKernel(int ksize, double sigma, int ktype) {
        return new Mat(getGaussianKernel_0(ksize, sigma, ktype));
    }

    /**
     * Returns Gaussian filter coefficients.
     *
     * @param ksize Aperture size. It should be odd ( {@code ksize} mod 2 = 1 ) and positive
     * @param sigma Gaussian standard deviation. If it is non-positive, it is computed from ksize as sigma =
     *              0.3*((ksize-1)*0.5 - 1) + 0.8
     * @return the normalized one-dimensional Gaussian coefficient column
     */
    public static Mat getGaussianKernel(int ksize, double sigma) {
        return new Mat(getGaussianKernel_1(ksize, sigma));
    }

    /**
     * Returns filter coefficients for computing spatial image derivatives. The function computes and returns the filter
     * coefficients for spatial image derivatives. When ksize=FILTER_SCHARR, the Scharr 3 times 3 kernels are generated
     * (see Scharr). Otherwise, Sobel kernels are generated (see Sobel). The filters are normally passed to sepFilter2D
     * or to.
     *
     * @param kx        Output matrix of row filter coefficients. It has the type ktype
     * @param ky        Output matrix of column filter coefficients. It has the type ktype
     * @param dx        Derivative order in respect of x
     * @param dy        Derivative order in respect of y
     * @param ksize     Aperture size. It can be FILTER_SCHARR, 1, 3, 5, or 7
     * @param normalize Flag indicating whether to normalize (scale down) the filter coefficients or not. Theoretically,
     *                  the coefficients should have the denominator =2^{ksize*2-dx-dy-2}. If you are going to filter
     *                  floating-point images, you are likely to use the normalized kernels. But if you compute
     *                  derivatives of an 8-bit image, store the results in a 16-bit image, and wish to preserve all the
     *                  fractional bits, you may want to set normalize=false
     * @param ktype     Type of filter coefficients. It can be CV_32f or CV_64F
     * @throws NullPointerException if kx, ky is null
     */
    public static void getDerivKernels(Mat kx, Mat ky, int dx, int dy, int ksize, boolean normalize, int ktype) {
        getDerivKernels_0(kx.nativeObj, ky.nativeObj, dx, dy, ksize, normalize, ktype);
    }

    /**
     * Returns filter coefficients for computing spatial image derivatives. The function computes and returns the filter
     * coefficients for spatial image derivatives. When ksize=FILTER_SCHARR, the Scharr 3 times 3 kernels are generated
     * (see Scharr). Otherwise, Sobel kernels are generated (see Sobel). The filters are normally passed to sepFilter2D
     * or to.
     *
     * @param kx        Output matrix of row filter coefficients. It has the type ktype
     * @param ky        Output matrix of column filter coefficients. It has the type ktype
     * @param dx        Derivative order in respect of x
     * @param dy        Derivative order in respect of y
     * @param ksize     Aperture size. It can be FILTER_SCHARR, 1, 3, 5, or 7
     * @param normalize Flag indicating whether to normalize (scale down) the filter coefficients or not. Theoretically,
     *                  the coefficients should have the denominator =2^{ksize*2-dx-dy-2}. If you are going to filter
     *                  floating-point images, you are likely to use the normalized kernels. But if you compute
     *                  derivatives of an 8-bit image, store the results in a 16-bit image, and wish to preserve all the
     *                  fractional bits, you may want to set normalize=false
     * @throws NullPointerException if kx, ky is null
     */
    public static void getDerivKernels(Mat kx, Mat ky, int dx, int dy, int ksize, boolean normalize) {
        getDerivKernels_1(kx.nativeObj, ky.nativeObj, dx, dy, ksize, normalize);
    }

    /**
     * Returns filter coefficients for computing spatial image derivatives. The function computes and returns the filter
     * coefficients for spatial image derivatives. When ksize=FILTER_SCHARR, the Scharr 3 times 3 kernels are generated
     * (see Scharr). Otherwise, Sobel kernels are generated (see Sobel). The filters are normally passed to sepFilter2D
     * or to.
     *
     * @param kx    Output matrix of row filter coefficients. It has the type ktype
     * @param ky    Output matrix of column filter coefficients. It has the type ktype
     * @param dx    Derivative order in respect of x
     * @param dy    Derivative order in respect of y
     * @param ksize Aperture size. It can be FILTER_SCHARR, 1, 3, 5, or 7
     * @throws NullPointerException if kx, ky is null
     */
    public static void getDerivKernels(Mat kx, Mat ky, int dx, int dy, int ksize) {
        getDerivKernels_2(kx.nativeObj, ky.nativeObj, dx, dy, ksize);
    }

    /**
     * Returns Gabor filter coefficients. For more details about gabor filter equations and parameters, see: Gabor
     * Filter (https://en.wikipedia.org/wiki/Gabor_filter).
     *
     * @param ksize Size of the filter returned
     * @param sigma Standard deviation of the gaussian envelope
     * @param theta Orientation of the normal to the parallel stripes of a Gabor function
     * @param lambd Wavelength of the sinusoidal factor
     * @param gamma Spatial aspect ratio
     * @param psi   Phase offset
     * @param ktype Type of filter coefficients. It can be CV_32F or CV_64F
     * @return the two-dimensional real Gabor filter kernel
     * @throws NullPointerException if ksize is null
     */
    public static Mat getGaborKernel(
            Size ksize,
            double sigma,
            double theta,
            double lambd,
            double gamma,
            double psi,
            int ktype) {
        return new Mat(getGaborKernel_0(ksize.width, ksize.height, sigma, theta, lambd, gamma, psi, ktype));
    }

    /**
     * Returns Gabor filter coefficients. For more details about gabor filter equations and parameters, see: Gabor
     * Filter (https://en.wikipedia.org/wiki/Gabor_filter).
     *
     * @param ksize Size of the filter returned
     * @param sigma Standard deviation of the gaussian envelope
     * @param theta Orientation of the normal to the parallel stripes of a Gabor function
     * @param lambd Wavelength of the sinusoidal factor
     * @param gamma Spatial aspect ratio
     * @param psi   Phase offset
     * @return the two-dimensional real Gabor filter kernel
     * @throws NullPointerException if ksize is null
     */
    public static Mat getGaborKernel(Size ksize, double sigma, double theta, double lambd, double gamma, double psi) {
        return new Mat(getGaborKernel_1(ksize.width, ksize.height, sigma, theta, lambd, gamma, psi));
    }

    /**
     * Returns Gabor filter coefficients. For more details about gabor filter equations and parameters, see: Gabor
     * Filter (https://en.wikipedia.org/wiki/Gabor_filter).
     *
     * @param ksize Size of the filter returned
     * @param sigma Standard deviation of the gaussian envelope
     * @param theta Orientation of the normal to the parallel stripes of a Gabor function
     * @param lambd Wavelength of the sinusoidal factor
     * @param gamma Spatial aspect ratio
     * @return the two-dimensional real Gabor filter kernel
     * @throws NullPointerException if ksize is null
     */
    public static Mat getGaborKernel(Size ksize, double sigma, double theta, double lambd, double gamma) {
        return new Mat(getGaborKernel_2(ksize.width, ksize.height, sigma, theta, lambd, gamma));
    }

    /**
     * Returns a structuring element of the specified size and shape for morphological operations. The function
     * constructs and returns the structuring element that can be further passed to erode, dilate or morphologyEx. But
     * you can also construct an arbitrary binary mask yourself and use it as the structuring element.
     *
     * @param shape  Element shape that could be one of MorphShapes
     * @param ksize  Size of the structuring element
     * @param anchor Anchor position within the element. The default value (-1, -1) means that the anchor is at the
     *               center. Note that only the shape of a cross-shaped element depends on the anchor position. In other
     *               cases the anchor just regulates how much the result of the morphological operation is shifted
     * @return a binary morphology kernel of the requested shape and size
     * @throws NullPointerException if ksize, anchor is null
     */
    public static Mat getStructuringElement(int shape, Size ksize, Point anchor) {
        return new Mat(getStructuringElement_0(shape, ksize.width, ksize.height, anchor.x, anchor.y));
    }

    /**
     * Returns a structuring element of the specified size and shape for morphological operations. The function
     * constructs and returns the structuring element that can be further passed to erode, dilate or morphologyEx. But
     * you can also construct an arbitrary binary mask yourself and use it as the structuring element.
     *
     * @param shape Element shape that could be one of MorphShapes
     * @param ksize Size of the structuring element
     * @return a binary morphology kernel of the requested shape and size
     * @throws NullPointerException if ksize is null
     */
    public static Mat getStructuringElement(int shape, Size ksize) {
        return new Mat(getStructuringElement_1(shape, ksize.width, ksize.height));
    }

    /**
     * Blurs an image using the median filter.
     *
     * @param src   input 1-, 3-, or 4-channel image; when ksize is 3 or 5, the image depth should be CV_8U, CV_16U, or
     *              CV_32F, for larger aperture sizes, it can only be CV_8U
     * @param dst   destination array of the same size and type as src
     * @param ksize aperture linear size; it must be odd and greater than 1, for example: 3, 5, 7
     * @throws NullPointerException if src, dst is null
     */
    public static void medianBlur(Mat src, Mat dst, int ksize) {
        medianBlur_0(src.nativeObj, dst.nativeObj, ksize);
    }

    /**
     * Blurs an image using a Gaussian filter. The function convolves the source image with the specified Gaussian
     * kernel. In-place filtering is supported.
     *
     * @param src        input image; the image can have any number of channels, which are processed independently, but
     *                   the depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        output image of the same size and type as src
     * @param ksize      Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive
     *                   and odd. Or, they can be zero's and then they are computed from sigma
     * @param sigmaX     Gaussian kernel standard deviation in X direction
     * @param sigmaY     Gaussian kernel standard deviation in Y direction; if sigmaY is zero, it is set to be equal to
     *                   sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height,
     *                   respectively (see getGaussianKernel for details); to fully control the result regardless of
     *                   possible future modifications of all this semantics, it is recommended to specify all of ksize,
     *                   sigmaX, and sigmaY
     * @param borderType pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @param hint       Implementation modification flags. See AlgorithmHint
     * @throws NullPointerException if src, dst, ksize is null
     */
    public static void GaussianBlur(
            Mat src,
            Mat dst,
            Size ksize,
            double sigmaX,
            double sigmaY,
            int borderType,
            int hint) {
        GaussianBlur_0(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaX, sigmaY, borderType, hint);
    }

    /**
     * Blurs an image using a Gaussian filter. The function convolves the source image with the specified Gaussian
     * kernel. In-place filtering is supported.
     *
     * @param src        input image; the image can have any number of channels, which are processed independently, but
     *                   the depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        output image of the same size and type as src
     * @param ksize      Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive
     *                   and odd. Or, they can be zero's and then they are computed from sigma
     * @param sigmaX     Gaussian kernel standard deviation in X direction
     * @param sigmaY     Gaussian kernel standard deviation in Y direction; if sigmaY is zero, it is set to be equal to
     *                   sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height,
     *                   respectively (see getGaussianKernel for details); to fully control the result regardless of
     *                   possible future modifications of all this semantics, it is recommended to specify all of ksize,
     *                   sigmaX, and sigmaY
     * @param borderType pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst, ksize is null
     */
    public static void GaussianBlur(Mat src, Mat dst, Size ksize, double sigmaX, double sigmaY, int borderType) {
        GaussianBlur_1(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaX, sigmaY, borderType);
    }

    /**
     * Blurs an image using a Gaussian filter. The function convolves the source image with the specified Gaussian
     * kernel. In-place filtering is supported.
     *
     * @param src    input image; the image can have any number of channels, which are processed independently, but the
     *               depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst    output image of the same size and type as src
     * @param ksize  Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive and
     *               odd. Or, they can be zero's and then they are computed from sigma
     * @param sigmaX Gaussian kernel standard deviation in X direction
     * @param sigmaY Gaussian kernel standard deviation in Y direction; if sigmaY is zero, it is set to be equal to
     *               sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height, respectively
     *               (see getGaussianKernel for details); to fully control the result regardless of possible future
     *               modifications of all this semantics, it is recommended to specify all of ksize, sigmaX, and sigmaY
     * @throws NullPointerException if src, dst, ksize is null
     */
    public static void GaussianBlur(Mat src, Mat dst, Size ksize, double sigmaX, double sigmaY) {
        GaussianBlur_2(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaX, sigmaY);
    }

    /**
     * Blurs an image using a Gaussian filter. The function convolves the source image with the specified Gaussian
     * kernel. In-place filtering is supported.
     *
     * @param src    input image; the image can have any number of channels, which are processed independently, but the
     *               depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst    output image of the same size and type as src
     * @param ksize  Gaussian kernel size. ksize.width and ksize.height can differ but they both must be positive and
     *               odd. Or, they can be zero's and then they are computed from sigma
     * @param sigmaX Gaussian kernel standard deviation in X direction
     * @throws NullPointerException if src, dst, ksize is null
     */
    public static void GaussianBlur(Mat src, Mat dst, Size ksize, double sigmaX) {
        GaussianBlur_3(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, sigmaX);
    }

    /**
     * Applies the bilateral filter to an image. The function applies bilateral filtering to the input image, as
     * described in https://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/MANDUCHI1/Bilateral_Filtering.html
     * bilateralFilter can reduce unwanted noise very well while keeping edges fairly sharp. However, it is very slow
     * compared to most filters. _Sigma values_: For simplicity, you can set the 2 sigma values to be the same. If they
     * are small (&lt; 10), the filter will not have much effect, whereas if they are large (&gt; 150), they will have a
     * very strong effect, making the image look "cartoonish". _Filter size_: Large filters (d &gt; 5) are very slow, so
     * it is recommended to use d=5 for real-time applications, and perhaps d=9 for offline applications that need heavy
     * noise filtering. This filter does not work inplace.
     *
     * @param src        Source 8-bit or floating-point, 1-channel or 3-channel image
     * @param dst        Destination image of the same size and type as src
     * @param d          Diameter of each pixel neighborhood that is used during filtering. If it is non-positive, it is
     *                   computed from sigmaSpace
     * @param sigmaColor Filter sigma in the color space. A larger value of the parameter means that farther colors
     *                   within the pixel neighborhood (see sigmaSpace) will be mixed together, resulting in larger
     *                   areas of semi-equal color
     * @param sigmaSpace Filter sigma in the coordinate space. A larger value of the parameter means that farther pixels
     *                   will influence each other as long as their colors are close enough (see sigmaColor ). When
     *                   d&gt;0, it specifies the neighborhood size regardless of sigmaSpace. Otherwise, d is
     *                   proportional to sigmaSpace
     * @param borderType border mode used to extrapolate pixels outside of the image, see BorderTypes
     * @throws NullPointerException if src, dst is null
     */
    public static void bilateralFilter(Mat src, Mat dst, int d, double sigmaColor, double sigmaSpace, int borderType) {
        bilateralFilter_0(src.nativeObj, dst.nativeObj, d, sigmaColor, sigmaSpace, borderType);
    }

    /**
     * Applies the bilateral filter to an image. The function applies bilateral filtering to the input image, as
     * described in https://homepages.inf.ed.ac.uk/rbf/CVonline/LOCAL_COPIES/MANDUCHI1/Bilateral_Filtering.html
     * bilateralFilter can reduce unwanted noise very well while keeping edges fairly sharp. However, it is very slow
     * compared to most filters. _Sigma values_: For simplicity, you can set the 2 sigma values to be the same. If they
     * are small (&lt; 10), the filter will not have much effect, whereas if they are large (&gt; 150), they will have a
     * very strong effect, making the image look "cartoonish". _Filter size_: Large filters (d &gt; 5) are very slow, so
     * it is recommended to use d=5 for real-time applications, and perhaps d=9 for offline applications that need heavy
     * noise filtering. This filter does not work inplace.
     *
     * @param src        Source 8-bit or floating-point, 1-channel or 3-channel image
     * @param dst        Destination image of the same size and type as src
     * @param d          Diameter of each pixel neighborhood that is used during filtering. If it is non-positive, it is
     *                   computed from sigmaSpace
     * @param sigmaColor Filter sigma in the color space. A larger value of the parameter means that farther colors
     *                   within the pixel neighborhood (see sigmaSpace) will be mixed together, resulting in larger
     *                   areas of semi-equal color
     * @param sigmaSpace Filter sigma in the coordinate space. A larger value of the parameter means that farther pixels
     *                   will influence each other as long as their colors are close enough (see sigmaColor ). When
     *                   d&gt;0, it specifies the neighborhood size regardless of sigmaSpace. Otherwise, d is
     *                   proportional to sigmaSpace
     * @throws NullPointerException if src, dst is null
     */
    public static void bilateralFilter(Mat src, Mat dst, int d, double sigmaColor, double sigmaSpace) {
        bilateralFilter_1(src.nativeObj, dst.nativeObj, d, sigmaColor, sigmaSpace);
    }

    /**
     * Blurs an image using the box filter.
     *
     * @param src        input image
     * @param dst        output image of the same size and type as src
     * @param ddepth     the output image depth (-1 to use src.depth())
     * @param ksize      blurring kernel size
     * @param anchor     anchor point; default value Point(-1,-1) means that the anchor is at the kernel center
     * @param normalize  flag, specifying whether the kernel is normalized by its area or not
     * @param borderType border mode used to extrapolate pixels outside of the image, see BorderTypes. BORDER_WRAP is
     *                   not supported
     * @throws NullPointerException if src, dst, ksize, anchor is null
     */
    public static void boxFilter(
            Mat src,
            Mat dst,
            int ddepth,
            Size ksize,
            Point anchor,
            boolean normalize,
            int borderType) {
        boxFilter_0(
                src.nativeObj,
                dst.nativeObj,
                ddepth,
                ksize.width,
                ksize.height,
                anchor.x,
                anchor.y,
                normalize,
                borderType);
    }

    /**
     * Blurs an image using the box filter.
     *
     * @param src       input image
     * @param dst       output image of the same size and type as src
     * @param ddepth    the output image depth (-1 to use src.depth())
     * @param ksize     blurring kernel size
     * @param anchor    anchor point; default value Point(-1,-1) means that the anchor is at the kernel center
     * @param normalize flag, specifying whether the kernel is normalized by its area or not
     * @throws NullPointerException if src, dst, ksize, anchor is null
     */
    public static void boxFilter(Mat src, Mat dst, int ddepth, Size ksize, Point anchor, boolean normalize) {
        boxFilter_1(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height, anchor.x, anchor.y, normalize);
    }

    /**
     * Blurs an image using the box filter.
     *
     * @param src    input image
     * @param dst    output image of the same size and type as src
     * @param ddepth the output image depth (-1 to use src.depth())
     * @param ksize  blurring kernel size
     * @param anchor anchor point; default value Point(-1,-1) means that the anchor is at the kernel center
     * @throws NullPointerException if src, dst, ksize, anchor is null
     */
    public static void boxFilter(Mat src, Mat dst, int ddepth, Size ksize, Point anchor) {
        boxFilter_2(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height, anchor.x, anchor.y);
    }

    /**
     * Blurs an image using the box filter.
     *
     * @param src    input image
     * @param dst    output image of the same size and type as src
     * @param ddepth the output image depth (-1 to use src.depth())
     * @param ksize  blurring kernel size
     * @throws NullPointerException if src, dst, ksize is null
     */
    public static void boxFilter(Mat src, Mat dst, int ddepth, Size ksize) {
        boxFilter_3(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height);
    }

    /**
     * Calculates the normalized sum of squares of the pixel values overlapping the filter. For every pixel (x, y) in
     * the source image, the function calculates the sum of squares of those neighboring pixel values which overlap the
     * filter placed over the pixel (x, y) . The unnormalized square box filter can be useful in computing local image
     * statistics such as the local variance and standard deviation around the neighborhood of a pixel.
     *
     * @param src        input image
     * @param dst        output image of the same size and type as src
     * @param ddepth     the output image depth (-1 to use src.depth())
     * @param ksize      kernel size
     * @param anchor     kernel anchor point. The default value of Point(-1, -1) denotes that the anchor is at the
     *                   kernel center
     * @param normalize  flag, specifying whether the kernel is to be normalized by it's area or not
     * @param borderType border mode used to extrapolate pixels outside of the image, see BorderTypes. BORDER_WRAP is
     *                   not supported
     * @throws NullPointerException if src, dst, ksize, anchor is null
     */
    public static void sqrBoxFilter(
            Mat src,
            Mat dst,
            int ddepth,
            Size ksize,
            Point anchor,
            boolean normalize,
            int borderType) {
        sqrBoxFilter_0(
                src.nativeObj,
                dst.nativeObj,
                ddepth,
                ksize.width,
                ksize.height,
                anchor.x,
                anchor.y,
                normalize,
                borderType);
    }

    /**
     * Calculates the normalized sum of squares of the pixel values overlapping the filter. For every pixel (x, y) in
     * the source image, the function calculates the sum of squares of those neighboring pixel values which overlap the
     * filter placed over the pixel (x, y) . The unnormalized square box filter can be useful in computing local image
     * statistics such as the local variance and standard deviation around the neighborhood of a pixel.
     *
     * @param src       input image
     * @param dst       output image of the same size and type as src
     * @param ddepth    the output image depth (-1 to use src.depth())
     * @param ksize     kernel size
     * @param anchor    kernel anchor point. The default value of Point(-1, -1) denotes that the anchor is at the kernel
     *                  center
     * @param normalize flag, specifying whether the kernel is to be normalized by it's area or not
     * @throws NullPointerException if src, dst, ksize, anchor is null
     */
    public static void sqrBoxFilter(Mat src, Mat dst, int ddepth, Size ksize, Point anchor, boolean normalize) {
        sqrBoxFilter_1(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height, anchor.x, anchor.y, normalize);
    }

    /**
     * Calculates the normalized sum of squares of the pixel values overlapping the filter. For every pixel (x, y) in
     * the source image, the function calculates the sum of squares of those neighboring pixel values which overlap the
     * filter placed over the pixel (x, y) . The unnormalized square box filter can be useful in computing local image
     * statistics such as the local variance and standard deviation around the neighborhood of a pixel.
     *
     * @param src    input image
     * @param dst    output image of the same size and type as src
     * @param ddepth the output image depth (-1 to use src.depth())
     * @param ksize  kernel size
     * @param anchor kernel anchor point. The default value of Point(-1, -1) denotes that the anchor is at the kernel
     *               center
     * @throws NullPointerException if src, dst, ksize, anchor is null
     */
    public static void sqrBoxFilter(Mat src, Mat dst, int ddepth, Size ksize, Point anchor) {
        sqrBoxFilter_2(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height, anchor.x, anchor.y);
    }

    /**
     * Calculates the normalized sum of squares of the pixel values overlapping the filter. For every pixel (x, y) in
     * the source image, the function calculates the sum of squares of those neighboring pixel values which overlap the
     * filter placed over the pixel (x, y) . The unnormalized square box filter can be useful in computing local image
     * statistics such as the local variance and standard deviation around the neighborhood of a pixel.
     *
     * @param src    input image
     * @param dst    output image of the same size and type as src
     * @param ddepth the output image depth (-1 to use src.depth())
     * @param ksize  kernel size
     * @throws NullPointerException if src, dst, ksize is null
     */
    public static void sqrBoxFilter(Mat src, Mat dst, int ddepth, Size ksize) {
        sqrBoxFilter_3(src.nativeObj, dst.nativeObj, ddepth, ksize.width, ksize.height);
    }

    /**
     * Blurs an image using the normalized box filter.
     *
     * @param src        input image; it can have any number of channels, which are processed independently, but the
     *                   depth should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        output image of the same size and type as src
     * @param ksize      blurring kernel size
     * @param anchor     anchor point; default value Point(-1,-1) means that the anchor is at the kernel center
     * @param borderType border mode used to extrapolate pixels outside of the image, see BorderTypes. BORDER_WRAP is
     *                   not supported
     * @throws NullPointerException if src, dst, ksize, anchor is null
     */
    public static void blur(Mat src, Mat dst, Size ksize, Point anchor, int borderType) {
        blur_0(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, anchor.x, anchor.y, borderType);
    }

    /**
     * Blurs an image using the normalized box filter.
     *
     * @param src    input image; it can have any number of channels, which are processed independently, but the depth
     *               should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst    output image of the same size and type as src
     * @param ksize  blurring kernel size
     * @param anchor anchor point; default value Point(-1,-1) means that the anchor is at the kernel center
     * @throws NullPointerException if src, dst, ksize, anchor is null
     */
    public static void blur(Mat src, Mat dst, Size ksize, Point anchor) {
        blur_1(src.nativeObj, dst.nativeObj, ksize.width, ksize.height, anchor.x, anchor.y);
    }

    /**
     * Blurs an image using the normalized box filter.
     *
     * @param src   input image; it can have any number of channels, which are processed independently, but the depth
     *              should be CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst   output image of the same size and type as src
     * @param ksize blurring kernel size
     * @throws NullPointerException if src, dst, ksize is null
     */
    public static void blur(Mat src, Mat dst, Size ksize) {
        blur_2(src.nativeObj, dst.nativeObj, ksize.width, ksize.height);
    }

    /**
     * Blurs an image using the stackBlur. The function applies and stackBlur to an image. stackBlur can generate
     * similar results as Gaussian blur, and the time consumption does not increase with the increase of kernel size. It
     * creates a kind of moving stack of colors whilst scanning through the image. Thereby it just has to add one new
     * block of color to the right side of the stack and remove the leftmost color. The remaining colors on the topmost
     * layer of the stack are either added on or reduced by one, depending on if they are on the right or on the left
     * side of the stack. The only supported borderType is BORDER_REPLICATE. Original paper was proposed by Mario
     * Klingemann, which can be found https://underdestruction.com/2004/02/25/stackblur-2004.
     *
     * @param src   input image. The number of channels can be arbitrary, but the depth should be one of CV_8U, CV_16U,
     *              CV_16S or CV_32F
     * @param dst   output image of the same size and type as src
     * @param ksize stack-blurring kernel size. The ksize.width and ksize.height can differ but they both must be
     *              positive and odd
     * @throws NullPointerException if src, dst, ksize is null
     */
    public static void stackBlur(Mat src, Mat dst, Size ksize) {
        stackBlur_0(src.nativeObj, dst.nativeObj, ksize.width, ksize.height);
    }

    /**
     * Correlates an image with the supplied kernel. The kernel is not mirrored; convolution requires the caller to flip
     * the kernel and adjust its anchor. Destination depth, anchor and border rules follow the selected overload.
     *
     * @param src        input image
     * @param dst        output image of the same size and the same number of channels as src
     * @param ddepth     desired depth of the destination image, see REF: filter_depths "combinations"
     * @param kernel     convolution kernel (or rather a correlation kernel), a single-channel floating point matrix; if
     *                   you want to apply different kernels to different channels, split the image into separate color
     *                   planes using split and process them individually
     * @param anchor     anchor of the kernel that indicates the relative position of a filtered point within the
     *                   kernel; the anchor should lie within the kernel; default value (-1,-1) means that the anchor is
     *                   at the kernel center
     * @param delta      optional value added to the filtered pixels before storing them in dst
     * @param borderType pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void filter2D(Mat src, Mat dst, int ddepth, Mat kernel, Point anchor, double delta, int borderType) {
        filter2D_0(src.nativeObj, dst.nativeObj, ddepth, kernel.nativeObj, anchor.x, anchor.y, delta, borderType);
    }

    /**
     * Correlates an image with the supplied kernel. The kernel is not mirrored; convolution requires the caller to flip
     * the kernel and adjust its anchor. Destination depth, anchor and border rules follow the selected overload.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth desired depth of the destination image, see REF: filter_depths "combinations"
     * @param kernel convolution kernel (or rather a correlation kernel), a single-channel floating point matrix; if you
     *               want to apply different kernels to different channels, split the image into separate color planes
     *               using split and process them individually
     * @param anchor anchor of the kernel that indicates the relative position of a filtered point within the kernel;
     *               the anchor should lie within the kernel; default value (-1,-1) means that the anchor is at the
     *               kernel center
     * @param delta  optional value added to the filtered pixels before storing them in dst
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void filter2D(Mat src, Mat dst, int ddepth, Mat kernel, Point anchor, double delta) {
        filter2D_1(src.nativeObj, dst.nativeObj, ddepth, kernel.nativeObj, anchor.x, anchor.y, delta);
    }

    /**
     * Correlates an image with the supplied kernel. The kernel is not mirrored; convolution requires the caller to flip
     * the kernel and adjust its anchor. Destination depth, anchor and border rules follow the selected overload.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth desired depth of the destination image, see REF: filter_depths "combinations"
     * @param kernel convolution kernel (or rather a correlation kernel), a single-channel floating point matrix; if you
     *               want to apply different kernels to different channels, split the image into separate color planes
     *               using split and process them individually
     * @param anchor anchor of the kernel that indicates the relative position of a filtered point within the kernel;
     *               the anchor should lie within the kernel; default value (-1,-1) means that the anchor is at the
     *               kernel center
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void filter2D(Mat src, Mat dst, int ddepth, Mat kernel, Point anchor) {
        filter2D_2(src.nativeObj, dst.nativeObj, ddepth, kernel.nativeObj, anchor.x, anchor.y);
    }

    /**
     * Correlates an image with the supplied kernel. The kernel is not mirrored; convolution requires the caller to flip
     * the kernel and adjust its anchor. Destination depth, anchor and border rules follow the selected overload.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth desired depth of the destination image, see REF: filter_depths "combinations"
     * @param kernel convolution kernel (or rather a correlation kernel), a single-channel floating point matrix; if you
     *               want to apply different kernels to different channels, split the image into separate color planes
     *               using split and process them individually
     * @throws NullPointerException if src, dst, kernel is null
     */
    public static void filter2D(Mat src, Mat dst, int ddepth, Mat kernel) {
        filter2D_3(src.nativeObj, dst.nativeObj, ddepth, kernel.nativeObj);
    }

    /**
     * Filters an image using the supplied native filtering parameter object.
     *
     * @param src    source matrix
     * @param dst    destination matrix that receives the result
     * @param kernel filtering or morphological kernel
     * @param params native filter depth, anchor, border, scale and shift settings
     * @throws NullPointerException if src, dst, kernel, params is null
     */
    public static void filter2Dp(Mat src, Mat dst, Mat kernel, Filter2DParams params) {
        filter2Dp_0(src.nativeObj, dst.nativeObj, kernel.nativeObj, params.getNativeObjAddr());
    }

    /**
     * Filters an image using the supplied native filtering parameter object.
     *
     * @param src    source matrix
     * @param dst    destination matrix that receives the result
     * @param kernel filtering or morphological kernel
     * @throws NullPointerException if src, dst, kernel is null
     */
    public static void filter2Dp(Mat src, Mat dst, Mat kernel) {
        filter2Dp_1(src.nativeObj, dst.nativeObj, kernel.nativeObj);
    }

    /**
     * Applies a separable linear filter to an image. The function applies a separable linear filter to the image. That
     * is, first, every row of src is filtered with the 1D kernel kernelX. Then, every column of the result is filtered
     * with the 1D kernel kernelY. The final result shifted by delta is stored in dst .
     *
     * @param src        Source image
     * @param dst        Destination image of the same size and the same number of channels as src
     * @param ddepth     Destination image depth, see REF: filter_depths "combinations"
     * @param kernelX    Coefficients for filtering each row
     * @param kernelY    Coefficients for filtering each column
     * @param anchor     Anchor position within the kernel. The default value (-1,-1) means that the anchor is at the
     *                   kernel center
     * @param delta      Value added to the filtered results before storing them
     * @param borderType Pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst, kernelX, kernelY, anchor is null
     */
    public static void sepFilter2D(
            Mat src,
            Mat dst,
            int ddepth,
            Mat kernelX,
            Mat kernelY,
            Point anchor,
            double delta,
            int borderType) {
        sepFilter2D_0(
                src.nativeObj,
                dst.nativeObj,
                ddepth,
                kernelX.nativeObj,
                kernelY.nativeObj,
                anchor.x,
                anchor.y,
                delta,
                borderType);
    }

    /**
     * Applies a separable linear filter to an image. The function applies a separable linear filter to the image. That
     * is, first, every row of src is filtered with the 1D kernel kernelX. Then, every column of the result is filtered
     * with the 1D kernel kernelY. The final result shifted by delta is stored in dst .
     *
     * @param src     Source image
     * @param dst     Destination image of the same size and the same number of channels as src
     * @param ddepth  Destination image depth, see REF: filter_depths "combinations"
     * @param kernelX Coefficients for filtering each row
     * @param kernelY Coefficients for filtering each column
     * @param anchor  Anchor position within the kernel. The default value (-1,-1) means that the anchor is at the
     *                kernel center
     * @param delta   Value added to the filtered results before storing them
     * @throws NullPointerException if src, dst, kernelX, kernelY, anchor is null
     */
    public static void sepFilter2D(Mat src, Mat dst, int ddepth, Mat kernelX, Mat kernelY, Point anchor, double delta) {
        sepFilter2D_1(
                src.nativeObj,
                dst.nativeObj,
                ddepth,
                kernelX.nativeObj,
                kernelY.nativeObj,
                anchor.x,
                anchor.y,
                delta);
    }

    /**
     * Applies a separable linear filter to an image. The function applies a separable linear filter to the image. That
     * is, first, every row of src is filtered with the 1D kernel kernelX. Then, every column of the result is filtered
     * with the 1D kernel kernelY. The final result shifted by delta is stored in dst .
     *
     * @param src     Source image
     * @param dst     Destination image of the same size and the same number of channels as src
     * @param ddepth  Destination image depth, see REF: filter_depths "combinations"
     * @param kernelX Coefficients for filtering each row
     * @param kernelY Coefficients for filtering each column
     * @param anchor  Anchor position within the kernel. The default value (-1,-1) means that the anchor is at the
     *                kernel center
     * @throws NullPointerException if src, dst, kernelX, kernelY, anchor is null
     */
    public static void sepFilter2D(Mat src, Mat dst, int ddepth, Mat kernelX, Mat kernelY, Point anchor) {
        sepFilter2D_2(src.nativeObj, dst.nativeObj, ddepth, kernelX.nativeObj, kernelY.nativeObj, anchor.x, anchor.y);
    }

    /**
     * Applies a separable linear filter to an image. The function applies a separable linear filter to the image. That
     * is, first, every row of src is filtered with the 1D kernel kernelX. Then, every column of the result is filtered
     * with the 1D kernel kernelY. The final result shifted by delta is stored in dst .
     *
     * @param src     Source image
     * @param dst     Destination image of the same size and the same number of channels as src
     * @param ddepth  Destination image depth, see REF: filter_depths "combinations"
     * @param kernelX Coefficients for filtering each row
     * @param kernelY Coefficients for filtering each column
     * @throws NullPointerException if src, dst, kernelX, kernelY is null
     */
    public static void sepFilter2D(Mat src, Mat dst, int ddepth, Mat kernelX, Mat kernelY) {
        sepFilter2D_3(src.nativeObj, dst.nativeObj, ddepth, kernelX.nativeObj, kernelY.nativeObj);
    }

    /**
     * Calculates the first, second, third, or mixed image derivatives using an extended Sobel operator.
     *
     * @param src        input image
     * @param dst        output image of the same size and the same number of channels as src
     * @param ddepth     output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images it
     *                   will result in truncated derivatives
     * @param dx         order of the derivative x
     * @param dy         order of the derivative y
     * @param ksize      size of the extended Sobel kernel; it must be 1, 3, 5, or 7
     * @param scale      optional scale factor for the computed derivative values; by default, no scaling is applied
     *                   (see getDerivKernels for details)
     * @param delta      optional delta value that is added to the results prior to storing them in dst
     * @param borderType pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst is null
     */
    public static void Sobel(
            Mat src,
            Mat dst,
            int ddepth,
            int dx,
            int dy,
            int ksize,
            double scale,
            double delta,
            int borderType) {
        Sobel_0(src.nativeObj, dst.nativeObj, ddepth, dx, dy, ksize, scale, delta, borderType);
    }

    /**
     * Calculates the first, second, third, or mixed image derivatives using an extended Sobel operator.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images it
     *               will result in truncated derivatives
     * @param dx     order of the derivative x
     * @param dy     order of the derivative y
     * @param ksize  size of the extended Sobel kernel; it must be 1, 3, 5, or 7
     * @param scale  optional scale factor for the computed derivative values; by default, no scaling is applied (see
     *               getDerivKernels for details)
     * @param delta  optional delta value that is added to the results prior to storing them in dst
     * @throws NullPointerException if src, dst is null
     */
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy, int ksize, double scale, double delta) {
        Sobel_1(src.nativeObj, dst.nativeObj, ddepth, dx, dy, ksize, scale, delta);
    }

    /**
     * Calculates the first, second, third, or mixed image derivatives using an extended Sobel operator.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images it
     *               will result in truncated derivatives
     * @param dx     order of the derivative x
     * @param dy     order of the derivative y
     * @param ksize  size of the extended Sobel kernel; it must be 1, 3, 5, or 7
     * @param scale  optional scale factor for the computed derivative values; by default, no scaling is applied (see
     *               getDerivKernels for details)
     * @throws NullPointerException if src, dst is null
     */
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy, int ksize, double scale) {
        Sobel_2(src.nativeObj, dst.nativeObj, ddepth, dx, dy, ksize, scale);
    }

    /**
     * Calculates the first, second, third, or mixed image derivatives using an extended Sobel operator.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images it
     *               will result in truncated derivatives
     * @param dx     order of the derivative x
     * @param dy     order of the derivative y
     * @param ksize  size of the extended Sobel kernel; it must be 1, 3, 5, or 7
     * @throws NullPointerException if src, dst is null
     */
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy, int ksize) {
        Sobel_3(src.nativeObj, dst.nativeObj, ddepth, dx, dy, ksize);
    }

    /**
     * Calculates the first, second, third, or mixed image derivatives using an extended Sobel operator.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images it
     *               will result in truncated derivatives
     * @param dx     order of the derivative x
     * @param dy     order of the derivative y
     * @throws NullPointerException if src, dst is null
     */
    public static void Sobel(Mat src, Mat dst, int ddepth, int dx, int dy) {
        Sobel_4(src.nativeObj, dst.nativeObj, ddepth, dx, dy);
    }

    /**
     * Calculates the first order image derivative in both x and y using a Sobel operator Equivalent to calling: Sobel(
     * src, dx, CV_16SC1, 1, 0, 3 ); Sobel( src, dy, CV_16SC1, 0, 1, 3 );.
     *
     * @param src        input image
     * @param dx         output image with first-order derivative in x
     * @param dy         output image with first-order derivative in y
     * @param ksize      size of Sobel kernel. It must be 3
     * @param borderType pixel extrapolation method, see BorderTypes. Only BORDER_DEFAULT=BORDER_REFLECT_101 and
     *                   BORDER_REPLICATE are supported
     * @throws NullPointerException if src, dx, dy is null
     */
    public static void spatialGradient(Mat src, Mat dx, Mat dy, int ksize, int borderType) {
        spatialGradient_0(src.nativeObj, dx.nativeObj, dy.nativeObj, ksize, borderType);
    }

    /**
     * Calculates the first order image derivative in both x and y using a Sobel operator Equivalent to calling: Sobel(
     * src, dx, CV_16SC1, 1, 0, 3 ); Sobel( src, dy, CV_16SC1, 0, 1, 3 );.
     *
     * @param src   input image
     * @param dx    output image with first-order derivative in x
     * @param dy    output image with first-order derivative in y
     * @param ksize size of Sobel kernel. It must be 3
     * @throws NullPointerException if src, dx, dy is null
     */
    public static void spatialGradient(Mat src, Mat dx, Mat dy, int ksize) {
        spatialGradient_1(src.nativeObj, dx.nativeObj, dy.nativeObj, ksize);
    }

    /**
     * Calculates the first order image derivative in both x and y using a Sobel operator Equivalent to calling: Sobel(
     * src, dx, CV_16SC1, 1, 0, 3 ); Sobel( src, dy, CV_16SC1, 0, 1, 3 );.
     *
     * @param src input image
     * @param dx  output image with first-order derivative in x
     * @param dy  output image with first-order derivative in y
     * @throws NullPointerException if src, dx, dy is null
     */
    public static void spatialGradient(Mat src, Mat dx, Mat dy) {
        spatialGradient_2(src.nativeObj, dx.nativeObj, dy.nativeObj);
    }

    /**
     * Calculates the first x- or y- image derivative using Scharr operator.
     *
     * @param src        input image
     * @param dst        output image of the same size and the same number of channels as src
     * @param ddepth     output image depth, see REF: filter_depths "combinations"
     * @param dx         order of the derivative x
     * @param dy         order of the derivative y
     * @param scale      optional scale factor for the computed derivative values; by default, no scaling is applied
     *                   (see getDerivKernels for details)
     * @param delta      optional delta value that is added to the results prior to storing them in dst
     * @param borderType pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst is null
     */
    public static void Scharr(
            Mat src,
            Mat dst,
            int ddepth,
            int dx,
            int dy,
            double scale,
            double delta,
            int borderType) {
        Scharr_0(src.nativeObj, dst.nativeObj, ddepth, dx, dy, scale, delta, borderType);
    }

    /**
     * Calculates the first x- or y- image derivative using Scharr operator.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth output image depth, see REF: filter_depths "combinations"
     * @param dx     order of the derivative x
     * @param dy     order of the derivative y
     * @param scale  optional scale factor for the computed derivative values; by default, no scaling is applied (see
     *               getDerivKernels for details)
     * @param delta  optional delta value that is added to the results prior to storing them in dst
     * @throws NullPointerException if src, dst is null
     */
    public static void Scharr(Mat src, Mat dst, int ddepth, int dx, int dy, double scale, double delta) {
        Scharr_1(src.nativeObj, dst.nativeObj, ddepth, dx, dy, scale, delta);
    }

    /**
     * Calculates the first x- or y- image derivative using Scharr operator.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth output image depth, see REF: filter_depths "combinations"
     * @param dx     order of the derivative x
     * @param dy     order of the derivative y
     * @param scale  optional scale factor for the computed derivative values; by default, no scaling is applied (see
     *               getDerivKernels for details)
     * @throws NullPointerException if src, dst is null
     */
    public static void Scharr(Mat src, Mat dst, int ddepth, int dx, int dy, double scale) {
        Scharr_2(src.nativeObj, dst.nativeObj, ddepth, dx, dy, scale);
    }

    /**
     * Calculates the first x- or y- image derivative using Scharr operator.
     *
     * @param src    input image
     * @param dst    output image of the same size and the same number of channels as src
     * @param ddepth output image depth, see REF: filter_depths "combinations"
     * @param dx     order of the derivative x
     * @param dy     order of the derivative y
     * @throws NullPointerException if src, dst is null
     */
    public static void Scharr(Mat src, Mat dst, int ddepth, int dx, int dy) {
        Scharr_3(src.nativeObj, dst.nativeObj, ddepth, dx, dy);
    }

    /**
     * Calculates the Laplacian of an image.
     *
     * @param src        Source image
     * @param dst        Destination image of the same size and the same number of channels as src
     * @param ddepth     Desired depth of the destination image, see REF: filter_depths "combinations"
     * @param ksize      Aperture size used to compute the second-derivative filters. See getDerivKernels for details.
     *                   The size must be positive and odd
     * @param scale      Optional scale factor for the computed Laplacian values. By default, no scaling is applied. See
     *                   getDerivKernels for details
     * @param delta      Optional delta value that is added to the results prior to storing them in dst
     * @param borderType Pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst is null
     */
    public static void Laplacian(Mat src, Mat dst, int ddepth, int ksize, double scale, double delta, int borderType) {
        Laplacian_0(src.nativeObj, dst.nativeObj, ddepth, ksize, scale, delta, borderType);
    }

    /**
     * Calculates the Laplacian of an image.
     *
     * @param src    Source image
     * @param dst    Destination image of the same size and the same number of channels as src
     * @param ddepth Desired depth of the destination image, see REF: filter_depths "combinations"
     * @param ksize  Aperture size used to compute the second-derivative filters. See getDerivKernels for details. The
     *               size must be positive and odd
     * @param scale  Optional scale factor for the computed Laplacian values. By default, no scaling is applied. See
     *               getDerivKernels for details
     * @param delta  Optional delta value that is added to the results prior to storing them in dst
     * @throws NullPointerException if src, dst is null
     */
    public static void Laplacian(Mat src, Mat dst, int ddepth, int ksize, double scale, double delta) {
        Laplacian_1(src.nativeObj, dst.nativeObj, ddepth, ksize, scale, delta);
    }

    /**
     * Calculates the Laplacian of an image.
     *
     * @param src    Source image
     * @param dst    Destination image of the same size and the same number of channels as src
     * @param ddepth Desired depth of the destination image, see REF: filter_depths "combinations"
     * @param ksize  Aperture size used to compute the second-derivative filters. See getDerivKernels for details. The
     *               size must be positive and odd
     * @param scale  Optional scale factor for the computed Laplacian values. By default, no scaling is applied. See
     *               getDerivKernels for details
     * @throws NullPointerException if src, dst is null
     */
    public static void Laplacian(Mat src, Mat dst, int ddepth, int ksize, double scale) {
        Laplacian_2(src.nativeObj, dst.nativeObj, ddepth, ksize, scale);
    }

    /**
     * Calculates the Laplacian of an image.
     *
     * @param src    Source image
     * @param dst    Destination image of the same size and the same number of channels as src
     * @param ddepth Desired depth of the destination image, see REF: filter_depths "combinations"
     * @param ksize  Aperture size used to compute the second-derivative filters. See getDerivKernels for details. The
     *               size must be positive and odd
     * @throws NullPointerException if src, dst is null
     */
    public static void Laplacian(Mat src, Mat dst, int ddepth, int ksize) {
        Laplacian_3(src.nativeObj, dst.nativeObj, ddepth, ksize);
    }

    /**
     * Calculates the Laplacian of an image.
     *
     * @param src    Source image
     * @param dst    Destination image of the same size and the same number of channels as src
     * @param ddepth Desired depth of the destination image, see REF: filter_depths "combinations"
     * @throws NullPointerException if src, dst is null
     */
    public static void Laplacian(Mat src, Mat dst, int ddepth) {
        Laplacian_4(src.nativeObj, dst.nativeObj, ddepth);
    }

    /**
     * Finds edges in an image using the Canny algorithm The function finds edges in the input image and marks them in
     * the output map edges using the Canny algorithm. The smallest value between threshold1 and threshold2 is used for
     * edge linking. The largest value is used to find initial segments of strong edges. See
     * &lt;https://en.wikipedia.org/wiki/Canny_edge_detector&gt;.
     *
     * @param image        8-bit input image
     * @param edges        output edge map; single channels 8-bit image, which has the same size as image
     * @param threshold1   first threshold for the hysteresis procedure
     * @param threshold2   second threshold for the hysteresis procedure
     * @param apertureSize aperture size for the Sobel operator
     * @param L2gradient   a flag, indicating whether a more accurate L_2 norm =sqrt{(dI/dx)^2 + (dI/dy)^2} should be
     *                     used to calculate the image gradient magnitude ( L2gradient=true ), or whether the default
     *                     L_1 norm =|dI/dx|+|dI/dy| is enough ( L2gradient=false ). For efficiency, when
     *                     L2gradient=true the implementation compares the squared gradient magnitude (dI/dx)^2 +
     *                     (dI/dy)^2 against the squared thresholds instead of computing the square root; the resulting
     *                     edge map is the same as if the L_2 norm were computed directly
     * @throws NullPointerException if image, edges is null
     */
    public static void Canny(
            Mat image,
            Mat edges,
            double threshold1,
            double threshold2,
            int apertureSize,
            boolean L2gradient) {
        Canny_0(image.nativeObj, edges.nativeObj, threshold1, threshold2, apertureSize, L2gradient);
    }

    /**
     * Finds edges in an image using the Canny algorithm The function finds edges in the input image and marks them in
     * the output map edges using the Canny algorithm. The smallest value between threshold1 and threshold2 is used for
     * edge linking. The largest value is used to find initial segments of strong edges. See
     * &lt;https://en.wikipedia.org/wiki/Canny_edge_detector&gt;.
     *
     * @param image        8-bit input image
     * @param edges        output edge map; single channels 8-bit image, which has the same size as image
     * @param threshold1   first threshold for the hysteresis procedure
     * @param threshold2   second threshold for the hysteresis procedure
     * @param apertureSize aperture size for the Sobel operator
     * @throws NullPointerException if image, edges is null
     */
    public static void Canny(Mat image, Mat edges, double threshold1, double threshold2, int apertureSize) {
        Canny_1(image.nativeObj, edges.nativeObj, threshold1, threshold2, apertureSize);
    }

    /**
     * Finds edges in an image using the Canny algorithm The function finds edges in the input image and marks them in
     * the output map edges using the Canny algorithm. The smallest value between threshold1 and threshold2 is used for
     * edge linking. The largest value is used to find initial segments of strong edges. See
     * &lt;https://en.wikipedia.org/wiki/Canny_edge_detector&gt;.
     *
     * @param image      8-bit input image
     * @param edges      output edge map; single channels 8-bit image, which has the same size as image
     * @param threshold1 first threshold for the hysteresis procedure
     * @param threshold2 second threshold for the hysteresis procedure
     * @throws NullPointerException if image, edges is null
     */
    public static void Canny(Mat image, Mat edges, double threshold1, double threshold2) {
        Canny_2(image.nativeObj, edges.nativeObj, threshold1, threshold2);
    }

    /**
     * Finds edges in an image using the Canny algorithm The function finds edges in the input image and marks them in
     * the output map edges using the Canny algorithm. The smallest value between threshold1 and threshold2 is used for
     * edge linking. The largest value is used to find initial segments of strong edges. See
     * &lt;https://en.wikipedia.org/wiki/Canny_edge_detector&gt;.
     *
     * @param dx         16-bit x derivative of input image (CV_16SC1 or CV_16SC3)
     * @param dy         16-bit y derivative of input image (same type as dx)
     * @param edges      output edge map; single channels 8-bit image, which has the same size as image
     * @param threshold1 first threshold for the hysteresis procedure
     * @param threshold2 second threshold for the hysteresis procedure
     * @param L2gradient a flag, indicating whether a more accurate L_2 norm =sqrt{(dI/dx)^2 + (dI/dy)^2} should be used
     *                   to calculate the image gradient magnitude ( L2gradient=true ), or whether the default L_1 norm
     *                   =|dI/dx|+|dI/dy| is enough ( L2gradient=false ). For efficiency, when L2gradient=true the
     *                   implementation compares the squared gradient magnitude (dI/dx)^2 + (dI/dy)^2 against the
     *                   squared thresholds instead of computing the square root; the resulting edge map is the same as
     *                   if the L_2 norm were computed directly
     * @throws NullPointerException if dx, dy, edges is null
     */
    public static void Canny(Mat dx, Mat dy, Mat edges, double threshold1, double threshold2, boolean L2gradient) {
        Canny_3(dx.nativeObj, dy.nativeObj, edges.nativeObj, threshold1, threshold2, L2gradient);
    }

    /**
     * Finds edges in an image using the Canny algorithm The function finds edges in the input image and marks them in
     * the output map edges using the Canny algorithm. The smallest value between threshold1 and threshold2 is used for
     * edge linking. The largest value is used to find initial segments of strong edges. See
     * &lt;https://en.wikipedia.org/wiki/Canny_edge_detector&gt;.
     *
     * @param dx         16-bit x derivative of input image (CV_16SC1 or CV_16SC3)
     * @param dy         16-bit y derivative of input image (same type as dx)
     * @param edges      output edge map; single channels 8-bit image, which has the same size as image
     * @param threshold1 first threshold for the hysteresis procedure
     * @param threshold2 second threshold for the hysteresis procedure
     * @throws NullPointerException if dx, dy, edges is null
     */
    public static void Canny(Mat dx, Mat dy, Mat edges, double threshold1, double threshold2) {
        Canny_4(dx.nativeObj, dy.nativeObj, edges.nativeObj, threshold1, threshold2);
    }

    /**
     * Calculates the minimal eigenvalue of gradient matrices for corner detection. The function is similar to
     * cornerEigenValsAndVecs but it calculates and stores only the minimal eigenvalue of the covariance matrix of
     * derivatives, that is, min(lambda_1, lambda_2) in terms of the formulae in the cornerEigenValsAndVecs description.
     *
     * @param src        Input single-channel 8-bit or floating-point image
     * @param dst        Image to store the minimal eigenvalues. It has the type CV_32FC1 and the same size as src
     * @param blockSize  Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @param ksize      Aperture parameter for the Sobel operator
     * @param borderType Pixel extrapolation method. See BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst is null
     */
    public static void cornerMinEigenVal(Mat src, Mat dst, int blockSize, int ksize, int borderType) {
        cornerMinEigenVal_0(src.nativeObj, dst.nativeObj, blockSize, ksize, borderType);
    }

    /**
     * Calculates the minimal eigenvalue of gradient matrices for corner detection. The function is similar to
     * cornerEigenValsAndVecs but it calculates and stores only the minimal eigenvalue of the covariance matrix of
     * derivatives, that is, min(lambda_1, lambda_2) in terms of the formulae in the cornerEigenValsAndVecs description.
     *
     * @param src       Input single-channel 8-bit or floating-point image
     * @param dst       Image to store the minimal eigenvalues. It has the type CV_32FC1 and the same size as src
     * @param blockSize Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @param ksize     Aperture parameter for the Sobel operator
     * @throws NullPointerException if src, dst is null
     */
    public static void cornerMinEigenVal(Mat src, Mat dst, int blockSize, int ksize) {
        cornerMinEigenVal_1(src.nativeObj, dst.nativeObj, blockSize, ksize);
    }

    /**
     * Calculates the minimal eigenvalue of gradient matrices for corner detection. The function is similar to
     * cornerEigenValsAndVecs but it calculates and stores only the minimal eigenvalue of the covariance matrix of
     * derivatives, that is, min(lambda_1, lambda_2) in terms of the formulae in the cornerEigenValsAndVecs description.
     *
     * @param src       Input single-channel 8-bit or floating-point image
     * @param dst       Image to store the minimal eigenvalues. It has the type CV_32FC1 and the same size as src
     * @param blockSize Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @throws NullPointerException if src, dst is null
     */
    public static void cornerMinEigenVal(Mat src, Mat dst, int blockSize) {
        cornerMinEigenVal_2(src.nativeObj, dst.nativeObj, blockSize);
    }

    /**
     * Harris corner detector.
     *
     * @param src        Input single-channel 8-bit or floating-point image
     * @param dst        Image to store the Harris detector responses. It has the type CV_32FC1 and the same size as src
     * @param blockSize  Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @param ksize      Aperture parameter for the Sobel operator
     * @param k          Harris detector free parameter. See the formula above
     * @param borderType Pixel extrapolation method. See BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst is null
     */
    public static void cornerHarris(Mat src, Mat dst, int blockSize, int ksize, double k, int borderType) {
        cornerHarris_0(src.nativeObj, dst.nativeObj, blockSize, ksize, k, borderType);
    }

    /**
     * Harris corner detector.
     *
     * @param src       Input single-channel 8-bit or floating-point image
     * @param dst       Image to store the Harris detector responses. It has the type CV_32FC1 and the same size as src
     * @param blockSize Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @param ksize     Aperture parameter for the Sobel operator
     * @param k         Harris detector free parameter. See the formula above
     * @throws NullPointerException if src, dst is null
     */
    public static void cornerHarris(Mat src, Mat dst, int blockSize, int ksize, double k) {
        cornerHarris_1(src.nativeObj, dst.nativeObj, blockSize, ksize, k);
    }

    /**
     * Calculates eigenvalues and eigenvectors of image blocks for corner detection.
     *
     * @param src        Input single-channel 8-bit or floating-point image
     * @param dst        Image to store the results. It has the same size as src and the type CV_32FC(6)
     * @param blockSize  Neighborhood size (see details below)
     * @param ksize      Aperture parameter for the Sobel operator
     * @param borderType Pixel extrapolation method. See BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst is null
     */
    public static void cornerEigenValsAndVecs(Mat src, Mat dst, int blockSize, int ksize, int borderType) {
        cornerEigenValsAndVecs_0(src.nativeObj, dst.nativeObj, blockSize, ksize, borderType);
    }

    /**
     * Calculates eigenvalues and eigenvectors of image blocks for corner detection.
     *
     * @param src       Input single-channel 8-bit or floating-point image
     * @param dst       Image to store the results. It has the same size as src and the type CV_32FC(6)
     * @param blockSize Neighborhood size (see details below)
     * @param ksize     Aperture parameter for the Sobel operator
     * @throws NullPointerException if src, dst is null
     */
    public static void cornerEigenValsAndVecs(Mat src, Mat dst, int blockSize, int ksize) {
        cornerEigenValsAndVecs_1(src.nativeObj, dst.nativeObj, blockSize, ksize);
    }

    /**
     * Calculates a feature map for corner detection.
     *
     * @param src        Source single-channel 8-bit of floating-point image
     * @param dst        Output image that has the type CV_32F and the same size as src
     * @param ksize      %Aperture size of the Sobel
     * @param borderType Pixel extrapolation method. See BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst is null
     */
    public static void preCornerDetect(Mat src, Mat dst, int ksize, int borderType) {
        preCornerDetect_0(src.nativeObj, dst.nativeObj, ksize, borderType);
    }

    /**
     * Calculates a feature map for corner detection.
     *
     * @param src   Source single-channel 8-bit of floating-point image
     * @param dst   Output image that has the type CV_32F and the same size as src
     * @param ksize %Aperture size of the Sobel
     * @throws NullPointerException if src, dst is null
     */
    public static void preCornerDetect(Mat src, Mat dst, int ksize) {
        preCornerDetect_1(src.nativeObj, dst.nativeObj, ksize);
    }

    /**
     * Refines the corner locations.
     *
     * @param image    Input single-channel, 8-bit or float image
     * @param corners  Initial coordinates of the input corners and refined coordinates provided for output
     * @param winSize  Half of the side length of the search window. For example, if winSize=Size(5,5) , then a (5*2+1)
     *                 times (5*2+1) = 11 times 11 search window is used
     * @param zeroZone Half of the size of the dead region in the middle of the search zone over which the summation in
     *                 the formula below is not done. It is used some x to avoid possible singularities of the
     *                 autocorrelation matrix. The value of (-1,-1) indicates that there is no such a size
     * @param criteria Criteria for termination of the iterative process of corner refinement. That is, the process of
     *                 corner position refinement stops either after criteria.maxCount iterations or when the corner
     *                 position moves by less than criteria.epsilon on some iteration
     * @throws NullPointerException if image, corners, winSize, zeroZone, criteria is null
     */
    public static void cornerSubPix(Mat image, Mat corners, Size winSize, Size zeroZone, TermCriteria criteria) {
        cornerSubPix_0(
                image.nativeObj,
                corners.nativeObj,
                winSize.width,
                winSize.height,
                zeroZone.width,
                zeroZone.height,
                criteria.type,
                criteria.maxCount,
                criteria.epsilon);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform. The function implements the standard or
     * standard multi-scale Hough transform algorithm for line detection. See
     * &lt;https://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm&gt; for a good explanation of Hough transform.
     *
     * @param image       8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines       Output vector of lines. Each line is represented by a 2 or 3 element vector (rho, theta) or
     *                    (rho, theta, textrm{votes}), where rho is the distance from the coordinate origin (0,0)
     *                    (top-left corner of the image), theta is the line rotation angle in radians ( 0 sim
     *                    textrm{vertical line}, pi/2 sim textrm{horizontal line} ), and textrm{votes} is the value of
     *                    accumulator
     * @param rho         Distance resolution of the accumulator in pixels
     * @param theta       Angle resolution of the accumulator in radians
     * @param threshold   %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                    &gt;{@code threshold} )
     * @param srn         For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The
     *                    coarse accumulator distance resolution is rho and the accurate accumulator resolution is
     *                    rho/srn. If both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both these
     *                    parameters should be positive
     * @param stn         For the multi-scale Hough transform, it is a divisor for the distance resolution theta
     * @param min_theta   For standard and multi-scale Hough transform, minimum angle to check for lines. Must fall
     *                    between 0 and max_theta
     * @param max_theta   For standard and multi-scale Hough transform, an upper bound for the angle. Must fall between
     *                    min_theta and CV_PI. The actual maximum angle in the accumulator may be slightly less than
     *                    max_theta, depending on the parameters min_theta and theta
     * @param use_edgeval True if you want to use weighted Hough transform
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLines(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta,
            double max_theta,
            boolean use_edgeval) {
        HoughLines_0(
                image.nativeObj,
                lines.nativeObj,
                rho,
                theta,
                threshold,
                srn,
                stn,
                min_theta,
                max_theta,
                use_edgeval);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform. The function implements the standard or
     * standard multi-scale Hough transform algorithm for line detection. See
     * &lt;https://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm&gt; for a good explanation of Hough transform.
     *
     * @param image     8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines     Output vector of lines. Each line is represented by a 2 or 3 element vector (rho, theta) or
     *                  (rho, theta, textrm{votes}), where rho is the distance from the coordinate origin (0,0)
     *                  (top-left corner of the image), theta is the line rotation angle in radians ( 0 sim
     *                  textrm{vertical line}, pi/2 sim textrm{horizontal line} ), and textrm{votes} is the value of
     *                  accumulator
     * @param rho       Distance resolution of the accumulator in pixels
     * @param theta     Angle resolution of the accumulator in radians
     * @param threshold %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                  &gt;{@code threshold} )
     * @param srn       For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The coarse
     *                  accumulator distance resolution is rho and the accurate accumulator resolution is rho/srn. If
     *                  both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both these parameters
     *                  should be positive
     * @param stn       For the multi-scale Hough transform, it is a divisor for the distance resolution theta
     * @param min_theta For standard and multi-scale Hough transform, minimum angle to check for lines. Must fall
     *                  between 0 and max_theta
     * @param max_theta For standard and multi-scale Hough transform, an upper bound for the angle. Must fall between
     *                  min_theta and CV_PI. The actual maximum angle in the accumulator may be slightly less than
     *                  max_theta, depending on the parameters min_theta and theta
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLines(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta,
            double max_theta) {
        HoughLines_1(image.nativeObj, lines.nativeObj, rho, theta, threshold, srn, stn, min_theta, max_theta);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform. The function implements the standard or
     * standard multi-scale Hough transform algorithm for line detection. See
     * &lt;https://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm&gt; for a good explanation of Hough transform.
     *
     * @param image     8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines     Output vector of lines. Each line is represented by a 2 or 3 element vector (rho, theta) or
     *                  (rho, theta, textrm{votes}), where rho is the distance from the coordinate origin (0,0)
     *                  (top-left corner of the image), theta is the line rotation angle in radians ( 0 sim
     *                  textrm{vertical line}, pi/2 sim textrm{horizontal line} ), and textrm{votes} is the value of
     *                  accumulator
     * @param rho       Distance resolution of the accumulator in pixels
     * @param theta     Angle resolution of the accumulator in radians
     * @param threshold %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                  &gt;{@code threshold} )
     * @param srn       For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The coarse
     *                  accumulator distance resolution is rho and the accurate accumulator resolution is rho/srn. If
     *                  both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both these parameters
     *                  should be positive
     * @param stn       For the multi-scale Hough transform, it is a divisor for the distance resolution theta
     * @param min_theta For standard and multi-scale Hough transform, minimum angle to check for lines. Must fall
     *                  between 0 and max_theta
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLines(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta) {
        HoughLines_2(image.nativeObj, lines.nativeObj, rho, theta, threshold, srn, stn, min_theta);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform. The function implements the standard or
     * standard multi-scale Hough transform algorithm for line detection. See
     * &lt;https://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm&gt; for a good explanation of Hough transform.
     *
     * @param image     8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines     Output vector of lines. Each line is represented by a 2 or 3 element vector (rho, theta) or
     *                  (rho, theta, textrm{votes}), where rho is the distance from the coordinate origin (0,0)
     *                  (top-left corner of the image), theta is the line rotation angle in radians ( 0 sim
     *                  textrm{vertical line}, pi/2 sim textrm{horizontal line} ), and textrm{votes} is the value of
     *                  accumulator
     * @param rho       Distance resolution of the accumulator in pixels
     * @param theta     Angle resolution of the accumulator in radians
     * @param threshold %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                  &gt;{@code threshold} )
     * @param srn       For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The coarse
     *                  accumulator distance resolution is rho and the accurate accumulator resolution is rho/srn. If
     *                  both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both these parameters
     *                  should be positive
     * @param stn       For the multi-scale Hough transform, it is a divisor for the distance resolution theta
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLines(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn) {
        HoughLines_3(image.nativeObj, lines.nativeObj, rho, theta, threshold, srn, stn);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform. The function implements the standard or
     * standard multi-scale Hough transform algorithm for line detection. See
     * &lt;https://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm&gt; for a good explanation of Hough transform.
     *
     * @param image     8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines     Output vector of lines. Each line is represented by a 2 or 3 element vector (rho, theta) or
     *                  (rho, theta, textrm{votes}), where rho is the distance from the coordinate origin (0,0)
     *                  (top-left corner of the image), theta is the line rotation angle in radians ( 0 sim
     *                  textrm{vertical line}, pi/2 sim textrm{horizontal line} ), and textrm{votes} is the value of
     *                  accumulator
     * @param rho       Distance resolution of the accumulator in pixels
     * @param theta     Angle resolution of the accumulator in radians
     * @param threshold %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                  &gt;{@code threshold} )
     * @param srn       For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The coarse
     *                  accumulator distance resolution is rho and the accurate accumulator resolution is rho/srn. If
     *                  both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both these parameters
     *                  should be positive
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLines(Mat image, Mat lines, double rho, double theta, int threshold, double srn) {
        HoughLines_4(image.nativeObj, lines.nativeObj, rho, theta, threshold, srn);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform. The function implements the standard or
     * standard multi-scale Hough transform algorithm for line detection. See
     * &lt;https://homepages.inf.ed.ac.uk/rbf/HIPR2/hough.htm&gt; for a good explanation of Hough transform.
     *
     * @param image     8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines     Output vector of lines. Each line is represented by a 2 or 3 element vector (rho, theta) or
     *                  (rho, theta, textrm{votes}), where rho is the distance from the coordinate origin (0,0)
     *                  (top-left corner of the image), theta is the line rotation angle in radians ( 0 sim
     *                  textrm{vertical line}, pi/2 sim textrm{horizontal line} ), and textrm{votes} is the value of
     *                  accumulator
     * @param rho       Distance resolution of the accumulator in pixels
     * @param theta     Angle resolution of the accumulator in radians
     * @param threshold %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                  &gt;{@code threshold} )
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLines(Mat image, Mat lines, double rho, double theta, int threshold) {
        HoughLines_5(image.nativeObj, lines.nativeObj, rho, theta, threshold);
    }

    /**
     * Finds line segments in a binary image using the probabilistic Hough transform. The function implements the
     * probabilistic Hough transform algorithm for line detection, described in function parameters have been tuned for:
     * And this is the output of the above program in case of the probabilistic Hough transform:.
     *
     * @param image         8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines         Output vector of lines. Each line is represented by a 4-element vector (x_1, y_1, x_2, y_2)
     *                      , where (x_1,y_1) and (x_2, y_2) are the ending points of each detected line segment
     * @param rho           Distance resolution of the accumulator in pixels
     * @param theta         Angle resolution of the accumulator in radians
     * @param threshold     %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                      &gt;{@code threshold} )
     * @param minLineLength Minimum line length. Line segments shorter than that are rejected
     * @param maxLineGap    Maximum allowed gap between points on the same line to link them
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesP(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double minLineLength,
            double maxLineGap) {
        HoughLinesP_0(image.nativeObj, lines.nativeObj, rho, theta, threshold, minLineLength, maxLineGap);
    }

    /**
     * Finds line segments in a binary image using the probabilistic Hough transform. The function implements the
     * probabilistic Hough transform algorithm for line detection, described in function parameters have been tuned for:
     * And this is the output of the above program in case of the probabilistic Hough transform:.
     *
     * @param image         8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines         Output vector of lines. Each line is represented by a 4-element vector (x_1, y_1, x_2, y_2)
     *                      , where (x_1,y_1) and (x_2, y_2) are the ending points of each detected line segment
     * @param rho           Distance resolution of the accumulator in pixels
     * @param theta         Angle resolution of the accumulator in radians
     * @param threshold     %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                      &gt;{@code threshold} )
     * @param minLineLength Minimum line length. Line segments shorter than that are rejected
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesP(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double minLineLength) {
        HoughLinesP_1(image.nativeObj, lines.nativeObj, rho, theta, threshold, minLineLength);
    }

    /**
     * Finds line segments in a binary image using the probabilistic Hough transform. The function implements the
     * probabilistic Hough transform algorithm for line detection, described in function parameters have been tuned for:
     * And this is the output of the above program in case of the probabilistic Hough transform:.
     *
     * @param image     8-bit, single-channel binary source image. The image may be modified by the function
     * @param lines     Output vector of lines. Each line is represented by a 4-element vector (x_1, y_1, x_2, y_2) ,
     *                  where (x_1,y_1) and (x_2, y_2) are the ending points of each detected line segment
     * @param rho       Distance resolution of the accumulator in pixels
     * @param theta     Angle resolution of the accumulator in radians
     * @param threshold %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                  &gt;{@code threshold} )
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesP(Mat image, Mat lines, double rho, double theta, int threshold) {
        HoughLinesP_2(image.nativeObj, lines.nativeObj, rho, theta, threshold);
    }

    /**
     * Finds lines in a set of points using the standard Hough transform. The function finds lines in a set of points
     * using a modification of the Hough transform. INCLUDE:.
     *
     * @param point      Input vector of points. Each vector must be encoded as a Point vector (x,y). Type must be
     *                   CV_32FC2 or CV_32SC2
     * @param lines      Output vector of found lines. Each vector is encoded as a vector&lt;3-component double
     *                   vector&gt; (votes, rho, theta). The larger the value of 'votes', the higher the reliability of
     *                   the Hough line
     * @param lines_max  Max count of Hough lines
     * @param threshold  %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                   &gt;{@code threshold} )
     * @param min_rho    Minimum value for rho for the accumulator (Note: rho can be negative. The absolute value |rho|
     *                   is the distance of a line to the origin.)
     * @param max_rho    Maximum value for rho for the accumulator
     * @param rho_step   Distance resolution of the accumulator
     * @param min_theta  Minimum angle value of the accumulator in radians
     * @param max_theta  Upper bound for the angle value of the accumulator in radians. The actual maximum angle may be
     *                   slightly less than max_theta, depending on the parameters min_theta and theta_step
     * @param theta_step Angle resolution of the accumulator in radians
     * @throws NullPointerException if point, lines is null
     */
    public static void HoughLinesPointSet(
            Mat point,
            Mat lines,
            int lines_max,
            int threshold,
            double min_rho,
            double max_rho,
            double rho_step,
            double min_theta,
            double max_theta,
            double theta_step) {
        HoughLinesPointSet_0(
                point.nativeObj,
                lines.nativeObj,
                lines_max,
                threshold,
                min_rho,
                max_rho,
                rho_step,
                min_theta,
                max_theta,
                theta_step);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform. The function finds circles in a grayscale image
     * using a modification of the Hough transform. Note: Usually the function detects the centers of circles well.
     * However, it may fail to find correct radii. You can assist to the function by specifying the radius range (
     * minRadius and maxRadius ) if you know it. Or, in the case of HOUGH_GRADIENT method you may set maxRadius to a
     * negative number to return centers only without radius search, and find the correct radius using an additional
     * procedure. It also helps to smooth image a bit unless it's already soft. For example, GaussianBlur() with 7x7
     * kernel and 1.5x1.5 sigma or similar blurring may help.
     *
     * @param image     8-bit, single-channel, grayscale input image
     * @param circles   Output vector of found circles. Each vector is encoded as 3 or 4 element floating-point vector
     *                  (x, y, radius) or (x, y, radius, votes)
     * @param method    Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and
     *                  HOUGH_GRADIENT_ALT
     * @param dp        Inverse ratio of the accumulator resolution to the image resolution. For example, if dp=1 , the
     *                  accumulator has the same resolution as the input image. If dp=2 , the accumulator has half as
     *                  big width and height. For HOUGH_GRADIENT_ALT the recommended value is dp=1.5, unless some small
     *                  very circles need to be detected
     * @param minDist   Minimum distance between the centers of the detected circles. If the parameter is too small,
     *                  multiple neighbor circles may be falsely detected in addition to a true one. If it is too large,
     *                  some circles may be missed
     * @param param1    First method-specific parameter. In case of HOUGH_GRADIENT and HOUGH_GRADIENT_ALT, it is the
     *                  higher threshold of the two passed to the Canny edge detector (the lower one is twice smaller).
     *                  Note that HOUGH_GRADIENT_ALT uses Scharr algorithm to compute image derivatives, so the
     *                  threshold value should normally be higher, such as 300 or normally exposed and contrasty images
     * @param param2    Second method-specific parameter. In case of HOUGH_GRADIENT, it is the accumulator threshold for
     *                  the circle centers at the detection stage. The smaller it is, the more false circles may be
     *                  detected. Circles, corresponding to the larger accumulator values, will be returned first. In
     *                  the case of HOUGH_GRADIENT_ALT algorithm, this is the circle "perfectness" measure. The closer
     *                  it to 1, the better shaped circles algorithm selects. In most cases 0.9 should be fine. If you
     *                  want get better detection of small circles, you may decrease it to 0.85, 0.8 or even less. But
     *                  then also try to limit the search range [minRadius, maxRadius] to avoid many false circles
     * @param minRadius Minimum circle radius
     * @param maxRadius Maximum circle radius. If &lt;= 0, uses the maximum image dimension. If &lt; 0, HOUGH_GRADIENT
     *                  returns centers without finding the radius. HOUGH_GRADIENT_ALT always computes circle radiuses
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCircles(
            Mat image,
            Mat circles,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2,
            int minRadius,
            int maxRadius) {
        HoughCircles_0(image.nativeObj, circles.nativeObj, method, dp, minDist, param1, param2, minRadius, maxRadius);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform. The function finds circles in a grayscale image
     * using a modification of the Hough transform. Note: Usually the function detects the centers of circles well.
     * However, it may fail to find correct radii. You can assist to the function by specifying the radius range (
     * minRadius and maxRadius ) if you know it. Or, in the case of HOUGH_GRADIENT method you may set maxRadius to a
     * negative number to return centers only without radius search, and find the correct radius using an additional
     * procedure. It also helps to smooth image a bit unless it's already soft. For example, GaussianBlur() with 7x7
     * kernel and 1.5x1.5 sigma or similar blurring may help.
     *
     * @param image     8-bit, single-channel, grayscale input image
     * @param circles   Output vector of found circles. Each vector is encoded as 3 or 4 element floating-point vector
     *                  (x, y, radius) or (x, y, radius, votes)
     * @param method    Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and
     *                  HOUGH_GRADIENT_ALT
     * @param dp        Inverse ratio of the accumulator resolution to the image resolution. For example, if dp=1 , the
     *                  accumulator has the same resolution as the input image. If dp=2 , the accumulator has half as
     *                  big width and height. For HOUGH_GRADIENT_ALT the recommended value is dp=1.5, unless some small
     *                  very circles need to be detected
     * @param minDist   Minimum distance between the centers of the detected circles. If the parameter is too small,
     *                  multiple neighbor circles may be falsely detected in addition to a true one. If it is too large,
     *                  some circles may be missed
     * @param param1    First method-specific parameter. In case of HOUGH_GRADIENT and HOUGH_GRADIENT_ALT, it is the
     *                  higher threshold of the two passed to the Canny edge detector (the lower one is twice smaller).
     *                  Note that HOUGH_GRADIENT_ALT uses Scharr algorithm to compute image derivatives, so the
     *                  threshold value should normally be higher, such as 300 or normally exposed and contrasty images
     * @param param2    Second method-specific parameter. In case of HOUGH_GRADIENT, it is the accumulator threshold for
     *                  the circle centers at the detection stage. The smaller it is, the more false circles may be
     *                  detected. Circles, corresponding to the larger accumulator values, will be returned first. In
     *                  the case of HOUGH_GRADIENT_ALT algorithm, this is the circle "perfectness" measure. The closer
     *                  it to 1, the better shaped circles algorithm selects. In most cases 0.9 should be fine. If you
     *                  want get better detection of small circles, you may decrease it to 0.85, 0.8 or even less. But
     *                  then also try to limit the search range [minRadius, maxRadius] to avoid many false circles
     * @param minRadius Minimum circle radius
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCircles(
            Mat image,
            Mat circles,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2,
            int minRadius) {
        HoughCircles_1(image.nativeObj, circles.nativeObj, method, dp, minDist, param1, param2, minRadius);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform. The function finds circles in a grayscale image
     * using a modification of the Hough transform. Note: Usually the function detects the centers of circles well.
     * However, it may fail to find correct radii. You can assist to the function by specifying the radius range (
     * minRadius and maxRadius ) if you know it. Or, in the case of HOUGH_GRADIENT method you may set maxRadius to a
     * negative number to return centers only without radius search, and find the correct radius using an additional
     * procedure. It also helps to smooth image a bit unless it's already soft. For example, GaussianBlur() with 7x7
     * kernel and 1.5x1.5 sigma or similar blurring may help.
     *
     * @param image   8-bit, single-channel, grayscale input image
     * @param circles Output vector of found circles. Each vector is encoded as 3 or 4 element floating-point vector (x,
     *                y, radius) or (x, y, radius, votes)
     * @param method  Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and HOUGH_GRADIENT_ALT
     * @param dp      Inverse ratio of the accumulator resolution to the image resolution. For example, if dp=1 , the
     *                accumulator has the same resolution as the input image. If dp=2 , the accumulator has half as big
     *                width and height. For HOUGH_GRADIENT_ALT the recommended value is dp=1.5, unless some small very
     *                circles need to be detected
     * @param minDist Minimum distance between the centers of the detected circles. If the parameter is too small,
     *                multiple neighbor circles may be falsely detected in addition to a true one. If it is too large,
     *                some circles may be missed
     * @param param1  First method-specific parameter. In case of HOUGH_GRADIENT and HOUGH_GRADIENT_ALT, it is the
     *                higher threshold of the two passed to the Canny edge detector (the lower one is twice smaller).
     *                Note that HOUGH_GRADIENT_ALT uses Scharr algorithm to compute image derivatives, so the threshold
     *                value should normally be higher, such as 300 or normally exposed and contrasty images
     * @param param2  Second method-specific parameter. In case of HOUGH_GRADIENT, it is the accumulator threshold for
     *                the circle centers at the detection stage. The smaller it is, the more false circles may be
     *                detected. Circles, corresponding to the larger accumulator values, will be returned first. In the
     *                case of HOUGH_GRADIENT_ALT algorithm, this is the circle "perfectness" measure. The closer it to
     *                1, the better shaped circles algorithm selects. In most cases 0.9 should be fine. If you want get
     *                better detection of small circles, you may decrease it to 0.85, 0.8 or even less. But then also
     *                try to limit the search range [minRadius, maxRadius] to avoid many false circles
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCircles(
            Mat image,
            Mat circles,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2) {
        HoughCircles_2(image.nativeObj, circles.nativeObj, method, dp, minDist, param1, param2);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform. The function finds circles in a grayscale image
     * using a modification of the Hough transform. Note: Usually the function detects the centers of circles well.
     * However, it may fail to find correct radii. You can assist to the function by specifying the radius range (
     * minRadius and maxRadius ) if you know it. Or, in the case of HOUGH_GRADIENT method you may set maxRadius to a
     * negative number to return centers only without radius search, and find the correct radius using an additional
     * procedure. It also helps to smooth image a bit unless it's already soft. For example, GaussianBlur() with 7x7
     * kernel and 1.5x1.5 sigma or similar blurring may help.
     *
     * @param image   8-bit, single-channel, grayscale input image
     * @param circles Output vector of found circles. Each vector is encoded as 3 or 4 element floating-point vector (x,
     *                y, radius) or (x, y, radius, votes)
     * @param method  Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and HOUGH_GRADIENT_ALT
     * @param dp      Inverse ratio of the accumulator resolution to the image resolution. For example, if dp=1 , the
     *                accumulator has the same resolution as the input image. If dp=2 , the accumulator has half as big
     *                width and height. For HOUGH_GRADIENT_ALT the recommended value is dp=1.5, unless some small very
     *                circles need to be detected
     * @param minDist Minimum distance between the centers of the detected circles. If the parameter is too small,
     *                multiple neighbor circles may be falsely detected in addition to a true one. If it is too large,
     *                some circles may be missed
     * @param param1  First method-specific parameter. In case of HOUGH_GRADIENT and HOUGH_GRADIENT_ALT, it is the
     *                higher threshold of the two passed to the Canny edge detector (the lower one is twice smaller).
     *                Note that HOUGH_GRADIENT_ALT uses Scharr algorithm to compute image derivatives, so the threshold
     *                value should normally be higher, such as 300 or normally exposed and contrasty images
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCircles(Mat image, Mat circles, int method, double dp, double minDist, double param1) {
        HoughCircles_3(image.nativeObj, circles.nativeObj, method, dp, minDist, param1);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform. The function finds circles in a grayscale image
     * using a modification of the Hough transform. Note: Usually the function detects the centers of circles well.
     * However, it may fail to find correct radii. You can assist to the function by specifying the radius range (
     * minRadius and maxRadius ) if you know it. Or, in the case of HOUGH_GRADIENT method you may set maxRadius to a
     * negative number to return centers only without radius search, and find the correct radius using an additional
     * procedure. It also helps to smooth image a bit unless it's already soft. For example, GaussianBlur() with 7x7
     * kernel and 1.5x1.5 sigma or similar blurring may help.
     *
     * @param image   8-bit, single-channel, grayscale input image
     * @param circles Output vector of found circles. Each vector is encoded as 3 or 4 element floating-point vector (x,
     *                y, radius) or (x, y, radius, votes)
     * @param method  Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and HOUGH_GRADIENT_ALT
     * @param dp      Inverse ratio of the accumulator resolution to the image resolution. For example, if dp=1 , the
     *                accumulator has the same resolution as the input image. If dp=2 , the accumulator has half as big
     *                width and height. For HOUGH_GRADIENT_ALT the recommended value is dp=1.5, unless some small very
     *                circles need to be detected
     * @param minDist Minimum distance between the centers of the detected circles. If the parameter is too small,
     *                multiple neighbor circles may be falsely detected in addition to a true one. If it is too large,
     *                some circles may be missed
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCircles(Mat image, Mat circles, int method, double dp, double minDist) {
        HoughCircles_4(image.nativeObj, circles.nativeObj, method, dp, minDist);
    }

    /**
     * Erodes an image by using a specific structuring element.
     *
     * @param src         input image; the number of channels can be arbitrary, but the depth should be one of CV_8U,
     *                    CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst         output image of the same size and type as src
     * @param kernel      structuring element used for erosion; if , a 3 x 3 rectangular structuring element is used.
     *                    Kernel can be created using getStructuringElement
     * @param anchor      position of the anchor within the element; default value (-1, -1) means that the anchor is at
     *                    the element center
     * @param iterations  number of times erosion is applied
     * @param borderType  pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @param borderValue border value in case of a constant border
     * @throws NullPointerException if src, dst, kernel, anchor, borderValue is null
     */
    public static void erode(
            Mat src,
            Mat dst,
            Mat kernel,
            Point anchor,
            int iterations,
            int borderType,
            Scalar borderValue) {
        erode_0(
                src.nativeObj,
                dst.nativeObj,
                kernel.nativeObj,
                anchor.x,
                anchor.y,
                iterations,
                borderType,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3]);
    }

    /**
     * Erodes an image by using a specific structuring element.
     *
     * @param src        input image; the number of channels can be arbitrary, but the depth should be one of CV_8U,
     *                   CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        output image of the same size and type as src
     * @param kernel     structuring element used for erosion; if , a 3 x 3 rectangular structuring element is used.
     *                   Kernel can be created using getStructuringElement
     * @param anchor     position of the anchor within the element; default value (-1, -1) means that the anchor is at
     *                   the element center
     * @param iterations number of times erosion is applied
     * @param borderType pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void erode(Mat src, Mat dst, Mat kernel, Point anchor, int iterations, int borderType) {
        erode_1(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y, iterations, borderType);
    }

    /**
     * Erodes an image by using a specific structuring element.
     *
     * @param src        input image; the number of channels can be arbitrary, but the depth should be one of CV_8U,
     *                   CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        output image of the same size and type as src
     * @param kernel     structuring element used for erosion; if , a 3 x 3 rectangular structuring element is used.
     *                   Kernel can be created using getStructuringElement
     * @param anchor     position of the anchor within the element; default value (-1, -1) means that the anchor is at
     *                   the element center
     * @param iterations number of times erosion is applied
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void erode(Mat src, Mat dst, Mat kernel, Point anchor, int iterations) {
        erode_2(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y, iterations);
    }

    /**
     * Erodes an image by using a specific structuring element.
     *
     * @param src    input image; the number of channels can be arbitrary, but the depth should be one of CV_8U, CV_16U,
     *               CV_16S, CV_32F or CV_64F
     * @param dst    output image of the same size and type as src
     * @param kernel structuring element used for erosion; if , a 3 x 3 rectangular structuring element is used. Kernel
     *               can be created using getStructuringElement
     * @param anchor position of the anchor within the element; default value (-1, -1) means that the anchor is at the
     *               element center
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void erode(Mat src, Mat dst, Mat kernel, Point anchor) {
        erode_3(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y);
    }

    /**
     * Erodes an image by using a specific structuring element.
     *
     * @param src    input image; the number of channels can be arbitrary, but the depth should be one of CV_8U, CV_16U,
     *               CV_16S, CV_32F or CV_64F
     * @param dst    output image of the same size and type as src
     * @param kernel structuring element used for erosion; if , a 3 x 3 rectangular structuring element is used. Kernel
     *               can be created using getStructuringElement
     * @throws NullPointerException if src, dst, kernel is null
     */
    public static void erode(Mat src, Mat dst, Mat kernel) {
        erode_4(src.nativeObj, dst.nativeObj, kernel.nativeObj);
    }

    /**
     * Dilates an image by using a specific structuring element.
     *
     * @param src         input image; the number of channels can be arbitrary, but the depth should be one of CV_8U,
     *                    CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst         output image of the same size and type as src
     * @param kernel      structuring element used for dilation; if , a 3 x 3 rectangular structuring element is used.
     *                    Kernel can be created using getStructuringElement
     * @param anchor      position of the anchor within the element; default value (-1, -1) means that the anchor is at
     *                    the element center
     * @param iterations  number of times dilation is applied
     * @param borderType  pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @param borderValue border value in case of a constant border
     * @throws NullPointerException if src, dst, kernel, anchor, borderValue is null
     */
    public static void dilate(
            Mat src,
            Mat dst,
            Mat kernel,
            Point anchor,
            int iterations,
            int borderType,
            Scalar borderValue) {
        dilate_0(
                src.nativeObj,
                dst.nativeObj,
                kernel.nativeObj,
                anchor.x,
                anchor.y,
                iterations,
                borderType,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3]);
    }

    /**
     * Dilates an image by using a specific structuring element.
     *
     * @param src        input image; the number of channels can be arbitrary, but the depth should be one of CV_8U,
     *                   CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        output image of the same size and type as src
     * @param kernel     structuring element used for dilation; if , a 3 x 3 rectangular structuring element is used.
     *                   Kernel can be created using getStructuringElement
     * @param anchor     position of the anchor within the element; default value (-1, -1) means that the anchor is at
     *                   the element center
     * @param iterations number of times dilation is applied
     * @param borderType pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void dilate(Mat src, Mat dst, Mat kernel, Point anchor, int iterations, int borderType) {
        dilate_1(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y, iterations, borderType);
    }

    /**
     * Dilates an image by using a specific structuring element.
     *
     * @param src        input image; the number of channels can be arbitrary, but the depth should be one of CV_8U,
     *                   CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        output image of the same size and type as src
     * @param kernel     structuring element used for dilation; if , a 3 x 3 rectangular structuring element is used.
     *                   Kernel can be created using getStructuringElement
     * @param anchor     position of the anchor within the element; default value (-1, -1) means that the anchor is at
     *                   the element center
     * @param iterations number of times dilation is applied
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void dilate(Mat src, Mat dst, Mat kernel, Point anchor, int iterations) {
        dilate_2(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y, iterations);
    }

    /**
     * Dilates an image by using a specific structuring element.
     *
     * @param src    input image; the number of channels can be arbitrary, but the depth should be one of CV_8U, CV_16U,
     *               CV_16S, CV_32F or CV_64F
     * @param dst    output image of the same size and type as src
     * @param kernel structuring element used for dilation; if , a 3 x 3 rectangular structuring element is used. Kernel
     *               can be created using getStructuringElement
     * @param anchor position of the anchor within the element; default value (-1, -1) means that the anchor is at the
     *               element center
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void dilate(Mat src, Mat dst, Mat kernel, Point anchor) {
        dilate_3(src.nativeObj, dst.nativeObj, kernel.nativeObj, anchor.x, anchor.y);
    }

    /**
     * Dilates an image by using a specific structuring element.
     *
     * @param src    input image; the number of channels can be arbitrary, but the depth should be one of CV_8U, CV_16U,
     *               CV_16S, CV_32F or CV_64F
     * @param dst    output image of the same size and type as src
     * @param kernel structuring element used for dilation; if , a 3 x 3 rectangular structuring element is used. Kernel
     *               can be created using getStructuringElement
     * @throws NullPointerException if src, dst, kernel is null
     */
    public static void dilate(Mat src, Mat dst, Mat kernel) {
        dilate_4(src.nativeObj, dst.nativeObj, kernel.nativeObj);
    }

    /**
     * Performs advanced morphological transformations. The function morphologyEx can perform advanced morphological
     * transformations using an erosion and dilation as basic operations. Any of the operations can be done in-place. In
     * case of multi-channel images, each channel is processed independently.
     *
     * @param src         Source image. The number of channels can be arbitrary. The depth should be one of CV_8U,
     *                    CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst         Destination image of the same size and type as source image
     * @param op          Type of a morphological operation, see MorphTypes
     * @param kernel      Structuring element. It can be created using getStructuringElement
     * @param anchor      Anchor position with the kernel. Negative values mean that the anchor is at the kernel center
     * @param iterations  Number of times erosion and dilation are applied
     * @param borderType  Pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @param borderValue Border value in case of a constant border. The default value has a special meaning
     * @throws NullPointerException if src, dst, kernel, anchor, borderValue is null
     */
    public static void morphologyEx(
            Mat src,
            Mat dst,
            int op,
            Mat kernel,
            Point anchor,
            int iterations,
            int borderType,
            Scalar borderValue) {
        morphologyEx_0(
                src.nativeObj,
                dst.nativeObj,
                op,
                kernel.nativeObj,
                anchor.x,
                anchor.y,
                iterations,
                borderType,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3]);
    }

    /**
     * Performs advanced morphological transformations. The function morphologyEx can perform advanced morphological
     * transformations using an erosion and dilation as basic operations. Any of the operations can be done in-place. In
     * case of multi-channel images, each channel is processed independently.
     *
     * @param src        Source image. The number of channels can be arbitrary. The depth should be one of CV_8U,
     *                   CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        Destination image of the same size and type as source image
     * @param op         Type of a morphological operation, see MorphTypes
     * @param kernel     Structuring element. It can be created using getStructuringElement
     * @param anchor     Anchor position with the kernel. Negative values mean that the anchor is at the kernel center
     * @param iterations Number of times erosion and dilation are applied
     * @param borderType Pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void morphologyEx(
            Mat src,
            Mat dst,
            int op,
            Mat kernel,
            Point anchor,
            int iterations,
            int borderType) {
        morphologyEx_1(src.nativeObj, dst.nativeObj, op, kernel.nativeObj, anchor.x, anchor.y, iterations, borderType);
    }

    /**
     * Performs advanced morphological transformations. The function morphologyEx can perform advanced morphological
     * transformations using an erosion and dilation as basic operations. Any of the operations can be done in-place. In
     * case of multi-channel images, each channel is processed independently.
     *
     * @param src        Source image. The number of channels can be arbitrary. The depth should be one of CV_8U,
     *                   CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst        Destination image of the same size and type as source image
     * @param op         Type of a morphological operation, see MorphTypes
     * @param kernel     Structuring element. It can be created using getStructuringElement
     * @param anchor     Anchor position with the kernel. Negative values mean that the anchor is at the kernel center
     * @param iterations Number of times erosion and dilation are applied
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void morphologyEx(Mat src, Mat dst, int op, Mat kernel, Point anchor, int iterations) {
        morphologyEx_2(src.nativeObj, dst.nativeObj, op, kernel.nativeObj, anchor.x, anchor.y, iterations);
    }

    /**
     * Performs advanced morphological transformations. The function morphologyEx can perform advanced morphological
     * transformations using an erosion and dilation as basic operations. Any of the operations can be done in-place. In
     * case of multi-channel images, each channel is processed independently.
     *
     * @param src    Source image. The number of channels can be arbitrary. The depth should be one of CV_8U, CV_16U,
     *               CV_16S, CV_32F or CV_64F
     * @param dst    Destination image of the same size and type as source image
     * @param op     Type of a morphological operation, see MorphTypes
     * @param kernel Structuring element. It can be created using getStructuringElement
     * @param anchor Anchor position with the kernel. Negative values mean that the anchor is at the kernel center
     * @throws NullPointerException if src, dst, kernel, anchor is null
     */
    public static void morphologyEx(Mat src, Mat dst, int op, Mat kernel, Point anchor) {
        morphologyEx_3(src.nativeObj, dst.nativeObj, op, kernel.nativeObj, anchor.x, anchor.y);
    }

    /**
     * Performs advanced morphological transformations. The function morphologyEx can perform advanced morphological
     * transformations using an erosion and dilation as basic operations. Any of the operations can be done in-place. In
     * case of multi-channel images, each channel is processed independently.
     *
     * @param src    Source image. The number of channels can be arbitrary. The depth should be one of CV_8U, CV_16U,
     *               CV_16S, CV_32F or CV_64F
     * @param dst    Destination image of the same size and type as source image
     * @param op     Type of a morphological operation, see MorphTypes
     * @param kernel Structuring element. It can be created using getStructuringElement
     * @throws NullPointerException if src, dst, kernel is null
     */
    public static void morphologyEx(Mat src, Mat dst, int op, Mat kernel) {
        morphologyEx_4(src.nativeObj, dst.nativeObj, op, kernel.nativeObj);
    }

    /**
     * Resizes an image. The function resize resizes the image src down to or up to the specified size. Note that the
     * initial dst type or size are not taken into account. Instead, the size and type are derived from the
     * src,dsize,fx, and fy. If you want to resize src so that it fits the pre-created dst, you may call the function as
     * follows: // explicitly specify dsize=dst.size(); fx and fy will be computed from that. resize(src, dst,
     * dst.size(), 0, 0, interpolation); If you want to decimate the image by factor of 2 in each direction, you can
     * call the function this way: // specify fx and fy and let the function compute the destination image size.
     * resize(src, dst, Size(), 0.5, 0.5, interpolation); To shrink an image, it will generally look best with
     * INTER_AREA interpolation, whereas to enlarge an image, it will generally look best with INTER_CUBIC (slow) or
     * INTER_LINEAR (faster but still looks OK).
     *
     * @param src           input image
     * @param dst           output image; it has the size dsize (when it is non-zero) or the size computed from
     *                      src.size(), fx, and fy; the type of dst is the same as of src
     * @param dsize         output image size; if it equals zero (None in Python), it is computed as: {@code dsize =
     *     Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or both fx and fy must be non-zero
     * @param fx            scale factor along the horizontal axis; when it equals 0, it is computed as {@code
     *     (double)dsize.width/src.cols}
     * @param fy            scale factor along the vertical axis; when it equals 0, it is computed as {@code
     *     (double)dsize.height/src.rows}
     * @param interpolation interpolation method, see InterpolationFlags
     * @throws NullPointerException if src, dst, dsize is null
     */
    public static void resize(Mat src, Mat dst, Size dsize, double fx, double fy, int interpolation) {
        resize_0(src.nativeObj, dst.nativeObj, dsize.width, dsize.height, fx, fy, interpolation);
    }

    /**
     * Resizes an image. The function resize resizes the image src down to or up to the specified size. Note that the
     * initial dst type or size are not taken into account. Instead, the size and type are derived from the
     * src,dsize,fx, and fy. If you want to resize src so that it fits the pre-created dst, you may call the function as
     * follows: // explicitly specify dsize=dst.size(); fx and fy will be computed from that. resize(src, dst,
     * dst.size(), 0, 0, interpolation); If you want to decimate the image by factor of 2 in each direction, you can
     * call the function this way: // specify fx and fy and let the function compute the destination image size.
     * resize(src, dst, Size(), 0.5, 0.5, interpolation); To shrink an image, it will generally look best with
     * INTER_AREA interpolation, whereas to enlarge an image, it will generally look best with INTER_CUBIC (slow) or
     * INTER_LINEAR (faster but still looks OK).
     *
     * @param src   input image
     * @param dst   output image; it has the size dsize (when it is non-zero) or the size computed from src.size(), fx,
     *              and fy; the type of dst is the same as of src
     * @param dsize output image size; if it equals zero (None in Python), it is computed as: {@code dsize =
     *     Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or both fx and fy must be non-zero
     * @param fx    scale factor along the horizontal axis; when it equals 0, it is computed as {@code
     *     (double)dsize.width/src.cols}
     * @param fy    scale factor along the vertical axis; when it equals 0, it is computed as {@code
     *     (double)dsize.height/src.rows}
     * @throws NullPointerException if src, dst, dsize is null
     */
    public static void resize(Mat src, Mat dst, Size dsize, double fx, double fy) {
        resize_1(src.nativeObj, dst.nativeObj, dsize.width, dsize.height, fx, fy);
    }

    /**
     * Resizes an image. The function resize resizes the image src down to or up to the specified size. Note that the
     * initial dst type or size are not taken into account. Instead, the size and type are derived from the
     * src,dsize,fx, and fy. If you want to resize src so that it fits the pre-created dst, you may call the function as
     * follows: // explicitly specify dsize=dst.size(); fx and fy will be computed from that. resize(src, dst,
     * dst.size(), 0, 0, interpolation); If you want to decimate the image by factor of 2 in each direction, you can
     * call the function this way: // specify fx and fy and let the function compute the destination image size.
     * resize(src, dst, Size(), 0.5, 0.5, interpolation); To shrink an image, it will generally look best with
     * INTER_AREA interpolation, whereas to enlarge an image, it will generally look best with INTER_CUBIC (slow) or
     * INTER_LINEAR (faster but still looks OK).
     *
     * @param src   input image
     * @param dst   output image; it has the size dsize (when it is non-zero) or the size computed from src.size(), fx,
     *              and fy; the type of dst is the same as of src
     * @param dsize output image size; if it equals zero (None in Python), it is computed as: {@code dsize =
     *     Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or both fx and fy must be non-zero
     * @param fx    scale factor along the horizontal axis; when it equals 0, it is computed as {@code
     *     (double)dsize.width/src.cols}
     * @throws NullPointerException if src, dst, dsize is null
     */
    public static void resize(Mat src, Mat dst, Size dsize, double fx) {
        resize_2(src.nativeObj, dst.nativeObj, dsize.width, dsize.height, fx);
    }

    /**
     * Resizes an image. The function resize resizes the image src down to or up to the specified size. Note that the
     * initial dst type or size are not taken into account. Instead, the size and type are derived from the
     * src,dsize,fx, and fy. If you want to resize src so that it fits the pre-created dst, you may call the function as
     * follows: // explicitly specify dsize=dst.size(); fx and fy will be computed from that. resize(src, dst,
     * dst.size(), 0, 0, interpolation); If you want to decimate the image by factor of 2 in each direction, you can
     * call the function this way: // specify fx and fy and let the function compute the destination image size.
     * resize(src, dst, Size(), 0.5, 0.5, interpolation); To shrink an image, it will generally look best with
     * INTER_AREA interpolation, whereas to enlarge an image, it will generally look best with INTER_CUBIC (slow) or
     * INTER_LINEAR (faster but still looks OK).
     *
     * @param src   input image
     * @param dst   output image; it has the size dsize (when it is non-zero) or the size computed from src.size(), fx,
     *              and fy; the type of dst is the same as of src
     * @param dsize output image size; if it equals zero (None in Python), it is computed as: {@code dsize =
     *     Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or both fx and fy must be non-zero
     * @throws NullPointerException if src, dst, dsize is null
     */
    public static void resize(Mat src, Mat dst, Size dsize) {
        resize_3(src.nativeObj, dst.nativeObj, dsize.width, dsize.height);
    }

    /**
     * Applies an affine transformation to an image.
     *
     * @param src         input image
     * @param dst         output image that has the size dsize and the same type as src
     * @param M           2 x 3 transformation matrix
     * @param dsize       size of the output image
     * @param flags       combination of interpolation methods (see InterpolationFlags) and the optional flag
     *                    WARP_INVERSE_MAP that means that M is the inverse transformation ( {@code dst} to {@code src}
     *                    )
     * @param borderMode  pixel extrapolation method (see BorderTypes); when borderMode=BORDER_TRANSPARENT, it means
     *                    that the pixels in the destination image corresponding to the "outliers" in the source image
     *                    are not modified by the function
     * @param borderValue value used in case of a constant border; by default, it is 0
     * @param hint        Implementation modification flags. Set ALGO_HINT_APPROX to use FP16 precision (if available)
     *                    for linear calculation for faster speed. See AlgorithmHint
     * @throws NullPointerException if src, dst, M, dsize, borderValue is null
     */
    public static void warpAffine(
            Mat src,
            Mat dst,
            Mat M,
            Size dsize,
            int flags,
            int borderMode,
            Scalar borderValue,
            int hint) {
        warpAffine_0(
                src.nativeObj,
                dst.nativeObj,
                M.nativeObj,
                dsize.width,
                dsize.height,
                flags,
                borderMode,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3],
                hint);
    }

    /**
     * Applies an affine transformation to an image.
     *
     * @param src         input image
     * @param dst         output image that has the size dsize and the same type as src
     * @param M           2 x 3 transformation matrix
     * @param dsize       size of the output image
     * @param flags       combination of interpolation methods (see InterpolationFlags) and the optional flag
     *                    WARP_INVERSE_MAP that means that M is the inverse transformation ( {@code dst} to {@code src}
     *                    )
     * @param borderMode  pixel extrapolation method (see BorderTypes); when borderMode=BORDER_TRANSPARENT, it means
     *                    that the pixels in the destination image corresponding to the "outliers" in the source image
     *                    are not modified by the function
     * @param borderValue value used in case of a constant border; by default, it is 0
     * @throws NullPointerException if src, dst, M, dsize, borderValue is null
     */
    public static void warpAffine(Mat src, Mat dst, Mat M, Size dsize, int flags, int borderMode, Scalar borderValue) {
        warpAffine_1(
                src.nativeObj,
                dst.nativeObj,
                M.nativeObj,
                dsize.width,
                dsize.height,
                flags,
                borderMode,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3]);
    }

    /**
     * Applies an affine transformation to an image.
     *
     * @param src        input image
     * @param dst        output image that has the size dsize and the same type as src
     * @param M          2 x 3 transformation matrix
     * @param dsize      size of the output image
     * @param flags      combination of interpolation methods (see InterpolationFlags) and the optional flag
     *                   WARP_INVERSE_MAP that means that M is the inverse transformation ( {@code dst} to {@code src} )
     * @param borderMode pixel extrapolation method (see BorderTypes); when borderMode=BORDER_TRANSPARENT, it means that
     *                   the pixels in the destination image corresponding to the "outliers" in the source image are not
     *                   modified by the function
     * @throws NullPointerException if src, dst, M, dsize is null
     */
    public static void warpAffine(Mat src, Mat dst, Mat M, Size dsize, int flags, int borderMode) {
        warpAffine_2(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height, flags, borderMode);
    }

    /**
     * Applies an affine transformation to an image.
     *
     * @param src   input image
     * @param dst   output image that has the size dsize and the same type as src
     * @param M     2 x 3 transformation matrix
     * @param dsize size of the output image
     * @param flags combination of interpolation methods (see InterpolationFlags) and the optional flag WARP_INVERSE_MAP
     *              that means that M is the inverse transformation ( {@code dst} to {@code src} )
     * @throws NullPointerException if src, dst, M, dsize is null
     */
    public static void warpAffine(Mat src, Mat dst, Mat M, Size dsize, int flags) {
        warpAffine_3(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height, flags);
    }

    /**
     * Applies an affine transformation to an image.
     *
     * @param src   input image
     * @param dst   output image that has the size dsize and the same type as src
     * @param M     2 x 3 transformation matrix
     * @param dsize size of the output image
     * @throws NullPointerException if src, dst, M, dsize is null
     */
    public static void warpAffine(Mat src, Mat dst, Mat M, Size dsize) {
        warpAffine_4(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height);
    }

    /**
     * Applies a perspective transformation to an image.
     *
     * @param src         input image
     * @param dst         output image that has the size dsize and the same type as src
     * @param M           3 x 3 transformation matrix
     * @param dsize       size of the output image
     * @param flags       combination of interpolation methods (INTER_LINEAR or INTER_NEAREST) and the optional flag
     *                    WARP_INVERSE_MAP, that sets M as the inverse transformation ( {@code dst} to {@code src} )
     * @param borderMode  pixel extrapolation method (BORDER_CONSTANT or BORDER_REPLICATE)
     * @param borderValue value used in case of a constant border; by default, it equals 0
     * @param hint        Implementation modification flags. Set ALGO_HINT_APPROX to use FP16 precision (if available)
     *                    for linear calculation for faster speed. See AlgorithmHint
     * @throws NullPointerException if src, dst, M, dsize, borderValue is null
     */
    public static void warpPerspective(
            Mat src,
            Mat dst,
            Mat M,
            Size dsize,
            int flags,
            int borderMode,
            Scalar borderValue,
            int hint) {
        warpPerspective_0(
                src.nativeObj,
                dst.nativeObj,
                M.nativeObj,
                dsize.width,
                dsize.height,
                flags,
                borderMode,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3],
                hint);
    }

    /**
     * Applies a perspective transformation to an image.
     *
     * @param src         input image
     * @param dst         output image that has the size dsize and the same type as src
     * @param M           3 x 3 transformation matrix
     * @param dsize       size of the output image
     * @param flags       combination of interpolation methods (INTER_LINEAR or INTER_NEAREST) and the optional flag
     *                    WARP_INVERSE_MAP, that sets M as the inverse transformation ( {@code dst} to {@code src} )
     * @param borderMode  pixel extrapolation method (BORDER_CONSTANT or BORDER_REPLICATE)
     * @param borderValue value used in case of a constant border; by default, it equals 0
     * @throws NullPointerException if src, dst, M, dsize, borderValue is null
     */
    public static void warpPerspective(
            Mat src,
            Mat dst,
            Mat M,
            Size dsize,
            int flags,
            int borderMode,
            Scalar borderValue) {
        warpPerspective_1(
                src.nativeObj,
                dst.nativeObj,
                M.nativeObj,
                dsize.width,
                dsize.height,
                flags,
                borderMode,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3]);
    }

    /**
     * Applies a perspective transformation to an image.
     *
     * @param src        input image
     * @param dst        output image that has the size dsize and the same type as src
     * @param M          3 x 3 transformation matrix
     * @param dsize      size of the output image
     * @param flags      combination of interpolation methods (INTER_LINEAR or INTER_NEAREST) and the optional flag
     *                   WARP_INVERSE_MAP, that sets M as the inverse transformation ( {@code dst} to {@code src} )
     * @param borderMode pixel extrapolation method (BORDER_CONSTANT or BORDER_REPLICATE)
     * @throws NullPointerException if src, dst, M, dsize is null
     */
    public static void warpPerspective(Mat src, Mat dst, Mat M, Size dsize, int flags, int borderMode) {
        warpPerspective_2(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height, flags, borderMode);
    }

    /**
     * Applies a perspective transformation to an image.
     *
     * @param src   input image
     * @param dst   output image that has the size dsize and the same type as src
     * @param M     3 x 3 transformation matrix
     * @param dsize size of the output image
     * @param flags combination of interpolation methods (INTER_LINEAR or INTER_NEAREST) and the optional flag
     *              WARP_INVERSE_MAP, that sets M as the inverse transformation ( {@code dst} to {@code src} )
     * @throws NullPointerException if src, dst, M, dsize is null
     */
    public static void warpPerspective(Mat src, Mat dst, Mat M, Size dsize, int flags) {
        warpPerspective_3(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height, flags);
    }

    /**
     * Applies a perspective transformation to an image.
     *
     * @param src   input image
     * @param dst   output image that has the size dsize and the same type as src
     * @param M     3 x 3 transformation matrix
     * @param dsize size of the output image
     * @throws NullPointerException if src, dst, M, dsize is null
     */
    public static void warpPerspective(Mat src, Mat dst, Mat M, Size dsize) {
        warpPerspective_4(src.nativeObj, dst.nativeObj, M.nativeObj, dsize.width, dsize.height);
    }

    /**
     * Applies a generic geometrical transformation to an image.
     *
     * @param src           Source image
     * @param dst           Destination image. It has the same size as map1 and the same type as src
     * @param map1          The first map of either (x,y) points or just x values having the type CV_16SC2 , CV_32FC1,
     *                      or CV_32FC2. See convertMaps for details on converting a floating point representation to
     *                      fixed-point
     * @param map2          The second map of y values having the type CV_16UC1, CV_32FC1, or none (empty map if map1 is
     *                      (x,y) points), respectively
     * @param interpolation Interpolation method (see InterpolationFlags). The methods INTER_AREA INTER_LINEAR_EXACT and
     *                      INTER_NEAREST_EXACT are not supported by this function. The extra flag WARP_RELATIVE_MAP can
     *                      be ORed to the interpolation method (e.g. INTER_LINEAR | WARP_RELATIVE_MAP)
     * @param borderMode    Pixel extrapolation method (see BorderTypes). When borderMode=BORDER_TRANSPARENT, it means
     *                      that the pixels in the destination image that corresponds to the "outliers" in the source
     *                      image are not modified by the function
     * @param borderValue   Value used in case of a constant border. By default, it is 0
     * @param hint          Implementation modification flags. Set ALGO_HINT_APPROX to use FP16 precision (if available)
     *                      for linear calculation for faster speed. See AlgorithmHint. Note: Due to current
     *                      implementation limitations the size of an input and output images should be less than
     *                      32767x32767
     * @throws NullPointerException if src, dst, map1, map2, borderValue is null
     */
    public static void remap(
            Mat src,
            Mat dst,
            Mat map1,
            Mat map2,
            int interpolation,
            int borderMode,
            Scalar borderValue,
            int hint) {
        remap_0(
                src.nativeObj,
                dst.nativeObj,
                map1.nativeObj,
                map2.nativeObj,
                interpolation,
                borderMode,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3],
                hint);
    }

    /**
     * Applies a generic geometrical transformation to an image.
     *
     * @param src           Source image
     * @param dst           Destination image. It has the same size as map1 and the same type as src
     * @param map1          The first map of either (x,y) points or just x values having the type CV_16SC2 , CV_32FC1,
     *                      or CV_32FC2. See convertMaps for details on converting a floating point representation to
     *                      fixed-point
     * @param map2          The second map of y values having the type CV_16UC1, CV_32FC1, or none (empty map if map1 is
     *                      (x,y) points), respectively
     * @param interpolation Interpolation method (see InterpolationFlags). The methods INTER_AREA INTER_LINEAR_EXACT and
     *                      INTER_NEAREST_EXACT are not supported by this function. The extra flag WARP_RELATIVE_MAP can
     *                      be ORed to the interpolation method (e.g. INTER_LINEAR | WARP_RELATIVE_MAP)
     * @param borderMode    Pixel extrapolation method (see BorderTypes). When borderMode=BORDER_TRANSPARENT, it means
     *                      that the pixels in the destination image that corresponds to the "outliers" in the source
     *                      image are not modified by the function
     * @param borderValue   Value used in case of a constant border. By default, it is 0
     * @throws NullPointerException if src, dst, map1, map2, borderValue is null
     */
    public static void remap(
            Mat src,
            Mat dst,
            Mat map1,
            Mat map2,
            int interpolation,
            int borderMode,
            Scalar borderValue) {
        remap_1(
                src.nativeObj,
                dst.nativeObj,
                map1.nativeObj,
                map2.nativeObj,
                interpolation,
                borderMode,
                borderValue.val[0],
                borderValue.val[1],
                borderValue.val[2],
                borderValue.val[3]);
    }

    /**
     * Applies a generic geometrical transformation to an image.
     *
     * @param src           Source image
     * @param dst           Destination image. It has the same size as map1 and the same type as src
     * @param map1          The first map of either (x,y) points or just x values having the type CV_16SC2 , CV_32FC1,
     *                      or CV_32FC2. See convertMaps for details on converting a floating point representation to
     *                      fixed-point
     * @param map2          The second map of y values having the type CV_16UC1, CV_32FC1, or none (empty map if map1 is
     *                      (x,y) points), respectively
     * @param interpolation Interpolation method (see InterpolationFlags). The methods INTER_AREA INTER_LINEAR_EXACT and
     *                      INTER_NEAREST_EXACT are not supported by this function. The extra flag WARP_RELATIVE_MAP can
     *                      be ORed to the interpolation method (e.g. INTER_LINEAR | WARP_RELATIVE_MAP)
     * @param borderMode    Pixel extrapolation method (see BorderTypes). When borderMode=BORDER_TRANSPARENT, it means
     *                      that the pixels in the destination image that corresponds to the "outliers" in the source
     *                      image are not modified by the function
     * @throws NullPointerException if src, dst, map1, map2 is null
     */
    public static void remap(Mat src, Mat dst, Mat map1, Mat map2, int interpolation, int borderMode) {
        remap_2(src.nativeObj, dst.nativeObj, map1.nativeObj, map2.nativeObj, interpolation, borderMode);
    }

    /**
     * Applies a generic geometrical transformation to an image.
     *
     * @param src           Source image
     * @param dst           Destination image. It has the same size as map1 and the same type as src
     * @param map1          The first map of either (x,y) points or just x values having the type CV_16SC2 , CV_32FC1,
     *                      or CV_32FC2. See convertMaps for details on converting a floating point representation to
     *                      fixed-point
     * @param map2          The second map of y values having the type CV_16UC1, CV_32FC1, or none (empty map if map1 is
     *                      (x,y) points), respectively
     * @param interpolation Interpolation method (see InterpolationFlags). The methods INTER_AREA INTER_LINEAR_EXACT and
     *                      INTER_NEAREST_EXACT are not supported by this function. The extra flag WARP_RELATIVE_MAP can
     *                      be ORed to the interpolation method (e.g. INTER_LINEAR | WARP_RELATIVE_MAP)
     * @throws NullPointerException if src, dst, map1, map2 is null
     */
    public static void remap(Mat src, Mat dst, Mat map1, Mat map2, int interpolation) {
        remap_3(src.nativeObj, dst.nativeObj, map1.nativeObj, map2.nativeObj, interpolation);
    }

    /**
     * Converts image transformation maps from one representation to another.
     *
     * @param map1            The first input map of type CV_16SC2, CV_32FC1, or CV_32FC2
     * @param map2            The second input map of type CV_16UC1, CV_32FC1, or none (empty matrix), respectively
     * @param dstmap1         The first output map that has the type dstmap1type and the same size as src
     * @param dstmap2         The second output map
     * @param dstmap1type     Type of the first output map that should be CV_16SC2, CV_32FC1, or CV_32FC2
     * @param nninterpolation Flag indicating whether the fixed-point maps are used for the nearest-neighbor or for a
     *                        more complex interpolation
     * @throws NullPointerException if map1, map2, dstmap1, dstmap2 is null
     */
    public static void convertMaps(
            Mat map1,
            Mat map2,
            Mat dstmap1,
            Mat dstmap2,
            int dstmap1type,
            boolean nninterpolation) {
        convertMaps_0(
                map1.nativeObj,
                map2.nativeObj,
                dstmap1.nativeObj,
                dstmap2.nativeObj,
                dstmap1type,
                nninterpolation);
    }

    /**
     * Converts image transformation maps from one representation to another.
     *
     * @param map1        The first input map of type CV_16SC2, CV_32FC1, or CV_32FC2
     * @param map2        The second input map of type CV_16UC1, CV_32FC1, or none (empty matrix), respectively
     * @param dstmap1     The first output map that has the type dstmap1type and the same size as src
     * @param dstmap2     The second output map
     * @param dstmap1type Type of the first output map that should be CV_16SC2, CV_32FC1, or CV_32FC2
     * @throws NullPointerException if map1, map2, dstmap1, dstmap2 is null
     */
    public static void convertMaps(Mat map1, Mat map2, Mat dstmap1, Mat dstmap2, int dstmap1type) {
        convertMaps_1(map1.nativeObj, map2.nativeObj, dstmap1.nativeObj, dstmap2.nativeObj, dstmap1type);
    }

    /**
     * Transforms an image to compensate for lens distortion. The function transforms an image to compensate radial and
     * tangential lens distortion. The function is simply a combination of initUndistortRectifyMap (with unity R ) and
     * remap (with bilinear interpolation). See the former function for details of the transformation being performed.
     * Those pixels in the destination image, for which there is no correspondent pixels in the source image, are filled
     * with zeros (black color). A particular subset of the source image that will be visible in the corrected image can
     * be regulated by newCameraMatrix. You can use getOptimalNewCameraMatrix to compute the appropriate newCameraMatrix
     * depending on your requirements. The camera matrix and the distortion parameters can be determined using
     * calibrateCamera. If the resolution of images is different from the resolution used at the calibration stage, f_x,
     * f_y, c_x and c_y need to be scaled accordingly, while the distortion coefficients remain the same.
     *
     * @param src             Input (distorted) image
     * @param dst             Output (corrected) image that has the same size and type as src
     * @param cameraMatrix    Input camera matrix A = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs      Input vector of distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1,
     *                        s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                        NULL/empty, the zero distortion coefficients are assumed
     * @param newCameraMatrix Camera matrix of the distorted image. By default, it is the same as cameraMatrix but you
     *                        may additionally scale and shift the result by using a different matrix
     * @throws NullPointerException if src, dst, cameraMatrix, distCoeffs, newCameraMatrix is null
     */
    public static void undistort(Mat src, Mat dst, Mat cameraMatrix, Mat distCoeffs, Mat newCameraMatrix) {
        undistort_0(
                src.nativeObj,
                dst.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                newCameraMatrix.nativeObj);
    }

    /**
     * Transforms an image to compensate for lens distortion. The function transforms an image to compensate radial and
     * tangential lens distortion. The function is simply a combination of initUndistortRectifyMap (with unity R ) and
     * remap (with bilinear interpolation). See the former function for details of the transformation being performed.
     * Those pixels in the destination image, for which there is no correspondent pixels in the source image, are filled
     * with zeros (black color). A particular subset of the source image that will be visible in the corrected image can
     * be regulated by newCameraMatrix. You can use getOptimalNewCameraMatrix to compute the appropriate newCameraMatrix
     * depending on your requirements. The camera matrix and the distortion parameters can be determined using
     * calibrateCamera. If the resolution of images is different from the resolution used at the calibration stage, f_x,
     * f_y, c_x and c_y need to be scaled accordingly, while the distortion coefficients remain the same.
     *
     * @param src          Input (distorted) image
     * @param dst          Output (corrected) image that has the same size and type as src
     * @param cameraMatrix Input camera matrix A = vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs   Input vector of distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2,
     *                     s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                     zero distortion coefficients are assumed
     * @throws NullPointerException if src, dst, cameraMatrix, distCoeffs is null
     */
    public static void undistort(Mat src, Mat dst, Mat cameraMatrix, Mat distCoeffs) {
        undistort_1(src.nativeObj, dst.nativeObj, cameraMatrix.nativeObj, distCoeffs.nativeObj);
    }

    /**
     * Computes the undistortion and rectification transformation map.
     *
     * @param cameraMatrix    Input camera matrix A=vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs      Input vector of distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1,
     *                        s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                        NULL/empty, the zero distortion coefficients are assumed
     * @param R               Optional rectification transformation in the object space (3x3 matrix). R1 or R2 ,
     *                        computed by stereoRectify can be passed here. If the matrix is empty, the identity
     *                        transformation is assumed. In initUndistortRectifyMap R assumed to be an identity matrix
     * @param newCameraMatrix New camera matrix A'=vecthreethree{f_x'}{0}{c_x'}{0}{f_y'}{c_y'}{0}{0}{1}
     * @param size            Undistorted image size
     * @param m1type          Type of the first output map that can be CV_32FC1, CV_32FC2 or CV_16SC2, see convertMaps
     * @param map1            The first output map
     * @param map2            The second output map
     * @throws NullPointerException if cameraMatrix, distCoeffs, R, newCameraMatrix, size, map1, map2 is null
     */
    public static void initUndistortRectifyMap(
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat R,
            Mat newCameraMatrix,
            Size size,
            int m1type,
            Mat map1,
            Mat map2) {
        initUndistortRectifyMap_0(
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                R.nativeObj,
                newCameraMatrix.nativeObj,
                size.width,
                size.height,
                m1type,
                map1.nativeObj,
                map2.nativeObj);
    }

    /**
     * Computes the projection and inverse-rectification transformation map.
     *
     * @param cameraMatrix    Input camera matrix A=vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs      Input vector of distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1,
     *                        s_2, s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                        NULL/empty, the zero distortion coefficients are assumed
     * @param R               Optional rectification transformation in the object space (3x3 matrix). R1 or R2, computed
     *                        by stereoRectify can be passed here. If the matrix is empty, the identity transformation
     *                        is assumed
     * @param newCameraMatrix New camera matrix A'=vecthreethree{f_x'}{0}{c_x'}{0}{f_y'}{c_y'}{0}{0}{1}
     * @param size            Distorted image size
     * @param m1type          Type of the first output map. Can be CV_32FC1, CV_32FC2 or CV_16SC2, see convertMaps
     * @param map1            The first output map for remap
     * @param map2            The second output map for remap
     * @throws NullPointerException if cameraMatrix, distCoeffs, R, newCameraMatrix, size, map1, map2 is null
     */
    public static void initInverseRectificationMap(
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat R,
            Mat newCameraMatrix,
            Size size,
            int m1type,
            Mat map1,
            Mat map2) {
        initInverseRectificationMap_0(
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                R.nativeObj,
                newCameraMatrix.nativeObj,
                size.width,
                size.height,
                m1type,
                map1.nativeObj,
                map2.nativeObj);
    }

    /**
     * Computes undistortion and rectification maps for image transform by remap(). If D is empty zero distortion is
     * used, if R or P is empty identity matrixes are used.
     *
     * @param K      Camera intrinsic matrix cameramatrix{K}
     * @param D      Input vector of distortion coefficients distcoeffsfisheye
     * @param R      Rectification transformation in the object space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or
     *               1x1 3-channel
     * @param P      New camera intrinsic matrix (3x3) or new projection matrix (3x4)
     * @param size   Undistorted image size
     * @param m1type Type of the first output map that can be CV_32FC1 or CV_16SC2 . See convertMaps() for details
     * @param map1   The first output map
     * @param map2   The second output map
     * @throws NullPointerException if K, D, R, P, size, map1, map2 is null
     */
    public static void fisheye_initUndistortRectifyMap(
            Mat K,
            Mat D,
            Mat R,
            Mat P,
            Size size,
            int m1type,
            Mat map1,
            Mat map2) {
        fisheye_initUndistortRectifyMap_0(
                K.nativeObj,
                D.nativeObj,
                R.nativeObj,
                P.nativeObj,
                size.width,
                size.height,
                m1type,
                map1.nativeObj,
                map2.nativeObj);
    }

    /**
     * Transforms an image to compensate for fisheye lens distortion.
     *
     * @param distorted   image with fisheye lens distortion
     * @param undistorted Output image with compensated fisheye lens distortion
     * @param K           Camera intrinsic matrix cameramatrix{K}
     * @param D           Input vector of distortion coefficients distcoeffsfisheye
     * @param Knew        Camera intrinsic matrix of the distorted image. By default, it is the identity matrix but you
     *                    may additionally scale and shift the result by using a different matrix
     * @param new_size    the new size The function transforms an image to compensate radial and tangential lens
     *                    distortion. The function is simply a combination of fisheye::initUndistortRectifyMap (with
     *                    unity R ) and remap (with bilinear interpolation). See the former function for details of the
     *                    transformation being performed. See below the results of undistortImage. a result of undistort
     *                    of perspective camera model (all possible coefficients (k_1, k_2, k_3, k_4, k_5, k_6) of
     *                    distortion were optimized under calibration) b result of fisheye::undistortImage of fisheye
     *                    camera model (all possible coefficients (k_1, k_2, k_3, k_4) of fisheye distortion were
     *                    optimized under calibration) c original image was captured with fisheye lens Pictures a) and
     *                    b) almost the same. But if we consider points of image located far from the center of image,
     *                    we can notice that on image a) these points are distorted
     * @throws NullPointerException if distorted, undistorted, K, D, Knew, new_size is null
     */
    public static void fisheye_undistortImage(Mat distorted, Mat undistorted, Mat K, Mat D, Mat Knew, Size new_size) {
        fisheye_undistortImage_0(
                distorted.nativeObj,
                undistorted.nativeObj,
                K.nativeObj,
                D.nativeObj,
                Knew.nativeObj,
                new_size.width,
                new_size.height);
    }

    /**
     * Transforms an image to compensate for fisheye lens distortion.
     *
     * @param distorted   image with fisheye lens distortion
     * @param undistorted Output image with compensated fisheye lens distortion
     * @param K           Camera intrinsic matrix cameramatrix{K}
     * @param D           Input vector of distortion coefficients distcoeffsfisheye
     * @param Knew        Camera intrinsic matrix of the distorted image. By default, it is the identity matrix but you
     *                    may additionally scale and shift the result by using a different matrix
     * @throws NullPointerException if distorted, undistorted, K, D, Knew is null
     */
    public static void fisheye_undistortImage(Mat distorted, Mat undistorted, Mat K, Mat D, Mat Knew) {
        fisheye_undistortImage_1(distorted.nativeObj, undistorted.nativeObj, K.nativeObj, D.nativeObj, Knew.nativeObj);
    }

    /**
     * Transforms an image to compensate for fisheye lens distortion.
     *
     * @param distorted   image with fisheye lens distortion
     * @param undistorted Output image with compensated fisheye lens distortion
     * @param K           Camera intrinsic matrix cameramatrix{K}
     * @param D           Input vector of distortion coefficients distcoeffsfisheye
     * @throws NullPointerException if distorted, undistorted, K, D is null
     */
    public static void fisheye_undistortImage(Mat distorted, Mat undistorted, Mat K, Mat D) {
        fisheye_undistortImage_2(distorted.nativeObj, undistorted.nativeObj, K.nativeObj, D.nativeObj);
    }

    /**
     * Retrieves a pixel rectangle from an image with sub-pixel accuracy.
     *
     * @param image     Source image
     * @param patchSize Size of the extracted patch
     * @param center    Floating point coordinates of the center of the extracted rectangle within the source image. The
     *                  center must be inside the image
     * @param patch     Extracted patch that has the size patchSize and the same number of channels as src
     * @param patchType Depth of the extracted pixels. By default, they have the same depth as src
     * @throws NullPointerException if image, patchSize, center, patch is null
     */
    public static void getRectSubPix(Mat image, Size patchSize, Point center, Mat patch, int patchType) {
        getRectSubPix_0(
                image.nativeObj,
                patchSize.width,
                patchSize.height,
                center.x,
                center.y,
                patch.nativeObj,
                patchType);
    }

    /**
     * Retrieves a pixel rectangle from an image with sub-pixel accuracy.
     *
     * @param image     Source image
     * @param patchSize Size of the extracted patch
     * @param center    Floating point coordinates of the center of the extracted rectangle within the source image. The
     *                  center must be inside the image
     * @param patch     Extracted patch that has the size patchSize and the same number of channels as src
     * @throws NullPointerException if image, patchSize, center, patch is null
     */
    public static void getRectSubPix(Mat image, Size patchSize, Point center, Mat patch) {
        getRectSubPix_1(image.nativeObj, patchSize.width, patchSize.height, center.x, center.y, patch.nativeObj);
    }

    /**
     * Remaps an image between Cartesian and polar coordinates. Flags select linear or logarithmic radial scaling,
     * interpolation and inverse mapping.
     *
     * @param src       Source image
     * @param dst       Destination image. It will have same type as src
     * @param dsize     The destination image size (see description for valid options)
     * @param center    The transformation center
     * @param maxRadius The radius of the bounding circle to transform. It determines the inverse magnitude scale
     *                  parameter too
     * @param flags     A combination of interpolation methods, InterpolationFlags + WarpPolarMode. Add
     *                  WARP_POLAR_LINEAR to select linear polar mapping (default) Add WARP_POLAR_LOG to select semilog
     *                  polar mapping Add WARP_INVERSE_MAP for reverse mapping. Note: The function can not operate
     *                  in-place. To calculate magnitude and angle in degrees cartToPolar is used internally thus angles
     *                  are measured from 0 to 360 with accuracy about 0.3 degrees. This function uses remap. Due to
     *                  current implementation limitations the size of an input and output images should be less than
     *                  32767x32767
     * @throws NullPointerException if src, dst, dsize, center is null
     */
    public static void warpPolar(Mat src, Mat dst, Size dsize, Point center, double maxRadius, int flags) {
        warpPolar_0(src.nativeObj, dst.nativeObj, dsize.width, dsize.height, center.x, center.y, maxRadius, flags);
    }

    /**
     * Computes the image integral, squared-value integral and optionally the tilted integral used for constant-time
     * regional sums.
     *
     * @param src     input image as W times H, 8-bit or floating-point (32f or 64f)
     * @param sum     integral image as (W+1) x (H+1) , 32-bit integer or floating-point (32f or 64f)
     * @param sqsum   integral image for squared pixel values; it is (W+1) x (H+1), double-precision floating-point
     *                (64f) array
     * @param tilted  integral for the image rotated by 45 degrees; it is (W+1) x (H+1) array with the same data type as
     *                sum
     * @param sdepth  desired depth of the integral and the tilted integral images, CV_32S, CV_32F, or CV_64F
     * @param sqdepth desired depth of the integral image of squared pixel values, CV_32F or CV_64F
     * @throws NullPointerException if src, sum, sqsum, tilted is null
     */
    public static void integral3(Mat src, Mat sum, Mat sqsum, Mat tilted, int sdepth, int sqdepth) {
        integral3_0(src.nativeObj, sum.nativeObj, sqsum.nativeObj, tilted.nativeObj, sdepth, sqdepth);
    }

    /**
     * Computes the image integral, squared-value integral and optionally the tilted integral used for constant-time
     * regional sums.
     *
     * @param src    input image as W times H, 8-bit or floating-point (32f or 64f)
     * @param sum    integral image as (W+1) x (H+1) , 32-bit integer or floating-point (32f or 64f)
     * @param sqsum  integral image for squared pixel values; it is (W+1) x (H+1), double-precision floating-point (64f)
     *               array
     * @param tilted integral for the image rotated by 45 degrees; it is (W+1) x (H+1) array with the same data type as
     *               sum
     * @param sdepth desired depth of the integral and the tilted integral images, CV_32S, CV_32F, or CV_64F
     * @throws NullPointerException if src, sum, sqsum, tilted is null
     */
    public static void integral3(Mat src, Mat sum, Mat sqsum, Mat tilted, int sdepth) {
        integral3_1(src.nativeObj, sum.nativeObj, sqsum.nativeObj, tilted.nativeObj, sdepth);
    }

    /**
     * Computes the image integral, squared-value integral and optionally the tilted integral used for constant-time
     * regional sums.
     *
     * @param src    input image as W times H, 8-bit or floating-point (32f or 64f)
     * @param sum    integral image as (W+1) x (H+1) , 32-bit integer or floating-point (32f or 64f)
     * @param sqsum  integral image for squared pixel values; it is (W+1) x (H+1), double-precision floating-point (64f)
     *               array
     * @param tilted integral for the image rotated by 45 degrees; it is (W+1) x (H+1) array with the same data type as
     *               sum
     * @throws NullPointerException if src, sum, sqsum, tilted is null
     */
    public static void integral3(Mat src, Mat sum, Mat sqsum, Mat tilted) {
        integral3_2(src.nativeObj, sum.nativeObj, sqsum.nativeObj, tilted.nativeObj);
    }

    /**
     * Computes an integral image of cumulative pixel sums.
     *
     * @param src    source matrix
     * @param sum    output integral image of cumulative pixel sums
     * @param sdepth element depth of the integral sum image
     * @throws NullPointerException if src, sum is null
     */
    public static void integral(Mat src, Mat sum, int sdepth) {
        integral_0(src.nativeObj, sum.nativeObj, sdepth);
    }

    /**
     * Computes an integral image of cumulative pixel sums.
     *
     * @param src source matrix
     * @param sum output integral image of cumulative pixel sums
     * @throws NullPointerException if src, sum is null
     */
    public static void integral(Mat src, Mat sum) {
        integral_1(src.nativeObj, sum.nativeObj);
    }

    /**
     * Computes integral images of pixel sums and squared pixel sums.
     *
     * @param src     source matrix
     * @param sum     output integral image of cumulative pixel sums
     * @param sqsum   output integral image of cumulative squared pixel sums
     * @param sdepth  element depth of the integral sum image
     * @param sqdepth element depth of the squared-sum image
     * @throws NullPointerException if src, sum, sqsum is null
     */
    public static void integral2(Mat src, Mat sum, Mat sqsum, int sdepth, int sqdepth) {
        integral2_0(src.nativeObj, sum.nativeObj, sqsum.nativeObj, sdepth, sqdepth);
    }

    /**
     * Computes integral images of pixel sums and squared pixel sums.
     *
     * @param src    source matrix
     * @param sum    output integral image of cumulative pixel sums
     * @param sqsum  output integral image of cumulative squared pixel sums
     * @param sdepth element depth of the integral sum image
     * @throws NullPointerException if src, sum, sqsum is null
     */
    public static void integral2(Mat src, Mat sum, Mat sqsum, int sdepth) {
        integral2_1(src.nativeObj, sum.nativeObj, sqsum.nativeObj, sdepth);
    }

    /**
     * Computes integral images of pixel sums and squared pixel sums.
     *
     * @param src   source matrix
     * @param sum   output integral image of cumulative pixel sums
     * @param sqsum output integral image of cumulative squared pixel sums
     * @throws NullPointerException if src, sum, sqsum is null
     */
    public static void integral2(Mat src, Mat sum, Mat sqsum) {
        integral2_2(src.nativeObj, sum.nativeObj, sqsum.nativeObj);
    }

    /**
     * Adds an image to the accumulator image.
     *
     * @param src  Input image of type CV_8UC(n), CV_16UC(n), CV_32FC(n) or CV_64FC(n), where n is a positive integer
     * @param dst  %Accumulator image with the same number of channels as input image, and a depth of CV_32F or CV_64F
     * @param mask Optional operation mask
     * @throws NullPointerException if src, dst, mask is null
     */
    public static void accumulate(Mat src, Mat dst, Mat mask) {
        accumulate_0(src.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Adds an image to the accumulator image.
     *
     * @param src Input image of type CV_8UC(n), CV_16UC(n), CV_32FC(n) or CV_64FC(n), where n is a positive integer
     * @param dst %Accumulator image with the same number of channels as input image, and a depth of CV_32F or CV_64F
     * @throws NullPointerException if src, dst is null
     */
    public static void accumulate(Mat src, Mat dst) {
        accumulate_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Adds the square of a source image to the accumulator image.
     *
     * @param src  Input image as 1- or 3-channel, 8-bit or 32-bit floating point
     * @param dst  %Accumulator image with the same number of channels as input image, 32-bit or 64-bit floating-point
     * @param mask Optional operation mask
     * @throws NullPointerException if src, dst, mask is null
     */
    public static void accumulateSquare(Mat src, Mat dst, Mat mask) {
        accumulateSquare_0(src.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Adds the square of a source image to the accumulator image.
     *
     * @param src Input image as 1- or 3-channel, 8-bit or 32-bit floating point
     * @param dst %Accumulator image with the same number of channels as input image, 32-bit or 64-bit floating-point
     * @throws NullPointerException if src, dst is null
     */
    public static void accumulateSquare(Mat src, Mat dst) {
        accumulateSquare_1(src.nativeObj, dst.nativeObj);
    }

    /**
     * Adds the per-element product of two input images to the accumulator image.
     *
     * @param src1 First input image, 1- or 3-channel, 8-bit or 32-bit floating point
     * @param src2 Second input image of the same type and the same size as src1
     * @param dst  %Accumulator image with the same number of channels as input images, 32-bit or 64-bit floating-point
     * @param mask Optional operation mask
     * @throws NullPointerException if src1, src2, dst, mask is null
     */
    public static void accumulateProduct(Mat src1, Mat src2, Mat dst, Mat mask) {
        accumulateProduct_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, mask.nativeObj);
    }

    /**
     * Adds the per-element product of two input images to the accumulator image.
     *
     * @param src1 First input image, 1- or 3-channel, 8-bit or 32-bit floating point
     * @param src2 Second input image of the same type and the same size as src1
     * @param dst  %Accumulator image with the same number of channels as input images, 32-bit or 64-bit floating-point
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void accumulateProduct(Mat src1, Mat src2, Mat dst) {
        accumulateProduct_1(src1.nativeObj, src2.nativeObj, dst.nativeObj);
    }

    /**
     * Updates a running average.
     *
     * @param src   Input image as 1- or 3-channel, 8-bit or 32-bit floating point
     * @param dst   %Accumulator image with the same number of channels as input image, 32-bit or 64-bit floating-point
     * @param alpha Weight of the input image
     * @param mask  Optional operation mask
     * @throws NullPointerException if src, dst, mask is null
     */
    public static void accumulateWeighted(Mat src, Mat dst, double alpha, Mat mask) {
        accumulateWeighted_0(src.nativeObj, dst.nativeObj, alpha, mask.nativeObj);
    }

    /**
     * Updates a running average.
     *
     * @param src   Input image as 1- or 3-channel, 8-bit or 32-bit floating point
     * @param dst   %Accumulator image with the same number of channels as input image, 32-bit or 64-bit floating-point
     * @param alpha Weight of the input image
     * @throws NullPointerException if src, dst is null
     */
    public static void accumulateWeighted(Mat src, Mat dst, double alpha) {
        accumulateWeighted_1(src.nativeObj, dst.nativeObj, alpha);
    }

    /**
     * The function is used to detect translational shifts that occur between two images.
     *
     * @param src1     Source floating point array (CV_32FC1 or CV_64FC1)
     * @param src2     Source floating point array (CV_32FC1 or CV_64FC1)
     * @param window   Floating point array with windowing coefficients to reduce edge effects (optional)
     * @param response Signal power within the 5x5 centroid around the peak, between 0 and 1 (optional)
     * @return detected phase shift (sub-pixel) between the two arrays
     * @throws NullPointerException if src1, src2, window is null
     */
    public static Point phaseCorrelate(Mat src1, Mat src2, Mat window, double[] response) {
        double[] response_out = new double[1];
        Point retVal = new Point(phaseCorrelate_0(src1.nativeObj, src2.nativeObj, window.nativeObj, response_out));
        if (response != null)
            response[0] = (double) response_out[0];
        return retVal;
    }

    /**
     * The function is used to detect translational shifts that occur between two images.
     *
     * @param src1   Source floating point array (CV_32FC1 or CV_64FC1)
     * @param src2   Source floating point array (CV_32FC1 or CV_64FC1)
     * @param window Floating point array with windowing coefficients to reduce edge effects (optional)
     * @return detected phase shift (sub-pixel) between the two arrays
     * @throws NullPointerException if src1, src2, window is null
     */
    public static Point phaseCorrelate(Mat src1, Mat src2, Mat window) {
        return new Point(phaseCorrelate_1(src1.nativeObj, src2.nativeObj, window.nativeObj));
    }

    /**
     * The function is used to detect translational shifts that occur between two images.
     *
     * @param src1 Source floating point array (CV_32FC1 or CV_64FC1)
     * @param src2 Source floating point array (CV_32FC1 or CV_64FC1)
     * @return detected phase shift (sub-pixel) between the two arrays
     * @throws NullPointerException if src1, src2 is null
     */
    public static Point phaseCorrelate(Mat src1, Mat src2) {
        return new Point(phaseCorrelate_2(src1.nativeObj, src2.nativeObj));
    }

    /**
     * Detects translational shifts between two images. This function extends the standard REF: phaseCorrelate method by
     * improving sub-pixel accuracy through iterative shift refinement in the phase-correlation space, as described in.
     *
     * @param src1     Source floating point array (CV_32FC1 or CV_64FC1)
     * @param src2     Source floating point array (CV_32FC1 or CV_64FC1)
     * @param L2size   The size of the correlation neighborhood used by the iterative shift refinement algorithm
     * @param maxIters The maximum number of iterations the iterative refinement algorithm will run
     * @return detected sub-pixel shift between the two arrays
     * @throws NullPointerException if src1, src2 is null
     */
    public static Point phaseCorrelateIterative(Mat src1, Mat src2, int L2size, int maxIters) {
        return new Point(phaseCorrelateIterative_0(src1.nativeObj, src2.nativeObj, L2size, maxIters));
    }

    /**
     * Detects translational shifts between two images. This function extends the standard REF: phaseCorrelate method by
     * improving sub-pixel accuracy through iterative shift refinement in the phase-correlation space, as described in.
     *
     * @param src1   Source floating point array (CV_32FC1 or CV_64FC1)
     * @param src2   Source floating point array (CV_32FC1 or CV_64FC1)
     * @param L2size The size of the correlation neighborhood used by the iterative shift refinement algorithm
     * @return detected sub-pixel shift between the two arrays
     * @throws NullPointerException if src1, src2 is null
     */
    public static Point phaseCorrelateIterative(Mat src1, Mat src2, int L2size) {
        return new Point(phaseCorrelateIterative_1(src1.nativeObj, src2.nativeObj, L2size));
    }

    /**
     * Detects translational shifts between two images. This function extends the standard REF: phaseCorrelate method by
     * improving sub-pixel accuracy through iterative shift refinement in the phase-correlation space, as described in.
     *
     * @param src1 Source floating point array (CV_32FC1 or CV_64FC1)
     * @param src2 Source floating point array (CV_32FC1 or CV_64FC1)
     * @return detected sub-pixel shift between the two arrays
     * @throws NullPointerException if src1, src2 is null
     */
    public static Point phaseCorrelateIterative(Mat src1, Mat src2) {
        return new Point(phaseCorrelateIterative_2(src1.nativeObj, src2.nativeObj));
    }

    /**
     * This function computes a Hanning window coefficients in two dimensions. See
     * (https://en.wikipedia.org/wiki/Hann_function) and (https://en.wikipedia.org/wiki/Window_function) for more
     * information. // create hanning window of size 100x100 and type CV_32F Mat hann; createHanningWindow(hann,
     * Size(100, 100), CV_32F);.
     *
     * @param dst     Destination array to place Hann coefficients in
     * @param winSize The window size specifications (both width and height must be &gt; 1)
     * @param type    Created array type
     * @throws NullPointerException if dst, winSize is null
     */
    public static void createHanningWindow(Mat dst, Size winSize, int type) {
        createHanningWindow_0(dst.nativeObj, winSize.width, winSize.height, type);
    }

    /**
     * Applies a fixed-level threshold to each array element. The function applies fixed-level thresholding to a
     * multiple-channel array. The function is typically used to get a bi-level (binary) image out of a grayscale image
     * ( compare could be also used for this purpose) or for removing a noise, that is, filtering out pixels with too
     * small or too large values. There are several types of thresholding supported by the function. They are determined
     * by type parameter. Also, the special values THRESH_OTSU or THRESH_TRIANGLE may be combined with one of the above
     * values. In these cases, the function determines the optimal threshold value using the Otsu's or Triangle
     * algorithm and uses it instead of the specified thresh. Note: Currently, the Otsu's method is implemented only for
     * CV_8UC1 and CV_16UC1 images, and the Triangle's method is implemented only for CV_8UC1 images.
     *
     * @param src    input array (multiple-channel, CV_8U, CV_16S, CV_16U, CV_32F or CV_64F)
     * @param dst    output array of the same size and type and the same number of channels as src
     * @param thresh threshold value
     * @param maxval maximum value to use with the THRESH_BINARY and THRESH_BINARY_INV thresholding types
     * @param type   thresholding type (see ThresholdTypes)
     * @return the computed threshold value if Otsu's or Triangle methods used
     * @throws NullPointerException if src, dst is null
     */
    public static double threshold(Mat src, Mat dst, double thresh, double maxval, int type) {
        return threshold_0(src.nativeObj, dst.nativeObj, thresh, maxval, type);
    }

    /**
     * Same as threshold, but with an optional mask Note: If the mask is empty, thresholdWithMask is equivalent to
     * threshold. If the mask is not empty, dst *must* be of the same size and type as src, so that outliers pixels are
     * left as-is.
     *
     * @param src    input array (multiple-channel, 8-bit or 32-bit floating point)
     * @param dst    output array of the same size and type and the same number of channels as src
     * @param mask   optional mask (same size as src, 8-bit)
     * @param thresh threshold value
     * @param maxval maximum value to use with the THRESH_BINARY and THRESH_BINARY_INV thresholding types
     * @param type   thresholding type (see ThresholdTypes)
     * @return the computed threshold value if Otsu's or Triangle methods used
     * @throws NullPointerException if src, dst, mask is null
     */
    public static double thresholdWithMask(Mat src, Mat dst, Mat mask, double thresh, double maxval, int type) {
        return thresholdWithMask_0(src.nativeObj, dst.nativeObj, mask.nativeObj, thresh, maxval, type);
    }

    /**
     * Applies an adaptive threshold to an array.
     *
     * @param src            Source 8-bit single-channel image
     * @param dst            Destination image of the same size and the same type as src
     * @param maxValue       Non-zero value assigned to the pixels for which the condition is satisfied
     * @param adaptiveMethod Adaptive thresholding algorithm to use, see AdaptiveThresholdTypes. The BORDER_REPLICATE |
     *                       BORDER_ISOLATED is used to process boundaries
     * @param thresholdType  Thresholding type that must be either THRESH_BINARY or THRESH_BINARY_INV, see
     *                       ThresholdTypes
     * @param blockSize      Size of a pixel neighborhood that is used to calculate a threshold value for the pixel: 3,
     *                       5, 7, and so on
     * @param C              Constant subtracted from the mean or weighted mean (see the details below). Normally, it is
     *                       positive but may be zero or negative as well
     * @throws NullPointerException if src, dst is null
     */
    public static void adaptiveThreshold(
            Mat src,
            Mat dst,
            double maxValue,
            int adaptiveMethod,
            int thresholdType,
            int blockSize,
            double C) {
        adaptiveThreshold_0(src.nativeObj, dst.nativeObj, maxValue, adaptiveMethod, thresholdType, blockSize, C);
    }

    /**
     * Blurs an image and downsamples it.
     *
     * @param src        input image
     * @param dst        output image; it has the specified size and the same type as src
     * @param dstsize    size of the output image
     * @param borderType Pixel extrapolation method, see BorderTypes (BORDER_CONSTANT isn't supported)
     * @throws NullPointerException if src, dst, dstsize is null
     */
    public static void pyrDown(Mat src, Mat dst, Size dstsize, int borderType) {
        pyrDown_0(src.nativeObj, dst.nativeObj, dstsize.width, dstsize.height, borderType);
    }

    /**
     * Blurs an image and downsamples it.
     *
     * @param src     input image
     * @param dst     output image; it has the specified size and the same type as src
     * @param dstsize size of the output image
     * @throws NullPointerException if src, dst, dstsize is null
     */
    public static void pyrDown(Mat src, Mat dst, Size dstsize) {
        pyrDown_1(src.nativeObj, dst.nativeObj, dstsize.width, dstsize.height);
    }

    /**
     * Blurs an image and downsamples it.
     *
     * @param src input image
     * @param dst output image; it has the specified size and the same type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void pyrDown(Mat src, Mat dst) {
        pyrDown_2(src.nativeObj, dst.nativeObj);
    }

    /**
     * Upsamples an image and then blurs it.
     *
     * @param src        input image
     * @param dst        output image. It has the specified size and the same type as src
     * @param dstsize    size of the output image
     * @param borderType Pixel extrapolation method, see BorderTypes (only BORDER_DEFAULT is supported)
     * @throws NullPointerException if src, dst, dstsize is null
     */
    public static void pyrUp(Mat src, Mat dst, Size dstsize, int borderType) {
        pyrUp_0(src.nativeObj, dst.nativeObj, dstsize.width, dstsize.height, borderType);
    }

    /**
     * Upsamples an image and then blurs it.
     *
     * @param src     input image
     * @param dst     output image. It has the specified size and the same type as src
     * @param dstsize size of the output image
     * @throws NullPointerException if src, dst, dstsize is null
     */
    public static void pyrUp(Mat src, Mat dst, Size dstsize) {
        pyrUp_1(src.nativeObj, dst.nativeObj, dstsize.width, dstsize.height);
    }

    /**
     * Upsamples an image and then blurs it.
     *
     * @param src input image
     * @param dst output image. It has the specified size and the same type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void pyrUp(Mat src, Mat dst) {
        pyrUp_2(src.nativeObj, dst.nativeObj);
    }

    /**
     * This variant supports only uniform histograms. ranges argument is either empty vector or a flattened vector of
     * histSize.size()*2 elements (histSize.size() element pairs). The first and second elements of each pair specify
     * the lower and upper boundaries.
     *
     * @param images     source image matrices used for histogram calculation or back-projection
     * @param channels   number of channels per matrix element
     * @param mask       8-bit single-channel pixel-selection mask; an empty mask includes every pixel
     * @param hist       histogram bin values used for back-projection
     * @param histSize   number of histogram bins along each dimension
     * @param ranges     half-open range to select along each dimension
     * @param accumulate whether accumulate is enabled
     * @throws NullPointerException if mask, hist is null
     */
    public static void calcHist(
            List<Mat> images,
            MatOfInt channels,
            Mat mask,
            Mat hist,
            MatOfInt histSize,
            MatOfFloat ranges,
            boolean accumulate) {
        Mat images_mat = Converters.vector_Mat_to_Mat(images);
        Mat channels_mat = channels;
        Mat histSize_mat = histSize;
        Mat ranges_mat = ranges;
        calcHist_0(
                images_mat.nativeObj,
                channels_mat.nativeObj,
                mask.nativeObj,
                hist.nativeObj,
                histSize_mat.nativeObj,
                ranges_mat.nativeObj,
                accumulate);
    }

    /**
     * This variant supports only uniform histograms. ranges argument is either empty vector or a flattened vector of
     * histSize.size()*2 elements (histSize.size() element pairs). The first and second elements of each pair specify
     * the lower and upper boundaries.
     *
     * @param images   source image matrices used for histogram calculation or back-projection
     * @param channels number of channels per matrix element
     * @param mask     8-bit single-channel pixel-selection mask; an empty mask includes every pixel
     * @param hist     histogram bin values used for back-projection
     * @param histSize number of histogram bins along each dimension
     * @param ranges   half-open range to select along each dimension
     * @throws NullPointerException if mask, hist is null
     */
    public static void calcHist(
            List<Mat> images,
            MatOfInt channels,
            Mat mask,
            Mat hist,
            MatOfInt histSize,
            MatOfFloat ranges) {
        Mat images_mat = Converters.vector_Mat_to_Mat(images);
        Mat channels_mat = channels;
        Mat histSize_mat = histSize;
        Mat ranges_mat = ranges;
        calcHist_1(
                images_mat.nativeObj,
                channels_mat.nativeObj,
                mask.nativeObj,
                hist.nativeObj,
                histSize_mat.nativeObj,
                ranges_mat.nativeObj);
    }

    /**
     * Back-projects histogram bin values onto the corresponding pixels of the input images.
     *
     * @param images   source image matrices used for histogram calculation or back-projection
     * @param channels number of channels per matrix element
     * @param hist     histogram bin values used for back-projection
     * @param dst      destination matrix that receives the result
     * @param ranges   half-open range to select along each dimension
     * @param scale    multiplicative scale applied by the operation
     * @throws NullPointerException if hist, dst is null
     */
    public static void calcBackProject(
            List<Mat> images,
            MatOfInt channels,
            Mat hist,
            Mat dst,
            MatOfFloat ranges,
            double scale) {
        Mat images_mat = Converters.vector_Mat_to_Mat(images);
        Mat channels_mat = channels;
        Mat ranges_mat = ranges;
        calcBackProject_0(
                images_mat.nativeObj,
                channels_mat.nativeObj,
                hist.nativeObj,
                dst.nativeObj,
                ranges_mat.nativeObj,
                scale);
    }

    /**
     * Compares two histograms. The function compareHist compares two dense or two sparse histograms using the specified
     * method. The function returns d(H_1, H_2) . While the function works well with 1-, 2-, 3-dimensional dense
     * histograms, it may not be suitable for high-dimensional sparse histograms. In such histograms, because of
     * aliasing and sampling problems, the coordinates of non-zero histogram bins can slightly shift. To compare such
     * histograms or more general sparse configurations of weighted points, consider using the EMD function.
     *
     * @param H1     First compared histogram
     * @param H2     Second compared histogram of the same size as H1
     * @param method Comparison method, see HistCompMethods
     * @return the histogram comparison score for the selected metric
     * @throws NullPointerException if H1, H2 is null
     */
    public static double compareHist(Mat H1, Mat H2, int method) {
        return compareHist_0(H1.nativeObj, H2.nativeObj, method);
    }

    /**
     * Equalizes the histogram of a grayscale image.
     *
     * @param src Source 8-bit single channel image
     * @param dst Destination image of the same size and type as src
     * @throws NullPointerException if src, dst is null
     */
    public static void equalizeHist(Mat src, Mat dst) {
        equalizeHist_0(src.nativeObj, dst.nativeObj);
    }

    /**
     * Creates a smart pointer to a CLAHE class and initializes it.
     *
     * @param clipLimit    Threshold for contrast limiting
     * @param tileGridSize Size of grid for histogram equalization. Input image will be divided into equally sized
     *                     rectangular tiles. tileGridSize defines the number of tiles in row and column
     * @return a new {@code CLAHE} wrapper configured with the selected options
     * @throws NullPointerException if tileGridSize is null
     */
    public static CLAHE createCLAHE(double clipLimit, Size tileGridSize) {
        return CLAHE.__fromPtr__(createCLAHE_0(clipLimit, tileGridSize.width, tileGridSize.height));
    }

    /**
     * Creates a smart pointer to a CLAHE class and initializes it.
     *
     * @param clipLimit Threshold for contrast limiting
     * @return a new {@code CLAHE} wrapper configured with the selected options
     */
    public static CLAHE createCLAHE(double clipLimit) {
        return CLAHE.__fromPtr__(createCLAHE_1(clipLimit));
    }

    /**
     * Creates a smart pointer to a CLAHE class and initializes it.
     *
     * @return a new {@code CLAHE} wrapper configured with the selected options
     */
    public static CLAHE createCLAHE() {
        return CLAHE.__fromPtr__(createCLAHE_2());
    }

    /**
     * Computes the "minimal work" distance between two weighted point configurations. The function computes the earth
     * mover distance and/or a lower boundary of the distance between the two weighted point configurations. One of the
     * applications described in is multi-dimensional histogram comparison for image retrieval. EMD is a transportation
     * problem that is solved using some modification of a simplex algorithm, thus the complexity is exponential in the
     * worst case, though, on average it is much faster. In the case of a real metric the lower boundary can be
     * calculated even faster (using linear-time algorithm) and it can be used to determine roughly whether the two
     * signatures are far enough so that they cannot relate to the same object.
     *
     * @param signature1 First signature, a {@code size1} x {@code dims}+1 floating-point matrix. Each row stores the
     *                   point weight followed by the point coordinates. The matrix is allowed to have a single column
     *                   (weights only) if the user-defined cost matrix is used. The weights must be non-negative and
     *                   have at least one non-zero value
     * @param signature2 Second signature of the same format as signature1 , though the number of rows may be different.
     *                   The total weights may be different. In this case an extra "dummy" point is added to either
     *                   signature1 or signature2. The weights must be non-negative and have at least one non-zero value
     * @param distType   Used metric. See DistanceTypes
     * @param cost       User-defined {@code size1} x {@code size2} cost matrix. Also, if a cost matrix is used, lower
     *                   boundary lowerBound cannot be calculated because it needs a metric function. signatures that is
     *                   a distance between mass centers. The lower boundary may not be calculated if the user-defined
     *                   cost matrix is used, the total weights of point configurations are not equal, or if the
     *                   signatures consist of weights only (the signature matrices have a single column). You must
     *                   initialize *lowerBound . If the calculated distance between mass centers is greater or equal to
     *                   *lowerBound (it means that the signatures are far enough), the function does not calculate EMD.
     *                   In any case *lowerBound is set to the calculated distance between mass centers on return. Thus,
     *                   if you want to calculate both distance between mass centers and EMD, *lowerBound should be set
     *                   to 0
     * @param flow       Resultant {@code size1} times {@code size2} flow matrix: {@code flow}_{i,j} is a flow from i
     *                   -th point of signature1 to j -th point of signature2
     * @return the minimum transportation cost between the weighted signatures
     * @throws NullPointerException if signature1, signature2, cost, flow is null
     */
    public static float EMD(Mat signature1, Mat signature2, int distType, Mat cost, Mat flow) {
        return EMD_0(signature1.nativeObj, signature2.nativeObj, distType, cost.nativeObj, flow.nativeObj);
    }

    /**
     * Computes the "minimal work" distance between two weighted point configurations. The function computes the earth
     * mover distance and/or a lower boundary of the distance between the two weighted point configurations. One of the
     * applications described in is multi-dimensional histogram comparison for image retrieval. EMD is a transportation
     * problem that is solved using some modification of a simplex algorithm, thus the complexity is exponential in the
     * worst case, though, on average it is much faster. In the case of a real metric the lower boundary can be
     * calculated even faster (using linear-time algorithm) and it can be used to determine roughly whether the two
     * signatures are far enough so that they cannot relate to the same object.
     *
     * @param signature1 First signature, a {@code size1} x {@code dims}+1 floating-point matrix. Each row stores the
     *                   point weight followed by the point coordinates. The matrix is allowed to have a single column
     *                   (weights only) if the user-defined cost matrix is used. The weights must be non-negative and
     *                   have at least one non-zero value
     * @param signature2 Second signature of the same format as signature1 , though the number of rows may be different.
     *                   The total weights may be different. In this case an extra "dummy" point is added to either
     *                   signature1 or signature2. The weights must be non-negative and have at least one non-zero value
     * @param distType   Used metric. See DistanceTypes
     * @param cost       User-defined {@code size1} x {@code size2} cost matrix. Also, if a cost matrix is used, lower
     *                   boundary lowerBound cannot be calculated because it needs a metric function. signatures that is
     *                   a distance between mass centers. The lower boundary may not be calculated if the user-defined
     *                   cost matrix is used, the total weights of point configurations are not equal, or if the
     *                   signatures consist of weights only (the signature matrices have a single column). You must
     *                   initialize *lowerBound . If the calculated distance between mass centers is greater or equal to
     *                   *lowerBound (it means that the signatures are far enough), the function does not calculate EMD.
     *                   In any case *lowerBound is set to the calculated distance between mass centers on return. Thus,
     *                   if you want to calculate both distance between mass centers and EMD, *lowerBound should be set
     *                   to 0
     * @return the minimum transportation cost between the weighted signatures
     * @throws NullPointerException if signature1, signature2, cost is null
     */
    public static float EMD(Mat signature1, Mat signature2, int distType, Mat cost) {
        return EMD_1(signature1.nativeObj, signature2.nativeObj, distType, cost.nativeObj);
    }

    /**
     * Computes the "minimal work" distance between two weighted point configurations. The function computes the earth
     * mover distance and/or a lower boundary of the distance between the two weighted point configurations. One of the
     * applications described in is multi-dimensional histogram comparison for image retrieval. EMD is a transportation
     * problem that is solved using some modification of a simplex algorithm, thus the complexity is exponential in the
     * worst case, though, on average it is much faster. In the case of a real metric the lower boundary can be
     * calculated even faster (using linear-time algorithm) and it can be used to determine roughly whether the two
     * signatures are far enough so that they cannot relate to the same object.
     *
     * @param signature1 First signature, a {@code size1} x {@code dims}+1 floating-point matrix. Each row stores the
     *                   point weight followed by the point coordinates. The matrix is allowed to have a single column
     *                   (weights only) if the user-defined cost matrix is used. The weights must be non-negative and
     *                   have at least one non-zero value
     * @param signature2 Second signature of the same format as signature1 , though the number of rows may be different.
     *                   The total weights may be different. In this case an extra "dummy" point is added to either
     *                   signature1 or signature2. The weights must be non-negative and have at least one non-zero value
     * @param distType   Used metric. See DistanceTypes
     * @return the minimum transportation cost between the weighted signatures
     * @throws NullPointerException if signature1, signature2 is null
     */
    public static float EMD(Mat signature1, Mat signature2, int distType) {
        return EMD_3(signature1.nativeObj, signature2.nativeObj, distType);
    }

    /**
     * Performs a marker-based image segmentation using the watershed algorithm. The function implements one of the
     * variants of watershed, non-parametric marker-based segmentation algorithm, described in Before passing the image
     * to the function, you have to roughly outline the desired regions in the image markers with positive (&gt;0)
     * indices. So, every region is represented as one or more connected components with the pixel values 1, 2, 3, and
     * so on. Such markers can be retrieved from a binary mask using findContours and drawContours markers , whose
     * relation to the outlined regions is not known and should be defined by the algorithm, should be set to 0's. In
     * the function output, each pixel in markers is set to a value of the "seed" components or to -1 at boundaries
     * between the regions. Note: Any two neighbor connected components are not necessarily separated by a watershed
     * boundary (-1's pixels); for example, they can touch each other in the initial marker image passed to the
     * function.
     *
     * @param image   Input 8-bit 3-channel image
     * @param markers Input/output 32-bit single-channel image (map) of markers. It should have the same size as image
     * @throws NullPointerException if image, markers is null
     */
    public static void watershed(Mat image, Mat markers) {
        watershed_0(image.nativeObj, markers.nativeObj);
    }

    /**
     * Performs initial step of meanshift segmentation of an image.
     *
     * @param src      The source 8-bit, 3-channel image
     * @param dst      The destination image of the same format and the same size as the source
     * @param sp       The spatial window radius
     * @param sr       The color window radius
     * @param maxLevel Maximum level of the pyramid for the segmentation
     * @param termcrit Termination criteria: when to stop meanshift iterations
     * @throws NullPointerException if src, dst, termcrit is null
     */
    public static void pyrMeanShiftFiltering(
            Mat src,
            Mat dst,
            double sp,
            double sr,
            int maxLevel,
            TermCriteria termcrit) {
        pyrMeanShiftFiltering_0(
                src.nativeObj,
                dst.nativeObj,
                sp,
                sr,
                maxLevel,
                termcrit.type,
                termcrit.maxCount,
                termcrit.epsilon);
    }

    /**
     * Performs initial step of meanshift segmentation of an image.
     *
     * @param src      The source 8-bit, 3-channel image
     * @param dst      The destination image of the same format and the same size as the source
     * @param sp       The spatial window radius
     * @param sr       The color window radius
     * @param maxLevel Maximum level of the pyramid for the segmentation
     * @throws NullPointerException if src, dst is null
     */
    public static void pyrMeanShiftFiltering(Mat src, Mat dst, double sp, double sr, int maxLevel) {
        pyrMeanShiftFiltering_1(src.nativeObj, dst.nativeObj, sp, sr, maxLevel);
    }

    /**
     * Performs initial step of meanshift segmentation of an image.
     *
     * @param src The source 8-bit, 3-channel image
     * @param dst The destination image of the same format and the same size as the source
     * @param sp  The spatial window radius
     * @param sr  The color window radius
     * @throws NullPointerException if src, dst is null
     */
    public static void pyrMeanShiftFiltering(Mat src, Mat dst, double sp, double sr) {
        pyrMeanShiftFiltering_2(src.nativeObj, dst.nativeObj, sp, sr);
    }

    /**
     * Runs the GrabCut algorithm. The function implements the GrabCut image segmentation algorithm
     * (https://en.wikipedia.org/wiki/GrabCut).
     *
     * @param img       Input 8-bit 3-channel image
     * @param mask      Input/output 8-bit single-channel mask. The mask is initialized by the function when mode is set
     *                  to GC_INIT_WITH_RECT. Its elements may have one of the GrabCutClasses
     * @param rect      ROI containing a segmented object. The pixels outside of the ROI are marked as "obvious
     *                  background". The parameter is only used when mode==GC_INIT_WITH_RECT
     * @param bgdModel  Temporary array for the background model. Do not modify it while you are processing the same
     *                  image
     * @param fgdModel  Temporary arrays for the foreground model. Do not modify it while you are processing the same
     *                  image
     * @param iterCount Number of iterations the algorithm should make before returning the result. Note that the result
     *                  can be refined with further calls with mode==GC_INIT_WITH_MASK or mode==GC_EVAL
     * @param mode      Operation mode that could be one of the GrabCutModes
     * @throws NullPointerException if img, mask, rect, bgdModel, fgdModel is null
     */
    public static void grabCut(Mat img, Mat mask, Rect rect, Mat bgdModel, Mat fgdModel, int iterCount, int mode) {
        grabCut_0(
                img.nativeObj,
                mask.nativeObj,
                rect.x,
                rect.y,
                rect.width,
                rect.height,
                bgdModel.nativeObj,
                fgdModel.nativeObj,
                iterCount,
                mode);
    }

    /**
     * Runs the GrabCut algorithm. The function implements the GrabCut image segmentation algorithm
     * (https://en.wikipedia.org/wiki/GrabCut).
     *
     * @param img       Input 8-bit 3-channel image
     * @param mask      Input/output 8-bit single-channel mask. The mask is initialized by the function when mode is set
     *                  to GC_INIT_WITH_RECT. Its elements may have one of the GrabCutClasses
     * @param rect      ROI containing a segmented object. The pixels outside of the ROI are marked as "obvious
     *                  background". The parameter is only used when mode==GC_INIT_WITH_RECT
     * @param bgdModel  Temporary array for the background model. Do not modify it while you are processing the same
     *                  image
     * @param fgdModel  Temporary arrays for the foreground model. Do not modify it while you are processing the same
     *                  image
     * @param iterCount Number of iterations the algorithm should make before returning the result. Note that the result
     *                  can be refined with further calls with mode==GC_INIT_WITH_MASK or mode==GC_EVAL
     * @throws NullPointerException if img, mask, rect, bgdModel, fgdModel is null
     */
    public static void grabCut(Mat img, Mat mask, Rect rect, Mat bgdModel, Mat fgdModel, int iterCount) {
        grabCut_1(
                img.nativeObj,
                mask.nativeObj,
                rect.x,
                rect.y,
                rect.width,
                rect.height,
                bgdModel.nativeObj,
                fgdModel.nativeObj,
                iterCount);
    }

    /**
     * Calculates the distance to the closest zero pixel for each pixel of the source image. The function
     * distanceTransform calculates the approximate or precise distance from every binary image pixel to the nearest
     * zero pixel. For zero image pixels, the distance will obviously be zero. When maskSize == DIST_MASK_PRECISE and
     * distanceType == DIST_L2 , the function runs the algorithm described in This algorithm is parallelized with the
     * TBB library. In other cases, the algorithm is used. This means that for a pixel the function finds the shortest
     * path to the nearest zero pixel consisting of basic shifts: horizontal, vertical, diagonal, or knight's move (the
     * latest is available for a 5times 5 mask). The overall distance is calculated as a sum of these basic distances.
     * Since the distance function should be symmetric, all of the horizontal and vertical shifts must have the same
     * cost (denoted as a ), all the diagonal shifts must have the same cost (denoted as b), and all knight's moves must
     * have the same cost (denoted as c). For the DIST_C and DIST_L1 types, the distance is calculated precisely,
     * whereas for DIST_L2 (Euclidean distance) the distance can be calculated only with a relative error (a 5times 5
     * mask gives more accurate results). For a,b, and c, OpenCV uses the values suggested in the original paper:
     * DIST_L1: a = 1, b = 2 DIST_L2: 3 x 3: a=0.955, b=1.3693 5 x 5: a=1, b=1.4, c=2.1969 DIST_C: a = 1, b = 1
     * Typically, for a fast, coarse distance estimation DIST_L2, a 3times 3 mask is used. For a more accurate distance
     * estimation DIST_L2, a 5times 5 mask or the precise algorithm is used. Note that both the precise and the
     * approximate algorithms are linear on the number of pixels. This variant of the function does not only compute the
     * minimum distance for each pixel (x, y) but also identifies the nearest connected component consisting of zero
     * pixels (labelType==DIST_LABEL_CCOMP) or the nearest zero pixel (labelType==DIST_LABEL_PIXEL). Index of the
     * component/pixel is stored in labels(x, y). When labelType==DIST_LABEL_CCOMP, the function automatically finds
     * connected components of zero pixels in the input image and marks them with distinct labels. When
     * labelType==DIST_LABEL_PIXEL, the function scans through the input image and marks all the zero pixels with
     * distinct labels. In this mode, the complexity is still linear. That is, the function provides a very fast way to
     * compute the Voronoi diagram for a binary image. Currently, the second variant can use only the approximate
     * distance transform algorithm, i.e. maskSize=DIST_MASK_PRECISE is not supported yet.
     *
     * @param src          8-bit, single-channel (binary) source image
     * @param dst          Output image with calculated distances. It is a 8-bit or 32-bit floating-point,
     *                     single-channel image of the same size as src
     * @param labels       Output 2D array of labels (the discrete Voronoi diagram). It has the type CV_32SC1 and the
     *                     same size as src
     * @param distanceType Type of distance, see DistanceTypes
     * @param maskSize     Size of the distance transform mask, see DistanceTransformMasks. DIST_MASK_PRECISE is not
     *                     supported by this variant. In case of the DIST_L1 or DIST_C distance type, the parameter is
     *                     forced to 3 because a 3 x 3 mask gives the same result as 5 x 5 or any larger aperture
     * @param labelType    Type of the label array to build, see DistanceTransformLabelTypes
     * @throws NullPointerException if src, dst, labels is null
     */
    public static void distanceTransformWithLabels(
            Mat src,
            Mat dst,
            Mat labels,
            int distanceType,
            int maskSize,
            int labelType) {
        distanceTransformWithLabels_0(
                src.nativeObj,
                dst.nativeObj,
                labels.nativeObj,
                distanceType,
                maskSize,
                labelType);
    }

    /**
     * Calculates the distance to the closest zero pixel for each pixel of the source image. The function
     * distanceTransform calculates the approximate or precise distance from every binary image pixel to the nearest
     * zero pixel. For zero image pixels, the distance will obviously be zero. When maskSize == DIST_MASK_PRECISE and
     * distanceType == DIST_L2 , the function runs the algorithm described in This algorithm is parallelized with the
     * TBB library. In other cases, the algorithm is used. This means that for a pixel the function finds the shortest
     * path to the nearest zero pixel consisting of basic shifts: horizontal, vertical, diagonal, or knight's move (the
     * latest is available for a 5times 5 mask). The overall distance is calculated as a sum of these basic distances.
     * Since the distance function should be symmetric, all of the horizontal and vertical shifts must have the same
     * cost (denoted as a ), all the diagonal shifts must have the same cost (denoted as b), and all knight's moves must
     * have the same cost (denoted as c). For the DIST_C and DIST_L1 types, the distance is calculated precisely,
     * whereas for DIST_L2 (Euclidean distance) the distance can be calculated only with a relative error (a 5times 5
     * mask gives more accurate results). For a,b, and c, OpenCV uses the values suggested in the original paper:
     * DIST_L1: a = 1, b = 2 DIST_L2: 3 x 3: a=0.955, b=1.3693 5 x 5: a=1, b=1.4, c=2.1969 DIST_C: a = 1, b = 1
     * Typically, for a fast, coarse distance estimation DIST_L2, a 3times 3 mask is used. For a more accurate distance
     * estimation DIST_L2, a 5times 5 mask or the precise algorithm is used. Note that both the precise and the
     * approximate algorithms are linear on the number of pixels. This variant of the function does not only compute the
     * minimum distance for each pixel (x, y) but also identifies the nearest connected component consisting of zero
     * pixels (labelType==DIST_LABEL_CCOMP) or the nearest zero pixel (labelType==DIST_LABEL_PIXEL). Index of the
     * component/pixel is stored in labels(x, y). When labelType==DIST_LABEL_CCOMP, the function automatically finds
     * connected components of zero pixels in the input image and marks them with distinct labels. When
     * labelType==DIST_LABEL_PIXEL, the function scans through the input image and marks all the zero pixels with
     * distinct labels. In this mode, the complexity is still linear. That is, the function provides a very fast way to
     * compute the Voronoi diagram for a binary image. Currently, the second variant can use only the approximate
     * distance transform algorithm, i.e. maskSize=DIST_MASK_PRECISE is not supported yet.
     *
     * @param src          8-bit, single-channel (binary) source image
     * @param dst          Output image with calculated distances. It is a 8-bit or 32-bit floating-point,
     *                     single-channel image of the same size as src
     * @param labels       Output 2D array of labels (the discrete Voronoi diagram). It has the type CV_32SC1 and the
     *                     same size as src
     * @param distanceType Type of distance, see DistanceTypes
     * @param maskSize     Size of the distance transform mask, see DistanceTransformMasks. DIST_MASK_PRECISE is not
     *                     supported by this variant. In case of the DIST_L1 or DIST_C distance type, the parameter is
     *                     forced to 3 because a 3 x 3 mask gives the same result as 5 x 5 or any larger aperture
     * @throws NullPointerException if src, dst, labels is null
     */
    public static void distanceTransformWithLabels(Mat src, Mat dst, Mat labels, int distanceType, int maskSize) {
        distanceTransformWithLabels_1(src.nativeObj, dst.nativeObj, labels.nativeObj, distanceType, maskSize);
    }

    /**
     * Computes the distance of each nonzero source pixel to the nearest zero pixel.
     *
     * @param src          8-bit, single-channel (binary) source image
     * @param dst          Output image with calculated distances. It is a 8-bit or 32-bit floating-point,
     *                     single-channel image of the same size as src
     * @param distanceType Type of distance, see DistanceTypes
     * @param maskSize     Size of the distance transform mask, see DistanceTransformMasks. In case of the DIST_L1 or
     *                     DIST_C distance type, the parameter is forced to 3 because a 3 x 3 mask gives the same result
     *                     as 5 x 5 or any larger aperture
     * @param dstType      Type of output image. It can be CV_8U or CV_32F. Type CV_8U can be used only for the first
     *                     variant of the function and distanceType == DIST_L1
     * @throws NullPointerException if src, dst is null
     */
    public static void distanceTransform(Mat src, Mat dst, int distanceType, int maskSize, int dstType) {
        distanceTransform_0(src.nativeObj, dst.nativeObj, distanceType, maskSize, dstType);
    }

    /**
     * Computes the distance of each nonzero source pixel to the nearest zero pixel.
     *
     * @param src          8-bit, single-channel (binary) source image
     * @param dst          Output image with calculated distances. It is a 8-bit or 32-bit floating-point,
     *                     single-channel image of the same size as src
     * @param distanceType Type of distance, see DistanceTypes
     * @param maskSize     Size of the distance transform mask, see DistanceTransformMasks. In case of the DIST_L1 or
     *                     DIST_C distance type, the parameter is forced to 3 because a 3 x 3 mask gives the same result
     *                     as 5 x 5 or any larger aperture
     * @throws NullPointerException if src, dst is null
     */
    public static void distanceTransform(Mat src, Mat dst, int distanceType, int maskSize) {
        distanceTransform_1(src.nativeObj, dst.nativeObj, distanceType, maskSize);
    }

    /**
     * Fills a connected component with the given color.
     *
     * @param image     Input/output 1- or 3-channel, 8-bit, or floating-point image. It is modified by the function
     *                  unless the FLOODFILL_MASK_ONLY flag is set in the second variant of the function. See the
     *                  details below
     * @param mask      Operation mask that should be a single-channel 8-bit image, 2 pixels wider and 2 pixels taller
     *                  than image. If an empty Mat is passed it will be created automatically. Since this is both an
     *                  input and output parameter, you must take responsibility of initializing it. Flood-filling
     *                  cannot go across non-zero pixels in the input mask. For example, an edge detector output can be
     *                  used as a mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                  pixels in the image are set to 1 or to the specified value in flags as described below.
     *                  Additionally, the function fills the border of the mask with ones to simplify internal
     *                  processing. It is therefore possible to use the same mask in multiple calls to the function to
     *                  make sure the filled areas do not overlap
     * @param seedPoint Starting point
     * @param newVal    New value of the repainted domain pixels
     * @param rect      Optional output parameter set by the function to the minimum bounding rectangle of the repainted
     *                  domain
     * @param loDiff    Maximal lower brightness/color difference between the currently observed pixel and one of its
     *                  neighbors belonging to the component, or a seed pixel being added to the component
     * @param upDiff    Maximal upper brightness/color difference between the currently observed pixel and one of its
     *                  neighbors belonging to the component, or a seed pixel being added to the component
     * @param flags     Operation flags. The first 8 bits contain a connectivity value. The default value of 4 means
     *                  that only the four nearest neighbor pixels (those that share an edge) are considered. A
     *                  connectivity value of 8 means that the eight nearest neighbor pixels (those that share a corner)
     *                  will be considered. The next 8 bits (8-16) contain a value between 1 and 255 with which to fill
     *                  the mask (the default value is 1). For example, 4 | ( 255 &lt;&lt; 8 ) will consider 4 nearest
     *                  neighbours and fill the mask with a value of 255. The following additional options occupy higher
     *                  bits and therefore may be further combined with the connectivity and mask fill values using
     *                  bit-wise or (|), see FloodFillFlags. Note: Since the mask is larger than the filled image, a
     *                  pixel (x, y) in image corresponds to the pixel (x+1, y+1) in the mask
     * @return the number of pixels included in the filled region
     * @throws NullPointerException if image, mask, seedPoint, newVal, loDiff, upDiff is null
     */
    public static int floodFill(
            Mat image,
            Mat mask,
            Point seedPoint,
            Scalar newVal,
            Rect rect,
            Scalar loDiff,
            Scalar upDiff,
            int flags) {
        double[] rect_out = new double[4];
        int retVal = floodFill_0(
                image.nativeObj,
                mask.nativeObj,
                seedPoint.x,
                seedPoint.y,
                newVal.val[0],
                newVal.val[1],
                newVal.val[2],
                newVal.val[3],
                rect_out,
                loDiff.val[0],
                loDiff.val[1],
                loDiff.val[2],
                loDiff.val[3],
                upDiff.val[0],
                upDiff.val[1],
                upDiff.val[2],
                upDiff.val[3],
                flags);
        if (rect != null) {
            rect.x = (int) rect_out[0];
            rect.y = (int) rect_out[1];
            rect.width = (int) rect_out[2];
            rect.height = (int) rect_out[3];
        }
        return retVal;
    }

    /**
     * Fills a connected component with the given color.
     *
     * @param image     Input/output 1- or 3-channel, 8-bit, or floating-point image. It is modified by the function
     *                  unless the FLOODFILL_MASK_ONLY flag is set in the second variant of the function. See the
     *                  details below
     * @param mask      Operation mask that should be a single-channel 8-bit image, 2 pixels wider and 2 pixels taller
     *                  than image. If an empty Mat is passed it will be created automatically. Since this is both an
     *                  input and output parameter, you must take responsibility of initializing it. Flood-filling
     *                  cannot go across non-zero pixels in the input mask. For example, an edge detector output can be
     *                  used as a mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                  pixels in the image are set to 1 or to the specified value in flags as described below.
     *                  Additionally, the function fills the border of the mask with ones to simplify internal
     *                  processing. It is therefore possible to use the same mask in multiple calls to the function to
     *                  make sure the filled areas do not overlap
     * @param seedPoint Starting point
     * @param newVal    New value of the repainted domain pixels
     * @param rect      Optional output parameter set by the function to the minimum bounding rectangle of the repainted
     *                  domain
     * @param loDiff    Maximal lower brightness/color difference between the currently observed pixel and one of its
     *                  neighbors belonging to the component, or a seed pixel being added to the component
     * @param upDiff    Maximal upper brightness/color difference between the currently observed pixel and one of its
     *                  neighbors belonging to the component, or a seed pixel being added to the component
     * @return the number of pixels included in the filled region
     * @throws NullPointerException if image, mask, seedPoint, newVal, loDiff, upDiff is null
     */
    public static int floodFill(
            Mat image,
            Mat mask,
            Point seedPoint,
            Scalar newVal,
            Rect rect,
            Scalar loDiff,
            Scalar upDiff) {
        double[] rect_out = new double[4];
        int retVal = floodFill_1(
                image.nativeObj,
                mask.nativeObj,
                seedPoint.x,
                seedPoint.y,
                newVal.val[0],
                newVal.val[1],
                newVal.val[2],
                newVal.val[3],
                rect_out,
                loDiff.val[0],
                loDiff.val[1],
                loDiff.val[2],
                loDiff.val[3],
                upDiff.val[0],
                upDiff.val[1],
                upDiff.val[2],
                upDiff.val[3]);
        if (rect != null) {
            rect.x = (int) rect_out[0];
            rect.y = (int) rect_out[1];
            rect.width = (int) rect_out[2];
            rect.height = (int) rect_out[3];
        }
        return retVal;
    }

    /**
     * Fills a connected component with the given color.
     *
     * @param image     Input/output 1- or 3-channel, 8-bit, or floating-point image. It is modified by the function
     *                  unless the FLOODFILL_MASK_ONLY flag is set in the second variant of the function. See the
     *                  details below
     * @param mask      Operation mask that should be a single-channel 8-bit image, 2 pixels wider and 2 pixels taller
     *                  than image. If an empty Mat is passed it will be created automatically. Since this is both an
     *                  input and output parameter, you must take responsibility of initializing it. Flood-filling
     *                  cannot go across non-zero pixels in the input mask. For example, an edge detector output can be
     *                  used as a mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                  pixels in the image are set to 1 or to the specified value in flags as described below.
     *                  Additionally, the function fills the border of the mask with ones to simplify internal
     *                  processing. It is therefore possible to use the same mask in multiple calls to the function to
     *                  make sure the filled areas do not overlap
     * @param seedPoint Starting point
     * @param newVal    New value of the repainted domain pixels
     * @param rect      Optional output parameter set by the function to the minimum bounding rectangle of the repainted
     *                  domain
     * @param loDiff    Maximal lower brightness/color difference between the currently observed pixel and one of its
     *                  neighbors belonging to the component, or a seed pixel being added to the component
     * @return the number of pixels included in the filled region
     * @throws NullPointerException if image, mask, seedPoint, newVal, loDiff is null
     */
    public static int floodFill(Mat image, Mat mask, Point seedPoint, Scalar newVal, Rect rect, Scalar loDiff) {
        double[] rect_out = new double[4];
        int retVal = floodFill_2(
                image.nativeObj,
                mask.nativeObj,
                seedPoint.x,
                seedPoint.y,
                newVal.val[0],
                newVal.val[1],
                newVal.val[2],
                newVal.val[3],
                rect_out,
                loDiff.val[0],
                loDiff.val[1],
                loDiff.val[2],
                loDiff.val[3]);
        if (rect != null) {
            rect.x = (int) rect_out[0];
            rect.y = (int) rect_out[1];
            rect.width = (int) rect_out[2];
            rect.height = (int) rect_out[3];
        }
        return retVal;
    }

    /**
     * Fills a connected component with the given color.
     *
     * @param image     Input/output 1- or 3-channel, 8-bit, or floating-point image. It is modified by the function
     *                  unless the FLOODFILL_MASK_ONLY flag is set in the second variant of the function. See the
     *                  details below
     * @param mask      Operation mask that should be a single-channel 8-bit image, 2 pixels wider and 2 pixels taller
     *                  than image. If an empty Mat is passed it will be created automatically. Since this is both an
     *                  input and output parameter, you must take responsibility of initializing it. Flood-filling
     *                  cannot go across non-zero pixels in the input mask. For example, an edge detector output can be
     *                  used as a mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                  pixels in the image are set to 1 or to the specified value in flags as described below.
     *                  Additionally, the function fills the border of the mask with ones to simplify internal
     *                  processing. It is therefore possible to use the same mask in multiple calls to the function to
     *                  make sure the filled areas do not overlap
     * @param seedPoint Starting point
     * @param newVal    New value of the repainted domain pixels
     * @param rect      Optional output parameter set by the function to the minimum bounding rectangle of the repainted
     *                  domain
     * @return the number of pixels included in the filled region
     * @throws NullPointerException if image, mask, seedPoint, newVal is null
     */
    public static int floodFill(Mat image, Mat mask, Point seedPoint, Scalar newVal, Rect rect) {
        double[] rect_out = new double[4];
        int retVal = floodFill_3(
                image.nativeObj,
                mask.nativeObj,
                seedPoint.x,
                seedPoint.y,
                newVal.val[0],
                newVal.val[1],
                newVal.val[2],
                newVal.val[3],
                rect_out);
        if (rect != null) {
            rect.x = (int) rect_out[0];
            rect.y = (int) rect_out[1];
            rect.width = (int) rect_out[2];
            rect.height = (int) rect_out[3];
        }
        return retVal;
    }

    /**
     * Fills a connected component with the given color.
     *
     * @param image     Input/output 1- or 3-channel, 8-bit, or floating-point image. It is modified by the function
     *                  unless the FLOODFILL_MASK_ONLY flag is set in the second variant of the function. See the
     *                  details below
     * @param mask      Operation mask that should be a single-channel 8-bit image, 2 pixels wider and 2 pixels taller
     *                  than image. If an empty Mat is passed it will be created automatically. Since this is both an
     *                  input and output parameter, you must take responsibility of initializing it. Flood-filling
     *                  cannot go across non-zero pixels in the input mask. For example, an edge detector output can be
     *                  used as a mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                  pixels in the image are set to 1 or to the specified value in flags as described below.
     *                  Additionally, the function fills the border of the mask with ones to simplify internal
     *                  processing. It is therefore possible to use the same mask in multiple calls to the function to
     *                  make sure the filled areas do not overlap
     * @param seedPoint Starting point
     * @param newVal    New value of the repainted domain pixels
     * @return the number of pixels included in the filled region
     * @throws NullPointerException if image, mask, seedPoint, newVal is null
     */
    public static int floodFill(Mat image, Mat mask, Point seedPoint, Scalar newVal) {
        return floodFill_4(
                image.nativeObj,
                mask.nativeObj,
                seedPoint.x,
                seedPoint.y,
                newVal.val[0],
                newVal.val[1],
                newVal.val[2],
                newVal.val[3]);
    }

    /**
     * Variant without mask parameter.
     *
     * @param src1     first source matrix or channel value
     * @param src2     second source matrix or channel value
     * @param weights1 weights of the first discrete distribution
     * @param weights2 weights of the second discrete distribution
     * @param dst      destination matrix that receives the result
     * @throws NullPointerException if src1, src2, weights1, weights2, dst is null
     */
    public static void blendLinear(Mat src1, Mat src2, Mat weights1, Mat weights2, Mat dst) {
        blendLinear_0(src1.nativeObj, src2.nativeObj, weights1.nativeObj, weights2.nativeObj, dst.nativeObj);
    }

    /**
     * Composites two images according to their alpha channels, using a Porter-Duff operator. The default operator,
     * ALPHA_COMPOSITE_OVER, draws overlay on top of background and blends the two according to overlay's alpha channel,
     * so that partially-transparent pixels mix smoothly into the background with no fringing at the edges. This is what
     * is usually wanted when pasting an RGBA sprite, an antialiased glyph or a segmentation mask onto a picture. The
     * other operators of AlphaCompositeOperations select the rest of the Porter-Duff set, for instance
     * ALPHA_COMPOSITE_ATOP to clip the overlay to the background's shape, or ALPHA_COMPOSITE_DEST_OUT to punch a hole
     * in the background. Only the compositing operator is configurable. The blend mode is always *normal*: the
     * overlay's color is taken as-is and is never mixed with the background's color the way the separable blend modes
     * of the same specification (multiply, screen, overlay, darken, and so on) do. Blend modes are a separate axis and
     * are not implemented here. All operators are evaluated in premultiplied alpha space, where each one reduces to a
     * pair of per-pixel weights F_a, F_b listed in AlphaCompositeOperations: C_o = C_s F_a + C_b F_b, qquad alpha_o =
     * alpha_s F_a + alpha_b F_b with C_s, alpha_s taken from overlay and C_b, alpha_b from background. Inputs that
     * carry straight (unassociated) alpha are premultiplied on entry and the result is un-premultiplied on exit. Both
     * steps reuse cvtColor with COLOR_RGBA2mRGBA and COLOR_mRGBA2RGBA, so alphaComposite follows exactly the same alpha
     * conventions as those conversions.
     *
     * @param overlay       8-bit 4-channel image (RGBA or BGRA; channel order does not matter as long as it matches
     *                      background) acting as the source of the operator; must be the same size as background
     * @param background    8-bit 3-channel or 4-channel image acting as the destination of the operator. A 4-channel
     *                      background contributes its own alpha to the blend, using the same straight/premultiplied
     *                      convention as overlay, and dst has 4 channels holding the composited alpha alpha_o. A
     *                      3-channel background is taken to be fully opaque (alpha_b = 1) and dst has 3 channels; since
     *                      such a dst cannot store alpha_o, the alpha is dropped and the premultiplied color is
     *                      written, which is what Cairo does when the target surface has no alpha channel. Operators
     *                      whose result is not opaque (ALPHA_COMPOSITE_CLEAR, ALPHA_COMPOSITE_OUT, ALPHA_COMPOSITE_XOR
     *                      and others) therefore darken toward black on a 3-channel background; pass a 4-channel
     *                      background to keep the alpha
     * @param dst           output image, created with the same size and type as background. It may be the same array as
     *                      background
     * @param op            compositing operator, one of AlphaCompositeOperations
     * @param premultiplied false (the default) if overlay's alpha (and background's, when it has one) is
     *                      straight/unassociated, as produced by e.g. imread(..., IMREAD_UNCHANGED); true if it is
     *                      already premultiplied, which skips the internal conversion
     * @throws NullPointerException if overlay, background, dst is null
     */
    public static void alphaComposite(Mat overlay, Mat background, Mat dst, int op, boolean premultiplied) {
        alphaComposite_0(overlay.nativeObj, background.nativeObj, dst.nativeObj, op, premultiplied);
    }

    /**
     * Composites two images according to their alpha channels, using a Porter-Duff operator. The default operator,
     * ALPHA_COMPOSITE_OVER, draws overlay on top of background and blends the two according to overlay's alpha channel,
     * so that partially-transparent pixels mix smoothly into the background with no fringing at the edges. This is what
     * is usually wanted when pasting an RGBA sprite, an antialiased glyph or a segmentation mask onto a picture. The
     * other operators of AlphaCompositeOperations select the rest of the Porter-Duff set, for instance
     * ALPHA_COMPOSITE_ATOP to clip the overlay to the background's shape, or ALPHA_COMPOSITE_DEST_OUT to punch a hole
     * in the background. Only the compositing operator is configurable. The blend mode is always *normal*: the
     * overlay's color is taken as-is and is never mixed with the background's color the way the separable blend modes
     * of the same specification (multiply, screen, overlay, darken, and so on) do. Blend modes are a separate axis and
     * are not implemented here. All operators are evaluated in premultiplied alpha space, where each one reduces to a
     * pair of per-pixel weights F_a, F_b listed in AlphaCompositeOperations: C_o = C_s F_a + C_b F_b, qquad alpha_o =
     * alpha_s F_a + alpha_b F_b with C_s, alpha_s taken from overlay and C_b, alpha_b from background. Inputs that
     * carry straight (unassociated) alpha are premultiplied on entry and the result is un-premultiplied on exit. Both
     * steps reuse cvtColor with COLOR_RGBA2mRGBA and COLOR_mRGBA2RGBA, so alphaComposite follows exactly the same alpha
     * conventions as those conversions.
     *
     * @param overlay    8-bit 4-channel image (RGBA or BGRA; channel order does not matter as long as it matches
     *                   background) acting as the source of the operator; must be the same size as background
     * @param background 8-bit 3-channel or 4-channel image acting as the destination of the operator. A 4-channel
     *                   background contributes its own alpha to the blend, using the same straight/premultiplied
     *                   convention as overlay, and dst has 4 channels holding the composited alpha alpha_o. A 3-channel
     *                   background is taken to be fully opaque (alpha_b = 1) and dst has 3 channels; since such a dst
     *                   cannot store alpha_o, the alpha is dropped and the premultiplied color is written, which is
     *                   what Cairo does when the target surface has no alpha channel. Operators whose result is not
     *                   opaque (ALPHA_COMPOSITE_CLEAR, ALPHA_COMPOSITE_OUT, ALPHA_COMPOSITE_XOR and others) therefore
     *                   darken toward black on a 3-channel background; pass a 4-channel background to keep the alpha
     * @param dst        output image, created with the same size and type as background. It may be the same array as
     *                   background
     * @param op         compositing operator, one of AlphaCompositeOperations
     * @throws NullPointerException if overlay, background, dst is null
     */
    public static void alphaComposite(Mat overlay, Mat background, Mat dst, int op) {
        alphaComposite_1(overlay.nativeObj, background.nativeObj, dst.nativeObj, op);
    }

    /**
     * Composites two images according to their alpha channels, using a Porter-Duff operator. The default operator,
     * ALPHA_COMPOSITE_OVER, draws overlay on top of background and blends the two according to overlay's alpha channel,
     * so that partially-transparent pixels mix smoothly into the background with no fringing at the edges. This is what
     * is usually wanted when pasting an RGBA sprite, an antialiased glyph or a segmentation mask onto a picture. The
     * other operators of AlphaCompositeOperations select the rest of the Porter-Duff set, for instance
     * ALPHA_COMPOSITE_ATOP to clip the overlay to the background's shape, or ALPHA_COMPOSITE_DEST_OUT to punch a hole
     * in the background. Only the compositing operator is configurable. The blend mode is always *normal*: the
     * overlay's color is taken as-is and is never mixed with the background's color the way the separable blend modes
     * of the same specification (multiply, screen, overlay, darken, and so on) do. Blend modes are a separate axis and
     * are not implemented here. All operators are evaluated in premultiplied alpha space, where each one reduces to a
     * pair of per-pixel weights F_a, F_b listed in AlphaCompositeOperations: C_o = C_s F_a + C_b F_b, qquad alpha_o =
     * alpha_s F_a + alpha_b F_b with C_s, alpha_s taken from overlay and C_b, alpha_b from background. Inputs that
     * carry straight (unassociated) alpha are premultiplied on entry and the result is un-premultiplied on exit. Both
     * steps reuse cvtColor with COLOR_RGBA2mRGBA and COLOR_mRGBA2RGBA, so alphaComposite follows exactly the same alpha
     * conventions as those conversions.
     *
     * @param overlay    8-bit 4-channel image (RGBA or BGRA; channel order does not matter as long as it matches
     *                   background) acting as the source of the operator; must be the same size as background
     * @param background 8-bit 3-channel or 4-channel image acting as the destination of the operator. A 4-channel
     *                   background contributes its own alpha to the blend, using the same straight/premultiplied
     *                   convention as overlay, and dst has 4 channels holding the composited alpha alpha_o. A 3-channel
     *                   background is taken to be fully opaque (alpha_b = 1) and dst has 3 channels; since such a dst
     *                   cannot store alpha_o, the alpha is dropped and the premultiplied color is written, which is
     *                   what Cairo does when the target surface has no alpha channel. Operators whose result is not
     *                   opaque (ALPHA_COMPOSITE_CLEAR, ALPHA_COMPOSITE_OUT, ALPHA_COMPOSITE_XOR and others) therefore
     *                   darken toward black on a 3-channel background; pass a 4-channel background to keep the alpha
     * @param dst        output image, created with the same size and type as background. It may be the same array as
     *                   background
     * @throws NullPointerException if overlay, background, dst is null
     */
    public static void alphaComposite(Mat overlay, Mat background, Mat dst) {
        alphaComposite_2(overlay.nativeObj, background.nativeObj, dst.nativeObj);
    }

    /**
     * Converts image channel values between the color representations selected by code. Supported source depths and
     * channel counts depend on the conversion. Use a zero destination channel count to select the conversion default.
     *
     * @param src   input image: 8-bit unsigned, 16-bit unsigned ( CV_16UC... ), or single-precision floating-point
     * @param dst   output image of the same size and depth as src
     * @param code  color space conversion code (see ColorConversionCodes)
     * @param dstCn number of channels in the destination image; if the parameter is 0, the number of the channels is
     *              derived automatically from src and code
     * @param hint  Implementation modification flags. See AlgorithmHint Note: The source image (src) must be of an
     *              appropriate type for the desired color conversion. see ColorConversionCodes
     * @throws NullPointerException if src, dst is null
     */
    public static void cvtColor(Mat src, Mat dst, int code, int dstCn, int hint) {
        cvtColor_0(src.nativeObj, dst.nativeObj, code, dstCn, hint);
    }

    /**
     * Converts image channel values between the color representations selected by code. Supported source depths and
     * channel counts depend on the conversion. Use a zero destination channel count to select the conversion default.
     *
     * @param src   input image: 8-bit unsigned, 16-bit unsigned ( CV_16UC... ), or single-precision floating-point
     * @param dst   output image of the same size and depth as src
     * @param code  color space conversion code (see ColorConversionCodes)
     * @param dstCn number of channels in the destination image; if the parameter is 0, the number of the channels is
     *              derived automatically from src and code
     * @throws NullPointerException if src, dst is null
     */
    public static void cvtColor(Mat src, Mat dst, int code, int dstCn) {
        cvtColor_1(src.nativeObj, dst.nativeObj, code, dstCn);
    }

    /**
     * Converts image channel values between the color representations selected by code. Supported source depths and
     * channel counts depend on the conversion. Use a zero destination channel count to select the conversion default.
     *
     * @param src  input image: 8-bit unsigned, 16-bit unsigned ( CV_16UC... ), or single-precision floating-point
     * @param dst  output image of the same size and depth as src
     * @param code color space conversion code (see ColorConversionCodes)
     * @throws NullPointerException if src, dst is null
     */
    public static void cvtColor(Mat src, Mat dst, int code) {
        cvtColor_2(src.nativeObj, dst.nativeObj, code);
    }

    /**
     * Converts an image from one color space to another where the source image is stored in two planes. This function
     * only supports YUV420 to RGB conversion as of now.
     *
     * @param src1 8-bit image (CV_8U) of the Y plane
     * @param src2 image containing interleaved U/V plane
     * @param dst  output image
     * @param code Specifies the type of conversion. It can take any of the following values: COLOR_YUV2BGR_NV12
     *             COLOR_YUV2RGB_NV12 COLOR_YUV2BGRA_NV12 COLOR_YUV2RGBA_NV12 COLOR_YUV2BGR_NV21 COLOR_YUV2RGB_NV21
     *             COLOR_YUV2BGRA_NV21 COLOR_YUV2RGBA_NV21
     * @param hint Implementation modification flags. See AlgorithmHint
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void cvtColorTwoPlane(Mat src1, Mat src2, Mat dst, int code, int hint) {
        cvtColorTwoPlane_0(src1.nativeObj, src2.nativeObj, dst.nativeObj, code, hint);
    }

    /**
     * Converts an image from one color space to another where the source image is stored in two planes. This function
     * only supports YUV420 to RGB conversion as of now.
     *
     * @param src1 8-bit image (CV_8U) of the Y plane
     * @param src2 image containing interleaved U/V plane
     * @param dst  output image
     * @param code Specifies the type of conversion. It can take any of the following values: COLOR_YUV2BGR_NV12
     *             COLOR_YUV2RGB_NV12 COLOR_YUV2BGRA_NV12 COLOR_YUV2RGBA_NV12 COLOR_YUV2BGR_NV21 COLOR_YUV2RGB_NV21
     *             COLOR_YUV2BGRA_NV21 COLOR_YUV2RGBA_NV21
     * @throws NullPointerException if src1, src2, dst is null
     */
    public static void cvtColorTwoPlane(Mat src1, Mat src2, Mat dst, int code) {
        cvtColorTwoPlane_1(src1.nativeObj, src2.nativeObj, dst.nativeObj, code);
    }

    /**
     * Main function for all demosaicing processes.
     *
     * @param src   input image: 8-bit unsigned or 16-bit unsigned
     * @param dst   output image of the same size and depth as src
     * @param code  Color space conversion code (see the description below)
     * @param dstCn number of channels in the destination image; if the parameter is 0, the number of the channels is
     *              derived automatically from src and code. The function can do the following transformations:
     *              Demosaicing using bilinear interpolation COLOR_BayerBG2BGR , COLOR_BayerGB2BGR , COLOR_BayerRG2BGR ,
     *              COLOR_BayerGR2BGR COLOR_BayerBG2GRAY , COLOR_BayerGB2GRAY , COLOR_BayerRG2GRAY , COLOR_BayerGR2GRAY
     *              Demosaicing using Variable Number of Gradients. COLOR_BayerBG2BGR_VNG , COLOR_BayerGB2BGR_VNG ,
     *              COLOR_BayerRG2BGR_VNG , COLOR_BayerGR2BGR_VNG Edge-Aware Demosaicing. COLOR_BayerBG2BGR_EA ,
     *              COLOR_BayerGB2BGR_EA , COLOR_BayerRG2BGR_EA , COLOR_BayerGR2BGR_EA Demosaicing with alpha channel
     *              COLOR_BayerBG2BGRA , COLOR_BayerGB2BGRA , COLOR_BayerRG2BGRA , COLOR_BayerGR2BGRA Note: The source
     *              image (src) must be of an appropriate type for the desired color conversion. see
     *              ColorConversionCodes
     * @throws NullPointerException if src, dst is null
     */
    public static void demosaicing(Mat src, Mat dst, int code, int dstCn) {
        demosaicing_0(src.nativeObj, dst.nativeObj, code, dstCn);
    }

    /**
     * Main function for all demosaicing processes.
     *
     * @param src  input image: 8-bit unsigned or 16-bit unsigned
     * @param dst  output image of the same size and depth as src
     * @param code Color space conversion code (see the description below)
     * @throws NullPointerException if src, dst is null
     */
    public static void demosaicing(Mat src, Mat dst, int code) {
        demosaicing_1(src.nativeObj, dst.nativeObj, code);
    }

    /**
     * Compares a template against overlapped image regions. The function slides through image , compares the overlapped
     * patches of size w times h against templ using the specified method and stores the comparison results in result .
     * TemplateMatchModes describes the formulae for the available comparison methods ( I denotes image, T template, R
     * result, M the optional mask ). The summation is done over template and/or the image patch: x' = 0...w-1, y' =
     * 0...h-1 After the function finishes the comparison, the best matches can be found as global minimums (when
     * TM_SQDIFF was used) or maximums (when TM_CCORR or TM_CCOEFF was used) using the minMaxLoc function. In case of a
     * color image, template summation in the numerator and each sum in the denominator is done over all of the channels
     * and separate mean values are used for each channel. That is, the function can take a color template and a color
     * image. The result will still be a single-channel image, which is easier to analyze.
     *
     * @param image  Image where the search is running. It must be 8-bit or 32-bit floating-point
     * @param templ  Searched template. It must be not greater than the source image and have the same data type
     * @param result Map of comparison results. It must be single-channel 32-bit floating-point. If image is W times H
     *               and templ is w times h , then result is (W-w+1) times (H-h+1)
     * @param method Parameter specifying the comparison method, see TemplateMatchModes
     * @param mask   Optional mask. It must have the same size as templ. It must either have the same number of channels
     *               as template or only one channel, which is then used for all template and image channels. If the
     *               data type is CV_8U, the mask is interpreted as a binary mask, meaning only elements where mask is
     *               nonzero are used and are kept unchanged independent of the actual mask value (weight equals 1). For
     *               data type CV_32F, the mask values are used as weights. The exact formulas are documented in
     *               TemplateMatchModes
     * @throws NullPointerException if image, templ, result, mask is null
     */
    public static void matchTemplate(Mat image, Mat templ, Mat result, int method, Mat mask) {
        matchTemplate_0(image.nativeObj, templ.nativeObj, result.nativeObj, method, mask.nativeObj);
    }

    /**
     * Compares a template against overlapped image regions. The function slides through image , compares the overlapped
     * patches of size w times h against templ using the specified method and stores the comparison results in result .
     * TemplateMatchModes describes the formulae for the available comparison methods ( I denotes image, T template, R
     * result, M the optional mask ). The summation is done over template and/or the image patch: x' = 0...w-1, y' =
     * 0...h-1 After the function finishes the comparison, the best matches can be found as global minimums (when
     * TM_SQDIFF was used) or maximums (when TM_CCORR or TM_CCOEFF was used) using the minMaxLoc function. In case of a
     * color image, template summation in the numerator and each sum in the denominator is done over all of the channels
     * and separate mean values are used for each channel. That is, the function can take a color template and a color
     * image. The result will still be a single-channel image, which is easier to analyze.
     *
     * @param image  Image where the search is running. It must be 8-bit or 32-bit floating-point
     * @param templ  Searched template. It must be not greater than the source image and have the same data type
     * @param result Map of comparison results. It must be single-channel 32-bit floating-point. If image is W times H
     *               and templ is w times h , then result is (W-w+1) times (H-h+1)
     * @param method Parameter specifying the comparison method, see TemplateMatchModes
     * @throws NullPointerException if image, templ, result is null
     */
    public static void matchTemplate(Mat image, Mat templ, Mat result, int method) {
        matchTemplate_1(image.nativeObj, templ.nativeObj, result.nativeObj, method);
    }

    /**
     * Computes the connected components labeled image of boolean image image with 4 or 8 way connectivity - returns N,
     * the total number of labels [0, N-1] where 0 represents the background label. ltype specifies the output label
     * image type, an important consideration based on the total number of labels or alternatively the total number of
     * pixels in the source image. ccltype specifies the connected components labeling algorithm to use, currently
     * Bolelli (Spaghetti) Grana (BBDT) and Wu's (SAUF) algorithms are supported, see the
     * ConnectedComponentsAlgorithmsTypes for details. Note that SAUF algorithm forces a row major ordering of labels
     * while Spaghetti and BBDT do not. This function uses parallel version of the algorithms if at least one allowed
     * parallel framework is enabled and if the rows of the image are at least twice the number returned by
     * getNumberOfCPUs.
     *
     * @param image        the 8-bit single-channel image to be labeled
     * @param labels       destination labeled image
     * @param connectivity 8 or 4 for 8-way or 4-way connectivity respectively
     * @param ltype        output image label type. Currently CV_32S and CV_16U are supported
     * @param ccltype      connected components algorithm type (see the ConnectedComponentsAlgorithmsTypes)
     * @return the number of labels, including label zero for the background
     * @throws NullPointerException if image, labels is null
     */
    public static int connectedComponentsWithAlgorithm(
            Mat image,
            Mat labels,
            int connectivity,
            int ltype,
            int ccltype) {
        return connectedComponentsWithAlgorithm_0(image.nativeObj, labels.nativeObj, connectivity, ltype, ccltype);
    }

    /**
     * Labels connected foreground components in a binary image; label zero represents the background.
     *
     * @param image        the 8-bit single-channel image to be labeled
     * @param labels       destination labeled image
     * @param connectivity 8 or 4 for 8-way or 4-way connectivity respectively
     * @param ltype        output image label type. Currently CV_32S and CV_16U are supported
     * @return the number of labels, including label zero for the background
     * @throws NullPointerException if image, labels is null
     */
    public static int connectedComponents(Mat image, Mat labels, int connectivity, int ltype) {
        return connectedComponents_0(image.nativeObj, labels.nativeObj, connectivity, ltype);
    }

    /**
     * Labels connected foreground components in a binary image; label zero represents the background.
     *
     * @param image        the 8-bit single-channel image to be labeled
     * @param labels       destination labeled image
     * @param connectivity 8 or 4 for 8-way or 4-way connectivity respectively
     * @return the number of labels, including label zero for the background
     * @throws NullPointerException if image, labels is null
     */
    public static int connectedComponents(Mat image, Mat labels, int connectivity) {
        return connectedComponents_1(image.nativeObj, labels.nativeObj, connectivity);
    }

    /**
     * Labels connected foreground components in a binary image; label zero represents the background.
     *
     * @param image  the 8-bit single-channel image to be labeled
     * @param labels destination labeled image
     * @return the number of labels, including label zero for the background
     * @throws NullPointerException if image, labels is null
     */
    public static int connectedComponents(Mat image, Mat labels) {
        return connectedComponents_2(image.nativeObj, labels.nativeObj);
    }

    /**
     * Computes the connected components labeled image of boolean image and also produces a statistics output for each
     * label image with 4 or 8 way connectivity - returns N, the total number of labels [0, N-1] where 0 represents the
     * background label. ltype specifies the output label image type, an important consideration based on the total
     * number of labels or alternatively the total number of pixels in the source image. ccltype specifies the connected
     * components labeling algorithm to use, currently Bolelli (Spaghetti) Grana (BBDT) and Wu's (SAUF) algorithms are
     * supported, see the ConnectedComponentsAlgorithmsTypes for details. Note that SAUF algorithm forces a row major
     * ordering of labels while Spaghetti and BBDT do not. This function uses parallel version of the algorithms
     * (statistics included) if at least one allowed parallel framework is enabled and if the rows of the image are at
     * least twice the number returned by getNumberOfCPUs.
     *
     * @param image        the 8-bit single-channel image to be labeled
     * @param labels       destination labeled image
     * @param stats        statistics output for each label, including the background label. Statistics are accessed via
     *                     stats(label, COLUMN) where COLUMN is one of ConnectedComponentsTypes, selecting the
     *                     statistic. The data type is CV_32S
     * @param centroids    centroid output for each label, including the background label. Centroids are accessed via
     *                     centroids(label, 0) for x and centroids(label, 1) for y. The data type CV_64F
     * @param connectivity 8 or 4 for 8-way or 4-way connectivity respectively
     * @param ltype        output image label type. Currently CV_32S and CV_16U are supported
     * @param ccltype      connected components algorithm type (see ConnectedComponentsAlgorithmsTypes)
     * @return the number of labels, including label zero for the background
     * @throws NullPointerException if image, labels, stats, centroids is null
     */
    public static int connectedComponentsWithStatsWithAlgorithm(
            Mat image,
            Mat labels,
            Mat stats,
            Mat centroids,
            int connectivity,
            int ltype,
            int ccltype) {
        return connectedComponentsWithStatsWithAlgorithm_0(
                image.nativeObj,
                labels.nativeObj,
                stats.nativeObj,
                centroids.nativeObj,
                connectivity,
                ltype,
                ccltype);
    }

    /**
     * Labels connected foreground components and computes their bounding boxes, pixel areas, and centroids.
     *
     * @param image        the 8-bit single-channel image to be labeled
     * @param labels       destination labeled image
     * @param stats        statistics output for each label, including the background label. Statistics are accessed via
     *                     stats(label, COLUMN) where COLUMN is one of ConnectedComponentsTypes, selecting the
     *                     statistic. The data type is CV_32S
     * @param centroids    centroid output for each label, including the background label. Centroids are accessed via
     *                     centroids(label, 0) for x and centroids(label, 1) for y. The data type CV_64F
     * @param connectivity 8 or 4 for 8-way or 4-way connectivity respectively
     * @param ltype        output image label type. Currently CV_32S and CV_16U are supported
     * @return the number of labels, including label zero for the background
     * @throws NullPointerException if image, labels, stats, centroids is null
     */
    public static int connectedComponentsWithStats(
            Mat image,
            Mat labels,
            Mat stats,
            Mat centroids,
            int connectivity,
            int ltype) {
        return connectedComponentsWithStats_0(
                image.nativeObj,
                labels.nativeObj,
                stats.nativeObj,
                centroids.nativeObj,
                connectivity,
                ltype);
    }

    /**
     * Labels connected foreground components and computes their bounding boxes, pixel areas, and centroids.
     *
     * @param image        the 8-bit single-channel image to be labeled
     * @param labels       destination labeled image
     * @param stats        statistics output for each label, including the background label. Statistics are accessed via
     *                     stats(label, COLUMN) where COLUMN is one of ConnectedComponentsTypes, selecting the
     *                     statistic. The data type is CV_32S
     * @param centroids    centroid output for each label, including the background label. Centroids are accessed via
     *                     centroids(label, 0) for x and centroids(label, 1) for y. The data type CV_64F
     * @param connectivity 8 or 4 for 8-way or 4-way connectivity respectively
     * @return the number of labels, including label zero for the background
     * @throws NullPointerException if image, labels, stats, centroids is null
     */
    public static int connectedComponentsWithStats(Mat image, Mat labels, Mat stats, Mat centroids, int connectivity) {
        return connectedComponentsWithStats_1(
                image.nativeObj,
                labels.nativeObj,
                stats.nativeObj,
                centroids.nativeObj,
                connectivity);
    }

    /**
     * Labels connected foreground components and computes their bounding boxes, pixel areas, and centroids.
     *
     * @param image     the 8-bit single-channel image to be labeled
     * @param labels    destination labeled image
     * @param stats     statistics output for each label, including the background label. Statistics are accessed via
     *                  stats(label, COLUMN) where COLUMN is one of ConnectedComponentsTypes, selecting the statistic.
     *                  The data type is CV_32S
     * @param centroids centroid output for each label, including the background label. Centroids are accessed via
     *                  centroids(label, 0) for x and centroids(label, 1) for y. The data type CV_64F
     * @return the number of labels, including label zero for the background
     * @throws NullPointerException if image, labels, stats, centroids is null
     */
    public static int connectedComponentsWithStats(Mat image, Mat labels, Mat stats, Mat centroids) {
        return connectedComponentsWithStats_2(image.nativeObj, labels.nativeObj, stats.nativeObj, centroids.nativeObj);
    }

    /**
     * Finds contours in a binary image. The function retrieves contours from the binary image. The contours are a
     * useful tool for shape analysis and Note: Since OpenCV 4.14, when mode is RETR_LIST and no hierarchy is requested,
     * this function automatically uses the TRUCO parallel algorithm a scalable lock-free method for contour extraction.
     * In all other cases, the sequential algorithm is used. Note: Since opencv 3.2 source image is not modified by this
     * function.
     *
     * @param image     Source, an 8-bit single-channel image. Non-zero pixels are treated as 1's. Zero pixels remain
     *                  0's, so the image is treated as binary . You can use compare, inRange, threshold ,
     *                  adaptiveThreshold, Canny, and others to create a binary image out of a grayscale or color one.
     *                  If mode equals to RETR_CCOMP or RETR_FLOODFILL, the input can also be a 32-bit integer image of
     *                  labels (CV_32SC1)
     * @param contours  Detected contours. Each contour is stored as a vector of points (e.g.
     *                  vector&lt;vector&lt;Point&gt; &gt;)
     * @param hierarchy Optional output vector (e.g. vector&lt;4-component integer vector&gt;), containing information
     *                  about the image topology. It has as many elements as the number of contours. For each i-th
     *                  contour contours[i], the elements hierarchy[i][0] , hierarchy[i][1] , hierarchy[i][2] , and
     *                  hierarchy[i][3] are set to 0-based indices in contours of the next and previous contours at the
     *                  same hierarchical level, the first child contour and the parent contour, respectively. If for
     *                  the contour i there are no next, previous, parent, or nested contours, the corresponding
     *                  elements of hierarchy[i] will be negative. Note: In Python, hierarchy is nested inside a top
     *                  level array. Use hierarchy[0][i] to access hierarchical elements of i-th contour
     * @param mode      Contour retrieval mode, see RetrievalModes
     * @param method    Contour approximation method, see ContourApproximationModes
     * @param offset    Optional offset by which every contour point is shifted. This is useful if the contours are
     *                  extracted from the image ROI and then they should be analyzed in the whole image context
     * @throws NullPointerException if image, hierarchy, offset is null
     */
    public static void findContours(
            Mat image,
            List<MatOfPoint> contours,
            Mat hierarchy,
            int mode,
            int method,
            Point offset) {
        Mat contours_mat = new Mat();
        findContours_0(image.nativeObj, contours_mat.nativeObj, hierarchy.nativeObj, mode, method, offset.x, offset.y);
        Converters.Mat_to_vector_vector_Point(contours_mat, contours);
        contours_mat.release();
    }

    /**
     * Finds contours in a binary image. The function retrieves contours from the binary image. The contours are a
     * useful tool for shape analysis and Note: Since OpenCV 4.14, when mode is RETR_LIST and no hierarchy is requested,
     * this function automatically uses the TRUCO parallel algorithm a scalable lock-free method for contour extraction.
     * In all other cases, the sequential algorithm is used. Note: Since opencv 3.2 source image is not modified by this
     * function.
     *
     * @param image     Source, an 8-bit single-channel image. Non-zero pixels are treated as 1's. Zero pixels remain
     *                  0's, so the image is treated as binary . You can use compare, inRange, threshold ,
     *                  adaptiveThreshold, Canny, and others to create a binary image out of a grayscale or color one.
     *                  If mode equals to RETR_CCOMP or RETR_FLOODFILL, the input can also be a 32-bit integer image of
     *                  labels (CV_32SC1)
     * @param contours  Detected contours. Each contour is stored as a vector of points (e.g.
     *                  vector&lt;vector&lt;Point&gt; &gt;)
     * @param hierarchy Optional output vector (e.g. vector&lt;4-component integer vector&gt;), containing information
     *                  about the image topology. It has as many elements as the number of contours. For each i-th
     *                  contour contours[i], the elements hierarchy[i][0] , hierarchy[i][1] , hierarchy[i][2] , and
     *                  hierarchy[i][3] are set to 0-based indices in contours of the next and previous contours at the
     *                  same hierarchical level, the first child contour and the parent contour, respectively. If for
     *                  the contour i there are no next, previous, parent, or nested contours, the corresponding
     *                  elements of hierarchy[i] will be negative. Note: In Python, hierarchy is nested inside a top
     *                  level array. Use hierarchy[0][i] to access hierarchical elements of i-th contour
     * @param mode      Contour retrieval mode, see RetrievalModes
     * @param method    Contour approximation method, see ContourApproximationModes
     * @throws NullPointerException if image, hierarchy is null
     */
    public static void findContours(Mat image, List<MatOfPoint> contours, Mat hierarchy, int mode, int method) {
        Mat contours_mat = new Mat();
        findContours_1(image.nativeObj, contours_mat.nativeObj, hierarchy.nativeObj, mode, method);
        Converters.Mat_to_vector_vector_Point(contours_mat, contours);
        contours_mat.release();
    }

    /**
     * Extracts contours from a binary image using linked runs of foreground pixels.
     *
     * @param image     input image
     * @param contours  collection receiving or supplying contour point sequences
     * @param hierarchy output matrix describing contour parent, child and sibling relationships
     * @throws NullPointerException if image, hierarchy is null
     */
    public static void findContoursLinkRuns(Mat image, List<Mat> contours, Mat hierarchy) {
        Mat contours_mat = new Mat();
        findContoursLinkRuns_0(image.nativeObj, contours_mat.nativeObj, hierarchy.nativeObj);
        Converters.Mat_to_vector_Mat(contours_mat, contours);
        contours_mat.release();
    }

    /**
     * Extracts contours from a binary image using linked runs of foreground pixels.
     *
     * @param image    input image
     * @param contours collection receiving or supplying contour point sequences
     * @throws NullPointerException if image is null
     */
    public static void findContoursLinkRuns(Mat image, List<Mat> contours) {
        Mat contours_mat = new Mat();
        findContoursLinkRuns_1(image.nativeObj, contours_mat.nativeObj);
        Converters.Mat_to_vector_Mat(contours_mat, contours);
        contours_mat.release();
    }

    /**
     * Creates a smart pointer to a GeneralizedHoughBallard class and initializes it.
     *
     * @return a new {@code GeneralizedHoughBallard} wrapper configured with the selected options
     */
    public static GeneralizedHoughBallard createGeneralizedHoughBallard() {
        return GeneralizedHoughBallard.__fromPtr__(createGeneralizedHoughBallard_0());
    }

    /**
     * Creates a smart pointer to a GeneralizedHoughGuil class and initializes it.
     *
     * @return a new {@code GeneralizedHoughGuil} wrapper configured with the selected options
     */
    public static GeneralizedHoughGuil createGeneralizedHoughGuil() {
        return GeneralizedHoughGuil.__fromPtr__(createGeneralizedHoughGuil_0());
    }

    /**
     * Applies a user colormap on a given image.
     *
     * @param src      The source image, grayscale or colored of type CV_8UC1 or CV_8UC3. If CV_8UC3, then the CV_8UC1
     *                 image is generated internally using COLOR_BGR2GRAY
     * @param dst      The result is the colormapped source image of the same number of channels as userColor. Note:
     *                 Mat.create is called on dst
     * @param colormap The colormap to apply, see ColormapTypes
     * @throws NullPointerException if src, dst is null
     */
    public static void applyColorMap(Mat src, Mat dst, int colormap) {
        applyColorMap_0(src.nativeObj, dst.nativeObj, colormap);
    }

    /**
     * Applies a user colormap on a given image.
     *
     * @param src       The source image, grayscale or colored of type CV_8UC1 or CV_8UC3. If CV_8UC3, then the CV_8UC1
     *                  image is generated internally using COLOR_BGR2GRAY
     * @param dst       The result is the colormapped source image of the same number of channels as userColor. Note:
     *                  Mat.create is called on dst
     * @param userColor The colormap to apply of type CV_8UC1 or CV_8UC3 and size 256
     * @throws NullPointerException if src, dst, userColor is null
     */
    public static void applyColorMap(Mat src, Mat dst, Mat userColor) {
        applyColorMap_1(src.nativeObj, dst.nativeObj, userColor.nativeObj);
    }

    /**
     * Draws a line segment connecting two points. The function line draws the line segment between pt1 and pt2 points
     * in the image. The line is clipped by the image boundaries. For non-antialiased lines with integer coordinates,
     * the 8-connected or 4-connected Bresenham algorithm is used. Thick lines are drawn with rounding endings.
     * Antialiased lines are drawn using Gaussian filtering.
     *
     * @param img       image to process or annotate
     * @param pt1       First point of the line segment
     * @param pt2       Second point of the line segment
     * @param color     Line color
     * @param thickness Line thickness
     * @param lineType  Type of the line. See LineTypes
     * @param shift     Number of fractional bits in the point coordinates
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void line(Mat img, Point pt1, Point pt2, Scalar color, int thickness, int lineType, int shift) {
        line_0(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                shift);
    }

    /**
     * Draws a line segment connecting two points. The function line draws the line segment between pt1 and pt2 points
     * in the image. The line is clipped by the image boundaries. For non-antialiased lines with integer coordinates,
     * the 8-connected or 4-connected Bresenham algorithm is used. Thick lines are drawn with rounding endings.
     * Antialiased lines are drawn using Gaussian filtering.
     *
     * @param img       image to process or annotate
     * @param pt1       First point of the line segment
     * @param pt2       Second point of the line segment
     * @param color     Line color
     * @param thickness Line thickness
     * @param lineType  Type of the line. See LineTypes
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void line(Mat img, Point pt1, Point pt2, Scalar color, int thickness, int lineType) {
        line_1(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws a line segment connecting two points. The function line draws the line segment between pt1 and pt2 points
     * in the image. The line is clipped by the image boundaries. For non-antialiased lines with integer coordinates,
     * the 8-connected or 4-connected Bresenham algorithm is used. Thick lines are drawn with rounding endings.
     * Antialiased lines are drawn using Gaussian filtering.
     *
     * @param img       image to process or annotate
     * @param pt1       First point of the line segment
     * @param pt2       Second point of the line segment
     * @param color     Line color
     * @param thickness Line thickness
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void line(Mat img, Point pt1, Point pt2, Scalar color, int thickness) {
        line_2(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws a line segment connecting two points. The function line draws the line segment between pt1 and pt2 points
     * in the image. The line is clipped by the image boundaries. For non-antialiased lines with integer coordinates,
     * the 8-connected or 4-connected Bresenham algorithm is used. Thick lines are drawn with rounding endings.
     * Antialiased lines are drawn using Gaussian filtering.
     *
     * @param img   image to process or annotate
     * @param pt1   First point of the line segment
     * @param pt2   Second point of the line segment
     * @param color Line color
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void line(Mat img, Point pt1, Point pt2, Scalar color) {
        line_3(img.nativeObj, pt1.x, pt1.y, pt2.x, pt2.y, color.val[0], color.val[1], color.val[2], color.val[3]);
    }

    /**
     * Draws an arrow segment pointing from the first point to the second one. The function arrowedLine draws an arrow
     * between pt1 and pt2 points in the image. See also line.
     *
     * @param img       image to process or annotate
     * @param pt1       The point the arrow starts from
     * @param pt2       The point the arrow points to
     * @param color     Line color
     * @param thickness Line thickness
     * @param line_type Type of the line. See LineTypes
     * @param shift     Number of fractional bits in the point coordinates
     * @param tipLength The length of the arrow tip in relation to the arrow length
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void arrowedLine(
            Mat img,
            Point pt1,
            Point pt2,
            Scalar color,
            int thickness,
            int line_type,
            int shift,
            double tipLength) {
        arrowedLine_0(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                line_type,
                shift,
                tipLength);
    }

    /**
     * Draws an arrow segment pointing from the first point to the second one. The function arrowedLine draws an arrow
     * between pt1 and pt2 points in the image. See also line.
     *
     * @param img       image to process or annotate
     * @param pt1       The point the arrow starts from
     * @param pt2       The point the arrow points to
     * @param color     Line color
     * @param thickness Line thickness
     * @param line_type Type of the line. See LineTypes
     * @param shift     Number of fractional bits in the point coordinates
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void arrowedLine(
            Mat img,
            Point pt1,
            Point pt2,
            Scalar color,
            int thickness,
            int line_type,
            int shift) {
        arrowedLine_1(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                line_type,
                shift);
    }

    /**
     * Draws an arrow segment pointing from the first point to the second one. The function arrowedLine draws an arrow
     * between pt1 and pt2 points in the image. See also line.
     *
     * @param img       image to process or annotate
     * @param pt1       The point the arrow starts from
     * @param pt2       The point the arrow points to
     * @param color     Line color
     * @param thickness Line thickness
     * @param line_type Type of the line. See LineTypes
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void arrowedLine(Mat img, Point pt1, Point pt2, Scalar color, int thickness, int line_type) {
        arrowedLine_2(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                line_type);
    }

    /**
     * Draws an arrow segment pointing from the first point to the second one. The function arrowedLine draws an arrow
     * between pt1 and pt2 points in the image. See also line.
     *
     * @param img       image to process or annotate
     * @param pt1       The point the arrow starts from
     * @param pt2       The point the arrow points to
     * @param color     Line color
     * @param thickness Line thickness
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void arrowedLine(Mat img, Point pt1, Point pt2, Scalar color, int thickness) {
        arrowedLine_3(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws an arrow segment pointing from the first point to the second one. The function arrowedLine draws an arrow
     * between pt1 and pt2 points in the image. See also line.
     *
     * @param img   image to process or annotate
     * @param pt1   The point the arrow starts from
     * @param pt2   The point the arrow points to
     * @param color Line color
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void arrowedLine(Mat img, Point pt1, Point pt2, Scalar color) {
        arrowedLine_4(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3]);
    }

    /**
     * Draw axes of the world/object coordinate system from pose estimation.
     *
     * @param image        Input/output image. It must have 1 or 3 channels. The number of channels is not altered
     * @param cameraMatrix Input 3x3 floating-point matrix of camera intrinsic parameters. cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Rotation vector (see REF: Rodrigues ) that, together with tvec, brings points from the model
     *                     coordinate system to the camera coordinate system
     * @param tvec         Translation vector
     * @param length       Length of the painted axes in the same unit than tvec (usually in meters)
     * @param thickness    Line thickness of the painted axes. This function draws the axes of the world/object
     *                     coordinate system w.r.t. to the camera frame. OX is drawn in red, OY in green and OZ in blue
     * @throws NullPointerException if image, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static void drawFrameAxes(
            Mat image,
            Mat cameraMatrix,
            Mat distCoeffs,
            Mat rvec,
            Mat tvec,
            float length,
            int thickness) {
        drawFrameAxes_0(
                image.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                length,
                thickness);
    }

    /**
     * Draw axes of the world/object coordinate system from pose estimation.
     *
     * @param image        Input/output image. It must have 1 or 3 channels. The number of channels is not altered
     * @param cameraMatrix Input 3x3 floating-point matrix of camera intrinsic parameters. cameramatrix{A}
     * @param distCoeffs   Input vector of distortion coefficients distcoeffs. If the vector is empty, the zero
     *                     distortion coefficients are assumed
     * @param rvec         Rotation vector (see REF: Rodrigues ) that, together with tvec, brings points from the model
     *                     coordinate system to the camera coordinate system
     * @param tvec         Translation vector
     * @param length       Length of the painted axes in the same unit than tvec (usually in meters)
     * @throws NullPointerException if image, cameraMatrix, distCoeffs, rvec, tvec is null
     */
    public static void drawFrameAxes(Mat image, Mat cameraMatrix, Mat distCoeffs, Mat rvec, Mat tvec, float length) {
        drawFrameAxes_1(
                image.nativeObj,
                cameraMatrix.nativeObj,
                distCoeffs.nativeObj,
                rvec.nativeObj,
                tvec.nativeObj,
                length);
    }

    /**
     * Draws a simple, thick, or filled up-right rectangle. The function rectangle draws a rectangle outline or a filled
     * rectangle whose two opposite corners are pt1 and pt2.
     *
     * @param img       image to process or annotate
     * @param pt1       Vertex of the rectangle
     * @param pt2       Vertex of the rectangle opposite to pt1
     * @param color     Rectangle color or brightness (grayscale image)
     * @param thickness Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                  function has to draw a filled rectangle
     * @param lineType  Type of the line. See LineTypes
     * @param shift     Number of fractional bits in the point coordinates
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void rectangle(Mat img, Point pt1, Point pt2, Scalar color, int thickness, int lineType, int shift) {
        rectangle_0(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                shift);
    }

    /**
     * Draws a simple, thick, or filled up-right rectangle. The function rectangle draws a rectangle outline or a filled
     * rectangle whose two opposite corners are pt1 and pt2.
     *
     * @param img       image to process or annotate
     * @param pt1       Vertex of the rectangle
     * @param pt2       Vertex of the rectangle opposite to pt1
     * @param color     Rectangle color or brightness (grayscale image)
     * @param thickness Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                  function has to draw a filled rectangle
     * @param lineType  Type of the line. See LineTypes
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void rectangle(Mat img, Point pt1, Point pt2, Scalar color, int thickness, int lineType) {
        rectangle_1(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws a simple, thick, or filled up-right rectangle. The function rectangle draws a rectangle outline or a filled
     * rectangle whose two opposite corners are pt1 and pt2.
     *
     * @param img       image to process or annotate
     * @param pt1       Vertex of the rectangle
     * @param pt2       Vertex of the rectangle opposite to pt1
     * @param color     Rectangle color or brightness (grayscale image)
     * @param thickness Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                  function has to draw a filled rectangle
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void rectangle(Mat img, Point pt1, Point pt2, Scalar color, int thickness) {
        rectangle_2(
                img.nativeObj,
                pt1.x,
                pt1.y,
                pt2.x,
                pt2.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws a simple, thick, or filled up-right rectangle. The function rectangle draws a rectangle outline or a filled
     * rectangle whose two opposite corners are pt1 and pt2.
     *
     * @param img   image to process or annotate
     * @param pt1   Vertex of the rectangle
     * @param pt2   Vertex of the rectangle opposite to pt1
     * @param color Rectangle color or brightness (grayscale image)
     * @throws NullPointerException if img, pt1, pt2, color is null
     */
    public static void rectangle(Mat img, Point pt1, Point pt2, Scalar color) {
        rectangle_3(img.nativeObj, pt1.x, pt1.y, pt2.x, pt2.y, color.val[0], color.val[1], color.val[2], color.val[3]);
    }

    /**
     * Draws a simple, thick, or filled up-right rectangle. The function rectangle draws a rectangle outline or a filled
     * rectangle whose two opposite corners are pt1 and pt2.
     *
     * @param img       image to process or annotate
     * @param rec       rec region
     * @param color     Rectangle color or brightness (grayscale image)
     * @param thickness Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                  function has to draw a filled rectangle
     * @param lineType  Type of the line. See LineTypes
     * @param shift     Number of fractional bits in the point coordinates
     * @throws NullPointerException if img, rec, color is null
     */
    public static void rectangle(Mat img, Rect rec, Scalar color, int thickness, int lineType, int shift) {
        rectangle_4(
                img.nativeObj,
                rec.x,
                rec.y,
                rec.width,
                rec.height,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                shift);
    }

    /**
     * Draws a simple, thick, or filled up-right rectangle. The function rectangle draws a rectangle outline or a filled
     * rectangle whose two opposite corners are pt1 and pt2.
     *
     * @param img       image to process or annotate
     * @param rec       rec region
     * @param color     Rectangle color or brightness (grayscale image)
     * @param thickness Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                  function has to draw a filled rectangle
     * @param lineType  Type of the line. See LineTypes
     * @throws NullPointerException if img, rec, color is null
     */
    public static void rectangle(Mat img, Rect rec, Scalar color, int thickness, int lineType) {
        rectangle_5(
                img.nativeObj,
                rec.x,
                rec.y,
                rec.width,
                rec.height,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws a simple, thick, or filled up-right rectangle. The function rectangle draws a rectangle outline or a filled
     * rectangle whose two opposite corners are pt1 and pt2.
     *
     * @param img       image to process or annotate
     * @param rec       rec region
     * @param color     Rectangle color or brightness (grayscale image)
     * @param thickness Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                  function has to draw a filled rectangle
     * @throws NullPointerException if img, rec, color is null
     */
    public static void rectangle(Mat img, Rect rec, Scalar color, int thickness) {
        rectangle_6(
                img.nativeObj,
                rec.x,
                rec.y,
                rec.width,
                rec.height,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws a simple, thick, or filled up-right rectangle. The function rectangle draws a rectangle outline or a filled
     * rectangle whose two opposite corners are pt1 and pt2.
     *
     * @param img   image to process or annotate
     * @param rec   rec region
     * @param color Rectangle color or brightness (grayscale image)
     * @throws NullPointerException if img, rec, color is null
     */
    public static void rectangle(Mat img, Rect rec, Scalar color) {
        rectangle_7(
                img.nativeObj,
                rec.x,
                rec.y,
                rec.width,
                rec.height,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3]);
    }

    /**
     * Draws a circle. The function circle draws a simple or filled circle with a given center and radius.
     *
     * @param img       Image where the circle is drawn
     * @param center    Center of the circle
     * @param radius    Radius of the circle
     * @param color     Circle color
     * @param thickness Thickness of the circle outline, if positive. Negative values, like FILLED, mean that a filled
     *                  circle is to be drawn
     * @param lineType  Type of the circle boundary. See LineTypes
     * @param shift     Number of fractional bits in the coordinates of the center and in the radius value
     * @throws NullPointerException if img, center, color is null
     */
    public static void circle(Mat img, Point center, int radius, Scalar color, int thickness, int lineType, int shift) {
        circle_0(
                img.nativeObj,
                center.x,
                center.y,
                radius,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                shift);
    }

    /**
     * Draws a circle. The function circle draws a simple or filled circle with a given center and radius.
     *
     * @param img       Image where the circle is drawn
     * @param center    Center of the circle
     * @param radius    Radius of the circle
     * @param color     Circle color
     * @param thickness Thickness of the circle outline, if positive. Negative values, like FILLED, mean that a filled
     *                  circle is to be drawn
     * @param lineType  Type of the circle boundary. See LineTypes
     * @throws NullPointerException if img, center, color is null
     */
    public static void circle(Mat img, Point center, int radius, Scalar color, int thickness, int lineType) {
        circle_1(
                img.nativeObj,
                center.x,
                center.y,
                radius,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws a circle. The function circle draws a simple or filled circle with a given center and radius.
     *
     * @param img       Image where the circle is drawn
     * @param center    Center of the circle
     * @param radius    Radius of the circle
     * @param color     Circle color
     * @param thickness Thickness of the circle outline, if positive. Negative values, like FILLED, mean that a filled
     *                  circle is to be drawn
     * @throws NullPointerException if img, center, color is null
     */
    public static void circle(Mat img, Point center, int radius, Scalar color, int thickness) {
        circle_2(
                img.nativeObj,
                center.x,
                center.y,
                radius,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws a circle. The function circle draws a simple or filled circle with a given center and radius.
     *
     * @param img    Image where the circle is drawn
     * @param center Center of the circle
     * @param radius Radius of the circle
     * @param color  Circle color
     * @throws NullPointerException if img, center, color is null
     */
    public static void circle(Mat img, Point center, int radius, Scalar color) {
        circle_3(img.nativeObj, center.x, center.y, radius, color.val[0], color.val[1], color.val[2], color.val[3]);
    }

    /**
     * Draws a simple or thick elliptic arc or fills an ellipse sector. The function ellipse with more parameters draws
     * an ellipse outline, a filled ellipse, an elliptic arc, or a filled ellipse sector. The drawing code uses general
     * parametric form. A piecewise-linear curve is used to approximate the elliptic arc boundary. If you need more
     * control of the ellipse rendering, you can retrieve the curve using ellipse2Poly and then render it with polylines
     * or fill it with fillPoly. If you use the first variant of the function and want to draw the whole ellipse, not an
     * arc, pass startAngle=0 and endAngle=360. If startAngle is greater than endAngle, they are swapped. The figure
     * below explains the meaning of the parameters to draw the blue arc.
     *
     * @param img        image to process or annotate
     * @param center     Center of the ellipse
     * @param axes       Half of the size of the ellipse main axes
     * @param angle      Ellipse rotation angle in degrees
     * @param startAngle Starting angle of the elliptic arc in degrees
     * @param endAngle   Ending angle of the elliptic arc in degrees
     * @param color      Ellipse color
     * @param thickness  Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                   ellipse sector is to be drawn
     * @param lineType   Type of the ellipse boundary. See LineTypes
     * @param shift      Number of fractional bits in the coordinates of the center and values of axes
     * @throws NullPointerException if img, center, axes, color is null
     */
    public static void ellipse(
            Mat img,
            Point center,
            Size axes,
            double angle,
            double startAngle,
            double endAngle,
            Scalar color,
            int thickness,
            int lineType,
            int shift) {
        ellipse_0(
                img.nativeObj,
                center.x,
                center.y,
                axes.width,
                axes.height,
                angle,
                startAngle,
                endAngle,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                shift);
    }

    /**
     * Draws a simple or thick elliptic arc or fills an ellipse sector. The function ellipse with more parameters draws
     * an ellipse outline, a filled ellipse, an elliptic arc, or a filled ellipse sector. The drawing code uses general
     * parametric form. A piecewise-linear curve is used to approximate the elliptic arc boundary. If you need more
     * control of the ellipse rendering, you can retrieve the curve using ellipse2Poly and then render it with polylines
     * or fill it with fillPoly. If you use the first variant of the function and want to draw the whole ellipse, not an
     * arc, pass startAngle=0 and endAngle=360. If startAngle is greater than endAngle, they are swapped. The figure
     * below explains the meaning of the parameters to draw the blue arc.
     *
     * @param img        image to process or annotate
     * @param center     Center of the ellipse
     * @param axes       Half of the size of the ellipse main axes
     * @param angle      Ellipse rotation angle in degrees
     * @param startAngle Starting angle of the elliptic arc in degrees
     * @param endAngle   Ending angle of the elliptic arc in degrees
     * @param color      Ellipse color
     * @param thickness  Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                   ellipse sector is to be drawn
     * @param lineType   Type of the ellipse boundary. See LineTypes
     * @throws NullPointerException if img, center, axes, color is null
     */
    public static void ellipse(
            Mat img,
            Point center,
            Size axes,
            double angle,
            double startAngle,
            double endAngle,
            Scalar color,
            int thickness,
            int lineType) {
        ellipse_1(
                img.nativeObj,
                center.x,
                center.y,
                axes.width,
                axes.height,
                angle,
                startAngle,
                endAngle,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws a simple or thick elliptic arc or fills an ellipse sector. The function ellipse with more parameters draws
     * an ellipse outline, a filled ellipse, an elliptic arc, or a filled ellipse sector. The drawing code uses general
     * parametric form. A piecewise-linear curve is used to approximate the elliptic arc boundary. If you need more
     * control of the ellipse rendering, you can retrieve the curve using ellipse2Poly and then render it with polylines
     * or fill it with fillPoly. If you use the first variant of the function and want to draw the whole ellipse, not an
     * arc, pass startAngle=0 and endAngle=360. If startAngle is greater than endAngle, they are swapped. The figure
     * below explains the meaning of the parameters to draw the blue arc.
     *
     * @param img        image to process or annotate
     * @param center     Center of the ellipse
     * @param axes       Half of the size of the ellipse main axes
     * @param angle      Ellipse rotation angle in degrees
     * @param startAngle Starting angle of the elliptic arc in degrees
     * @param endAngle   Ending angle of the elliptic arc in degrees
     * @param color      Ellipse color
     * @param thickness  Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                   ellipse sector is to be drawn
     * @throws NullPointerException if img, center, axes, color is null
     */
    public static void ellipse(
            Mat img,
            Point center,
            Size axes,
            double angle,
            double startAngle,
            double endAngle,
            Scalar color,
            int thickness) {
        ellipse_2(
                img.nativeObj,
                center.x,
                center.y,
                axes.width,
                axes.height,
                angle,
                startAngle,
                endAngle,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws a simple or thick elliptic arc or fills an ellipse sector. The function ellipse with more parameters draws
     * an ellipse outline, a filled ellipse, an elliptic arc, or a filled ellipse sector. The drawing code uses general
     * parametric form. A piecewise-linear curve is used to approximate the elliptic arc boundary. If you need more
     * control of the ellipse rendering, you can retrieve the curve using ellipse2Poly and then render it with polylines
     * or fill it with fillPoly. If you use the first variant of the function and want to draw the whole ellipse, not an
     * arc, pass startAngle=0 and endAngle=360. If startAngle is greater than endAngle, they are swapped. The figure
     * below explains the meaning of the parameters to draw the blue arc.
     *
     * @param img        image to process or annotate
     * @param center     Center of the ellipse
     * @param axes       Half of the size of the ellipse main axes
     * @param angle      Ellipse rotation angle in degrees
     * @param startAngle Starting angle of the elliptic arc in degrees
     * @param endAngle   Ending angle of the elliptic arc in degrees
     * @param color      Ellipse color
     * @throws NullPointerException if img, center, axes, color is null
     */
    public static void ellipse(
            Mat img,
            Point center,
            Size axes,
            double angle,
            double startAngle,
            double endAngle,
            Scalar color) {
        ellipse_3(
                img.nativeObj,
                center.x,
                center.y,
                axes.width,
                axes.height,
                angle,
                startAngle,
                endAngle,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3]);
    }

    /**
     * Draws a simple or thick elliptic arc or fills an ellipse sector. The function ellipse with more parameters draws
     * an ellipse outline, a filled ellipse, an elliptic arc, or a filled ellipse sector. The drawing code uses general
     * parametric form. A piecewise-linear curve is used to approximate the elliptic arc boundary. If you need more
     * control of the ellipse rendering, you can retrieve the curve using ellipse2Poly and then render it with polylines
     * or fill it with fillPoly. If you use the first variant of the function and want to draw the whole ellipse, not an
     * arc, pass startAngle=0 and endAngle=360. If startAngle is greater than endAngle, they are swapped. The figure
     * below explains the meaning of the parameters to draw the blue arc.
     *
     * @param img       image to process or annotate
     * @param box       Alternative ellipse representation via RotatedRect. This means that the function draws an
     *                  ellipse inscribed in the rotated rectangle
     * @param color     Ellipse color
     * @param thickness Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                  ellipse sector is to be drawn
     * @param lineType  Type of the ellipse boundary. See LineTypes
     * @throws NullPointerException if img, box, color is null
     */
    public static void ellipse(Mat img, RotatedRect box, Scalar color, int thickness, int lineType) {
        ellipse_4(
                img.nativeObj,
                box.center.x,
                box.center.y,
                box.size.width,
                box.size.height,
                box.angle,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws a simple or thick elliptic arc or fills an ellipse sector. The function ellipse with more parameters draws
     * an ellipse outline, a filled ellipse, an elliptic arc, or a filled ellipse sector. The drawing code uses general
     * parametric form. A piecewise-linear curve is used to approximate the elliptic arc boundary. If you need more
     * control of the ellipse rendering, you can retrieve the curve using ellipse2Poly and then render it with polylines
     * or fill it with fillPoly. If you use the first variant of the function and want to draw the whole ellipse, not an
     * arc, pass startAngle=0 and endAngle=360. If startAngle is greater than endAngle, they are swapped. The figure
     * below explains the meaning of the parameters to draw the blue arc.
     *
     * @param img       image to process or annotate
     * @param box       Alternative ellipse representation via RotatedRect. This means that the function draws an
     *                  ellipse inscribed in the rotated rectangle
     * @param color     Ellipse color
     * @param thickness Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                  ellipse sector is to be drawn
     * @throws NullPointerException if img, box, color is null
     */
    public static void ellipse(Mat img, RotatedRect box, Scalar color, int thickness) {
        ellipse_5(
                img.nativeObj,
                box.center.x,
                box.center.y,
                box.size.width,
                box.size.height,
                box.angle,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws a simple or thick elliptic arc or fills an ellipse sector. The function ellipse with more parameters draws
     * an ellipse outline, a filled ellipse, an elliptic arc, or a filled ellipse sector. The drawing code uses general
     * parametric form. A piecewise-linear curve is used to approximate the elliptic arc boundary. If you need more
     * control of the ellipse rendering, you can retrieve the curve using ellipse2Poly and then render it with polylines
     * or fill it with fillPoly. If you use the first variant of the function and want to draw the whole ellipse, not an
     * arc, pass startAngle=0 and endAngle=360. If startAngle is greater than endAngle, they are swapped. The figure
     * below explains the meaning of the parameters to draw the blue arc.
     *
     * @param img   image to process or annotate
     * @param box   Alternative ellipse representation via RotatedRect. This means that the function draws an ellipse
     *              inscribed in the rotated rectangle
     * @param color Ellipse color
     * @throws NullPointerException if img, box, color is null
     */
    public static void ellipse(Mat img, RotatedRect box, Scalar color) {
        ellipse_6(
                img.nativeObj,
                box.center.x,
                box.center.y,
                box.size.width,
                box.size.height,
                box.angle,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3]);
    }

    /**
     * Draws a marker on a predefined position in an image. The function drawMarker draws a marker on a given position
     * in the image. For the moment several marker types are supported, see MarkerTypes for more information.
     *
     * @param img        image to process or annotate
     * @param position   The point where the crosshair is positioned
     * @param color      Line color
     * @param markerType The specific type of marker you want to use, see MarkerTypes
     * @param markerSize The length of the marker axis [default = 20 pixels]
     * @param thickness  Line thickness
     * @param line_type  Type of the line, See LineTypes
     * @throws NullPointerException if img, position, color is null
     */
    public static void drawMarker(
            Mat img,
            Point position,
            Scalar color,
            int markerType,
            int markerSize,
            int thickness,
            int line_type) {
        drawMarker_0(
                img.nativeObj,
                position.x,
                position.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                markerType,
                markerSize,
                thickness,
                line_type);
    }

    /**
     * Draws a marker on a predefined position in an image. The function drawMarker draws a marker on a given position
     * in the image. For the moment several marker types are supported, see MarkerTypes for more information.
     *
     * @param img        image to process or annotate
     * @param position   The point where the crosshair is positioned
     * @param color      Line color
     * @param markerType The specific type of marker you want to use, see MarkerTypes
     * @param markerSize The length of the marker axis [default = 20 pixels]
     * @param thickness  Line thickness
     * @throws NullPointerException if img, position, color is null
     */
    public static void drawMarker(
            Mat img,
            Point position,
            Scalar color,
            int markerType,
            int markerSize,
            int thickness) {
        drawMarker_1(
                img.nativeObj,
                position.x,
                position.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                markerType,
                markerSize,
                thickness);
    }

    /**
     * Draws a marker on a predefined position in an image. The function drawMarker draws a marker on a given position
     * in the image. For the moment several marker types are supported, see MarkerTypes for more information.
     *
     * @param img        image to process or annotate
     * @param position   The point where the crosshair is positioned
     * @param color      Line color
     * @param markerType The specific type of marker you want to use, see MarkerTypes
     * @param markerSize The length of the marker axis [default = 20 pixels]
     * @throws NullPointerException if img, position, color is null
     */
    public static void drawMarker(Mat img, Point position, Scalar color, int markerType, int markerSize) {
        drawMarker_2(
                img.nativeObj,
                position.x,
                position.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                markerType,
                markerSize);
    }

    /**
     * Draws a marker on a predefined position in an image. The function drawMarker draws a marker on a given position
     * in the image. For the moment several marker types are supported, see MarkerTypes for more information.
     *
     * @param img        image to process or annotate
     * @param position   The point where the crosshair is positioned
     * @param color      Line color
     * @param markerType The specific type of marker you want to use, see MarkerTypes
     * @throws NullPointerException if img, position, color is null
     */
    public static void drawMarker(Mat img, Point position, Scalar color, int markerType) {
        drawMarker_3(
                img.nativeObj,
                position.x,
                position.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                markerType);
    }

    /**
     * Draws a marker on a predefined position in an image. The function drawMarker draws a marker on a given position
     * in the image. For the moment several marker types are supported, see MarkerTypes for more information.
     *
     * @param img      image to process or annotate
     * @param position The point where the crosshair is positioned
     * @param color    Line color
     * @throws NullPointerException if img, position, color is null
     */
    public static void drawMarker(Mat img, Point position, Scalar color) {
        drawMarker_4(img.nativeObj, position.x, position.y, color.val[0], color.val[1], color.val[2], color.val[3]);
    }

    /**
     * Fills a convex polygon. The function fillConvexPoly draws a filled convex polygon. This function is much faster
     * than the function fillPoly . It can fill not only convex polygons but any monotonic polygon without
     * self-intersections, that is, a polygon whose contour intersects every horizontal line (scan line) twice at the
     * most (though, its top-most and/or the bottom edge could be horizontal).
     *
     * @param img      image to process or annotate
     * @param points   Polygon vertices
     * @param color    Polygon color
     * @param lineType Type of the polygon boundaries. See LineTypes
     * @param shift    Number of fractional bits in the vertex coordinates
     * @throws NullPointerException if img, color is null
     */
    public static void fillConvexPoly(Mat img, MatOfPoint points, Scalar color, int lineType, int shift) {
        Mat points_mat = points;
        fillConvexPoly_0(
                img.nativeObj,
                points_mat.nativeObj,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                lineType,
                shift);
    }

    /**
     * Fills a convex polygon. The function fillConvexPoly draws a filled convex polygon. This function is much faster
     * than the function fillPoly . It can fill not only convex polygons but any monotonic polygon without
     * self-intersections, that is, a polygon whose contour intersects every horizontal line (scan line) twice at the
     * most (though, its top-most and/or the bottom edge could be horizontal).
     *
     * @param img      image to process or annotate
     * @param points   Polygon vertices
     * @param color    Polygon color
     * @param lineType Type of the polygon boundaries. See LineTypes
     * @throws NullPointerException if img, color is null
     */
    public static void fillConvexPoly(Mat img, MatOfPoint points, Scalar color, int lineType) {
        Mat points_mat = points;
        fillConvexPoly_1(
                img.nativeObj,
                points_mat.nativeObj,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                lineType);
    }

    /**
     * Fills a convex polygon. The function fillConvexPoly draws a filled convex polygon. This function is much faster
     * than the function fillPoly . It can fill not only convex polygons but any monotonic polygon without
     * self-intersections, that is, a polygon whose contour intersects every horizontal line (scan line) twice at the
     * most (though, its top-most and/or the bottom edge could be horizontal).
     *
     * @param img    image to process or annotate
     * @param points Polygon vertices
     * @param color  Polygon color
     * @throws NullPointerException if img, color is null
     */
    public static void fillConvexPoly(Mat img, MatOfPoint points, Scalar color) {
        Mat points_mat = points;
        fillConvexPoly_2(img.nativeObj, points_mat.nativeObj, color.val[0], color.val[1], color.val[2], color.val[3]);
    }

    /**
     * Fills the area bounded by one or more polygons. The function fillPoly fills an area bounded by several polygonal
     * contours. The function can fill complex areas, for example, areas with holes, contours with self-intersections
     * (some of their parts), and so forth.
     *
     * @param img      image to process or annotate
     * @param pts      Array of polygons where each polygon is represented as an array of points
     * @param color    Polygon color
     * @param lineType Type of the polygon boundaries. See LineTypes
     * @param shift    Number of fractional bits in the vertex coordinates
     * @param offset   Optional offset of all points of the contours
     * @throws NullPointerException if img, color, offset is null
     */
    public static void fillPoly(Mat img, List<MatOfPoint> pts, Scalar color, int lineType, int shift, Point offset) {
        List<Mat> pts_tmplm = new ArrayList<Mat>((pts != null) ? pts.size() : 0);
        Mat pts_mat = Converters.vector_vector_Point_to_Mat(pts, pts_tmplm);
        fillPoly_0(
                img.nativeObj,
                pts_mat.nativeObj,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                lineType,
                shift,
                offset.x,
                offset.y);
    }

    /**
     * Fills the area bounded by one or more polygons. The function fillPoly fills an area bounded by several polygonal
     * contours. The function can fill complex areas, for example, areas with holes, contours with self-intersections
     * (some of their parts), and so forth.
     *
     * @param img      image to process or annotate
     * @param pts      Array of polygons where each polygon is represented as an array of points
     * @param color    Polygon color
     * @param lineType Type of the polygon boundaries. See LineTypes
     * @param shift    Number of fractional bits in the vertex coordinates
     * @throws NullPointerException if img, color is null
     */
    public static void fillPoly(Mat img, List<MatOfPoint> pts, Scalar color, int lineType, int shift) {
        List<Mat> pts_tmplm = new ArrayList<Mat>((pts != null) ? pts.size() : 0);
        Mat pts_mat = Converters.vector_vector_Point_to_Mat(pts, pts_tmplm);
        fillPoly_1(
                img.nativeObj,
                pts_mat.nativeObj,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                lineType,
                shift);
    }

    /**
     * Fills the area bounded by one or more polygons. The function fillPoly fills an area bounded by several polygonal
     * contours. The function can fill complex areas, for example, areas with holes, contours with self-intersections
     * (some of their parts), and so forth.
     *
     * @param img      image to process or annotate
     * @param pts      Array of polygons where each polygon is represented as an array of points
     * @param color    Polygon color
     * @param lineType Type of the polygon boundaries. See LineTypes
     * @throws NullPointerException if img, color is null
     */
    public static void fillPoly(Mat img, List<MatOfPoint> pts, Scalar color, int lineType) {
        List<Mat> pts_tmplm = new ArrayList<Mat>((pts != null) ? pts.size() : 0);
        Mat pts_mat = Converters.vector_vector_Point_to_Mat(pts, pts_tmplm);
        fillPoly_2(img.nativeObj, pts_mat.nativeObj, color.val[0], color.val[1], color.val[2], color.val[3], lineType);
    }

    /**
     * Fills the area bounded by one or more polygons. The function fillPoly fills an area bounded by several polygonal
     * contours. The function can fill complex areas, for example, areas with holes, contours with self-intersections
     * (some of their parts), and so forth.
     *
     * @param img   image to process or annotate
     * @param pts   Array of polygons where each polygon is represented as an array of points
     * @param color Polygon color
     * @throws NullPointerException if img, color is null
     */
    public static void fillPoly(Mat img, List<MatOfPoint> pts, Scalar color) {
        List<Mat> pts_tmplm = new ArrayList<Mat>((pts != null) ? pts.size() : 0);
        Mat pts_mat = Converters.vector_vector_Point_to_Mat(pts, pts_tmplm);
        fillPoly_3(img.nativeObj, pts_mat.nativeObj, color.val[0], color.val[1], color.val[2], color.val[3]);
    }

    /**
     * Draws several polygonal curves.
     *
     * @param img       image to process or annotate
     * @param pts       Array of polygonal curves
     * @param isClosed  Flag indicating whether the drawn polylines are closed or not. If they are closed, the function
     *                  draws a line from the last vertex of each curve to its first vertex
     * @param color     Polyline color
     * @param thickness Thickness of the polyline edges
     * @param lineType  Type of the line segments. See LineTypes
     * @param shift     Number of fractional bits in the vertex coordinates. The function polylines draws one or more
     *                  polygonal curves
     * @throws NullPointerException if img, color is null
     */
    public static void polylines(
            Mat img,
            List<MatOfPoint> pts,
            boolean isClosed,
            Scalar color,
            int thickness,
            int lineType,
            int shift) {
        List<Mat> pts_tmplm = new ArrayList<Mat>((pts != null) ? pts.size() : 0);
        Mat pts_mat = Converters.vector_vector_Point_to_Mat(pts, pts_tmplm);
        polylines_0(
                img.nativeObj,
                pts_mat.nativeObj,
                isClosed,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                shift);
    }

    /**
     * Draws several polygonal curves.
     *
     * @param img       image to process or annotate
     * @param pts       Array of polygonal curves
     * @param isClosed  Flag indicating whether the drawn polylines are closed or not. If they are closed, the function
     *                  draws a line from the last vertex of each curve to its first vertex
     * @param color     Polyline color
     * @param thickness Thickness of the polyline edges
     * @param lineType  Type of the line segments. See LineTypes
     * @throws NullPointerException if img, color is null
     */
    public static void polylines(
            Mat img,
            List<MatOfPoint> pts,
            boolean isClosed,
            Scalar color,
            int thickness,
            int lineType) {
        List<Mat> pts_tmplm = new ArrayList<Mat>((pts != null) ? pts.size() : 0);
        Mat pts_mat = Converters.vector_vector_Point_to_Mat(pts, pts_tmplm);
        polylines_1(
                img.nativeObj,
                pts_mat.nativeObj,
                isClosed,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws several polygonal curves.
     *
     * @param img       image to process or annotate
     * @param pts       Array of polygonal curves
     * @param isClosed  Flag indicating whether the drawn polylines are closed or not. If they are closed, the function
     *                  draws a line from the last vertex of each curve to its first vertex
     * @param color     Polyline color
     * @param thickness Thickness of the polyline edges
     * @throws NullPointerException if img, color is null
     */
    public static void polylines(Mat img, List<MatOfPoint> pts, boolean isClosed, Scalar color, int thickness) {
        List<Mat> pts_tmplm = new ArrayList<Mat>((pts != null) ? pts.size() : 0);
        Mat pts_mat = Converters.vector_vector_Point_to_Mat(pts, pts_tmplm);
        polylines_2(
                img.nativeObj,
                pts_mat.nativeObj,
                isClosed,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws several polygonal curves.
     *
     * @param img      image to process or annotate
     * @param pts      Array of polygonal curves
     * @param isClosed Flag indicating whether the drawn polylines are closed or not. If they are closed, the function
     *                 draws a line from the last vertex of each curve to its first vertex
     * @param color    Polyline color
     * @throws NullPointerException if img, color is null
     */
    public static void polylines(Mat img, List<MatOfPoint> pts, boolean isClosed, Scalar color) {
        List<Mat> pts_tmplm = new ArrayList<Mat>((pts != null) ? pts.size() : 0);
        Mat pts_mat = Converters.vector_vector_Point_to_Mat(pts, pts_tmplm);
        polylines_3(img.nativeObj, pts_mat.nativeObj, isClosed, color.val[0], color.val[1], color.val[2], color.val[3]);
    }

    /**
     * Draws contour outlines or fills contour interiors using the requested color, thickness, hierarchy and coordinate
     * offset.
     *
     * @param image      Destination image
     * @param contours   All the input contours. Each contour is stored as a point vector
     * @param contourIdx Parameter indicating a contour to draw. If it is negative, all the contours are drawn
     * @param color      Color of the contours
     * @param thickness  Thickness of lines the contours are drawn with. If it is negative (for example,
     *                   thickness=FILLED ), the contour interiors are drawn
     * @param lineType   Line connectivity. See LineTypes
     * @param hierarchy  Optional information about hierarchy. It is only needed if you want to draw only some of the
     *                   contours (see maxLevel )
     * @param maxLevel   Maximal level for drawn contours. If it is 0, only the specified contour is drawn. If it is 1,
     *                   the function draws the contour(s) and all the nested contours. If it is 2, the function draws
     *                   the contours, all the nested contours, all the nested-to-nested contours, and so on. This
     *                   parameter is only taken into account when there is hierarchy available
     * @param offset     Optional contour shift parameter. Shift all the drawn contours by the specified {@code
     *     offset}    =(dx,dy) . Note: When thickness=FILLED, the function is designed to handle connected components
     *                   with holes correctly even when no hierarchy data is provided. This is done by analyzing all the
     *                   outlines together using even-odd rule. This may give incorrect results if you have a joint
     *                   collection of separately retrieved contours. In order to solve this problem, you need to call
     *                   drawContours separately for each sub-group of contours, or iterate over the collection using
     *                   contourIdx parameter
     * @throws NullPointerException if image, color, hierarchy, offset is null
     */
    public static void drawContours(
            Mat image,
            List<MatOfPoint> contours,
            int contourIdx,
            Scalar color,
            int thickness,
            int lineType,
            Mat hierarchy,
            int maxLevel,
            Point offset) {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_0(
                image.nativeObj,
                contours_mat.nativeObj,
                contourIdx,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                hierarchy.nativeObj,
                maxLevel,
                offset.x,
                offset.y);
    }

    /**
     * Draws contour outlines or fills contour interiors using the requested color, thickness, hierarchy and coordinate
     * offset.
     *
     * @param image      Destination image
     * @param contours   All the input contours. Each contour is stored as a point vector
     * @param contourIdx Parameter indicating a contour to draw. If it is negative, all the contours are drawn
     * @param color      Color of the contours
     * @param thickness  Thickness of lines the contours are drawn with. If it is negative (for example,
     *                   thickness=FILLED ), the contour interiors are drawn
     * @param lineType   Line connectivity. See LineTypes
     * @param hierarchy  Optional information about hierarchy. It is only needed if you want to draw only some of the
     *                   contours (see maxLevel )
     * @param maxLevel   Maximal level for drawn contours. If it is 0, only the specified contour is drawn. If it is 1,
     *                   the function draws the contour(s) and all the nested contours. If it is 2, the function draws
     *                   the contours, all the nested contours, all the nested-to-nested contours, and so on. This
     *                   parameter is only taken into account when there is hierarchy available
     * @throws NullPointerException if image, color, hierarchy is null
     */
    public static void drawContours(
            Mat image,
            List<MatOfPoint> contours,
            int contourIdx,
            Scalar color,
            int thickness,
            int lineType,
            Mat hierarchy,
            int maxLevel) {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_1(
                image.nativeObj,
                contours_mat.nativeObj,
                contourIdx,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                hierarchy.nativeObj,
                maxLevel);
    }

    /**
     * Draws contour outlines or fills contour interiors using the requested color, thickness, hierarchy and coordinate
     * offset.
     *
     * @param image      Destination image
     * @param contours   All the input contours. Each contour is stored as a point vector
     * @param contourIdx Parameter indicating a contour to draw. If it is negative, all the contours are drawn
     * @param color      Color of the contours
     * @param thickness  Thickness of lines the contours are drawn with. If it is negative (for example,
     *                   thickness=FILLED ), the contour interiors are drawn
     * @param lineType   Line connectivity. See LineTypes
     * @param hierarchy  Optional information about hierarchy. It is only needed if you want to draw only some of the
     *                   contours (see maxLevel )
     * @throws NullPointerException if image, color, hierarchy is null
     */
    public static void drawContours(
            Mat image,
            List<MatOfPoint> contours,
            int contourIdx,
            Scalar color,
            int thickness,
            int lineType,
            Mat hierarchy) {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_2(
                image.nativeObj,
                contours_mat.nativeObj,
                contourIdx,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                hierarchy.nativeObj);
    }

    /**
     * Draws contour outlines or fills contour interiors using the requested color, thickness, hierarchy and coordinate
     * offset.
     *
     * @param image      Destination image
     * @param contours   All the input contours. Each contour is stored as a point vector
     * @param contourIdx Parameter indicating a contour to draw. If it is negative, all the contours are drawn
     * @param color      Color of the contours
     * @param thickness  Thickness of lines the contours are drawn with. If it is negative (for example,
     *                   thickness=FILLED ), the contour interiors are drawn
     * @param lineType   Line connectivity. See LineTypes
     * @throws NullPointerException if image, color is null
     */
    public static void drawContours(
            Mat image,
            List<MatOfPoint> contours,
            int contourIdx,
            Scalar color,
            int thickness,
            int lineType) {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_3(
                image.nativeObj,
                contours_mat.nativeObj,
                contourIdx,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws contour outlines or fills contour interiors using the requested color, thickness, hierarchy and coordinate
     * offset.
     *
     * @param image      Destination image
     * @param contours   All the input contours. Each contour is stored as a point vector
     * @param contourIdx Parameter indicating a contour to draw. If it is negative, all the contours are drawn
     * @param color      Color of the contours
     * @param thickness  Thickness of lines the contours are drawn with. If it is negative (for example,
     *                   thickness=FILLED ), the contour interiors are drawn
     * @throws NullPointerException if image, color is null
     */
    public static void drawContours(Mat image, List<MatOfPoint> contours, int contourIdx, Scalar color, int thickness) {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_4(
                image.nativeObj,
                contours_mat.nativeObj,
                contourIdx,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws contour outlines or fills contour interiors using the requested color, thickness, hierarchy and coordinate
     * offset.
     *
     * @param image      Destination image
     * @param contours   All the input contours. Each contour is stored as a point vector
     * @param contourIdx Parameter indicating a contour to draw. If it is negative, all the contours are drawn
     * @param color      Color of the contours
     * @throws NullPointerException if image, color is null
     */
    public static void drawContours(Mat image, List<MatOfPoint> contours, int contourIdx, Scalar color) {
        List<Mat> contours_tmplm = new ArrayList<Mat>((contours != null) ? contours.size() : 0);
        Mat contours_mat = Converters.vector_vector_Point_to_Mat(contours, contours_tmplm);
        drawContours_5(
                image.nativeObj,
                contours_mat.nativeObj,
                contourIdx,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3]);
    }

    /**
     * Clips a line segment to the specified image rectangle and updates its endpoints.
     *
     * @param imgRect Image rectangle
     * @param pt1     First line point
     * @param pt2     Second line point
     * @return true if the clipped line intersects the image rectangle
     * @throws NullPointerException if imgRect is null
     */
    public static boolean clipLine(Rect imgRect, Point pt1, Point pt2) {
        double[] pt1_out = new double[2];
        double[] pt2_out = new double[2];
        boolean retVal = clipLine_0(
                imgRect.x,
                imgRect.y,
                imgRect.width,
                imgRect.height,
                pt1.x,
                pt1.y,
                pt1_out,
                pt2.x,
                pt2.y,
                pt2_out);
        if (pt1 != null) {
            pt1.x = pt1_out[0];
            pt1.y = pt1_out[1];
        }
        if (pt2 != null) {
            pt2.x = pt2_out[0];
            pt2.y = pt2_out[1];
        }
        return retVal;
    }

    /**
     * Approximates an elliptic arc with a polyline. The function ellipse2Poly computes the vertices of a polyline that
     * approximates the specified elliptic arc. It is used by ellipse. If arcStart is greater than arcEnd, they are
     * swapped.
     *
     * @param center   Center of the arc
     * @param axes     Half of the size of the ellipse main axes. See ellipse for details
     * @param angle    Rotation angle of the ellipse in degrees. See ellipse for details
     * @param arcStart Starting angle of the elliptic arc in degrees
     * @param arcEnd   Ending angle of the elliptic arc in degrees
     * @param delta    Angle between the subsequent polyline vertices. It defines the approximation accuracy
     * @param pts      Output vector of polyline vertices
     * @throws NullPointerException if center, axes is null
     */
    public static void ellipse2Poly(
            Point center,
            Size axes,
            int angle,
            int arcStart,
            int arcEnd,
            int delta,
            MatOfPoint pts) {
        Mat pts_mat = pts;
        ellipse2Poly_0(center.x, center.y, axes.width, axes.height, angle, arcStart, arcEnd, delta, pts_mat.nativeObj);
    }

    /**
     * Draws a text string using specified font. The function putText renders the specified text string in the image.
     * Symbols that cannot be rendered using the specified font are replaced by question marks. See getTextSize for a
     * text rendering code example. The function returns the coordinates in pixels from where the text can be continued.
     *
     * @param img              image to process or annotate
     * @param text             Text string to be drawn
     * @param org              Bottom-left corner of the first character of the printed text (see PUT_TEXT_ALIGN_...
     *                         though)
     * @param fontFace         Font type, see HersheyFonts
     * @param fontScale        Font scale factor that is multiplied by the font-specific base size
     * @param color            Text color
     * @param thickness        Thickness of the lines used to draw a text
     * @param lineType         Line type. See LineTypes
     * @param bottomLeftOrigin When true, the image data origin is at the bottom-left corner. Otherwise, it is at the
     *                         top-left corner
     * @throws NullPointerException if img, org, color is null
     */
    public static void putText(
            Mat img,
            String text,
            Point org,
            int fontFace,
            double fontScale,
            Scalar color,
            int thickness,
            int lineType,
            boolean bottomLeftOrigin) {
        putText_0(
                img.nativeObj,
                text,
                org.x,
                org.y,
                fontFace,
                fontScale,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType,
                bottomLeftOrigin);
    }

    /**
     * Draws a text string using specified font. The function putText renders the specified text string in the image.
     * Symbols that cannot be rendered using the specified font are replaced by question marks. See getTextSize for a
     * text rendering code example. The function returns the coordinates in pixels from where the text can be continued.
     *
     * @param img       image to process or annotate
     * @param text      Text string to be drawn
     * @param org       Bottom-left corner of the first character of the printed text (see PUT_TEXT_ALIGN_... though)
     * @param fontFace  Font type, see HersheyFonts
     * @param fontScale Font scale factor that is multiplied by the font-specific base size
     * @param color     Text color
     * @param thickness Thickness of the lines used to draw a text
     * @param lineType  Line type. See LineTypes it is at the top-left corner
     * @throws NullPointerException if img, org, color is null
     */
    public static void putText(
            Mat img,
            String text,
            Point org,
            int fontFace,
            double fontScale,
            Scalar color,
            int thickness,
            int lineType) {
        putText_1(
                img.nativeObj,
                text,
                org.x,
                org.y,
                fontFace,
                fontScale,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness,
                lineType);
    }

    /**
     * Draws a text string using specified font. The function putText renders the specified text string in the image.
     * Symbols that cannot be rendered using the specified font are replaced by question marks. See getTextSize for a
     * text rendering code example. The function returns the coordinates in pixels from where the text can be continued.
     *
     * @param img       image to process or annotate
     * @param text      Text string to be drawn
     * @param org       Bottom-left corner of the first character of the printed text (see PUT_TEXT_ALIGN_... though)
     * @param fontFace  Font type, see HersheyFonts
     * @param fontScale Font scale factor that is multiplied by the font-specific base size
     * @param color     Text color
     * @param thickness Thickness of the lines used to draw a text. it is at the top-left corner
     * @throws NullPointerException if img, org, color is null
     */
    public static void putText(
            Mat img,
            String text,
            Point org,
            int fontFace,
            double fontScale,
            Scalar color,
            int thickness) {
        putText_2(
                img.nativeObj,
                text,
                org.x,
                org.y,
                fontFace,
                fontScale,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                thickness);
    }

    /**
     * Draws a text string using specified font. The function putText renders the specified text string in the image.
     * Symbols that cannot be rendered using the specified font are replaced by question marks. See getTextSize for a
     * text rendering code example. The function returns the coordinates in pixels from where the text can be continued.
     *
     * @param img       image to process or annotate
     * @param text      Text string to be drawn
     * @param org       Bottom-left corner of the first character of the printed text (see PUT_TEXT_ALIGN_... though)
     * @param fontFace  Font type, see HersheyFonts
     * @param fontScale Font scale factor that is multiplied by the font-specific base size
     * @param color     Text color
     * @throws NullPointerException if img, org, color is null
     */
    public static void putText(Mat img, String text, Point org, int fontFace, double fontScale, Scalar color) {
        putText_3(
                img.nativeObj,
                text,
                org.x,
                org.y,
                fontFace,
                fontScale,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3]);
    }

    /**
     * Calculates the font-specific size to use to achieve a given height in pixels.
     *
     * @param fontFace    Font to use, see HersheyFonts
     * @param pixelHeight Pixel height to compute the fontScale for
     * @param thickness   Thickness of lines used to render the text.See putText for details
     * @return The fontSize to use for putText
     */
    public static double getFontScaleFromHeight(int fontFace, int pixelHeight, int thickness) {
        return getFontScaleFromHeight_0(fontFace, pixelHeight, thickness);
    }

    /**
     * Calculates the font-specific size to use to achieve a given height in pixels.
     *
     * @param fontFace    Font to use, see HersheyFonts
     * @param pixelHeight Pixel height to compute the fontScale for
     * @return The fontSize to use for putText
     */
    public static double getFontScaleFromHeight(int fontFace, int pixelHeight) {
        return getFontScaleFromHeight_1(fontFace, pixelHeight);
    }

    /**
     * Draws a text string using specified font. The function putText renders the specified text string in the image.
     * Symbols that cannot be rendered using the specified font are replaced by question marks. See getTextSize for a
     * text rendering code example. The function returns the coordinates in pixels from where the text can be continued.
     *
     * @param img    image to process or annotate
     * @param text   Text string to be drawn
     * @param org    Bottom-left corner of the first character of the printed text (see PUT_TEXT_ALIGN_... though)
     * @param color  Text color
     * @param fface  The font to use for the text
     * @param size   Font size in pixels (by default) or pts
     * @param weight Font weight, 100..1000, where 100 is "thin" font, 400 is "regular", 600 is "semibold", 800 is
     *               "bold" and beyond that is "black". The parameter is ignored if the font is not a variable font or
     *               if it does not provide variation along 'wght' axis. If the weight is 0, then the weight, currently
     *               set via setInstance(), is used
     * @param flags  Various flags, see PUT_TEXT_
     * @param wrap   The optional text wrapping range: In the case of left-to-right (LTR) text if the printed character
     *               would cross wrap.end boundary, the "cursor" is set to wrap.start. In the case of right-to-left
     *               (RTL) text it's vice versa. If the parameters is not set, [org.x, img.cols] is used for LTR text
     *               and [0, org.x] is for RTL one
     * @return the final pen position after rendering the text
     * @throws NullPointerException if img, org, color, fface, wrap is null
     */
    public static Point putText(
            Mat img,
            String text,
            Point org,
            Scalar color,
            FontFace fface,
            int size,
            int weight,
            int flags,
            Range wrap) {
        return new Point(putText_4(
                img.nativeObj,
                text,
                org.x,
                org.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                fface.getNativeObjAddr(),
                size,
                weight,
                flags,
                wrap.start,
                wrap.end));
    }

    /**
     * Draws a text string using specified font. The function putText renders the specified text string in the image.
     * Symbols that cannot be rendered using the specified font are replaced by question marks. See getTextSize for a
     * text rendering code example. The function returns the coordinates in pixels from where the text can be continued.
     *
     * @param img    image to process or annotate
     * @param text   Text string to be drawn
     * @param org    Bottom-left corner of the first character of the printed text (see PUT_TEXT_ALIGN_... though)
     * @param color  Text color
     * @param fface  The font to use for the text
     * @param size   Font size in pixels (by default) or pts
     * @param weight Font weight, 100..1000, where 100 is "thin" font, 400 is "regular", 600 is "semibold", 800 is
     *               "bold" and beyond that is "black". The parameter is ignored if the font is not a variable font or
     *               if it does not provide variation along 'wght' axis. If the weight is 0, then the weight, currently
     *               set via setInstance(), is used
     * @param flags  Various flags, see PUT_TEXT_
     * @return the final pen position after rendering the text
     * @throws NullPointerException if img, org, color, fface is null
     */
    public static Point putText(
            Mat img,
            String text,
            Point org,
            Scalar color,
            FontFace fface,
            int size,
            int weight,
            int flags) {
        return new Point(putText_5(
                img.nativeObj,
                text,
                org.x,
                org.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                fface.getNativeObjAddr(),
                size,
                weight,
                flags));
    }

    /**
     * Draws a text string using specified font. The function putText renders the specified text string in the image.
     * Symbols that cannot be rendered using the specified font are replaced by question marks. See getTextSize for a
     * text rendering code example. The function returns the coordinates in pixels from where the text can be continued.
     *
     * @param img    image to process or annotate
     * @param text   Text string to be drawn
     * @param org    Bottom-left corner of the first character of the printed text (see PUT_TEXT_ALIGN_... though)
     * @param color  Text color
     * @param fface  The font to use for the text
     * @param size   Font size in pixels (by default) or pts
     * @param weight Font weight, 100..1000, where 100 is "thin" font, 400 is "regular", 600 is "semibold", 800 is
     *               "bold" and beyond that is "black". The parameter is ignored if the font is not a variable font or
     *               if it does not provide variation along 'wght' axis. If the weight is 0, then the weight, currently
     *               set via setInstance(), is used
     * @return the final pen position after rendering the text
     * @throws NullPointerException if img, org, color, fface is null
     */
    public static Point putText(Mat img, String text, Point org, Scalar color, FontFace fface, int size, int weight) {
        return new Point(putText_6(
                img.nativeObj,
                text,
                org.x,
                org.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                fface.getNativeObjAddr(),
                size,
                weight));
    }

    /**
     * Draws a text string using specified font. The function putText renders the specified text string in the image.
     * Symbols that cannot be rendered using the specified font are replaced by question marks. See getTextSize for a
     * text rendering code example. The function returns the coordinates in pixels from where the text can be continued.
     *
     * @param img   image to process or annotate
     * @param text  Text string to be drawn
     * @param org   Bottom-left corner of the first character of the printed text (see PUT_TEXT_ALIGN_... though)
     * @param color Text color
     * @param fface The font to use for the text
     * @param size  Font size in pixels (by default) or pts
     * @return the final pen position after rendering the text
     * @throws NullPointerException if img, org, color, fface is null
     */
    public static Point putText(Mat img, String text, Point org, Scalar color, FontFace fface, int size) {
        return new Point(putText_7(
                img.nativeObj,
                text,
                org.x,
                org.y,
                color.val[0],
                color.val[1],
                color.val[2],
                color.val[3],
                fface.getNativeObjAddr(),
                size));
    }

    /**
     * Finds lines in a binary image using the standard Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image       input image
     * @param lines       matrix receiving or containing detected line segments
     * @param rho         distance resolution of the line accumulator, in pixels
     * @param theta       angular resolution of the line accumulator, in radians
     * @param threshold   minimum accumulator vote threshold for retaining a detected line
     * @param srn         distance-resolution divisor for the multiscale Hough transform
     * @param stn         angle-resolution divisor for the multiscale Hough transform
     * @param min_theta   minimum line angle searched, in radians
     * @param max_theta   maximum line angle searched, in radians
     * @param use_edgeval whether edgeval is enabled
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesWithAccumulator(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta,
            double max_theta,
            boolean use_edgeval) {
        HoughLinesWithAccumulator_0(
                image.nativeObj,
                lines.nativeObj,
                rho,
                theta,
                threshold,
                srn,
                stn,
                min_theta,
                max_theta,
                use_edgeval);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image     input image
     * @param lines     matrix receiving or containing detected line segments
     * @param rho       distance resolution of the line accumulator, in pixels
     * @param theta     angular resolution of the line accumulator, in radians
     * @param threshold minimum accumulator vote threshold for retaining a detected line
     * @param srn       distance-resolution divisor for the multiscale Hough transform
     * @param stn       angle-resolution divisor for the multiscale Hough transform
     * @param min_theta minimum line angle searched, in radians
     * @param max_theta maximum line angle searched, in radians
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesWithAccumulator(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta,
            double max_theta) {
        HoughLinesWithAccumulator_1(
                image.nativeObj,
                lines.nativeObj,
                rho,
                theta,
                threshold,
                srn,
                stn,
                min_theta,
                max_theta);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image     input image
     * @param lines     matrix receiving or containing detected line segments
     * @param rho       distance resolution of the line accumulator, in pixels
     * @param theta     angular resolution of the line accumulator, in radians
     * @param threshold minimum accumulator vote threshold for retaining a detected line
     * @param srn       distance-resolution divisor for the multiscale Hough transform
     * @param stn       angle-resolution divisor for the multiscale Hough transform
     * @param min_theta minimum line angle searched, in radians
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesWithAccumulator(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta) {
        HoughLinesWithAccumulator_2(image.nativeObj, lines.nativeObj, rho, theta, threshold, srn, stn, min_theta);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image     input image
     * @param lines     matrix receiving or containing detected line segments
     * @param rho       distance resolution of the line accumulator, in pixels
     * @param theta     angular resolution of the line accumulator, in radians
     * @param threshold minimum accumulator vote threshold for retaining a detected line
     * @param srn       distance-resolution divisor for the multiscale Hough transform
     * @param stn       angle-resolution divisor for the multiscale Hough transform
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesWithAccumulator(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn) {
        HoughLinesWithAccumulator_3(image.nativeObj, lines.nativeObj, rho, theta, threshold, srn, stn);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image     input image
     * @param lines     matrix receiving or containing detected line segments
     * @param rho       distance resolution of the line accumulator, in pixels
     * @param theta     angular resolution of the line accumulator, in radians
     * @param threshold minimum accumulator vote threshold for retaining a detected line
     * @param srn       distance-resolution divisor for the multiscale Hough transform
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesWithAccumulator(
            Mat image,
            Mat lines,
            double rho,
            double theta,
            int threshold,
            double srn) {
        HoughLinesWithAccumulator_4(image.nativeObj, lines.nativeObj, rho, theta, threshold, srn);
    }

    /**
     * Finds lines in a binary image using the standard Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image     input image
     * @param lines     matrix receiving or containing detected line segments
     * @param rho       distance resolution of the line accumulator, in pixels
     * @param theta     angular resolution of the line accumulator, in radians
     * @param threshold minimum accumulator vote threshold for retaining a detected line
     * @throws NullPointerException if image, lines is null
     */
    public static void HoughLinesWithAccumulator(Mat image, Mat lines, double rho, double theta, int threshold) {
        HoughLinesWithAccumulator_5(image.nativeObj, lines.nativeObj, rho, theta, threshold);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image     input image
     * @param circles   output matrix of detected circle centers and radii
     * @param method    algorithm or decomposition selector
     * @param dp        ratio of image resolution to accumulator resolution
     * @param minDist   minimum distance in image pixels between detected centers
     * @param param1    upper edge-detector threshold used by circle detection
     * @param param2    circle-method-specific accumulator or detection threshold
     * @param minRadius minimum circle radius, in pixels
     * @param maxRadius maximum circle radius, in pixels
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCirclesWithAccumulator(
            Mat image,
            Mat circles,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2,
            int minRadius,
            int maxRadius) {
        HoughCirclesWithAccumulator_0(
                image.nativeObj,
                circles.nativeObj,
                method,
                dp,
                minDist,
                param1,
                param2,
                minRadius,
                maxRadius);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image     input image
     * @param circles   output matrix of detected circle centers and radii
     * @param method    algorithm or decomposition selector
     * @param dp        ratio of image resolution to accumulator resolution
     * @param minDist   minimum distance in image pixels between detected centers
     * @param param1    upper edge-detector threshold used by circle detection
     * @param param2    circle-method-specific accumulator or detection threshold
     * @param minRadius minimum circle radius, in pixels
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCirclesWithAccumulator(
            Mat image,
            Mat circles,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2,
            int minRadius) {
        HoughCirclesWithAccumulator_1(
                image.nativeObj,
                circles.nativeObj,
                method,
                dp,
                minDist,
                param1,
                param2,
                minRadius);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image   input image
     * @param circles output matrix of detected circle centers and radii
     * @param method  algorithm or decomposition selector
     * @param dp      ratio of image resolution to accumulator resolution
     * @param minDist minimum distance in image pixels between detected centers
     * @param param1  upper edge-detector threshold used by circle detection
     * @param param2  circle-method-specific accumulator or detection threshold
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCirclesWithAccumulator(
            Mat image,
            Mat circles,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2) {
        HoughCirclesWithAccumulator_2(image.nativeObj, circles.nativeObj, method, dp, minDist, param1, param2);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image   input image
     * @param circles output matrix of detected circle centers and radii
     * @param method  algorithm or decomposition selector
     * @param dp      ratio of image resolution to accumulator resolution
     * @param minDist minimum distance in image pixels between detected centers
     * @param param1  upper edge-detector threshold used by circle detection
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCirclesWithAccumulator(
            Mat image,
            Mat circles,
            int method,
            double dp,
            double minDist,
            double param1) {
        HoughCirclesWithAccumulator_3(image.nativeObj, circles.nativeObj, method, dp, minDist, param1);
    }

    /**
     * Finds circles in a grayscale image using the Hough transform and get accumulator. Note: This function is for
     * bindings use only. Use original function in C++ code.
     *
     * @param image   input image
     * @param circles output matrix of detected circle centers and radii
     * @param method  algorithm or decomposition selector
     * @param dp      ratio of image resolution to accumulator resolution
     * @param minDist minimum distance in image pixels between detected centers
     * @throws NullPointerException if image, circles is null
     */
    public static void HoughCirclesWithAccumulator(Mat image, Mat circles, int method, double dp, double minDist) {
        HoughCirclesWithAccumulator_4(image.nativeObj, circles.nativeObj, method, dp, minDist);
    }

    /**
     * Returns the text bounding width and height, excluding the baseline offset returned separately.
     *
     * @param text      text whose bounding dimensions and baseline are measured
     * @param fontFace  Hershey font identifier and optional style flags
     * @param fontScale scale applied to the selected Hershey font
     * @param thickness stroke width in pixels; a negative value requests filling where supported
     * @param baseLine  optional one-element array receiving the baseline offset; null omits that output
     * @return the text bounding width and height, excluding the baseline offset returned separately
     * @throws IllegalArgumentException if baseLine is non-null and its length is not one
     */
    public static Size getTextSize(String text, int fontFace, double fontScale, int thickness, int[] baseLine) {
        if (baseLine != null && baseLine.length != 1)
            throw new java.lang.IllegalArgumentException("'baseLine' must be 'int[1]' or 'null'.");
        Size retVal = new Size(n_getTextSize(text, fontFace, fontScale, thickness, baseLine));
        return retVal;
    }

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector(int,double,double,double,double,double,double,int)}.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @param ang_th      Gradient angle tolerance in degrees
     * @param log_eps     Detection threshold: -log10(NFA) &gt; log_eps. Used only when advance refinement is chosen
     * @param density_th  Minimal density of aligned region points in the enclosing rectangle
     * @param n_bins      Number of bins in pseudo-ordering of gradient modulus
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_0(
            int refine,
            double scale,
            double sigma_scale,
            double quant,
            double ang_th,
            double log_eps,
            double density_th,
            int n_bins);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector(int,double,double,double,double,double,double)}.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @param ang_th      Gradient angle tolerance in degrees
     * @param log_eps     Detection threshold: -log10(NFA) &gt; log_eps. Used only when advance refinement is chosen
     * @param density_th  Minimal density of aligned region points in the enclosing rectangle
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_1(
            int refine,
            double scale,
            double sigma_scale,
            double quant,
            double ang_th,
            double log_eps,
            double density_th);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector(int,double,double,double,double,double)}.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @param ang_th      Gradient angle tolerance in degrees
     * @param log_eps     Detection threshold: -log10(NFA) &gt; log_eps. Used only when advance refinement is chosen
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_2(
            int refine,
            double scale,
            double sigma_scale,
            double quant,
            double ang_th,
            double log_eps);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector(int,double,double,double,double)}.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @param ang_th      Gradient angle tolerance in degrees
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_3(
            int refine,
            double scale,
            double sigma_scale,
            double quant,
            double ang_th);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector(int,double,double,double)}.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @param quant       Bound to the quantization error on the gradient norm
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_4(int refine, double scale, double sigma_scale, double quant);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector(int,double,double)}.
     *
     * @param refine      The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale       The scale of the image that will be used to find the lines. Range (0..1]
     * @param sigma_scale Sigma for Gaussian filter. It is computed as sigma = sigma_scale/scale
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_5(int refine, double scale, double sigma_scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector(int,double)}.
     *
     * @param refine The way found lines will be refined, see LineSegmentDetectorModes
     * @param scale  The scale of the image that will be used to find the lines. Range (0..1]
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_6(int refine, double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector(int)}.
     *
     * @param refine The way found lines will be refined, see LineSegmentDetectorModes
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_7(int refine);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createLineSegmentDetector()}.
     *
     * @return native binding handle for a new {@code LineSegmentDetector} wrapper configured with the selected options
     */
    private static native long createLineSegmentDetector_8();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getGaussianKernel(int,double,int)}.
     *
     * @param ksize Aperture size. It should be odd ( {@code ksize} mod 2 = 1 ) and positive
     * @param sigma Gaussian standard deviation. If it is non-positive, it is computed from ksize as sigma =
     *              0.3*((ksize-1)*0.5 - 1) + 0.8
     * @param ktype Type of filter coefficients. It can be CV_32F or CV_64F
     * @return native binding handle for the normalized one-dimensional Gaussian coefficient column
     */
    private static native long getGaussianKernel_0(int ksize, double sigma, int ktype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getGaussianKernel(int,double)}.
     *
     * @param ksize Aperture size. It should be odd ( {@code ksize} mod 2 = 1 ) and positive
     * @param sigma Gaussian standard deviation. If it is non-positive, it is computed from ksize as sigma =
     *              0.3*((ksize-1)*0.5 - 1) + 0.8
     * @return native binding handle for the normalized one-dimensional Gaussian coefficient column
     */
    private static native long getGaussianKernel_1(int ksize, double sigma);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#getDerivKernels(Mat,Mat,int,int,int,boolean,int)}.
     *
     * @param kx_nativeObj native matrix or object handle for {@code kx}; Output matrix of row filter coefficients. It
     *                     has the type ktype
     * @param ky_nativeObj native matrix or object handle for {@code ky}; Output matrix of column filter coefficients.
     *                     It has the type ktype
     * @param dx           Derivative order in respect of x
     * @param dy           Derivative order in respect of y
     * @param ksize        Aperture size. It can be FILTER_SCHARR, 1, 3, 5, or 7
     * @param normalize    Flag indicating whether to normalize (scale down) the filter coefficients or not.
     *                     Theoretically, the coefficients should have the denominator =2^{ksize*2-dx-dy-2}. If you are
     *                     going to filter floating-point images, you are likely to use the normalized kernels. But if
     *                     you compute derivatives of an 8-bit image, store the results in a 16-bit image, and wish to
     *                     preserve all the fractional bits, you may want to set normalize=false
     * @param ktype        Type of filter coefficients. It can be CV_32f or CV_64F
     */
    private static native void getDerivKernels_0(
            long kx_nativeObj,
            long ky_nativeObj,
            int dx,
            int dy,
            int ksize,
            boolean normalize,
            int ktype);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#getDerivKernels(Mat,Mat,int,int,int,boolean)}.
     *
     * @param kx_nativeObj native matrix or object handle for {@code kx}; Output matrix of row filter coefficients. It
     *                     has the type ktype
     * @param ky_nativeObj native matrix or object handle for {@code ky}; Output matrix of column filter coefficients.
     *                     It has the type ktype
     * @param dx           Derivative order in respect of x
     * @param dy           Derivative order in respect of y
     * @param ksize        Aperture size. It can be FILTER_SCHARR, 1, 3, 5, or 7
     * @param normalize    Flag indicating whether to normalize (scale down) the filter coefficients or not.
     *                     Theoretically, the coefficients should have the denominator =2^{ksize*2-dx-dy-2}. If you are
     *                     going to filter floating-point images, you are likely to use the normalized kernels. But if
     *                     you compute derivatives of an 8-bit image, store the results in a 16-bit image, and wish to
     *                     preserve all the fractional bits, you may want to set normalize=false
     */
    private static native void getDerivKernels_1(
            long kx_nativeObj,
            long ky_nativeObj,
            int dx,
            int dy,
            int ksize,
            boolean normalize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getDerivKernels(Mat,Mat,int,int,int)}.
     *
     * @param kx_nativeObj native matrix or object handle for {@code kx}; Output matrix of row filter coefficients. It
     *                     has the type ktype
     * @param ky_nativeObj native matrix or object handle for {@code ky}; Output matrix of column filter coefficients.
     *                     It has the type ktype
     * @param dx           Derivative order in respect of x
     * @param dy           Derivative order in respect of y
     * @param ksize        Aperture size. It can be FILTER_SCHARR, 1, 3, 5, or 7
     */
    private static native void getDerivKernels_2(long kx_nativeObj, long ky_nativeObj, int dx, int dy, int ksize);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#getGaborKernel(Size,double,double,double,double,double,int)}.
     *
     * @param ksize_width  {@code ksize.width} component of Size of the filter returned
     * @param ksize_height {@code ksize.height} component of Size of the filter returned
     * @param sigma        Standard deviation of the gaussian envelope
     * @param theta        Orientation of the normal to the parallel stripes of a Gabor function
     * @param lambd        Wavelength of the sinusoidal factor
     * @param gamma        Spatial aspect ratio
     * @param psi          Phase offset
     * @param ktype        Type of filter coefficients. It can be CV_32F or CV_64F
     * @return native binding handle for the two-dimensional real Gabor filter kernel
     */
    private static native long getGaborKernel_0(
            double ksize_width,
            double ksize_height,
            double sigma,
            double theta,
            double lambd,
            double gamma,
            double psi,
            int ktype);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#getGaborKernel(Size,double,double,double,double,double)}.
     *
     * @param ksize_width  {@code ksize.width} component of Size of the filter returned
     * @param ksize_height {@code ksize.height} component of Size of the filter returned
     * @param sigma        Standard deviation of the gaussian envelope
     * @param theta        Orientation of the normal to the parallel stripes of a Gabor function
     * @param lambd        Wavelength of the sinusoidal factor
     * @param gamma        Spatial aspect ratio
     * @param psi          Phase offset
     * @return native binding handle for the two-dimensional real Gabor filter kernel
     */
    private static native long getGaborKernel_1(
            double ksize_width,
            double ksize_height,
            double sigma,
            double theta,
            double lambd,
            double gamma,
            double psi);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#getGaborKernel(Size,double,double,double,double)}.
     *
     * @param ksize_width  {@code ksize.width} component of Size of the filter returned
     * @param ksize_height {@code ksize.height} component of Size of the filter returned
     * @param sigma        Standard deviation of the gaussian envelope
     * @param theta        Orientation of the normal to the parallel stripes of a Gabor function
     * @param lambd        Wavelength of the sinusoidal factor
     * @param gamma        Spatial aspect ratio
     * @return native binding handle for the two-dimensional real Gabor filter kernel
     */
    private static native long getGaborKernel_2(
            double ksize_width,
            double ksize_height,
            double sigma,
            double theta,
            double lambd,
            double gamma);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getStructuringElement(int,Size,Point)}.
     *
     * @param shape        Element shape that could be one of MorphShapes
     * @param ksize_width  {@code ksize.width} component of Size of the structuring element
     * @param ksize_height {@code ksize.height} component of Size of the structuring element
     * @param anchor_x     {@code anchor.x} component of Anchor position within the element. The default value (-1, -1)
     *                     means that the anchor is at the center. Note that only the shape of a cross-shaped element
     *                     depends on the anchor position. In other cases the anchor just regulates how much the result
     *                     of the morphological operation is shifted
     * @param anchor_y     {@code anchor.y} component of Anchor position within the element. The default value (-1, -1)
     *                     means that the anchor is at the center. Note that only the shape of a cross-shaped element
     *                     depends on the anchor position. In other cases the anchor just regulates how much the result
     *                     of the morphological operation is shifted
     * @return native binding handle for a binary morphology kernel of the requested shape and size
     */
    private static native long getStructuringElement_0(
            int shape,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getStructuringElement(int,Size)}.
     *
     * @param shape        Element shape that could be one of MorphShapes
     * @param ksize_width  {@code ksize.width} component of Size of the structuring element
     * @param ksize_height {@code ksize.height} component of Size of the structuring element
     * @return native binding handle for a binary morphology kernel of the requested shape and size
     */
    private static native long getStructuringElement_1(int shape, double ksize_width, double ksize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#medianBlur(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input 1-, 3-, or 4-channel image; when ksize
     *                      is 3 or 5, the image depth should be CV_8U, CV_16U, or CV_32F, for larger aperture sizes, it
     *                      can only be CV_8U
     * @param dst_nativeObj native matrix or object handle for {@code dst}; destination array of the same size and type
     *                      as src
     * @param ksize         aperture linear size; it must be odd and greater than 1, for example: 3, 5, 7
     */
    private static native void medianBlur_0(long src_nativeObj, long dst_nativeObj, int ksize);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#GaussianBlur(Mat,Mat,Size,double,double,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image; the image can have any number
     *                      of channels, which are processed independently, but the depth should be CV_8U, CV_16U,
     *                      CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ksize_width   {@code ksize.width} component of Gaussian kernel size. ksize.width and ksize.height can
     *                      differ but they both must be positive and odd. Or, they can be zero's and then they are
     *                      computed from sigma
     * @param ksize_height  {@code ksize.height} component of Gaussian kernel size. ksize.width and ksize.height can
     *                      differ but they both must be positive and odd. Or, they can be zero's and then they are
     *                      computed from sigma
     * @param sigmaX        Gaussian kernel standard deviation in X direction
     * @param sigmaY        Gaussian kernel standard deviation in Y direction; if sigmaY is zero, it is set to be equal
     *                      to sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height,
     *                      respectively (see getGaussianKernel for details); to fully control the result regardless of
     *                      possible future modifications of all this semantics, it is recommended to specify all of
     *                      ksize, sigmaX, and sigmaY
     * @param borderType    pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @param hint          Implementation modification flags. See AlgorithmHint
     */
    private static native void GaussianBlur_0(
            long src_nativeObj,
            long dst_nativeObj,
            double ksize_width,
            double ksize_height,
            double sigmaX,
            double sigmaY,
            int borderType,
            int hint);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#GaussianBlur(Mat,Mat,Size,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image; the image can have any number
     *                      of channels, which are processed independently, but the depth should be CV_8U, CV_16U,
     *                      CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ksize_width   {@code ksize.width} component of Gaussian kernel size. ksize.width and ksize.height can
     *                      differ but they both must be positive and odd. Or, they can be zero's and then they are
     *                      computed from sigma
     * @param ksize_height  {@code ksize.height} component of Gaussian kernel size. ksize.width and ksize.height can
     *                      differ but they both must be positive and odd. Or, they can be zero's and then they are
     *                      computed from sigma
     * @param sigmaX        Gaussian kernel standard deviation in X direction
     * @param sigmaY        Gaussian kernel standard deviation in Y direction; if sigmaY is zero, it is set to be equal
     *                      to sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height,
     *                      respectively (see getGaussianKernel for details); to fully control the result regardless of
     *                      possible future modifications of all this semantics, it is recommended to specify all of
     *                      ksize, sigmaX, and sigmaY
     * @param borderType    pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void GaussianBlur_1(
            long src_nativeObj,
            long dst_nativeObj,
            double ksize_width,
            double ksize_height,
            double sigmaX,
            double sigmaY,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#GaussianBlur(Mat,Mat,Size,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image; the image can have any number
     *                      of channels, which are processed independently, but the depth should be CV_8U, CV_16U,
     *                      CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ksize_width   {@code ksize.width} component of Gaussian kernel size. ksize.width and ksize.height can
     *                      differ but they both must be positive and odd. Or, they can be zero's and then they are
     *                      computed from sigma
     * @param ksize_height  {@code ksize.height} component of Gaussian kernel size. ksize.width and ksize.height can
     *                      differ but they both must be positive and odd. Or, they can be zero's and then they are
     *                      computed from sigma
     * @param sigmaX        Gaussian kernel standard deviation in X direction
     * @param sigmaY        Gaussian kernel standard deviation in Y direction; if sigmaY is zero, it is set to be equal
     *                      to sigmaX, if both sigmas are zeros, they are computed from ksize.width and ksize.height,
     *                      respectively (see getGaussianKernel for details); to fully control the result regardless of
     *                      possible future modifications of all this semantics, it is recommended to specify all of
     *                      ksize, sigmaX, and sigmaY
     */
    private static native void GaussianBlur_2(
            long src_nativeObj,
            long dst_nativeObj,
            double ksize_width,
            double ksize_height,
            double sigmaX,
            double sigmaY);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#GaussianBlur(Mat,Mat,Size,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image; the image can have any number
     *                      of channels, which are processed independently, but the depth should be CV_8U, CV_16U,
     *                      CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ksize_width   {@code ksize.width} component of Gaussian kernel size. ksize.width and ksize.height can
     *                      differ but they both must be positive and odd. Or, they can be zero's and then they are
     *                      computed from sigma
     * @param ksize_height  {@code ksize.height} component of Gaussian kernel size. ksize.width and ksize.height can
     *                      differ but they both must be positive and odd. Or, they can be zero's and then they are
     *                      computed from sigma
     * @param sigmaX        Gaussian kernel standard deviation in X direction
     */
    private static native void GaussianBlur_3(
            long src_nativeObj,
            long dst_nativeObj,
            double ksize_width,
            double ksize_height,
            double sigmaX);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#bilateralFilter(Mat,Mat,int,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source 8-bit or floating-point, 1-channel or
     *                      3-channel image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same size and type
     *                      as src
     * @param d             Diameter of each pixel neighborhood that is used during filtering. If it is non-positive, it
     *                      is computed from sigmaSpace
     * @param sigmaColor    Filter sigma in the color space. A larger value of the parameter means that farther colors
     *                      within the pixel neighborhood (see sigmaSpace) will be mixed together, resulting in larger
     *                      areas of semi-equal color
     * @param sigmaSpace    Filter sigma in the coordinate space. A larger value of the parameter means that farther
     *                      pixels will influence each other as long as their colors are close enough (see sigmaColor ).
     *                      When d&gt;0, it specifies the neighborhood size regardless of sigmaSpace. Otherwise, d is
     *                      proportional to sigmaSpace
     * @param borderType    border mode used to extrapolate pixels outside of the image, see BorderTypes
     */
    private static native void bilateralFilter_0(
            long src_nativeObj,
            long dst_nativeObj,
            int d,
            double sigmaColor,
            double sigmaSpace,
            int borderType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#bilateralFilter(Mat,Mat,int,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source 8-bit or floating-point, 1-channel or
     *                      3-channel image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same size and type
     *                      as src
     * @param d             Diameter of each pixel neighborhood that is used during filtering. If it is non-positive, it
     *                      is computed from sigmaSpace
     * @param sigmaColor    Filter sigma in the color space. A larger value of the parameter means that farther colors
     *                      within the pixel neighborhood (see sigmaSpace) will be mixed together, resulting in larger
     *                      areas of semi-equal color
     * @param sigmaSpace    Filter sigma in the coordinate space. A larger value of the parameter means that farther
     *                      pixels will influence each other as long as their colors are close enough (see sigmaColor ).
     *                      When d&gt;0, it specifies the neighborhood size regardless of sigmaSpace. Otherwise, d is
     *                      proportional to sigmaSpace
     */
    private static native void bilateralFilter_1(
            long src_nativeObj,
            long dst_nativeObj,
            int d,
            double sigmaColor,
            double sigmaSpace);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#boxFilter(Mat,Mat,int,Size,Point,boolean,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ddepth        the output image depth (-1 to use src.depth())
     * @param ksize_width   {@code ksize.width} component of blurring kernel size
     * @param ksize_height  {@code ksize.height} component of blurring kernel size
     * @param anchor_x      {@code anchor.x} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     * @param anchor_y      {@code anchor.y} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     * @param normalize     flag, specifying whether the kernel is normalized by its area or not
     * @param borderType    border mode used to extrapolate pixels outside of the image, see BorderTypes. BORDER_WRAP is
     *                      not supported
     */
    private static native void boxFilter_0(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y,
            boolean normalize,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#boxFilter(Mat,Mat,int,Size,Point,boolean)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ddepth        the output image depth (-1 to use src.depth())
     * @param ksize_width   {@code ksize.width} component of blurring kernel size
     * @param ksize_height  {@code ksize.height} component of blurring kernel size
     * @param anchor_x      {@code anchor.x} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     * @param anchor_y      {@code anchor.y} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     * @param normalize     flag, specifying whether the kernel is normalized by its area or not
     */
    private static native void boxFilter_1(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y,
            boolean normalize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#boxFilter(Mat,Mat,int,Size,Point)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ddepth        the output image depth (-1 to use src.depth())
     * @param ksize_width   {@code ksize.width} component of blurring kernel size
     * @param ksize_height  {@code ksize.height} component of blurring kernel size
     * @param anchor_x      {@code anchor.x} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     * @param anchor_y      {@code anchor.y} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     */
    private static native void boxFilter_2(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#boxFilter(Mat,Mat,int,Size)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ddepth        the output image depth (-1 to use src.depth())
     * @param ksize_width   {@code ksize.width} component of blurring kernel size
     * @param ksize_height  {@code ksize.height} component of blurring kernel size
     */
    private static native void boxFilter_3(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            double ksize_width,
            double ksize_height);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#sqrBoxFilter(Mat,Mat,int,Size,Point,boolean,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ddepth        the output image depth (-1 to use src.depth())
     * @param ksize_width   {@code ksize.width} component of kernel size
     * @param ksize_height  {@code ksize.height} component of kernel size
     * @param anchor_x      {@code anchor.x} component of kernel anchor point. The default value of Point(-1, -1)
     *                      denotes that the anchor is at the kernel center
     * @param anchor_y      {@code anchor.y} component of kernel anchor point. The default value of Point(-1, -1)
     *                      denotes that the anchor is at the kernel center
     * @param normalize     flag, specifying whether the kernel is to be normalized by it's area or not
     * @param borderType    border mode used to extrapolate pixels outside of the image, see BorderTypes. BORDER_WRAP is
     *                      not supported
     */
    private static native void sqrBoxFilter_0(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y,
            boolean normalize,
            int borderType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#sqrBoxFilter(Mat,Mat,int,Size,Point,boolean)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ddepth        the output image depth (-1 to use src.depth())
     * @param ksize_width   {@code ksize.width} component of kernel size
     * @param ksize_height  {@code ksize.height} component of kernel size
     * @param anchor_x      {@code anchor.x} component of kernel anchor point. The default value of Point(-1, -1)
     *                      denotes that the anchor is at the kernel center
     * @param anchor_y      {@code anchor.y} component of kernel anchor point. The default value of Point(-1, -1)
     *                      denotes that the anchor is at the kernel center
     * @param normalize     flag, specifying whether the kernel is to be normalized by it's area or not
     */
    private static native void sqrBoxFilter_1(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y,
            boolean normalize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#sqrBoxFilter(Mat,Mat,int,Size,Point)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ddepth        the output image depth (-1 to use src.depth())
     * @param ksize_width   {@code ksize.width} component of kernel size
     * @param ksize_height  {@code ksize.height} component of kernel size
     * @param anchor_x      {@code anchor.x} component of kernel anchor point. The default value of Point(-1, -1)
     *                      denotes that the anchor is at the kernel center
     * @param anchor_y      {@code anchor.y} component of kernel anchor point. The default value of Point(-1, -1)
     *                      denotes that the anchor is at the kernel center
     */
    private static native void sqrBoxFilter_2(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#sqrBoxFilter(Mat,Mat,int,Size)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ddepth        the output image depth (-1 to use src.depth())
     * @param ksize_width   {@code ksize.width} component of kernel size
     * @param ksize_height  {@code ksize.height} component of kernel size
     */
    private static native void sqrBoxFilter_3(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            double ksize_width,
            double ksize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#blur(Mat,Mat,Size,Point,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image; it can have any number of
     *                      channels, which are processed independently, but the depth should be CV_8U, CV_16U, CV_16S,
     *                      CV_32F or CV_64F
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ksize_width   {@code ksize.width} component of blurring kernel size
     * @param ksize_height  {@code ksize.height} component of blurring kernel size
     * @param anchor_x      {@code anchor.x} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     * @param anchor_y      {@code anchor.y} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     * @param borderType    border mode used to extrapolate pixels outside of the image, see BorderTypes. BORDER_WRAP is
     *                      not supported
     */
    private static native void blur_0(
            long src_nativeObj,
            long dst_nativeObj,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#blur(Mat,Mat,Size,Point)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image; it can have any number of
     *                      channels, which are processed independently, but the depth should be CV_8U, CV_16U, CV_16S,
     *                      CV_32F or CV_64F
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ksize_width   {@code ksize.width} component of blurring kernel size
     * @param ksize_height  {@code ksize.height} component of blurring kernel size
     * @param anchor_x      {@code anchor.x} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     * @param anchor_y      {@code anchor.y} component of anchor point; default value Point(-1,-1) means that the anchor
     *                      is at the kernel center
     */
    private static native void blur_1(
            long src_nativeObj,
            long dst_nativeObj,
            double ksize_width,
            double ksize_height,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#blur(Mat,Mat,Size)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image; it can have any number of
     *                      channels, which are processed independently, but the depth should be CV_8U, CV_16U, CV_16S,
     *                      CV_32F or CV_64F
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ksize_width   {@code ksize.width} component of blurring kernel size
     * @param ksize_height  {@code ksize.height} component of blurring kernel size
     */
    private static native void blur_2(long src_nativeObj, long dst_nativeObj, double ksize_width, double ksize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#stackBlur(Mat,Mat,Size)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image. The number of channels can be
     *                      arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S or CV_32F
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and type as
     *                      src
     * @param ksize_width   {@code ksize.width} component of stack-blurring kernel size. The ksize.width and
     *                      ksize.height can differ but they both must be positive and odd
     * @param ksize_height  {@code ksize.height} component of stack-blurring kernel size. The ksize.width and
     *                      ksize.height can differ but they both must be positive and odd
     */
    private static native void stackBlur_0(
            long src_nativeObj,
            long dst_nativeObj,
            double ksize_width,
            double ksize_height);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#filter2D(Mat,Mat,int,Mat,Point,double,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and the
     *                         same number of channels as src
     * @param ddepth           desired depth of the destination image, see REF: filter_depths "combinations"
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; convolution kernel (or rather a
     *                         correlation kernel), a single-channel floating point matrix; if you want to apply
     *                         different kernels to different channels, split the image into separate color planes using
     *                         split and process them individually
     * @param anchor_x         {@code anchor.x} component of anchor of the kernel that indicates the relative position
     *                         of a filtered point within the kernel; the anchor should lie within the kernel; default
     *                         value (-1,-1) means that the anchor is at the kernel center
     * @param anchor_y         {@code anchor.y} component of anchor of the kernel that indicates the relative position
     *                         of a filtered point within the kernel; the anchor should lie within the kernel; default
     *                         value (-1,-1) means that the anchor is at the kernel center
     * @param delta            optional value added to the filtered pixels before storing them in dst
     * @param borderType       pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void filter2D_0(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            double delta,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#filter2D(Mat,Mat,int,Mat,Point,double)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and the
     *                         same number of channels as src
     * @param ddepth           desired depth of the destination image, see REF: filter_depths "combinations"
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; convolution kernel (or rather a
     *                         correlation kernel), a single-channel floating point matrix; if you want to apply
     *                         different kernels to different channels, split the image into separate color planes using
     *                         split and process them individually
     * @param anchor_x         {@code anchor.x} component of anchor of the kernel that indicates the relative position
     *                         of a filtered point within the kernel; the anchor should lie within the kernel; default
     *                         value (-1,-1) means that the anchor is at the kernel center
     * @param anchor_y         {@code anchor.y} component of anchor of the kernel that indicates the relative position
     *                         of a filtered point within the kernel; the anchor should lie within the kernel; default
     *                         value (-1,-1) means that the anchor is at the kernel center
     * @param delta            optional value added to the filtered pixels before storing them in dst
     */
    private static native void filter2D_1(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            double delta);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#filter2D(Mat,Mat,int,Mat,Point)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and the
     *                         same number of channels as src
     * @param ddepth           desired depth of the destination image, see REF: filter_depths "combinations"
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; convolution kernel (or rather a
     *                         correlation kernel), a single-channel floating point matrix; if you want to apply
     *                         different kernels to different channels, split the image into separate color planes using
     *                         split and process them individually
     * @param anchor_x         {@code anchor.x} component of anchor of the kernel that indicates the relative position
     *                         of a filtered point within the kernel; the anchor should lie within the kernel; default
     *                         value (-1,-1) means that the anchor is at the kernel center
     * @param anchor_y         {@code anchor.y} component of anchor of the kernel that indicates the relative position
     *                         of a filtered point within the kernel; the anchor should lie within the kernel; default
     *                         value (-1,-1) means that the anchor is at the kernel center
     */
    private static native void filter2D_2(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#filter2D(Mat,Mat,int,Mat)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and the
     *                         same number of channels as src
     * @param ddepth           desired depth of the destination image, see REF: filter_depths "combinations"
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; convolution kernel (or rather a
     *                         correlation kernel), a single-channel floating point matrix; if you want to apply
     *                         different kernels to different channels, split the image into separate color planes using
     *                         split and process them individually
     */
    private static native void filter2D_3(long src_nativeObj, long dst_nativeObj, int ddepth, long kernel_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#filter2Dp(Mat,Mat,Mat,Filter2DParams)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; source matrix
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; destination matrix that receives the
     *                         result
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; filtering or morphological kernel
     * @param params_nativeObj {@code params.getNativeObjAddr()} component of native filter depth, anchor, border, scale
     *                         and shift settings
     */
    private static native void filter2Dp_0(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            long params_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#filter2Dp(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; source matrix
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; destination matrix that receives the
     *                         result
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; filtering or morphological kernel
     */
    private static native void filter2Dp_1(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#sepFilter2D(Mat,Mat,int,Mat,Mat,Point,double,int)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Destination image of the same size and
     *                          the same number of channels as src
     * @param ddepth            Destination image depth, see REF: filter_depths "combinations"
     * @param kernelX_nativeObj native matrix or object handle for {@code kernelX}; Coefficients for filtering each row
     * @param kernelY_nativeObj native matrix or object handle for {@code kernelY}; Coefficients for filtering each
     *                          column
     * @param anchor_x          {@code anchor.x} component of Anchor position within the kernel. The default value
     *                          (-1,-1) means that the anchor is at the kernel center
     * @param anchor_y          {@code anchor.y} component of Anchor position within the kernel. The default value
     *                          (-1,-1) means that the anchor is at the kernel center
     * @param delta             Value added to the filtered results before storing them
     * @param borderType        Pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void sepFilter2D_0(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            long kernelX_nativeObj,
            long kernelY_nativeObj,
            double anchor_x,
            double anchor_y,
            double delta,
            int borderType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#sepFilter2D(Mat,Mat,int,Mat,Mat,Point,double)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Destination image of the same size and
     *                          the same number of channels as src
     * @param ddepth            Destination image depth, see REF: filter_depths "combinations"
     * @param kernelX_nativeObj native matrix or object handle for {@code kernelX}; Coefficients for filtering each row
     * @param kernelY_nativeObj native matrix or object handle for {@code kernelY}; Coefficients for filtering each
     *                          column
     * @param anchor_x          {@code anchor.x} component of Anchor position within the kernel. The default value
     *                          (-1,-1) means that the anchor is at the kernel center
     * @param anchor_y          {@code anchor.y} component of Anchor position within the kernel. The default value
     *                          (-1,-1) means that the anchor is at the kernel center
     * @param delta             Value added to the filtered results before storing them
     */
    private static native void sepFilter2D_1(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            long kernelX_nativeObj,
            long kernelY_nativeObj,
            double anchor_x,
            double anchor_y,
            double delta);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#sepFilter2D(Mat,Mat,int,Mat,Mat,Point)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Destination image of the same size and
     *                          the same number of channels as src
     * @param ddepth            Destination image depth, see REF: filter_depths "combinations"
     * @param kernelX_nativeObj native matrix or object handle for {@code kernelX}; Coefficients for filtering each row
     * @param kernelY_nativeObj native matrix or object handle for {@code kernelY}; Coefficients for filtering each
     *                          column
     * @param anchor_x          {@code anchor.x} component of Anchor position within the kernel. The default value
     *                          (-1,-1) means that the anchor is at the kernel center
     * @param anchor_y          {@code anchor.y} component of Anchor position within the kernel. The default value
     *                          (-1,-1) means that the anchor is at the kernel center
     */
    private static native void sepFilter2D_2(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            long kernelX_nativeObj,
            long kernelY_nativeObj,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#sepFilter2D(Mat,Mat,int,Mat,Mat)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; Destination image of the same size and
     *                          the same number of channels as src
     * @param ddepth            Destination image depth, see REF: filter_depths "combinations"
     * @param kernelX_nativeObj native matrix or object handle for {@code kernelX}; Coefficients for filtering each row
     * @param kernelY_nativeObj native matrix or object handle for {@code kernelY}; Coefficients for filtering each
     *                          column
     */
    private static native void sepFilter2D_3(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            long kernelX_nativeObj,
            long kernelY_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#Sobel(Mat,Mat,int,int,int,int,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images
     *                      it will result in truncated derivatives
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     * @param ksize         size of the extended Sobel kernel; it must be 1, 3, 5, or 7
     * @param scale         optional scale factor for the computed derivative values; by default, no scaling is applied
     *                      (see getDerivKernels for details)
     * @param delta         optional delta value that is added to the results prior to storing them in dst
     * @param borderType    pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void Sobel_0(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            int dx,
            int dy,
            int ksize,
            double scale,
            double delta,
            int borderType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#Sobel(Mat,Mat,int,int,int,int,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images
     *                      it will result in truncated derivatives
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     * @param ksize         size of the extended Sobel kernel; it must be 1, 3, 5, or 7
     * @param scale         optional scale factor for the computed derivative values; by default, no scaling is applied
     *                      (see getDerivKernels for details)
     * @param delta         optional delta value that is added to the results prior to storing them in dst
     */
    private static native void Sobel_1(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            int dx,
            int dy,
            int ksize,
            double scale,
            double delta);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Sobel(Mat,Mat,int,int,int,int,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images
     *                      it will result in truncated derivatives
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     * @param ksize         size of the extended Sobel kernel; it must be 1, 3, 5, or 7
     * @param scale         optional scale factor for the computed derivative values; by default, no scaling is applied
     *                      (see getDerivKernels for details)
     */
    private static native void Sobel_2(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            int dx,
            int dy,
            int ksize,
            double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Sobel(Mat,Mat,int,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images
     *                      it will result in truncated derivatives
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     * @param ksize         size of the extended Sobel kernel; it must be 1, 3, 5, or 7
     */
    private static native void Sobel_3(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy, int ksize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Sobel(Mat,Mat,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"; in the case of 8-bit input images
     *                      it will result in truncated derivatives
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     */
    private static native void Sobel_4(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#spatialGradient(Mat,Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dx_nativeObj  native matrix or object handle for {@code dx}; output image with first-order derivative in x
     * @param dy_nativeObj  native matrix or object handle for {@code dy}; output image with first-order derivative in y
     * @param ksize         size of Sobel kernel. It must be 3
     * @param borderType    pixel extrapolation method, see BorderTypes. Only BORDER_DEFAULT=BORDER_REFLECT_101 and
     *                      BORDER_REPLICATE are supported
     */
    private static native void spatialGradient_0(
            long src_nativeObj,
            long dx_nativeObj,
            long dy_nativeObj,
            int ksize,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#spatialGradient(Mat,Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dx_nativeObj  native matrix or object handle for {@code dx}; output image with first-order derivative in x
     * @param dy_nativeObj  native matrix or object handle for {@code dy}; output image with first-order derivative in y
     * @param ksize         size of Sobel kernel. It must be 3
     */
    private static native void spatialGradient_1(long src_nativeObj, long dx_nativeObj, long dy_nativeObj, int ksize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#spatialGradient(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dx_nativeObj  native matrix or object handle for {@code dx}; output image with first-order derivative in x
     * @param dy_nativeObj  native matrix or object handle for {@code dy}; output image with first-order derivative in y
     */
    private static native void spatialGradient_2(long src_nativeObj, long dx_nativeObj, long dy_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#Scharr(Mat,Mat,int,int,int,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     * @param scale         optional scale factor for the computed derivative values; by default, no scaling is applied
     *                      (see getDerivKernels for details)
     * @param delta         optional delta value that is added to the results prior to storing them in dst
     * @param borderType    pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void Scharr_0(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            int dx,
            int dy,
            double scale,
            double delta,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Scharr(Mat,Mat,int,int,int,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     * @param scale         optional scale factor for the computed derivative values; by default, no scaling is applied
     *                      (see getDerivKernels for details)
     * @param delta         optional delta value that is added to the results prior to storing them in dst
     */
    private static native void Scharr_1(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            int dx,
            int dy,
            double scale,
            double delta);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Scharr(Mat,Mat,int,int,int,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     * @param scale         optional scale factor for the computed derivative values; by default, no scaling is applied
     *                      (see getDerivKernels for details)
     */
    private static native void Scharr_2(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            int dx,
            int dy,
            double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Scharr(Mat,Mat,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and the same
     *                      number of channels as src
     * @param ddepth        output image depth, see REF: filter_depths "combinations"
     * @param dx            order of the derivative x
     * @param dy            order of the derivative y
     */
    private static native void Scharr_3(long src_nativeObj, long dst_nativeObj, int ddepth, int dx, int dy);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#Laplacian(Mat,Mat,int,int,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same size and the
     *                      same number of channels as src
     * @param ddepth        Desired depth of the destination image, see REF: filter_depths "combinations"
     * @param ksize         Aperture size used to compute the second-derivative filters. See getDerivKernels for
     *                      details. The size must be positive and odd
     * @param scale         Optional scale factor for the computed Laplacian values. By default, no scaling is applied.
     *                      See getDerivKernels for details
     * @param delta         Optional delta value that is added to the results prior to storing them in dst
     * @param borderType    Pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void Laplacian_0(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            int ksize,
            double scale,
            double delta,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Laplacian(Mat,Mat,int,int,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same size and the
     *                      same number of channels as src
     * @param ddepth        Desired depth of the destination image, see REF: filter_depths "combinations"
     * @param ksize         Aperture size used to compute the second-derivative filters. See getDerivKernels for
     *                      details. The size must be positive and odd
     * @param scale         Optional scale factor for the computed Laplacian values. By default, no scaling is applied.
     *                      See getDerivKernels for details
     * @param delta         Optional delta value that is added to the results prior to storing them in dst
     */
    private static native void Laplacian_1(
            long src_nativeObj,
            long dst_nativeObj,
            int ddepth,
            int ksize,
            double scale,
            double delta);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Laplacian(Mat,Mat,int,int,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same size and the
     *                      same number of channels as src
     * @param ddepth        Desired depth of the destination image, see REF: filter_depths "combinations"
     * @param ksize         Aperture size used to compute the second-derivative filters. See getDerivKernels for
     *                      details. The size must be positive and odd
     * @param scale         Optional scale factor for the computed Laplacian values. By default, no scaling is applied.
     *                      See getDerivKernels for details
     */
    private static native void Laplacian_2(long src_nativeObj, long dst_nativeObj, int ddepth, int ksize, double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Laplacian(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same size and the
     *                      same number of channels as src
     * @param ddepth        Desired depth of the destination image, see REF: filter_depths "combinations"
     * @param ksize         Aperture size used to compute the second-derivative filters. See getDerivKernels for
     *                      details. The size must be positive and odd
     */
    private static native void Laplacian_3(long src_nativeObj, long dst_nativeObj, int ddepth, int ksize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Laplacian(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same size and the
     *                      same number of channels as src
     * @param ddepth        Desired depth of the destination image, see REF: filter_depths "combinations"
     */
    private static native void Laplacian_4(long src_nativeObj, long dst_nativeObj, int ddepth);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Canny(Mat,Mat,double,double,int,boolean)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit input image
     * @param edges_nativeObj native matrix or object handle for {@code edges}; output edge map; single channels 8-bit
     *                        image, which has the same size as image
     * @param threshold1      first threshold for the hysteresis procedure
     * @param threshold2      second threshold for the hysteresis procedure
     * @param apertureSize    aperture size for the Sobel operator
     * @param L2gradient      a flag, indicating whether a more accurate L_2 norm =sqrt{(dI/dx)^2 + (dI/dy)^2} should be
     *                        used to calculate the image gradient magnitude ( L2gradient=true ), or whether the default
     *                        L_1 norm =|dI/dx|+|dI/dy| is enough ( L2gradient=false ). For efficiency, when
     *                        L2gradient=true the implementation compares the squared gradient magnitude (dI/dx)^2 +
     *                        (dI/dy)^2 against the squared thresholds instead of computing the square root; the
     *                        resulting edge map is the same as if the L_2 norm were computed directly
     */
    private static native void Canny_0(
            long image_nativeObj,
            long edges_nativeObj,
            double threshold1,
            double threshold2,
            int apertureSize,
            boolean L2gradient);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Canny(Mat,Mat,double,double,int)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit input image
     * @param edges_nativeObj native matrix or object handle for {@code edges}; output edge map; single channels 8-bit
     *                        image, which has the same size as image
     * @param threshold1      first threshold for the hysteresis procedure
     * @param threshold2      second threshold for the hysteresis procedure
     * @param apertureSize    aperture size for the Sobel operator
     */
    private static native void Canny_1(
            long image_nativeObj,
            long edges_nativeObj,
            double threshold1,
            double threshold2,
            int apertureSize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Canny(Mat,Mat,double,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit input image
     * @param edges_nativeObj native matrix or object handle for {@code edges}; output edge map; single channels 8-bit
     *                        image, which has the same size as image
     * @param threshold1      first threshold for the hysteresis procedure
     * @param threshold2      second threshold for the hysteresis procedure
     */
    private static native void Canny_2(
            long image_nativeObj,
            long edges_nativeObj,
            double threshold1,
            double threshold2);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Canny(Mat,Mat,Mat,double,double,boolean)}.
     *
     * @param dx_nativeObj    native matrix or object handle for {@code dx}; 16-bit x derivative of input image
     *                        (CV_16SC1 or CV_16SC3)
     * @param dy_nativeObj    native matrix or object handle for {@code dy}; 16-bit y derivative of input image (same
     *                        type as dx)
     * @param edges_nativeObj native matrix or object handle for {@code edges}; output edge map; single channels 8-bit
     *                        image, which has the same size as image
     * @param threshold1      first threshold for the hysteresis procedure
     * @param threshold2      second threshold for the hysteresis procedure
     * @param L2gradient      a flag, indicating whether a more accurate L_2 norm =sqrt{(dI/dx)^2 + (dI/dy)^2} should be
     *                        used to calculate the image gradient magnitude ( L2gradient=true ), or whether the default
     *                        L_1 norm =|dI/dx|+|dI/dy| is enough ( L2gradient=false ). For efficiency, when
     *                        L2gradient=true the implementation compares the squared gradient magnitude (dI/dx)^2 +
     *                        (dI/dy)^2 against the squared thresholds instead of computing the square root; the
     *                        resulting edge map is the same as if the L_2 norm were computed directly
     */
    private static native void Canny_3(
            long dx_nativeObj,
            long dy_nativeObj,
            long edges_nativeObj,
            double threshold1,
            double threshold2,
            boolean L2gradient);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#Canny(Mat,Mat,Mat,double,double)}.
     *
     * @param dx_nativeObj    native matrix or object handle for {@code dx}; 16-bit x derivative of input image
     *                        (CV_16SC1 or CV_16SC3)
     * @param dy_nativeObj    native matrix or object handle for {@code dy}; 16-bit y derivative of input image (same
     *                        type as dx)
     * @param edges_nativeObj native matrix or object handle for {@code edges}; output edge map; single channels 8-bit
     *                        image, which has the same size as image
     * @param threshold1      first threshold for the hysteresis procedure
     * @param threshold2      second threshold for the hysteresis procedure
     */
    private static native void Canny_4(
            long dx_nativeObj,
            long dy_nativeObj,
            long edges_nativeObj,
            double threshold1,
            double threshold2);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cornerMinEigenVal(Mat,Mat,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input single-channel 8-bit or floating-point
     *                      image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Image to store the minimal eigenvalues. It
     *                      has the type CV_32FC1 and the same size as src
     * @param blockSize     Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @param ksize         Aperture parameter for the Sobel operator
     * @param borderType    Pixel extrapolation method. See BorderTypes. BORDER_WRAP is not supported
     */
    private static native void cornerMinEigenVal_0(
            long src_nativeObj,
            long dst_nativeObj,
            int blockSize,
            int ksize,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cornerMinEigenVal(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input single-channel 8-bit or floating-point
     *                      image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Image to store the minimal eigenvalues. It
     *                      has the type CV_32FC1 and the same size as src
     * @param blockSize     Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @param ksize         Aperture parameter for the Sobel operator
     */
    private static native void cornerMinEigenVal_1(long src_nativeObj, long dst_nativeObj, int blockSize, int ksize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cornerMinEigenVal(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input single-channel 8-bit or floating-point
     *                      image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Image to store the minimal eigenvalues. It
     *                      has the type CV_32FC1 and the same size as src
     * @param blockSize     Neighborhood size (see the details on cornerEigenValsAndVecs )
     */
    private static native void cornerMinEigenVal_2(long src_nativeObj, long dst_nativeObj, int blockSize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cornerHarris(Mat,Mat,int,int,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input single-channel 8-bit or floating-point
     *                      image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Image to store the Harris detector
     *                      responses. It has the type CV_32FC1 and the same size as src
     * @param blockSize     Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @param ksize         Aperture parameter for the Sobel operator
     * @param k             Harris detector free parameter. See the formula above
     * @param borderType    Pixel extrapolation method. See BorderTypes. BORDER_WRAP is not supported
     */
    private static native void cornerHarris_0(
            long src_nativeObj,
            long dst_nativeObj,
            int blockSize,
            int ksize,
            double k,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cornerHarris(Mat,Mat,int,int,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input single-channel 8-bit or floating-point
     *                      image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Image to store the Harris detector
     *                      responses. It has the type CV_32FC1 and the same size as src
     * @param blockSize     Neighborhood size (see the details on cornerEigenValsAndVecs )
     * @param ksize         Aperture parameter for the Sobel operator
     * @param k             Harris detector free parameter. See the formula above
     */
    private static native void cornerHarris_1(
            long src_nativeObj,
            long dst_nativeObj,
            int blockSize,
            int ksize,
            double k);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#cornerEigenValsAndVecs(Mat,Mat,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input single-channel 8-bit or floating-point
     *                      image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Image to store the results. It has the same
     *                      size as src and the type CV_32FC(6)
     * @param blockSize     Neighborhood size (see details below)
     * @param ksize         Aperture parameter for the Sobel operator
     * @param borderType    Pixel extrapolation method. See BorderTypes. BORDER_WRAP is not supported
     */
    private static native void cornerEigenValsAndVecs_0(
            long src_nativeObj,
            long dst_nativeObj,
            int blockSize,
            int ksize,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cornerEigenValsAndVecs(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input single-channel 8-bit or floating-point
     *                      image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Image to store the results. It has the same
     *                      size as src and the type CV_32FC(6)
     * @param blockSize     Neighborhood size (see details below)
     * @param ksize         Aperture parameter for the Sobel operator
     */
    private static native void cornerEigenValsAndVecs_1(
            long src_nativeObj,
            long dst_nativeObj,
            int blockSize,
            int ksize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#preCornerDetect(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source single-channel 8-bit of
     *                      floating-point image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output image that has the type CV_32F and
     *                      the same size as src
     * @param ksize         %Aperture size of the Sobel
     * @param borderType    Pixel extrapolation method. See BorderTypes. BORDER_WRAP is not supported
     */
    private static native void preCornerDetect_0(long src_nativeObj, long dst_nativeObj, int ksize, int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#preCornerDetect(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source single-channel 8-bit of
     *                      floating-point image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output image that has the type CV_32F and
     *                      the same size as src
     * @param ksize         %Aperture size of the Sobel
     */
    private static native void preCornerDetect_1(long src_nativeObj, long dst_nativeObj, int ksize);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#cornerSubPix(Mat,Mat,Size,Size,TermCriteria)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; Input single-channel, 8-bit or float
     *                          image
     * @param corners_nativeObj native matrix or object handle for {@code corners}; Initial coordinates of the input
     *                          corners and refined coordinates provided for output
     * @param winSize_width     {@code winSize.width} component of Half of the side length of the search window. For
     *                          example, if winSize=Size(5,5) , then a (5*2+1) times (5*2+1) = 11 times 11 search window
     *                          is used
     * @param winSize_height    {@code winSize.height} component of Half of the side length of the search window. For
     *                          example, if winSize=Size(5,5) , then a (5*2+1) times (5*2+1) = 11 times 11 search window
     *                          is used
     * @param zeroZone_width    {@code zeroZone.width} component of Half of the size of the dead region in the middle of
     *                          the search zone over which the summation in the formula below is not done. It is used
     *                          some x to avoid possible singularities of the autocorrelation matrix. The value of
     *                          (-1,-1) indicates that there is no such a size
     * @param zeroZone_height   {@code zeroZone.height} component of Half of the size of the dead region in the middle
     *                          of the search zone over which the summation in the formula below is not done. It is used
     *                          some x to avoid possible singularities of the autocorrelation matrix. The value of
     *                          (-1,-1) indicates that there is no such a size
     * @param criteria_type     {@code criteria.type} component of Criteria for termination of the iterative process of
     *                          corner refinement. That is, the process of corner position refinement stops either after
     *                          criteria.maxCount iterations or when the corner position moves by less than
     *                          criteria.epsilon on some iteration
     * @param criteria_maxCount {@code criteria.maxCount} component of Criteria for termination of the iterative process
     *                          of corner refinement. That is, the process of corner position refinement stops either
     *                          after criteria.maxCount iterations or when the corner position moves by less than
     *                          criteria.epsilon on some iteration
     * @param criteria_epsilon  {@code criteria.epsilon} component of Criteria for termination of the iterative process
     *                          of corner refinement. That is, the process of corner position refinement stops either
     *                          after criteria.maxCount iterations or when the corner position moves by less than
     *                          criteria.epsilon on some iteration
     */
    private static native void cornerSubPix_0(
            long image_nativeObj,
            long corners_nativeObj,
            double winSize_width,
            double winSize_height,
            double zeroZone_width,
            double zeroZone_height,
            int criteria_type,
            int criteria_maxCount,
            double criteria_epsilon);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLines(Mat,Mat,double,double,int,double,double,double,double,boolean)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 2 or 3 element vector (rho, theta) or (rho, theta, textrm{votes}), where
     *                        rho is the distance from the coordinate origin (0,0) (top-left corner of the image), theta
     *                        is the line rotation angle in radians ( 0 sim textrm{vertical line}, pi/2 sim
     *                        textrm{horizontal line} ), and textrm{votes} is the value of accumulator
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     * @param srn             For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The
     *                        coarse accumulator distance resolution is rho and the accurate accumulator resolution is
     *                        rho/srn. If both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both
     *                        these parameters should be positive
     * @param stn             For the multi-scale Hough transform, it is a divisor for the distance resolution theta
     * @param min_theta       For standard and multi-scale Hough transform, minimum angle to check for lines. Must fall
     *                        between 0 and max_theta
     * @param max_theta       For standard and multi-scale Hough transform, an upper bound for the angle. Must fall
     *                        between min_theta and CV_PI. The actual maximum angle in the accumulator may be slightly
     *                        less than max_theta, depending on the parameters min_theta and theta
     * @param use_edgeval     True if you want to use weighted Hough transform
     */
    private static native void HoughLines_0(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta,
            double max_theta,
            boolean use_edgeval);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLines(Mat,Mat,double,double,int,double,double,double,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 2 or 3 element vector (rho, theta) or (rho, theta, textrm{votes}), where
     *                        rho is the distance from the coordinate origin (0,0) (top-left corner of the image), theta
     *                        is the line rotation angle in radians ( 0 sim textrm{vertical line}, pi/2 sim
     *                        textrm{horizontal line} ), and textrm{votes} is the value of accumulator
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     * @param srn             For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The
     *                        coarse accumulator distance resolution is rho and the accurate accumulator resolution is
     *                        rho/srn. If both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both
     *                        these parameters should be positive
     * @param stn             For the multi-scale Hough transform, it is a divisor for the distance resolution theta
     * @param min_theta       For standard and multi-scale Hough transform, minimum angle to check for lines. Must fall
     *                        between 0 and max_theta
     * @param max_theta       For standard and multi-scale Hough transform, an upper bound for the angle. Must fall
     *                        between min_theta and CV_PI. The actual maximum angle in the accumulator may be slightly
     *                        less than max_theta, depending on the parameters min_theta and theta
     */
    private static native void HoughLines_1(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta,
            double max_theta);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLines(Mat,Mat,double,double,int,double,double,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 2 or 3 element vector (rho, theta) or (rho, theta, textrm{votes}), where
     *                        rho is the distance from the coordinate origin (0,0) (top-left corner of the image), theta
     *                        is the line rotation angle in radians ( 0 sim textrm{vertical line}, pi/2 sim
     *                        textrm{horizontal line} ), and textrm{votes} is the value of accumulator
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     * @param srn             For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The
     *                        coarse accumulator distance resolution is rho and the accurate accumulator resolution is
     *                        rho/srn. If both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both
     *                        these parameters should be positive
     * @param stn             For the multi-scale Hough transform, it is a divisor for the distance resolution theta
     * @param min_theta       For standard and multi-scale Hough transform, minimum angle to check for lines. Must fall
     *                        between 0 and max_theta
     */
    private static native void HoughLines_2(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLines(Mat,Mat,double,double,int,double,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 2 or 3 element vector (rho, theta) or (rho, theta, textrm{votes}), where
     *                        rho is the distance from the coordinate origin (0,0) (top-left corner of the image), theta
     *                        is the line rotation angle in radians ( 0 sim textrm{vertical line}, pi/2 sim
     *                        textrm{horizontal line} ), and textrm{votes} is the value of accumulator
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     * @param srn             For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The
     *                        coarse accumulator distance resolution is rho and the accurate accumulator resolution is
     *                        rho/srn. If both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both
     *                        these parameters should be positive
     * @param stn             For the multi-scale Hough transform, it is a divisor for the distance resolution theta
     */
    private static native void HoughLines_3(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLines(Mat,Mat,double,double,int,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 2 or 3 element vector (rho, theta) or (rho, theta, textrm{votes}), where
     *                        rho is the distance from the coordinate origin (0,0) (top-left corner of the image), theta
     *                        is the line rotation angle in radians ( 0 sim textrm{vertical line}, pi/2 sim
     *                        textrm{horizontal line} ), and textrm{votes} is the value of accumulator
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     * @param srn             For the multi-scale Hough transform, it is a divisor for the distance resolution rho. The
     *                        coarse accumulator distance resolution is rho and the accurate accumulator resolution is
     *                        rho/srn. If both srn=0 and stn=0, the classical Hough transform is used. Otherwise, both
     *                        these parameters should be positive
     */
    private static native void HoughLines_4(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLines(Mat,Mat,double,double,int)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 2 or 3 element vector (rho, theta) or (rho, theta, textrm{votes}), where
     *                        rho is the distance from the coordinate origin (0,0) (top-left corner of the image), theta
     *                        is the line rotation angle in radians ( 0 sim textrm{vertical line}, pi/2 sim
     *                        textrm{horizontal line} ), and textrm{votes} is the value of accumulator
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     */
    private static native void HoughLines_5(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesP(Mat,Mat,double,double,int,double,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 4-element vector (x_1, y_1, x_2, y_2) , where (x_1,y_1) and (x_2, y_2)
     *                        are the ending points of each detected line segment
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     * @param minLineLength   Minimum line length. Line segments shorter than that are rejected
     * @param maxLineGap      Maximum allowed gap between points on the same line to link them
     */
    private static native void HoughLinesP_0(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double minLineLength,
            double maxLineGap);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesP(Mat,Mat,double,double,int,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 4-element vector (x_1, y_1, x_2, y_2) , where (x_1,y_1) and (x_2, y_2)
     *                        are the ending points of each detected line segment
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     * @param minLineLength   Minimum line length. Line segments shorter than that are rejected
     */
    private static native void HoughLinesP_1(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double minLineLength);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesP(Mat,Mat,double,double,int)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; 8-bit, single-channel binary source
     *                        image. The image may be modified by the function
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of lines. Each line is
     *                        represented by a 4-element vector (x_1, y_1, x_2, y_2) , where (x_1,y_1) and (x_2, y_2)
     *                        are the ending points of each detected line segment
     * @param rho             Distance resolution of the accumulator in pixels
     * @param theta           Angle resolution of the accumulator in radians
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     */
    private static native void HoughLinesP_2(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesPointSet(Mat,Mat,int,int,double,double,double,double,double,double)}.
     *
     * @param point_nativeObj native matrix or object handle for {@code point}; Input vector of points. Each vector must
     *                        be encoded as a Point vector (x,y). Type must be CV_32FC2 or CV_32SC2
     * @param lines_nativeObj native matrix or object handle for {@code lines}; Output vector of found lines. Each
     *                        vector is encoded as a vector&lt;3-component double vector&gt; (votes, rho, theta). The
     *                        larger the value of 'votes', the higher the reliability of the Hough line
     * @param lines_max       Max count of Hough lines
     * @param threshold       %Accumulator threshold parameter. Only those lines are returned that get enough votes (
     *                        &gt;{@code threshold} )
     * @param min_rho         Minimum value for rho for the accumulator (Note: rho can be negative. The absolute value
     *                        |rho| is the distance of a line to the origin.)
     * @param max_rho         Maximum value for rho for the accumulator
     * @param rho_step        Distance resolution of the accumulator
     * @param min_theta       Minimum angle value of the accumulator in radians
     * @param max_theta       Upper bound for the angle value of the accumulator in radians. The actual maximum angle
     *                        may be slightly less than max_theta, depending on the parameters min_theta and theta_step
     * @param theta_step      Angle resolution of the accumulator in radians
     */
    private static native void HoughLinesPointSet_0(
            long point_nativeObj,
            long lines_nativeObj,
            int lines_max,
            int threshold,
            double min_rho,
            double max_rho,
            double rho_step,
            double min_theta,
            double max_theta,
            double theta_step);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCircles(Mat,Mat,int,double,double,double,double,int,int)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; 8-bit, single-channel, grayscale input
     *                          image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; Output vector of found circles. Each
     *                          vector is encoded as 3 or 4 element floating-point vector (x, y, radius) or (x, y,
     *                          radius, votes)
     * @param method            Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and
     *                          HOUGH_GRADIENT_ALT
     * @param dp                Inverse ratio of the accumulator resolution to the image resolution. For example, if
     *                          dp=1 , the accumulator has the same resolution as the input image. If dp=2 , the
     *                          accumulator has half as big width and height. For HOUGH_GRADIENT_ALT the recommended
     *                          value is dp=1.5, unless some small very circles need to be detected
     * @param minDist           Minimum distance between the centers of the detected circles. If the parameter is too
     *                          small, multiple neighbor circles may be falsely detected in addition to a true one. If
     *                          it is too large, some circles may be missed
     * @param param1            First method-specific parameter. In case of HOUGH_GRADIENT and HOUGH_GRADIENT_ALT, it is
     *                          the higher threshold of the two passed to the Canny edge detector (the lower one is
     *                          twice smaller). Note that HOUGH_GRADIENT_ALT uses Scharr algorithm to compute image
     *                          derivatives, so the threshold value should normally be higher, such as 300 or normally
     *                          exposed and contrasty images
     * @param param2            Second method-specific parameter. In case of HOUGH_GRADIENT, it is the accumulator
     *                          threshold for the circle centers at the detection stage. The smaller it is, the more
     *                          false circles may be detected. Circles, corresponding to the larger accumulator values,
     *                          will be returned first. In the case of HOUGH_GRADIENT_ALT algorithm, this is the circle
     *                          "perfectness" measure. The closer it to 1, the better shaped circles algorithm selects.
     *                          In most cases 0.9 should be fine. If you want get better detection of small circles, you
     *                          may decrease it to 0.85, 0.8 or even less. But then also try to limit the search range
     *                          [minRadius, maxRadius] to avoid many false circles
     * @param minRadius         Minimum circle radius
     * @param maxRadius         Maximum circle radius. If &lt;= 0, uses the maximum image dimension. If &lt; 0,
     *                          HOUGH_GRADIENT returns centers without finding the radius. HOUGH_GRADIENT_ALT always
     *                          computes circle radiuses
     */
    private static native void HoughCircles_0(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2,
            int minRadius,
            int maxRadius);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCircles(Mat,Mat,int,double,double,double,double,int)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; 8-bit, single-channel, grayscale input
     *                          image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; Output vector of found circles. Each
     *                          vector is encoded as 3 or 4 element floating-point vector (x, y, radius) or (x, y,
     *                          radius, votes)
     * @param method            Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and
     *                          HOUGH_GRADIENT_ALT
     * @param dp                Inverse ratio of the accumulator resolution to the image resolution. For example, if
     *                          dp=1 , the accumulator has the same resolution as the input image. If dp=2 , the
     *                          accumulator has half as big width and height. For HOUGH_GRADIENT_ALT the recommended
     *                          value is dp=1.5, unless some small very circles need to be detected
     * @param minDist           Minimum distance between the centers of the detected circles. If the parameter is too
     *                          small, multiple neighbor circles may be falsely detected in addition to a true one. If
     *                          it is too large, some circles may be missed
     * @param param1            First method-specific parameter. In case of HOUGH_GRADIENT and HOUGH_GRADIENT_ALT, it is
     *                          the higher threshold of the two passed to the Canny edge detector (the lower one is
     *                          twice smaller). Note that HOUGH_GRADIENT_ALT uses Scharr algorithm to compute image
     *                          derivatives, so the threshold value should normally be higher, such as 300 or normally
     *                          exposed and contrasty images
     * @param param2            Second method-specific parameter. In case of HOUGH_GRADIENT, it is the accumulator
     *                          threshold for the circle centers at the detection stage. The smaller it is, the more
     *                          false circles may be detected. Circles, corresponding to the larger accumulator values,
     *                          will be returned first. In the case of HOUGH_GRADIENT_ALT algorithm, this is the circle
     *                          "perfectness" measure. The closer it to 1, the better shaped circles algorithm selects.
     *                          In most cases 0.9 should be fine. If you want get better detection of small circles, you
     *                          may decrease it to 0.85, 0.8 or even less. But then also try to limit the search range
     *                          [minRadius, maxRadius] to avoid many false circles
     * @param minRadius         Minimum circle radius
     */
    private static native void HoughCircles_1(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2,
            int minRadius);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCircles(Mat,Mat,int,double,double,double,double)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; 8-bit, single-channel, grayscale input
     *                          image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; Output vector of found circles. Each
     *                          vector is encoded as 3 or 4 element floating-point vector (x, y, radius) or (x, y,
     *                          radius, votes)
     * @param method            Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and
     *                          HOUGH_GRADIENT_ALT
     * @param dp                Inverse ratio of the accumulator resolution to the image resolution. For example, if
     *                          dp=1 , the accumulator has the same resolution as the input image. If dp=2 , the
     *                          accumulator has half as big width and height. For HOUGH_GRADIENT_ALT the recommended
     *                          value is dp=1.5, unless some small very circles need to be detected
     * @param minDist           Minimum distance between the centers of the detected circles. If the parameter is too
     *                          small, multiple neighbor circles may be falsely detected in addition to a true one. If
     *                          it is too large, some circles may be missed
     * @param param1            First method-specific parameter. In case of HOUGH_GRADIENT and HOUGH_GRADIENT_ALT, it is
     *                          the higher threshold of the two passed to the Canny edge detector (the lower one is
     *                          twice smaller). Note that HOUGH_GRADIENT_ALT uses Scharr algorithm to compute image
     *                          derivatives, so the threshold value should normally be higher, such as 300 or normally
     *                          exposed and contrasty images
     * @param param2            Second method-specific parameter. In case of HOUGH_GRADIENT, it is the accumulator
     *                          threshold for the circle centers at the detection stage. The smaller it is, the more
     *                          false circles may be detected. Circles, corresponding to the larger accumulator values,
     *                          will be returned first. In the case of HOUGH_GRADIENT_ALT algorithm, this is the circle
     *                          "perfectness" measure. The closer it to 1, the better shaped circles algorithm selects.
     *                          In most cases 0.9 should be fine. If you want get better detection of small circles, you
     *                          may decrease it to 0.85, 0.8 or even less. But then also try to limit the search range
     *                          [minRadius, maxRadius] to avoid many false circles
     */
    private static native void HoughCircles_2(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCircles(Mat,Mat,int,double,double,double)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; 8-bit, single-channel, grayscale input
     *                          image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; Output vector of found circles. Each
     *                          vector is encoded as 3 or 4 element floating-point vector (x, y, radius) or (x, y,
     *                          radius, votes)
     * @param method            Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and
     *                          HOUGH_GRADIENT_ALT
     * @param dp                Inverse ratio of the accumulator resolution to the image resolution. For example, if
     *                          dp=1 , the accumulator has the same resolution as the input image. If dp=2 , the
     *                          accumulator has half as big width and height. For HOUGH_GRADIENT_ALT the recommended
     *                          value is dp=1.5, unless some small very circles need to be detected
     * @param minDist           Minimum distance between the centers of the detected circles. If the parameter is too
     *                          small, multiple neighbor circles may be falsely detected in addition to a true one. If
     *                          it is too large, some circles may be missed
     * @param param1            First method-specific parameter. In case of HOUGH_GRADIENT and HOUGH_GRADIENT_ALT, it is
     *                          the higher threshold of the two passed to the Canny edge detector (the lower one is
     *                          twice smaller). Note that HOUGH_GRADIENT_ALT uses Scharr algorithm to compute image
     *                          derivatives, so the threshold value should normally be higher, such as 300 or normally
     *                          exposed and contrasty images
     */
    private static native void HoughCircles_3(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist,
            double param1);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCircles(Mat,Mat,int,double,double)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; 8-bit, single-channel, grayscale input
     *                          image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; Output vector of found circles. Each
     *                          vector is encoded as 3 or 4 element floating-point vector (x, y, radius) or (x, y,
     *                          radius, votes)
     * @param method            Detection method, see HoughModes. The available methods are HOUGH_GRADIENT and
     *                          HOUGH_GRADIENT_ALT
     * @param dp                Inverse ratio of the accumulator resolution to the image resolution. For example, if
     *                          dp=1 , the accumulator has the same resolution as the input image. If dp=2 , the
     *                          accumulator has half as big width and height. For HOUGH_GRADIENT_ALT the recommended
     *                          value is dp=1.5, unless some small very circles need to be detected
     * @param minDist           Minimum distance between the centers of the detected circles. If the parameter is too
     *                          small, multiple neighbor circles may be falsely detected in addition to a true one. If
     *                          it is too large, some circles may be missed
     */
    private static native void HoughCircles_4(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#erode(Mat,Mat,Mat,Point,int,int,Scalar)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for erosion;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     * @param anchor_x         {@code anchor.x} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param anchor_y         {@code anchor.y} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param iterations       number of times erosion is applied
     * @param borderType       pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @param borderValue_val0 {@code borderValue.val[0]} component of border value in case of a constant border
     * @param borderValue_val1 {@code borderValue.val[1]} component of border value in case of a constant border
     * @param borderValue_val2 {@code borderValue.val[2]} component of border value in case of a constant border
     * @param borderValue_val3 {@code borderValue.val[3]} component of border value in case of a constant border
     */
    private static native void erode_0(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations,
            int borderType,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#erode(Mat,Mat,Mat,Point,int,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for erosion;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     * @param anchor_x         {@code anchor.x} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param anchor_y         {@code anchor.y} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param iterations       number of times erosion is applied
     * @param borderType       pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void erode_1(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#erode(Mat,Mat,Mat,Point,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for erosion;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     * @param anchor_x         {@code anchor.x} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param anchor_y         {@code anchor.y} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param iterations       number of times erosion is applied
     */
    private static native void erode_2(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#erode(Mat,Mat,Mat,Point)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for erosion;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     * @param anchor_x         {@code anchor.x} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param anchor_y         {@code anchor.y} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     */
    private static native void erode_3(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#erode(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for erosion;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     */
    private static native void erode_4(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#dilate(Mat,Mat,Mat,Point,int,int,Scalar)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for dilation;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     * @param anchor_x         {@code anchor.x} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param anchor_y         {@code anchor.y} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param iterations       number of times dilation is applied
     * @param borderType       pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @param borderValue_val0 {@code borderValue.val[0]} component of border value in case of a constant border
     * @param borderValue_val1 {@code borderValue.val[1]} component of border value in case of a constant border
     * @param borderValue_val2 {@code borderValue.val[2]} component of border value in case of a constant border
     * @param borderValue_val3 {@code borderValue.val[3]} component of border value in case of a constant border
     */
    private static native void dilate_0(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations,
            int borderType,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#dilate(Mat,Mat,Mat,Point,int,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for dilation;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     * @param anchor_x         {@code anchor.x} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param anchor_y         {@code anchor.y} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param iterations       number of times dilation is applied
     * @param borderType       pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void dilate_1(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#dilate(Mat,Mat,Mat,Point,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for dilation;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     * @param anchor_x         {@code anchor.x} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param anchor_y         {@code anchor.y} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param iterations       number of times dilation is applied
     */
    private static native void dilate_2(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#dilate(Mat,Mat,Mat,Point)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for dilation;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     * @param anchor_x         {@code anchor.x} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     * @param anchor_y         {@code anchor.y} component of position of the anchor within the element; default value
     *                         (-1, -1) means that the anchor is at the element center
     */
    private static native void dilate_3(
            long src_nativeObj,
            long dst_nativeObj,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#dilate(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image; the number of channels can
     *                         be arbitrary, but the depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image of the same size and type as
     *                         src
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; structuring element used for dilation;
     *                         if , a 3 x 3 rectangular structuring element is used. Kernel can be created using
     *                         getStructuringElement
     */
    private static native void dilate_4(long src_nativeObj, long dst_nativeObj, long kernel_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#morphologyEx(Mat,Mat,int,Mat,Point,int,int,Scalar)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; Source image. The number of channels can
     *                         be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Destination image of the same size and
     *                         type as source image
     * @param op               Type of a morphological operation, see MorphTypes
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; Structuring element. It can be created
     *                         using getStructuringElement
     * @param anchor_x         {@code anchor.x} component of Anchor position with the kernel. Negative values mean that
     *                         the anchor is at the kernel center
     * @param anchor_y         {@code anchor.y} component of Anchor position with the kernel. Negative values mean that
     *                         the anchor is at the kernel center
     * @param iterations       Number of times erosion and dilation are applied
     * @param borderType       Pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     * @param borderValue_val0 {@code borderValue.val[0]} component of Border value in case of a constant border. The
     *                         default value has a special meaning
     * @param borderValue_val1 {@code borderValue.val[1]} component of Border value in case of a constant border. The
     *                         default value has a special meaning
     * @param borderValue_val2 {@code borderValue.val[2]} component of Border value in case of a constant border. The
     *                         default value has a special meaning
     * @param borderValue_val3 {@code borderValue.val[3]} component of Border value in case of a constant border. The
     *                         default value has a special meaning
     */
    private static native void morphologyEx_0(
            long src_nativeObj,
            long dst_nativeObj,
            int op,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations,
            int borderType,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#morphologyEx(Mat,Mat,int,Mat,Point,int,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; Source image. The number of channels can
     *                         be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Destination image of the same size and
     *                         type as source image
     * @param op               Type of a morphological operation, see MorphTypes
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; Structuring element. It can be created
     *                         using getStructuringElement
     * @param anchor_x         {@code anchor.x} component of Anchor position with the kernel. Negative values mean that
     *                         the anchor is at the kernel center
     * @param anchor_y         {@code anchor.y} component of Anchor position with the kernel. Negative values mean that
     *                         the anchor is at the kernel center
     * @param iterations       Number of times erosion and dilation are applied
     * @param borderType       Pixel extrapolation method, see BorderTypes. BORDER_WRAP is not supported
     */
    private static native void morphologyEx_1(
            long src_nativeObj,
            long dst_nativeObj,
            int op,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#morphologyEx(Mat,Mat,int,Mat,Point,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; Source image. The number of channels can
     *                         be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Destination image of the same size and
     *                         type as source image
     * @param op               Type of a morphological operation, see MorphTypes
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; Structuring element. It can be created
     *                         using getStructuringElement
     * @param anchor_x         {@code anchor.x} component of Anchor position with the kernel. Negative values mean that
     *                         the anchor is at the kernel center
     * @param anchor_y         {@code anchor.y} component of Anchor position with the kernel. Negative values mean that
     *                         the anchor is at the kernel center
     * @param iterations       Number of times erosion and dilation are applied
     */
    private static native void morphologyEx_2(
            long src_nativeObj,
            long dst_nativeObj,
            int op,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y,
            int iterations);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#morphologyEx(Mat,Mat,int,Mat,Point)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; Source image. The number of channels can
     *                         be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Destination image of the same size and
     *                         type as source image
     * @param op               Type of a morphological operation, see MorphTypes
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; Structuring element. It can be created
     *                         using getStructuringElement
     * @param anchor_x         {@code anchor.x} component of Anchor position with the kernel. Negative values mean that
     *                         the anchor is at the kernel center
     * @param anchor_y         {@code anchor.y} component of Anchor position with the kernel. Negative values mean that
     *                         the anchor is at the kernel center
     */
    private static native void morphologyEx_3(
            long src_nativeObj,
            long dst_nativeObj,
            int op,
            long kernel_nativeObj,
            double anchor_x,
            double anchor_y);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#morphologyEx(Mat,Mat,int,Mat)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; Source image. The number of channels can
     *                         be arbitrary. The depth should be one of CV_8U, CV_16U, CV_16S, CV_32F or CV_64F
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Destination image of the same size and
     *                         type as source image
     * @param op               Type of a morphological operation, see MorphTypes
     * @param kernel_nativeObj native matrix or object handle for {@code kernel}; Structuring element. It can be created
     *                         using getStructuringElement
     */
    private static native void morphologyEx_4(long src_nativeObj, long dst_nativeObj, int op, long kernel_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#resize(Mat,Mat,Size,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image; it has the size dsize (when it
     *                      is non-zero) or the size computed from src.size(), fx, and fy; the type of dst is the same
     *                      as of src
     * @param dsize_width   {@code dsize.width} component of output image size; if it equals zero (None in Python), it
     *                      is computed as: {@code dsize = Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or
     *                      both fx and fy must be non-zero
     * @param dsize_height  {@code dsize.height} component of output image size; if it equals zero (None in Python), it
     *                      is computed as: {@code dsize = Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or
     *                      both fx and fy must be non-zero
     * @param fx            scale factor along the horizontal axis; when it equals 0, it is computed as {@code
     *     (double)dsize.width/src.cols}
     * @param fy            scale factor along the vertical axis; when it equals 0, it is computed as {@code
     *     (double)dsize.height/src.rows}
     * @param interpolation interpolation method, see InterpolationFlags
     */
    private static native void resize_0(
            long src_nativeObj,
            long dst_nativeObj,
            double dsize_width,
            double dsize_height,
            double fx,
            double fy,
            int interpolation);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#resize(Mat,Mat,Size,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image; it has the size dsize (when it
     *                      is non-zero) or the size computed from src.size(), fx, and fy; the type of dst is the same
     *                      as of src
     * @param dsize_width   {@code dsize.width} component of output image size; if it equals zero (None in Python), it
     *                      is computed as: {@code dsize = Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or
     *                      both fx and fy must be non-zero
     * @param dsize_height  {@code dsize.height} component of output image size; if it equals zero (None in Python), it
     *                      is computed as: {@code dsize = Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or
     *                      both fx and fy must be non-zero
     * @param fx            scale factor along the horizontal axis; when it equals 0, it is computed as {@code
     *     (double)dsize.width/src.cols}
     * @param fy            scale factor along the vertical axis; when it equals 0, it is computed as {@code
     *     (double)dsize.height/src.rows}
     */
    private static native void resize_1(
            long src_nativeObj,
            long dst_nativeObj,
            double dsize_width,
            double dsize_height,
            double fx,
            double fy);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#resize(Mat,Mat,Size,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image; it has the size dsize (when it
     *                      is non-zero) or the size computed from src.size(), fx, and fy; the type of dst is the same
     *                      as of src
     * @param dsize_width   {@code dsize.width} component of output image size; if it equals zero (None in Python), it
     *                      is computed as: {@code dsize = Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or
     *                      both fx and fy must be non-zero
     * @param dsize_height  {@code dsize.height} component of output image size; if it equals zero (None in Python), it
     *                      is computed as: {@code dsize = Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or
     *                      both fx and fy must be non-zero
     * @param fx            scale factor along the horizontal axis; when it equals 0, it is computed as {@code
     *     (double)dsize.width/src.cols}
     */
    private static native void resize_2(
            long src_nativeObj,
            long dst_nativeObj,
            double dsize_width,
            double dsize_height,
            double fx);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#resize(Mat,Mat,Size)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image; it has the size dsize (when it
     *                      is non-zero) or the size computed from src.size(), fx, and fy; the type of dst is the same
     *                      as of src
     * @param dsize_width   {@code dsize.width} component of output image size; if it equals zero (None in Python), it
     *                      is computed as: {@code dsize = Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or
     *                      both fx and fy must be non-zero
     * @param dsize_height  {@code dsize.height} component of output image size; if it equals zero (None in Python), it
     *                      is computed as: {@code dsize = Size(round(fx*src.cols), round(fy*src.rows))} Either dsize or
     *                      both fx and fy must be non-zero
     */
    private static native void resize_3(
            long src_nativeObj,
            long dst_nativeObj,
            double dsize_width,
            double dsize_height);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpAffine(Mat,Mat,Mat,Size,int,int,Scalar,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image that has the size dsize and
     *                         the same type as src
     * @param M_nativeObj      native matrix or object handle for {@code M}; 2 x 3 transformation matrix
     * @param dsize_width      {@code dsize.width} component of size of the output image
     * @param dsize_height     {@code dsize.height} component of size of the output image
     * @param flags            combination of interpolation methods (see InterpolationFlags) and the optional flag
     *                         WARP_INVERSE_MAP that means that M is the inverse transformation ( {@code dst} to
     *                         {@code src} )
     * @param borderMode       pixel extrapolation method (see BorderTypes); when borderMode=BORDER_TRANSPARENT, it
     *                         means that the pixels in the destination image corresponding to the "outliers" in the
     *                         source image are not modified by the function
     * @param borderValue_val0 {@code borderValue.val[0]} component of value used in case of a constant border; by
     *                         default, it is 0
     * @param borderValue_val1 {@code borderValue.val[1]} component of value used in case of a constant border; by
     *                         default, it is 0
     * @param borderValue_val2 {@code borderValue.val[2]} component of value used in case of a constant border; by
     *                         default, it is 0
     * @param borderValue_val3 {@code borderValue.val[3]} component of value used in case of a constant border; by
     *                         default, it is 0
     * @param hint             Implementation modification flags. Set ALGO_HINT_APPROX to use FP16 precision (if
     *                         available) for linear calculation for faster speed. See AlgorithmHint
     */
    private static native void warpAffine_0(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height,
            int flags,
            int borderMode,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3,
            int hint);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpAffine(Mat,Mat,Mat,Size,int,int,Scalar)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image that has the size dsize and
     *                         the same type as src
     * @param M_nativeObj      native matrix or object handle for {@code M}; 2 x 3 transformation matrix
     * @param dsize_width      {@code dsize.width} component of size of the output image
     * @param dsize_height     {@code dsize.height} component of size of the output image
     * @param flags            combination of interpolation methods (see InterpolationFlags) and the optional flag
     *                         WARP_INVERSE_MAP that means that M is the inverse transformation ( {@code dst} to
     *                         {@code src} )
     * @param borderMode       pixel extrapolation method (see BorderTypes); when borderMode=BORDER_TRANSPARENT, it
     *                         means that the pixels in the destination image corresponding to the "outliers" in the
     *                         source image are not modified by the function
     * @param borderValue_val0 {@code borderValue.val[0]} component of value used in case of a constant border; by
     *                         default, it is 0
     * @param borderValue_val1 {@code borderValue.val[1]} component of value used in case of a constant border; by
     *                         default, it is 0
     * @param borderValue_val2 {@code borderValue.val[2]} component of value used in case of a constant border; by
     *                         default, it is 0
     * @param borderValue_val3 {@code borderValue.val[3]} component of value used in case of a constant border; by
     *                         default, it is 0
     */
    private static native void warpAffine_1(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height,
            int flags,
            int borderMode,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpAffine(Mat,Mat,Mat,Size,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image that has the size dsize and the
     *                      same type as src
     * @param M_nativeObj   native matrix or object handle for {@code M}; 2 x 3 transformation matrix
     * @param dsize_width   {@code dsize.width} component of size of the output image
     * @param dsize_height  {@code dsize.height} component of size of the output image
     * @param flags         combination of interpolation methods (see InterpolationFlags) and the optional flag
     *                      WARP_INVERSE_MAP that means that M is the inverse transformation ( {@code dst} to
     *                      {@code src} )
     * @param borderMode    pixel extrapolation method (see BorderTypes); when borderMode=BORDER_TRANSPARENT, it means
     *                      that the pixels in the destination image corresponding to the "outliers" in the source image
     *                      are not modified by the function
     */
    private static native void warpAffine_2(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height,
            int flags,
            int borderMode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpAffine(Mat,Mat,Mat,Size,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image that has the size dsize and the
     *                      same type as src
     * @param M_nativeObj   native matrix or object handle for {@code M}; 2 x 3 transformation matrix
     * @param dsize_width   {@code dsize.width} component of size of the output image
     * @param dsize_height  {@code dsize.height} component of size of the output image
     * @param flags         combination of interpolation methods (see InterpolationFlags) and the optional flag
     *                      WARP_INVERSE_MAP that means that M is the inverse transformation ( {@code dst} to
     *                      {@code src} )
     */
    private static native void warpAffine_3(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height,
            int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpAffine(Mat,Mat,Mat,Size)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image that has the size dsize and the
     *                      same type as src
     * @param M_nativeObj   native matrix or object handle for {@code M}; 2 x 3 transformation matrix
     * @param dsize_width   {@code dsize.width} component of size of the output image
     * @param dsize_height  {@code dsize.height} component of size of the output image
     */
    private static native void warpAffine_4(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpPerspective(Mat,Mat,Mat,Size,int,int,Scalar,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image that has the size dsize and
     *                         the same type as src
     * @param M_nativeObj      native matrix or object handle for {@code M}; 3 x 3 transformation matrix
     * @param dsize_width      {@code dsize.width} component of size of the output image
     * @param dsize_height     {@code dsize.height} component of size of the output image
     * @param flags            combination of interpolation methods (INTER_LINEAR or INTER_NEAREST) and the optional
     *                         flag WARP_INVERSE_MAP, that sets M as the inverse transformation ( {@code dst} to
     *                         {@code src} )
     * @param borderMode       pixel extrapolation method (BORDER_CONSTANT or BORDER_REPLICATE)
     * @param borderValue_val0 {@code borderValue.val[0]} component of value used in case of a constant border; by
     *                         default, it equals 0
     * @param borderValue_val1 {@code borderValue.val[1]} component of value used in case of a constant border; by
     *                         default, it equals 0
     * @param borderValue_val2 {@code borderValue.val[2]} component of value used in case of a constant border; by
     *                         default, it equals 0
     * @param borderValue_val3 {@code borderValue.val[3]} component of value used in case of a constant border; by
     *                         default, it equals 0
     * @param hint             Implementation modification flags. Set ALGO_HINT_APPROX to use FP16 precision (if
     *                         available) for linear calculation for faster speed. See AlgorithmHint
     */
    private static native void warpPerspective_0(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height,
            int flags,
            int borderMode,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3,
            int hint);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpPerspective(Mat,Mat,Mat,Size,int,int,Scalar)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; output image that has the size dsize and
     *                         the same type as src
     * @param M_nativeObj      native matrix or object handle for {@code M}; 3 x 3 transformation matrix
     * @param dsize_width      {@code dsize.width} component of size of the output image
     * @param dsize_height     {@code dsize.height} component of size of the output image
     * @param flags            combination of interpolation methods (INTER_LINEAR or INTER_NEAREST) and the optional
     *                         flag WARP_INVERSE_MAP, that sets M as the inverse transformation ( {@code dst} to
     *                         {@code src} )
     * @param borderMode       pixel extrapolation method (BORDER_CONSTANT or BORDER_REPLICATE)
     * @param borderValue_val0 {@code borderValue.val[0]} component of value used in case of a constant border; by
     *                         default, it equals 0
     * @param borderValue_val1 {@code borderValue.val[1]} component of value used in case of a constant border; by
     *                         default, it equals 0
     * @param borderValue_val2 {@code borderValue.val[2]} component of value used in case of a constant border; by
     *                         default, it equals 0
     * @param borderValue_val3 {@code borderValue.val[3]} component of value used in case of a constant border; by
     *                         default, it equals 0
     */
    private static native void warpPerspective_1(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height,
            int flags,
            int borderMode,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpPerspective(Mat,Mat,Mat,Size,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image that has the size dsize and the
     *                      same type as src
     * @param M_nativeObj   native matrix or object handle for {@code M}; 3 x 3 transformation matrix
     * @param dsize_width   {@code dsize.width} component of size of the output image
     * @param dsize_height  {@code dsize.height} component of size of the output image
     * @param flags         combination of interpolation methods (INTER_LINEAR or INTER_NEAREST) and the optional flag
     *                      WARP_INVERSE_MAP, that sets M as the inverse transformation ( {@code dst} to {@code src} )
     * @param borderMode    pixel extrapolation method (BORDER_CONSTANT or BORDER_REPLICATE)
     */
    private static native void warpPerspective_2(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height,
            int flags,
            int borderMode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpPerspective(Mat,Mat,Mat,Size,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image that has the size dsize and the
     *                      same type as src
     * @param M_nativeObj   native matrix or object handle for {@code M}; 3 x 3 transformation matrix
     * @param dsize_width   {@code dsize.width} component of size of the output image
     * @param dsize_height  {@code dsize.height} component of size of the output image
     * @param flags         combination of interpolation methods (INTER_LINEAR or INTER_NEAREST) and the optional flag
     *                      WARP_INVERSE_MAP, that sets M as the inverse transformation ( {@code dst} to {@code src} )
     */
    private static native void warpPerspective_3(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height,
            int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpPerspective(Mat,Mat,Mat,Size)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image that has the size dsize and the
     *                      same type as src
     * @param M_nativeObj   native matrix or object handle for {@code M}; 3 x 3 transformation matrix
     * @param dsize_width   {@code dsize.width} component of size of the output image
     * @param dsize_height  {@code dsize.height} component of size of the output image
     */
    private static native void warpPerspective_4(
            long src_nativeObj,
            long dst_nativeObj,
            long M_nativeObj,
            double dsize_width,
            double dsize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#remap(Mat,Mat,Mat,Mat,int,int,Scalar,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Destination image. It has the same size
     *                         as map1 and the same type as src
     * @param map1_nativeObj   native matrix or object handle for {@code map1}; The first map of either (x,y) points or
     *                         just x values having the type CV_16SC2 , CV_32FC1, or CV_32FC2. See convertMaps for
     *                         details on converting a floating point representation to fixed-point
     * @param map2_nativeObj   native matrix or object handle for {@code map2}; The second map of y values having the
     *                         type CV_16UC1, CV_32FC1, or none (empty map if map1 is (x,y) points), respectively
     * @param interpolation    Interpolation method (see InterpolationFlags). The methods INTER_AREA INTER_LINEAR_EXACT
     *                         and INTER_NEAREST_EXACT are not supported by this function. The extra flag
     *                         WARP_RELATIVE_MAP can be ORed to the interpolation method (e.g. INTER_LINEAR |
     *                         WARP_RELATIVE_MAP)
     * @param borderMode       Pixel extrapolation method (see BorderTypes). When borderMode=BORDER_TRANSPARENT, it
     *                         means that the pixels in the destination image that corresponds to the "outliers" in the
     *                         source image are not modified by the function
     * @param borderValue_val0 {@code borderValue.val[0]} component of Value used in case of a constant border. By
     *                         default, it is 0
     * @param borderValue_val1 {@code borderValue.val[1]} component of Value used in case of a constant border. By
     *                         default, it is 0
     * @param borderValue_val2 {@code borderValue.val[2]} component of Value used in case of a constant border. By
     *                         default, it is 0
     * @param borderValue_val3 {@code borderValue.val[3]} component of Value used in case of a constant border. By
     *                         default, it is 0
     * @param hint             Implementation modification flags. Set ALGO_HINT_APPROX to use FP16 precision (if
     *                         available) for linear calculation for faster speed. See AlgorithmHint. Note: Due to
     *                         current implementation limitations the size of an input and output images should be less
     *                         than 32767x32767
     */
    private static native void remap_0(
            long src_nativeObj,
            long dst_nativeObj,
            long map1_nativeObj,
            long map2_nativeObj,
            int interpolation,
            int borderMode,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3,
            int hint);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#remap(Mat,Mat,Mat,Mat,int,int,Scalar)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Destination image. It has the same size
     *                         as map1 and the same type as src
     * @param map1_nativeObj   native matrix or object handle for {@code map1}; The first map of either (x,y) points or
     *                         just x values having the type CV_16SC2 , CV_32FC1, or CV_32FC2. See convertMaps for
     *                         details on converting a floating point representation to fixed-point
     * @param map2_nativeObj   native matrix or object handle for {@code map2}; The second map of y values having the
     *                         type CV_16UC1, CV_32FC1, or none (empty map if map1 is (x,y) points), respectively
     * @param interpolation    Interpolation method (see InterpolationFlags). The methods INTER_AREA INTER_LINEAR_EXACT
     *                         and INTER_NEAREST_EXACT are not supported by this function. The extra flag
     *                         WARP_RELATIVE_MAP can be ORed to the interpolation method (e.g. INTER_LINEAR |
     *                         WARP_RELATIVE_MAP)
     * @param borderMode       Pixel extrapolation method (see BorderTypes). When borderMode=BORDER_TRANSPARENT, it
     *                         means that the pixels in the destination image that corresponds to the "outliers" in the
     *                         source image are not modified by the function
     * @param borderValue_val0 {@code borderValue.val[0]} component of Value used in case of a constant border. By
     *                         default, it is 0
     * @param borderValue_val1 {@code borderValue.val[1]} component of Value used in case of a constant border. By
     *                         default, it is 0
     * @param borderValue_val2 {@code borderValue.val[2]} component of Value used in case of a constant border. By
     *                         default, it is 0
     * @param borderValue_val3 {@code borderValue.val[3]} component of Value used in case of a constant border. By
     *                         default, it is 0
     */
    private static native void remap_1(
            long src_nativeObj,
            long dst_nativeObj,
            long map1_nativeObj,
            long map2_nativeObj,
            int interpolation,
            int borderMode,
            double borderValue_val0,
            double borderValue_val1,
            double borderValue_val2,
            double borderValue_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#remap(Mat,Mat,Mat,Mat,int,int)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; Destination image. It has the same size as
     *                       map1 and the same type as src
     * @param map1_nativeObj native matrix or object handle for {@code map1}; The first map of either (x,y) points or
     *                       just x values having the type CV_16SC2 , CV_32FC1, or CV_32FC2. See convertMaps for details
     *                       on converting a floating point representation to fixed-point
     * @param map2_nativeObj native matrix or object handle for {@code map2}; The second map of y values having the type
     *                       CV_16UC1, CV_32FC1, or none (empty map if map1 is (x,y) points), respectively
     * @param interpolation  Interpolation method (see InterpolationFlags). The methods INTER_AREA INTER_LINEAR_EXACT
     *                       and INTER_NEAREST_EXACT are not supported by this function. The extra flag
     *                       WARP_RELATIVE_MAP can be ORed to the interpolation method (e.g. INTER_LINEAR |
     *                       WARP_RELATIVE_MAP)
     * @param borderMode     Pixel extrapolation method (see BorderTypes). When borderMode=BORDER_TRANSPARENT, it means
     *                       that the pixels in the destination image that corresponds to the "outliers" in the source
     *                       image are not modified by the function
     */
    private static native void remap_2(
            long src_nativeObj,
            long dst_nativeObj,
            long map1_nativeObj,
            long map2_nativeObj,
            int interpolation,
            int borderMode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#remap(Mat,Mat,Mat,Mat,int)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; Destination image. It has the same size as
     *                       map1 and the same type as src
     * @param map1_nativeObj native matrix or object handle for {@code map1}; The first map of either (x,y) points or
     *                       just x values having the type CV_16SC2 , CV_32FC1, or CV_32FC2. See convertMaps for details
     *                       on converting a floating point representation to fixed-point
     * @param map2_nativeObj native matrix or object handle for {@code map2}; The second map of y values having the type
     *                       CV_16UC1, CV_32FC1, or none (empty map if map1 is (x,y) points), respectively
     * @param interpolation  Interpolation method (see InterpolationFlags). The methods INTER_AREA INTER_LINEAR_EXACT
     *                       and INTER_NEAREST_EXACT are not supported by this function. The extra flag
     *                       WARP_RELATIVE_MAP can be ORed to the interpolation method (e.g. INTER_LINEAR |
     *                       WARP_RELATIVE_MAP)
     */
    private static native void remap_3(
            long src_nativeObj,
            long dst_nativeObj,
            long map1_nativeObj,
            long map2_nativeObj,
            int interpolation);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#convertMaps(Mat,Mat,Mat,Mat,int,boolean)}.
     *
     * @param map1_nativeObj    native matrix or object handle for {@code map1}; The first input map of type CV_16SC2,
     *                          CV_32FC1, or CV_32FC2
     * @param map2_nativeObj    native matrix or object handle for {@code map2}; The second input map of type CV_16UC1,
     *                          CV_32FC1, or none (empty matrix), respectively
     * @param dstmap1_nativeObj native matrix or object handle for {@code dstmap1}; The first output map that has the
     *                          type dstmap1type and the same size as src
     * @param dstmap2_nativeObj native matrix or object handle for {@code dstmap2}; The second output map
     * @param dstmap1type       Type of the first output map that should be CV_16SC2, CV_32FC1, or CV_32FC2
     * @param nninterpolation   Flag indicating whether the fixed-point maps are used for the nearest-neighbor or for a
     *                          more complex interpolation
     */
    private static native void convertMaps_0(
            long map1_nativeObj,
            long map2_nativeObj,
            long dstmap1_nativeObj,
            long dstmap2_nativeObj,
            int dstmap1type,
            boolean nninterpolation);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#convertMaps(Mat,Mat,Mat,Mat,int)}.
     *
     * @param map1_nativeObj    native matrix or object handle for {@code map1}; The first input map of type CV_16SC2,
     *                          CV_32FC1, or CV_32FC2
     * @param map2_nativeObj    native matrix or object handle for {@code map2}; The second input map of type CV_16UC1,
     *                          CV_32FC1, or none (empty matrix), respectively
     * @param dstmap1_nativeObj native matrix or object handle for {@code dstmap1}; The first output map that has the
     *                          type dstmap1type and the same size as src
     * @param dstmap2_nativeObj native matrix or object handle for {@code dstmap2}; The second output map
     * @param dstmap1type       Type of the first output map that should be CV_16SC2, CV_32FC1, or CV_32FC2
     */
    private static native void convertMaps_1(
            long map1_nativeObj,
            long map2_nativeObj,
            long dstmap1_nativeObj,
            long dstmap2_nativeObj,
            int dstmap1type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#undistort(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj             native matrix or object handle for {@code src}; Input (distorted) image
     * @param dst_nativeObj             native matrix or object handle for {@code dst}; Output (corrected) image that
     *                                  has the same size and type as src
     * @param cameraMatrix_nativeObj    native matrix or object handle for {@code cameraMatrix}; Input camera matrix A =
     *                                  vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj      native matrix or object handle for {@code distCoeffs}; Input vector of
     *                                  distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2,
     *                                  s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                                  NULL/empty, the zero distortion coefficients are assumed
     * @param newCameraMatrix_nativeObj native matrix or object handle for {@code newCameraMatrix}; Camera matrix of the
     *                                  distorted image. By default, it is the same as cameraMatrix but you may
     *                                  additionally scale and shift the result by using a different matrix
     */
    private static native void undistort_0(
            long src_nativeObj,
            long dst_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long newCameraMatrix_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#undistort(Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj          native matrix or object handle for {@code src}; Input (distorted) image
     * @param dst_nativeObj          native matrix or object handle for {@code dst}; Output (corrected) image that has
     *                               the same size and type as src
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input camera matrix A =
     *                               vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2, s_3, s_4[,
     *                               tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is NULL/empty, the
     *                               zero distortion coefficients are assumed
     */
    private static native void undistort_1(
            long src_nativeObj,
            long dst_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#initUndistortRectifyMap(Mat,Mat,Mat,Mat,Size,int,Mat,Mat)}.
     *
     * @param cameraMatrix_nativeObj    native matrix or object handle for {@code cameraMatrix}; Input camera matrix
     *                                  A=vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj      native matrix or object handle for {@code distCoeffs}; Input vector of
     *                                  distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2,
     *                                  s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                                  NULL/empty, the zero distortion coefficients are assumed
     * @param R_nativeObj               native matrix or object handle for {@code R}; Optional rectification
     *                                  transformation in the object space (3x3 matrix). R1 or R2 , computed by
     *                                  stereoRectify can be passed here. If the matrix is empty, the identity
     *                                  transformation is assumed. In initUndistortRectifyMap R assumed to be an
     *                                  identity matrix
     * @param newCameraMatrix_nativeObj native matrix or object handle for {@code newCameraMatrix}; New camera matrix
     *                                  A'=vecthreethree{f_x'}{0}{c_x'}{0}{f_y'}{c_y'}{0}{0}{1}
     * @param size_width                {@code size.width} component of Undistorted image size
     * @param size_height               {@code size.height} component of Undistorted image size
     * @param m1type                    Type of the first output map that can be CV_32FC1, CV_32FC2 or CV_16SC2, see
     *                                  convertMaps
     * @param map1_nativeObj            native matrix or object handle for {@code map1}; The first output map
     * @param map2_nativeObj            native matrix or object handle for {@code map2}; The second output map
     */
    private static native void initUndistortRectifyMap_0(
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long R_nativeObj,
            long newCameraMatrix_nativeObj,
            double size_width,
            double size_height,
            int m1type,
            long map1_nativeObj,
            long map2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#initInverseRectificationMap(Mat,Mat,Mat,Mat,Size,int,Mat,Mat)}.
     *
     * @param cameraMatrix_nativeObj    native matrix or object handle for {@code cameraMatrix}; Input camera matrix
     *                                  A=vecthreethree{f_x}{0}{c_x}{0}{f_y}{c_y}{0}{0}{1}
     * @param distCoeffs_nativeObj      native matrix or object handle for {@code distCoeffs}; Input vector of
     *                                  distortion coefficients (k_1, k_2, p_1, p_2[, k_3[, k_4, k_5, k_6[, s_1, s_2,
     *                                  s_3, s_4[, tau_x, tau_y]]]]) of 4, 5, 8, 12 or 14 elements. If the vector is
     *                                  NULL/empty, the zero distortion coefficients are assumed
     * @param R_nativeObj               native matrix or object handle for {@code R}; Optional rectification
     *                                  transformation in the object space (3x3 matrix). R1 or R2, computed by
     *                                  stereoRectify can be passed here. If the matrix is empty, the identity
     *                                  transformation is assumed
     * @param newCameraMatrix_nativeObj native matrix or object handle for {@code newCameraMatrix}; New camera matrix
     *                                  A'=vecthreethree{f_x'}{0}{c_x'}{0}{f_y'}{c_y'}{0}{0}{1}
     * @param size_width                {@code size.width} component of Distorted image size
     * @param size_height               {@code size.height} component of Distorted image size
     * @param m1type                    Type of the first output map. Can be CV_32FC1, CV_32FC2 or CV_16SC2, see
     *                                  convertMaps
     * @param map1_nativeObj            native matrix or object handle for {@code map1}; The first output map for remap
     * @param map2_nativeObj            native matrix or object handle for {@code map2}; The second output map for remap
     */
    private static native void initInverseRectificationMap_0(
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long R_nativeObj,
            long newCameraMatrix_nativeObj,
            double size_width,
            double size_height,
            int m1type,
            long map1_nativeObj,
            long map2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#fisheye_initUndistortRectifyMap(Mat,Mat,Mat,Mat,Size,int,Mat,Mat)}.
     *
     * @param K_nativeObj    native matrix or object handle for {@code K}; Camera intrinsic matrix cameramatrix{K}
     * @param D_nativeObj    native matrix or object handle for {@code D}; Input vector of distortion coefficients
     *                       distcoeffsfisheye
     * @param R_nativeObj    native matrix or object handle for {@code R}; Rectification transformation in the object
     *                       space: 3x3 1-channel, or vector: 3x1/1x3 1-channel or 1x1 3-channel
     * @param P_nativeObj    native matrix or object handle for {@code P}; New camera intrinsic matrix (3x3) or new
     *                       projection matrix (3x4)
     * @param size_width     {@code size.width} component of Undistorted image size
     * @param size_height    {@code size.height} component of Undistorted image size
     * @param m1type         Type of the first output map that can be CV_32FC1 or CV_16SC2 . See convertMaps() for
     *                       details
     * @param map1_nativeObj native matrix or object handle for {@code map1}; The first output map
     * @param map2_nativeObj native matrix or object handle for {@code map2}; The second output map
     */
    private static native void fisheye_initUndistortRectifyMap_0(
            long K_nativeObj,
            long D_nativeObj,
            long R_nativeObj,
            long P_nativeObj,
            double size_width,
            double size_height,
            int m1type,
            long map1_nativeObj,
            long map2_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#fisheye_undistortImage(Mat,Mat,Mat,Mat,Mat,Size)}.
     *
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; image with fisheye lens
     *                              distortion
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; Output image with
     *                              compensated fisheye lens distortion
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K}
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of distortion
     *                              coefficients distcoeffsfisheye
     * @param Knew_nativeObj        native matrix or object handle for {@code Knew}; Camera intrinsic matrix of the
     *                              distorted image. By default, it is the identity matrix but you may additionally
     *                              scale and shift the result by using a different matrix
     * @param new_size_width        {@code new_size.width} component of the new size The function transforms an image to
     *                              compensate radial and tangential lens distortion. The function is simply a
     *                              combination of fisheye::initUndistortRectifyMap (with unity R ) and remap (with
     *                              bilinear interpolation). See the former function for details of the transformation
     *                              being performed. See below the results of undistortImage. a result of undistort of
     *                              perspective camera model (all possible coefficients (k_1, k_2, k_3, k_4, k_5, k_6)
     *                              of distortion were optimized under calibration) b result of fisheye::undistortImage
     *                              of fisheye camera model (all possible coefficients (k_1, k_2, k_3, k_4) of fisheye
     *                              distortion were optimized under calibration) c original image was captured with
     *                              fisheye lens Pictures a) and b) almost the same. But if we consider points of image
     *                              located far from the center of image, we can notice that on image a) these points
     *                              are distorted
     * @param new_size_height       {@code new_size.height} component of the new size The function transforms an image
     *                              to compensate radial and tangential lens distortion. The function is simply a
     *                              combination of fisheye::initUndistortRectifyMap (with unity R ) and remap (with
     *                              bilinear interpolation). See the former function for details of the transformation
     *                              being performed. See below the results of undistortImage. a result of undistort of
     *                              perspective camera model (all possible coefficients (k_1, k_2, k_3, k_4, k_5, k_6)
     *                              of distortion were optimized under calibration) b result of fisheye::undistortImage
     *                              of fisheye camera model (all possible coefficients (k_1, k_2, k_3, k_4) of fisheye
     *                              distortion were optimized under calibration) c original image was captured with
     *                              fisheye lens Pictures a) and b) almost the same. But if we consider points of image
     *                              located far from the center of image, we can notice that on image a) these points
     *                              are distorted
     */
    private static native void fisheye_undistortImage_0(
            long distorted_nativeObj,
            long undistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            long Knew_nativeObj,
            double new_size_width,
            double new_size_height);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#fisheye_undistortImage(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; image with fisheye lens
     *                              distortion
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; Output image with
     *                              compensated fisheye lens distortion
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K}
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of distortion
     *                              coefficients distcoeffsfisheye
     * @param Knew_nativeObj        native matrix or object handle for {@code Knew}; Camera intrinsic matrix of the
     *                              distorted image. By default, it is the identity matrix but you may additionally
     *                              scale and shift the result by using a different matrix
     */
    private static native void fisheye_undistortImage_1(
            long distorted_nativeObj,
            long undistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj,
            long Knew_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#fisheye_undistortImage(Mat,Mat,Mat,Mat)}.
     *
     * @param distorted_nativeObj   native matrix or object handle for {@code distorted}; image with fisheye lens
     *                              distortion
     * @param undistorted_nativeObj native matrix or object handle for {@code undistorted}; Output image with
     *                              compensated fisheye lens distortion
     * @param K_nativeObj           native matrix or object handle for {@code K}; Camera intrinsic matrix
     *                              cameramatrix{K}
     * @param D_nativeObj           native matrix or object handle for {@code D}; Input vector of distortion
     *                              coefficients distcoeffsfisheye
     */
    private static native void fisheye_undistortImage_2(
            long distorted_nativeObj,
            long undistorted_nativeObj,
            long K_nativeObj,
            long D_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getRectSubPix(Mat,Size,Point,Mat,int)}.
     *
     * @param image_nativeObj  native matrix or object handle for {@code image}; Source image
     * @param patchSize_width  {@code patchSize.width} component of Size of the extracted patch
     * @param patchSize_height {@code patchSize.height} component of Size of the extracted patch
     * @param center_x         {@code center.x} component of Floating point coordinates of the center of the extracted
     *                         rectangle within the source image. The center must be inside the image
     * @param center_y         {@code center.y} component of Floating point coordinates of the center of the extracted
     *                         rectangle within the source image. The center must be inside the image
     * @param patch_nativeObj  native matrix or object handle for {@code patch}; Extracted patch that has the size
     *                         patchSize and the same number of channels as src
     * @param patchType        Depth of the extracted pixels. By default, they have the same depth as src
     */
    private static native void getRectSubPix_0(
            long image_nativeObj,
            double patchSize_width,
            double patchSize_height,
            double center_x,
            double center_y,
            long patch_nativeObj,
            int patchType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getRectSubPix(Mat,Size,Point,Mat)}.
     *
     * @param image_nativeObj  native matrix or object handle for {@code image}; Source image
     * @param patchSize_width  {@code patchSize.width} component of Size of the extracted patch
     * @param patchSize_height {@code patchSize.height} component of Size of the extracted patch
     * @param center_x         {@code center.x} component of Floating point coordinates of the center of the extracted
     *                         rectangle within the source image. The center must be inside the image
     * @param center_y         {@code center.y} component of Floating point coordinates of the center of the extracted
     *                         rectangle within the source image. The center must be inside the image
     * @param patch_nativeObj  native matrix or object handle for {@code patch}; Extracted patch that has the size
     *                         patchSize and the same number of channels as src
     */
    private static native void getRectSubPix_1(
            long image_nativeObj,
            double patchSize_width,
            double patchSize_height,
            double center_x,
            double center_y,
            long patch_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#warpPolar(Mat,Mat,Size,Point,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image. It will have same type as
     *                      src
     * @param dsize_width   {@code dsize.width} component of The destination image size (see description for valid
     *                      options)
     * @param dsize_height  {@code dsize.height} component of The destination image size (see description for valid
     *                      options)
     * @param center_x      {@code center.x} component of The transformation center
     * @param center_y      {@code center.y} component of The transformation center
     * @param maxRadius     The radius of the bounding circle to transform. It determines the inverse magnitude scale
     *                      parameter too
     * @param flags         A combination of interpolation methods, InterpolationFlags + WarpPolarMode. Add
     *                      WARP_POLAR_LINEAR to select linear polar mapping (default) Add WARP_POLAR_LOG to select
     *                      semilog polar mapping Add WARP_INVERSE_MAP for reverse mapping. Note: The function can not
     *                      operate in-place. To calculate magnitude and angle in degrees cartToPolar is used internally
     *                      thus angles are measured from 0 to 360 with accuracy about 0.3 degrees. This function uses
     *                      remap. Due to current implementation limitations the size of an input and output images
     *                      should be less than 32767x32767
     */
    private static native void warpPolar_0(
            long src_nativeObj,
            long dst_nativeObj,
            double dsize_width,
            double dsize_height,
            double center_x,
            double center_y,
            double maxRadius,
            int flags);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#integral3(Mat,Mat,Mat,Mat,int,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image as W times H, 8-bit or
     *                         floating-point (32f or 64f)
     * @param sum_nativeObj    native matrix or object handle for {@code sum}; integral image as (W+1) x (H+1) , 32-bit
     *                         integer or floating-point (32f or 64f)
     * @param sqsum_nativeObj  native matrix or object handle for {@code sqsum}; integral image for squared pixel
     *                         values; it is (W+1) x (H+1), double-precision floating-point (64f) array
     * @param tilted_nativeObj native matrix or object handle for {@code tilted}; integral for the image rotated by 45
     *                         degrees; it is (W+1) x (H+1) array with the same data type as sum
     * @param sdepth           desired depth of the integral and the tilted integral images, CV_32S, CV_32F, or CV_64F
     * @param sqdepth          desired depth of the integral image of squared pixel values, CV_32F or CV_64F
     */
    private static native void integral3_0(
            long src_nativeObj,
            long sum_nativeObj,
            long sqsum_nativeObj,
            long tilted_nativeObj,
            int sdepth,
            int sqdepth);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#integral3(Mat,Mat,Mat,Mat,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image as W times H, 8-bit or
     *                         floating-point (32f or 64f)
     * @param sum_nativeObj    native matrix or object handle for {@code sum}; integral image as (W+1) x (H+1) , 32-bit
     *                         integer or floating-point (32f or 64f)
     * @param sqsum_nativeObj  native matrix or object handle for {@code sqsum}; integral image for squared pixel
     *                         values; it is (W+1) x (H+1), double-precision floating-point (64f) array
     * @param tilted_nativeObj native matrix or object handle for {@code tilted}; integral for the image rotated by 45
     *                         degrees; it is (W+1) x (H+1) array with the same data type as sum
     * @param sdepth           desired depth of the integral and the tilted integral images, CV_32S, CV_32F, or CV_64F
     */
    private static native void integral3_1(
            long src_nativeObj,
            long sum_nativeObj,
            long sqsum_nativeObj,
            long tilted_nativeObj,
            int sdepth);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#integral3(Mat,Mat,Mat,Mat)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; input image as W times H, 8-bit or
     *                         floating-point (32f or 64f)
     * @param sum_nativeObj    native matrix or object handle for {@code sum}; integral image as (W+1) x (H+1) , 32-bit
     *                         integer or floating-point (32f or 64f)
     * @param sqsum_nativeObj  native matrix or object handle for {@code sqsum}; integral image for squared pixel
     *                         values; it is (W+1) x (H+1), double-precision floating-point (64f) array
     * @param tilted_nativeObj native matrix or object handle for {@code tilted}; integral for the image rotated by 45
     *                         degrees; it is (W+1) x (H+1) array with the same data type as sum
     */
    private static native void integral3_2(
            long src_nativeObj,
            long sum_nativeObj,
            long sqsum_nativeObj,
            long tilted_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#integral(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; source matrix
     * @param sum_nativeObj native matrix or object handle for {@code sum}; output integral image of cumulative pixel
     *                      sums
     * @param sdepth        element depth of the integral sum image
     */
    private static native void integral_0(long src_nativeObj, long sum_nativeObj, int sdepth);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#integral(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; source matrix
     * @param sum_nativeObj native matrix or object handle for {@code sum}; output integral image of cumulative pixel
     *                      sums
     */
    private static native void integral_1(long src_nativeObj, long sum_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#integral2(Mat,Mat,Mat,int,int)}.
     *
     * @param src_nativeObj   native matrix or object handle for {@code src}; source matrix
     * @param sum_nativeObj   native matrix or object handle for {@code sum}; output integral image of cumulative pixel
     *                        sums
     * @param sqsum_nativeObj native matrix or object handle for {@code sqsum}; output integral image of cumulative
     *                        squared pixel sums
     * @param sdepth          element depth of the integral sum image
     * @param sqdepth         element depth of the squared-sum image
     */
    private static native void integral2_0(
            long src_nativeObj,
            long sum_nativeObj,
            long sqsum_nativeObj,
            int sdepth,
            int sqdepth);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#integral2(Mat,Mat,Mat,int)}.
     *
     * @param src_nativeObj   native matrix or object handle for {@code src}; source matrix
     * @param sum_nativeObj   native matrix or object handle for {@code sum}; output integral image of cumulative pixel
     *                        sums
     * @param sqsum_nativeObj native matrix or object handle for {@code sqsum}; output integral image of cumulative
     *                        squared pixel sums
     * @param sdepth          element depth of the integral sum image
     */
    private static native void integral2_1(long src_nativeObj, long sum_nativeObj, long sqsum_nativeObj, int sdepth);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#integral2(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj   native matrix or object handle for {@code src}; source matrix
     * @param sum_nativeObj   native matrix or object handle for {@code sum}; output integral image of cumulative pixel
     *                        sums
     * @param sqsum_nativeObj native matrix or object handle for {@code sqsum}; output integral image of cumulative
     *                        squared pixel sums
     */
    private static native void integral2_2(long src_nativeObj, long sum_nativeObj, long sqsum_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#accumulate(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; Input image of type CV_8UC(n), CV_16UC(n),
     *                       CV_32FC(n) or CV_64FC(n), where n is a positive integer
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; %Accumulator image with the same number of
     *                       channels as input image, and a depth of CV_32F or CV_64F
     * @param mask_nativeObj native matrix or object handle for {@code mask}; Optional operation mask
     */
    private static native void accumulate_0(long src_nativeObj, long dst_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#accumulate(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input image of type CV_8UC(n), CV_16UC(n),
     *                      CV_32FC(n) or CV_64FC(n), where n is a positive integer
     * @param dst_nativeObj native matrix or object handle for {@code dst}; %Accumulator image with the same number of
     *                      channels as input image, and a depth of CV_32F or CV_64F
     */
    private static native void accumulate_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#accumulateSquare(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; Input image as 1- or 3-channel, 8-bit or
     *                       32-bit floating point
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; %Accumulator image with the same number of
     *                       channels as input image, 32-bit or 64-bit floating-point
     * @param mask_nativeObj native matrix or object handle for {@code mask}; Optional operation mask
     */
    private static native void accumulateSquare_0(long src_nativeObj, long dst_nativeObj, long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#accumulateSquare(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input image as 1- or 3-channel, 8-bit or
     *                      32-bit floating point
     * @param dst_nativeObj native matrix or object handle for {@code dst}; %Accumulator image with the same number of
     *                      channels as input image, 32-bit or 64-bit floating-point
     */
    private static native void accumulateSquare_1(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#accumulateProduct(Mat,Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; First input image, 1- or 3-channel, 8-bit
     *                       or 32-bit floating point
     * @param src2_nativeObj native matrix or object handle for {@code src2}; Second input image of the same type and
     *                       the same size as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; %Accumulator image with the same number of
     *                       channels as input images, 32-bit or 64-bit floating-point
     * @param mask_nativeObj native matrix or object handle for {@code mask}; Optional operation mask
     */
    private static native void accumulateProduct_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#accumulateProduct(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; First input image, 1- or 3-channel, 8-bit
     *                       or 32-bit floating point
     * @param src2_nativeObj native matrix or object handle for {@code src2}; Second input image of the same type and
     *                       the same size as src1
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; %Accumulator image with the same number of
     *                       channels as input images, 32-bit or 64-bit floating-point
     */
    private static native void accumulateProduct_1(long src1_nativeObj, long src2_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#accumulateWeighted(Mat,Mat,double,Mat)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; Input image as 1- or 3-channel, 8-bit or
     *                       32-bit floating point
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; %Accumulator image with the same number of
     *                       channels as input image, 32-bit or 64-bit floating-point
     * @param alpha          Weight of the input image
     * @param mask_nativeObj native matrix or object handle for {@code mask}; Optional operation mask
     */
    private static native void accumulateWeighted_0(
            long src_nativeObj,
            long dst_nativeObj,
            double alpha,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#accumulateWeighted(Mat,Mat,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Input image as 1- or 3-channel, 8-bit or
     *                      32-bit floating point
     * @param dst_nativeObj native matrix or object handle for {@code dst}; %Accumulator image with the same number of
     *                      channels as input image, 32-bit or 64-bit floating-point
     * @param alpha         Weight of the input image
     */
    private static native void accumulateWeighted_1(long src_nativeObj, long dst_nativeObj, double alpha);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#phaseCorrelate(Mat,Mat,Mat,double[])}.
     *
     * @param src1_nativeObj   native matrix or object handle for {@code src1}; Source floating point array (CV_32FC1 or
     *                         CV_64FC1)
     * @param src2_nativeObj   native matrix or object handle for {@code src2}; Source floating point array (CV_32FC1 or
     *                         CV_64FC1)
     * @param window_nativeObj native matrix or object handle for {@code window}; Floating point array with windowing
     *                         coefficients to reduce edge effects (optional)
     * @param response_out     output array receiving {@code response} from the native call
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] phaseCorrelate_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long window_nativeObj,
            double[] response_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#phaseCorrelate(Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj   native matrix or object handle for {@code src1}; Source floating point array (CV_32FC1 or
     *                         CV_64FC1)
     * @param src2_nativeObj   native matrix or object handle for {@code src2}; Source floating point array (CV_32FC1 or
     *                         CV_64FC1)
     * @param window_nativeObj native matrix or object handle for {@code window}; Floating point array with windowing
     *                         coefficients to reduce edge effects (optional)
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] phaseCorrelate_1(long src1_nativeObj, long src2_nativeObj, long window_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#phaseCorrelate(Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; Source floating point array (CV_32FC1 or
     *                       CV_64FC1)
     * @param src2_nativeObj native matrix or object handle for {@code src2}; Source floating point array (CV_32FC1 or
     *                       CV_64FC1)
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] phaseCorrelate_2(long src1_nativeObj, long src2_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#phaseCorrelateIterative(Mat,Mat,int,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; Source floating point array (CV_32FC1 or
     *                       CV_64FC1)
     * @param src2_nativeObj native matrix or object handle for {@code src2}; Source floating point array (CV_32FC1 or
     *                       CV_64FC1)
     * @param L2size         The size of the correlation neighborhood used by the iterative shift refinement algorithm
     * @param maxIters       The maximum number of iterations the iterative refinement algorithm will run
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] phaseCorrelateIterative_0(
            long src1_nativeObj,
            long src2_nativeObj,
            int L2size,
            int maxIters);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#phaseCorrelateIterative(Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; Source floating point array (CV_32FC1 or
     *                       CV_64FC1)
     * @param src2_nativeObj native matrix or object handle for {@code src2}; Source floating point array (CV_32FC1 or
     *                       CV_64FC1)
     * @param L2size         The size of the correlation neighborhood used by the iterative shift refinement algorithm
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] phaseCorrelateIterative_1(long src1_nativeObj, long src2_nativeObj, int L2size);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#phaseCorrelateIterative(Mat,Mat)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; Source floating point array (CV_32FC1 or
     *                       CV_64FC1)
     * @param src2_nativeObj native matrix or object handle for {@code src2}; Source floating point array (CV_32FC1 or
     *                       CV_64FC1)
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] phaseCorrelateIterative_2(long src1_nativeObj, long src2_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createHanningWindow(Mat,Size,int)}.
     *
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; Destination array to place Hann
     *                       coefficients in
     * @param winSize_width  {@code winSize.width} component of The window size specifications (both width and height
     *                       must be &gt; 1)
     * @param winSize_height {@code winSize.height} component of The window size specifications (both width and height
     *                       must be &gt; 1)
     * @param type           Created array type
     */
    private static native void createHanningWindow_0(
            long dst_nativeObj,
            double winSize_width,
            double winSize_height,
            int type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#threshold(Mat,Mat,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input array (multiple-channel, CV_8U,
     *                      CV_16S, CV_16U, CV_32F or CV_64F)
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output array of the same size and type and
     *                      the same number of channels as src
     * @param thresh        threshold value
     * @param maxval        maximum value to use with the THRESH_BINARY and THRESH_BINARY_INV thresholding types
     * @param type          thresholding type (see ThresholdTypes)
     * @return the computed threshold value if Otsu's or Triangle methods used
     */
    private static native double threshold_0(
            long src_nativeObj,
            long dst_nativeObj,
            double thresh,
            double maxval,
            int type);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#thresholdWithMask(Mat,Mat,Mat,double,double,int)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; input array (multiple-channel, 8-bit or
     *                       32-bit floating point)
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output array of the same size and type and
     *                       the same number of channels as src
     * @param mask_nativeObj native matrix or object handle for {@code mask}; optional mask (same size as src, 8-bit)
     * @param thresh         threshold value
     * @param maxval         maximum value to use with the THRESH_BINARY and THRESH_BINARY_INV thresholding types
     * @param type           thresholding type (see ThresholdTypes)
     * @return the computed threshold value if Otsu's or Triangle methods used
     */
    private static native double thresholdWithMask_0(
            long src_nativeObj,
            long dst_nativeObj,
            long mask_nativeObj,
            double thresh,
            double maxval,
            int type);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#adaptiveThreshold(Mat,Mat,double,int,int,int,double)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; Source 8-bit single-channel image
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; Destination image of the same size and the
     *                       same type as src
     * @param maxValue       Non-zero value assigned to the pixels for which the condition is satisfied
     * @param adaptiveMethod Adaptive thresholding algorithm to use, see AdaptiveThresholdTypes. The BORDER_REPLICATE |
     *                       BORDER_ISOLATED is used to process boundaries
     * @param thresholdType  Thresholding type that must be either THRESH_BINARY or THRESH_BINARY_INV, see
     *                       ThresholdTypes
     * @param blockSize      Size of a pixel neighborhood that is used to calculate a threshold value for the pixel: 3,
     *                       5, 7, and so on
     * @param C              Constant subtracted from the mean or weighted mean (see the details below). Normally, it is
     *                       positive but may be zero or negative as well
     */
    private static native void adaptiveThreshold_0(
            long src_nativeObj,
            long dst_nativeObj,
            double maxValue,
            int adaptiveMethod,
            int thresholdType,
            int blockSize,
            double C);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrDown(Mat,Mat,Size,int)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output image; it has the specified size and
     *                       the same type as src
     * @param dstsize_width  {@code dstsize.width} component of size of the output image
     * @param dstsize_height {@code dstsize.height} component of size of the output image
     * @param borderType     Pixel extrapolation method, see BorderTypes (BORDER_CONSTANT isn't supported)
     */
    private static native void pyrDown_0(
            long src_nativeObj,
            long dst_nativeObj,
            double dstsize_width,
            double dstsize_height,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrDown(Mat,Mat,Size)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output image; it has the specified size and
     *                       the same type as src
     * @param dstsize_width  {@code dstsize.width} component of size of the output image
     * @param dstsize_height {@code dstsize.height} component of size of the output image
     */
    private static native void pyrDown_1(
            long src_nativeObj,
            long dst_nativeObj,
            double dstsize_width,
            double dstsize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrDown(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image; it has the specified size and
     *                      the same type as src
     */
    private static native void pyrDown_2(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrUp(Mat,Mat,Size,int)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output image. It has the specified size and
     *                       the same type as src
     * @param dstsize_width  {@code dstsize.width} component of size of the output image
     * @param dstsize_height {@code dstsize.height} component of size of the output image
     * @param borderType     Pixel extrapolation method, see BorderTypes (only BORDER_DEFAULT is supported)
     */
    private static native void pyrUp_0(
            long src_nativeObj,
            long dst_nativeObj,
            double dstsize_width,
            double dstsize_height,
            int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrUp(Mat,Mat,Size)}.
     *
     * @param src_nativeObj  native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output image. It has the specified size and
     *                       the same type as src
     * @param dstsize_width  {@code dstsize.width} component of size of the output image
     * @param dstsize_height {@code dstsize.height} component of size of the output image
     */
    private static native void pyrUp_1(
            long src_nativeObj,
            long dst_nativeObj,
            double dstsize_width,
            double dstsize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrUp(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image. It has the specified size and
     *                      the same type as src
     */
    private static native void pyrUp_2(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#calcHist(List<Mat>,MatOfInt,Mat,Mat,MatOfInt,MatOfFloat,boolean)}.
     *
     * @param images_mat_nativeObj   native matrix or object handle for {@code images_mat}; source image matrices used
     *                               for histogram calculation or back-projection
     * @param channels_mat_nativeObj native matrix or object handle for {@code channels_mat}; number of channels per
     *                               matrix element
     * @param mask_nativeObj         native matrix or object handle for {@code mask}; 8-bit single-channel
     *                               pixel-selection mask; an empty mask includes every pixel
     * @param hist_nativeObj         native matrix or object handle for {@code hist}; histogram bin values used for
     *                               back-projection
     * @param histSize_mat_nativeObj native matrix or object handle for {@code histSize_mat}; number of histogram bins
     *                               along each dimension
     * @param ranges_mat_nativeObj   native matrix or object handle for {@code ranges_mat}; half-open range to select
     *                               along each dimension
     * @param accumulate             whether accumulate is enabled
     */
    private static native void calcHist_0(
            long images_mat_nativeObj,
            long channels_mat_nativeObj,
            long mask_nativeObj,
            long hist_nativeObj,
            long histSize_mat_nativeObj,
            long ranges_mat_nativeObj,
            boolean accumulate);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#calcHist(List<Mat>,MatOfInt,Mat,Mat,MatOfInt,MatOfFloat)}.
     *
     * @param images_mat_nativeObj   native matrix or object handle for {@code images_mat}; source image matrices used
     *                               for histogram calculation or back-projection
     * @param channels_mat_nativeObj native matrix or object handle for {@code channels_mat}; number of channels per
     *                               matrix element
     * @param mask_nativeObj         native matrix or object handle for {@code mask}; 8-bit single-channel
     *                               pixel-selection mask; an empty mask includes every pixel
     * @param hist_nativeObj         native matrix or object handle for {@code hist}; histogram bin values used for
     *                               back-projection
     * @param histSize_mat_nativeObj native matrix or object handle for {@code histSize_mat}; number of histogram bins
     *                               along each dimension
     * @param ranges_mat_nativeObj   native matrix or object handle for {@code ranges_mat}; half-open range to select
     *                               along each dimension
     */
    private static native void calcHist_1(
            long images_mat_nativeObj,
            long channels_mat_nativeObj,
            long mask_nativeObj,
            long hist_nativeObj,
            long histSize_mat_nativeObj,
            long ranges_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#calcBackProject(List<Mat>,MatOfInt,Mat,Mat,MatOfFloat,double)}.
     *
     * @param images_mat_nativeObj   native matrix or object handle for {@code images_mat}; source image matrices used
     *                               for histogram calculation or back-projection
     * @param channels_mat_nativeObj native matrix or object handle for {@code channels_mat}; number of channels per
     *                               matrix element
     * @param hist_nativeObj         native matrix or object handle for {@code hist}; histogram bin values used for
     *                               back-projection
     * @param dst_nativeObj          native matrix or object handle for {@code dst}; destination matrix that receives
     *                               the result
     * @param ranges_mat_nativeObj   native matrix or object handle for {@code ranges_mat}; half-open range to select
     *                               along each dimension
     * @param scale                  multiplicative scale applied by the operation
     */
    private static native void calcBackProject_0(
            long images_mat_nativeObj,
            long channels_mat_nativeObj,
            long hist_nativeObj,
            long dst_nativeObj,
            long ranges_mat_nativeObj,
            double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#compareHist(Mat,Mat,int)}.
     *
     * @param H1_nativeObj native matrix or object handle for {@code H1}; First compared histogram
     * @param H2_nativeObj native matrix or object handle for {@code H2}; Second compared histogram of the same size as
     *                     H1
     * @param method       Comparison method, see HistCompMethods
     * @return the histogram comparison score for the selected metric
     */
    private static native double compareHist_0(long H1_nativeObj, long H2_nativeObj, int method);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#equalizeHist(Mat,Mat)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; Source 8-bit single channel image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image of the same size and type
     *                      as src
     */
    private static native void equalizeHist_0(long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createCLAHE(double,Size)}.
     *
     * @param clipLimit           Threshold for contrast limiting
     * @param tileGridSize_width  {@code tileGridSize.width} component of Size of grid for histogram equalization. Input
     *                            image will be divided into equally sized rectangular tiles. tileGridSize defines the
     *                            number of tiles in row and column
     * @param tileGridSize_height {@code tileGridSize.height} component of Size of grid for histogram equalization.
     *                            Input image will be divided into equally sized rectangular tiles. tileGridSize defines
     *                            the number of tiles in row and column
     * @return native binding handle for a new {@code CLAHE} wrapper configured with the selected options
     */
    private static native long createCLAHE_0(double clipLimit, double tileGridSize_width, double tileGridSize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createCLAHE(double)}.
     *
     * @param clipLimit Threshold for contrast limiting
     * @return native binding handle for a new {@code CLAHE} wrapper configured with the selected options
     */
    private static native long createCLAHE_1(double clipLimit);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createCLAHE()}.
     *
     * @return native binding handle for a new {@code CLAHE} wrapper configured with the selected options
     */
    private static native long createCLAHE_2();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#EMD(Mat,Mat,int,Mat,Mat)}.
     *
     * @param signature1_nativeObj native matrix or object handle for {@code signature1}; First signature, a
     *                             {@code size1} x {@code dims}+1 floating-point matrix. Each row stores the point
     *                             weight followed by the point coordinates. The matrix is allowed to have a single
     *                             column (weights only) if the user-defined cost matrix is used. The weights must be
     *                             non-negative and have at least one non-zero value
     * @param signature2_nativeObj native matrix or object handle for {@code signature2}; Second signature of the same
     *                             format as signature1 , though the number of rows may be different. The total weights
     *                             may be different. In this case an extra "dummy" point is added to either signature1
     *                             or signature2. The weights must be non-negative and have at least one non-zero value
     * @param distType             Used metric. See DistanceTypes
     * @param cost_nativeObj       native matrix or object handle for {@code cost}; User-defined {@code size1} x
     *                             {@code size2} cost matrix. Also, if a cost matrix is used, lower boundary lowerBound
     *                             cannot be calculated because it needs a metric function. signatures that is a
     *                             distance between mass centers. The lower boundary may not be calculated if the
     *                             user-defined cost matrix is used, the total weights of point configurations are not
     *                             equal, or if the signatures consist of weights only (the signature matrices have a
     *                             single column). You must initialize *lowerBound . If the calculated distance between
     *                             mass centers is greater or equal to *lowerBound (it means that the signatures are far
     *                             enough), the function does not calculate EMD. In any case *lowerBound is set to the
     *                             calculated distance between mass centers on return. Thus, if you want to calculate
     *                             both distance between mass centers and EMD, *lowerBound should be set to 0
     * @param flow_nativeObj       native matrix or object handle for {@code flow}; Resultant {@code size1} times
     *                             {@code size2} flow matrix: {@code flow}_{i,j} is a flow from i -th point of
     *                             signature1 to j -th point of signature2
     * @return the minimum transportation cost between the weighted signatures
     */
    private static native float EMD_0(
            long signature1_nativeObj,
            long signature2_nativeObj,
            int distType,
            long cost_nativeObj,
            long flow_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#EMD(Mat,Mat,int,Mat)}.
     *
     * @param signature1_nativeObj native matrix or object handle for {@code signature1}; First signature, a
     *                             {@code size1} x {@code dims}+1 floating-point matrix. Each row stores the point
     *                             weight followed by the point coordinates. The matrix is allowed to have a single
     *                             column (weights only) if the user-defined cost matrix is used. The weights must be
     *                             non-negative and have at least one non-zero value
     * @param signature2_nativeObj native matrix or object handle for {@code signature2}; Second signature of the same
     *                             format as signature1 , though the number of rows may be different. The total weights
     *                             may be different. In this case an extra "dummy" point is added to either signature1
     *                             or signature2. The weights must be non-negative and have at least one non-zero value
     * @param distType             Used metric. See DistanceTypes
     * @param cost_nativeObj       native matrix or object handle for {@code cost}; User-defined {@code size1} x
     *                             {@code size2} cost matrix. Also, if a cost matrix is used, lower boundary lowerBound
     *                             cannot be calculated because it needs a metric function. signatures that is a
     *                             distance between mass centers. The lower boundary may not be calculated if the
     *                             user-defined cost matrix is used, the total weights of point configurations are not
     *                             equal, or if the signatures consist of weights only (the signature matrices have a
     *                             single column). You must initialize *lowerBound . If the calculated distance between
     *                             mass centers is greater or equal to *lowerBound (it means that the signatures are far
     *                             enough), the function does not calculate EMD. In any case *lowerBound is set to the
     *                             calculated distance between mass centers on return. Thus, if you want to calculate
     *                             both distance between mass centers and EMD, *lowerBound should be set to 0
     * @return the minimum transportation cost between the weighted signatures
     */
    private static native float EMD_1(
            long signature1_nativeObj,
            long signature2_nativeObj,
            int distType,
            long cost_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#EMD(Mat,Mat,int)}.
     *
     * @param signature1_nativeObj native matrix or object handle for {@code signature1}; First signature, a
     *                             {@code size1} x {@code dims}+1 floating-point matrix. Each row stores the point
     *                             weight followed by the point coordinates. The matrix is allowed to have a single
     *                             column (weights only) if the user-defined cost matrix is used. The weights must be
     *                             non-negative and have at least one non-zero value
     * @param signature2_nativeObj native matrix or object handle for {@code signature2}; Second signature of the same
     *                             format as signature1 , though the number of rows may be different. The total weights
     *                             may be different. In this case an extra "dummy" point is added to either signature1
     *                             or signature2. The weights must be non-negative and have at least one non-zero value
     * @param distType             Used metric. See DistanceTypes
     * @return the minimum transportation cost between the weighted signatures
     */
    private static native float EMD_3(long signature1_nativeObj, long signature2_nativeObj, int distType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#watershed(Mat,Mat)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; Input 8-bit 3-channel image
     * @param markers_nativeObj native matrix or object handle for {@code markers}; Input/output 32-bit single-channel
     *                          image (map) of markers. It should have the same size as image
     */
    private static native void watershed_0(long image_nativeObj, long markers_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrMeanShiftFiltering(Mat,Mat,double,double,int,TermCriteria)}.
     *
     * @param src_nativeObj     native matrix or object handle for {@code src}; The source 8-bit, 3-channel image
     * @param dst_nativeObj     native matrix or object handle for {@code dst}; The destination image of the same format
     *                          and the same size as the source
     * @param sp                The spatial window radius
     * @param sr                The color window radius
     * @param maxLevel          Maximum level of the pyramid for the segmentation
     * @param termcrit_type     {@code termcrit.type} component of Termination criteria: when to stop meanshift
     *                          iterations
     * @param termcrit_maxCount {@code termcrit.maxCount} component of Termination criteria: when to stop meanshift
     *                          iterations
     * @param termcrit_epsilon  {@code termcrit.epsilon} component of Termination criteria: when to stop meanshift
     *                          iterations
     */
    private static native void pyrMeanShiftFiltering_0(
            long src_nativeObj,
            long dst_nativeObj,
            double sp,
            double sr,
            int maxLevel,
            int termcrit_type,
            int termcrit_maxCount,
            double termcrit_epsilon);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrMeanShiftFiltering(Mat,Mat,double,double,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; The source 8-bit, 3-channel image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; The destination image of the same format and
     *                      the same size as the source
     * @param sp            The spatial window radius
     * @param sr            The color window radius
     * @param maxLevel      Maximum level of the pyramid for the segmentation
     */
    private static native void pyrMeanShiftFiltering_1(
            long src_nativeObj,
            long dst_nativeObj,
            double sp,
            double sr,
            int maxLevel);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#pyrMeanShiftFiltering(Mat,Mat,double,double)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; The source 8-bit, 3-channel image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; The destination image of the same format and
     *                      the same size as the source
     * @param sp            The spatial window radius
     * @param sr            The color window radius
     */
    private static native void pyrMeanShiftFiltering_2(long src_nativeObj, long dst_nativeObj, double sp, double sr);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#grabCut(Mat,Mat,Rect,Mat,Mat,int,int)}.
     *
     * @param img_nativeObj      native matrix or object handle for {@code img}; Input 8-bit 3-channel image
     * @param mask_nativeObj     native matrix or object handle for {@code mask}; Input/output 8-bit single-channel
     *                           mask. The mask is initialized by the function when mode is set to GC_INIT_WITH_RECT.
     *                           Its elements may have one of the GrabCutClasses
     * @param rect_x             {@code rect.x} component of ROI containing a segmented object. The pixels outside of
     *                           the ROI are marked as "obvious background". The parameter is only used when
     *                           mode==GC_INIT_WITH_RECT
     * @param rect_y             {@code rect.y} component of ROI containing a segmented object. The pixels outside of
     *                           the ROI are marked as "obvious background". The parameter is only used when
     *                           mode==GC_INIT_WITH_RECT
     * @param rect_width         {@code rect.width} component of ROI containing a segmented object. The pixels outside
     *                           of the ROI are marked as "obvious background". The parameter is only used when
     *                           mode==GC_INIT_WITH_RECT
     * @param rect_height        {@code rect.height} component of ROI containing a segmented object. The pixels outside
     *                           of the ROI are marked as "obvious background". The parameter is only used when
     *                           mode==GC_INIT_WITH_RECT
     * @param bgdModel_nativeObj native matrix or object handle for {@code bgdModel}; Temporary array for the background
     *                           model. Do not modify it while you are processing the same image
     * @param fgdModel_nativeObj native matrix or object handle for {@code fgdModel}; Temporary arrays for the
     *                           foreground model. Do not modify it while you are processing the same image
     * @param iterCount          Number of iterations the algorithm should make before returning the result. Note that
     *                           the result can be refined with further calls with mode==GC_INIT_WITH_MASK or
     *                           mode==GC_EVAL
     * @param mode               Operation mode that could be one of the GrabCutModes
     */
    private static native void grabCut_0(
            long img_nativeObj,
            long mask_nativeObj,
            int rect_x,
            int rect_y,
            int rect_width,
            int rect_height,
            long bgdModel_nativeObj,
            long fgdModel_nativeObj,
            int iterCount,
            int mode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#grabCut(Mat,Mat,Rect,Mat,Mat,int)}.
     *
     * @param img_nativeObj      native matrix or object handle for {@code img}; Input 8-bit 3-channel image
     * @param mask_nativeObj     native matrix or object handle for {@code mask}; Input/output 8-bit single-channel
     *                           mask. The mask is initialized by the function when mode is set to GC_INIT_WITH_RECT.
     *                           Its elements may have one of the GrabCutClasses
     * @param rect_x             {@code rect.x} component of ROI containing a segmented object. The pixels outside of
     *                           the ROI are marked as "obvious background". The parameter is only used when
     *                           mode==GC_INIT_WITH_RECT
     * @param rect_y             {@code rect.y} component of ROI containing a segmented object. The pixels outside of
     *                           the ROI are marked as "obvious background". The parameter is only used when
     *                           mode==GC_INIT_WITH_RECT
     * @param rect_width         {@code rect.width} component of ROI containing a segmented object. The pixels outside
     *                           of the ROI are marked as "obvious background". The parameter is only used when
     *                           mode==GC_INIT_WITH_RECT
     * @param rect_height        {@code rect.height} component of ROI containing a segmented object. The pixels outside
     *                           of the ROI are marked as "obvious background". The parameter is only used when
     *                           mode==GC_INIT_WITH_RECT
     * @param bgdModel_nativeObj native matrix or object handle for {@code bgdModel}; Temporary array for the background
     *                           model. Do not modify it while you are processing the same image
     * @param fgdModel_nativeObj native matrix or object handle for {@code fgdModel}; Temporary arrays for the
     *                           foreground model. Do not modify it while you are processing the same image
     * @param iterCount          Number of iterations the algorithm should make before returning the result. Note that
     *                           the result can be refined with further calls with mode==GC_INIT_WITH_MASK or
     *                           mode==GC_EVAL
     */
    private static native void grabCut_1(
            long img_nativeObj,
            long mask_nativeObj,
            int rect_x,
            int rect_y,
            int rect_width,
            int rect_height,
            long bgdModel_nativeObj,
            long fgdModel_nativeObj,
            int iterCount);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#distanceTransformWithLabels(Mat,Mat,Mat,int,int,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; 8-bit, single-channel (binary) source
     *                         image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Output image with calculated distances.
     *                         It is a 8-bit or 32-bit floating-point, single-channel image of the same size as src
     * @param labels_nativeObj native matrix or object handle for {@code labels}; Output 2D array of labels (the
     *                         discrete Voronoi diagram). It has the type CV_32SC1 and the same size as src
     * @param distanceType     Type of distance, see DistanceTypes
     * @param maskSize         Size of the distance transform mask, see DistanceTransformMasks. DIST_MASK_PRECISE is not
     *                         supported by this variant. In case of the DIST_L1 or DIST_C distance type, the parameter
     *                         is forced to 3 because a 3 x 3 mask gives the same result as 5 x 5 or any larger aperture
     * @param labelType        Type of the label array to build, see DistanceTransformLabelTypes
     */
    private static native void distanceTransformWithLabels_0(
            long src_nativeObj,
            long dst_nativeObj,
            long labels_nativeObj,
            int distanceType,
            int maskSize,
            int labelType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#distanceTransformWithLabels(Mat,Mat,Mat,int,int)}.
     *
     * @param src_nativeObj    native matrix or object handle for {@code src}; 8-bit, single-channel (binary) source
     *                         image
     * @param dst_nativeObj    native matrix or object handle for {@code dst}; Output image with calculated distances.
     *                         It is a 8-bit or 32-bit floating-point, single-channel image of the same size as src
     * @param labels_nativeObj native matrix or object handle for {@code labels}; Output 2D array of labels (the
     *                         discrete Voronoi diagram). It has the type CV_32SC1 and the same size as src
     * @param distanceType     Type of distance, see DistanceTypes
     * @param maskSize         Size of the distance transform mask, see DistanceTransformMasks. DIST_MASK_PRECISE is not
     *                         supported by this variant. In case of the DIST_L1 or DIST_C distance type, the parameter
     *                         is forced to 3 because a 3 x 3 mask gives the same result as 5 x 5 or any larger aperture
     */
    private static native void distanceTransformWithLabels_1(
            long src_nativeObj,
            long dst_nativeObj,
            long labels_nativeObj,
            int distanceType,
            int maskSize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#distanceTransform(Mat,Mat,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; 8-bit, single-channel (binary) source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output image with calculated distances. It
     *                      is a 8-bit or 32-bit floating-point, single-channel image of the same size as src
     * @param distanceType  Type of distance, see DistanceTypes
     * @param maskSize      Size of the distance transform mask, see DistanceTransformMasks. In case of the DIST_L1 or
     *                      DIST_C distance type, the parameter is forced to 3 because a 3 x 3 mask gives the same
     *                      result as 5 x 5 or any larger aperture
     * @param dstType       Type of output image. It can be CV_8U or CV_32F. Type CV_8U can be used only for the first
     *                      variant of the function and distanceType == DIST_L1
     */
    private static native void distanceTransform_0(
            long src_nativeObj,
            long dst_nativeObj,
            int distanceType,
            int maskSize,
            int dstType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#distanceTransform(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; 8-bit, single-channel (binary) source image
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Output image with calculated distances. It
     *                      is a 8-bit or 32-bit floating-point, single-channel image of the same size as src
     * @param distanceType  Type of distance, see DistanceTypes
     * @param maskSize      Size of the distance transform mask, see DistanceTransformMasks. In case of the DIST_L1 or
     *                      DIST_C distance type, the parameter is forced to 3 because a 3 x 3 mask gives the same
     *                      result as 5 x 5 or any larger aperture
     */
    private static native void distanceTransform_1(
            long src_nativeObj,
            long dst_nativeObj,
            int distanceType,
            int maskSize);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#floodFill(Mat,Mat,Point,Scalar,Rect,Scalar,Scalar,int)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; Input/output 1- or 3-channel, 8-bit, or
     *                        floating-point image. It is modified by the function unless the FLOODFILL_MASK_ONLY flag
     *                        is set in the second variant of the function. See the details below
     * @param mask_nativeObj  native matrix or object handle for {@code mask}; Operation mask that should be a
     *                        single-channel 8-bit image, 2 pixels wider and 2 pixels taller than image. If an empty Mat
     *                        is passed it will be created automatically. Since this is both an input and output
     *                        parameter, you must take responsibility of initializing it. Flood-filling cannot go across
     *                        non-zero pixels in the input mask. For example, an edge detector output can be used as a
     *                        mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                        pixels in the image are set to 1 or to the specified value in flags as described below.
     *                        Additionally, the function fills the border of the mask with ones to simplify internal
     *                        processing. It is therefore possible to use the same mask in multiple calls to the
     *                        function to make sure the filled areas do not overlap
     * @param seedPoint_x     {@code seedPoint.x} component of Starting point
     * @param seedPoint_y     {@code seedPoint.y} component of Starting point
     * @param newVal_val0     {@code newVal.val[0]} component of New value of the repainted domain pixels
     * @param newVal_val1     {@code newVal.val[1]} component of New value of the repainted domain pixels
     * @param newVal_val2     {@code newVal.val[2]} component of New value of the repainted domain pixels
     * @param newVal_val3     {@code newVal.val[3]} component of New value of the repainted domain pixels
     * @param rect_out        output array receiving {@code rect} from the native call
     * @param loDiff_val0     {@code loDiff.val[0]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val1     {@code loDiff.val[1]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val2     {@code loDiff.val[2]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val3     {@code loDiff.val[3]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param upDiff_val0     {@code upDiff.val[0]} component of Maximal upper brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param upDiff_val1     {@code upDiff.val[1]} component of Maximal upper brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param upDiff_val2     {@code upDiff.val[2]} component of Maximal upper brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param upDiff_val3     {@code upDiff.val[3]} component of Maximal upper brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param flags           Operation flags. The first 8 bits contain a connectivity value. The default value of 4
     *                        means that only the four nearest neighbor pixels (those that share an edge) are
     *                        considered. A connectivity value of 8 means that the eight nearest neighbor pixels (those
     *                        that share a corner) will be considered. The next 8 bits (8-16) contain a value between 1
     *                        and 255 with which to fill the mask (the default value is 1). For example, 4 | ( 255
     *                        &lt;&lt; 8 ) will consider 4 nearest neighbours and fill the mask with a value of 255. The
     *                        following additional options occupy higher bits and therefore may be further combined with
     *                        the connectivity and mask fill values using bit-wise or (|), see FloodFillFlags. Note:
     *                        Since the mask is larger than the filled image, a pixel (x, y) in image corresponds to the
     *                        pixel (x+1, y+1) in the mask
     * @return the number of pixels included in the filled region
     */
    private static native int floodFill_0(
            long image_nativeObj,
            long mask_nativeObj,
            double seedPoint_x,
            double seedPoint_y,
            double newVal_val0,
            double newVal_val1,
            double newVal_val2,
            double newVal_val3,
            double[] rect_out,
            double loDiff_val0,
            double loDiff_val1,
            double loDiff_val2,
            double loDiff_val3,
            double upDiff_val0,
            double upDiff_val1,
            double upDiff_val2,
            double upDiff_val3,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#floodFill(Mat,Mat,Point,Scalar,Rect,Scalar,Scalar)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; Input/output 1- or 3-channel, 8-bit, or
     *                        floating-point image. It is modified by the function unless the FLOODFILL_MASK_ONLY flag
     *                        is set in the second variant of the function. See the details below
     * @param mask_nativeObj  native matrix or object handle for {@code mask}; Operation mask that should be a
     *                        single-channel 8-bit image, 2 pixels wider and 2 pixels taller than image. If an empty Mat
     *                        is passed it will be created automatically. Since this is both an input and output
     *                        parameter, you must take responsibility of initializing it. Flood-filling cannot go across
     *                        non-zero pixels in the input mask. For example, an edge detector output can be used as a
     *                        mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                        pixels in the image are set to 1 or to the specified value in flags as described below.
     *                        Additionally, the function fills the border of the mask with ones to simplify internal
     *                        processing. It is therefore possible to use the same mask in multiple calls to the
     *                        function to make sure the filled areas do not overlap
     * @param seedPoint_x     {@code seedPoint.x} component of Starting point
     * @param seedPoint_y     {@code seedPoint.y} component of Starting point
     * @param newVal_val0     {@code newVal.val[0]} component of New value of the repainted domain pixels
     * @param newVal_val1     {@code newVal.val[1]} component of New value of the repainted domain pixels
     * @param newVal_val2     {@code newVal.val[2]} component of New value of the repainted domain pixels
     * @param newVal_val3     {@code newVal.val[3]} component of New value of the repainted domain pixels
     * @param rect_out        output array receiving {@code rect} from the native call
     * @param loDiff_val0     {@code loDiff.val[0]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val1     {@code loDiff.val[1]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val2     {@code loDiff.val[2]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val3     {@code loDiff.val[3]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param upDiff_val0     {@code upDiff.val[0]} component of Maximal upper brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param upDiff_val1     {@code upDiff.val[1]} component of Maximal upper brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param upDiff_val2     {@code upDiff.val[2]} component of Maximal upper brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param upDiff_val3     {@code upDiff.val[3]} component of Maximal upper brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @return the number of pixels included in the filled region
     */
    private static native int floodFill_1(
            long image_nativeObj,
            long mask_nativeObj,
            double seedPoint_x,
            double seedPoint_y,
            double newVal_val0,
            double newVal_val1,
            double newVal_val2,
            double newVal_val3,
            double[] rect_out,
            double loDiff_val0,
            double loDiff_val1,
            double loDiff_val2,
            double loDiff_val3,
            double upDiff_val0,
            double upDiff_val1,
            double upDiff_val2,
            double upDiff_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#floodFill(Mat,Mat,Point,Scalar,Rect,Scalar)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; Input/output 1- or 3-channel, 8-bit, or
     *                        floating-point image. It is modified by the function unless the FLOODFILL_MASK_ONLY flag
     *                        is set in the second variant of the function. See the details below
     * @param mask_nativeObj  native matrix or object handle for {@code mask}; Operation mask that should be a
     *                        single-channel 8-bit image, 2 pixels wider and 2 pixels taller than image. If an empty Mat
     *                        is passed it will be created automatically. Since this is both an input and output
     *                        parameter, you must take responsibility of initializing it. Flood-filling cannot go across
     *                        non-zero pixels in the input mask. For example, an edge detector output can be used as a
     *                        mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                        pixels in the image are set to 1 or to the specified value in flags as described below.
     *                        Additionally, the function fills the border of the mask with ones to simplify internal
     *                        processing. It is therefore possible to use the same mask in multiple calls to the
     *                        function to make sure the filled areas do not overlap
     * @param seedPoint_x     {@code seedPoint.x} component of Starting point
     * @param seedPoint_y     {@code seedPoint.y} component of Starting point
     * @param newVal_val0     {@code newVal.val[0]} component of New value of the repainted domain pixels
     * @param newVal_val1     {@code newVal.val[1]} component of New value of the repainted domain pixels
     * @param newVal_val2     {@code newVal.val[2]} component of New value of the repainted domain pixels
     * @param newVal_val3     {@code newVal.val[3]} component of New value of the repainted domain pixels
     * @param rect_out        output array receiving {@code rect} from the native call
     * @param loDiff_val0     {@code loDiff.val[0]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val1     {@code loDiff.val[1]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val2     {@code loDiff.val[2]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @param loDiff_val3     {@code loDiff.val[3]} component of Maximal lower brightness/color difference between the
     *                        currently observed pixel and one of its neighbors belonging to the component, or a seed
     *                        pixel being added to the component
     * @return the number of pixels included in the filled region
     */
    private static native int floodFill_2(
            long image_nativeObj,
            long mask_nativeObj,
            double seedPoint_x,
            double seedPoint_y,
            double newVal_val0,
            double newVal_val1,
            double newVal_val2,
            double newVal_val3,
            double[] rect_out,
            double loDiff_val0,
            double loDiff_val1,
            double loDiff_val2,
            double loDiff_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#floodFill(Mat,Mat,Point,Scalar,Rect)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; Input/output 1- or 3-channel, 8-bit, or
     *                        floating-point image. It is modified by the function unless the FLOODFILL_MASK_ONLY flag
     *                        is set in the second variant of the function. See the details below
     * @param mask_nativeObj  native matrix or object handle for {@code mask}; Operation mask that should be a
     *                        single-channel 8-bit image, 2 pixels wider and 2 pixels taller than image. If an empty Mat
     *                        is passed it will be created automatically. Since this is both an input and output
     *                        parameter, you must take responsibility of initializing it. Flood-filling cannot go across
     *                        non-zero pixels in the input mask. For example, an edge detector output can be used as a
     *                        mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                        pixels in the image are set to 1 or to the specified value in flags as described below.
     *                        Additionally, the function fills the border of the mask with ones to simplify internal
     *                        processing. It is therefore possible to use the same mask in multiple calls to the
     *                        function to make sure the filled areas do not overlap
     * @param seedPoint_x     {@code seedPoint.x} component of Starting point
     * @param seedPoint_y     {@code seedPoint.y} component of Starting point
     * @param newVal_val0     {@code newVal.val[0]} component of New value of the repainted domain pixels
     * @param newVal_val1     {@code newVal.val[1]} component of New value of the repainted domain pixels
     * @param newVal_val2     {@code newVal.val[2]} component of New value of the repainted domain pixels
     * @param newVal_val3     {@code newVal.val[3]} component of New value of the repainted domain pixels
     * @param rect_out        output array receiving {@code rect} from the native call
     * @return the number of pixels included in the filled region
     */
    private static native int floodFill_3(
            long image_nativeObj,
            long mask_nativeObj,
            double seedPoint_x,
            double seedPoint_y,
            double newVal_val0,
            double newVal_val1,
            double newVal_val2,
            double newVal_val3,
            double[] rect_out);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#floodFill(Mat,Mat,Point,Scalar)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; Input/output 1- or 3-channel, 8-bit, or
     *                        floating-point image. It is modified by the function unless the FLOODFILL_MASK_ONLY flag
     *                        is set in the second variant of the function. See the details below
     * @param mask_nativeObj  native matrix or object handle for {@code mask}; Operation mask that should be a
     *                        single-channel 8-bit image, 2 pixels wider and 2 pixels taller than image. If an empty Mat
     *                        is passed it will be created automatically. Since this is both an input and output
     *                        parameter, you must take responsibility of initializing it. Flood-filling cannot go across
     *                        non-zero pixels in the input mask. For example, an edge detector output can be used as a
     *                        mask to stop filling at edges. On output, pixels in the mask corresponding to filled
     *                        pixels in the image are set to 1 or to the specified value in flags as described below.
     *                        Additionally, the function fills the border of the mask with ones to simplify internal
     *                        processing. It is therefore possible to use the same mask in multiple calls to the
     *                        function to make sure the filled areas do not overlap
     * @param seedPoint_x     {@code seedPoint.x} component of Starting point
     * @param seedPoint_y     {@code seedPoint.y} component of Starting point
     * @param newVal_val0     {@code newVal.val[0]} component of New value of the repainted domain pixels
     * @param newVal_val1     {@code newVal.val[1]} component of New value of the repainted domain pixels
     * @param newVal_val2     {@code newVal.val[2]} component of New value of the repainted domain pixels
     * @param newVal_val3     {@code newVal.val[3]} component of New value of the repainted domain pixels
     * @return the number of pixels included in the filled region
     */
    private static native int floodFill_4(
            long image_nativeObj,
            long mask_nativeObj,
            double seedPoint_x,
            double seedPoint_y,
            double newVal_val0,
            double newVal_val1,
            double newVal_val2,
            double newVal_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#blendLinear(Mat,Mat,Mat,Mat,Mat)}.
     *
     * @param src1_nativeObj     native matrix or object handle for {@code src1}; first source matrix or channel value
     * @param src2_nativeObj     native matrix or object handle for {@code src2}; second source matrix or channel value
     * @param weights1_nativeObj native matrix or object handle for {@code weights1}; weights of the first discrete
     *                           distribution
     * @param weights2_nativeObj native matrix or object handle for {@code weights2}; weights of the second discrete
     *                           distribution
     * @param dst_nativeObj      native matrix or object handle for {@code dst}; destination matrix that receives the
     *                           result
     */
    private static native void blendLinear_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long weights1_nativeObj,
            long weights2_nativeObj,
            long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#alphaComposite(Mat,Mat,Mat,int,boolean)}.
     *
     * @param overlay_nativeObj    native matrix or object handle for {@code overlay}; 8-bit 4-channel image (RGBA or
     *                             BGRA; channel order does not matter as long as it matches background) acting as the
     *                             source of the operator; must be the same size as background
     * @param background_nativeObj native matrix or object handle for {@code background}; 8-bit 3-channel or 4-channel
     *                             image acting as the destination of the operator. A 4-channel background contributes
     *                             its own alpha to the blend, using the same straight/premultiplied convention as
     *                             overlay, and dst has 4 channels holding the composited alpha alpha_o. A 3-channel
     *                             background is taken to be fully opaque (alpha_b = 1) and dst has 3 channels; since
     *                             such a dst cannot store alpha_o, the alpha is dropped and the premultiplied color is
     *                             written, which is what Cairo does when the target surface has no alpha channel.
     *                             Operators whose result is not opaque (ALPHA_COMPOSITE_CLEAR, ALPHA_COMPOSITE_OUT,
     *                             ALPHA_COMPOSITE_XOR and others) therefore darken toward black on a 3-channel
     *                             background; pass a 4-channel background to keep the alpha
     * @param dst_nativeObj        native matrix or object handle for {@code dst}; output image, created with the same
     *                             size and type as background. It may be the same array as background
     * @param op                   compositing operator, one of AlphaCompositeOperations
     * @param premultiplied        false (the default) if overlay's alpha (and background's, when it has one) is
     *                             straight/unassociated, as produced by e.g. imread(..., IMREAD_UNCHANGED); true if it
     *                             is already premultiplied, which skips the internal conversion
     */
    private static native void alphaComposite_0(
            long overlay_nativeObj,
            long background_nativeObj,
            long dst_nativeObj,
            int op,
            boolean premultiplied);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#alphaComposite(Mat,Mat,Mat,int)}.
     *
     * @param overlay_nativeObj    native matrix or object handle for {@code overlay}; 8-bit 4-channel image (RGBA or
     *                             BGRA; channel order does not matter as long as it matches background) acting as the
     *                             source of the operator; must be the same size as background
     * @param background_nativeObj native matrix or object handle for {@code background}; 8-bit 3-channel or 4-channel
     *                             image acting as the destination of the operator. A 4-channel background contributes
     *                             its own alpha to the blend, using the same straight/premultiplied convention as
     *                             overlay, and dst has 4 channels holding the composited alpha alpha_o. A 3-channel
     *                             background is taken to be fully opaque (alpha_b = 1) and dst has 3 channels; since
     *                             such a dst cannot store alpha_o, the alpha is dropped and the premultiplied color is
     *                             written, which is what Cairo does when the target surface has no alpha channel.
     *                             Operators whose result is not opaque (ALPHA_COMPOSITE_CLEAR, ALPHA_COMPOSITE_OUT,
     *                             ALPHA_COMPOSITE_XOR and others) therefore darken toward black on a 3-channel
     *                             background; pass a 4-channel background to keep the alpha
     * @param dst_nativeObj        native matrix or object handle for {@code dst}; output image, created with the same
     *                             size and type as background. It may be the same array as background
     * @param op                   compositing operator, one of AlphaCompositeOperations
     */
    private static native void alphaComposite_1(
            long overlay_nativeObj,
            long background_nativeObj,
            long dst_nativeObj,
            int op);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#alphaComposite(Mat,Mat,Mat)}.
     *
     * @param overlay_nativeObj    native matrix or object handle for {@code overlay}; 8-bit 4-channel image (RGBA or
     *                             BGRA; channel order does not matter as long as it matches background) acting as the
     *                             source of the operator; must be the same size as background
     * @param background_nativeObj native matrix or object handle for {@code background}; 8-bit 3-channel or 4-channel
     *                             image acting as the destination of the operator. A 4-channel background contributes
     *                             its own alpha to the blend, using the same straight/premultiplied convention as
     *                             overlay, and dst has 4 channels holding the composited alpha alpha_o. A 3-channel
     *                             background is taken to be fully opaque (alpha_b = 1) and dst has 3 channels; since
     *                             such a dst cannot store alpha_o, the alpha is dropped and the premultiplied color is
     *                             written, which is what Cairo does when the target surface has no alpha channel.
     *                             Operators whose result is not opaque (ALPHA_COMPOSITE_CLEAR, ALPHA_COMPOSITE_OUT,
     *                             ALPHA_COMPOSITE_XOR and others) therefore darken toward black on a 3-channel
     *                             background; pass a 4-channel background to keep the alpha
     * @param dst_nativeObj        native matrix or object handle for {@code dst}; output image, created with the same
     *                             size and type as background. It may be the same array as background
     */
    private static native void alphaComposite_2(long overlay_nativeObj, long background_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cvtColor(Mat,Mat,int,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image: 8-bit unsigned, 16-bit unsigned
     *                      ( CV_16UC... ), or single-precision floating-point
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and depth as
     *                      src
     * @param code          color space conversion code (see ColorConversionCodes)
     * @param dstCn         number of channels in the destination image; if the parameter is 0, the number of the
     *                      channels is derived automatically from src and code
     * @param hint          Implementation modification flags. See AlgorithmHint Note: The source image (src) must be of
     *                      an appropriate type for the desired color conversion. see ColorConversionCodes
     */
    private static native void cvtColor_0(long src_nativeObj, long dst_nativeObj, int code, int dstCn, int hint);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cvtColor(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image: 8-bit unsigned, 16-bit unsigned
     *                      ( CV_16UC... ), or single-precision floating-point
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and depth as
     *                      src
     * @param code          color space conversion code (see ColorConversionCodes)
     * @param dstCn         number of channels in the destination image; if the parameter is 0, the number of the
     *                      channels is derived automatically from src and code
     */
    private static native void cvtColor_1(long src_nativeObj, long dst_nativeObj, int code, int dstCn);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cvtColor(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image: 8-bit unsigned, 16-bit unsigned
     *                      ( CV_16UC... ), or single-precision floating-point
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and depth as
     *                      src
     * @param code          color space conversion code (see ColorConversionCodes)
     */
    private static native void cvtColor_2(long src_nativeObj, long dst_nativeObj, int code);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cvtColorTwoPlane(Mat,Mat,Mat,int,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; 8-bit image (CV_8U) of the Y plane
     * @param src2_nativeObj native matrix or object handle for {@code src2}; image containing interleaved U/V plane
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output image
     * @param code           Specifies the type of conversion. It can take any of the following values:
     *                       COLOR_YUV2BGR_NV12 COLOR_YUV2RGB_NV12 COLOR_YUV2BGRA_NV12 COLOR_YUV2RGBA_NV12
     *                       COLOR_YUV2BGR_NV21 COLOR_YUV2RGB_NV21 COLOR_YUV2BGRA_NV21 COLOR_YUV2RGBA_NV21
     * @param hint           Implementation modification flags. See AlgorithmHint
     */
    private static native void cvtColorTwoPlane_0(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            int code,
            int hint);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#cvtColorTwoPlane(Mat,Mat,Mat,int)}.
     *
     * @param src1_nativeObj native matrix or object handle for {@code src1}; 8-bit image (CV_8U) of the Y plane
     * @param src2_nativeObj native matrix or object handle for {@code src2}; image containing interleaved U/V plane
     * @param dst_nativeObj  native matrix or object handle for {@code dst}; output image
     * @param code           Specifies the type of conversion. It can take any of the following values:
     *                       COLOR_YUV2BGR_NV12 COLOR_YUV2RGB_NV12 COLOR_YUV2BGRA_NV12 COLOR_YUV2RGBA_NV12
     *                       COLOR_YUV2BGR_NV21 COLOR_YUV2RGB_NV21 COLOR_YUV2BGRA_NV21 COLOR_YUV2RGBA_NV21
     */
    private static native void cvtColorTwoPlane_1(
            long src1_nativeObj,
            long src2_nativeObj,
            long dst_nativeObj,
            int code);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#demosaicing(Mat,Mat,int,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image: 8-bit unsigned or 16-bit
     *                      unsigned
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and depth as
     *                      src
     * @param code          Color space conversion code (see the description below)
     * @param dstCn         number of channels in the destination image; if the parameter is 0, the number of the
     *                      channels is derived automatically from src and code. The function can do the following
     *                      transformations: Demosaicing using bilinear interpolation COLOR_BayerBG2BGR ,
     *                      COLOR_BayerGB2BGR , COLOR_BayerRG2BGR , COLOR_BayerGR2BGR COLOR_BayerBG2GRAY ,
     *                      COLOR_BayerGB2GRAY , COLOR_BayerRG2GRAY , COLOR_BayerGR2GRAY Demosaicing using Variable
     *                      Number of Gradients. COLOR_BayerBG2BGR_VNG , COLOR_BayerGB2BGR_VNG , COLOR_BayerRG2BGR_VNG ,
     *                      COLOR_BayerGR2BGR_VNG Edge-Aware Demosaicing. COLOR_BayerBG2BGR_EA , COLOR_BayerGB2BGR_EA ,
     *                      COLOR_BayerRG2BGR_EA , COLOR_BayerGR2BGR_EA Demosaicing with alpha channel
     *                      COLOR_BayerBG2BGRA , COLOR_BayerGB2BGRA , COLOR_BayerRG2BGRA , COLOR_BayerGR2BGRA Note: The
     *                      source image (src) must be of an appropriate type for the desired color conversion. see
     *                      ColorConversionCodes
     */
    private static native void demosaicing_0(long src_nativeObj, long dst_nativeObj, int code, int dstCn);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#demosaicing(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; input image: 8-bit unsigned or 16-bit
     *                      unsigned
     * @param dst_nativeObj native matrix or object handle for {@code dst}; output image of the same size and depth as
     *                      src
     * @param code          Color space conversion code (see the description below)
     */
    private static native void demosaicing_1(long src_nativeObj, long dst_nativeObj, int code);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#matchTemplate(Mat,Mat,Mat,int,Mat)}.
     *
     * @param image_nativeObj  native matrix or object handle for {@code image}; Image where the search is running. It
     *                         must be 8-bit or 32-bit floating-point
     * @param templ_nativeObj  native matrix or object handle for {@code templ}; Searched template. It must be not
     *                         greater than the source image and have the same data type
     * @param result_nativeObj native matrix or object handle for {@code result}; Map of comparison results. It must be
     *                         single-channel 32-bit floating-point. If image is W times H and templ is w times h , then
     *                         result is (W-w+1) times (H-h+1)
     * @param method           Parameter specifying the comparison method, see TemplateMatchModes
     * @param mask_nativeObj   native matrix or object handle for {@code mask}; Optional mask. It must have the same
     *                         size as templ. It must either have the same number of channels as template or only one
     *                         channel, which is then used for all template and image channels. If the data type is
     *                         CV_8U, the mask is interpreted as a binary mask, meaning only elements where mask is
     *                         nonzero are used and are kept unchanged independent of the actual mask value (weight
     *                         equals 1). For data type CV_32F, the mask values are used as weights. The exact formulas
     *                         are documented in TemplateMatchModes
     */
    private static native void matchTemplate_0(
            long image_nativeObj,
            long templ_nativeObj,
            long result_nativeObj,
            int method,
            long mask_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#matchTemplate(Mat,Mat,Mat,int)}.
     *
     * @param image_nativeObj  native matrix or object handle for {@code image}; Image where the search is running. It
     *                         must be 8-bit or 32-bit floating-point
     * @param templ_nativeObj  native matrix or object handle for {@code templ}; Searched template. It must be not
     *                         greater than the source image and have the same data type
     * @param result_nativeObj native matrix or object handle for {@code result}; Map of comparison results. It must be
     *                         single-channel 32-bit floating-point. If image is W times H and templ is w times h , then
     *                         result is (W-w+1) times (H-h+1)
     * @param method           Parameter specifying the comparison method, see TemplateMatchModes
     */
    private static native void matchTemplate_1(
            long image_nativeObj,
            long templ_nativeObj,
            long result_nativeObj,
            int method);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#connectedComponentsWithAlgorithm(Mat,Mat,int,int,int)}.
     *
     * @param image_nativeObj  native matrix or object handle for {@code image}; the 8-bit single-channel image to be
     *                         labeled
     * @param labels_nativeObj native matrix or object handle for {@code labels}; destination labeled image
     * @param connectivity     8 or 4 for 8-way or 4-way connectivity respectively
     * @param ltype            output image label type. Currently CV_32S and CV_16U are supported
     * @param ccltype          connected components algorithm type (see the ConnectedComponentsAlgorithmsTypes)
     * @return the number of labels, including label zero for the background
     */
    private static native int connectedComponentsWithAlgorithm_0(
            long image_nativeObj,
            long labels_nativeObj,
            int connectivity,
            int ltype,
            int ccltype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#connectedComponents(Mat,Mat,int,int)}.
     *
     * @param image_nativeObj  native matrix or object handle for {@code image}; the 8-bit single-channel image to be
     *                         labeled
     * @param labels_nativeObj native matrix or object handle for {@code labels}; destination labeled image
     * @param connectivity     8 or 4 for 8-way or 4-way connectivity respectively
     * @param ltype            output image label type. Currently CV_32S and CV_16U are supported
     * @return the number of labels, including label zero for the background
     */
    private static native int connectedComponents_0(
            long image_nativeObj,
            long labels_nativeObj,
            int connectivity,
            int ltype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#connectedComponents(Mat,Mat,int)}.
     *
     * @param image_nativeObj  native matrix or object handle for {@code image}; the 8-bit single-channel image to be
     *                         labeled
     * @param labels_nativeObj native matrix or object handle for {@code labels}; destination labeled image
     * @param connectivity     8 or 4 for 8-way or 4-way connectivity respectively
     * @return the number of labels, including label zero for the background
     */
    private static native int connectedComponents_1(long image_nativeObj, long labels_nativeObj, int connectivity);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#connectedComponents(Mat,Mat)}.
     *
     * @param image_nativeObj  native matrix or object handle for {@code image}; the 8-bit single-channel image to be
     *                         labeled
     * @param labels_nativeObj native matrix or object handle for {@code labels}; destination labeled image
     * @return the number of labels, including label zero for the background
     */
    private static native int connectedComponents_2(long image_nativeObj, long labels_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#connectedComponentsWithStatsWithAlgorithm(Mat,Mat,Mat,Mat,int,int,int)}.
     *
     * @param image_nativeObj     native matrix or object handle for {@code image}; the 8-bit single-channel image to be
     *                            labeled
     * @param labels_nativeObj    native matrix or object handle for {@code labels}; destination labeled image
     * @param stats_nativeObj     native matrix or object handle for {@code stats}; statistics output for each label,
     *                            including the background label. Statistics are accessed via stats(label, COLUMN) where
     *                            COLUMN is one of ConnectedComponentsTypes, selecting the statistic. The data type is
     *                            CV_32S
     * @param centroids_nativeObj native matrix or object handle for {@code centroids}; centroid output for each label,
     *                            including the background label. Centroids are accessed via centroids(label, 0) for x
     *                            and centroids(label, 1) for y. The data type CV_64F
     * @param connectivity        8 or 4 for 8-way or 4-way connectivity respectively
     * @param ltype               output image label type. Currently CV_32S and CV_16U are supported
     * @param ccltype             connected components algorithm type (see ConnectedComponentsAlgorithmsTypes)
     * @return the number of labels, including label zero for the background
     */
    private static native int connectedComponentsWithStatsWithAlgorithm_0(
            long image_nativeObj,
            long labels_nativeObj,
            long stats_nativeObj,
            long centroids_nativeObj,
            int connectivity,
            int ltype,
            int ccltype);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#connectedComponentsWithStats(Mat,Mat,Mat,Mat,int,int)}.
     *
     * @param image_nativeObj     native matrix or object handle for {@code image}; the 8-bit single-channel image to be
     *                            labeled
     * @param labels_nativeObj    native matrix or object handle for {@code labels}; destination labeled image
     * @param stats_nativeObj     native matrix or object handle for {@code stats}; statistics output for each label,
     *                            including the background label. Statistics are accessed via stats(label, COLUMN) where
     *                            COLUMN is one of ConnectedComponentsTypes, selecting the statistic. The data type is
     *                            CV_32S
     * @param centroids_nativeObj native matrix or object handle for {@code centroids}; centroid output for each label,
     *                            including the background label. Centroids are accessed via centroids(label, 0) for x
     *                            and centroids(label, 1) for y. The data type CV_64F
     * @param connectivity        8 or 4 for 8-way or 4-way connectivity respectively
     * @param ltype               output image label type. Currently CV_32S and CV_16U are supported
     * @return the number of labels, including label zero for the background
     */
    private static native int connectedComponentsWithStats_0(
            long image_nativeObj,
            long labels_nativeObj,
            long stats_nativeObj,
            long centroids_nativeObj,
            int connectivity,
            int ltype);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#connectedComponentsWithStats(Mat,Mat,Mat,Mat,int)}.
     *
     * @param image_nativeObj     native matrix or object handle for {@code image}; the 8-bit single-channel image to be
     *                            labeled
     * @param labels_nativeObj    native matrix or object handle for {@code labels}; destination labeled image
     * @param stats_nativeObj     native matrix or object handle for {@code stats}; statistics output for each label,
     *                            including the background label. Statistics are accessed via stats(label, COLUMN) where
     *                            COLUMN is one of ConnectedComponentsTypes, selecting the statistic. The data type is
     *                            CV_32S
     * @param centroids_nativeObj native matrix or object handle for {@code centroids}; centroid output for each label,
     *                            including the background label. Centroids are accessed via centroids(label, 0) for x
     *                            and centroids(label, 1) for y. The data type CV_64F
     * @param connectivity        8 or 4 for 8-way or 4-way connectivity respectively
     * @return the number of labels, including label zero for the background
     */
    private static native int connectedComponentsWithStats_1(
            long image_nativeObj,
            long labels_nativeObj,
            long stats_nativeObj,
            long centroids_nativeObj,
            int connectivity);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#connectedComponentsWithStats(Mat,Mat,Mat,Mat)}.
     *
     * @param image_nativeObj     native matrix or object handle for {@code image}; the 8-bit single-channel image to be
     *                            labeled
     * @param labels_nativeObj    native matrix or object handle for {@code labels}; destination labeled image
     * @param stats_nativeObj     native matrix or object handle for {@code stats}; statistics output for each label,
     *                            including the background label. Statistics are accessed via stats(label, COLUMN) where
     *                            COLUMN is one of ConnectedComponentsTypes, selecting the statistic. The data type is
     *                            CV_32S
     * @param centroids_nativeObj native matrix or object handle for {@code centroids}; centroid output for each label,
     *                            including the background label. Centroids are accessed via centroids(label, 0) for x
     *                            and centroids(label, 1) for y. The data type CV_64F
     * @return the number of labels, including label zero for the background
     */
    private static native int connectedComponentsWithStats_2(
            long image_nativeObj,
            long labels_nativeObj,
            long stats_nativeObj,
            long centroids_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#findContours(Mat,List<MatOfPoint>,Mat,int,int,Point)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Source, an 8-bit single-channel
     *                               image. Non-zero pixels are treated as 1's. Zero pixels remain 0's, so the image is
     *                               treated as binary . You can use compare, inRange, threshold , adaptiveThreshold,
     *                               Canny, and others to create a binary image out of a grayscale or color one. If mode
     *                               equals to RETR_CCOMP or RETR_FLOODFILL, the input can also be a 32-bit integer
     *                               image of labels (CV_32SC1)
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; Detected contours. Each
     *                               contour is stored as a vector of points (e.g. vector&lt;vector&lt;Point&gt; &gt;)
     * @param hierarchy_nativeObj    native matrix or object handle for {@code hierarchy}; Optional output vector (e.g.
     *                               vector&lt;4-component integer vector&gt;), containing information about the image
     *                               topology. It has as many elements as the number of contours. For each i-th contour
     *                               contours[i], the elements hierarchy[i][0] , hierarchy[i][1] , hierarchy[i][2] , and
     *                               hierarchy[i][3] are set to 0-based indices in contours of the next and previous
     *                               contours at the same hierarchical level, the first child contour and the parent
     *                               contour, respectively. If for the contour i there are no next, previous, parent, or
     *                               nested contours, the corresponding elements of hierarchy[i] will be negative. Note:
     *                               In Python, hierarchy is nested inside a top level array. Use hierarchy[0][i] to
     *                               access hierarchical elements of i-th contour
     * @param mode                   Contour retrieval mode, see RetrievalModes
     * @param method                 Contour approximation method, see ContourApproximationModes
     * @param offset_x               {@code offset.x} component of Optional offset by which every contour point is
     *                               shifted. This is useful if the contours are extracted from the image ROI and then
     *                               they should be analyzed in the whole image context
     * @param offset_y               {@code offset.y} component of Optional offset by which every contour point is
     *                               shifted. This is useful if the contours are extracted from the image ROI and then
     *                               they should be analyzed in the whole image context
     */
    private static native void findContours_0(
            long image_nativeObj,
            long contours_mat_nativeObj,
            long hierarchy_nativeObj,
            int mode,
            int method,
            double offset_x,
            double offset_y);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#findContours(Mat,List<MatOfPoint>,Mat,int,int)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Source, an 8-bit single-channel
     *                               image. Non-zero pixels are treated as 1's. Zero pixels remain 0's, so the image is
     *                               treated as binary . You can use compare, inRange, threshold , adaptiveThreshold,
     *                               Canny, and others to create a binary image out of a grayscale or color one. If mode
     *                               equals to RETR_CCOMP or RETR_FLOODFILL, the input can also be a 32-bit integer
     *                               image of labels (CV_32SC1)
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; Detected contours. Each
     *                               contour is stored as a vector of points (e.g. vector&lt;vector&lt;Point&gt; &gt;)
     * @param hierarchy_nativeObj    native matrix or object handle for {@code hierarchy}; Optional output vector (e.g.
     *                               vector&lt;4-component integer vector&gt;), containing information about the image
     *                               topology. It has as many elements as the number of contours. For each i-th contour
     *                               contours[i], the elements hierarchy[i][0] , hierarchy[i][1] , hierarchy[i][2] , and
     *                               hierarchy[i][3] are set to 0-based indices in contours of the next and previous
     *                               contours at the same hierarchical level, the first child contour and the parent
     *                               contour, respectively. If for the contour i there are no next, previous, parent, or
     *                               nested contours, the corresponding elements of hierarchy[i] will be negative. Note:
     *                               In Python, hierarchy is nested inside a top level array. Use hierarchy[0][i] to
     *                               access hierarchical elements of i-th contour
     * @param mode                   Contour retrieval mode, see RetrievalModes
     * @param method                 Contour approximation method, see ContourApproximationModes
     */
    private static native void findContours_1(
            long image_nativeObj,
            long contours_mat_nativeObj,
            long hierarchy_nativeObj,
            int mode,
            int method);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#findContoursLinkRuns(Mat,List<Mat>,Mat)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; input image
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; collection receiving or
     *                               supplying contour point sequences
     * @param hierarchy_nativeObj    native matrix or object handle for {@code hierarchy}; output matrix describing
     *                               contour parent, child and sibling relationships
     */
    private static native void findContoursLinkRuns_0(
            long image_nativeObj,
            long contours_mat_nativeObj,
            long hierarchy_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#findContoursLinkRuns(Mat,List<Mat>)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; input image
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; collection receiving or
     *                               supplying contour point sequences
     */
    private static native void findContoursLinkRuns_1(long image_nativeObj, long contours_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createGeneralizedHoughBallard()}.
     *
     * @return native binding handle for a new {@code GeneralizedHoughBallard} wrapper configured with the selected
     *         options
     */
    private static native long createGeneralizedHoughBallard_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#createGeneralizedHoughGuil()}.
     *
     * @return native binding handle for a new {@code GeneralizedHoughGuil} wrapper configured with the selected options
     */
    private static native long createGeneralizedHoughGuil_0();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#applyColorMap(Mat,Mat,int)}.
     *
     * @param src_nativeObj native matrix or object handle for {@code src}; The source image, grayscale or colored of
     *                      type CV_8UC1 or CV_8UC3. If CV_8UC3, then the CV_8UC1 image is generated internally using
     *                      COLOR_BGR2GRAY
     * @param dst_nativeObj native matrix or object handle for {@code dst}; The result is the colormapped source image
     *                      of the same number of channels as userColor. Note: Mat.create is called on dst
     * @param colormap      The colormap to apply, see ColormapTypes
     */
    private static native void applyColorMap_0(long src_nativeObj, long dst_nativeObj, int colormap);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#applyColorMap(Mat,Mat,Mat)}.
     *
     * @param src_nativeObj       native matrix or object handle for {@code src}; The source image, grayscale or colored
     *                            of type CV_8UC1 or CV_8UC3. If CV_8UC3, then the CV_8UC1 image is generated internally
     *                            using COLOR_BGR2GRAY
     * @param dst_nativeObj       native matrix or object handle for {@code dst}; The result is the colormapped source
     *                            image of the same number of channels as userColor. Note: Mat.create is called on dst
     * @param userColor_nativeObj native matrix or object handle for {@code userColor}; The colormap to apply of type
     *                            CV_8UC1 or CV_8UC3 and size 256
     */
    private static native void applyColorMap_1(long src_nativeObj, long dst_nativeObj, long userColor_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#line(Mat,Point,Point,Scalar,int,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of First point of the line segment
     * @param pt1_y         {@code pt1.y} component of First point of the line segment
     * @param pt2_x         {@code pt2.x} component of Second point of the line segment
     * @param pt2_y         {@code pt2.y} component of Second point of the line segment
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param thickness     Line thickness
     * @param lineType      Type of the line. See LineTypes
     * @param shift         Number of fractional bits in the point coordinates
     */
    private static native void line_0(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            int shift);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#line(Mat,Point,Point,Scalar,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of First point of the line segment
     * @param pt1_y         {@code pt1.y} component of First point of the line segment
     * @param pt2_x         {@code pt2.x} component of Second point of the line segment
     * @param pt2_y         {@code pt2.y} component of Second point of the line segment
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param thickness     Line thickness
     * @param lineType      Type of the line. See LineTypes
     */
    private static native void line_1(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#line(Mat,Point,Point,Scalar,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of First point of the line segment
     * @param pt1_y         {@code pt1.y} component of First point of the line segment
     * @param pt2_x         {@code pt2.x} component of Second point of the line segment
     * @param pt2_y         {@code pt2.y} component of Second point of the line segment
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param thickness     Line thickness
     */
    private static native void line_2(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#line(Mat,Point,Point,Scalar)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of First point of the line segment
     * @param pt1_y         {@code pt1.y} component of First point of the line segment
     * @param pt2_x         {@code pt2.x} component of Second point of the line segment
     * @param pt2_y         {@code pt2.y} component of Second point of the line segment
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     */
    private static native void line_3(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#arrowedLine(Mat,Point,Point,Scalar,int,int,int,double)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of The point the arrow starts from
     * @param pt1_y         {@code pt1.y} component of The point the arrow starts from
     * @param pt2_x         {@code pt2.x} component of The point the arrow points to
     * @param pt2_y         {@code pt2.y} component of The point the arrow points to
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param thickness     Line thickness
     * @param line_type     Type of the line. See LineTypes
     * @param shift         Number of fractional bits in the point coordinates
     * @param tipLength     The length of the arrow tip in relation to the arrow length
     */
    private static native void arrowedLine_0(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int line_type,
            int shift,
            double tipLength);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#arrowedLine(Mat,Point,Point,Scalar,int,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of The point the arrow starts from
     * @param pt1_y         {@code pt1.y} component of The point the arrow starts from
     * @param pt2_x         {@code pt2.x} component of The point the arrow points to
     * @param pt2_y         {@code pt2.y} component of The point the arrow points to
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param thickness     Line thickness
     * @param line_type     Type of the line. See LineTypes
     * @param shift         Number of fractional bits in the point coordinates
     */
    private static native void arrowedLine_1(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int line_type,
            int shift);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#arrowedLine(Mat,Point,Point,Scalar,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of The point the arrow starts from
     * @param pt1_y         {@code pt1.y} component of The point the arrow starts from
     * @param pt2_x         {@code pt2.x} component of The point the arrow points to
     * @param pt2_y         {@code pt2.y} component of The point the arrow points to
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param thickness     Line thickness
     * @param line_type     Type of the line. See LineTypes
     */
    private static native void arrowedLine_2(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int line_type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#arrowedLine(Mat,Point,Point,Scalar,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of The point the arrow starts from
     * @param pt1_y         {@code pt1.y} component of The point the arrow starts from
     * @param pt2_x         {@code pt2.x} component of The point the arrow points to
     * @param pt2_y         {@code pt2.y} component of The point the arrow points to
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param thickness     Line thickness
     */
    private static native void arrowedLine_3(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#arrowedLine(Mat,Point,Point,Scalar)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of The point the arrow starts from
     * @param pt1_y         {@code pt1.y} component of The point the arrow starts from
     * @param pt2_x         {@code pt2.x} component of The point the arrow points to
     * @param pt2_y         {@code pt2.y} component of The point the arrow points to
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     */
    private static native void arrowedLine_4(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawFrameAxes(Mat,Mat,Mat,Mat,Mat,float,int)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Input/output image. It must have
     *                               1 or 3 channels. The number of channels is not altered
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input 3x3 floating-point
     *                               matrix of camera intrinsic parameters. cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is empty, the zero distortion coefficients
     *                               are assumed
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Translation vector
     * @param length                 Length of the painted axes in the same unit than tvec (usually in meters)
     * @param thickness              Line thickness of the painted axes. This function draws the axes of the
     *                               world/object coordinate system w.r.t. to the camera frame. OX is drawn in red, OY
     *                               in green and OZ in blue
     */
    private static native void drawFrameAxes_0(
            long image_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            float length,
            int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawFrameAxes(Mat,Mat,Mat,Mat,Mat,float)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Input/output image. It must have
     *                               1 or 3 channels. The number of channels is not altered
     * @param cameraMatrix_nativeObj native matrix or object handle for {@code cameraMatrix}; Input 3x3 floating-point
     *                               matrix of camera intrinsic parameters. cameramatrix{A}
     * @param distCoeffs_nativeObj   native matrix or object handle for {@code distCoeffs}; Input vector of distortion
     *                               coefficients distcoeffs. If the vector is empty, the zero distortion coefficients
     *                               are assumed
     * @param rvec_nativeObj         native matrix or object handle for {@code rvec}; Rotation vector (see REF:
     *                               Rodrigues ) that, together with tvec, brings points from the model coordinate
     *                               system to the camera coordinate system
     * @param tvec_nativeObj         native matrix or object handle for {@code tvec}; Translation vector
     * @param length                 Length of the painted axes in the same unit than tvec (usually in meters)
     */
    private static native void drawFrameAxes_1(
            long image_nativeObj,
            long cameraMatrix_nativeObj,
            long distCoeffs_nativeObj,
            long rvec_nativeObj,
            long tvec_nativeObj,
            float length);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#rectangle(Mat,Point,Point,Scalar,int,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of Vertex of the rectangle
     * @param pt1_y         {@code pt1.y} component of Vertex of the rectangle
     * @param pt2_x         {@code pt2.x} component of Vertex of the rectangle opposite to pt1
     * @param pt2_y         {@code pt2.y} component of Vertex of the rectangle opposite to pt1
     * @param color_val0    {@code color.val[0]} component of Rectangle color or brightness (grayscale image)
     * @param color_val1    {@code color.val[1]} component of Rectangle color or brightness (grayscale image)
     * @param color_val2    {@code color.val[2]} component of Rectangle color or brightness (grayscale image)
     * @param color_val3    {@code color.val[3]} component of Rectangle color or brightness (grayscale image)
     * @param thickness     Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                      function has to draw a filled rectangle
     * @param lineType      Type of the line. See LineTypes
     * @param shift         Number of fractional bits in the point coordinates
     */
    private static native void rectangle_0(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            int shift);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#rectangle(Mat,Point,Point,Scalar,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of Vertex of the rectangle
     * @param pt1_y         {@code pt1.y} component of Vertex of the rectangle
     * @param pt2_x         {@code pt2.x} component of Vertex of the rectangle opposite to pt1
     * @param pt2_y         {@code pt2.y} component of Vertex of the rectangle opposite to pt1
     * @param color_val0    {@code color.val[0]} component of Rectangle color or brightness (grayscale image)
     * @param color_val1    {@code color.val[1]} component of Rectangle color or brightness (grayscale image)
     * @param color_val2    {@code color.val[2]} component of Rectangle color or brightness (grayscale image)
     * @param color_val3    {@code color.val[3]} component of Rectangle color or brightness (grayscale image)
     * @param thickness     Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                      function has to draw a filled rectangle
     * @param lineType      Type of the line. See LineTypes
     */
    private static native void rectangle_1(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#rectangle(Mat,Point,Point,Scalar,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of Vertex of the rectangle
     * @param pt1_y         {@code pt1.y} component of Vertex of the rectangle
     * @param pt2_x         {@code pt2.x} component of Vertex of the rectangle opposite to pt1
     * @param pt2_y         {@code pt2.y} component of Vertex of the rectangle opposite to pt1
     * @param color_val0    {@code color.val[0]} component of Rectangle color or brightness (grayscale image)
     * @param color_val1    {@code color.val[1]} component of Rectangle color or brightness (grayscale image)
     * @param color_val2    {@code color.val[2]} component of Rectangle color or brightness (grayscale image)
     * @param color_val3    {@code color.val[3]} component of Rectangle color or brightness (grayscale image)
     * @param thickness     Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                      function has to draw a filled rectangle
     */
    private static native void rectangle_2(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#rectangle(Mat,Point,Point,Scalar)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param pt1_x         {@code pt1.x} component of Vertex of the rectangle
     * @param pt1_y         {@code pt1.y} component of Vertex of the rectangle
     * @param pt2_x         {@code pt2.x} component of Vertex of the rectangle opposite to pt1
     * @param pt2_y         {@code pt2.y} component of Vertex of the rectangle opposite to pt1
     * @param color_val0    {@code color.val[0]} component of Rectangle color or brightness (grayscale image)
     * @param color_val1    {@code color.val[1]} component of Rectangle color or brightness (grayscale image)
     * @param color_val2    {@code color.val[2]} component of Rectangle color or brightness (grayscale image)
     * @param color_val3    {@code color.val[3]} component of Rectangle color or brightness (grayscale image)
     */
    private static native void rectangle_3(
            long img_nativeObj,
            double pt1_x,
            double pt1_y,
            double pt2_x,
            double pt2_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#rectangle(Mat,Rect,Scalar,int,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param rec_x         {@code rec.x} component of rec region
     * @param rec_y         {@code rec.y} component of rec region
     * @param rec_width     {@code rec.width} component of rec region
     * @param rec_height    {@code rec.height} component of rec region
     * @param color_val0    {@code color.val[0]} component of Rectangle color or brightness (grayscale image)
     * @param color_val1    {@code color.val[1]} component of Rectangle color or brightness (grayscale image)
     * @param color_val2    {@code color.val[2]} component of Rectangle color or brightness (grayscale image)
     * @param color_val3    {@code color.val[3]} component of Rectangle color or brightness (grayscale image)
     * @param thickness     Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                      function has to draw a filled rectangle
     * @param lineType      Type of the line. See LineTypes
     * @param shift         Number of fractional bits in the point coordinates
     */
    private static native void rectangle_4(
            long img_nativeObj,
            int rec_x,
            int rec_y,
            int rec_width,
            int rec_height,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            int shift);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#rectangle(Mat,Rect,Scalar,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param rec_x         {@code rec.x} component of rec region
     * @param rec_y         {@code rec.y} component of rec region
     * @param rec_width     {@code rec.width} component of rec region
     * @param rec_height    {@code rec.height} component of rec region
     * @param color_val0    {@code color.val[0]} component of Rectangle color or brightness (grayscale image)
     * @param color_val1    {@code color.val[1]} component of Rectangle color or brightness (grayscale image)
     * @param color_val2    {@code color.val[2]} component of Rectangle color or brightness (grayscale image)
     * @param color_val3    {@code color.val[3]} component of Rectangle color or brightness (grayscale image)
     * @param thickness     Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                      function has to draw a filled rectangle
     * @param lineType      Type of the line. See LineTypes
     */
    private static native void rectangle_5(
            long img_nativeObj,
            int rec_x,
            int rec_y,
            int rec_width,
            int rec_height,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#rectangle(Mat,Rect,Scalar,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param rec_x         {@code rec.x} component of rec region
     * @param rec_y         {@code rec.y} component of rec region
     * @param rec_width     {@code rec.width} component of rec region
     * @param rec_height    {@code rec.height} component of rec region
     * @param color_val0    {@code color.val[0]} component of Rectangle color or brightness (grayscale image)
     * @param color_val1    {@code color.val[1]} component of Rectangle color or brightness (grayscale image)
     * @param color_val2    {@code color.val[2]} component of Rectangle color or brightness (grayscale image)
     * @param color_val3    {@code color.val[3]} component of Rectangle color or brightness (grayscale image)
     * @param thickness     Thickness of lines that make up the rectangle. Negative values, like FILLED, mean that the
     *                      function has to draw a filled rectangle
     */
    private static native void rectangle_6(
            long img_nativeObj,
            int rec_x,
            int rec_y,
            int rec_width,
            int rec_height,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#rectangle(Mat,Rect,Scalar)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param rec_x         {@code rec.x} component of rec region
     * @param rec_y         {@code rec.y} component of rec region
     * @param rec_width     {@code rec.width} component of rec region
     * @param rec_height    {@code rec.height} component of rec region
     * @param color_val0    {@code color.val[0]} component of Rectangle color or brightness (grayscale image)
     * @param color_val1    {@code color.val[1]} component of Rectangle color or brightness (grayscale image)
     * @param color_val2    {@code color.val[2]} component of Rectangle color or brightness (grayscale image)
     * @param color_val3    {@code color.val[3]} component of Rectangle color or brightness (grayscale image)
     */
    private static native void rectangle_7(
            long img_nativeObj,
            int rec_x,
            int rec_y,
            int rec_width,
            int rec_height,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#circle(Mat,Point,int,Scalar,int,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; Image where the circle is drawn
     * @param center_x      {@code center.x} component of Center of the circle
     * @param center_y      {@code center.y} component of Center of the circle
     * @param radius        Radius of the circle
     * @param color_val0    {@code color.val[0]} component of Circle color
     * @param color_val1    {@code color.val[1]} component of Circle color
     * @param color_val2    {@code color.val[2]} component of Circle color
     * @param color_val3    {@code color.val[3]} component of Circle color
     * @param thickness     Thickness of the circle outline, if positive. Negative values, like FILLED, mean that a
     *                      filled circle is to be drawn
     * @param lineType      Type of the circle boundary. See LineTypes
     * @param shift         Number of fractional bits in the coordinates of the center and in the radius value
     */
    private static native void circle_0(
            long img_nativeObj,
            double center_x,
            double center_y,
            int radius,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            int shift);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#circle(Mat,Point,int,Scalar,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; Image where the circle is drawn
     * @param center_x      {@code center.x} component of Center of the circle
     * @param center_y      {@code center.y} component of Center of the circle
     * @param radius        Radius of the circle
     * @param color_val0    {@code color.val[0]} component of Circle color
     * @param color_val1    {@code color.val[1]} component of Circle color
     * @param color_val2    {@code color.val[2]} component of Circle color
     * @param color_val3    {@code color.val[3]} component of Circle color
     * @param thickness     Thickness of the circle outline, if positive. Negative values, like FILLED, mean that a
     *                      filled circle is to be drawn
     * @param lineType      Type of the circle boundary. See LineTypes
     */
    private static native void circle_1(
            long img_nativeObj,
            double center_x,
            double center_y,
            int radius,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#circle(Mat,Point,int,Scalar,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; Image where the circle is drawn
     * @param center_x      {@code center.x} component of Center of the circle
     * @param center_y      {@code center.y} component of Center of the circle
     * @param radius        Radius of the circle
     * @param color_val0    {@code color.val[0]} component of Circle color
     * @param color_val1    {@code color.val[1]} component of Circle color
     * @param color_val2    {@code color.val[2]} component of Circle color
     * @param color_val3    {@code color.val[3]} component of Circle color
     * @param thickness     Thickness of the circle outline, if positive. Negative values, like FILLED, mean that a
     *                      filled circle is to be drawn
     */
    private static native void circle_2(
            long img_nativeObj,
            double center_x,
            double center_y,
            int radius,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#circle(Mat,Point,int,Scalar)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; Image where the circle is drawn
     * @param center_x      {@code center.x} component of Center of the circle
     * @param center_y      {@code center.y} component of Center of the circle
     * @param radius        Radius of the circle
     * @param color_val0    {@code color.val[0]} component of Circle color
     * @param color_val1    {@code color.val[1]} component of Circle color
     * @param color_val2    {@code color.val[2]} component of Circle color
     * @param color_val3    {@code color.val[3]} component of Circle color
     */
    private static native void circle_3(
            long img_nativeObj,
            double center_x,
            double center_y,
            int radius,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#ellipse(Mat,Point,Size,double,double,double,Scalar,int,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param center_x      {@code center.x} component of Center of the ellipse
     * @param center_y      {@code center.y} component of Center of the ellipse
     * @param axes_width    {@code axes.width} component of Half of the size of the ellipse main axes
     * @param axes_height   {@code axes.height} component of Half of the size of the ellipse main axes
     * @param angle         Ellipse rotation angle in degrees
     * @param startAngle    Starting angle of the elliptic arc in degrees
     * @param endAngle      Ending angle of the elliptic arc in degrees
     * @param color_val0    {@code color.val[0]} component of Ellipse color
     * @param color_val1    {@code color.val[1]} component of Ellipse color
     * @param color_val2    {@code color.val[2]} component of Ellipse color
     * @param color_val3    {@code color.val[3]} component of Ellipse color
     * @param thickness     Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                      ellipse sector is to be drawn
     * @param lineType      Type of the ellipse boundary. See LineTypes
     * @param shift         Number of fractional bits in the coordinates of the center and values of axes
     */
    private static native void ellipse_0(
            long img_nativeObj,
            double center_x,
            double center_y,
            double axes_width,
            double axes_height,
            double angle,
            double startAngle,
            double endAngle,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            int shift);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#ellipse(Mat,Point,Size,double,double,double,Scalar,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param center_x      {@code center.x} component of Center of the ellipse
     * @param center_y      {@code center.y} component of Center of the ellipse
     * @param axes_width    {@code axes.width} component of Half of the size of the ellipse main axes
     * @param axes_height   {@code axes.height} component of Half of the size of the ellipse main axes
     * @param angle         Ellipse rotation angle in degrees
     * @param startAngle    Starting angle of the elliptic arc in degrees
     * @param endAngle      Ending angle of the elliptic arc in degrees
     * @param color_val0    {@code color.val[0]} component of Ellipse color
     * @param color_val1    {@code color.val[1]} component of Ellipse color
     * @param color_val2    {@code color.val[2]} component of Ellipse color
     * @param color_val3    {@code color.val[3]} component of Ellipse color
     * @param thickness     Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                      ellipse sector is to be drawn
     * @param lineType      Type of the ellipse boundary. See LineTypes
     */
    private static native void ellipse_1(
            long img_nativeObj,
            double center_x,
            double center_y,
            double axes_width,
            double axes_height,
            double angle,
            double startAngle,
            double endAngle,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#ellipse(Mat,Point,Size,double,double,double,Scalar,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param center_x      {@code center.x} component of Center of the ellipse
     * @param center_y      {@code center.y} component of Center of the ellipse
     * @param axes_width    {@code axes.width} component of Half of the size of the ellipse main axes
     * @param axes_height   {@code axes.height} component of Half of the size of the ellipse main axes
     * @param angle         Ellipse rotation angle in degrees
     * @param startAngle    Starting angle of the elliptic arc in degrees
     * @param endAngle      Ending angle of the elliptic arc in degrees
     * @param color_val0    {@code color.val[0]} component of Ellipse color
     * @param color_val1    {@code color.val[1]} component of Ellipse color
     * @param color_val2    {@code color.val[2]} component of Ellipse color
     * @param color_val3    {@code color.val[3]} component of Ellipse color
     * @param thickness     Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                      ellipse sector is to be drawn
     */
    private static native void ellipse_2(
            long img_nativeObj,
            double center_x,
            double center_y,
            double axes_width,
            double axes_height,
            double angle,
            double startAngle,
            double endAngle,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#ellipse(Mat,Point,Size,double,double,double,Scalar)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param center_x      {@code center.x} component of Center of the ellipse
     * @param center_y      {@code center.y} component of Center of the ellipse
     * @param axes_width    {@code axes.width} component of Half of the size of the ellipse main axes
     * @param axes_height   {@code axes.height} component of Half of the size of the ellipse main axes
     * @param angle         Ellipse rotation angle in degrees
     * @param startAngle    Starting angle of the elliptic arc in degrees
     * @param endAngle      Ending angle of the elliptic arc in degrees
     * @param color_val0    {@code color.val[0]} component of Ellipse color
     * @param color_val1    {@code color.val[1]} component of Ellipse color
     * @param color_val2    {@code color.val[2]} component of Ellipse color
     * @param color_val3    {@code color.val[3]} component of Ellipse color
     */
    private static native void ellipse_3(
            long img_nativeObj,
            double center_x,
            double center_y,
            double axes_width,
            double axes_height,
            double angle,
            double startAngle,
            double endAngle,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#ellipse(Mat,RotatedRect,Scalar,int,int)}.
     *
     * @param img_nativeObj   native matrix or object handle for {@code img}; image to process or annotate
     * @param box_center_x    {@code box.center.x} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_center_y    {@code box.center.y} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_size_width  {@code box.size.width} component of Alternative ellipse representation via RotatedRect.
     *                        This means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_size_height {@code box.size.height} component of Alternative ellipse representation via RotatedRect.
     *                        This means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_angle       {@code box.angle} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param color_val0      {@code color.val[0]} component of Ellipse color
     * @param color_val1      {@code color.val[1]} component of Ellipse color
     * @param color_val2      {@code color.val[2]} component of Ellipse color
     * @param color_val3      {@code color.val[3]} component of Ellipse color
     * @param thickness       Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                        ellipse sector is to be drawn
     * @param lineType        Type of the ellipse boundary. See LineTypes
     */
    private static native void ellipse_4(
            long img_nativeObj,
            double box_center_x,
            double box_center_y,
            double box_size_width,
            double box_size_height,
            double box_angle,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#ellipse(Mat,RotatedRect,Scalar,int)}.
     *
     * @param img_nativeObj   native matrix or object handle for {@code img}; image to process or annotate
     * @param box_center_x    {@code box.center.x} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_center_y    {@code box.center.y} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_size_width  {@code box.size.width} component of Alternative ellipse representation via RotatedRect.
     *                        This means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_size_height {@code box.size.height} component of Alternative ellipse representation via RotatedRect.
     *                        This means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_angle       {@code box.angle} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param color_val0      {@code color.val[0]} component of Ellipse color
     * @param color_val1      {@code color.val[1]} component of Ellipse color
     * @param color_val2      {@code color.val[2]} component of Ellipse color
     * @param color_val3      {@code color.val[3]} component of Ellipse color
     * @param thickness       Thickness of the ellipse arc outline, if positive. Otherwise, this indicates that a filled
     *                        ellipse sector is to be drawn
     */
    private static native void ellipse_5(
            long img_nativeObj,
            double box_center_x,
            double box_center_y,
            double box_size_width,
            double box_size_height,
            double box_angle,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#ellipse(Mat,RotatedRect,Scalar)}.
     *
     * @param img_nativeObj   native matrix or object handle for {@code img}; image to process or annotate
     * @param box_center_x    {@code box.center.x} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_center_y    {@code box.center.y} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_size_width  {@code box.size.width} component of Alternative ellipse representation via RotatedRect.
     *                        This means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_size_height {@code box.size.height} component of Alternative ellipse representation via RotatedRect.
     *                        This means that the function draws an ellipse inscribed in the rotated rectangle
     * @param box_angle       {@code box.angle} component of Alternative ellipse representation via RotatedRect. This
     *                        means that the function draws an ellipse inscribed in the rotated rectangle
     * @param color_val0      {@code color.val[0]} component of Ellipse color
     * @param color_val1      {@code color.val[1]} component of Ellipse color
     * @param color_val2      {@code color.val[2]} component of Ellipse color
     * @param color_val3      {@code color.val[3]} component of Ellipse color
     */
    private static native void ellipse_6(
            long img_nativeObj,
            double box_center_x,
            double box_center_y,
            double box_size_width,
            double box_size_height,
            double box_angle,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawMarker(Mat,Point,Scalar,int,int,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param position_x    {@code position.x} component of The point where the crosshair is positioned
     * @param position_y    {@code position.y} component of The point where the crosshair is positioned
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param markerType    The specific type of marker you want to use, see MarkerTypes
     * @param markerSize    The length of the marker axis [default = 20 pixels]
     * @param thickness     Line thickness
     * @param line_type     Type of the line, See LineTypes
     */
    private static native void drawMarker_0(
            long img_nativeObj,
            double position_x,
            double position_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int markerType,
            int markerSize,
            int thickness,
            int line_type);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawMarker(Mat,Point,Scalar,int,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param position_x    {@code position.x} component of The point where the crosshair is positioned
     * @param position_y    {@code position.y} component of The point where the crosshair is positioned
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param markerType    The specific type of marker you want to use, see MarkerTypes
     * @param markerSize    The length of the marker axis [default = 20 pixels]
     * @param thickness     Line thickness
     */
    private static native void drawMarker_1(
            long img_nativeObj,
            double position_x,
            double position_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int markerType,
            int markerSize,
            int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawMarker(Mat,Point,Scalar,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param position_x    {@code position.x} component of The point where the crosshair is positioned
     * @param position_y    {@code position.y} component of The point where the crosshair is positioned
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param markerType    The specific type of marker you want to use, see MarkerTypes
     * @param markerSize    The length of the marker axis [default = 20 pixels]
     */
    private static native void drawMarker_2(
            long img_nativeObj,
            double position_x,
            double position_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int markerType,
            int markerSize);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawMarker(Mat,Point,Scalar,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param position_x    {@code position.x} component of The point where the crosshair is positioned
     * @param position_y    {@code position.y} component of The point where the crosshair is positioned
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     * @param markerType    The specific type of marker you want to use, see MarkerTypes
     */
    private static native void drawMarker_3(
            long img_nativeObj,
            double position_x,
            double position_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int markerType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawMarker(Mat,Point,Scalar)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param position_x    {@code position.x} component of The point where the crosshair is positioned
     * @param position_y    {@code position.y} component of The point where the crosshair is positioned
     * @param color_val0    {@code color.val[0]} component of Line color
     * @param color_val1    {@code color.val[1]} component of Line color
     * @param color_val2    {@code color.val[2]} component of Line color
     * @param color_val3    {@code color.val[3]} component of Line color
     */
    private static native void drawMarker_4(
            long img_nativeObj,
            double position_x,
            double position_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#fillConvexPoly(Mat,MatOfPoint,Scalar,int,int)}.
     *
     * @param img_nativeObj        native matrix or object handle for {@code img}; image to process or annotate
     * @param points_mat_nativeObj native matrix or object handle for {@code points_mat}; Polygon vertices
     * @param color_val0           {@code color.val[0]} component of Polygon color
     * @param color_val1           {@code color.val[1]} component of Polygon color
     * @param color_val2           {@code color.val[2]} component of Polygon color
     * @param color_val3           {@code color.val[3]} component of Polygon color
     * @param lineType             Type of the polygon boundaries. See LineTypes
     * @param shift                Number of fractional bits in the vertex coordinates
     */
    private static native void fillConvexPoly_0(
            long img_nativeObj,
            long points_mat_nativeObj,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int lineType,
            int shift);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#fillConvexPoly(Mat,MatOfPoint,Scalar,int)}.
     *
     * @param img_nativeObj        native matrix or object handle for {@code img}; image to process or annotate
     * @param points_mat_nativeObj native matrix or object handle for {@code points_mat}; Polygon vertices
     * @param color_val0           {@code color.val[0]} component of Polygon color
     * @param color_val1           {@code color.val[1]} component of Polygon color
     * @param color_val2           {@code color.val[2]} component of Polygon color
     * @param color_val3           {@code color.val[3]} component of Polygon color
     * @param lineType             Type of the polygon boundaries. See LineTypes
     */
    private static native void fillConvexPoly_1(
            long img_nativeObj,
            long points_mat_nativeObj,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int lineType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#fillConvexPoly(Mat,MatOfPoint,Scalar)}.
     *
     * @param img_nativeObj        native matrix or object handle for {@code img}; image to process or annotate
     * @param points_mat_nativeObj native matrix or object handle for {@code points_mat}; Polygon vertices
     * @param color_val0           {@code color.val[0]} component of Polygon color
     * @param color_val1           {@code color.val[1]} component of Polygon color
     * @param color_val2           {@code color.val[2]} component of Polygon color
     * @param color_val3           {@code color.val[3]} component of Polygon color
     */
    private static native void fillConvexPoly_2(
            long img_nativeObj,
            long points_mat_nativeObj,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#fillPoly(Mat,List<MatOfPoint>,Scalar,int,int,Point)}.
     *
     * @param img_nativeObj     native matrix or object handle for {@code img}; image to process or annotate
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Array of polygons where each polygon
     *                          is represented as an array of points
     * @param color_val0        {@code color.val[0]} component of Polygon color
     * @param color_val1        {@code color.val[1]} component of Polygon color
     * @param color_val2        {@code color.val[2]} component of Polygon color
     * @param color_val3        {@code color.val[3]} component of Polygon color
     * @param lineType          Type of the polygon boundaries. See LineTypes
     * @param shift             Number of fractional bits in the vertex coordinates
     * @param offset_x          {@code offset.x} component of Optional offset of all points of the contours
     * @param offset_y          {@code offset.y} component of Optional offset of all points of the contours
     */
    private static native void fillPoly_0(
            long img_nativeObj,
            long pts_mat_nativeObj,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int lineType,
            int shift,
            double offset_x,
            double offset_y);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#fillPoly(Mat,List<MatOfPoint>,Scalar,int,int)}.
     *
     * @param img_nativeObj     native matrix or object handle for {@code img}; image to process or annotate
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Array of polygons where each polygon
     *                          is represented as an array of points
     * @param color_val0        {@code color.val[0]} component of Polygon color
     * @param color_val1        {@code color.val[1]} component of Polygon color
     * @param color_val2        {@code color.val[2]} component of Polygon color
     * @param color_val3        {@code color.val[3]} component of Polygon color
     * @param lineType          Type of the polygon boundaries. See LineTypes
     * @param shift             Number of fractional bits in the vertex coordinates
     */
    private static native void fillPoly_1(
            long img_nativeObj,
            long pts_mat_nativeObj,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int lineType,
            int shift);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#fillPoly(Mat,List<MatOfPoint>,Scalar,int)}.
     *
     * @param img_nativeObj     native matrix or object handle for {@code img}; image to process or annotate
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Array of polygons where each polygon
     *                          is represented as an array of points
     * @param color_val0        {@code color.val[0]} component of Polygon color
     * @param color_val1        {@code color.val[1]} component of Polygon color
     * @param color_val2        {@code color.val[2]} component of Polygon color
     * @param color_val3        {@code color.val[3]} component of Polygon color
     * @param lineType          Type of the polygon boundaries. See LineTypes
     */
    private static native void fillPoly_2(
            long img_nativeObj,
            long pts_mat_nativeObj,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int lineType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#fillPoly(Mat,List<MatOfPoint>,Scalar)}.
     *
     * @param img_nativeObj     native matrix or object handle for {@code img}; image to process or annotate
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Array of polygons where each polygon
     *                          is represented as an array of points
     * @param color_val0        {@code color.val[0]} component of Polygon color
     * @param color_val1        {@code color.val[1]} component of Polygon color
     * @param color_val2        {@code color.val[2]} component of Polygon color
     * @param color_val3        {@code color.val[3]} component of Polygon color
     */
    private static native void fillPoly_3(
            long img_nativeObj,
            long pts_mat_nativeObj,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#polylines(Mat,List<MatOfPoint>,boolean,Scalar,int,int,int)}.
     *
     * @param img_nativeObj     native matrix or object handle for {@code img}; image to process or annotate
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Array of polygonal curves
     * @param isClosed          Flag indicating whether the drawn polylines are closed or not. If they are closed, the
     *                          function draws a line from the last vertex of each curve to its first vertex
     * @param color_val0        {@code color.val[0]} component of Polyline color
     * @param color_val1        {@code color.val[1]} component of Polyline color
     * @param color_val2        {@code color.val[2]} component of Polyline color
     * @param color_val3        {@code color.val[3]} component of Polyline color
     * @param thickness         Thickness of the polyline edges
     * @param lineType          Type of the line segments. See LineTypes
     * @param shift             Number of fractional bits in the vertex coordinates. The function polylines draws one or
     *                          more polygonal curves
     */
    private static native void polylines_0(
            long img_nativeObj,
            long pts_mat_nativeObj,
            boolean isClosed,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            int shift);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#polylines(Mat,List<MatOfPoint>,boolean,Scalar,int,int)}.
     *
     * @param img_nativeObj     native matrix or object handle for {@code img}; image to process or annotate
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Array of polygonal curves
     * @param isClosed          Flag indicating whether the drawn polylines are closed or not. If they are closed, the
     *                          function draws a line from the last vertex of each curve to its first vertex
     * @param color_val0        {@code color.val[0]} component of Polyline color
     * @param color_val1        {@code color.val[1]} component of Polyline color
     * @param color_val2        {@code color.val[2]} component of Polyline color
     * @param color_val3        {@code color.val[3]} component of Polyline color
     * @param thickness         Thickness of the polyline edges
     * @param lineType          Type of the line segments. See LineTypes
     */
    private static native void polylines_1(
            long img_nativeObj,
            long pts_mat_nativeObj,
            boolean isClosed,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#polylines(Mat,List<MatOfPoint>,boolean,Scalar,int)}.
     *
     * @param img_nativeObj     native matrix or object handle for {@code img}; image to process or annotate
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Array of polygonal curves
     * @param isClosed          Flag indicating whether the drawn polylines are closed or not. If they are closed, the
     *                          function draws a line from the last vertex of each curve to its first vertex
     * @param color_val0        {@code color.val[0]} component of Polyline color
     * @param color_val1        {@code color.val[1]} component of Polyline color
     * @param color_val2        {@code color.val[2]} component of Polyline color
     * @param color_val3        {@code color.val[3]} component of Polyline color
     * @param thickness         Thickness of the polyline edges
     */
    private static native void polylines_2(
            long img_nativeObj,
            long pts_mat_nativeObj,
            boolean isClosed,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#polylines(Mat,List<MatOfPoint>,boolean,Scalar)}.
     *
     * @param img_nativeObj     native matrix or object handle for {@code img}; image to process or annotate
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Array of polygonal curves
     * @param isClosed          Flag indicating whether the drawn polylines are closed or not. If they are closed, the
     *                          function draws a line from the last vertex of each curve to its first vertex
     * @param color_val0        {@code color.val[0]} component of Polyline color
     * @param color_val1        {@code color.val[1]} component of Polyline color
     * @param color_val2        {@code color.val[2]} component of Polyline color
     * @param color_val3        {@code color.val[3]} component of Polyline color
     */
    private static native void polylines_3(
            long img_nativeObj,
            long pts_mat_nativeObj,
            boolean isClosed,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawContours(Mat,List<MatOfPoint>,int,Scalar,int,int,Mat,int,Point)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Destination image
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; All the input contours.
     *                               Each contour is stored as a point vector
     * @param contourIdx             Parameter indicating a contour to draw. If it is negative, all the contours are
     *                               drawn
     * @param color_val0             {@code color.val[0]} component of Color of the contours
     * @param color_val1             {@code color.val[1]} component of Color of the contours
     * @param color_val2             {@code color.val[2]} component of Color of the contours
     * @param color_val3             {@code color.val[3]} component of Color of the contours
     * @param thickness              Thickness of lines the contours are drawn with. If it is negative (for example,
     *                               thickness=FILLED ), the contour interiors are drawn
     * @param lineType               Line connectivity. See LineTypes
     * @param hierarchy_nativeObj    native matrix or object handle for {@code hierarchy}; Optional information about
     *                               hierarchy. It is only needed if you want to draw only some of the contours (see
     *                               maxLevel )
     * @param maxLevel               Maximal level for drawn contours. If it is 0, only the specified contour is drawn.
     *                               If it is 1, the function draws the contour(s) and all the nested contours. If it is
     *                               2, the function draws the contours, all the nested contours, all the
     *                               nested-to-nested contours, and so on. This parameter is only taken into account
     *                               when there is hierarchy available
     * @param offset_x               {@code offset.x} component of Optional contour shift parameter. Shift all the drawn
     *                               contours by the specified {@code offset}=(dx,dy) . Note: When thickness=FILLED, the
     *                               function is designed to handle connected components with holes correctly even when
     *                               no hierarchy data is provided. This is done by analyzing all the outlines together
     *                               using even-odd rule. This may give incorrect results if you have a joint collection
     *                               of separately retrieved contours. In order to solve this problem, you need to call
     *                               drawContours separately for each sub-group of contours, or iterate over the
     *                               collection using contourIdx parameter
     * @param offset_y               {@code offset.y} component of Optional contour shift parameter. Shift all the drawn
     *                               contours by the specified {@code offset}=(dx,dy) . Note: When thickness=FILLED, the
     *                               function is designed to handle connected components with holes correctly even when
     *                               no hierarchy data is provided. This is done by analyzing all the outlines together
     *                               using even-odd rule. This may give incorrect results if you have a joint collection
     *                               of separately retrieved contours. In order to solve this problem, you need to call
     *                               drawContours separately for each sub-group of contours, or iterate over the
     *                               collection using contourIdx parameter
     */
    private static native void drawContours_0(
            long image_nativeObj,
            long contours_mat_nativeObj,
            int contourIdx,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            long hierarchy_nativeObj,
            int maxLevel,
            double offset_x,
            double offset_y);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawContours(Mat,List<MatOfPoint>,int,Scalar,int,int,Mat,int)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Destination image
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; All the input contours.
     *                               Each contour is stored as a point vector
     * @param contourIdx             Parameter indicating a contour to draw. If it is negative, all the contours are
     *                               drawn
     * @param color_val0             {@code color.val[0]} component of Color of the contours
     * @param color_val1             {@code color.val[1]} component of Color of the contours
     * @param color_val2             {@code color.val[2]} component of Color of the contours
     * @param color_val3             {@code color.val[3]} component of Color of the contours
     * @param thickness              Thickness of lines the contours are drawn with. If it is negative (for example,
     *                               thickness=FILLED ), the contour interiors are drawn
     * @param lineType               Line connectivity. See LineTypes
     * @param hierarchy_nativeObj    native matrix or object handle for {@code hierarchy}; Optional information about
     *                               hierarchy. It is only needed if you want to draw only some of the contours (see
     *                               maxLevel )
     * @param maxLevel               Maximal level for drawn contours. If it is 0, only the specified contour is drawn.
     *                               If it is 1, the function draws the contour(s) and all the nested contours. If it is
     *                               2, the function draws the contours, all the nested contours, all the
     *                               nested-to-nested contours, and so on. This parameter is only taken into account
     *                               when there is hierarchy available
     */
    private static native void drawContours_1(
            long image_nativeObj,
            long contours_mat_nativeObj,
            int contourIdx,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            long hierarchy_nativeObj,
            int maxLevel);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawContours(Mat,List<MatOfPoint>,int,Scalar,int,int,Mat)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Destination image
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; All the input contours.
     *                               Each contour is stored as a point vector
     * @param contourIdx             Parameter indicating a contour to draw. If it is negative, all the contours are
     *                               drawn
     * @param color_val0             {@code color.val[0]} component of Color of the contours
     * @param color_val1             {@code color.val[1]} component of Color of the contours
     * @param color_val2             {@code color.val[2]} component of Color of the contours
     * @param color_val3             {@code color.val[3]} component of Color of the contours
     * @param thickness              Thickness of lines the contours are drawn with. If it is negative (for example,
     *                               thickness=FILLED ), the contour interiors are drawn
     * @param lineType               Line connectivity. See LineTypes
     * @param hierarchy_nativeObj    native matrix or object handle for {@code hierarchy}; Optional information about
     *                               hierarchy. It is only needed if you want to draw only some of the contours (see
     *                               maxLevel )
     */
    private static native void drawContours_2(
            long image_nativeObj,
            long contours_mat_nativeObj,
            int contourIdx,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            long hierarchy_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawContours(Mat,List<MatOfPoint>,int,Scalar,int,int)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Destination image
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; All the input contours.
     *                               Each contour is stored as a point vector
     * @param contourIdx             Parameter indicating a contour to draw. If it is negative, all the contours are
     *                               drawn
     * @param color_val0             {@code color.val[0]} component of Color of the contours
     * @param color_val1             {@code color.val[1]} component of Color of the contours
     * @param color_val2             {@code color.val[2]} component of Color of the contours
     * @param color_val3             {@code color.val[3]} component of Color of the contours
     * @param thickness              Thickness of lines the contours are drawn with. If it is negative (for example,
     *                               thickness=FILLED ), the contour interiors are drawn
     * @param lineType               Line connectivity. See LineTypes
     */
    private static native void drawContours_3(
            long image_nativeObj,
            long contours_mat_nativeObj,
            int contourIdx,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawContours(Mat,List<MatOfPoint>,int,Scalar,int)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Destination image
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; All the input contours.
     *                               Each contour is stored as a point vector
     * @param contourIdx             Parameter indicating a contour to draw. If it is negative, all the contours are
     *                               drawn
     * @param color_val0             {@code color.val[0]} component of Color of the contours
     * @param color_val1             {@code color.val[1]} component of Color of the contours
     * @param color_val2             {@code color.val[2]} component of Color of the contours
     * @param color_val3             {@code color.val[3]} component of Color of the contours
     * @param thickness              Thickness of lines the contours are drawn with. If it is negative (for example,
     *                               thickness=FILLED ), the contour interiors are drawn
     */
    private static native void drawContours_4(
            long image_nativeObj,
            long contours_mat_nativeObj,
            int contourIdx,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#drawContours(Mat,List<MatOfPoint>,int,Scalar)}.
     *
     * @param image_nativeObj        native matrix or object handle for {@code image}; Destination image
     * @param contours_mat_nativeObj native matrix or object handle for {@code contours_mat}; All the input contours.
     *                               Each contour is stored as a point vector
     * @param contourIdx             Parameter indicating a contour to draw. If it is negative, all the contours are
     *                               drawn
     * @param color_val0             {@code color.val[0]} component of Color of the contours
     * @param color_val1             {@code color.val[1]} component of Color of the contours
     * @param color_val2             {@code color.val[2]} component of Color of the contours
     * @param color_val3             {@code color.val[3]} component of Color of the contours
     */
    private static native void drawContours_5(
            long image_nativeObj,
            long contours_mat_nativeObj,
            int contourIdx,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#clipLine(Rect,Point,Point)}.
     *
     * @param imgRect_x      {@code imgRect.x} component of Image rectangle
     * @param imgRect_y      {@code imgRect.y} component of Image rectangle
     * @param imgRect_width  {@code imgRect.width} component of Image rectangle
     * @param imgRect_height {@code imgRect.height} component of Image rectangle
     * @param pt1_x          {@code pt1.x} component of First line point
     * @param pt1_y          {@code pt1.y} component of First line point
     * @param pt1_out        output array receiving {@code pt1} from the native call
     * @param pt2_x          {@code pt2.x} component of Second line point
     * @param pt2_y          {@code pt2.y} component of Second line point
     * @param pt2_out        output array receiving {@code pt2} from the native call
     * @return true if the clipped line intersects the image rectangle
     */
    private static native boolean clipLine_0(
            int imgRect_x,
            int imgRect_y,
            int imgRect_width,
            int imgRect_height,
            double pt1_x,
            double pt1_y,
            double[] pt1_out,
            double pt2_x,
            double pt2_y,
            double[] pt2_out);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#ellipse2Poly(Point,Size,int,int,int,int,MatOfPoint)}.
     *
     * @param center_x          {@code center.x} component of Center of the arc
     * @param center_y          {@code center.y} component of Center of the arc
     * @param axes_width        {@code axes.width} component of Half of the size of the ellipse main axes. See ellipse
     *                          for details
     * @param axes_height       {@code axes.height} component of Half of the size of the ellipse main axes. See ellipse
     *                          for details
     * @param angle             Rotation angle of the ellipse in degrees. See ellipse for details
     * @param arcStart          Starting angle of the elliptic arc in degrees
     * @param arcEnd            Ending angle of the elliptic arc in degrees
     * @param delta             Angle between the subsequent polyline vertices. It defines the approximation accuracy
     * @param pts_mat_nativeObj native matrix or object handle for {@code pts_mat}; Output vector of polyline vertices
     */
    private static native void ellipse2Poly_0(
            double center_x,
            double center_y,
            double axes_width,
            double axes_height,
            int angle,
            int arcStart,
            int arcEnd,
            int delta,
            long pts_mat_nativeObj);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#putText(Mat,String,Point,int,double,Scalar,int,int,boolean)}.
     *
     * @param img_nativeObj    native matrix or object handle for {@code img}; image to process or annotate
     * @param text             Text string to be drawn
     * @param org_x            {@code org.x} component of Bottom-left corner of the first character of the printed text
     *                         (see PUT_TEXT_ALIGN_... though)
     * @param org_y            {@code org.y} component of Bottom-left corner of the first character of the printed text
     *                         (see PUT_TEXT_ALIGN_... though)
     * @param fontFace         Font type, see HersheyFonts
     * @param fontScale        Font scale factor that is multiplied by the font-specific base size
     * @param color_val0       {@code color.val[0]} component of Text color
     * @param color_val1       {@code color.val[1]} component of Text color
     * @param color_val2       {@code color.val[2]} component of Text color
     * @param color_val3       {@code color.val[3]} component of Text color
     * @param thickness        Thickness of the lines used to draw a text
     * @param lineType         Line type. See LineTypes
     * @param bottomLeftOrigin When true, the image data origin is at the bottom-left corner. Otherwise, it is at the
     *                         top-left corner
     */
    private static native void putText_0(
            long img_nativeObj,
            String text,
            double org_x,
            double org_y,
            int fontFace,
            double fontScale,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType,
            boolean bottomLeftOrigin);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#putText(Mat,String,Point,int,double,Scalar,int,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param text          Text string to be drawn
     * @param org_x         {@code org.x} component of Bottom-left corner of the first character of the printed text
     *                      (see PUT_TEXT_ALIGN_... though)
     * @param org_y         {@code org.y} component of Bottom-left corner of the first character of the printed text
     *                      (see PUT_TEXT_ALIGN_... though)
     * @param fontFace      Font type, see HersheyFonts
     * @param fontScale     Font scale factor that is multiplied by the font-specific base size
     * @param color_val0    {@code color.val[0]} component of Text color
     * @param color_val1    {@code color.val[1]} component of Text color
     * @param color_val2    {@code color.val[2]} component of Text color
     * @param color_val3    {@code color.val[3]} component of Text color
     * @param thickness     Thickness of the lines used to draw a text
     * @param lineType      Line type. See LineTypes it is at the top-left corner
     */
    private static native void putText_1(
            long img_nativeObj,
            String text,
            double org_x,
            double org_y,
            int fontFace,
            double fontScale,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness,
            int lineType);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#putText(Mat,String,Point,int,double,Scalar,int)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param text          Text string to be drawn
     * @param org_x         {@code org.x} component of Bottom-left corner of the first character of the printed text
     *                      (see PUT_TEXT_ALIGN_... though)
     * @param org_y         {@code org.y} component of Bottom-left corner of the first character of the printed text
     *                      (see PUT_TEXT_ALIGN_... though)
     * @param fontFace      Font type, see HersheyFonts
     * @param fontScale     Font scale factor that is multiplied by the font-specific base size
     * @param color_val0    {@code color.val[0]} component of Text color
     * @param color_val1    {@code color.val[1]} component of Text color
     * @param color_val2    {@code color.val[2]} component of Text color
     * @param color_val3    {@code color.val[3]} component of Text color
     * @param thickness     Thickness of the lines used to draw a text. it is at the top-left corner
     */
    private static native void putText_2(
            long img_nativeObj,
            String text,
            double org_x,
            double org_y,
            int fontFace,
            double fontScale,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            int thickness);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#putText(Mat,String,Point,int,double,Scalar)}.
     *
     * @param img_nativeObj native matrix or object handle for {@code img}; image to process or annotate
     * @param text          Text string to be drawn
     * @param org_x         {@code org.x} component of Bottom-left corner of the first character of the printed text
     *                      (see PUT_TEXT_ALIGN_... though)
     * @param org_y         {@code org.y} component of Bottom-left corner of the first character of the printed text
     *                      (see PUT_TEXT_ALIGN_... though)
     * @param fontFace      Font type, see HersheyFonts
     * @param fontScale     Font scale factor that is multiplied by the font-specific base size
     * @param color_val0    {@code color.val[0]} component of Text color
     * @param color_val1    {@code color.val[1]} component of Text color
     * @param color_val2    {@code color.val[2]} component of Text color
     * @param color_val3    {@code color.val[3]} component of Text color
     */
    private static native void putText_3(
            long img_nativeObj,
            String text,
            double org_x,
            double org_y,
            int fontFace,
            double fontScale,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getFontScaleFromHeight(int,int,int)}.
     *
     * @param fontFace    Font to use, see HersheyFonts
     * @param pixelHeight Pixel height to compute the fontScale for
     * @param thickness   Thickness of lines used to render the text.See putText for details
     * @return The fontSize to use for putText
     */
    private static native double getFontScaleFromHeight_0(int fontFace, int pixelHeight, int thickness);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getFontScaleFromHeight(int,int)}.
     *
     * @param fontFace    Font to use, see HersheyFonts
     * @param pixelHeight Pixel height to compute the fontScale for
     * @return The fontSize to use for putText
     */
    private static native double getFontScaleFromHeight_1(int fontFace, int pixelHeight);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#putText(Mat,String,Point,Scalar,FontFace,int,int,int,Range)}.
     *
     * @param img_nativeObj   native matrix or object handle for {@code img}; image to process or annotate
     * @param text            Text string to be drawn
     * @param org_x           {@code org.x} component of Bottom-left corner of the first character of the printed text
     *                        (see PUT_TEXT_ALIGN_... though)
     * @param org_y           {@code org.y} component of Bottom-left corner of the first character of the printed text
     *                        (see PUT_TEXT_ALIGN_... though)
     * @param color_val0      {@code color.val[0]} component of Text color
     * @param color_val1      {@code color.val[1]} component of Text color
     * @param color_val2      {@code color.val[2]} component of Text color
     * @param color_val3      {@code color.val[3]} component of Text color
     * @param fface_nativeObj {@code fface.getNativeObjAddr()} component of The font to use for the text
     * @param size            Font size in pixels (by default) or pts
     * @param weight          Font weight, 100..1000, where 100 is "thin" font, 400 is "regular", 600 is "semibold", 800
     *                        is "bold" and beyond that is "black". The parameter is ignored if the font is not a
     *                        variable font or if it does not provide variation along 'wght' axis. If the weight is 0,
     *                        then the weight, currently set via setInstance(), is used
     * @param flags           Various flags, see PUT_TEXT_
     * @param wrap_start      {@code wrap.start} component of The optional text wrapping range: In the case of
     *                        left-to-right (LTR) text if the printed character would cross wrap.end boundary, the
     *                        "cursor" is set to wrap.start. In the case of right-to-left (RTL) text it's vice versa. If
     *                        the parameters is not set, [org.x, img.cols] is used for LTR text and [0, org.x] is for
     *                        RTL one
     * @param wrap_end        {@code wrap.end} component of The optional text wrapping range: In the case of
     *                        left-to-right (LTR) text if the printed character would cross wrap.end boundary, the
     *                        "cursor" is set to wrap.start. In the case of right-to-left (RTL) text it's vice versa. If
     *                        the parameters is not set, [org.x, img.cols] is used for LTR text and [0, org.x] is for
     *                        RTL one
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] putText_4(
            long img_nativeObj,
            String text,
            double org_x,
            double org_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            long fface_nativeObj,
            int size,
            int weight,
            int flags,
            int wrap_start,
            int wrap_end);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#putText(Mat,String,Point,Scalar,FontFace,int,int,int)}.
     *
     * @param img_nativeObj   native matrix or object handle for {@code img}; image to process or annotate
     * @param text            Text string to be drawn
     * @param org_x           {@code org.x} component of Bottom-left corner of the first character of the printed text
     *                        (see PUT_TEXT_ALIGN_... though)
     * @param org_y           {@code org.y} component of Bottom-left corner of the first character of the printed text
     *                        (see PUT_TEXT_ALIGN_... though)
     * @param color_val0      {@code color.val[0]} component of Text color
     * @param color_val1      {@code color.val[1]} component of Text color
     * @param color_val2      {@code color.val[2]} component of Text color
     * @param color_val3      {@code color.val[3]} component of Text color
     * @param fface_nativeObj {@code fface.getNativeObjAddr()} component of The font to use for the text
     * @param size            Font size in pixels (by default) or pts
     * @param weight          Font weight, 100..1000, where 100 is "thin" font, 400 is "regular", 600 is "semibold", 800
     *                        is "bold" and beyond that is "black". The parameter is ignored if the font is not a
     *                        variable font or if it does not provide variation along 'wght' axis. If the weight is 0,
     *                        then the weight, currently set via setInstance(), is used
     * @param flags           Various flags, see PUT_TEXT_
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] putText_5(
            long img_nativeObj,
            String text,
            double org_x,
            double org_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            long fface_nativeObj,
            int size,
            int weight,
            int flags);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#putText(Mat,String,Point,Scalar,FontFace,int,int)}.
     *
     * @param img_nativeObj   native matrix or object handle for {@code img}; image to process or annotate
     * @param text            Text string to be drawn
     * @param org_x           {@code org.x} component of Bottom-left corner of the first character of the printed text
     *                        (see PUT_TEXT_ALIGN_... though)
     * @param org_y           {@code org.y} component of Bottom-left corner of the first character of the printed text
     *                        (see PUT_TEXT_ALIGN_... though)
     * @param color_val0      {@code color.val[0]} component of Text color
     * @param color_val1      {@code color.val[1]} component of Text color
     * @param color_val2      {@code color.val[2]} component of Text color
     * @param color_val3      {@code color.val[3]} component of Text color
     * @param fface_nativeObj {@code fface.getNativeObjAddr()} component of The font to use for the text
     * @param size            Font size in pixels (by default) or pts
     * @param weight          Font weight, 100..1000, where 100 is "thin" font, 400 is "regular", 600 is "semibold", 800
     *                        is "bold" and beyond that is "black". The parameter is ignored if the font is not a
     *                        variable font or if it does not provide variation along 'wght' axis. If the weight is 0,
     *                        then the weight, currently set via setInstance(), is used
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] putText_6(
            long img_nativeObj,
            String text,
            double org_x,
            double org_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            long fface_nativeObj,
            int size,
            int weight);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#putText(Mat,String,Point,Scalar,FontFace,int)}.
     *
     * @param img_nativeObj   native matrix or object handle for {@code img}; image to process or annotate
     * @param text            Text string to be drawn
     * @param org_x           {@code org.x} component of Bottom-left corner of the first character of the printed text
     *                        (see PUT_TEXT_ALIGN_... though)
     * @param org_y           {@code org.y} component of Bottom-left corner of the first character of the printed text
     *                        (see PUT_TEXT_ALIGN_... though)
     * @param color_val0      {@code color.val[0]} component of Text color
     * @param color_val1      {@code color.val[1]} component of Text color
     * @param color_val2      {@code color.val[2]} component of Text color
     * @param color_val3      {@code color.val[3]} component of Text color
     * @param fface_nativeObj {@code fface.getNativeObjAddr()} component of The font to use for the text
     * @param size            Font size in pixels (by default) or pts
     * @return packed numeric fields used by the Java wrapper to construct {@code Point}
     */
    private static native double[] putText_7(
            long img_nativeObj,
            String text,
            double org_x,
            double org_y,
            double color_val0,
            double color_val1,
            double color_val2,
            double color_val3,
            long fface_nativeObj,
            int size);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesWithAccumulator(Mat,Mat,double,double,int,double,double,double,double,boolean)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; input image
     * @param lines_nativeObj native matrix or object handle for {@code lines}; matrix receiving or containing detected
     *                        line segments
     * @param rho             distance resolution of the line accumulator, in pixels
     * @param theta           angular resolution of the line accumulator, in radians
     * @param threshold       minimum accumulator vote threshold for retaining a detected line
     * @param srn             distance-resolution divisor for the multiscale Hough transform
     * @param stn             angle-resolution divisor for the multiscale Hough transform
     * @param min_theta       minimum line angle searched, in radians
     * @param max_theta       maximum line angle searched, in radians
     * @param use_edgeval     whether edgeval is enabled
     */
    private static native void HoughLinesWithAccumulator_0(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta,
            double max_theta,
            boolean use_edgeval);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesWithAccumulator(Mat,Mat,double,double,int,double,double,double,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; input image
     * @param lines_nativeObj native matrix or object handle for {@code lines}; matrix receiving or containing detected
     *                        line segments
     * @param rho             distance resolution of the line accumulator, in pixels
     * @param theta           angular resolution of the line accumulator, in radians
     * @param threshold       minimum accumulator vote threshold for retaining a detected line
     * @param srn             distance-resolution divisor for the multiscale Hough transform
     * @param stn             angle-resolution divisor for the multiscale Hough transform
     * @param min_theta       minimum line angle searched, in radians
     * @param max_theta       maximum line angle searched, in radians
     */
    private static native void HoughLinesWithAccumulator_1(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta,
            double max_theta);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesWithAccumulator(Mat,Mat,double,double,int,double,double,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; input image
     * @param lines_nativeObj native matrix or object handle for {@code lines}; matrix receiving or containing detected
     *                        line segments
     * @param rho             distance resolution of the line accumulator, in pixels
     * @param theta           angular resolution of the line accumulator, in radians
     * @param threshold       minimum accumulator vote threshold for retaining a detected line
     * @param srn             distance-resolution divisor for the multiscale Hough transform
     * @param stn             angle-resolution divisor for the multiscale Hough transform
     * @param min_theta       minimum line angle searched, in radians
     */
    private static native void HoughLinesWithAccumulator_2(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn,
            double min_theta);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesWithAccumulator(Mat,Mat,double,double,int,double,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; input image
     * @param lines_nativeObj native matrix or object handle for {@code lines}; matrix receiving or containing detected
     *                        line segments
     * @param rho             distance resolution of the line accumulator, in pixels
     * @param theta           angular resolution of the line accumulator, in radians
     * @param threshold       minimum accumulator vote threshold for retaining a detected line
     * @param srn             distance-resolution divisor for the multiscale Hough transform
     * @param stn             angle-resolution divisor for the multiscale Hough transform
     */
    private static native void HoughLinesWithAccumulator_3(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn,
            double stn);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesWithAccumulator(Mat,Mat,double,double,int,double)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; input image
     * @param lines_nativeObj native matrix or object handle for {@code lines}; matrix receiving or containing detected
     *                        line segments
     * @param rho             distance resolution of the line accumulator, in pixels
     * @param theta           angular resolution of the line accumulator, in radians
     * @param threshold       minimum accumulator vote threshold for retaining a detected line
     * @param srn             distance-resolution divisor for the multiscale Hough transform
     */
    private static native void HoughLinesWithAccumulator_4(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold,
            double srn);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughLinesWithAccumulator(Mat,Mat,double,double,int)}.
     *
     * @param image_nativeObj native matrix or object handle for {@code image}; input image
     * @param lines_nativeObj native matrix or object handle for {@code lines}; matrix receiving or containing detected
     *                        line segments
     * @param rho             distance resolution of the line accumulator, in pixels
     * @param theta           angular resolution of the line accumulator, in radians
     * @param threshold       minimum accumulator vote threshold for retaining a detected line
     */
    private static native void HoughLinesWithAccumulator_5(
            long image_nativeObj,
            long lines_nativeObj,
            double rho,
            double theta,
            int threshold);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCirclesWithAccumulator(Mat,Mat,int,double,double,double,double,int,int)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; input image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; output matrix of detected circle
     *                          centers and radii
     * @param method            algorithm or decomposition selector
     * @param dp                ratio of image resolution to accumulator resolution
     * @param minDist           minimum distance in image pixels between detected centers
     * @param param1            upper edge-detector threshold used by circle detection
     * @param param2            circle-method-specific accumulator or detection threshold
     * @param minRadius         minimum circle radius, in pixels
     * @param maxRadius         maximum circle radius, in pixels
     */
    private static native void HoughCirclesWithAccumulator_0(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2,
            int minRadius,
            int maxRadius);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCirclesWithAccumulator(Mat,Mat,int,double,double,double,double,int)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; input image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; output matrix of detected circle
     *                          centers and radii
     * @param method            algorithm or decomposition selector
     * @param dp                ratio of image resolution to accumulator resolution
     * @param minDist           minimum distance in image pixels between detected centers
     * @param param1            upper edge-detector threshold used by circle detection
     * @param param2            circle-method-specific accumulator or detection threshold
     * @param minRadius         minimum circle radius, in pixels
     */
    private static native void HoughCirclesWithAccumulator_1(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2,
            int minRadius);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCirclesWithAccumulator(Mat,Mat,int,double,double,double,double)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; input image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; output matrix of detected circle
     *                          centers and radii
     * @param method            algorithm or decomposition selector
     * @param dp                ratio of image resolution to accumulator resolution
     * @param minDist           minimum distance in image pixels between detected centers
     * @param param1            upper edge-detector threshold used by circle detection
     * @param param2            circle-method-specific accumulator or detection threshold
     */
    private static native void HoughCirclesWithAccumulator_2(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist,
            double param1,
            double param2);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCirclesWithAccumulator(Mat,Mat,int,double,double,double)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; input image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; output matrix of detected circle
     *                          centers and radii
     * @param method            algorithm or decomposition selector
     * @param dp                ratio of image resolution to accumulator resolution
     * @param minDist           minimum distance in image pixels between detected centers
     * @param param1            upper edge-detector threshold used by circle detection
     */
    private static native void HoughCirclesWithAccumulator_3(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist,
            double param1);

    /**
     * Native implementation of
     * {@link org.miaixz.bus.opencv.imgproc.Imgproc#HoughCirclesWithAccumulator(Mat,Mat,int,double,double)}.
     *
     * @param image_nativeObj   native matrix or object handle for {@code image}; input image
     * @param circles_nativeObj native matrix or object handle for {@code circles}; output matrix of detected circle
     *                          centers and radii
     * @param method            algorithm or decomposition selector
     * @param dp                ratio of image resolution to accumulator resolution
     * @param minDist           minimum distance in image pixels between detected centers
     */
    private static native void HoughCirclesWithAccumulator_4(
            long image_nativeObj,
            long circles_nativeObj,
            int method,
            double dp,
            double minDist);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Imgproc#getTextSize(String,int,double,int,int[])}.
     *
     * @param text      text whose bounding dimensions and baseline are measured
     * @param fontFace  Hershey font identifier and optional style flags
     * @param fontScale scale applied to the selected Hershey font
     * @param thickness stroke width in pixels; a negative value requests filling where supported
     * @param baseLine  optional one-element array receiving the baseline offset; null omits that output
     * @return packed numeric fields used by the Java wrapper to construct {@code Size}
     */
    private static native double[] n_getTextSize(
            String text,
            int fontFace,
            double fontScale,
            int thickness,
            int[] baseLine);

}
