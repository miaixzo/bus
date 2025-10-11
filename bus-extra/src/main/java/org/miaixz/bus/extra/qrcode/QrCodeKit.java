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
package org.miaixz.bus.extra.qrcode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.extra.image.ImageKit;
import org.miaixz.bus.extra.qrcode.render.AsciiArtRender;
import org.miaixz.bus.extra.qrcode.render.BitMatrixRender;
import org.miaixz.bus.extra.qrcode.render.ImageRender;
import org.miaixz.bus.extra.qrcode.render.SVGRender;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

/**
 * QR code utility class based on Zxing, supporting:
 * <ul>
 * <li>QR code generation and recognition, see {@link BarcodeFormat#QR_CODE}</li>
 * <li>Barcode generation and recognition, see {@link BarcodeFormat#CODE_39} and many other standard formats</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrCodeKit {

    /**
     * SVG vector graphic format.
     */
    public static final String QR_TYPE_SVG = "svg";
    /**
     * Ascii Art character text format.
     */
    public static final String QR_TYPE_TXT = "txt";

    /**
     * Generates a Base64 encoded QR code in String format.
     *
     * <p>
     * The output format is: data:image/[type];base64,[data]
     * </p>
     *
     * @param content   The content to encode in the QR code.
     * @param qrConfig  QR code configuration, including width, height, margin, color, etc.
     * @param imageType The image type (file extension), see {@link #QR_TYPE_SVG}, {@link #QR_TYPE_TXT},
     *                  {@link ImageKit}.
     * @return The Base64 encoded image string.
     */
    public static String generateAsBase64DataUri(
            final String content,
            final QrConfig qrConfig,
            final String imageType) {
        switch (imageType) {
            case QR_TYPE_SVG:
                return svgToBase64DataUri(generateAsSvg(content, qrConfig));

            case QR_TYPE_TXT:
                return txtToBase64DataUri(generateAsAsciiArt(content, qrConfig));

            default:
                BufferedImage img = null;
                try {
                    img = generate(content, qrConfig);
                    return ImageKit.toBase64DataUri(img, imageType);
                } finally {
                    ImageKit.flush(img);
                }
        }
    }

    /**
     * Generates a PNG format QR code image as a byte array.
     *
     * @param content The content to encode in the QR code.
     * @param width   The width of the QR code image.
     * @param height  The height of the QR code image.
     * @return The QR code image as a byte array.
     */
    public static byte[] generatePng(final String content, final int width, final int height) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        generate(content, width, height, ImageKit.IMAGE_TYPE_PNG, out);
        return out.toByteArray();
    }

    /**
     * Generates a PNG format QR code image as a byte array with custom configuration.
     *
     * @param content The content to encode in the QR code.
     * @param config  QR code configuration, including width, height, margin, color, etc.
     * @return The QR code image as a byte array.
     */
    public static byte[] generatePng(final String content, final QrConfig config) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        generate(content, config, ImageKit.IMAGE_TYPE_PNG, out);
        return out.toByteArray();
    }

    /**
     * Generates a QR code to a file. The image format depends on the file's extension.
     *
     * @param content    The text content to encode.
     * @param width      The width (in pixels for general images or SVG, in character units for Ascii Art).
     * @param height     The height (in pixels for general images or SVG, in character units for Ascii Art).
     * @param targetFile The target file, whose extension determines the output format.
     * @return The target file.
     */
    public static File generate(final String content, final int width, final int height, final File targetFile) {
        return generate(content, QrConfig.of(width, height), targetFile);
    }

    /**
     * Generates a QR code to a file with custom configuration. The image format depends on the file's extension.
     *
     * @param content    The text content to encode.
     * @param config     QR code configuration, including width, height, margin, color, etc.
     * @param targetFile The target file, whose extension determines the output format.
     * @return The target file.
     */
    public static File generate(final String content, final QrConfig config, final File targetFile) {
        final String extName = FileName.extName(targetFile);
        try (final BufferedOutputStream outputStream = FileKit.getOutputStream(targetFile)) {
            generate(content, config, extName, outputStream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return targetFile;
    }

    /**
     * Generates a QR code to an output stream.
     *
     * @param content   The text content to encode.
     * @param width     The width (in pixels for general images or SVG, in character units for Ascii Art).
     * @param height    The height (in pixels for general images or SVG, in character units for Ascii Art).
     * @param imageType The image type (file extension), see {@link #QR_TYPE_SVG}, {@link #QR_TYPE_TXT},
     *                  {@link ImageKit}.
     * @param out       The target output stream.
     */
    public static void generate(
            final String content,
            final int width,
            final int height,
            final String imageType,
            final OutputStream out) {
        generate(content, QrConfig.of(width, height), imageType, out);
    }

    /**
     * Generates a QR code to an output stream with custom configuration.
     *
     * @param content   The text content to encode.
     * @param config    QR code configuration, including width, height, margin, color, etc.
     * @param imageType The image type (file extension), see {@link ImageKit}.
     * @param out       The target output stream.
     */
    public static void generate(
            final String content,
            final QrConfig config,
            final String imageType,
            final OutputStream out) {
        final BitMatrixRender render;
        switch (imageType) {
            case QR_TYPE_SVG:
                render = new SVGRender(config);
                break;

            case QR_TYPE_TXT:
                render = new AsciiArtRender(config);
                break;

            default:
                render = new ImageRender(config, imageType);
        }
        render.render(encode(content, config), out);
    }

    /**
     * Generates a QR code image (black and white).
     *
     * @param content The text content to encode.
     * @param width   The width of the QR code image.
     * @param height  The height of the QR code image.
     * @return The generated QR code image.
     */
    public static BufferedImage generate(final String content, final int width, final int height) {
        return generate(content, QrConfig.of(width, height));
    }

    /**
     * Generates a QR code or barcode image with custom configuration. The image configuration in {@link QrConfig} is
     * only effective for QR codes.
     *
     * @param content The text content to encode.
     * @param config  QR code configuration, including width, height, margin, color, etc.
     * @return The generated QR code image (black and white).
     */
    public static BufferedImage generate(final String content, final QrConfig config) {
        return new ImageRender(ObjectKit.defaultIfNull(config, QrConfig::new), null).render(encode(content, config));
    }

    /**
     * Encodes text content into a barcode or QR code {@link BitMatrix}.
     *
     * @param content The text content to encode.
     * @param config  QR code configuration, including width, height, margin, color, format, etc.
     * @return The {@link BitMatrix} representing the encoded content.
     */
    public static BitMatrix encode(final CharSequence content, final QrConfig config) {
        return QrEncoder.of(config).encode(content);
    }

    /**
     * Decodes a QR code or barcode image from an input stream into text.
     *
     * @param qrCodeInputstream The input stream of the QR code image.
     * @return The decoded text.
     */
    public static String decode(final InputStream qrCodeInputstream) {
        BufferedImage image = null;
        try {
            image = ImageKit.read(qrCodeInputstream);
            return decode(image);
        } finally {
            ImageKit.flush(image);
        }
    }

    /**
     * Decodes a QR code or barcode image from a file into text.
     *
     * @param qrCodeFile The file containing the QR code image.
     * @return The decoded text.
     */
    public static String decode(final File qrCodeFile) {
        BufferedImage image = null;
        try {
            image = ImageKit.read(qrCodeFile);
            return decode(image);
        } finally {
            ImageKit.flush(image);
        }
    }

    /**
     * Decodes a QR code or barcode image into text.
     *
     * @param image The {@link Image} object of the QR code.
     * @return The decoded text.
     */
    public static String decode(final Image image) {
        return decode(image, true, false);
    }

    /**
     * Decodes a QR code or barcode image into text. This method attempts to parse using both {@link HybridBinarizer}
     * and {@link GlobalHistogramBinarizer} modes. Note that some QR codes without a logo may fail to parse in
     * PureBarcode mode, in which case this option should be set to {@code false}.
     *
     * @param image         The {@link Image} object of the QR code.
     * @param isTryHarder   Whether to optimize for accuracy (try harder).
     * @param isPureBarcode Whether to use pure barcode mode (for scanning QR codes with logos, set to {@code true}).
     * @return The decoded text.
     */
    public static String decode(final Image image, final boolean isTryHarder, final boolean isPureBarcode) {
        return QrDecoder.of(isTryHarder, isPureBarcode).decode(image);
    }

    /**
     * Decodes a QR code or barcode image into text with custom decoding hints. This method attempts to parse using both
     * {@link HybridBinarizer} and {@link GlobalHistogramBinarizer} modes.
     *
     * @param image The {@link Image} object of the QR code.
     * @param hints Custom scanning configuration, including algorithm, encoding, complex mode, etc.
     * @return The decoded text.
     */
    public static String decode(final Image image, final Map<DecodeHintType, Object> hints) {
        return QrDecoder.of(hints).decode(image);
    }

    /**
     * Converts a {@link BitMatrix} to a {@link BufferedImage}.
     *
     * @param matrix    The {@link BitMatrix} to convert.
     * @param foreColor The foreground color.
     * @param backColor The background color ({@code null} for transparent background).
     * @return The converted {@link BufferedImage}.
     */
    public static BufferedImage toImage(final BitMatrix matrix, final int foreColor, final Integer backColor) {
        final int width = matrix.getWidth();
        final int height = matrix.getHeight();
        final BufferedImage image = new BufferedImage(width, height,
                null == backColor ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
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
     * Generates an SVG vector graphic of a QR code.
     *
     * @param content  The content to encode in the QR code.
     * @param qrConfig QR code configuration, including width, height, margin, color, etc.
     * @return The SVG vector graphic as a string.
     */
    public static String generateAsSvg(final String content, final QrConfig qrConfig) {
        return toSVG(encode(content, qrConfig), qrConfig);
    }

    /**
     * Converts a {@link BitMatrix} to an SVG string.
     *
     * @param matrix The {@link BitMatrix} to convert.
     * @param config The {@link QrConfig} for SVG rendering.
     * @return The SVG vector graphic as a string.
     */
    public static String toSVG(final BitMatrix matrix, final QrConfig config) {
        final StringBuilder result = new StringBuilder();
        new SVGRender(config).render(matrix, result);
        return result.toString();
    }

    /**
     * Generates an ASCII Art character representation of a QR code.
     *
     * @param content  The content to encode in the QR code.
     * @param qrConfig QR code configuration, only width, height, and margin are effective.
     * @return The ASCII Art character representation of the QR code.
     */
    public static String generateAsAsciiArt(final String content, final QrConfig qrConfig) {
        return toAsciiArt(encode(content, qrConfig), qrConfig);
    }

    /**
     * Converts a {@link BitMatrix} to an ASCII Art character representation of a QR code.
     *
     * @param matrix The {@link BitMatrix} to convert.
     * @param config The {@link QrConfig} for ASCII Art rendering.
     * @return The ASCII Art character representation of the QR code.
     */
    public static String toAsciiArt(final BitMatrix matrix, final QrConfig config) {
        final StringBuilder result = new StringBuilder();
        new AsciiArtRender(config).render(matrix, result);
        return result.toString();
    }

    /**
     * Converts text to a Base64 encoded Data URI.
     *
     * @param txt The text to be converted to a Base64 encoded Data URI.
     * @return The converted Base64 encoded Data URI string.
     */
    private static String txtToBase64DataUri(final String txt) {
        return UrlKit.getDataUriBase64("text/plain", Base64.encode(txt));
    }

    /**
     * Converts an SVG string to a Base64 Data URI format.
     * <p>
     * This method encodes the SVG content into Base64 and wraps it in a Data URI, allowing direct embedding of SVG
     * images in HTML or CSS.
     * </p>
     *
     * @param svg The SVG image content as a string.
     * @return The converted Base64 Data URI string, which can be used to display SVG images directly in HTML or CSS.
     */
    private static String svgToBase64DataUri(final String svg) {
        return UrlKit.getDataUriBase64("image/svg+xml", Base64.encode(svg));
    }

}
