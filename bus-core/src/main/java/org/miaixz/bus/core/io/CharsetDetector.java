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
package org.miaixz.bus.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Charset detector for files and input streams.
 *
 * @author Kimi Liu
 */
public class CharsetDetector {

    /**
     * Keeps byte-stream charset detection on the static API.
     */
    public CharsetDetector() {
        // No initialization required.
    }

    /**
     * Default charsets to participate in the detection test.
     */
    private static final java.nio.charset.Charset[] DEFAULT_CHARSETS;

    static {
        final String[] names = { Charset.DEFAULT_UTF_8, Charset.DEFAULT_GBK, Charset.DEFAULT_GB_2312,
                Charset.DEFAULT_GB_18030, Charset.DEFAULT_UTF_16_BE, Charset.DEFAULT_UTF_16_LE, Charset.DEFAULT_UTF_16,
                "BIG5", "UNICODE", Charset.DEFAULT_US_ASCII };
        DEFAULT_CHARSETS = Convert.convert(java.nio.charset.Charset[].class, names);
    }

    /**
     * Detects the charset of a given file.
     *
     * @param file     The file to detect the charset from.
     * @param charsets The charsets to test. If null or empty, {@link #DEFAULT_CHARSETS} will be used.
     * @return The detected {@link java.nio.charset.Charset}, or null if no charset could be reliably detected.
     */
    public static java.nio.charset.Charset detect(final File file, final java.nio.charset.Charset... charsets) {
        return detect(FileKit.getInputStream(file), charsets);
    }

    /**
     * Detects the charset of a given input stream. Note: This method reads a portion of the stream and then closes it.
     * If the stream needs to be reused, please use a stream that supports the {@code reset()} method.
     *
     * @param in       The input stream to detect the charset from. This stream will be closed after detection.
     * @param charsets The charsets to test. If null or empty, {@link #DEFAULT_CHARSETS} will be used.
     * @return The detected {@link java.nio.charset.Charset}, or null if no charset could be reliably detected.
     * @throws InternalException if an {@link IOException} occurs during stream reading.
     */
    public static java.nio.charset.Charset detect(final InputStream in, final java.nio.charset.Charset... charsets) {
        return detect(Normal._8192, in, charsets);
    }

    /**
     * Detects the charset of a given input stream with a specified buffer size. Note: This method reads a portion of
     * the stream and then closes it. If the stream needs to be reused, please use a stream that supports the
     * {@code reset()} method.
     *
     * @param bufferSize The custom buffer size, i.e., the length checked each time.
     * @param in         The input stream to detect the charset from. This stream will be closed after detection.
     * @param charsets   The charsets to test. If null or empty, {@link #DEFAULT_CHARSETS} will be used.
     * @return The detected {@link java.nio.charset.Charset}, or null if no charset could be reliably detected.
     * @throws InternalException if an {@link IOException} occurs during stream reading.
     */
    public static java.nio.charset.Charset detect(
            final int bufferSize,
            final InputStream in,
            java.nio.charset.Charset... charsets) {
        if (ArrayKit.isEmpty(charsets)) {
            charsets = DEFAULT_CHARSETS;
        }

        final byte[] buffer = new byte[bufferSize];
        try {
            while (in.read(buffer) > -1) {
                for (final java.nio.charset.Charset charset : charsets) {
                    final CharsetDecoder decoder = charset.newDecoder();
                    if (identify(buffer, decoder)) {
                        return charset;
                    }
                }
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }
        return null;
    }

    /**
     * Attempts to identify if the given bytes can be decoded by the provided {@link CharsetDecoder}.
     *
     * @param bytes   The bytes to test.
     * @param decoder The {@link CharsetDecoder} to use for identification.
     * @return True if the bytes can be decoded by the decoder, false otherwise.
     */
    private static boolean identify(final byte[] bytes, final CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (final CharacterCodingException e) {
            return false;
        }
        return true;
    }

}
