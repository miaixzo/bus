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

import org.miaixz.bus.opencv.core.Algorithm;
import org.miaixz.bus.opencv.core.Mat;

/**
 * Base class for computing image hashes and comparing hash values.
 */
public class ImgHashBase extends Algorithm {

    /**
     * Wraps an existing native ImgHashBase handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected ImgHashBase(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static ImgHashBase __fromPtr__(long addr) {
        return new ImgHashBase(addr);
    }

    /**
     * Computes hash of the input image.
     *
     * @param inputArr  input image want to compute hash value
     * @param outputArr hash of the image
     * @throws NullPointerException if inputArr, outputArr is null
     */
    public void compute(Mat inputArr, Mat outputArr) {
        compute_0(nativeObj, inputArr.nativeObj, outputArr.nativeObj);
    }

    /**
     * Compare the hash value between inOne and inTwo.
     *
     * @param hashOne Hash value one
     * @param hashTwo Hash value two
     * @return value indicate similarity between inOne and inTwo, the meaning of the value vary from algorithms to
     *         algorithms
     * @throws NullPointerException if hashOne, hashTwo is null
     */
    public double compare(Mat hashOne, Mat hashTwo) {
        return compare_0(nativeObj, hashOne.nativeObj, hashTwo.nativeObj);
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.ImgHashBase#compute(Mat,Mat)}.
     *
     * @param nativeObj           native binding handle of this {@code ImgHashBase} instance
     * @param inputArr_nativeObj  native matrix or object handle for {@code inputArr}; input image want to compute hash
     *                            value
     * @param outputArr_nativeObj native matrix or object handle for {@code outputArr}; hash of the image
     */
    private static native void compute_0(long nativeObj, long inputArr_nativeObj, long outputArr_nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.ImgHashBase#compare(Mat,Mat)}.
     *
     * @param nativeObj         native binding handle of this {@code ImgHashBase} instance
     * @param hashOne_nativeObj native matrix or object handle for {@code hashOne}; Hash value one
     * @param hashTwo_nativeObj native matrix or object handle for {@code hashTwo}; Hash value two
     * @return value indicate similarity between inOne and inTwo, the meaning of the value vary from algorithms to
     *         algorithms
     */
    private static native double compare_0(long nativeObj, long hashOne_nativeObj, long hashTwo_nativeObj);

    /**
     * Deletes the native {@code ImgHashBase} binding handle. This compatibility declaration is retained in addition to
     * cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
