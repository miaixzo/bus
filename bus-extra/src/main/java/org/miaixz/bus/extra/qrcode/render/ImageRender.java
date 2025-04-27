/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.qrcode.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

import org.miaixz.bus.extra.image.ImageKit;
import org.miaixz.bus.extra.image.Images;
import org.miaixz.bus.extra.qrcode.QrConfig;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;

/**
 * 二维码图片渲染器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageRender implements BitMatrixRender {

    private final QrConfig config;
    private final String imageType;

    /**
     * 构造
     *
     * @param config    二维码配置
     * @param imageType 图片类型
     */
    public ImageRender(final QrConfig config, final String imageType) {
        this.config = config;
        this.imageType = imageType;
    }

    @Override
    public void render(final BitMatrix matrix, final OutputStream out) {
        BufferedImage img = null;
        try {
            img = render(matrix);
            ImageKit.write(img, imageType, out);
        } finally {
            ImageKit.flush(img);
        }
    }

    /**
     * 渲染
     *
     * @param matrix 二维码矩阵
     * @return 图片
     */
    public BufferedImage render(final BitMatrix matrix) {
        final BufferedImage image = getBufferedImage(matrix);

        final Image logo = config.getImg();
        if (null != logo && BarcodeFormat.QR_CODE == config.getFormat()) {
            pressLogo(image, logo);
        }
        return image;
    }

    /**
     * 获取图片
     *
     * @param matrix 二维码矩阵
     * @return 图片
     */
    private BufferedImage getBufferedImage(final BitMatrix matrix) {
        final BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(),
                null == config.getBackColor() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        final Integer foreColor = config.getForeColor();
        final Integer backColor = config.getBackColor();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    image.setRGB(x, y, foreColor);
                } else if (null != backColor) {
                    image.setRGB(x, y, backColor);
                }
            }
        }
        return image;
    }

    /**
     * 贴图
     *
     * @param image   二维码图片
     * @param logoImg logo图片
     */
    private void pressLogo(final BufferedImage image, final Image logoImg) {
        // 只有二维码可以贴图
        final int qrWidth = image.getWidth();
        final int qrHeight = image.getHeight();
        final int imgWidth;
        final int imgHeight;
        // 按照最短的边做比例缩放
        if (qrWidth < qrHeight) {
            imgWidth = qrWidth / config.getRatio();
            imgHeight = logoImg.getHeight(null) * imgWidth / logoImg.getWidth(null);
        } else {
            imgHeight = qrHeight / config.getRatio();
            imgWidth = logoImg.getWidth(null) * imgHeight / logoImg.getHeight(null);
        }

        // 原图片上直接绘制水印
        Images.from(image).pressImage(//
                Images.from(logoImg).round(config.getImgRound()).getImg(), // 圆角
                new Rectangle(imgWidth, imgHeight), // 位置
                1// 不透明
        );
    }

}
