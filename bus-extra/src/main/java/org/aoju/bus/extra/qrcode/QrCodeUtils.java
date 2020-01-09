/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.extra.qrcode;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.FileType;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * 基于Zxing的二维码工具类
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public class QrCodeUtils {

    /**
     * 生成PNG格式的二维码图片,以byte[]形式表示
     *
     * @param content 内容
     * @param width   宽度
     * @param height  高度
     * @return 图片的byte[]
     */
    public static byte[] generatePng(String content, int width, int height) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        generate(content, width, height, FileType.TYPE_PNG, out);
        return out.toByteArray();
    }

    /**
     * 生成PNG格式的二维码图片,以byte[]形式表示
     *
     * @param content 内容
     * @param config  二维码配置,包括长、宽、边距、颜色等
     * @return 图片的byte[]
     */
    public static byte[] generatePng(String content, QrConfig config) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        generate(content, config, FileType.TYPE_PNG, out);
        return out.toByteArray();
    }

    /**
     * 生成二维码到文件,二维码图片格式取决于文件的扩展名
     *
     * @param content    文本内容
     * @param width      宽度
     * @param height     高度
     * @param targetFile 目标文件,扩展名决定输出格式
     * @return 目标文件
     */
    public static File generate(String content, int width, int height, File targetFile) {
        final BufferedImage image = generate(content, width, height);
        ImageUtils.write(image, targetFile);
        return targetFile;
    }

    /**
     * 生成二维码到文件,二维码图片格式取决于文件的扩展名
     *
     * @param content    文本内容
     * @param config     二维码配置,包括长、宽、边距、颜色等
     * @param targetFile 目标文件,扩展名决定输出格式
     * @return 目标文件
     */
    public static File generate(String content, QrConfig config, File targetFile) {
        final BufferedImage image = generate(content, config);
        ImageUtils.write(image, targetFile);
        return targetFile;
    }

    /**
     * 生成二维码到输出流
     *
     * @param content   文本内容
     * @param width     宽度
     * @param height    高度
     * @param imageType 图片类型（图片扩展名）,见{@link ImageUtils}
     * @param out       目标流
     */
    public static void generate(String content, int width, int height, String imageType, OutputStream out) {
        final BufferedImage image = generate(content, width, height);
        ImageUtils.write(image, imageType, out);
    }

    /**
     * 生成二维码到输出流
     *
     * @param content   文本内容
     * @param config    二维码配置,包括长、宽、边距、颜色等
     * @param imageType 图片类型（图片扩展名）,见{@link ImageUtils}
     * @param out       目标流
     */
    public static void generate(String content, QrConfig config, String imageType, OutputStream out) {
        final BufferedImage image = generate(content, config);
        ImageUtils.write(image, imageType, out);
    }

    /**
     * 生成二维码图片
     *
     * @param content 文本内容
     * @param width   宽度
     * @param height  高度
     * @return 二维码图片（黑白）
     */
    public static BufferedImage generate(String content, int width, int height) {
        return generate(content, new QrConfig(width, height));
    }

    /**
     * 生成二维码或条形码图片
     *
     * @param content 文本内容
     * @param format  格式,可选二维码或者条形码
     * @param width   宽度
     * @param height  高度
     * @return 二维码图片（黑白）
     */
    public static BufferedImage generate(String content, BarcodeFormat format, int width, int height) {
        return generate(content, format, new QrConfig(width, height));
    }

    /**
     * 生成二维码图片
     *
     * @param content 文本内容
     * @param config  二维码配置,包括长、宽、边距、颜色等
     * @return 二维码图片（黑白）
     */
    public static BufferedImage generate(String content, QrConfig config) {
        return generate(content, BarcodeFormat.QR_CODE, config);
    }

    /**
     * 生成二维码或条形码图片
     * 只有二维码时QrConfig中的图片才有效
     *
     * @param content 文本内容
     * @param format  格式,可选二维码、条形码等
     * @param config  二维码配置,包括长、宽、边距、颜色等
     * @return 二维码图片（黑白）
     */
    public static BufferedImage generate(String content, BarcodeFormat format, QrConfig config) {
        final BitMatrix bitMatrix = encode(content, format, config);
        final BufferedImage image = toImage(bitMatrix, config.foreColor, config.backColor);
        final java.awt.Image logoImage = config.img;
        if (null != logoImage && BarcodeFormat.QR_CODE == format) {
            // 只有二维码可以贴图
            final int qrWidth = image.getWidth();
            final int qrHeight = image.getHeight();
            int width;
            int height;
            // 按照最短的边做比例缩放
            if (qrWidth < qrHeight) {
                width = qrWidth / config.ratio;
                height = logoImage.getHeight(null) * width / logoImage.getWidth(null);
            } else {
                height = qrHeight / config.ratio;
                width = logoImage.getWidth(null) * height / logoImage.getHeight(null);
            }

            org.aoju.bus.core.image.Image.from(image).pressImage(//
                    org.aoju.bus.core.image.Image.from(logoImage).round(0.3).getImg(), // 圆角
                    new Rectangle(width, height), //
                    1//
            );
        }
        return image;
    }

    /**
     * 将文本内容编码为二维码
     *
     * @param content 文本内容
     * @param width   宽度
     * @param height  高度
     * @return {@link BitMatrix}
     */
    public static BitMatrix encode(String content, int width, int height) {
        return encode(content, BarcodeFormat.QR_CODE, width, height);
    }

    /**
     * 将文本内容编码为二维码
     *
     * @param content 文本内容
     * @param config  二维码配置,包括长、宽、边距、颜色等
     * @return {@link BitMatrix}
     */
    public static BitMatrix encode(String content, QrConfig config) {
        return encode(content, BarcodeFormat.QR_CODE, config);
    }

    /**
     * 将文本内容编码为条形码或二维码
     *
     * @param content 文本内容
     * @param format  格式枚举
     * @param width   宽度
     * @param height  高度
     * @return {@link BitMatrix}
     */
    public static BitMatrix encode(String content, BarcodeFormat format, int width, int height) {
        return encode(content, format, new QrConfig(width, height));
    }

    /**
     * 将文本内容编码为条形码或二维码
     *
     * @param content 文本内容
     * @param format  格式枚举
     * @param config  二维码配置,包括长、宽、边距、颜色等
     * @return {@link BitMatrix}
     */
    public static BitMatrix encode(String content, BarcodeFormat format, QrConfig config) {
        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        if (null == config) {
            // 默认配置
            config = new QrConfig();
        }
        BitMatrix bitMatrix;
        try {
            bitMatrix = multiFormatWriter.encode(content, format, config.width, config.height, config.toHints());
        } catch (WriterException e) {
            throw new InstrumentException(e);
        }

        return bitMatrix;
    }

    /**
     * 解码二维码图片为文本
     *
     * @param qrCodeInputstream 二维码输入流
     * @return 解码文本
     */
    public static String decode(InputStream qrCodeInputstream) {
        return decode(ImageUtils.read(qrCodeInputstream));
    }

    /**
     * 解码二维码图片为文本
     *
     * @param qrCodeFile 二维码文件
     * @return 解码文本
     */
    public static String decode(File qrCodeFile) {
        return decode(ImageUtils.read(qrCodeFile));
    }

    /**
     * 将二维码图片解码为文本
     *
     * @param image {@link java.awt.Image} 二维码图片
     * @return 解码后的文本
     */
    public static String decode(java.awt.Image image) {
        return decode(image, true, false);
    }

    /**
     * 将二维码图片解码为文本
     *
     * @param image         {@link java.awt.Image} 二维码图片
     * @param isTryHarder   是否优化精度
     * @param isPureBarcode 是否使用复杂模式,扫描带logo的二维码设为true
     * @return 解码后的文本
     */
    public static String decode(java.awt.Image image, boolean isTryHarder, boolean isPureBarcode) {
        final MultiFormatReader formatReader = new MultiFormatReader();

        final LuminanceSource source = new BufferedImageLuminanceSource(ImageUtils.toBufferedImage(image));
        final Binarizer binarizer = new HybridBinarizer(source);
        final BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

        final HashMap<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, Charset.UTF_8);
        // 优化精度
        hints.put(DecodeHintType.TRY_HARDER, Boolean.valueOf(isTryHarder));
        // 复杂模式,开启PURE_BARCODE模式
        hints.put(DecodeHintType.PURE_BARCODE, Boolean.valueOf(isPureBarcode));
        Result result;
        try {
            result = formatReader.decode(binaryBitmap, hints);
        } catch (NotFoundException e) {
            // 报错尝试关闭复杂模式
            hints.remove(DecodeHintType.PURE_BARCODE);
            try {
                result = formatReader.decode(binaryBitmap, hints);
            } catch (NotFoundException e1) {
                throw new InstrumentException(e1);
            }
        }

        return result.getText();
    }

    /**
     * BitMatrix转BufferedImage
     *
     * @param matrix    BitMatrix
     * @param foreColor 前景色
     * @param backColor 背景色
     * @return BufferedImage
     */
    public static BufferedImage toImage(BitMatrix matrix, int foreColor, int backColor) {
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? foreColor : backColor);
            }
        }
        return image;
    }

}
