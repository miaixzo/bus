/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.opencv.img_hash;

import org.opencv.core.MatOfDouble;

// C++: class BlockMeanHash
/**
 * Image hash based on block mean.
 *
 * See CITE: zauner2010implementation for details.
 */
public class BlockMeanHash extends ImgHashBase {

    protected BlockMeanHash(long addr) {
        super(addr);
    }

    // internal usage only
    public static BlockMeanHash __fromPtr__(long addr) {
        return new BlockMeanHash(addr);
    }

    //
    // C++: void cv::img_hash::BlockMeanHash::setMode(int mode)
    //

    public static BlockMeanHash create(int mode) {
        return BlockMeanHash.__fromPtr__(create_0(mode));
    }

    //
    // C++: vector_double cv::img_hash::BlockMeanHash::getMean()
    //

    public static BlockMeanHash create() {
        return BlockMeanHash.__fromPtr__(create_1());
    }

    //
    // C++: static Ptr_BlockMeanHash cv::img_hash::BlockMeanHash::create(int mode = BLOCK_MEAN_HASH_MODE_0)
    //

    // C++: void cv::img_hash::BlockMeanHash::setMode(int mode)
    private static native void setMode_0(long nativeObj, int mode);

    // C++: vector_double cv::img_hash::BlockMeanHash::getMean()
    private static native long getMean_0(long nativeObj);

    // C++: static Ptr_BlockMeanHash cv::img_hash::BlockMeanHash::create(int mode = BLOCK_MEAN_HASH_MODE_0)
    private static native long create_0(int mode);

    private static native long create_1();

    // native support for deleting native object
    private static native void delete(long nativeObj);

    /**
     * Create BlockMeanHash object
     *
     * @param mode the mode
     */
    public void setMode(int mode) {
        setMode_0(nativeObj, mode);
    }

    public MatOfDouble getMean() {
        return MatOfDouble.fromNativeAddr(getMean_0(nativeObj));
    }

}
