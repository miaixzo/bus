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

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.image.Images;
import org.miaixz.bus.core.lang.Ansi;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.FileType;
import org.miaixz.bus.core.toolkit.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于Zxing的二维码工具类
 * <ul>
 *     <li>二维码生成和识别，见{@link BarcodeFormat#QR_CODE}</li>
 *     <li>条形码生成和识别，见{@link BarcodeFormat#CODE_39}等很多标准格式</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrCodeKit {

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
        String extName = FileKit.getSuffix(targetFile);
        switch (extName) {
            case FileType.TYPE_SVG:
                String svg = generateAsSvg(content, new QrConfig(width, height));
                FileKit.writeString(svg, targetFile, Charset.UTF_8);
                break;
            case FileType.TYPE_TXT:
                String txt = generateAsAsciiArt(content, new QrConfig(width, height));
                FileKit.writeString(txt, targetFile, Charset.UTF_8);
                break;
            default:
                final BufferedImage image = generate(content, width, height);
                ImageKit.write(image, targetFile);
                break;
        }

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
        String extName = FileKit.getSuffix(targetFile);
        switch (extName) {
            case FileType.TYPE_SVG:
                final String svg = generateAsSvg(content, config);
                FileKit.writeString(svg, targetFile, Charset.UTF_8);
                break;
            case FileType.TYPE_TXT:
                final String txt = generateAsAsciiArt(content, config);
                FileKit.writeString(txt, targetFile, Charset.UTF_8);
                break;
            default:
                final BufferedImage image = generate(content, config);
                ImageKit.write(image, targetFile);
                break;
        }
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
        switch (imageType) {
            case FileType.TYPE_SVG:
                final String svg = generateAsSvg(content, new QrConfig(width, height));
                IoKit.writeUtf8(out, false, svg);
                break;
            case FileType.TYPE_TXT:
                final String txt = generateAsAsciiArt(content, new QrConfig(width, height));
                IoKit.writeUtf8(out, false, txt);
                break;
            default:
                final BufferedImage image = generate(content, width, height);
                ImageKit.write(image, imageType, out);
                break;
        }
    }

    /**
     * 生成二维码到输出流
     *
     * @param content   文本内容
     * @param config    二维码配置，包括长、宽、边距、颜色等
     * @param imageType 类型（图片扩展名）
     * @param out       目标流
     */
    public static void generate(String content, QrConfig config, String imageType, OutputStream out) {
        switch (imageType) {
            case FileType.TYPE_SVG:
                final String svg = generateAsSvg(content, config);
                IoKit.writeUtf8(out, false, svg);
                break;
            case FileType.TYPE_TXT:
                final String txt = generateAsAsciiArt(content, config);
                IoKit.writeUtf8(out, false, txt);
                break;
            default:
                final BufferedImage image = generate(content, config);
                ImageKit.write(image, imageType, out);
                break;
        }
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
     * 生成代 logo 图片的 Base64 编码格式的二维码，以 String 形式表示
     *
     * @param content    内容
     * @param qrConfig   二维码配置，包括长、宽、边距、颜色等
     * @param imageType  图片类型(图片扩展名)
     * @param logoBase64 logo 图片的 base64 编码
     * @return 图片 Base64 编码字符串
     */
    public static String generateAsBase64(String content, QrConfig qrConfig, String imageType, String logoBase64) {
        return generateAsBase64(content, qrConfig, imageType, Base64.decode(logoBase64));
    }

    /**
     * 生成代 logo 图片的 Base64 编码格式的二维码，以 String 形式表示
     *
     * @param content   内容
     * @param qrConfig  二维码配置，包括长、宽、边距、颜色等
     * @param imageType 类型（图片扩展名）
     * @param logo      logo 图片的byte[]
     * @return 图片 Base64 编码字符串
     */
    public static String generateAsBase64(String content, QrConfig qrConfig, String imageType, byte[] logo) {
        return generateAsBase64(content, qrConfig, imageType, ImageKit.toImage(logo));
    }

    /**
     * 生成代 logo 图片的 Base64 编码格式的二维码，以 String 形式表示
     *
     * @param content   内容
     * @param qrConfig  二维码配置，包括长、宽、边距、颜色等
     * @param imageType 类型（图片扩展名）
     * @param logo      logo 图片的byte[]
     * @return 图片 Base64 编码字符串
     */
    public static String generateAsBase64(String content, QrConfig qrConfig, String imageType, Image logo) {
        qrConfig.setImg(logo);
        return generateAsBase64(content, qrConfig, imageType);
    }

    /**
     * 生成 Base64 编码格式的二维码，以 String 形式表示
     *
     * <p>
     * 输出格式为: data:image/[type];base64,[data]
     * </p>
     *
     * @param content   内容
     * @param qrConfig  二维码配置，包括长、宽、边距、颜色等
     * @param imageType 类型（图片扩展名）
     * @return 图片 Base64 编码字符串
     */
    public static String generateAsBase64(String content, QrConfig qrConfig, String imageType) {
        String result;
        switch (imageType) {
            case FileType.TYPE_SVG:
                String svg = generateAsSvg(content, qrConfig);
                result = UriKit.toURL("image/svg+xml", "base64", Base64.encode(svg));
                break;
            case FileType.TYPE_TXT:
                String txt = generateAsAsciiArt(content, qrConfig);
                result = UriKit.toURL("text/plain", "base64", Base64.encode(txt));
                break;
            default:
                final BufferedImage img = generate(content, qrConfig);
                result = ImageKit.toBase64Uri(img, imageType);
                break;
        }


        return result;
    }

    /**
     * 生成PNG格式的二维码图片，以byte[]形式表示
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
     * @param content  内容
     * @param qrConfig 二维码配置，包括长、宽、边距、颜色等
     * @return SVG矢量图（字符串）
     */
    public static String generateAsSvg(String content, QrConfig qrConfig) {
        return toSVG(encode(content, qrConfig), qrConfig.foreColor, qrConfig.backColor, qrConfig.img, qrConfig.getRatio());
    }

    /**
     * 生成ASCII Art字符画形式的二维码
     *
     * @param content 内容
     * @return ASCII Art字符画形式的二维码字符串
     */
    public static String generateAsAsciiArt(String content) {
        return generateAsAsciiArt(content, 0, 0, 1);
    }

    /**
     * 生成ASCII Art字符画形式的二维码
     *
     * @param content  内容
     * @param qrConfig 二维码配置，仅长、宽、边距配置有效
     * @return ASCII Art字符画形式的二维码
     */
    public static String generateAsAsciiArt(String content, QrConfig qrConfig) {
        return toAsciiArt(encode(content, qrConfig), qrConfig);
    }

    /**
     * @param content 内容
     * @param width   宽
     * @param height  长
     * @return ASCII Art字符画形式的二维码
     */
    public static String generateAsAsciiArt(String content, int width, int height, int margin) {
        QrConfig qrConfig = new QrConfig(width, height).setMargin(margin);
        return generateAsAsciiArt(content, qrConfig);
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
            bitMatrix = multiFormatWriter.encode(content, format, config.width, config.height, config.toHints(format));
        } catch (WriterException e) {
            throw new InternalException(e);
        }

        return bitMatrix;
    }

    /**
     * 解码二维码或条形码图片为文本
     *
     * @param qrCodeInputstream 二维码输入流
     * @return 解码文本
     */
    public static String decode(InputStream qrCodeInputstream) {
        return decode(ImageKit.read(qrCodeInputstream));
    }

    /**
     * 解码二维码或条形码图片为文本
     *
     * @param qrCodeFile 二维码文件
     * @return 解码文本
     */
    public static String decode(File qrCodeFile) {
        return decode(ImageKit.read(qrCodeFile));
    }

    /**
     * 将二维码或条形码图片解码为文本
     *
     * @param image {@link java.awt.Image} 二维码图片
     * @return 解码后的文本
     */
    public static String decode(java.awt.Image image) {
        return decode(image, true, false);
    }

    /**
     * 将二维码或条形码图片解码为文本
     * 此方法会尝试使用{@link HybridBinarizer}和{@link GlobalHistogramBinarizer}两种模式解析
     * 需要注意部分二维码如果不带logo，使用PureBarcode模式会解析失败，此时须设置此选项为false
     *
     * @param image         {@link Image} 二维码图片
     * @param isTryHarder   是否优化精度
     * @param isPureBarcode 是否使用复杂模式，扫描带logo的二维码设为true
     * @return 解码后的文本
     */
    public static String decode(Image image, boolean isTryHarder, boolean isPureBarcode) {
        return decode(image, buildHints(isTryHarder, isPureBarcode));
    }

    /**
     * 将二维码或条形码图片解码为文本
     * 此方法会尝试使用{@link HybridBinarizer}和{@link GlobalHistogramBinarizer}两种模式解析
     * 需要注意部分二维码如果不带logo，使用PureBarcode模式会解析失败，此时须设置此选项为false
     *
     * @param image {@link Image} 二维码图片
     * @param hints 自定义扫码配置，包括算法、编码、复杂模式等
     * @return 解码后的文本
     */
    public static String decode(Image image, Map<DecodeHintType, Object> hints) {
        final MultiFormatReader formatReader = new MultiFormatReader();
        formatReader.setHints(hints);

        final com.google.zxing.LuminanceSource source = new org.miaixz.bus.extra.qrcode.LuminanceSource(ImageKit.toBufferedImage(image));

        Result result = _decode(formatReader, new HybridBinarizer(source));
        if (null == result) {
            result = _decode(formatReader, new GlobalHistogramBinarizer(source));
        }

        return null != result ? result.getText() : null;
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

    /**
     * BitMatrix转SVG(字符串)
     *
     * @param matrix    二维的位矩阵
     * @param foreColor 前景色
     * @param backColor 背景色(null表示透明背景)
     * @param ratio     二维码中的Logo缩放的比例系数，如5表示长宽最小值的1/5
     * @return SVG矢量图（字符串）
     */
    public static String toSVG(BitMatrix matrix, int foreColor, Integer backColor, Image logoImg, int ratio) {
        StringBuilder sb = new StringBuilder();
        int qrWidth = matrix.getWidth();
        int qrHeight = matrix.getHeight();
        int moduleHeight = (qrHeight == 1) ? qrWidth / 2 : 1;
        for (int y = 0; y < qrHeight; y++) {
            for (int x = 0; x < qrWidth; x++) {
                if (matrix.get(x, y)) {
                    sb.append(" M" + x + "," + y + "h1v" + moduleHeight + "h-1z");
                }
            }
        }
        qrHeight *= moduleHeight;
        String logoBase64 = "";
        int logoWidth = 0;
        int logoHeight = 0;
        int logoX = 0;
        int logoY = 0;
        if (logoImg != null) {
            logoBase64 = ImageKit.toBase64Uri(logoImg, "png");
            // 按照最短的边做比例缩放
            if (qrWidth < qrHeight) {
                logoWidth = qrWidth / ratio;
                logoHeight = logoImg.getHeight(null) * logoWidth / logoImg.getWidth(null);
            } else {
                logoHeight = qrHeight / ratio;
                logoWidth = logoImg.getWidth(null) * logoHeight / logoImg.getHeight(null);
            }
            logoX = (qrWidth - logoWidth) / 2;
            logoY = (qrHeight - logoHeight) / 2;

        }

        Color fore = new Color(foreColor, true);

        StringBuilder result = StringKit.builder();
        result.append("<svg width=\"" + qrWidth + "\" height=\"" + qrHeight + "\" \n");
        if (backColor != null) {
            Color back = new Color(backColor, true);
            result.append("style=\"background-color:rgba(" + back.getRed() + "," + back.getGreen() + "," + back.getBlue() + "," + back.getAlpha() + ")\"\n");
        }
        result.append("viewBox=\"0 0 " + qrWidth + " " + qrHeight + "\" \n");
        result.append("xmlns=\"http://www.w3.org/2000/svg\" \n");
        result.append("xmlns:xlink=\"http://www.w3.org/1999/xlink\" >\n");
        result.append("<path d=\"" + sb + "\" stroke=\"rgba(" + fore.getRed() + "," + fore.getGreen() + "," + fore.getBlue() + "," + fore.getAlpha() + ")\" /> \n");
        if (StringKit.isNotBlank(logoBase64)) {
            result.append("<image xlink:href=\"" + logoBase64 + "\" height=\"" + logoHeight + "\" width=\"" + logoWidth + "\" y=\"" + logoY + "\" x=\"" + logoX + "\" />\n");
        }
        result.append("</svg>");
        return result.toString();
    }

    /**
     * BitMatrix转ASCII Art字符画形式的二维码
     *
     * @param bitMatrix
     * @return ASCII Art字符画形式的二维码
     */
    public static String toAsciiArt(BitMatrix bitMatrix, QrConfig qrConfig) {
        final int width = bitMatrix.getWidth();
        final int height = bitMatrix.getHeight();


        final Ansi.Element foreground = qrConfig.foreColor == null ? null : Ansi.Color8Bit.foreground(rgbToAnsi8BitValue(qrConfig.foreColor));
        final Ansi.Element background = qrConfig.backColor == null ? null : Ansi.Color8Bit.background(rgbToAnsi8BitValue(qrConfig.backColor));

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= height; i += 2) {
            StringBuilder rowBuilder = new StringBuilder();
            for (int j = 0; j < width; j++) {
                boolean tp = bitMatrix.get(i, j);
                boolean bt = i + 1 >= height || bitMatrix.get(i + 1, j);
                if (tp && bt) {
                    rowBuilder.append(' ');//'\u0020'
                } else if (tp) {
                    rowBuilder.append('▄');//'\u2584'
                } else if (bt) {
                    rowBuilder.append('▀');//'\u2580'
                } else {
                    rowBuilder.append('█');//'\u2588'
                }
            }
            builder.append(Ansi.encode(foreground, background, rowBuilder)).append('\n');
        }
        return builder.toString();
    }

    /**
     * rgb转Ansi8Bit值
     *
     * @param rgb rgb颜色值
     * @return Ansi8bit颜色值
     */
    private static int rgbToAnsi8BitValue(int rgb) {
        final int r = (rgb >> 16) & 0xff;
        final int g = (rgb >> 8) & 0xff;
        final int b = (rgb) & 0xff;

        final int l;
        if (r == g && g == b) {
            final int i = (int) (MathKit.div(MathKit.mul(r - 10.625, 23), (255 - 10.625), 0));
            l = i >= 0 ? 232 + i : 0;
        } else {
            l = 16 + (int) (36 * MathKit.div(MathKit.mul(r, 5), 255, 0)) + (int) (6.0 * (g / 256.0 * 6.0)) + (int) (b / 256.0 * 6.0);
        }
        return l;
    }

    /**
     * 创建解码选项
     *
     * @param isTryHarder   是否优化精度
     * @param isPureBarcode 是否使用复杂模式，扫描带logo的二维码设为true
     * @return 选项Map
     */
    private static Map<DecodeHintType, Object> buildHints(boolean isTryHarder, boolean isPureBarcode) {
        final HashMap<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, Charset.UTF_8);

        // 优化精度
        if (isTryHarder) {
            hints.put(DecodeHintType.TRY_HARDER, true);
        }
        // 复杂模式，开启PURE_BARCODE模式
        if (isPureBarcode) {
            hints.put(DecodeHintType.PURE_BARCODE, true);
        }
        return hints;
    }

    /**
     * 解码多种类型的码，包括二维码和条形码
     *
     * @param formatReader {@link MultiFormatReader}
     * @param binarizer    {@link Binarizer}
     * @return {@link Result}
     */
    private static Result _decode(MultiFormatReader formatReader, Binarizer binarizer) {
        try {
            return formatReader.decodeWithState(new BinaryBitmap(binarizer));
        } catch (NotFoundException e) {
            return null;
        }
    }

}
