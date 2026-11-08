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
package org.miaixz.bus.opencv.core;

/**
 * Native parameters that configure matrix reduction operations.
 */
public class ReduceParams {

    /**
     * Opaque native handle retained by this wrapper and passed to JNI operations.
     */
    protected final long nativeObj;

    /**
     * Wraps an existing native ReduceParams handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected ReduceParams(long addr) {
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
    public static ReduceParams __fromPtr__(long addr) {
        return new ReduceParams(addr);
    }

    /**
     * Creates a configured ReduceParams value using the supplied values.
     *
     * @param rtype    desired result type or depth code
     * @param axes     ellipse axis lengths
     * @param keepdims whether keepdims is enabled
     * @param dtype    desired output depth; a negative value keeps the source depth when supported
     */
    public ReduceParams(int rtype, MatOfInt axes, boolean keepdims, int dtype) {
        Mat axes_mat = axes;
        nativeObj = ReduceParams_0(rtype, axes_mat.nativeObj, keepdims, dtype);
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates a configured ReduceParams value using the supplied values.
     *
     * @param rtype    desired result type or depth code
     * @param axes     ellipse axis lengths
     * @param keepdims whether keepdims is enabled
     */
    public ReduceParams(int rtype, MatOfInt axes, boolean keepdims) {
        Mat axes_mat = axes;
        nativeObj = ReduceParams_1(rtype, axes_mat.nativeObj, keepdims);
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates a configured ReduceParams value using the supplied values.
     *
     * @param rtype desired result type or depth code
     * @param axes  ellipse axis lengths
     */
    public ReduceParams(int rtype, MatOfInt axes) {
        Mat axes_mat = axes;
        nativeObj = ReduceParams_2(rtype, axes_mat.nativeObj);
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates a configured ReduceParams value using the supplied values.
     *
     * @param rtype desired result type or depth code
     */
    public ReduceParams(int rtype) {
        nativeObj = ReduceParams_3(rtype);
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Creates an empty ReduceParams value.
     */
    public ReduceParams() {
        nativeObj = ReduceParams_4();
        long nativeObjCopy = nativeObj;
        org.miaixz.bus.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    /**
     * Returns reduction operation code.
     *
     * @return reduction operation code
     */
    public int get_rtype() {
        return get_rtype_0(nativeObj);
    }

    /**
     * Sets reduction operation code.
     *
     * @param rtype desired result type or depth code
     */
    public void set_rtype(int rtype) {
        set_rtype_0(nativeObj, rtype);
    }

    /**
     * Returns matrix of dimension indices to reduce.
     *
     * @return matrix of dimension indices to reduce
     */
    public MatOfInt get_axes() {
        return MatOfInt.fromNativeAddr(get_axes_0(nativeObj));
    }

    /**
     * Sets matrix of dimension indices to reduce.
     *
     * @param axes ellipse axis lengths
     */
    public void set_axes(MatOfInt axes) {
        Mat axes_mat = axes;
        set_axes_0(nativeObj, axes_mat.nativeObj);
    }

    /**
     * Returns true if reduced axes remain as singleton dimensions.
     *
     * @return true if reduced axes remain as singleton dimensions
     */
    public boolean get_keepdims() {
        return get_keepdims_0(nativeObj);
    }

    /**
     * Sets whether reduced axes remain singleton dimensions.
     *
     * @param keepdims whether keepdims is enabled
     */
    public void set_keepdims(boolean keepdims) {
        set_keepdims_0(nativeObj, keepdims);
    }

    /**
     * Returns output channel depth; a negative value keeps the source depth.
     *
     * @return output channel depth; a negative value keeps the source depth
     */
    public int get_dtype() {
        return get_dtype_0(nativeObj);
    }

    /**
     * Sets output channel depth; a negative value keeps the source depth.
     *
     * @param dtype desired output depth; a negative value keeps the source depth when supported
     */
    public void set_dtype(int dtype) {
        set_dtype_0(nativeObj, dtype);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#ReduceParams(int,MatOfInt,boolean,int)}.
     *
     * @param rtype              desired result type or depth code
     * @param axes_mat_nativeObj native matrix or object handle for {@code axes_mat}; ellipse axis lengths
     * @param keepdims           whether keepdims is enabled
     * @param dtype              desired output depth; a negative value keeps the source depth when supported
     * @return a newly allocated native {@code ReduceParams} binding handle
     */
    private static native long ReduceParams_0(int rtype, long axes_mat_nativeObj, boolean keepdims, int dtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#ReduceParams(int,MatOfInt,boolean)}.
     *
     * @param rtype              desired result type or depth code
     * @param axes_mat_nativeObj native matrix or object handle for {@code axes_mat}; ellipse axis lengths
     * @param keepdims           whether keepdims is enabled
     * @return a newly allocated native {@code ReduceParams} binding handle
     */
    private static native long ReduceParams_1(int rtype, long axes_mat_nativeObj, boolean keepdims);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#ReduceParams(int,MatOfInt)}.
     *
     * @param rtype              desired result type or depth code
     * @param axes_mat_nativeObj native matrix or object handle for {@code axes_mat}; ellipse axis lengths
     * @return a newly allocated native {@code ReduceParams} binding handle
     */
    private static native long ReduceParams_2(int rtype, long axes_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#ReduceParams(int)}.
     *
     * @param rtype desired result type or depth code
     * @return a newly allocated native {@code ReduceParams} binding handle
     */
    private static native long ReduceParams_3(int rtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#ReduceParams()}.
     *
     * @return a newly allocated native {@code ReduceParams} binding handle
     */
    private static native long ReduceParams_4();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#get_rtype()}.
     *
     * @param nativeObj native binding handle of this {@code ReduceParams} instance
     * @return reduction operation code
     */
    private static native int get_rtype_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#set_rtype(int)}.
     *
     * @param nativeObj native binding handle of this {@code ReduceParams} instance
     * @param rtype     desired result type or depth code
     */
    private static native void set_rtype_0(long nativeObj, int rtype);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#get_axes()}.
     *
     * @param nativeObj native binding handle of this {@code ReduceParams} instance
     * @return native binding handle for matrix of dimension indices to reduce
     */
    private static native long get_axes_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#set_axes(MatOfInt)}.
     *
     * @param nativeObj          native binding handle of this {@code ReduceParams} instance
     * @param axes_mat_nativeObj native matrix or object handle for {@code axes_mat}; ellipse axis lengths
     */
    private static native void set_axes_0(long nativeObj, long axes_mat_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#get_keepdims()}.
     *
     * @param nativeObj native binding handle of this {@code ReduceParams} instance
     * @return true if reduced axes remain as singleton dimensions
     */
    private static native boolean get_keepdims_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#set_keepdims(boolean)}.
     *
     * @param nativeObj native binding handle of this {@code ReduceParams} instance
     * @param keepdims  whether keepdims is enabled
     */
    private static native void set_keepdims_0(long nativeObj, boolean keepdims);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#get_dtype()}.
     *
     * @param nativeObj native binding handle of this {@code ReduceParams} instance
     * @return output channel depth; a negative value keeps the source depth
     */
    private static native int get_dtype_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.core.ReduceParams#set_dtype(int)}.
     *
     * @param nativeObj native binding handle of this {@code ReduceParams} instance
     * @param dtype     desired output depth; a negative value keeps the source depth when supported
     */
    private static native void set_dtype_0(long nativeObj, int dtype);

    /**
     * Deletes the native binding handle after the owning Java wrapper becomes unreachable.
     *
     * @param nativeObj native binding handle of this {@code ReduceParams} instance
     */
    private static native void delete(long nativeObj);

}
