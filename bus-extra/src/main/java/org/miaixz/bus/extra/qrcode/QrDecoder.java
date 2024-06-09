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
package org.miaixz.bus.extra.qrcode;

import com.google.zxing.*;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.extra.image.ImageKit;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码（条形码等）解码器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrDecoder implements Decoder<Image, String> {

    private final Map<DecodeHintType, Object> hints;

    /**
     * 构造
     *
     * @param hints 自定义扫码配置，包括算法、编码、复杂模式等
     */
    public QrDecoder(final Map<DecodeHintType, Object> hints) {
        this.hints = hints;
    }

    /**
     * 创建二维码（条形码等）解码器，用于将二维码（条形码等）解码为所代表的内容字符串
     *
     * @param isTryHarder   是否优化精度
     * @param isPureBarcode 是否使用复杂模式，扫描带logo的二维码设为true
     * @return QrDecoder
     */
    public static QrDecoder of(final boolean isTryHarder, final boolean isPureBarcode) {
        return of(buildHints(isTryHarder, isPureBarcode));
    }

    /**
     * 创建二维码（条形码等）解码器
     *
     * @param hints 自定义扫码配置，包括算法、编码、复杂模式等
     * @return QrDecoder
     */
    public static QrDecoder of(final Map<DecodeHintType, Object> hints) {
        return new QrDecoder(hints);
    }

    /**
     * 解码多种类型的码，包括二维码和条形码
     *
     * @param formatReader {@link MultiFormatReader}
     * @param binarizer    {@link Binarizer}
     * @return {@link Result}
     */
    private static Result _decode(final MultiFormatReader formatReader, final Binarizer binarizer) {
        try {
            return formatReader.decodeWithState(new BinaryBitmap(binarizer));
        } catch (final NotFoundException e) {
            return null;
        }
    }

    /**
     * 创建解码选项
     *
     * @param isTryHarder   是否优化精度
     * @param isPureBarcode 是否使用复杂模式，扫描带logo的二维码设为true
     * @return 选项Map
     */
    private static Map<DecodeHintType, Object> buildHints(final boolean isTryHarder, final boolean isPureBarcode) {
        final HashMap<DecodeHintType, Object> hints = new HashMap<>(3, 1);
        hints.put(DecodeHintType.CHARACTER_SET, Charset.DEFAULT_UTF_8);

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

    @Override
    public String decode(final Image image) {
        final MultiFormatReader formatReader = new MultiFormatReader();
        formatReader.setHints(hints);

        final LuminanceSource source = new BufferedImageLuminanceSource(
                ImageKit.castToBufferedImage(image, ImageKit.IMAGE_TYPE_JPG));

        Result result = _decode(formatReader, new HybridBinarizer(source));
        if (null == result) {
            result = _decode(formatReader, new GlobalHistogramBinarizer(source));
        }

        return null != result ? result.getText() : null;
    }
}
