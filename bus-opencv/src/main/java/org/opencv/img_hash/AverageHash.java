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

// C++: class AverageHash
/**
 * Computes average hash value of the input image
 *
 * This is a fast image hashing algorithm, but only work on simple case. For more details, please refer to CITE:
 * lookslikeit
 */
public class AverageHash extends ImgHashBase {

    protected AverageHash(long addr) {
        super(addr);
    }

    // internal usage only
    public static AverageHash __fromPtr__(long addr) {
        return new AverageHash(addr);
    }

    //
    // C++: static Ptr_AverageHash cv::img_hash::AverageHash::create()
    //

    public static AverageHash create() {
        return AverageHash.__fromPtr__(create_0());
    }

    // C++: static Ptr_AverageHash cv::img_hash::AverageHash::create()
    private static native long create_0();

    // native support for deleting native object
    private static native void delete(long nativeObj);

}
