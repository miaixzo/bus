/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.io.File;
import java.util.HashMap;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.extra.image.ImageKit;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * QR code configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrConfig {

    /**
     * The color black.
     */
    private static final int BLACK = 0xFF000000;
    /**
     * The color white.
     */
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * Width.
     */
    protected int width;
    /**
     * Height.
     */
    protected int height;
    /**
     * Foreground color (QR code color).
     */
    protected Integer foreColor = BLACK;
    /**
     * Background color, defaults to white, null means transparent.
     */
    protected Integer backColor = WHITE;
    /**
     * Margin, 0-4.
     */
    protected Integer margin = 2;
    /**
     * Sets the amount of information in the QR code, can be set to an integer from 0-40.
     */
    protected Integer qrVersion;
    /**
     * Error correction level.
     */
    protected ErrorCorrectionLevel errorCorrection = ErrorCorrectionLevel.M;

    /**
     * Character encoding.
     */
    protected java.nio.charset.Charset charset = Charset.UTF_8;
    /**
     * Logo in the QR code.
     */
    protected Image img;

    /**
     * Corner radius of the QR code logo, 0-1, as a ratio of width/height.
     */
    protected double imgRound = 0.3;
    /**
     * Scaling ratio for the logo in the QR code, e.g., 5 means 1/5 of the smaller dimension (width or height).
     */
    protected int ratio = 6;
    /**
     * Symbol shape for DATA_MATRIX.
     */
    protected SymbolShapeHint shapeHint = SymbolShapeHint.FORCE_NONE;

    /**
     * Format of the generated code, defaults to QR code.
     */
    protected BarcodeFormat format = BarcodeFormat.QR_CODE;

    /**
     * Constructor, default width and height are 300.
     */
    public QrConfig() {
        this(300, 300);
    }

    /**
     * Constructor.
     *
     * @param width  Width.
     * @param height Height.
     */
    public QrConfig(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Creates a QrConfig.
     *
     * @return A new QrConfig instance.
     */
    public static QrConfig of() {
        return new QrConfig();
    }

    /**
     * Creates a QrConfig.
     *
     * @param width  Width.
     * @param height Height.
     * @return A new QrConfig instance.
     */
    public static QrConfig of(final int width, final int height) {
        return new QrConfig(width, height);
    }

    /**
     * Gets the width.
     *
     * @return The width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width.
     *
     * @param width The width.
     * @return this
     */
    public QrConfig setWidth(final int width) {
        this.width = width;
        return this;
    }

    /**
     * Gets the height.
     *
     * @return The height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height.
     *
     * @param height The height.
     * @return this
     */
    public QrConfig setHeight(final int height) {
        this.height = height;
        return this;
    }

    /**
     * Gets the foreground color.
     *
     * @return The foreground color.
     */
    public Integer getForeColor() {
        return foreColor;
    }

    /**
     * Sets the foreground color, e.g., Color.BLUE.getRGB().
     *
     * @param foreColor The foreground color.
     * @return this
     */
    public QrConfig setForeColor(final Color foreColor) {
        if (null == foreColor) {
            this.foreColor = null;
        } else {
            this.foreColor = foreColor.getRGB();
        }
        return this;
    }

    /**
     * Gets the background color.
     *
     * @return The background color.
     */
    public Integer getBackColor() {
        return backColor;
    }

    /**
     * Sets the background color, e.g., Color.BLUE. Null means transparent background.
     *
     * @param backColor The background color.
     * @return this
     */
    public QrConfig setBackColor(final Color backColor) {
        if (null == backColor) {
            this.backColor = null;
        } else {
            this.backColor = backColor.getRGB();
        }
        return this;
    }

    /**
     * Gets the margin.
     *
     * @return The margin.
     */
    public Integer getMargin() {
        return margin;
    }

    /**
     * Sets the margin.
     *
     * @param margin The margin.
     * @return this
     */
    public QrConfig setMargin(final Integer margin) {
        this.margin = margin;
        return this;
    }

    /**
     * Gets the amount of information in the QR code. Can be an integer from 0-40. The QR code image will also change
     * based on the qrVersion. 0 means it changes automatically based on the input information.
     *
     * @return The amount of information in the QR code.
     */
    public Integer getQrVersion() {
        return qrVersion;
    }

    /**
     * Sets the amount of information in the QR code. Can be an integer from 0-40. The QR code image will also change
     * based on the qrVersion. 0 means it changes automatically based on the input information.
     *
     * @param qrVersion The amount of information in the QR code.
     * @return this
     */
    public QrConfig setQrVersion(final Integer qrVersion) {
        this.qrVersion = qrVersion;
        return this;
    }

    /**
     * Gets the error correction level.
     *
     * @return The error correction level.
     */
    public ErrorCorrectionLevel getErrorCorrection() {
        return errorCorrection;
    }

    /**
     * Sets the error correction level.
     *
     * @param errorCorrection The error correction level.
     * @return this
     */
    public QrConfig setErrorCorrection(final ErrorCorrectionLevel errorCorrection) {
        this.errorCorrection = errorCorrection;
        return this;
    }

    /**
     * Enables or disables ECI encoding. If enableEci=false, the QR code will not contain ECI information, meaning the
     * {@link #charset} character encoding is set to {@code null}. This is best for English characters. If
     * enableEci=true, the QR code will contain ECI information, set according to the {@link #charset} encoding. This is
     * best for including Chinese characters, otherwise they will be garbled.
     *
     * <ul>
     * <li>Reference 1: <a href="https://github.com/nutzam/nutz-qrcode/issues/6">About the \000026 issue</a></li>
     * <li>Reference 2: <a href="https://en.wikipedia.org/wiki/Extended_Channel_Interpretation">ECI
     * (Extended_Channel_Interpretation) mode</a></li>
     * <li>Reference 3: <a href="https://www.51cto.com/article/414082.html">QR code generation details and
     * principles</a></li>
     * </ul>
     *
     * <p>
     * QR code encoding can be in ECI mode or non-ECI mode. In ECI mode, the first byte is used as an encoding
     * identifier, whereas in non-ECI mode, it is the data stream directly. ECI mode is actually a better solution, as
     * it allows for different encoding methods to be used during decoding based on the identifier. Non-ECI mode can
     * only be processed in a single, unified way. However, some devices do not support ECI mode, which can lead to
     * recognition failures. Using barcode scanners/guns might result in a "\000026" character. Using a mobile phone or
     * other QR code parsing software will not have this issue.
     * </p>
     *
     * <p>
     * The ECI encoding table shows that UTF-8 corresponds to "\000026" (or the number 22).
     * </p>
     *
     * <p>
     * In summary: If the QR code content is entirely characters without Chinese, there is no need to encode using
     * UTF-8. Encoding is only necessary for special symbols like Chinese.
     * </p>
     *
     * @param enableEci Whether to enable ECI.
     * @see EncodeHintType#PDF417_AUTO_ECI
     */
    public void setEnableEci(final boolean enableEci) {
        if (enableEci) {
            if (null == this.charset) {
                this.charset = Charset.UTF_8;
            }
        } else {
            this.charset = null;
        }
    }

    /**
     * Gets the character encoding.
     *
     * @return The character encoding.
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * Sets the character encoding.
     *
     * @param charset The character encoding.
     * @return this
     */
    public QrConfig setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Gets the logo in the QR code.
     *
     * @return The logo image.
     */
    public Image getImg() {
        return img;
    }

    /**
     * Sets the logo file for the QR code.
     *
     * @param imgPath The path to the logo in the QR code.
     * @return this
     */
    public QrConfig setImg(final String imgPath) {
        return setImg(FileKit.file(imgPath));
    }

    /**
     * Sets the logo file for the QR code from a byte array.
     *
     * @param imageBytes The byte array representation of the logo image.
     * @return this
     */
    public QrConfig setImg(final byte[] imageBytes) {
        return setImg(ImageKit.toImage(imageBytes));
    }

    /**
     * Sets the logo file for the QR code.
     *
     * @param imgFile The logo file for the QR code.
     * @return this
     */
    public QrConfig setImg(final File imgFile) {
        return setImg(ImageKit.read(imgFile));
    }

    /**
     * Sets the logo for the QR code.
     *
     * @param img The logo for the QR code.
     * @return this
     */
    public QrConfig setImg(final Image img) {
        this.img = img;
        return this;
    }

    /**
     * Gets the corner radius of the QR code logo, from 0 to 1, as a ratio of width/height.
     *
     * @return The corner radius of the QR code logo.
     */
    public double getImgRound() {
        return imgRound;
    }

    /**
     * Sets the corner radius of the QR code logo, from 0 to 1, as a ratio of width/height.
     *
     * @param imgRound The corner radius of the QR code logo.
     * @return this
     */
    public QrConfig setImgRound(final double imgRound) {
        this.imgRound = imgRound;
        return this;
    }

    /**
     * Gets the scaling ratio of the logo in the QR code, e.g., 5 means 1/5 of the smaller dimension (width or height).
     *
     * @return The scaling ratio of the logo in the QR code.
     */
    public int getRatio() {
        return this.ratio;
    }

    /**
     * Sets the scaling ratio of the logo in the QR code, e.g., 5 means 1/5 of the smaller dimension (width or height).
     *
     * @param ratio The scaling ratio of the logo in the QR code.
     * @return this
     */
    public QrConfig setRatio(final int ratio) {
        this.ratio = ratio;
        return this;
    }

    /**
     * Sets the symbol shape for DATA_MATRIX.
     *
     * @param shapeHint The symbol shape for DATA_MATRIX.
     * @return this
     */
    public QrConfig setShapeHint(final SymbolShapeHint shapeHint) {
        this.shapeHint = shapeHint;
        return this;
    }

    /**
     * Gets the code format.
     *
     * @return The code format, defaults to QR code.
     */
    public BarcodeFormat getFormat() {
        return format;
    }

    /**
     * Sets the code format, defaults to QR code.
     *
     * @param format The code format.
     * @return this
     */
    public QrConfig setFormat(final BarcodeFormat format) {
        this.format = format;
        return this;
    }

    /**
     * Converts to Zxing's QR code configuration.
     *
     * @return The configuration.
     */
    public HashMap<EncodeHintType, Object> toHints() {
        // Configuration
        final HashMap<EncodeHintType, Object> hints = new HashMap<>();
        // Use custom character encoding only if ECI encoding is not disabled (i.e., enabled)
        // If the QR code content is just English characters, it's recommended not to set the encoding; it works fine.
        // For Chinese, it will be garbled.
        if (null != this.charset) {
            hints.put(EncodeHintType.CHARACTER_SET, charset.toString().toLowerCase());
        }
        if (null != this.errorCorrection) {
            final Object value;
            if (BarcodeFormat.AZTEC == format || BarcodeFormat.PDF_417 == format) {
                value = this.errorCorrection.getBits();
            } else {
                value = this.errorCorrection;
            }

            hints.put(EncodeHintType.ERROR_CORRECTION, value);
            hints.put(EncodeHintType.DATA_MATRIX_SHAPE, shapeHint);
        }
        if (null != this.margin) {
            hints.put(EncodeHintType.MARGIN, this.margin);
        }
        if (null != this.qrVersion) {
            hints.put(EncodeHintType.QR_VERSION, this.qrVersion);
        }
        return hints;
    }

}
