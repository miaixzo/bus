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

import org.miaixz.bus.opencv.core.Algorithm;
import org.miaixz.bus.opencv.core.Mat;
import org.miaixz.bus.opencv.core.Size;

/**
 * Contrast-limited adaptive histogram equalization for improving local image contrast.
 */
public class CLAHE extends Algorithm {

    /**
     * Wraps an existing native CLAHE handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected CLAHE(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static CLAHE __fromPtr__(long addr) {
        return new CLAHE(addr);
    }

    /**
     * Equalizes the histogram of a grayscale image using Contrast Limited Adaptive Histogram Equalization.
     *
     * @param src Source image of type CV_8UC1 or CV_16UC1
     * @param dst Destination image
     * @throws NullPointerException if src, dst is null
     */
    public void apply(Mat src, Mat dst) {
        apply_0(nativeObj, src.nativeObj, dst.nativeObj);
    }

    /**
     * Sets threshold for contrast limiting.
     *
     * @param clipLimit threshold value
     */
    public void setClipLimit(double clipLimit) {
        setClipLimit_0(nativeObj, clipLimit);
    }

    /**
     * Returns contrast-limiting threshold.
     *
     * @return contrast-limiting threshold
     */
    public double getClipLimit() {
        return getClipLimit_0(nativeObj);
    }

    /**
     * Sets size of grid for histogram equalization. Input image will be divided into equally sized rectangular tiles.
     *
     * @param tileGridSize defines the number of tiles in row and column
     * @throws NullPointerException if tileGridSize is null
     */
    public void setTilesGridSize(Size tileGridSize) {
        setTilesGridSize_0(nativeObj, tileGridSize.width, tileGridSize.height);
    }

    /**
     * Returns number of contextual tiles horizontally and vertically.
     *
     * @return number of contextual tiles horizontally and vertically
     */
    public Size getTilesGridSize() {
        return new Size(getTilesGridSize_0(nativeObj));
    }

    /**
     * Sets bit shift parameter for histogram bins.
     *
     * @param bitShift bit shift value (default is 0)
     */
    public void setBitShift(int bitShift) {
        setBitShift_0(nativeObj, bitShift);
    }

    /**
     * Returns the bit shift parameter for histogram bins.
     *
     * @return right shift applied to input values before histogram-bin selection
     */
    public int getBitShift() {
        return getBitShift_0(nativeObj);
    }

    /**
     * Releases reusable internal processing buffers retained by the algorithm.
     */
    public void collectGarbage() {
        collectGarbage_0(nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.CLAHE#apply(Mat,Mat)}.
     *
     * @param nativeObj     native binding handle of this {@code CLAHE} instance
     * @param src_nativeObj native matrix or object handle for {@code src}; Source image of type CV_8UC1 or CV_16UC1
     * @param dst_nativeObj native matrix or object handle for {@code dst}; Destination image
     */
    private static native void apply_0(long nativeObj, long src_nativeObj, long dst_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.CLAHE#setClipLimit(double)}.
     *
     * @param nativeObj native binding handle of this {@code CLAHE} instance
     * @param clipLimit threshold value
     */
    private static native void setClipLimit_0(long nativeObj, double clipLimit);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.CLAHE#getClipLimit()}.
     *
     * @param nativeObj native binding handle of this {@code CLAHE} instance
     * @return contrast-limiting threshold
     */
    private static native double getClipLimit_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.CLAHE#setTilesGridSize(Size)}.
     *
     * @param nativeObj           native binding handle of this {@code CLAHE} instance
     * @param tileGridSize_width  {@code tileGridSize.width} component of defines the number of tiles in row and column
     * @param tileGridSize_height {@code tileGridSize.height} component of defines the number of tiles in row and column
     */
    private static native void setTilesGridSize_0(
            long nativeObj,
            double tileGridSize_width,
            double tileGridSize_height);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.CLAHE#getTilesGridSize()}.
     *
     * @param nativeObj native binding handle of this {@code CLAHE} instance
     * @return packed numeric fields used by the Java wrapper to construct {@code Size}
     */
    private static native double[] getTilesGridSize_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.CLAHE#setBitShift(int)}.
     *
     * @param nativeObj native binding handle of this {@code CLAHE} instance
     * @param bitShift  bit shift value (default is 0)
     */
    private static native void setBitShift_0(long nativeObj, int bitShift);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.CLAHE#getBitShift()}.
     *
     * @param nativeObj native binding handle of this {@code CLAHE} instance
     * @return right shift applied to input values before histogram-bin selection
     */
    private static native int getBitShift_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.CLAHE#collectGarbage()}.
     *
     * @param nativeObj native binding handle of this {@code CLAHE} instance
     */
    private static native void collectGarbage_0(long nativeObj);

    /**
     * Deletes the native {@code CLAHE} binding handle. This compatibility declaration is retained in addition to
     * cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
