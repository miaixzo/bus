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

import org.miaixz.bus.opencv.core.MatOfDouble;

/**
 * An image hash algorithm based on intensity means in image blocks.
 */
public class BlockMeanHash extends ImgHashBase {

    /**
     * Wraps an existing native BlockMeanHash handle and registers it for cleanup.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     */
    protected BlockMeanHash(long addr) {
        super(addr);
    }

    /**
     * Creates a Java wrapper for an existing native handle. This bridge is intended for binding and subclass
     * integration; the address must identify the native handle expected by this wrapper.
     *
     * @param addr native binding handle to retain; the caller must supply a handle of the exact native wrapper type
     * @return a new Java wrapper that retains the supplied native binding handle for cleanup
     */
    public static BlockMeanHash __fromPtr__(long addr) {
        return new BlockMeanHash(addr);
    }

    /**
     * Create BlockMeanHash object.
     *
     * @param mode block-mean hashing mode, selected by the BLOCK_MEAN_HASH_MODE constants
     */
    public void setMode(int mode) {
        setMode_0(nativeObj, mode);
    }

    /**
     * Returns matrix of block mean intensities computed by the most recent hash calculation.
     *
     * @return matrix of block mean intensities computed by the most recent hash calculation
     */
    public MatOfDouble getMean() {
        return MatOfDouble.fromNativeAddr(getMean_0(nativeObj));
    }

    /**
     * Creates a configured BlockMeanHash instance using the supplied options.
     *
     * @param mode block-mean hashing mode, selected by the BLOCK_MEAN_HASH_MODE constants
     * @return a new {@code BlockMeanHash} wrapper configured with the selected options
     */
    public static BlockMeanHash create(int mode) {
        return BlockMeanHash.__fromPtr__(create_0(mode));
    }

    /**
     * Creates a configured BlockMeanHash instance using the supplied options.
     *
     * @return a new {@code BlockMeanHash} wrapper configured with the selected options
     */
    public static BlockMeanHash create() {
        return BlockMeanHash.__fromPtr__(create_1());
    }

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.BlockMeanHash#setMode(int)}.
     *
     * @param nativeObj native binding handle of this {@code BlockMeanHash} instance
     * @param mode      block-mean hashing mode, selected by the BLOCK_MEAN_HASH_MODE constants
     */
    private static native void setMode_0(long nativeObj, int mode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.BlockMeanHash#getMean()}.
     *
     * @param nativeObj native binding handle of this {@code BlockMeanHash} instance
     * @return native binding handle for matrix of block mean intensities computed by the most recent hash calculation
     */
    private static native long getMean_0(long nativeObj);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.BlockMeanHash#create(int)}.
     *
     * @param mode block-mean hashing mode, selected by the BLOCK_MEAN_HASH_MODE constants
     * @return native binding handle for a new {@code BlockMeanHash} wrapper configured with the selected options
     */
    private static native long create_0(int mode);

    /**
     * Native implementation of {@link org.miaixz.bus.opencv.img_hash.BlockMeanHash#create()}.
     *
     * @return native binding handle for a new {@code BlockMeanHash} wrapper configured with the selected options
     */
    private static native long create_1();

    /**
     * Deletes the native {@code BlockMeanHash} binding handle. This compatibility declaration is retained in addition
     * to cleanup inherited from Algorithm.
     *
     * @param nativeObj native binding handle of the matching algorithm type
     */
    private static native void delete(long nativeObj);

}
