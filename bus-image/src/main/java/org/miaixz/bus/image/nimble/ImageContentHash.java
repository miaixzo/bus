/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.image.nimble;

import org.opencv.core.Mat;
import org.opencv.img_hash.*;

/**
 * Algorithms to compare image content
 *
 * @author Kimi Liu
 * @see <a href="http://qtandopencv.blogspot.com/2016/06/introduction-to-image-hash-module-of.html">Hash for pixel data</a>
 * @since Java 17+
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
