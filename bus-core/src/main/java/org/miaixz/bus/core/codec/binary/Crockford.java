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
package org.miaixz.bus.core.codec.binary;

import java.util.Objects;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

/**
 * An implementation of Crockford's Base32 encoding and decoding.
 *
 * <p>
 * This class provides Base32 encoding and decoding as defined by <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC
 * 4648</a>, but it uses a custom alphabet first coined by Douglas Crockford. One addition to the alphabet is that 'u'
 * and 'U' characters decode as if they were 'V' to improve tolerance for human input errors.
 *
 * <p>
 * This class operates directly on byte streams, and not character streams.
 *
 * @author Kimi Liu
 * @see <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC 4648</a>
 * @see <a href="http://www.crockford.com/wrmg/base32.html">Douglas Crockford's Base32 Encoding</a>
 * @since Java 17+
 */
public class Crockford {

    /**
     * Mask used to extract 8 bits, used in decoding bytes
     */
    protected static final int MASK_8BITS = 0xff;
    private static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;
    private static final int DEFAULT_BUFFER_RESIZE_FACTOR = 2;
    /**
     * Mask used to extract 5 bits, used when encoding Base32 bytes
     */
    private static final int MASK_5BITS = 0x1f;
    /**
     * BASE32 characters are 5 bits in length. They are formed by taking a block of five octets to form a 40-bit string,
     * which is converted into eight BASE32 characters.
     */
    private static final int BITS_PER_ENCODED_BYTE = 5;
    private static final int BYTES_PER_ENCODED_BLOCK = 8;
    private static final int BYTES_PER_UNENCODED_BLOCK = 5;
    /**
     * This array is a lookup table that translates 5-bit positive integer index values into their "Base32 Alphabet"
     * equivalents as specified in Table 3 of RFC 2045.
     */
    private static final byte[] ENCODE_TABLE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };
    /**
     * Convenience variable to help us determine when our buffer is going to run out of room and needs resizing.
     * {@code decodeSize = BYTES_PER_ENCODED_BLOCK - 1;}
     */
    private final int decodeSize;
    /**
     * Convenience variable to help us determine when our buffer is going to run out of room and needs resizing.
     * {@code encodeSize = BYTES_PER_ENCODED_BLOCK;}
     */
    private final int encodeSize;
    /**
     * Whether this encoder should use a padding character at the end of encoded Strings.
     */
    private final boolean usePaddingCharacter;
    /**
     * Buffer for streaming.
     */
    protected byte[] buffer;
    /**
     * Position where next character should be written in the buffer.
     */
    protected int pos;
    /**
     * Boolean flag to indicate the EOF has been reached. Once EOF has been reached, this object becomes useless, and
     * must be thrown away.
     */
    protected boolean eof;
    /**
     * Writes to the buffer only occur after every 5 reads when encoding, and every 8 reads when decoding. This variable
     * helps track that.
     */
    protected int modulus;
    /**
     * Place holder for the bytes we're dealing with for our based logic. Bitwise operations store and extract the
     * encoding or decoding from this variable.
     */
    private long bitWorkArea;

    /**
     * Constructs a new Crockford codec with padding disabled.
     */
    public Crockford() {
        this(false);
    }

    /**
     * Constructs a {@code Crockford} codec with an option to enable or disable padding.
     *
     * @param usePaddingCharacter If {@code true}, the padding character '=' will be used.
     */
    public Crockford(final boolean usePaddingCharacter) {
        this.usePaddingCharacter = usePaddingCharacter;
        this.encodeSize = BYTES_PER_ENCODED_BLOCK;
        this.decodeSize = this.encodeSize - 1;
    }

    /**
     * Checks if a byte value is whitespace or not. Whitespace is taken to mean: space, tab, CR, LF
     *
     * @param byteToCheck the byte to check
     * @return true if byte is whitespace, false otherwise
     */
    protected static boolean isWhiteSpace(final byte byteToCheck) {
        switch (byteToCheck) {
            case Symbol.C_SPACE:
            case '\n':
            case '\r':
            case '\t':
                return true;

            default:
                return false;
        }
    }

    /**
     * Tests a given String to see if it contains only valid characters within the alphabet. The method treats
     * whitespace and PAD as valid.
     *
     * @param base32 String to test
     * @return {@code true} if all characters in the String are valid characters in the alphabet or if the String is
     *         empty; {@code false}, otherwise
     * @see #isInAlphabet(byte[], boolean)
     */
    public static boolean isInAlphabet(final String base32) {
        return isInAlphabet(base32.getBytes(DEFAULT_CHARSET), true);
    }

    /**
     * Tests a given byte array to see if it contains only valid characters within the alphabet. The method optionally
     * treats whitespace and pad as valid.
     *
     * @param arrayOctet byte array to test
     * @param allowWSPad if {@code true}, then whitespace and PAD are also allowed
     * @return {@code true} if all bytes are valid characters in the alphabet or if the byte array is empty;
     *         {@code false}, otherwise
     */
    public static boolean isInAlphabet(final byte[] arrayOctet, final boolean allowWSPad) {
        for (final byte b : arrayOctet) {
            if (!isInAlphabet(b) && (!allowWSPad || (b != Symbol.C_EQUAL) && !isWhiteSpace(b))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the {@code octet} is in the Base32 alphabet.
     *
     * @param octet The value to test
     * @return {@code true} if the value is defined in the Base32 alphabet {@code false} otherwise.
     */
    public static boolean isInAlphabet(final byte octet) {
        return decode(octet) != -1;
    }

    /**
     * Writes a long value as a Crockford Base32 string to the given character buffer.
     *
     * @param buffer The character buffer to write to.
     * @param value  The long value to encode.
     * @param count  The number of characters to write.
     * @param offset The starting position in the buffer.
     */
    public static void writeCrockford(final char[] buffer, final long value, final int count, final int offset) {
        for (int i = 0; i < count; i++) {
            final int index = (int) ((value >>> ((count - i - 1) * BITS_PER_ENCODED_BYTE)) & MASK_5BITS);
            buffer[offset + i] = (char) ENCODE_TABLE[index];
        }
    }

    /**
     * Appends a long value as a Crockford Base32 string to the given {@link StringBuilder}.
     *
     * @param builder The {@link StringBuilder} to append to.
     * @param value   The long value to encode.
     * @param count   The number of characters to append.
     */
    public static void appendCrockford(final StringBuilder builder, final long value, final int count) {
        for (int i = count - 1; i >= 0; i--) {
            final int index = (int) ((value >>> (i * BITS_PER_ENCODED_BYTE)) & MASK_5BITS);
            builder.append(ENCODE_TABLE[index]);
        }
    }

    /**
     * Parses a Crockford Base32 string into a long value.
     *
     * @param input The Crockford Base32 string.
     * @return The parsed long value.
     */
    public static long parseCrockford(final String input) {
        Objects.requireNonNull(input, "input must not be null!");
        final int length = input.length();
        if (length > 12) {
            throw new IllegalArgumentException("input length must not exceed 12 but was " + length + "!");
        }

        long result = 0;
        for (int i = 0; i < length; i++) {
            final char current = input.charAt(i);
            final byte value = decode((byte) current);
            if (value < 0) {
                throw new IllegalArgumentException("Illegal character '" + current + "'!");
            }
            result |= ((long) value) << ((length - 1 - i) * BITS_PER_ENCODED_BYTE);
        }
        return result;
    }

    private static byte decode(final byte octet) {
        return switch (octet) {
            case '0', 'O', 'o' -> 0;
            case '1', 'I', 'i', 'L', 'l' -> 1;
            case '2' -> 2;
            case '3' -> 3;
            case '4' -> 4;
            case '5' -> 5;
            case '6' -> 6;
            case '7' -> 7;
            case '8' -> 8;
            case '9' -> 9;
            case 'A', 'a' -> 10;
            case 'B', 'b' -> 11;
            case 'C', 'c' -> 12;
            case 'D', 'd' -> 13;
            case 'E', 'e' -> 14;
            case 'F', 'f' -> 15;
            case 'G', 'g' -> 16;
            case 'H', 'h' -> 17;
            case 'J', 'j' -> 18;
            case 'K', 'k' -> 19;
            case 'M', 'm' -> 20;
            case 'N', 'n' -> 21;
            case 'P', 'p' -> 22;
            case 'Q', 'q' -> 23;
            case 'R', 'r' -> 24;
            case 'S', 's' -> 25;
            case 'T', 't' -> 26;
            case 'U', 'u', 'V', 'v' -> 27;
            case 'W', 'w' -> 28;
            case 'X', 'x' -> 29;
            case 'Y', 'y' -> 30;
            case 'Z', 'z' -> 31;
            default -> -1;
        };
    }

    /**
     * Returns the amount of buffered data available for reading.
     *
     * @return The amount of buffered data available for reading.
     */
    int available() { // package protected for access from I/O streams
        return buffer != null ? pos : 0;
    }

    /**
     * Increases our buffer by the {@link #DEFAULT_BUFFER_RESIZE_FACTOR}.
     */
    private void resizeBuffer() {
        if (buffer == null) {
            buffer = new byte[Normal._8192];
            pos = 0;
        } else {
            final byte[] b = new byte[buffer.length * DEFAULT_BUFFER_RESIZE_FACTOR];
            System.arraycopy(buffer, 0, b, 0, buffer.length);
            buffer = b;
        }
    }

    /**
     * Ensure that the buffer has room for {@code size} bytes
     *
     * @param size minimum spare space required
     */
    protected void ensureBufferSize(final int size) {
        if ((buffer == null) || (buffer.length < pos + size)) {
            resizeBuffer();
        }
    }

    /**
     * Extracts buffered data into the provided byte[] array. Returns how many bytes were actually extracted.
     *
     * @param b byte[] array to extract the buffered data into.
     * @return The number of bytes successfully extracted.
     */
    int readResults(final byte[] b) { // package protected for access from I/O streams
        if (buffer != null) {
            final int len = available();
            System.arraycopy(buffer, 0, b, 0, len);
            buffer = null; // so hasData() will return false
            return len;
        }
        return eof ? -1 : 0;
    }

    /**
     * Resets this object to its initial state.
     */
    private void reset() {
        buffer = null;
        pos = 0;
        modulus = 0;
        eof = false;
    }

    /**
     * Encodes a String into a Base32 String.
     *
     * @param pArray A String to be encoded.
     * @return A String containing Base32 characters.
     */
    public String encodeToString(final String pArray) {
        return encodeToString(pArray.getBytes(DEFAULT_CHARSET));
    }

    /**
     * Encodes a byte array into a Base32 String.
     *
     * @param pArray a byte array containing binary data
     * @return A String containing Base32 characters.
     */
    public String encodeToString(final byte[] pArray) {
        return new String(encode(pArray), DEFAULT_CHARSET);
    }

    /**
     * Decodes a Base32 String into a UTF-8 String.
     *
     * @param pArray A String containing Base32 character data
     * @return A UTF-8 decoded String
     */
    public String decodeToString(final String pArray) {
        return decodeToString(pArray.getBytes(DEFAULT_CHARSET));
    }

    /**
     * Decodes a byte array of Base32 characters into a UTF-8 String.
     *
     * @param pArray a byte array containing Base32 character data
     * @return A UTF-8 decoded String
     */
    public String decodeToString(final byte[] pArray) {
        return new String(decode(pArray), DEFAULT_CHARSET);
    }

    /**
     * Decodes a Base32 String into its binary representation.
     *
     * @param pArray A String containing Base32 character data
     * @return a byte array containing binary data
     */
    public byte[] decode(final String pArray) {
        return decode(pArray.getBytes(DEFAULT_CHARSET));
    }

    /**
     * Encodes a String into a Base32 byte array.
     *
     * @param pArray A String to be encoded.
     * @return a byte array containing Base32 character data
     */
    public byte[] encode(final String pArray) {
        return encode(pArray.getBytes(DEFAULT_CHARSET));
    }

    /**
     * Decodes a byte array of Base32 characters into its binary representation.
     *
     * @param pArray A byte array containing Base32 character data
     * @return a byte array containing binary data
     */
    public byte[] decode(final byte[] pArray) {
        reset();
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        decode(pArray, 0, pArray.length);
        decode(pArray, 0, -1); // Notify decoder of EOF.
        final byte[] result = new byte[pos];
        readResults(result);
        return result;
    }

    /**
     * Encodes a byte array into a Base32 byte array.
     *
     * @param pArray a byte array containing binary data
     * @return A byte array containing Base32 character data
     */
    public byte[] encode(final byte[] pArray) {
        reset();
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        encode(pArray, 0, pArray.length);
        encode(pArray, 0, -1); // Notify encoder of EOF.
        final byte[] buf = new byte[pos];
        readResults(buf);
        return buf;
    }

    /**
     * Calculates the amount of space needed to encode the supplied array.
     *
     * @param pArray byte[] array which will later be encoded
     * @return amount of space needed to encode the supplied array.
     */
    public long getEncodedLength(final byte[] pArray) {
        return ((pArray.length + BYTES_PER_UNENCODED_BLOCK - 1) / BYTES_PER_UNENCODED_BLOCK)
                * (long) BYTES_PER_ENCODED_BLOCK;
    }

    /**
     * Decodes a portion of the provided data.
     *
     * @param in      byte[] array of Base32 data to decode.
     * @param inPos   Position to start reading data from.
     * @param inAvail Amount of bytes available from input for decoding.
     */
    void decode(final byte[] in, int inPos, final int inAvail) {
        if (eof) {
            return;
        }
        if (inAvail < 0) {
            eof = true;
        }
        for (int i = 0; i < inAvail; i++) {
            final byte b = in[inPos++];
            if (b == Symbol.C_EQUAL) {
                eof = true;
                break;
            }
            ensureBufferSize(decodeSize);
            final int result = decode(b);
            if (result >= 0) {
                modulus = (modulus + 1) % BYTES_PER_ENCODED_BLOCK;
                bitWorkArea = (bitWorkArea << BITS_PER_ENCODED_BYTE) + result;
                if (modulus == 0) { // we can output 5 bytes
                    buffer[pos++] = (byte) ((bitWorkArea >> 32) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea >> 24) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea >> 16) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea >> 8) & MASK_8BITS);
                    buffer[pos++] = (byte) (bitWorkArea & MASK_8BITS);
                }
            }
        }

        if (eof && modulus >= 2) {
            ensureBufferSize(decodeSize);
            switch (modulus) {
                case 2 -> buffer[pos++] = (byte) ((bitWorkArea >> 2) & MASK_8BITS);
                case 3 -> buffer[pos++] = (byte) ((bitWorkArea >> 7) & MASK_8BITS);
                case 4 -> {
                    bitWorkArea = bitWorkArea >> 4;
                    buffer[pos++] = (byte) ((bitWorkArea >> 8) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea) & MASK_8BITS);
                }
                case 5 -> {
                    bitWorkArea = bitWorkArea >> 1;
                    buffer[pos++] = (byte) ((bitWorkArea >> 16) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea >> 8) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea) & MASK_8BITS);
                }
                case 6 -> {
                    bitWorkArea = bitWorkArea >> 6;
                    buffer[pos++] = (byte) ((bitWorkArea >> 16) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea >> 8) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea) & MASK_8BITS);
                }
                case 7 -> {
                    bitWorkArea = bitWorkArea >> 3;
                    buffer[pos++] = (byte) ((bitWorkArea >> 24) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea >> 16) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea >> 8) & MASK_8BITS);
                    buffer[pos++] = (byte) ((bitWorkArea) & MASK_8BITS);
                }
            }
        }
    }

    /**
     * Encodes a portion of the provided data.
     *
     * @param in      byte[] array of binary data to Base32 encode.
     * @param inPos   Position to start reading data from.
     * @param inAvail Amount of bytes available from input for encoding.
     */
    void encode(final byte[] in, int inPos, final int inAvail) {
        if (eof) {
            return;
        }
        if (inAvail < 0) {
            eof = true;
            if (0 == modulus) {
                return; // no leftovers to process
            }
            ensureBufferSize(encodeSize);
            switch (modulus) {
                case 1 -> {
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 3) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea << 2) & MASK_5BITS];
                    if (usePaddingCharacter) {
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                    }
                }
                case 2 -> {
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 11) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 6) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 1) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea << 4) & MASK_5BITS];
                    if (usePaddingCharacter) {
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                    }
                }
                case 3 -> {
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 19) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 14) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 9) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 4) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea << 1) & MASK_5BITS];
                    if (usePaddingCharacter) {
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                        buffer[pos++] = Symbol.C_EQUAL;
                    }
                }
                case 4 -> {
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 27) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 22) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 17) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 12) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 7) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 2) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea << 3) & MASK_5BITS];
                    if (usePaddingCharacter) {
                        buffer[pos++] = Symbol.C_EQUAL;
                    }
                }
            }
        } else {
            for (int i = 0; i < inAvail; i++) {
                ensureBufferSize(encodeSize);
                modulus = (modulus + 1) % BYTES_PER_UNENCODED_BLOCK;
                int b = in[inPos++];
                if (b < 0) {
                    b += 256;
                }
                bitWorkArea = (bitWorkArea << 8) + b;
                if (0 == modulus) {
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 35) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 30) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 25) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 20) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 15) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 10) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) (bitWorkArea >> 5) & MASK_5BITS];
                    buffer[pos++] = ENCODE_TABLE[(int) bitWorkArea & MASK_5BITS];
                }
            }
        }
    }

}
