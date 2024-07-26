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
package org.miaixz.bus.image.nimble.opencv;

import org.opencv.core.Mat;
import org.opencv.core.Size;

/**
 * @author Kimi Liu
 * @since Java 17+
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
