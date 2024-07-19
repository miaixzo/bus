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
package org.miaixz.bus.image.nimble.opencv.op;

import org.miaixz.bus.image.nimble.opencv.ImageCV;
import org.miaixz.bus.image.nimble.opencv.ImageProcessor;
import org.miaixz.bus.logger.Logger;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
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
            Scalar color =
                    c == null ? new Scalar(0, 0, 0) : new Scalar(c.getBlue(), c.getGreen(), c.getRed());
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
