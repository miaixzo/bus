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
package org.miaixz.bus.image.nimble.opencv.op;

import java.awt.*;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public class MaskArea {

    private final Color color;
    private final List<Shape> shapeList;

    public MaskArea(List<Shape> shapeList) {
        this(shapeList, null);
    }

    public MaskArea(List<Shape> shapeList, Color color) {
        this.shapeList = Objects.requireNonNull(shapeList);
        this.color = color;
    }

    public static ImageCV drawShape(Mat srcImg, MaskArea maskArea) {
        if (maskArea != null && !maskArea.getShapeList().isEmpty()) {
            Color c = maskArea.getColor();
            ImageCV dstImg = new ImageCV();
            srcImg.copyTo(dstImg);
            Scalar color = c == null ? new Scalar(0, 0, 0) : new Scalar(c.getBlue(), c.getGreen(), c.getRed());
            for (Shape shape : maskArea.getShapeList()) {
                if (c == null && shape instanceof Rectangle r) {
                    r = r.intersection(new Rectangle(0, 0, srcImg.width(), srcImg.height()));
                    Rect rect2d = new Rect(r.x, r.y, r.width, r.height);
                    if (r.width < 3 || r.height < 3) {
                        Logger.warn("The masking shape is not applicable: {}", r);
                    } else {
                        Imgproc.blur(srcImg.submat(rect2d), dstImg.submat(rect2d), new Size(7, 7));
                    }
                } else {
                    List<MatOfPoint> pts = ImageProcessor.transformShapeToContour(shape, true);
                    Imgproc.fillPoly(dstImg, pts, color);
                }
            }
            return dstImg;
        }
        return ImageCV.toImageCV(srcImg);
    }

    public Color getColor() {
        return color;
    }

    public List<Shape> getShapeList() {
        return shapeList;
    }

}
