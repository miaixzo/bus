/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.qrcode;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.extra.image.ImageKit;

import com.google.zxing.*;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

/**
 * QR code (barcode, etc.) decoder.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QrDecoder implements Decoder<Image, String> {

    /**
     * Custom scanning configuration, including algorithm, encoding, complex mode, etc.
     */
    private final Map<DecodeHintType, Object> hints;

    /**
     * Constructor.
     *
     * @param hints Custom scanning configuration, including algorithm, encoding, complex mode, etc.
     */
    public QrDecoder(final Map<DecodeHintType, Object> hints) {
        this.hints = hints;
    }

    /**
     * Create QR code (barcode, etc.) decoder to decode QR code (barcode, etc.) into the represented content string.
     *
     * @param isTryHarder   Whether to optimize accuracy
     * @param isPureBarcode Whether to use complex mode, set to true for scanning QR codes with logo
     * @return QrDecoder
     */
    public static QrDecoder of(final boolean isTryHarder, final boolean isPureBarcode) {
        return of(buildHints(isTryHarder, isPureBarcode));
    }

    /**
     * Create QR code (barcode, etc.) decoder.
     *
     * @param hints Custom scanning configuration, including algorithm, encoding, complex mode, etc.
     * @return QrDecoder
     */
    public static QrDecoder of(final Map<DecodeHintType, Object> hints) {
        return new QrDecoder(hints);
    }

    /**
     * Decode various types of codes, including QR codes and barcodes.
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
     * Create decoding options.
     *
     * @param isTryHarder   Whether to optimize accuracy
     * @param isPureBarcode Whether to use complex mode, set to true for scanning QR codes with logo
     * @return Options Map
     */
    private static Map<DecodeHintType, Object> buildHints(final boolean isTryHarder, final boolean isPureBarcode) {
        final HashMap<DecodeHintType, Object> hints = new HashMap<>(3, 1);
        hints.put(DecodeHintType.CHARACTER_SET, Charset.DEFAULT_UTF_8);

        // Optimize accuracy
        if (isTryHarder) {
            hints.put(DecodeHintType.TRY_HARDER, true);
        }
        // Complex mode, enable PURE_BARCODE mode
        if (isPureBarcode) {
            hints.put(DecodeHintType.PURE_BARCODE, true);
        }
        return hints;
    }

    /**
     * Implementation of the Decoder interface to decode image data. This method is designed to be overridden by
     * subclasses for custom decoding logic. When overriding, ensure thread-safety and proper handling of null results.
     *
     * @param image The image containing the code to decode
     * @return The decoded content string, or null if decoding fails
     */
    @Override
    public String decode(final Image image) {
        final MultiFormatReader formatReader = new MultiFormatReader();
        formatReader.setHints(hints);

        final com.google.zxing.LuminanceSource source = new LuminanceSource(
                ImageKit.castToBufferedImage(image, ImageKit.IMAGE_TYPE_JPG));

        Result result = _decode(formatReader, new HybridBinarizer(source));
        if (null == result) {
            result = _decode(formatReader, new GlobalHistogramBinarizer(source));
        }

        return null != result ? result.getText() : null;
    }

}
