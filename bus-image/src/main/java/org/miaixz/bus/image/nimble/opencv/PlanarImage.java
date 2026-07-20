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
package org.miaixz.bus.image.nimble.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * Represents a planar image with OpenCV Mat functionality. Provides resource management through AutoCloseable and
 * memory size calculation through ImageSize.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface PlanarImage extends ImageSize, AutoCloseable {

    /**
     * Executes the channels operation.
     *
     * @return the operation result.
     */
    int channels();

    /**
     * Executes the dims operation.
     *
     * @return the operation result.
     */
    int dims();

    /**
     * Executes the depth operation.
     *
     * @return the operation result.
     */
    int depth();

    /**
     * Executes the elem size operation.
     *
     * @return the operation result.
     */
    long elemSize();

    /**
     * Executes the elem size 1 operation.
     *
     * @return the operation result.
     */
    long elemSize1();

    /**
     * Executes the size operation.
     *
     * @return the operation result.
     */
    Size size();

    /**
     * Executes the type operation.
     *
     * @return the operation result.
     */
    int type();

    /**
     * Executes the height operation.
     *
     * @return the operation result.
     */
    int height();

    /**
     * Executes the width operation.
     *
     * @return the operation result.
     */
    int width();

    /**
     * Returns the get.
     *
     * @param row    the row.
     * @param column the column.
     * @return the get.
     */
    double[] get(int row, int column);

    /**
     * Returns the get.
     *
     * @param i         the i.
     * @param j         the j.
     * @param pixelData the pixel data.
     * @return the get.
     */
    int get(int i, int j, byte[] pixelData);

    /**
     * Returns the get.
     *
     * @param i    the i.
     * @param j    the j.
     * @param data the data.
     * @return the get.
     */
    int get(int i, int j, short[] data);

    /**
     * Returns the get.
     *
     * @param i    the i.
     * @param j    the j.
     * @param data the data.
     * @return the get.
     */
    int get(int i, int j, int[] data);

    /**
     * Returns the get.
     *
     * @param i    the i.
     * @param j    the j.
     * @param data the data.
     * @return the get.
     */
    int get(int i, int j, float[] data);

    /**
     * Returns the get.
     *
     * @param i    the i.
     * @param j    the j.
     * @param data the data.
     * @return the get.
     */
    int get(int i, int j, double[] data);

    /**
     * Executes the assign to operation.
     *
     * @param dstImg the dst img.
     */
    void assignTo(Mat dstImg);

    /**
     * Executes the release operation.
     */
    void release();

    /**
     * Checks whether the released condition is true.
     *
     * @return true if the released condition is true; otherwise false.
     */
    boolean isReleased();

    /**
     * Checks whether the released after processing condition is true.
     *
     * @return true if the released after processing condition is true; otherwise false.
     */
    boolean isReleasedAfterProcessing();

    /**
     * Sets the released after processing.
     *
     * @param releasedAfterProcessing the released after processing.
     */
    void setReleasedAfterProcessing(boolean releasedAfterProcessing);

    /**
     * Executes the close operation.
     */
    @Override
    void close();

    /**
     * Executes the to mat operation.
     *
     * @return the operation result.
     */
    default Mat toMat() {
        if (this instanceof Mat mat) {
            return mat;
        }
        throw new UnsupportedOperationException("Conversion to Mat not supported for this implementation");
    }

    /**
     * Executes the to image CV operation.
     *
     * @return the operation result.
     */
    default ImageCV toImageCV() {
        if (this instanceof ImageCV imageCV) {
            return imageCV;
        }
        if (this instanceof Mat mat) {
            return ImageCV.fromMat(mat);
        }
        throw new UnsupportedOperationException("Conversion to ImageCV not supported for this implementation");
    }

}
