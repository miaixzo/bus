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
 * @author Kimi Liu
 * @since Java 21+
 */
public interface PlanarImage extends ImageSize, AutoCloseable {

    int channels();

    int dims();

    int depth();

    long elemSize();

    long elemSize1();

    void release();

    Size size();

    int type();

    int height();

    int width();

    double[] get(int row, int column);

    int get(int i, int j, byte[] pixelData);

    int get(int i, int j, short[] data);

    int get(int i, int j, int[] data);

    int get(int i, int j, float[] data);

    int get(int i, int j, double[] data);

    void assignTo(Mat dstImg);

    boolean isHasBeenReleased();

    boolean isReleasedAfterProcessing();

    void setReleasedAfterProcessing(boolean releasedAfterProcessing);

    @Override
    void close();

    default Mat toMat() {
        if (this instanceof Mat mat) {
            return mat;
        } else {
            throw new IllegalAccessError("Not implemented yet");
        }
    }

    default ImageCV toImageCV() {
        if (this instanceof Mat) {
            if (this instanceof ImageCV img) {
                return img;
            }
            ImageCV dstImg = new ImageCV();
            this.assignTo(dstImg);
            return dstImg;
        } else {
            throw new IllegalAccessError("Not implemented yet");
        }
    }

}
