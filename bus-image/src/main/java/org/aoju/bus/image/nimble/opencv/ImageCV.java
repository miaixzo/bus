/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.nimble.opencv;

import org.opencv.core.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageCV extends Mat implements PlanarImage {

    public ImageCV() {
        super();
    }

    public ImageCV(int rows, int cols, int type) {
        super(rows, cols, type);
    }

    public ImageCV(Size size, int type, Scalar s) {
        super(size, type, s);
    }

    public ImageCV(int rows, int cols, int type, Scalar s) {
        super(rows, cols, type, s);
    }

    public ImageCV(Mat m, Range rowRange, Range colRange) {
        super(m, rowRange, colRange);
    }

    public ImageCV(Mat m, Range rowRange) {
        super(m, rowRange);
    }

    public ImageCV(Mat m, Rect roi) {
        super(m, roi);
    }

    public ImageCV(Size size, int type) {
        super(size, type);
    }

    public static Mat toMat(PlanarImage source) {
        if (source instanceof Mat) {
            return (Mat) source;
        } else {
            throw new IllegalAccessError("Not implemented yet");
        }
    }

    public static ImageCV toImageCV(Mat source) {
        if (source instanceof ImageCV) {
            return (ImageCV) source;
        }
        ImageCV dstImg = new ImageCV();
        source.assignTo(dstImg);
        return dstImg;
    }

    @Override
    public long physicalBytes() {
        return total() * elemSize();
    }

    // TODO remove for Java 8
    @Override
    public Mat toMat() {
        if (this instanceof Mat) {
            return this;
        } else {
            throw new IllegalAccessError("Not implemented yet");
        }
    }

    @Override
    public ImageCV toImageCV() {
        if (this instanceof Mat) {
            if (this instanceof ImageCV) {
                return this;
            }
            ImageCV dstImg = new ImageCV();
            this.assignTo(dstImg);
            return dstImg;
        } else {
            throw new IllegalAccessError("Not implemented yet");
        }
    }

    @Override
    public void close() {
        this.release();
    }

}
