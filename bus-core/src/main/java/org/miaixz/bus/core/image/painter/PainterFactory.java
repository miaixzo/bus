/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.image.painter;

import org.miaixz.bus.core.image.element.AbstractElement;
import org.miaixz.bus.core.image.element.ImageElement;
import org.miaixz.bus.core.image.element.RectangleElement;
import org.miaixz.bus.core.image.element.TextElement;

/**
 * 绘制工厂
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PainterFactory {

    /**
     * 图片绘制器
     */
    private static ImagePainter imagePainter;
    /**
     * 文本绘制器
     */
    private static TextPainter textPainter;
    /**
     * 矩形绘制器
     */
    private static RectanglePainter rectanglePainter;

    public static Painter newInstance(AbstractElement element) throws Exception {
        // 考虑到性能，这里用单件，先不lock了
        if (element instanceof ImageElement) {
            if (null == imagePainter) {
                imagePainter = new ImagePainter();
            }
            return imagePainter;
        } else if (element instanceof TextElement) {
            if (null == textPainter) {
                textPainter = new TextPainter();
            }
            return textPainter;
        } else if (element instanceof RectangleElement) {
            if (rectanglePainter == null) {
                rectanglePainter = new RectanglePainter();
            }
            return rectanglePainter;
        } else {
            throw new Exception("不支持的Painter类型");
        }
    }

}
