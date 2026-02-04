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
package org.miaixz.bus.image.nimble.opencv;

import org.opencv.core.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageCV extends Mat implements PlanarImage {

    private boolean releasedAfterProcessing;
    private boolean hasBeenReleased = false;

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
        if (source instanceof Mat mat) {
            return mat;
        } else {
            throw new IllegalAccessError("Not implemented yet");
        }
    }

    public static ImageCV toImageCV(Mat source) {
        if (source instanceof ImageCV img) {
            return img;
        }
        ImageCV dstImg = new ImageCV();
        source.assignTo(dstImg);
        return dstImg;
    }

    @Override
    public long physicalBytes() {
        return total() * elemSize();
    }

    @Override
    public void release() {
        if (!hasBeenReleased) {
            super.release();
            this.hasBeenReleased = true;
        }
    }

    public boolean isHasBeenReleased() {
        return hasBeenReleased;
    }

    public boolean isReleasedAfterProcessing() {
        return releasedAfterProcessing;
    }

    public void setReleasedAfterProcessing(boolean releasedAfterProcessing) {
        this.releasedAfterProcessing = releasedAfterProcessing;
    }

    @Override
    public void close() {
        release();
    }

}
