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
package org.miaixz.bus.core.codec;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;

import org.miaixz.bus.core.codec.binary.provider.Base16Provider;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Percent-encoding, also known as URL encoding. Percent-encoding can be used for URI encoding and also for preparing
 * data for "application/x-www-form-urlencoded" MIME type.
 *
 * <p>
 * Percent-encoding encodes characters that are not allowed in URIs or other special allowed characters. For encoded
 * characters, they are eventually converted to a form starting with a percent sign "%" followed by two hexadecimal
 * digits. For example, the space character (SP) is a disallowed character, its ASCII binary value is "00100000", and it
 * is finally converted to "%20".
 *
 * <p>
 * Different scenarios should follow different specifications:
 * <ul>
 * <li>URI: Follows RFC 3986 reserved character specification.</li>
 * <li>application/x-www-form-urlencoded: Follows W3C HTML Form content types specification, e.g., spaces must be
 * converted to '+'.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PercentCodec implements Encoder<byte[], byte[]>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852281130770L;

    /**
     * Stores the set of safe characters that should not be encoded.
     */
    private final BitSet safeCharacters;

    /**
     * A flag indicating whether spaces should be encoded as '+'. If {@code true}, spaces are encoded as "+". This is
     * typically used in "application/x-www-form-urlencoded". If {@code false}, spaces are encoded as "%20". This is
     * generally used in the Query part of URLs (RFC3986 specification).
     */
    private boolean encodeSpaceAsPlus = false;

    /**
     * Constructs a new {@code PercentCodec} instance. By default, alphanumeric characters (a-z, A-Z, 0-9) are
     * considered safe and will not be encoded.
     */
    public PercentCodec() {
        this(new BitSet(Normal._256));
    }

    /**
     * Constructs a new {@code PercentCodec} instance with a custom set of safe characters.
     *
     * @param safeCharacters A {@link BitSet} where each set bit indicates a safe character that should not be encoded.
     */
    public PercentCodec(final BitSet safeCharacters) {
        this.safeCharacters = safeCharacters;
    }

    /**
     * Checks if the given character is considered a safe character by this codec. Safe characters are not encoded.
     *
     * @param c The character to check.
     * @return {@code true} if the character is safe, {@code false} otherwise.
     */
    public boolean isSafe(final char c) {
        return this.safeCharacters.get(c);
    }

    /**
     * Encodes a byte array using percent-encoding. Each byte is processed, and if it's not a safe character (or a space
     * to be encoded as '+'), it's converted to its percent-encoded form (%HH).
     *
     * @param bytes The byte array to be encoded.
     * @return The percent-encoded byte array.
     */
    @Override
    public byte[] encode(final byte[] bytes) {
        // Initial capacity calculation, roughly assuming all bytes need to be escaped, capacity is three times.
        final ByteBuffer buffer = ByteBuffer.allocate(bytes.length * 3);
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < bytes.length; i++) {
            encodeTo(buffer, bytes[i]);
        }

        return buffer.array();
    }

    /**
     * Encodes a character sequence into a percent-encoded string using the specified charset. Custom safe characters
     * can be provided to prevent them from being encoded.
     *
     * @param path           The character sequence to encode.
     * @param charset        The charset to use for converting characters to bytes before encoding. If {@code null}, the
     *                       original string is returned without encoding.
     * @param customSafeChar Optional custom safe characters that should not be encoded.
     * @return The percent-encoded string.
     */
    public String encode(final CharSequence path, final Charset charset, final char... customSafeChar) {
        if (null == charset || StringKit.isEmpty(path)) {
            return StringKit.toStringOrNull(path);
        }

        final StringBuilder rewrittenPath = new StringBuilder(path.length() * 3);
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        final OutputStreamWriter writer = new OutputStreamWriter(buf, charset);

        char c;
        for (int i = 0; i < path.length(); i++) {
            c = path.charAt(i);
            if (safeCharacters.get(c) || ArrayKit.contains(customSafeChar, c)) {
                rewrittenPath.append(c);
            } else if (encodeSpaceAsPlus && c == Symbol.C_SPACE) {
                // Special handling for space
                rewrittenPath.append(Symbol.C_PLUS);
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write(c);
                    writer.flush();
                } catch (final IOException e) {
                    buf.reset();
                    continue;
                }

                // Handle double-byte Unicode characters (e.g., some emojis)
                final byte[] ba = buf.toByteArray();
                for (final byte toEncode : ba) {
                    // Converting each byte in the buffer
                    rewrittenPath.append(Symbol.C_PERCENT);
                    HexKit.appendHex(rewrittenPath, toEncode, false);
                }
                buf.reset();
            }
        }
        return rewrittenPath.toString();
    }

    /**
     * Encodes a single byte into the provided {@link ByteBuffer}. If the byte represents a safe character or a space to
     * be encoded as '+', it's written directly. Otherwise, it's written as its percent-encoded form (%HH).
     *
     * @param buffer The {@link ByteBuffer} to write the encoded byte to.
     * @param b      The byte to encode.
     */
    private void encodeTo(final ByteBuffer buffer, final byte b) {
        if (safeCharacters.get(b)) {
            // Skip safe characters
            buffer.put(b);
        } else if (encodeSpaceAsPlus && b == Symbol.C_SPACE) {
            // Special handling for space
            buffer.put((byte) Symbol.C_PLUS);
        } else {
            buffer.put((byte) Symbol.C_PERCENT);
            buffer.put((byte) Base16Provider.CODEC_UPPER.hexDigit(b >> 4));
            buffer.put((byte) Base16Provider.CODEC_UPPER.hexDigit(b));
        }
    }

    /**
     * Builder for {@link PercentCodec}. Since {@link PercentCodec} itself should be an immutable object, its
     * construction is handled by this Builder.
     */
    public static class Builder implements org.miaixz.bus.core.Builder<PercentCodec> {

        /**
         * The internal {@link PercentCodec} instance being built.
         */
        private final PercentCodec codec;

        /**
         * Private constructor to create a Builder for a given {@link PercentCodec}.
         *
         * @param codec The {@link PercentCodec} instance to build upon.
         */
        private Builder(final PercentCodec codec) {
            this.codec = codec;
        }

        /**
         * Creates a new {@code Builder} instance by copying the safe characters from an existing {@link PercentCodec}.
         *
         * @param codec The existing {@link PercentCodec} to copy safe characters from.
         * @return A new {@code Builder} instance.
         */
        public static Builder of(final PercentCodec codec) {
            return new Builder(new PercentCodec((BitSet) codec.safeCharacters.clone()));
        }

        /**
         * Creates a new {@code Builder} instance, initializing its safe characters with those present in the given
         * character sequence.
         *
         * @param chars The character sequence whose characters will be marked as safe. Must not be null.
         * @return A new {@code Builder} instance.
         * @throws NullPointerException if {@code chars} is null.
         */
        public static Builder of(final CharSequence chars) {
            Assert.notNull(chars, "chars must not be null");
            final Builder builder = of(new PercentCodec());
            final int length = chars.length();
            for (int i = 0; i < length; i++) {
                builder.addSafe(chars.charAt(i));
            }
            return builder;
        }

        /**
         * Adds a single character to the set of safe characters. Safe characters will not be percent-encoded.
         *
         * @param c The character to add as a safe character.
         * @return This {@code Builder} instance for method chaining.
         */
        public Builder addSafe(final char c) {
            codec.safeCharacters.set(c);
            return this;
        }

        /**
         * Adds all characters from the given string to the set of safe characters. Safe characters will not be
         * percent-encoded.
         *
         * @param chars The string containing characters to add as safe characters.
         * @return This {@code Builder} instance for method chaining.
         */
        public Builder addSafes(final String chars) {
            final int length = chars.length();
            for (int i = 0; i < length; i++) {
                addSafe(chars.charAt(i));
            }
            return this;
        }

        /**
         * Removes a single character from the set of safe characters. Characters removed from the safe set will be
         * subject to percent-encoding.
         *
         * @param c The character to remove from safe characters.
         * @return This {@code Builder} instance for method chaining.
         */
        public Builder removeSafe(final char c) {
            codec.safeCharacters.clear(c);
            return this;
        }

        /**
         * Performs a logical OR operation on the current set of safe characters with the safe characters from another
         * {@link PercentCodec}. This effectively adds all safe characters from the {@code otherCodec} to this builder's
         * safe characters.
         *
         * @param otherCodec The {@link PercentCodec} whose safe characters will be combined with this builder's.
         * @return This {@code Builder} instance for method chaining.
         */
        public Builder or(final PercentCodec otherCodec) {
            codec.safeCharacters.or(otherCodec.safeCharacters);
            return this;
        }

        /**
         * Sets whether spaces should be encoded as '+' or '%20'. If {@code true}, spaces are encoded as "+". This is
         * typically used in "application/x-www-form-urlencoded". If {@code false}, spaces are encoded as "%20". This is
         * generally used in the Query part of URLs (RFC3986 specification).
         *
         * @param encodeSpaceAsPlus A boolean indicating whether to encode spaces as '+'.
         * @return This {@code Builder} instance for method chaining.
         */
        public Builder setEncodeSpaceAsPlus(final boolean encodeSpaceAsPlus) {
            codec.encodeSpaceAsPlus = encodeSpaceAsPlus;
            return this;
        }

        /**
         * Builds and returns the configured {@link PercentCodec} instance.
         *
         * @return A new {@link PercentCodec} instance with the configured settings.
         */
        @Override
        public PercentCodec build() {
            return codec;
        }
    }

}
