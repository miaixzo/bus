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

import java.util.Objects;

import org.opencv.core.Mat;
import org.opencv.img_hash.AverageHash;
import org.opencv.img_hash.BlockMeanHash;
import org.opencv.img_hash.ColorMomentHash;
import org.opencv.img_hash.ImgHashBase;
import org.opencv.img_hash.MarrHildrethHash;
import org.opencv.img_hash.PHash;
import org.opencv.img_hash.RadialVarianceHash;

/**
 * Algorithms to compare image content of two images.
 *
 * @see <a href="http://qtandopencv.blogspot.com/2016/06/introduction-to-image-hash-module-of.html">Hash for pixel
 *      data</a>
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ImageContentHash {

    /**
     * Fast hash algorithm, ideal when speed is prioritized over accuracy.
     */
    AVERAGE(AverageHash::create),

    /**
     * Perceptual hash, best general-purpose algorithm balancing accuracy and performance.
     */
    PHASH(PHash::create),

    /**
     * Structural similarity hash using edge detection.
     */
    MARR_HILDRETH(MarrHildrethHash::create),

    /**
     * Rotation-invariant hash based on radial variance.
     */
    RADIAL_VARIANCE(RadialVarianceHash::create),

    /**
     * Block-based mean hash with mode 0 for regional analysis.
     */
    BLOCK_MEAN_ZERO(() -> BlockMeanHash.create(0)),

    /**
     * Block-based mean hash with mode 1 for enhanced regional analysis.
     */
    BLOCK_MEAN_ONE(() -> BlockMeanHash.create(1)),

    /**
     * Color-based hash using statistical moments.
     */
    COLOR_MOMENT(ColorMomentHash::create);

    /**
     * The algorithm factory value.
     */
    private final AlgorithmFactory algorithmFactory;

    /**
     * Creates a new instance.
     *
     * @param algorithmFactory the algorithm factory.
     */
    ImageContentHash(AlgorithmFactory algorithmFactory) {
        this.algorithmFactory = algorithmFactory;
    }

    /**
     * Gets the algorithm.
     *
     * @return the algorithm.
     */
    public ImgHashBase getAlgorithm() {
        return algorithmFactory.create();
    }

    /**
     * Compares two values.
     *
     * @param imgIn  the img in.
     * @param imgOut the img out.
     * @return the operation result.
     */
    public double compare(Mat imgIn, Mat imgOut) {
        Objects.requireNonNull(imgIn, "Input image cannot be null");
        Objects.requireNonNull(imgOut, "Output image cannot be null");

        if (imgIn.empty() || imgOut.empty()) {
            throw new IllegalArgumentException("Images cannot be empty");
        }

        ImgHashBase hashAlgorithm = getAlgorithm();
        Mat inHash = new Mat();
        Mat outHash = new Mat();
        hashAlgorithm.compute(imgIn, inHash);
        hashAlgorithm.compute(imgOut, outHash);
        return hashAlgorithm.compare(inHash, outHash);
    }

    /**
     * Defines the AlgorithmFactory contract.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FunctionalInterface
    private interface AlgorithmFactory {

        /**
         * Executes the create operation.
         *
         * @return the operation result.
         */
        ImgHashBase create();

    }

}
