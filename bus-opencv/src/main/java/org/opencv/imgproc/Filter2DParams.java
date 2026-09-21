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
package org.opencv.imgproc;

// C++: class Filter2DParams

/**
 * Provides the {@code Filter2DParams} API.
 */
public class Filter2DParams {

    /**
     * The {@code nativeObj} value.
     */
    protected final long nativeObj;

    /**
     * Creates a new {@code Filter2DParams} instance.
     *
     * @param addr the {@code addr} value
     */
    protected Filter2DParams(long addr) {
        nativeObj = addr;
        long nativeObjCopy = nativeObj;
        org.opencv.core.CleanableMat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    // internal usage only
    /**
     * Performs the {@code __fromPtr__} operation.
     *
     * @param addr the {@code addr} value
     * @return the operation result
     */
    public static Filter2DParams __fromPtr__(long addr) {
        return new Filter2DParams(addr);
    }

    // C++: int Filter2DParams::anchorX
    private static native int get_anchorX_0(long nativeObj);

    //
    // C++: int Filter2DParams::anchorX
    //

    // C++: void Filter2DParams::anchorX
    private static native void set_anchorX_0(long nativeObj, int anchorX);

    //
    // C++: void Filter2DParams::anchorX
    //

    // C++: int Filter2DParams::anchorY
    private static native int get_anchorY_0(long nativeObj);

    //
    // C++: int Filter2DParams::anchorY
    //

    // C++: void Filter2DParams::anchorY
    private static native void set_anchorY_0(long nativeObj, int anchorY);

    //
    // C++: void Filter2DParams::anchorY
    //

    // C++: int Filter2DParams::borderType
    private static native int get_borderType_0(long nativeObj);

    //
    // C++: int Filter2DParams::borderType
    //

    // C++: void Filter2DParams::borderType
    private static native void set_borderType_0(long nativeObj, int borderType);

    //
    // C++: void Filter2DParams::borderType
    //

    // C++: int Filter2DParams::ddepth
    private static native int get_ddepth_0(long nativeObj);

    //
    // C++: int Filter2DParams::ddepth
    //

    // C++: void Filter2DParams::ddepth
    private static native void set_ddepth_0(long nativeObj, int ddepth);

    //
    // C++: void Filter2DParams::ddepth
    //

    // C++: double Filter2DParams::scale
    private static native double get_scale_0(long nativeObj);

    //
    // C++: double Filter2DParams::scale
    //

    // C++: void Filter2DParams::scale
    private static native void set_scale_0(long nativeObj, double scale);

    //
    // C++: void Filter2DParams::scale
    //

    // C++: double Filter2DParams::shift
    private static native double get_shift_0(long nativeObj);

    //
    // C++: double Filter2DParams::shift
    //

    // C++: void Filter2DParams::shift
    private static native void set_shift_0(long nativeObj, double shift);

    //
    // C++: void Filter2DParams::shift
    //

    // native support for java finalize() or cleaner
    private static native void delete(long nativeObj);

    /**
     * Performs the {@code getNativeObjAddr} operation.
     *
     * @return the operation result
     */
    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Performs the {@code get_anchorX} operation.
     *
     * @return the operation result
     */
    public int get_anchorX() {
        return get_anchorX_0(nativeObj);
    }

    /**
     * Performs the {@code set_anchorX} operation.
     *
     * @param anchorX the {@code anchorX} value
     */
    public void set_anchorX(int anchorX) {
        set_anchorX_0(nativeObj, anchorX);
    }

    /**
     * Performs the {@code get_anchorY} operation.
     *
     * @return the operation result
     */
    public int get_anchorY() {
        return get_anchorY_0(nativeObj);
    }

    /**
     * Performs the {@code set_anchorY} operation.
     *
     * @param anchorY the {@code anchorY} value
     */
    public void set_anchorY(int anchorY) {
        set_anchorY_0(nativeObj, anchorY);
    }

    /**
     * Performs the {@code get_borderType} operation.
     *
     * @return the operation result
     */
    public int get_borderType() {
        return get_borderType_0(nativeObj);
    }

    /**
     * Performs the {@code set_borderType} operation.
     *
     * @param borderType the {@code borderType} value
     */
    public void set_borderType(int borderType) {
        set_borderType_0(nativeObj, borderType);
    }

    /**
     * Performs the {@code get_ddepth} operation.
     *
     * @return the operation result
     */
    public int get_ddepth() {
        return get_ddepth_0(nativeObj);
    }

    /**
     * Performs the {@code set_ddepth} operation.
     *
     * @param ddepth the {@code ddepth} value
     */
    public void set_ddepth(int ddepth) {
        set_ddepth_0(nativeObj, ddepth);
    }

    /**
     * Performs the {@code get_scale} operation.
     *
     * @return the operation result
     */
    public double get_scale() {
        return get_scale_0(nativeObj);
    }

    /**
     * Performs the {@code set_scale} operation.
     *
     * @param scale the {@code scale} value
     */
    public void set_scale(double scale) {
        set_scale_0(nativeObj, scale);
    }

    /**
     * Performs the {@code get_shift} operation.
     *
     * @return the operation result
     */
    public double get_shift() {
        return get_shift_0(nativeObj);
    }

    /**
     * Performs the {@code set_shift} operation.
     *
     * @param shift the {@code shift} value
     */
    public void set_shift(double shift) {
        set_shift_0(nativeObj, shift);
    }

}
