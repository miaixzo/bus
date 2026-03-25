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
package org.miaixz.bus.image.nimble;

import org.opencv.core.Mat;
import org.opencv.img_hash.*;

/**
 * Algorithms to compare image content
 *
 * @author Kimi Liu
 * @see <a href="http://qtandopencv.blogspot.com/2016/06/introduction-to-image-hash-module-of.html">Hash for pixel
 *      data</a>
 * @since Java 21+
 */
public enum ImageContentHash {

    AVERAGE() {

        @Override
        public ImgHashBase getAlgorithm() {
            return AverageHash.create();
        }
    },
    PHASH() {

        @Override
        public ImgHashBase getAlgorithm() {
            return PHash.create();
        }
    },
    MARR_HILDRETH() {

        @Override
        public ImgHashBase getAlgorithm() {
            return MarrHildrethHash.create();
        }
    },
    RADIAL_VARIANCE() {

        @Override
        public ImgHashBase getAlgorithm() {
            return RadialVarianceHash.create();
        }
    },
    BLOCK_MEAN_ZERO() {

        @Override
        public ImgHashBase getAlgorithm() {
            return BlockMeanHash.create(0);
        }
    },
    BLOCK_MEAN_ONE() {

        @Override
        public ImgHashBase getAlgorithm() {
            return BlockMeanHash.create(1);
        }
    },
    COLOR_MOMENT() {

        @Override
        public ImgHashBase getAlgorithm() {
            return ColorMomentHash.create();
        }
    };

    public abstract ImgHashBase getAlgorithm();

    public double compare(Mat imgIn, Mat imgOut) {
        ImgHashBase hashAlgorithm = getAlgorithm();
        Mat inHash = new Mat();
        Mat outHash = new Mat();
        hashAlgorithm.compute(imgIn, inHash);
        hashAlgorithm.compute(imgOut, outHash);
        return hashAlgorithm.compare(inHash, outHash);
    }

}
