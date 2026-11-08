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

/**
 * Native parameters for two-dimensional image filtering.
 */
public class Filter2DParams {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native Filter2DParams handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected Filter2DParams(long addr) {
        nativeObj = addr;
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Returns the opaque native handle associated with this Java wrapper.
     *
     * @return the opaque native handle
     */
    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static Filter2DParams __fromPtr__(long addr) {
        return new Filter2DParams(addr);
    }

    /**
     * Returns horizontal kernel-anchor coordinate; a negative value selects the default anchor.
     *
     * @return horizontal kernel-anchor coordinate; a negative value selects the default anchor
     */
    public int get_anchorX() {
        return get_anchorX_0(nativeObj);
    }

    /**
     * Sets horizontal kernel-anchor coordinate; a negative value selects the default anchor.
     *
     * @param anchorX horizontal kernel-anchor coordinate; a negative value selects the default anchor
     */
    public void set_anchorX(int anchorX) {
        set_anchorX_0(nativeObj, anchorX);
    }

    /**
     * Returns vertical kernel-anchor coordinate; a negative value selects the default anchor.
     *
     * @return vertical kernel-anchor coordinate; a negative value selects the default anchor
     */
    public int get_anchorY() {
        return get_anchorY_0(nativeObj);
    }

    /**
     * Sets vertical kernel-anchor coordinate; a negative value selects the default anchor.
     *
     * @param anchorY vertical kernel-anchor coordinate; a negative value selects the default anchor
     */
    public void set_anchorY(int anchorY) {
        set_anchorY_0(nativeObj, anchorY);
    }

    /**
     * Returns border extrapolation mode used by the filter.
     *
     * @return border extrapolation mode used by the filter
     */
    public int get_borderType() {
        return get_borderType_0(nativeObj);
    }

    /**
     * Sets border extrapolation mode used by the filter.
     *
     * @param borderType pixel extrapolation mode at the image boundary
     */
    public void set_borderType(int borderType) {
        set_borderType_0(nativeObj, borderType);
    }

    /**
     * Returns destination channel depth; a negative value keeps the source depth.
     *
     * @return destination channel depth; a negative value keeps the source depth
     */
    public int get_ddepth() {
        return get_ddepth_0(nativeObj);
    }

    /**
     * Sets destination channel depth; a negative value keeps the source depth.
     *
     * @param ddepth desired destination element depth
     */
    public void set_ddepth(int ddepth) {
        set_ddepth_0(nativeObj, ddepth);
    }

    /**
     * Returns multiplicative factor applied by the filter.
     *
     * @return multiplicative factor applied by the filter
     */
    public double get_scale() {
        return get_scale_0(nativeObj);
    }

    /**
     * Sets multiplicative factor applied by the filter.
     *
     * @param scale multiplicative scale applied by the operation
     */
    public void set_scale(double scale) {
        set_scale_0(nativeObj, scale);
    }

    /**
     * Returns additive offset applied after scaling.
     *
     * @return additive offset applied after scaling
     */
    public double get_shift() {
        return get_shift_0(nativeObj);
    }

    /**
     * Sets additive offset applied after scaling.
     *
     * @param shift number of fractional bits in integer drawing coordinates
     */
    public void set_shift(double shift) {
        set_shift_0(nativeObj, shift);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#get_anchorX()}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @return horizontal kernel-anchor coordinate; a negative value selects the default anchor
     */
    private static native int get_anchorX_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#set_anchorX(int)}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @param anchorX   horizontal kernel-anchor coordinate; a negative value selects the default anchor
     */
    private static native void set_anchorX_0(long nativeObj, int anchorX);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#get_anchorY()}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @return vertical kernel-anchor coordinate; a negative value selects the default anchor
     */
    private static native int get_anchorY_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#set_anchorY(int)}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @param anchorY   vertical kernel-anchor coordinate; a negative value selects the default anchor
     */
    private static native void set_anchorY_0(long nativeObj, int anchorY);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#get_borderType()}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @return border extrapolation mode used by the filter
     */
    private static native int get_borderType_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#set_borderType(int)}.
     *
     * @param nativeObj  native binding handle of this {@code Filter2DParams} instance
     * @param borderType pixel extrapolation mode at the image boundary
     */
    private static native void set_borderType_0(long nativeObj, int borderType);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#get_ddepth()}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @return destination channel depth; a negative value keeps the source depth
     */
    private static native int get_ddepth_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#set_ddepth(int)}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @param ddepth    desired destination element depth
     */
    private static native void set_ddepth_0(long nativeObj, int ddepth);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#get_scale()}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @return multiplicative factor applied by the filter
     */
    private static native double get_scale_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#set_scale(double)}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @param scale     multiplicative scale applied by the operation
     */
    private static native void set_scale_0(long nativeObj, double scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#get_shift()}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @return additive offset applied after scaling
     */
    private static native double get_shift_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.imgproc.Filter2DParams#set_shift(double)}.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     * @param shift     number of fractional bits in integer drawing coordinates
     */
    private static native void set_shift_0(long nativeObj, double shift);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code Filter2DParams} instance
     */
    private static native void delete(long nativeObj);

}
