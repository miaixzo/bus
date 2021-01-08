/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.extra.qrcode;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import org.aoju.bus.core.image.Images;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.FileType;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.ImageKit;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于Zxing的二维码工具类
 *
 * @author Kimi Liu
 * @version 6.1.8
 * @since JDK 1.8+
 */
public class QrCodeKit {

    /**
     * 生成代 logo 图片的 Base64 编码格式的二维码，以 String 形式表示
     *
     * @param content    内容
     * @param qrConfig   二维码配置，包括长、宽、边距、颜色等
     * @param imageType  图片类型(图片扩展名)
     * @param logoBase64 logo 图片的 base64 编码
     * @return 图片 Base64 编码字符串
     */
    public static String generate(String content, QrConfig qrConfig, String imageType, String logoBase64) {
        byte[] decode;
        try {
            decode = Base64.getDecoder().decode(logoBase64);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
        return generate(content, qrConfig, imageType, decode);
    }

    /**
     * 生成代 logo 图片的 Base64 编码格式的二维码，以 String 形式表示
     *
     * @param content   内容
     * @param qrConfig  二维码配置，包括长、宽、边距、颜色等
     * @param imageType 图片类型(图片扩展名)
     * @param logo      logo 图片的byte[]
     * @return 图片 Base64 编码字符串
     */
    public static String generate(String content, QrConfig qrConfig, String imageType, byte[] logo) {
        BufferedImage img;
        try {
            img = ImageIO.read(new ByteArrayInputStream(logo));
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        qrConfig.setImg(img);
        return generate(content, qrConfig, imageType);
    }

    /**
     * 生成 Base64 编码格式的二维码，以 String 形式表示
     *
     * @param content   内容
     * @param qrConfig  二维码配置，包括长、宽、边距、颜色等
     * @param imageType 图片类型(图片扩展名)
     * @return 图片 Base64 编码字符串
     */
    public static String generate(String content, QrConfig qrConfig, String imageType) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        generate(content, qrConfig, imageType, bos);
        byte[] encode = Base64.getEncoder().encode(bos.toByteArray());
        return MessageFormat.format("data:image/{0};base64,{1}", imageType, new String(encode));
    }

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
        ImageKit.write(image, targetFile);
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
        ImageKit.write(image, targetFile);
        return targetFile;
    }

    /**
     * 生成二维码到输出流
     *
     * @param content   文本内容
     * @param width     宽度
     * @param height    高度
     * @param imageType 图片类型(图片扩展名),见{@link ImageKit}
     * @param out       目标流
     */
    public static void generate(String content, int width, int height, String imageType, OutputStream out) {
        final BufferedImage image = generate(content, width, height);
        ImageKit.write(image, imageType, out);
    }

    /**
     * 生成二维码到输出流
     *
     * @param content   文本内容
     * @param config    二维码配置,包括长、宽、边距、颜色等
     * @param imageType 图片类型(图片扩展名),见{@link ImageKit}
     * @param out       目标流
     */
    public static void generate(String content, QrConfig config, String imageType, OutputStream out) {
        final BufferedImage image = generate(content, config);
        ImageKit.write(image, imageType, out);
    }

    /**
     * 生成二维码图片
     *
     * @param content 文本内容
     * @param width   宽度
     * @param height  高度
     * @return 二维码图片(黑白)
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
     * @return 二维码图片(黑白)
     */
    public static BufferedImage generate(String content, BarcodeFormat format, int width, int height) {
        return generate(content, format, new QrConfig(width, height));
    }

    /**
     * 生成二维码图片
     *
     * @param content 文本内容
     * @param config  二维码配置,包括长、宽、边距、颜色等
     * @return 二维码图片(黑白)
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
     * @return 二维码图片(黑白)
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

            Images.from(image).pressImage(//
                    Images.from(logoImage).round(0.3).getImg(), // 圆角
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
        return decode(ImageKit.read(qrCodeInputstream));
    }

    /**
     * 解码二维码图片为文本
     *
     * @param qrCodeFile 二维码文件
     * @return 解码文本
     */
    public static String decode(File qrCodeFile) {
        return decode(ImageKit.read(qrCodeFile));
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

        final com.google.zxing.LuminanceSource source = new LuminanceSource(ImageKit.toBufferedImage(image));
        final Binarizer binarizer = new HybridBinarizer(source);
        final BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

        final Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, Charset.UTF_8);
        // 优化精度
        hints.put(DecodeHintType.TRY_HARDER, isTryHarder);
        // 复杂模式,开启PURE_BARCODE模式
        hints.put(DecodeHintType.PURE_BARCODE, isPureBarcode);
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
    public static BufferedImage toImage(BitMatrix matrix, int foreColor, Integer backColor) {
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, null == backColor ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
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

}
