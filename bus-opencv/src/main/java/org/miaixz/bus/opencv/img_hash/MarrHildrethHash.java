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
package org.miaixz.bus.opencv.img_hash;

/**
 * An image hash algorithm based on a Marr-Hildreth image representation.
 */
public class MarrHildrethHash extends ImgHashBase {

    /**
     * Wraps an existing native MarrHildrethHash handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected MarrHildrethHash(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static MarrHildrethHash __fromPtr__(long addr) {
        return new MarrHildrethHash(addr);
    }

    /**
     * Self explain.
     *
     * @return Marr-Hildreth kernel parameter alpha
     */
    public float getAlpha() {
        return getAlpha_0(nativeObj);
    }

    /**
     * Self explain.
     *
     * @return Marr-Hildreth kernel scale
     */
    public float getScale() {
        return getScale_0(nativeObj);
    }

    /**
     * Set Mh kernel parameters.
     *
     * @param alpha int scale factor for marr wavelet (default=2)
     * @param scale int level of scale factor (default = 1)
     */
    public void setKernelParam(float alpha, float scale) {
        setKernelParam_0(nativeObj, alpha, scale);
    }

    /**
     * Creates a configured MarrHildrethHash instance using the supplied options.
     *
     * @param alpha int scale factor for marr wavelet (default=2)
     * @param scale int level of scale factor (default = 1)
     * @return a new {@code MarrHildrethHash} wrapper configured with the selected options
     */
    public static MarrHildrethHash create(float alpha, float scale) {
        return MarrHildrethHash.__fromPtr__(create_0(alpha, scale));
    }

    /**
     * Creates a configured MarrHildrethHash instance using the supplied options.
     *
     * @param alpha int scale factor for marr wavelet (default=2)
     * @return a new {@code MarrHildrethHash} wrapper configured with the selected options
     */
    public static MarrHildrethHash create(float alpha) {
        return MarrHildrethHash.__fromPtr__(create_1(alpha));
    }

    /**
     * Creates a configured MarrHildrethHash instance using the supplied options.
     *
     * @return a new {@code MarrHildrethHash} wrapper configured with the selected options
     */
    public static MarrHildrethHash create() {
        return MarrHildrethHash.__fromPtr__(create_2());
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.MarrHildrethHash#getAlpha()}.
     *
     * @param nativeObj native binding handle of this {@code MarrHildrethHash} instance
     * @return Marr-Hildreth kernel parameter alpha
     */
    private static native float getAlpha_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.MarrHildrethHash#getScale()}.
     *
     * @param nativeObj native binding handle of this {@code MarrHildrethHash} instance
     * @return Marr-Hildreth kernel scale
     */
    private static native float getScale_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.MarrHildrethHash#setKernelParam(float,float)}.
     *
     * @param nativeObj native binding handle of this {@code MarrHildrethHash} instance
     * @param alpha     int scale factor for marr wavelet (default=2)
     * @param scale     int level of scale factor (default = 1)
     */
    private static native void setKernelParam_0(long nativeObj, float alpha, float scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.MarrHildrethHash#create(float,float)}.
     *
     * @param alpha int scale factor for marr wavelet (default=2)
     * @param scale int level of scale factor (default = 1)
     * @return native binding handle for a new {@code MarrHildrethHash} wrapper configured with the selected options
     */
    private static native long create_0(float alpha, float scale);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.MarrHildrethHash#create(float)}.
     *
     * @param alpha int scale factor for marr wavelet (default=2)
     * @return native binding handle for a new {@code MarrHildrethHash} wrapper configured with the selected options
     */
    private static native long create_1(float alpha);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.MarrHildrethHash#create()}.
     *
     * @return native binding handle for a new {@code MarrHildrethHash} wrapper configured with the selected options
     */
    private static native long create_2();

    /**
     * Deletes the native {@code MarrHildrethHash} binding handle. This compatibility declaration is retained in
     * addition to cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
