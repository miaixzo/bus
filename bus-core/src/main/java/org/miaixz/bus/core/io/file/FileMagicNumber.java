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
package org.miaixz.bus.core.io.file;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Encapsulation of file type magic numbers.
 * <p>
 * This enum class identifies file types based on file magic numbers (header byte sequences). Each enum constant
 * represents a file type and implements matching logic.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum FileMagicNumber {

    /**
     * Unknown type.
     */
    UNKNOWN(null, null) {

        /**
         * Always returns false for unknown type.
         *
         * @param bytes the file bytes to check
         * @return {@code false}
         */
        @Override
        public boolean match(final byte[] bytes) {
            return false;
        }
    },
    /**
     * JPEG image format.
     *
     * <pre>
     *     prefix：FFD8FF
     * </pre>
     */
    JPEG("image/jpeg", "jpg") {

        /**
         * Checks if the given bytes match the JPEG file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match JPEG signature (FFD8FF)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 2 && Objects.equals(bytes[0], (byte) 0xff) && Objects.equals(bytes[1], (byte) 0xd8)
                    && Objects.equals(bytes[2], (byte) 0xff);
        }
    },
    /**
     * JXR image format.
     *
     * <pre>
     *     prefix：4949BC
     * </pre>
     */
    JXR("image/vnd.ms-photo", "jxr") {

        /**
         * Checks if the given bytes match the JXR file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match JXR signature (4949BC)
         */
        @Override
        public boolean match(final byte[] bytes) {
            // file magic number https://www.iana.org/assignments/media-types/image/jxr
            return bytes.length > 2 && Objects.equals(bytes[0], (byte) 0x49) && Objects.equals(bytes[1], (byte) 0x49)
                    && Objects.equals(bytes[2], (byte) 0xbc);
        }
    },
    /**
     * APNG image format.
     *
     * <pre>
     *     prefix 8 bits：89504E47 0D0A1A0A
     * </pre>
     */
    APNG("image/apng", "apng") {

        /**
         * Checks if the given bytes match the APNG file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match APNG signature (89504E470D0A1A0A)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final boolean b = ArrayKit.startWith(
                    bytes,
                    (byte) 0x89,
                    (byte) 0x50,
                    (byte) 0x4e,
                    (byte) 0x47,
                    (byte) 0x0d,
                    (byte) 0x0a,
                    (byte) 0x1a,
                    (byte) 0x0a);

            if (b) {
                int i = 8;
                while (i < bytes.length) {
                    try {
                        final int dataLength = new BigInteger(1, Arrays.copyOfRange(bytes, i, i + 4)).intValue();
                        i += 4;
                        final String chunkType = new String(bytes, i, 4, Charset.ISO_8859_1);
                        i += 4;
                        if (Objects.equals(chunkType, "IDAT") || Objects.equals(chunkType, "IEND")) {
                            return false;
                        } else if (Objects.equals(chunkType, "acTL")) {
                            return true;
                        }
                        i += dataLength + 4;
                    } catch (final Exception e) {
                        return false;
                    }
                }
            }
            return false;
        }
    },
    /**
     * PNG image format.
     *
     * <pre>
     *     prefix: 89504E47
     * </pre>
     */
    PNG("image/png", "png") {

        /**
         * Checks if the given bytes match the PNG file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match PNG signature (89504E47)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47);
        }
    },
    /**
     * GIF image format.
     *
     * <pre>
     *     prefix: 474946
     * </pre>
     */
    GIF("image/gif", "gif") {

        /**
         * Checks if the given bytes match the GIF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match GIF signature (474946)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x47, (byte) 0x49, (byte) 0x46);
        }
    },
    /**
     * BMP image format.
     *
     * <pre>
     *     prefix: 424D
     * </pre>
     */
    BMP("image/bmp", "bmp") {

        /**
         * Checks if the given bytes match the BMP file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match BMP signature (424D)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x42, (byte) 0x4d);
        }
    },
    /**
     * TIFF image format.
     *
     * <pre>
     *     prefix: 49492A00 or 4D4D002A
     * </pre>
     */
    TIFF("image/tiff", "tiff") {

        /**
         * Checks if the given bytes match the TIFF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match TIFF signature (49492A00 or 4D4D002A)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x49, (byte) 0x49, (byte) 0x2a, (byte) 0x00)
                    || ArrayKit.startWith(bytes, (byte) 0x4d, (byte) 0x4d, (byte) 0x00, (byte) 0x2a);
        }
    },
    /**
     * DWG image format.
     *
     * <pre>
     *     prefix: 41433130
     * </pre>
     */
    DWG("image/vnd.dwg", "dwg") {

        /**
         * Checks if the given bytes match the DWG file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match DWG signature (41433130)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x41, (byte) 0x43, (byte) 0x31, (byte) 0x30);
        }
    },
    /**
     * WEBP image format.
     *
     * <pre>
     *     [8:11]: 57454250
     * </pre>
     */
    WEBP("image/webp", "webp") {

        /**
         * Checks if the given bytes match the WEBP file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match WEBP signature (57454250 at offset 8)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.isSubEquals(bytes, 8, (byte) 0x57, (byte) 0x45, (byte) 0x42, (byte) 0x50);
        }
    },
    /**
     * PSD image format.
     *
     * <pre>
     *     prefix: 38425053
     * </pre>
     */
    PSD("image/vnd.adobe.photoshop", "psd") {

        /**
         * Checks if the given bytes match the PSD file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match PSD signature (38425053)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x38, (byte) 0x42, (byte) 0x50, (byte) 0x53);
        }
    },
    /**
     * ICO image format.
     *
     * <pre>
     *     prefix: 00000100
     * </pre>
     */
    ICO("image/x-icon", "ico") {

        /**
         * Checks if the given bytes match the ICO file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match ICO signature (00000100)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00);
        }
    },
    /**
     * XCF image format.
     *
     * <pre>
     *     prefix: 67696D70 20786366 2076
     * </pre>
     */
    XCF("image/x-xcf", "xcf") {

        /**
         * Checks if the given bytes match the XCF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match XCF signature (67696D70207863662076)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(
                    bytes,
                    (byte) 0x67,
                    (byte) 0x69,
                    (byte) 0x6d,
                    (byte) 0x70,
                    (byte) 0x20,
                    (byte) 0x78,
                    (byte) 0x63,
                    (byte) 0x66,
                    (byte) 0x20,
                    (byte) 0x76);
        }
    },

    /**
     * WAV audio format.
     *
     * <pre>
     *     prefix: 52494646
     *     [8:11]: 57415645
     * </pre>
     */
    WAV("audio/x-wav", "wav") {

        /**
         * Checks if the given bytes match the WAV file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match WAV signature (52494646 and 57415645 at offset 8)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46)
                    && ArrayKit.isSubEquals(bytes, 8, (byte) 0x57, (byte) 0x41, (byte) 0x56, (byte) 0x45);
        }
    },
    /**
     * MIDI audio format.
     *
     * <pre>
     *     prefix: 4D546864
     * </pre>
     */
    MIDI("audio/midi", "midi") {

        /**
         * Checks if the given bytes match the MIDI file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match MIDI signature (4D546864)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x4d, (byte) 0x54, (byte) 0x68, (byte) 0x64);
        }
    },
    /**
     * MP3 audio format.
     *
     * <pre>
     *     prefix: 494433 or FFFB or FFF3 or FFF2
     * </pre>
     */
    MP3("audio/mpeg", "mp3") {

        /**
         * Checks if the given bytes match the MP3 file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match MP3 signature (494433 or FFFB or FFF3 or FFF2)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x49, (byte) 0x44, (byte) 0x33)
                    || ArrayKit.startWith(bytes, (byte) 0xFF, (byte) 0xFB)
                    || ArrayKit.startWith(bytes, (byte) 0xFF, (byte) 0xF3)
                    || ArrayKit.startWith(bytes, (byte) 0xFF, (byte) 0xF2);
        }
    },
    /**
     * OGG audio format.
     *
     * <pre>
     *     prefix: 4F676753
     * </pre>
     */
    OGG("audio/ogg", "ogg") {

        /**
         * Checks if the given bytes match the OGG file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match OGG signature (4F676753)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x4F, (byte) 0x67, (byte) 0x67, (byte) 0x53);
        }
    },
    /**
     * FLAC audio format.
     *
     * <pre>
     *    prefix: 664C6143
     * </pre>
     */
    FLAC("audio/x-flac", "flac") {

        /**
         * Checks if the given bytes match the FLAC file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match FLAC signature (664C6143)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x66, (byte) 0x4c, (byte) 0x61, (byte) 0x43);
        }
    },
    /**
     * M4A audio format.
     */
    M4A("audio/mp4", "m4a") {

        /**
         * Checks if the given bytes match the M4A file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match M4A signature (667479704D3441)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return (bytes.length > 10 && Objects.equals(bytes[4], (byte) 0x66) && Objects.equals(bytes[5], (byte) 0x74)
                    && Objects.equals(bytes[6], (byte) 0x79) && Objects.equals(bytes[7], (byte) 0x70)
                    && Objects.equals(bytes[8], (byte) 0x4d) && Objects.equals(bytes[9], (byte) 0x34)
                    && Objects.equals(bytes[10], (byte) 0x41))
                    || (Objects.equals(bytes[0], (byte) 0x4d) && Objects.equals(bytes[1], (byte) 0x34)
                            && Objects.equals(bytes[2], (byte) 0x41) && Objects.equals(bytes[3], (byte) 0x20));
        }
    },
    /**
     * AAC audio format.
     */
    AAC("audio/aac", "aac") {

        /**
         * Checks if the given bytes match the AAC file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match AAC signature (FFF1 or FFF9)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 1) {
                return false;
            }
            final boolean flag1 = Objects.equals(bytes[0], (byte) 0xFF) && Objects.equals(bytes[1], (byte) 0xF1);
            final boolean flag2 = Objects.equals(bytes[0], (byte) 0xFF) && Objects.equals(bytes[1], (byte) 0xF9);
            return flag1 || flag2;
        }
    },
    /**
     * AMR audio format.
     */
    AMR("audio/amr", "amr") {

        /**
         * Checks if the given bytes match the AMR file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match AMR signature (23414D520A or 23414D5F4D43312E300A)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit
                    .startWith(bytes, (byte) 0x23, (byte) 0x21, (byte) 0x41, (byte) 0x4d, (byte) 0x52, (byte) 0x0A)
                    || ArrayKit.startWith(
                            bytes,
                            (byte) 0x23,
                            (byte) 0x21,
                            (byte) 0x41,
                            (byte) 0x4d,
                            (byte) 0x52,
                            (byte) 0x5F,
                            (byte) 0x4d,
                            (byte) 0x43,
                            (byte) 0x31,
                            (byte) 0x2e,
                            (byte) 0x30,
                            (byte) 0x0a);
        }
    },
    /**
     * AC3 audio format.
     */
    AC3("audio/ac3", "ac3") {

        /**
         * Checks if the given bytes match the AC3 file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match AC3 signature (0B77)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(bytes, (byte) 0x0b, (byte) 0x77);
        }
    },
    /**
     * AIFF audio format.
     */
    AIFF("audio/x-aiff", "aiff") {

        /**
         * Checks if the given bytes match the AIFF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match AIFF signature (464F524D and 41494646)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 11 && Objects.equals(bytes[0], (byte) 0x46) && Objects.equals(bytes[1], (byte) 0x4f)
                    && Objects.equals(bytes[2], (byte) 0x52) && Objects.equals(bytes[3], (byte) 0x4d)

                    && Objects.equals(bytes[8], (byte) 0x41) && Objects.equals(bytes[9], (byte) 0x49)
                    && Objects.equals(bytes[10], (byte) 0x46) && Objects.equals(bytes[11], (byte) 0x46);
        }
    },

    /**
     * WOFF font format. The existing registration "application/font-woff" is deprecated in favor of "font/woff".
     */
    WOFF("font/woff", "woff") {

        /**
         * Checks if the given bytes match the WOFF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match WOFF signature (774F4646)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 8) {
                return false;
            }
            final boolean flag1 = Objects.equals(bytes[0], (byte) 0x77) && Objects.equals(bytes[1], (byte) 0x4f)
                    && Objects.equals(bytes[2], (byte) 0x46) && Objects.equals(bytes[3], (byte) 0x46);
            final boolean flag2 = Objects.equals(bytes[4], (byte) 0x00) && Objects.equals(bytes[5], (byte) 0x01)
                    && Objects.equals(bytes[6], (byte) 0x00) && Objects.equals(bytes[7], (byte) 0x00);
            final boolean flag3 = Objects.equals(bytes[4], (byte) 0x4f) && Objects.equals(bytes[5], (byte) 0x54)
                    && Objects.equals(bytes[6], (byte) 0x54) && Objects.equals(bytes[7], (byte) 0x4f);
            final boolean flag4 = Objects.equals(bytes[4], (byte) 0x74) && Objects.equals(bytes[5], (byte) 0x72)
                    && Objects.equals(bytes[6], (byte) 0x75) && Objects.equals(bytes[7], (byte) 0x65);
            return flag1 && (flag2 || flag3 || flag4);
        }
    },
    /**
     * WOFF2 font format.
     */
    WOFF2("font/woff2", "woff2") {

        /**
         * Checks if the given bytes match the WOFF2 file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match WOFF2 signature (774F4632)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 8) {
                return false;
            }
            final boolean flag1 = Objects.equals(bytes[0], (byte) 0x77) && Objects.equals(bytes[1], (byte) 0x4f)
                    && Objects.equals(bytes[2], (byte) 0x46) && Objects.equals(bytes[3], (byte) 0x32);
            final boolean flag2 = Objects.equals(bytes[4], (byte) 0x00) && Objects.equals(bytes[5], (byte) 0x01)
                    && Objects.equals(bytes[6], (byte) 0x00) && Objects.equals(bytes[7], (byte) 0x00);
            final boolean flag3 = Objects.equals(bytes[4], (byte) 0x4f) && Objects.equals(bytes[5], (byte) 0x54)
                    && Objects.equals(bytes[6], (byte) 0x54) && Objects.equals(bytes[7], (byte) 0x4f);
            final boolean flag4 = Objects.equals(bytes[4], (byte) 0x74) && Objects.equals(bytes[5], (byte) 0x72)
                    && Objects.equals(bytes[6], (byte) 0x75) && Objects.equals(bytes[7], (byte) 0x65);
            return flag1 && (flag2 || flag3 || flag4);
        }
    },
    /**
     * TTF font format.
     */
    TTF("font/ttf", "ttf") {

        /**
         * Checks if the given bytes match the TTF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match TTF signature (00010000000)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 4 && Objects.equals(bytes[0], (byte) 0x00) && Objects.equals(bytes[1], (byte) 0x01)
                    && Objects.equals(bytes[2], (byte) 0x00) && Objects.equals(bytes[3], (byte) 0x00)
                    && Objects.equals(bytes[4], (byte) 0x00);
        }
    },
    /**
     * OTF font format.
     */
    OTF("font/otf", "otf") {

        /**
         * Checks if the given bytes match the OTF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match OTF signature (4F54544F00)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 4 && Objects.equals(bytes[0], (byte) 0x4f) && Objects.equals(bytes[1], (byte) 0x54)
                    && Objects.equals(bytes[2], (byte) 0x54) && Objects.equals(bytes[3], (byte) 0x4f)
                    && Objects.equals(bytes[4], (byte) 0x00);
        }
    },

    /**
     * EPUB format.
     */
    EPUB("application/epub+zip", "epub") {

        /**
         * Checks if the given bytes match the EPUB file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match EPUB signature
         *         (504B0304...6D696565747970656170706C69636174696F6E2F657075622B7A6970)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 58 && Objects.equals(bytes[0], (byte) 0x50) && Objects.equals(bytes[1], (byte) 0x4b)
                    && Objects.equals(bytes[2], (byte) 0x03) && Objects.equals(bytes[3], (byte) 0x04)
                    && Objects.equals(bytes[30], (byte) 0x6d) && Objects.equals(bytes[31], (byte) 0x69)
                    && Objects.equals(bytes[32], (byte) 0x6d) && Objects.equals(bytes[33], (byte) 0x65)
                    && Objects.equals(bytes[34], (byte) 0x74) && Objects.equals(bytes[35], (byte) 0x79)
                    && Objects.equals(bytes[36], (byte) 0x70) && Objects.equals(bytes[37], (byte) 0x65)
                    && Objects.equals(bytes[38], (byte) 0x61) && Objects.equals(bytes[39], (byte) 0x70)
                    && Objects.equals(bytes[40], (byte) 0x70) && Objects.equals(bytes[41], (byte) 0x6c)
                    && Objects.equals(bytes[42], (byte) 0x69) && Objects.equals(bytes[43], (byte) 0x63)
                    && Objects.equals(bytes[44], (byte) 0x61) && Objects.equals(bytes[45], (byte) 0x74)
                    && Objects.equals(bytes[46], (byte) 0x69) && Objects.equals(bytes[47], (byte) 0x6f)
                    && Objects.equals(bytes[48], (byte) 0x6e) && Objects.equals(bytes[49], (byte) 0x2f)
                    && Objects.equals(bytes[50], (byte) 0x65) && Objects.equals(bytes[51], (byte) 0x70)
                    && Objects.equals(bytes[52], (byte) 0x75) && Objects.equals(bytes[53], (byte) 0x62)
                    && Objects.equals(bytes[54], (byte) 0x2b) && Objects.equals(bytes[55], (byte) 0x7a)
                    && Objects.equals(bytes[56], (byte) 0x69) && Objects.equals(bytes[57], (byte) 0x70);
        }
    },
    /**
     * ZIP archive format.
     */
    ZIP("application/zip", "zip") {

        /**
         * Checks if the given bytes match the ZIP file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match ZIP signature (504B0304, 504B0506, or 504B0708)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 4) {
                return false;
            }
            final boolean flag1 = Objects.equals(bytes[0], (byte) 0x50) && Objects.equals(bytes[1], (byte) 0x4b);
            final boolean flag2 = Objects.equals(bytes[2], (byte) 0x03) || Objects.equals(bytes[2], (byte) 0x05)
                    || Objects.equals(bytes[2], (byte) 0x07);
            final boolean flag3 = Objects.equals(bytes[3], (byte) 0x04) || Objects.equals(bytes[3], (byte) 0x06)
                    || Objects.equals(bytes[3], (byte) 0x08);
            return flag1 && flag2 && flag3;
        }
    },
    /**
     * TAR archive format.
     */
    TAR("application/x-tar", "tar") {

        /**
         * Checks if the given bytes match the TAR file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match TAR signature (7573746172 at offset 257)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 261 && Objects.equals(bytes[257], (byte) 0x75)
                    && Objects.equals(bytes[258], (byte) 0x73) && Objects.equals(bytes[259], (byte) 0x74)
                    && Objects.equals(bytes[260], (byte) 0x61) && Objects.equals(bytes[261], (byte) 0x72);
        }
    },
    /**
     * RAR archive format.
     */
    RAR("application/x-rar-compressed", "rar") {

        /**
         * Checks if the given bytes match the RAR file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match RAR signature (526172211A0700 or 526172211A0701)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 6 && Objects.equals(bytes[0], (byte) 0x52) && Objects.equals(bytes[1], (byte) 0x61)
                    && Objects.equals(bytes[2], (byte) 0x72) && Objects.equals(bytes[3], (byte) 0x21)
                    && Objects.equals(bytes[4], (byte) 0x1a) && Objects.equals(bytes[5], (byte) 0x07)
                    && (Objects.equals(bytes[6], (byte) 0x00) || Objects.equals(bytes[6], (byte) 0x01));
        }
    },
    /**
     * GZ archive format.
     */
    GZ("application/gzip", "gz") {

        /**
         * Checks if the given bytes match the GZ file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match GZ signature (1F8B08)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 2 && Objects.equals(bytes[0], (byte) 0x1f) && Objects.equals(bytes[1], (byte) 0x8b)
                    && Objects.equals(bytes[2], (byte) 0x08);
        }
    },
    /**
     * BZ2 archive format.
     */
    BZ2("application/x-bzip2", "bz2") {

        /**
         * Checks if the given bytes match the BZ2 file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match BZ2 signature (425A68)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 2 && Objects.equals(bytes[0], (byte) 0x42) && Objects.equals(bytes[1], (byte) 0x5a)
                    && Objects.equals(bytes[2], (byte) 0x68);
        }
    },
    /**
     * 7Z archive format.
     */
    SevenZ("application/x-7z-compressed", "7z") {

        /**
         * Checks if the given bytes match the 7Z file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match 7Z signature (377ABCAF271C00)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 6 && Objects.equals(bytes[0], (byte) 0x37) && Objects.equals(bytes[1], (byte) 0x7a)
                    && Objects.equals(bytes[2], (byte) 0xbc) && Objects.equals(bytes[3], (byte) 0xaf)
                    && Objects.equals(bytes[4], (byte) 0x27) && Objects.equals(bytes[5], (byte) 0x1c)
                    && Objects.equals(bytes[6], (byte) 0x00);
        }
    },
    /**
     * PDF format.
     */
    PDF("application/pdf", "pdf") {

        /**
         * Checks if the given bytes match the PDF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match PDF signature (25504446)
         */
        @Override
        public boolean match(byte[] bytes) {
            // Remove BOM header if present and skip three bytes
            if (bytes.length > 3 && Objects.equals(bytes[0], (byte) 0xEF) && Objects.equals(bytes[1], (byte) 0xBB)
                    && Objects.equals(bytes[2], (byte) 0xBF)) {
                // UTF8 BOM
                bytes = Arrays.copyOfRange(bytes, 3, bytes.length);
            }
            return bytes.length > 3 && Objects.equals(bytes[0], (byte) 0x25) && Objects.equals(bytes[1], (byte) 0x50)
                    && Objects.equals(bytes[2], (byte) 0x44) && Objects.equals(bytes[3], (byte) 0x46);
        }
    },
    /**
     * EXE executable format.
     */
    EXE("application/x-msdownload", "exe") {

        /**
         * Checks if the given bytes match the EXE file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match EXE signature (4D5A)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 1 && Objects.equals(bytes[0], (byte) 0x4d) && Objects.equals(bytes[1], (byte) 0x5a);
        }
    },
    /**
     * SWF format.
     */
    SWF("application/x-shockwave-flash", "swf") {

        /**
         * Checks if the given bytes match the SWF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match SWF signature (435753 or 465753)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 2 && (Objects.equals(bytes[0], 0x43) || Objects.equals(bytes[0], (byte) 0x46))
                    && Objects.equals(bytes[1], (byte) 0x57) && Objects.equals(bytes[2], (byte) 0x53);
        }
    },
    /**
     * RTF format.
     */
    RTF("application/rtf", "rtf") {

        /**
         * Checks if the given bytes match the RTF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match RTF signature (7B5C727466)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 4 && Objects.equals(bytes[0], (byte) 0x7b) && Objects.equals(bytes[1], (byte) 0x5c)
                    && Objects.equals(bytes[2], (byte) 0x72) && Objects.equals(bytes[3], (byte) 0x74)
                    && Objects.equals(bytes[4], (byte) 0x66);
        }
    },
    /**
     * NES ROM format.
     */
    NES("application/x-nintendo-nes-rom", "nes") {

        /**
         * Checks if the given bytes match the NES file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match NES signature (4E45531A)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 3 && Objects.equals(bytes[0], (byte) 0x4e) && Objects.equals(bytes[1], (byte) 0x45)
                    && Objects.equals(bytes[2], (byte) 0x53) && Objects.equals(bytes[3], (byte) 0x1a);
        }
    },
    /**
     * CRX format.
     */
    CRX("application/x-google-chrome-extension", "crx") {

        /**
         * Checks if the given bytes match the CRX file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match CRX signature (43723234)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 3 && Objects.equals(bytes[0], (byte) 0x43) && Objects.equals(bytes[1], (byte) 0x72)
                    && Objects.equals(bytes[2], (byte) 0x32) && Objects.equals(bytes[3], (byte) 0x34);
        }
    },
    /**
     * CAB format.
     */
    CAB("application/vnd.ms-cab-compressed", "cab") {

        /**
         * Checks if the given bytes match the CAB file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match CAB signature (4D534346 or 49536328)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 4) {
                return false;
            }
            final boolean flag1 = Objects.equals(bytes[0], (byte) 0x4d) && Objects.equals(bytes[1], (byte) 0x53)
                    && Objects.equals(bytes[2], (byte) 0x43) && Objects.equals(bytes[3], (byte) 0x46);
            final boolean flag2 = Objects.equals(bytes[0], (byte) 0x49) && Objects.equals(bytes[1], (byte) 0x53)
                    && Objects.equals(bytes[2], (byte) 0x63) && Objects.equals(bytes[3], (byte) 0x28);
            return flag1 || flag2;
        }
    },
    /**
     * PS format.
     */
    PS("application/postscript", "ps") {

        /**
         * Checks if the given bytes match the PS file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match PS signature (2521)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 1 && Objects.equals(bytes[0], (byte) 0x25) && Objects.equals(bytes[1], (byte) 0x21);
        }
    },
    /**
     * XZ archive format.
     */
    XZ("application/x-xz", "xz") {

        /**
         * Checks if the given bytes match the XZ file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match XZ signature (FD377A585A00)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 5 && Objects.equals(bytes[0], (byte) 0xFD) && Objects.equals(bytes[1], (byte) 0x37)
                    && Objects.equals(bytes[2], (byte) 0x7a) && Objects.equals(bytes[3], (byte) 0x58)
                    && Objects.equals(bytes[4], (byte) 0x5A) && Objects.equals(bytes[5], (byte) 0x00);
        }
    },
    /**
     * SQLITE format.
     */
    SQLITE("application/x-sqlite3", "sqlite") {

        /**
         * Checks if the given bytes match the SQLITE file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match SQLITE signature (514C69746520666F726D6174203300)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 15 && Objects.equals(bytes[0], (byte) 0x53) && Objects.equals(bytes[1], (byte) 0x51)
                    && Objects.equals(bytes[2], (byte) 0x4c) && Objects.equals(bytes[3], (byte) 0x69)
                    && Objects.equals(bytes[4], (byte) 0x74) && Objects.equals(bytes[5], (byte) 0x65)
                    && Objects.equals(bytes[6], (byte) 0x20) && Objects.equals(bytes[7], (byte) 0x66)
                    && Objects.equals(bytes[8], (byte) 0x6f) && Objects.equals(bytes[9], (byte) 0x72)
                    && Objects.equals(bytes[10], (byte) 0x6d) && Objects.equals(bytes[11], (byte) 0x61)
                    && Objects.equals(bytes[12], (byte) 0x74) && Objects.equals(bytes[13], (byte) 0x20)
                    && Objects.equals(bytes[14], (byte) 0x33) && Objects.equals(bytes[15], (byte) 0x00);
        }
    },
    /**
     * DEB format.
     */
    DEB("application/x-deb", "deb") {

        /**
         * Checks if the given bytes match the DEB file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match DEB signature (213C617263683E0A...64656269616E2D62696E617279)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 20 && Objects.equals(bytes[0], (byte) 0x21) && Objects.equals(bytes[1], (byte) 0x3c)
                    && Objects.equals(bytes[2], (byte) 0x61) && Objects.equals(bytes[3], (byte) 0x72)
                    && Objects.equals(bytes[4], (byte) 0x63) && Objects.equals(bytes[5], (byte) 0x68)
                    && Objects.equals(bytes[6], (byte) 0x3e) && Objects.equals(bytes[7], (byte) 0x0a)
                    && Objects.equals(bytes[8], (byte) 0x64) && Objects.equals(bytes[9], (byte) 0x65)
                    && Objects.equals(bytes[10], (byte) 0x62) && Objects.equals(bytes[11], (byte) 0x69)
                    && Objects.equals(bytes[12], (byte) 0x61) && Objects.equals(bytes[13], (byte) 0x6e)
                    && Objects.equals(bytes[14], (byte) 0x2d) && Objects.equals(bytes[15], (byte) 0x62)
                    && Objects.equals(bytes[16], (byte) 0x69) && Objects.equals(bytes[17], (byte) 0x6e)
                    && Objects.equals(bytes[18], (byte) 0x61) && Objects.equals(bytes[19], (byte) 0x72)
                    && Objects.equals(bytes[20], (byte) 0x79);
        }
    },
    /**
     * AR archive format.
     */
    AR("application/x-unix-archive", "ar") {

        /**
         * Checks if the given bytes match the AR file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match AR signature (213C617263683E)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 6 && Objects.equals(bytes[0], (byte) 0x21) && Objects.equals(bytes[1], (byte) 0x3c)
                    && Objects.equals(bytes[2], (byte) 0x61) && Objects.equals(bytes[3], (byte) 0x72)
                    && Objects.equals(bytes[4], (byte) 0x63) && Objects.equals(bytes[5], (byte) 0x68)
                    && Objects.equals(bytes[6], (byte) 0x3e);
        }
    },
    /**
     * LZOP format.
     */
    LZOP("application/x-lzop", "lzo") {

        /**
         * Checks if the given bytes match the LZOP file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match LZOP signature (894C5A4F000D0A1A)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 7 && Objects.equals(bytes[0], (byte) 0x89) && Objects.equals(bytes[1], (byte) 0x4c)
                    && Objects.equals(bytes[2], (byte) 0x5a) && Objects.equals(bytes[3], (byte) 0x4f)
                    && Objects.equals(bytes[4], (byte) 0x00) && Objects.equals(bytes[5], (byte) 0x0d)
                    && Objects.equals(bytes[6], (byte) 0x0a) && Objects.equals(bytes[7], (byte) 0x1a);
        }
    },
    /**
     * LZ format.
     */
    LZ("application/x-lzip", "lz") {

        /**
         * Checks if the given bytes match the LZ file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match LZ signature (4C5A4950)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 3 && Objects.equals(bytes[0], (byte) 0x4c) && Objects.equals(bytes[1], (byte) 0x5a)
                    && Objects.equals(bytes[2], (byte) 0x49) && Objects.equals(bytes[3], (byte) 0x50);
        }
    },
    /**
     * ELF executable format.
     */
    ELF("application/x-executable", "elf") {

        /**
         * Checks if the given bytes match the ELF file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match ELF signature (7F454C46)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 52 && Objects.equals(bytes[0], (byte) 0x7f) && Objects.equals(bytes[1], (byte) 0x45)
                    && Objects.equals(bytes[2], (byte) 0x4c) && Objects.equals(bytes[3], (byte) 0x46);
        }
    },
    /**
     * LZ4 format.
     */
    LZ4("application/x-lz4", "lz4") {

        /**
         * Checks if the given bytes match the LZ4 file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match LZ4 signature (04224D18)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 4 && Objects.equals(bytes[0], (byte) 0x04) && Objects.equals(bytes[1], (byte) 0x22)
                    && Objects.equals(bytes[2], (byte) 0x4d) && Objects.equals(bytes[3], (byte) 0x18);
        }
    },
    /**
     * BR format. Reference: https://github.com/madler/brotli/blob/master/br-format-v3.txt, brotli does not have a fixed
     * file magic number, so this is just a reference.
     */
    BR("application/x-brotli", "br") {

        /**
         * Checks if the given bytes match the BR file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match BR signature (CEB2CF81)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 3 && Objects.equals(bytes[0], (byte) 0xce) && Objects.equals(bytes[1], (byte) 0xb2)
                    && Objects.equals(bytes[2], (byte) 0xcf) && Objects.equals(bytes[3], (byte) 0x81);
        }
    },
    /**
     * DCM format.
     */
    DCM("application/x-dicom", "dcm") {

        /**
         * Checks if the given bytes match the DCM file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match DCM signature (4449434D at offset 128)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 128 && Objects.equals(bytes[128], (byte) 0x44)
                    && Objects.equals(bytes[129], (byte) 0x49) && Objects.equals(bytes[130], (byte) 0x43)
                    && Objects.equals(bytes[131], (byte) 0x4d);
        }
    },
    /**
     * RPM format.
     */
    RPM("application/x-rpm", "rpm") {

        /**
         * Checks if the given bytes match the RPM file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match RPM signature (EDABEEDB)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 4 && Objects.equals(bytes[0], (byte) 0xed) && Objects.equals(bytes[1], (byte) 0xab)
                    && Objects.equals(bytes[2], (byte) 0xee) && Objects.equals(bytes[3], (byte) 0xdb);
        }
    },
    /**
     * ZSTD format.
     */
    ZSTD("application/x-zstd", "zst") {

        /**
         * Checks if the given bytes match the ZSTD file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match ZSTD signature (22-28 B52FFD or 50-5F 2A4D18)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final int length = bytes.length;
            if (length < 5) {
                return false;
            }
            final byte[] buf1 = new byte[] { (byte) 0x22, (byte) 0x23, (byte) 0x24, (byte) 0x25, (byte) 0x26,
                    (byte) 0x27, (byte) 0x28 };
            final boolean flag1 = ArrayKit.contains(buf1, bytes[0]) && Objects.equals(bytes[1], (byte) 0xb5)
                    && Objects.equals(bytes[2], (byte) 0x2f) && Objects.equals(bytes[3], (byte) 0xfd);
            if (flag1) {
                return true;
            }
            if ((bytes[0] & 0xF0) == 0x50) {
                return bytes[1] == 0x2A && bytes[2] == 0x4D && bytes[3] == 0x18;
            }
            return false;
        }
    },

    /**
     * MP4 video format.
     */
    MP4("video/mp4", "mp4") {

        /**
         * Checks if the given bytes match the MP4 file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match MP4 signature (667479704D534E56 or 6674797069736F6D)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 13) {
                return false;
            }
            final boolean flag1 = Objects.equals(bytes[4], (byte) 0x66) && Objects.equals(bytes[5], (byte) 0x74)
                    && Objects.equals(bytes[6], (byte) 0x79) && Objects.equals(bytes[7], (byte) 0x70)
                    && Objects.equals(bytes[8], (byte) 0x4d) && Objects.equals(bytes[9], (byte) 0x53)
                    && Objects.equals(bytes[10], (byte) 0x4e) && Objects.equals(bytes[11], (byte) 0x56);
            final boolean flag2 = Objects.equals(bytes[4], (byte) 0x66) && Objects.equals(bytes[5], (byte) 0x74)
                    && Objects.equals(bytes[6], (byte) 0x79) && Objects.equals(bytes[7], (byte) 0x70)
                    && Objects.equals(bytes[8], (byte) 0x69) && Objects.equals(bytes[9], (byte) 0x73)
                    && Objects.equals(bytes[10], (byte) 0x6f) && Objects.equals(bytes[11], (byte) 0x6d);
            return flag1 || flag2;
        }
    },
    /**
     * AVI video format.
     */
    AVI("video/x-msvideo", "avi") {

        /**
         * Checks if the given bytes match the AVI file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match AVI signature (52494646 and 41564920 at offset 8)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 11 && Objects.equals(bytes[0], (byte) 0x52) && Objects.equals(bytes[1], (byte) 0x49)
                    && Objects.equals(bytes[2], (byte) 0x46) && Objects.equals(bytes[3], (byte) 0x46)
                    && Objects.equals(bytes[8], (byte) 0x41) && Objects.equals(bytes[9], (byte) 0x56)
                    && Objects.equals(bytes[10], (byte) 0x49) && Objects.equals(bytes[11], (byte) 0x20);
        }
    },
    /**
     * WMV video format.
     */
    WMV("video/x-ms-wmv", "wmv") {

        /**
         * Checks if the given bytes match the WMV file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match WMV signature (3026B2758E66CF11A6D9)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 9 && Objects.equals(bytes[0], (byte) 0x30) && Objects.equals(bytes[1], (byte) 0x26)
                    && Objects.equals(bytes[2], (byte) 0xb2) && Objects.equals(bytes[3], (byte) 0x75)
                    && Objects.equals(bytes[4], (byte) 0x8e) && Objects.equals(bytes[5], (byte) 0x66)
                    && Objects.equals(bytes[6], (byte) 0xcf) && Objects.equals(bytes[7], (byte) 0x11)
                    && Objects.equals(bytes[8], (byte) 0xa6) && Objects.equals(bytes[9], (byte) 0xd9);
        }
    },
    /**
     * M4V video format.
     */
    M4V("video/x-m4v", "m4v") {

        /**
         * Checks if the given bytes match the M4V file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match M4V signature (667479704D345620 or 667479706D703432)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 12) {
                return false;
            }
            final boolean flag1 = Objects.equals(bytes[4], (byte) 0x66) && Objects.equals(bytes[5], (byte) 0x74)
                    && Objects.equals(bytes[6], (byte) 0x79) && Objects.equals(bytes[7], (byte) 0x70)
                    && Objects.equals(bytes[8], (byte) 0x4d) && Objects.equals(bytes[9], (byte) 0x34)
                    && Objects.equals(bytes[10], (byte) 0x56) && Objects.equals(bytes[11], (byte) 0x20);
            final boolean flag2 = Objects.equals(bytes[4], (byte) 0x66) && Objects.equals(bytes[5], (byte) 0x74)
                    && Objects.equals(bytes[6], (byte) 0x79) && Objects.equals(bytes[7], (byte) 0x70)
                    && Objects.equals(bytes[8], (byte) 0x6d) && Objects.equals(bytes[9], (byte) 0x70)
                    && Objects.equals(bytes[10], (byte) 0x34) && Objects.equals(bytes[11], (byte) 0x32);
            return flag1 || flag2;
        }
    },
    /**
     * FLV video format.
     */
    FLV("video/x-flv", "flv") {

        /**
         * Checks if the given bytes match the FLV file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match FLV signature (464C5601)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 3 && Objects.equals(bytes[0], (byte) 0x46) && Objects.equals(bytes[1], (byte) 0x4c)
                    && Objects.equals(bytes[2], (byte) 0x56) && Objects.equals(bytes[3], (byte) 0x01);
        }
    },
    /**
     * MKV video format.
     */
    MKV("video/x-matroska", "mkv") {

        /**
         * Checks if the given bytes match the MKV file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match MKV signature (1A45DFA3 and contains 4282886D6174726F736B61)
         */
        @Override
        public boolean match(final byte[] bytes) {
            // 0x42 0x82 0x88 0x6d 0x61 0x74 0x72 0x6f 0x73 0x6b 0x61
            final boolean flag1 = bytes.length > 11 && Objects.equals(bytes[0], (byte) 0x1a)
                    && Objects.equals(bytes[1], (byte) 0x45) && Objects.equals(bytes[2], (byte) 0xdf)
                    && Objects.equals(bytes[3], (byte) 0xa3);

            if (flag1) {
                // Need to check if it contains '\x42\x82\x88matroska', algorithm similar to KMP
                final byte[] bytes1 = { (byte) 0x42, (byte) 0x82, (byte) 0x88, (byte) 0x6d, (byte) 0x61, (byte) 0x74,
                        (byte) 0x72, (byte) 0x6f, (byte) 0x73, (byte) 0x6b, (byte) 0x61 };
                final int index = FileMagicNumber.indexOf(bytes, bytes1);
                return index > 0;
            }
            return false;
        }
    },
    /**
     * WEBM video format.
     */
    WEBM("video/webm", "webm") {

        /**
         * Checks if the given bytes match the WEBM file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match WEBM signature (1A45DFA3 and contains 4282887765626D)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final boolean flag1 = bytes.length > 8 && Objects.equals(bytes[0], (byte) 0x1a)
                    && Objects.equals(bytes[1], (byte) 0x45) && Objects.equals(bytes[2], (byte) 0xdf)
                    && Objects.equals(bytes[3], (byte) 0xa3);
            if (flag1) {
                // Need to check if it contains '\x42\x82\x88webm', algorithm similar to KMP
                final byte[] bytes1 = { (byte) 0x42, (byte) 0x82, (byte) 0x88, (byte) 0x77, (byte) 0x65, (byte) 0x62,
                        (byte) 0x6d };
                final int index = FileMagicNumber.indexOf(bytes, bytes1);
                return index > 0;
            }
            return false;
        }
    },
    /**
     * MOV video format. This file signature is very complex, only checking common ones.
     */
    MOV("video/quicktime", "mov") {

        /**
         * Checks if the given bytes match the MOV file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match MOV signature (66747970707420 or 6D6F6F76 or 66726565 or 6D646174 or
         *         77696465 or 706E6F74 or 736B6970)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 12) {
                return false;
            }
            final boolean flag1 = Objects.equals(bytes[4], (byte) 0x66) && Objects.equals(bytes[5], (byte) 0x74)
                    && Objects.equals(bytes[6], (byte) 0x79) && Objects.equals(bytes[7], (byte) 0x70)
                    && Objects.equals(bytes[8], (byte) 0x71) && Objects.equals(bytes[9], (byte) 0x74)
                    && Objects.equals(bytes[10], (byte) 0x20) && Objects.equals(bytes[11], (byte) 0x20);
            final boolean flag2 = Objects.equals(bytes[4], (byte) 0x6D) && Objects.equals(bytes[5], (byte) 0x6F)
                    && Objects.equals(bytes[6], (byte) 0x6F) && Objects.equals(bytes[7], (byte) 0x76);
            final boolean flag3 = Objects.equals(bytes[4], (byte) 0x66) && Objects.equals(bytes[5], (byte) 0x72)
                    && Objects.equals(bytes[6], (byte) 0x65) && Objects.equals(bytes[7], (byte) 0x65);
            final boolean flag4 = Objects.equals(bytes[4], (byte) 0x6D) && Objects.equals(bytes[5], (byte) 0x64)
                    && Objects.equals(bytes[6], (byte) 0x61) && Objects.equals(bytes[7], (byte) 0x74);
            final boolean flag5 = Objects.equals(bytes[4], (byte) 0x77) && Objects.equals(bytes[5], (byte) 0x69)
                    && Objects.equals(bytes[6], (byte) 0x64) && Objects.equals(bytes[7], (byte) 0x65);
            final boolean flag6 = Objects.equals(bytes[4], (byte) 0x70) && Objects.equals(bytes[5], (byte) 0x6E)
                    && Objects.equals(bytes[6], (byte) 0x6F) && Objects.equals(bytes[7], (byte) 0x74);
            final boolean flag7 = Objects.equals(bytes[4], (byte) 0x73) && Objects.equals(bytes[5], (byte) 0x6B)
                    && Objects.equals(bytes[6], (byte) 0x69) && Objects.equals(bytes[7], (byte) 0x70);
            return flag1 || flag2 || flag3 || flag4 || flag5 || flag6 || flag7;
        }
    },
    /**
     * MPEG video format.
     */
    MPEG("video/mpeg", "mpg") {

        /**
         * Checks if the given bytes match the MPEG file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match MPEG signature (000001B0-000001BF)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 3 && Objects.equals(bytes[0], (byte) 0x00) && Objects.equals(bytes[1], (byte) 0x00)
                    && Objects.equals(bytes[2], (byte) 0x01) && (bytes[3] >= (byte) 0xb0 && bytes[3] <= (byte) 0xbf);
        }
    },
    /**
     * RMVB video format.
     */
    RMVB("video/vnd.rn-realvideo", "rmvb") {

        /**
         * Checks if the given bytes match the RMVB file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match RMVB signature (2E524D46)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 4 && Objects.equals(bytes[0], (byte) 0x2E) && Objects.equals(bytes[1], (byte) 0x52)
                    && Objects.equals(bytes[2], (byte) 0x4D) && Objects.equals(bytes[3], (byte) 0x46);
        }
    },
    /**
     * 3GP video format.
     */
    M3GP("video/3gpp", "3gp") {

        /**
         * Checks if the given bytes match the 3GP file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match 3GP signature (66747970336770)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 10 && Objects.equals(bytes[4], (byte) 0x66) && Objects.equals(bytes[5], (byte) 0x74)
                    && Objects.equals(bytes[6], (byte) 0x79) && Objects.equals(bytes[7], (byte) 0x70)
                    && Objects.equals(bytes[8], (byte) 0x33) && Objects.equals(bytes[9], (byte) 0x67)
                    && Objects.equals(bytes[10], (byte) 0x70);
        }
    },

    /**
     * DOC format.
     */
    DOC("application/msword", "doc") {

        /**
         * Checks if the given bytes match the DOC file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match DOC signature (D0CF11E0A1B11AE1 with ECA5C100 or specific document
         *         content)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0xd0, (byte) 0xcf, (byte) 0x11, (byte) 0xe0, (byte) 0xa1,
                    (byte) 0xb1, (byte) 0x1a, (byte) 0xe1 };
            if (bytes.length > 515 && ArrayKit.isSubEquals(bytes, 0, byte1)) {
                final byte[] byte2 = new byte[] { (byte) 0xec, (byte) 0xa5, (byte) 0xc1, (byte) 0x00 };
                // check 512:516
                if (ArrayKit.isSubEquals(bytes, 512, byte2)) {
                    return true;
                }
                final byte[] byte3 = new byte[] { (byte) 0x00, (byte) 0x0a, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x4d, (byte) 0x53, (byte) 0x57, (byte) 0x6f, (byte) 0x72, (byte) 0x64, (byte) 0x44,
                        (byte) 0x6f, (byte) 0x63, (byte) 0x00, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x57, (byte) 0x6f, (byte) 0x72, (byte) 0x64, (byte) 0x2e, (byte) 0x44, (byte) 0x6f,
                        (byte) 0x63, (byte) 0x75, (byte) 0x6d, (byte) 0x65, (byte) 0x6e, (byte) 0x74, (byte) 0x2e,
                        (byte) 0x38, (byte) 0x00, (byte) 0xf4, (byte) 0x39, (byte) 0xb2, (byte) 0x71 };
                final byte[] range = Arrays.copyOfRange(bytes, 2075, 2142);
                return bytes.length > 2142 && FileMagicNumber.indexOf(range, byte3) > 0;
            }
            return false;
        }
    },
    /**
     * XLS format.
     */
    XLS("application/vnd.ms-excel", "xls") {

        /**
         * Checks if the given bytes match the XLS file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match XLS signature (D0CF11E0A1B11AE1 with FDFFFFFF or 09081000000605...)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0xd0, (byte) 0xcf, (byte) 0x11, (byte) 0xe0, (byte) 0xa1,
                    (byte) 0xb1, (byte) 0x1a, (byte) 0xe1 };
            final boolean flag1 = bytes.length > 520 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 8), byte1);
            if (flag1) {
                final byte[] byte2 = new byte[] { (byte) 0xfd, (byte) 0xff, (byte) 0xff, (byte) 0xff };
                final boolean flag2 = Arrays.equals(Arrays.copyOfRange(bytes, 512, 516), byte2)
                        && (bytes[518] == 0x00 || bytes[518] == 0x02);
                final byte[] byte3 = new byte[] { (byte) 0x09, (byte) 0x08, (byte) 0x10, (byte) 0x00, (byte) 0x00,
                        (byte) 0x06, (byte) 0x05, (byte) 0x00 };
                final boolean flag3 = Arrays.equals(Arrays.copyOfRange(bytes, 512, 520), byte3);
                final byte[] byte4 = new byte[] { (byte) 0xe2, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x5c,
                        (byte) 0x00, (byte) 0x70, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x43,
                        (byte) 0x61, (byte) 0x6c, (byte) 0x63 };
                final boolean flag4 = bytes.length > 2095
                        && Arrays.equals(Arrays.copyOfRange(bytes, 1568, 2095), byte4);
                return flag2 || flag3 || flag4;
            }
            return false;
        }

    },
    /**
     * PPT format.
     */
    PPT("application/vnd.ms-powerpoint", "ppt") {

        /**
         * Checks if the given bytes match the PPT file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match PPT signature (D0CF11E0A1B11AE1 with A0461DF0 or 006E1EF0 or 0F00E803
         *         or FDFFFFFF...)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0xd0, (byte) 0xcf, (byte) 0x11, (byte) 0xe0, (byte) 0xa1,
                    (byte) 0xb1, (byte) 0x1a, (byte) 0xe1 };
            final boolean flag1 = bytes.length > 524 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 8), byte1);
            if (flag1) {
                final byte[] byte2 = new byte[] { (byte) 0xa0, (byte) 0x46, (byte) 0x1d, (byte) 0xf0 };
                final byte[] byteRange = Arrays.copyOfRange(bytes, 512, 516);
                final boolean flag2 = Arrays.equals(byteRange, byte2);
                final byte[] byte3 = new byte[] { (byte) 0x00, (byte) 0x6e, (byte) 0x1e, (byte) 0xf0 };
                final boolean flag3 = Arrays.equals(byteRange, byte3);
                final byte[] byte4 = new byte[] { (byte) 0x0f, (byte) 0x00, (byte) 0xe8, (byte) 0x03 };
                final boolean flag4 = Arrays.equals(byteRange, byte4);
                final byte[] byte5 = new byte[] { (byte) 0xfd, (byte) 0xff, (byte) 0xff, (byte) 0xff };
                final boolean flag5 = Arrays.equals(byteRange, byte5) && bytes[522] == 0x00 && bytes[523] == 0x00;
                final byte[] byte6 = new byte[] { (byte) 0x00, (byte) 0xb9, (byte) 0x29, (byte) 0xe8, (byte) 0x11,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4d, (byte) 0x53, (byte) 0x20, (byte) 0x50,
                        (byte) 0x6f, (byte) 0x77, (byte) 0x65, (byte) 0x72, (byte) 0x50, (byte) 0x6f, (byte) 0x69,
                        (byte) 0x6e, (byte) 0x74, (byte) 0x20, (byte) 0x39, (byte) 0x37 };
                final boolean flag6 = bytes.length > 2096
                        && Arrays.equals(Arrays.copyOfRange(bytes, 2072, 2096), byte6);
                return flag2 || flag3 || flag4 || flag5 || flag6;
            }
            return false;
        }
    },
    /**
     * DOCX format.
     */
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx") {

        /**
         * Checks if the given bytes match the DOCX file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match DOCX signature (ZIP with word/ content)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return Objects.equals(FileMagicNumber.matchDocument(bytes), DOCX);
        }
    },
    /**
     * PPTX format.
     */
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx") {

        /**
         * Checks if the given bytes match the PPTX file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match PPTX signature (ZIP with ppt/ content)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return Objects.equals(FileMagicNumber.matchDocument(bytes), PPTX);
        }
    },
    /**
     * XLSX format.
     */
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx") {

        /**
         * Checks if the given bytes match the XLSX file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match XLSX signature (ZIP with xl/ content)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return Objects.equals(FileMagicNumber.matchDocument(bytes), XLSX);
        }
    },
    /**
     * WASM format.
     */
    WASM("application/wasm", "wasm") {

        /**
         * Checks if the given bytes match the WASM file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match WASM signature (0061736D01000000)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return ArrayKit.startWith(
                    bytes,
                    (byte) 0x00,
                    (byte) 0x61,
                    (byte) 0x73,
                    (byte) 0x6D,
                    (byte) 0x01,
                    (byte) 0x00,
                    (byte) 0x00,
                    (byte) 0x00);
        }
    },
    /**
     * DEX format. Reference: https://source.android.com/devices/tech/dalvik/dex-format#dex-file-magic
     */
    DEX("application/vnd.android.dex", "dex") {

        /**
         * Checks if the given bytes match the DEX file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match DEX signature (64656X0A...70)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 36 && Objects.equals(bytes[0], (byte) 0x64) && Objects.equals(bytes[1], (byte) 0x65)
                    && Objects.equals(bytes[2], (byte) 0x78) && Objects.equals(bytes[3], (byte) 0x0A)
                    && Objects.equals(bytes[36], (byte) 0x70);
        }
    },
    /**
     * DEY format.
     */
    DEY("application/vnd.android.dey", "dey") {

        /**
         * Checks if the given bytes match the DEY file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match DEY signature (6465790A... and contains DEX)
         */
        @Override
        public boolean match(final byte[] bytes) {
            return bytes.length > 100 && Objects.equals(bytes[0], (byte) 0x64) && Objects.equals(bytes[1], (byte) 0x65)
                    && Objects.equals(bytes[2], (byte) 0x79) && Objects.equals(bytes[3], (byte) 0x0A)
                    && DEX.match(Arrays.copyOfRange(bytes, 40, 100));
        }
    },
    /**
     * EML format.
     */
    EML("message/rfc822", "eml") {

        /**
         * Checks if the given bytes match the EML file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match EML signature (46726F6D202020 or 46726F6D203F3F3F or 46726F6D3A20 or
         *         52657475726E2D506174683A20)
         */
        @Override
        public boolean match(final byte[] bytes) {
            if (bytes.length < 8) {
                return false;
            }
            final byte[] byte1 = new byte[] { (byte) 0x46, (byte) 0x72, (byte) 0x6F, (byte) 0x6D, (byte) 0x20,
                    (byte) 0x20, (byte) 0x20 };
            final byte[] byte2 = new byte[] { (byte) 0x46, (byte) 0x72, (byte) 0x6F, (byte) 0x6D, (byte) 0x20,
                    (byte) 0x3F, (byte) 0x3F, (byte) 0x3F };
            final byte[] byte3 = new byte[] { (byte) 0x46, (byte) 0x72, (byte) 0x6F, (byte) 0x6D, (byte) 0x3A,
                    (byte) 0x20 };
            final byte[] byte4 = new byte[] { (byte) 0x52, (byte) 0x65, (byte) 0x74, (byte) 0x75, (byte) 0x72,
                    (byte) 0x6E, (byte) 0x2D, (byte) 0x50, (byte) 0x61, (byte) 0x74, (byte) 0x68, (byte) 0x3A,
                    (byte) 0x20 };
            return Arrays.equals(Arrays.copyOfRange(bytes, 0, 7), byte1)
                    || Arrays.equals(Arrays.copyOfRange(bytes, 0, 8), byte2)
                    || Arrays.equals(Arrays.copyOfRange(bytes, 0, 6), byte3)
                    || bytes.length > 13 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 13), byte4);
        }
    },
    /**
     * MDB format.
     */
    MDB("application/vnd.ms-access", "mdb") {

        /**
         * Checks if the given bytes match the MDB file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match MDB signature (000100005374616E64617264204A6574204442)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x53,
                    (byte) 0x74, (byte) 0x61, (byte) 0x6E, (byte) 0x64, (byte) 0x61, (byte) 0x72, (byte) 0x64,
                    (byte) 0x20, (byte) 0x4A, (byte) 0x65, (byte) 0x74, (byte) 0x20, (byte) 0x44, (byte) 0x42 };
            return bytes.length > 18 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 18), byte1);
        }
    },
    /**
     * CHM format. 49 54 53 46
     */
    CHM("application/vnd.ms-htmlhelp", "chm") {

        /**
         * Checks if the given bytes match the CHM file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match CHM signature (49545346)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0x49, (byte) 0x54, (byte) 0x53, (byte) 0x46 };
            return bytes.length > 4 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), byte1);
        }
    },

    /**
     * CLASS format. CA FE BA BE
     */
    CLASS("application/java-vm", "class") {

        /**
         * Checks if the given bytes match the CLASS file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match CLASS signature (CAFEBABE)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE };
            return bytes.length > 4 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), byte1);
        }
    },
    /**
     * TORRENT format. 64 38 3A 61 6E 6E 6F 75 6E 63 65
     */
    TORRENT("application/x-bittorrent", "torrent") {

        /**
         * Checks if the given bytes match the TORRENT file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match TORRENT signature (64383A616E6E6F756E6365)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0x64, (byte) 0x38, (byte) 0x3A, (byte) 0x61, (byte) 0x6E,
                    (byte) 0x6E, (byte) 0x6F, (byte) 0x75, (byte) 0x6E, (byte) 0x63, (byte) 0x65 };
            return bytes.length > 11 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 11), byte1);
        }
    },
    /**
     * WPD format.
     */
    WPD("application/vnd.wordperfect", "wpd") {

        /**
         * Checks if the given bytes match the WPD file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match WPD signature (FF575043)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0xFF, (byte) 0x57, (byte) 0x50, (byte) 0x43 };
            return bytes.length > 4 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), byte1);
        }
    },
    /**
     * DBX format.
     */
    DBX("", "dbx") {

        /**
         * Checks if the given bytes match the DBX file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match DBX signature (CFAD12FE)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0xCF, (byte) 0xAD, (byte) 0x12, (byte) 0xFE };
            return bytes.length > 4 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), byte1);
        }
    },
    /**
     * PST format.
     */
    PST("application/vnd.ms-outlook-pst", "pst") {

        /**
         * Checks if the given bytes match the PST file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match PST signature (2142444E)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0x21, (byte) 0x42, (byte) 0x44, (byte) 0x4E };
            return bytes.length > 4 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), byte1);
        }
    },
    /**
     * RAM format.
     */
    RAM("audio/x-pn-realaudio", "ram") {

        /**
         * Checks if the given bytes match the RAM file signature.
         *
         * @param bytes the file bytes to check
         * @return {@code true} if the bytes match RAM signature (2E7261FD00)
         */
        @Override
        public boolean match(final byte[] bytes) {
            final byte[] byte1 = new byte[] { (byte) 0x2E, (byte) 0x72, (byte) 0x61, (byte) 0xFD, (byte) 0x00 };
            return bytes.length > 5 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 5), byte1);
        }
    };

    /**
     * The MIME type of the file.
     */
    private final String mimeType;
    /**
     * The file extension.
     */
    private final String extension;

    /**
     * Constructs a FileMagicNumber enum constant.
     *
     * @param mimeType  the MIME type
     * @param extension the file extension
     */
    FileMagicNumber(final String mimeType, final String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    /**
     * Gets the corresponding FileMagicNumber based on the given bytes.
     *
     * @param bytes the magic number bytes
     * @return the FileMagicNumber
     */
    public static FileMagicNumber getMagicNumber(final byte[] bytes) {
        if (ObjectKit.isNull(bytes)) {
            return UNKNOWN;
        }

        final FileMagicNumber number = Arrays.stream(values()).filter(fileMagicNumber -> fileMagicNumber.match(bytes))
                .findFirst().orElse(UNKNOWN);

        // For compressed document types like office or jar
        if (FileMagicNumber.ZIP.equals(number)) {
            final FileMagicNumber fn = FileMagicNumber.matchDocument(bytes);
            return fn == UNKNOWN ? ZIP : fn;
        }
        return number;
    }

    /**
     * Finds the index of the target array in the source array.
     *
     * @param array  the source array
     * @param target the target array to find
     * @return the starting index or -1 if not found
     */
    private static int indexOf(final byte[] array, final byte[] target) {
        if (array == null || target == null || array.length < target.length) {
            return -1;
        }
        if (target.length == 0) {
            return 0;
        } else {
            label1: for (int i = 0; i < array.length - target.length + 1; ++i) {
                for (int j = 0; j < target.length; ++j) {
                    if (array[i + j] != target[j]) {
                        continue label1;
                    }
                }
                return i;
            }
            return -1;
        }
    }

    /**
     * Compares a slice of bytes from the buffer with the given slice starting at the offset.
     *
     * @param buf         the buffer
     * @param slice       the slice to compare
     * @param startOffset the starting offset in the buffer
     * @return true if matches, false otherwise
     */
    private static boolean compareBytes(final byte[] buf, final byte[] slice, final int startOffset) {
        final int sl = slice.length;
        if (startOffset + sl > buf.length) {
            return false;
        }
        final byte[] sub = Arrays.copyOfRange(buf, startOffset, startOffset + sl);
        return Arrays.equals(sub, slice);
    }

    /**
     * Matches the Open XML mime type.
     *
     * @param bytes  the bytes
     * @param offset the offset
     * @return the FileMagicNumber
     */
    private static FileMagicNumber matchOpenXmlMime(final byte[] bytes, final int offset) {
        final byte[] word = new byte[] { 'w', 'o', 'r', 'd', '/' };
        final byte[] ppt = new byte[] { 'p', 'p', 't', '/' };
        final byte[] xl = new byte[] { 'x', 'l', '/' };
        if (FileMagicNumber.compareBytes(bytes, word, offset)) {
            return FileMagicNumber.DOCX;
        }
        if (FileMagicNumber.compareBytes(bytes, ppt, offset)) {
            return FileMagicNumber.PPTX;
        }
        if (FileMagicNumber.compareBytes(bytes, xl, offset)) {
            return FileMagicNumber.XLSX;
        }
        return FileMagicNumber.UNKNOWN;
    }

    /**
     * Matches the document type.
     *
     * @param bytes the bytes
     * @return the matched FileMagicNumber
     */
    private static FileMagicNumber matchDocument(final byte[] bytes) {
        final FileMagicNumber fileMagicNumber = FileMagicNumber.matchOpenXmlMime(bytes, 0x1e);
        if (!fileMagicNumber.equals(UNKNOWN)) {
            return fileMagicNumber;
        }
        final byte[] bytes1 = new byte[] { 0x5B, 0x43, 0x6F, 0x6E, 0x74, 0x65, 0x6E, 0x74, 0x5F, 0x54, 0x79, 0x70, 0x65,
                0x73, 0x5D, 0x2E, 0x78, 0x6D, 0x6C };
        final byte[] bytes2 = new byte[] { 0x5F, 0x72, 0x65, 0x6C, 0x73, 0x2F, 0x2E, 0x72, 0x65, 0x6C, 0x73 };
        final byte[] bytes3 = new byte[] { 0x64, 0x6F, 0x63, 0x50, 0x72, 0x6F, 0x70, 0x73 };
        final boolean flag1 = FileMagicNumber.compareBytes(bytes, bytes1, 0x1e);
        final boolean flag2 = FileMagicNumber.compareBytes(bytes, bytes2, 0x1e);
        final boolean flag3 = FileMagicNumber.compareBytes(bytes, bytes3, 0x1e);
        if (!(flag1 || flag2 || flag3)) {
            return UNKNOWN;
        }
        int index = 0;
        for (int i = 0; i < 4; i++) {
            index = searchSignature(bytes, index + 4, 6000);
            if (index == -1) {
                continue;
            }
            final FileMagicNumber fn = FileMagicNumber.matchOpenXmlMime(bytes, index + 30);
            if (!fn.equals(UNKNOWN)) {
                return fn;
            }
        }
        return UNKNOWN;
    }

    /**
     * Searches for the document signature.
     *
     * @param bytes    the bytes
     * @param start    the start position
     * @param rangeNum the step length
     * @return the signature index
     */
    private static int searchSignature(final byte[] bytes, final int start, final int rangeNum) {
        final byte[] signature = new byte[] { 0x50, 0x4B, 0x03, 0x04 };
        final int length = bytes.length;
        int end = start + rangeNum;
        if (end > length) {
            end = length;
        }
        final int index = FileMagicNumber.indexOf(Arrays.copyOfRange(bytes, start, end), signature);
        return (index == -1) ? -1 : (start + index);
    }

    /**
     * Gets the MIME type.
     *
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Gets the extension.
     *
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Checks if matches the bytes.
     *
     * @param bytes the bytes
     * @return true if matches
     */
    public abstract boolean match(byte[] bytes);

}
