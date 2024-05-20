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
package org.miaixz.bus.extra.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import org.miaixz.bus.extra.image.Images;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 二维码图片封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrImage extends BufferedImage {

    /**
     * 构造
     *
     * @param content 文本内容
     * @param config  {@link QrConfig} 二维码配置，包括宽度、高度、边距、颜色、格式等
     */
    public QrImage(final String content, final QrConfig config) {
        this(QrCodeKit.encode(content, config), config);
    }

    /**
     * 构造
     *
     * @param matrix {@link BitMatrix}
     * @param config {@link QrConfig}，非空
     */
    public QrImage(final BitMatrix matrix, final QrConfig config) {
        super(matrix.getWidth(), matrix.getHeight(), null == config.backColor ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        init(matrix, config);
    }

    /**
     * 初始化
     *
     * @param matrix {@link BitMatrix}
     * @param config {@link QrConfig}
     */
    private void init(final BitMatrix matrix, final QrConfig config) {
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        final Integer foreColor = config.foreColor;
        final Integer backColor = config.backColor;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    setRGB(x, y, foreColor);
                } else if (null != backColor) {
                    setRGB(x, y, backColor);
                }
            }
        }

        final Image logoImg = config.img;
        if (null != logoImg && BarcodeFormat.QR_CODE == config.format) {
            // 只有二维码可以贴图
            final int qrWidth = getWidth();
            final int qrHeight = getHeight();
            final int imgWidth;
            final int imgHeight;
            // 按照最短的边做比例缩放
            if (qrWidth < qrHeight) {
                imgWidth = qrWidth / config.ratio;
                imgHeight = logoImg.getHeight(null) * imgWidth / logoImg.getWidth(null);
            } else {
                imgHeight = qrHeight / config.ratio;
                imgWidth = logoImg.getWidth(null) * imgHeight / logoImg.getHeight(null);
            }

            // 原图片上直接绘制水印
            Images.from(this).pressImage(//
                    Images.from(logoImg).round(config.imgRound).getImg(), // 圆角
                    new Rectangle(imgWidth, imgHeight), // 位置
                    1//不透明
            );
        }
    }

}
