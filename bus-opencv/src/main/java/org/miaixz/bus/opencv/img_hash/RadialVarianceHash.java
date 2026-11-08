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
 * An image hash algorithm based on radial intensity variance.
 */
public class RadialVarianceHash extends ImgHashBase {

    /**
     * Wraps an existing native RadialVarianceHash handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected RadialVarianceHash(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static RadialVarianceHash __fromPtr__(long addr) {
        return new RadialVarianceHash(addr);
    }

    /**
     * Creates a configured RadialVarianceHash instance using the supplied options.
     *
     * @param sigma          Gaussian smoothing standard deviation used by radial-variance hashing
     * @param numOfAngleLine number of angular projection lines used by radial-variance hashing
     * @return a new {@code RadialVarianceHash} wrapper configured with the selected options
     */
    public static RadialVarianceHash create(double sigma, int numOfAngleLine) {
        return RadialVarianceHash.__fromPtr__(create_0(sigma, numOfAngleLine));
    }

    /**
     * Creates a configured RadialVarianceHash instance using the supplied options.
     *
     * @param sigma Gaussian smoothing standard deviation used by radial-variance hashing
     * @return a new {@code RadialVarianceHash} wrapper configured with the selected options
     */
    public static RadialVarianceHash create(double sigma) {
        return RadialVarianceHash.__fromPtr__(create_1(sigma));
    }

    /**
     * Creates a configured RadialVarianceHash instance using the supplied options.
     *
     * @return a new {@code RadialVarianceHash} wrapper configured with the selected options
     */
    public static RadialVarianceHash create() {
        return RadialVarianceHash.__fromPtr__(create_2());
    }

    /**
     * Returns number of angular projection lines used by radial-variance hashing.
     *
     * @return number of angular projection lines used by radial-variance hashing
     */
    public int getNumOfAngleLine() {
        return getNumOfAngleLine_0(nativeObj);
    }

    /**
     * Returns Gaussian smoothing standard deviation used by radial-variance hashing.
     *
     * @return Gaussian smoothing standard deviation used by radial-variance hashing
     */
    public double getSigma() {
        return getSigma_0(nativeObj);
    }

    /**
     * Sets number of angular projection lines used by radial-variance hashing.
     *
     * @param value number of angular projection lines used by radial-variance hashing
     */
    public void setNumOfAngleLine(int value) {
        setNumOfAngleLine_0(nativeObj, value);
    }

    /**
     * Sets Gaussian smoothing standard deviation used by radial-variance hashing.
     *
     * @param value Gaussian smoothing standard deviation used by radial-variance hashing
     */
    public void setSigma(double value) {
        setSigma_0(nativeObj, value);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.RadialVarianceHash#create(double,int)}.
     *
     * @param sigma          Gaussian smoothing standard deviation used by radial-variance hashing
     * @param numOfAngleLine number of angular projection lines used by radial-variance hashing
     * @return native binding handle for a new {@code RadialVarianceHash} wrapper configured with the selected options
     */
    private static native long create_0(double sigma, int numOfAngleLine);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.RadialVarianceHash#create(double)}.
     *
     * @param sigma Gaussian smoothing standard deviation used by radial-variance hashing
     * @return native binding handle for a new {@code RadialVarianceHash} wrapper configured with the selected options
     */
    private static native long create_1(double sigma);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.RadialVarianceHash#create()}.
     *
     * @return native binding handle for a new {@code RadialVarianceHash} wrapper configured with the selected options
     */
    private static native long create_2();

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.RadialVarianceHash#getNumOfAngleLine()}.
     *
     * @param nativeObj native binding handle of this {@code RadialVarianceHash} instance
     * @return number of angular projection lines used by radial-variance hashing
     */
    private static native int getNumOfAngleLine_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.RadialVarianceHash#getSigma()}.
     *
     * @param nativeObj native binding handle of this {@code RadialVarianceHash} instance
     * @return Gaussian smoothing standard deviation used by radial-variance hashing
     */
    private static native double getSigma_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.RadialVarianceHash#setNumOfAngleLine(int)}.
     *
     * @param nativeObj native binding handle of this {@code RadialVarianceHash} instance
     * @param value     number of angular projection lines used by radial-variance hashing
     */
    private static native void setNumOfAngleLine_0(long nativeObj, int value);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.RadialVarianceHash#setSigma(double)}.
     *
     * @param nativeObj native binding handle of this {@code RadialVarianceHash} instance
     * @param value     Gaussian smoothing standard deviation used by radial-variance hashing
     */
    private static native void setSigma_0(long nativeObj, double value);

    /**
     * Deletes the native {@code RadialVarianceHash} binding handle. This compatibility declaration is retained in
     * addition to cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
