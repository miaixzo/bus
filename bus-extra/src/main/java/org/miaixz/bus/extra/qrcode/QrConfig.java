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
import com.google.zxing.EncodeHintType;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.ImageKit;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码设置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrConfig {


    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * 宽
     */
    public int width;
    /**
     * 长
     */
    public int height;
    /**
     * 前景色(二维码颜色)
     */
    public Integer foreColor = BLACK;
    /**
     * 背景色，默认白色，null表示透明
     */
    public Integer backColor = WHITE;
    /**
     * 边距1~4
     */
    public Integer margin = 2;
    /**
     * 纠错级别
     */
    public ErrorCorrectionLevel errorCorrection = ErrorCorrectionLevel.M;
    /**
     * 编码
     */
    public java.nio.charset.Charset charset = Charset.UTF_8;
    /**
     * 二维码中的Logo
     */
    public Image img;
    /**
     * 二维码中的Logo缩放的比例系数,如5表示长宽最小值的1/5
     */
    public int ratio = 6;
    /**
     * 设置二维码中的信息量，可设置1-40的整数
     */
    protected Integer qrVersion;

    /**
     * DATA_MATRIX的符号形状
     */
    protected SymbolShapeHint shapeHint = SymbolShapeHint.FORCE_NONE;

    /**
     * 构造,默认长宽为300
     */
    public QrConfig() {
        this(300, 300);
    }

    /**
     * 构造
     *
     * @param width  宽
     * @param height 长
     */
    public QrConfig(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 创建QrConfig
     *
     * @return QrConfig
     */
    public static QrConfig create() {
        return new QrConfig();
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * 设置宽度
     *
     * @param width 宽度
     * @return this
     */
    public QrConfig setWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * 获取高度
     *
     * @return 高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * 设置高度
     *
     * @param height 高度
     * @return this;
     */
    public QrConfig setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * 获取前景色
     *
     * @return 前景色
     */
    public int getForeColor() {
        return foreColor;
    }

    /**
     * 设置前景色，例如：Color.BLUE.getRGB()
     *
     * @param foreColor 前景色
     * @return this
     */
    public QrConfig setForeColor(Color foreColor) {
        if (null == foreColor) {
            this.foreColor = null;
        } else {
            this.foreColor = foreColor.getRGB();
        }
        return this;
    }

    /**
     * 获取背景色
     *
     * @return 背景色
     */
    public int getBackColor() {
        return backColor;
    }

    /**
     * 设置背景色，例如：Color.BLUE
     *
     * @param backColor 背景色,null表示透明背景
     * @return this
     */
    public QrConfig setBackColor(Color backColor) {
        if (null == backColor) {
            this.backColor = null;
        } else {
            this.backColor = backColor.getRGB();
        }
        return this;
    }

    /**
     * 获取边距
     *
     * @return 边距
     */
    public Integer getMargin() {
        return margin;
    }

    /**
     * 设置边距
     *
     * @param margin 边距
     * @return this
     */
    public QrConfig setMargin(Integer margin) {
        this.margin = margin;
        return this;
    }

    /**
     * 获取纠错级别
     *
     * @return 纠错级别
     */
    public ErrorCorrectionLevel getErrorCorrection() {
        return errorCorrection;
    }

    /**
     * 设置纠错级别
     *
     * @param errorCorrection 纠错级别
     * @return this
     */
    public QrConfig setErrorCorrection(ErrorCorrectionLevel errorCorrection) {
        this.errorCorrection = errorCorrection;
        return this;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * 设置编码
     *
     * @param charset 编码
     * @return this
     */
    public QrConfig setCharset(java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 获取二维码中的Logo
     *
     * @return Logo图片
     */
    public Image getImg() {
        return img;
    }

    /**
     * 设置二维码中的Logo文件
     *
     * @param imgPath 二维码中的Logo路径
     * @return this;
     */
    public QrConfig setImg(String imgPath) {
        return setImg(FileKit.file(imgPath));
    }

    /**
     * 设置二维码中的Logo文件
     *
     * @param imgFile 二维码中的Logo
     * @return this;
     */
    public QrConfig setImg(File imgFile) {
        return setImg(ImageKit.read(imgFile));
    }

    /**
     * 设置二维码中的Logo
     *
     * @param img 二维码中的Logo
     * @return this;
     */
    public QrConfig setImg(Image img) {
        this.img = img;
        return this;
    }

    /**
     * 获取二维码中的Logo缩放的比例系数,如5表示长宽最小值的1/5
     *
     * @return 二维码中的Logo缩放的比例系数, 如5表示长宽最小值的1/5
     */
    public int getRatio() {
        return this.ratio;
    }

    /**
     * 设置二维码中的Logo缩放的比例系数,如5表示长宽最小值的1/5
     *
     * @param ratio 二维码中的Logo缩放的比例系数,如5表示长宽最小值的1/5
     * @return this;
     */
    public QrConfig setRatio(int ratio) {
        this.ratio = ratio;
        return this;
    }

    /**
     * 设置DATA_MATRIX的符号形状
     *
     * @param shapeHint DATA_MATRIX的符号形状
     * @return this;
     */
    public QrConfig setShapeHint(SymbolShapeHint shapeHint) {
        this.shapeHint = shapeHint;
        return this;
    }

    /**
     * 设置二维码中的信息量，可设置0-40的整数，二维码图片也会根据qrVersion而变化，0表示根据传入信息自动变化
     *
     * @return 二维码中的信息量
     */
    public Integer getQrVersion() {
        return qrVersion;
    }

    /**
     * 设置二维码中的信息量，可设置0-40的整数，二维码图片也会根据qrVersion而变化，0表示根据传入信息自动变化
     *
     * @param qrVersion 二维码中的信息量
     * @return this
     */
    public QrConfig setQrVersion(Integer qrVersion) {
        this.qrVersion = qrVersion;
        return this;
    }

    /**
     * 转换为Zxing的二维码配置
     *
     * @return 配置
     */
    public Map<EncodeHintType, Object> toHints() {
        return toHints(BarcodeFormat.QR_CODE);
    }

    /**
     * 转换为Zxing的二维码配置
     *
     * @param format 格式，根据格式不同，{@link #errorCorrection}的值类型有所不同
     * @return 配置
     */
    public HashMap<EncodeHintType, Object> toHints(BarcodeFormat format) {
        // 配置
        final HashMap<EncodeHintType, Object> hints = new HashMap<>();
        if (null != this.charset) {
            hints.put(EncodeHintType.CHARACTER_SET, charset.toString().toLowerCase());
        }
        if (null != this.errorCorrection) {
            Object value;
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
