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
package org.miaixz.bus.core.io;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * An immutable sequence of bytes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ByteString implements Serializable, Comparable<ByteString> {

    @Serial
    private static final long serialVersionUID = 2852277011102L;

    /**
     * A singleton empty {@code ByteString}.
     */
    public static final ByteString EMPTY = ByteString.of();
    /**
     * The raw byte data of this ByteString.
     */
    public final byte[] data;
    /**
     * Lazily computed hash code; 0 if unknown.
     */
    public transient int hashCode;
    /**
     * Lazily computed UTF-8 string representation.
     */
    public transient String utf8;

    /**
     * Constructs a new ByteString from the given byte array. The byte array is not cloned, so it should not be modified
     * after construction.
     *
     * @param data The byte array.
     */
    public ByteString(byte[] data) {
        this.data = data; // Trusted internal constructor doesn't clone data.
    }

    /**
     * Returns a new byte string containing a copy of the given bytes.
     *
     * @param data The bytes to copy.
     * @return A new {@link ByteString} instance.
     * @throws IllegalArgumentException if {@code data} is null.
     */
    public static ByteString of(byte... data) {
        if (null == data) {
            throw new IllegalArgumentException("data == null");
        }
        return new ByteString(data.clone());
    }

    /**
     * Returns a new byte string containing a copy of {@code byteCount} bytes of {@code data} starting at
     * {@code offset}.
     *
     * @param data      The source byte array.
     * @param offset    The start offset in the source array.
     * @param byteCount The number of bytes to copy.
     * @return A new {@link ByteString} instance.
     * @throws IllegalArgumentException if {@code data} is null or if the offset and count are out of bounds.
     */
    public static ByteString of(byte[] data, int offset, int byteCount) {
        if (null == data) {
            throw new IllegalArgumentException("data == null");
        }
        IoKit.checkOffsetAndCount(data.length, offset, byteCount);

        byte[] copy = new byte[byteCount];
        System.arraycopy(data, offset, copy, 0, byteCount);
        return new ByteString(copy);
    }

    /**
     * Returns a new byte string containing a copy of the bytes remaining in {@code data}.
     *
     * @param data The {@link ByteBuffer} to copy from.
     * @return A new {@link ByteString} instance.
     * @throws IllegalArgumentException if {@code data} is null.
     */
    public static ByteString of(ByteBuffer data) {
        if (null == data) {
            throw new IllegalArgumentException("data == null");
        }

        byte[] copy = new byte[data.remaining()];
        data.get(copy);
        return new ByteString(copy);
    }

    /**
     * Returns a new byte string containing the {@code UTF-8} bytes of {@code s}.
     *
     * @param s The string to encode.
     * @return A new {@link ByteString} instance encoded in UTF-8.
     * @throws IllegalArgumentException if {@code s} is null.
     */
    public static ByteString encodeUtf8(String s) {
        if (null == s) {
            throw new IllegalArgumentException("s == null");
        }
        ByteString byteString = new ByteString(s.getBytes(Charset.UTF_8));
        byteString.utf8 = s;
        return byteString;
    }

    /**
     * Returns a new byte string containing the {@code charset}-encoded bytes of {@code s}.
     *
     * @param s       The string to encode.
     * @param charset The charset to use for encoding.
     * @return A new {@link ByteString} instance.
     * @throws IllegalArgumentException if {@code s} or {@code charset} is null.
     */
    public static ByteString encodeString(String s, java.nio.charset.Charset charset) {
        if (null == s) {
            throw new IllegalArgumentException("s == null");
        }
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }
        return new ByteString(s.getBytes(charset));
    }

    /**
     * Decodes the Base64-encoded bytes and returns their value as a byte string. Returns null if {@code base64} is not
     * a Base64-encoded sequence of bytes.
     *
     * @param base64 The Base64 encoded string.
     * @return A new {@link ByteString} instance, or null if decoding fails.
     * @throws IllegalArgumentException if {@code base64} is null.
     */
    public static ByteString decodeBase64(String base64) {
        if (null == base64) {
            throw new IllegalArgumentException("base64 == null");
        }
        byte[] decoded = Base64.decode(base64);
        return null != decoded ? new ByteString(decoded) : null;
    }

    /**
     * Decodes the hex-encoded bytes and returns their value as a byte string.
     *
     * @param hex The hex encoded string.
     * @return A new {@link ByteString} instance.
     * @throws IllegalArgumentException if {@code hex} is null or has an odd length, or contains invalid hex digits.
     */
    public static ByteString decodeHex(String hex) {
        if (null == hex) {
            throw new IllegalArgumentException("hex == null");
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Unexpected hex string: " + hex);
        }

        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int d1 = decodeHexDigit(hex.charAt(i * 2)) << 4;
            int d2 = decodeHexDigit(hex.charAt(i * 2 + 1));
            result[i] = (byte) (d1 + d2);
        }
        return of(result);
    }

    /**
     * Decodes a single hexadecimal character to its integer value.
     *
     * @param c The hexadecimal character.
     * @return The integer value of the hex digit.
     * @throws IllegalArgumentException if the character is not a valid hex digit.
     */
    private static int decodeHexDigit(char c) {
        if (c >= Symbol.C_ZERO && c <= Symbol.C_NINE)
            return c - Symbol.C_ZERO;
        if (c >= 'a' && c <= 'f')
            return c - 'a' + 10;
        if (c >= 'A' && c <= 'F')
            return c - 'A' + 10;
        throw new IllegalArgumentException("Unexpected hex digit: " + c);
    }

    /**
     * Reads {@code count} bytes from {@code in} and returns the result.
     *
     * @param in        The input stream to read from.
     * @param byteCount The number of bytes to read.
     * @return A new {@link ByteString} instance containing the read bytes.
     * @throws IllegalArgumentException if {@code in} is null or {@code byteCount} is negative.
     * @throws EOFException             if {@code in} has fewer than {@code count} bytes to read.
     * @throws IOException              if an I/O error occurs.
     */
    public static ByteString read(InputStream in, int byteCount) throws IOException {
        if (null == in) {
            throw new IllegalArgumentException("in == null");
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }

        byte[] result = new byte[byteCount];
        for (int offset = 0, read; offset < byteCount; offset += read) {
            read = in.read(result, offset, byteCount - offset);
            if (read == -1)
                throw new EOFException();
        }
        return new ByteString(result);
    }

    /**
     * Converts a code point index to a character index in a string.
     *
     * @param s              The string.
     * @param codePointCount The number of code points.
     * @return The character index, or -1 if an invalid character is encountered.
     */
    static int codePointIndexToCharIndex(String s, int codePointCount) {
        for (int i = 0, j = 0, length = s.length(), c; i < length; i += Character.charCount(c)) {
            if (j == codePointCount) {
                return i;
            }
            c = s.codePointAt(i);
            if ((Character.isISOControl(c) && c != '\n' && c != '\r') || c == Buffer.REPLACEMENT_CHARACTER) {
                return -1;
            }
            j++;
        }
        return s.length();
    }

    /**
     * Constructs a new {@code String} by decoding the bytes as {@code UTF-8}.
     *
     * @return The UTF-8 string representation of this byte string.
     */
    public String utf8() {
        String result = utf8;
        // We don't care if we double-allocate in racy code.
        return null != result ? result : (utf8 = new String(data, Charset.UTF_8));
    }

    /**
     * Constructs a new {@code String} by decoding the bytes using the specified {@code charset}.
     *
     * @param charset The charset to use for decoding.
     * @return The string representation of this byte string.
     * @throws IllegalArgumentException if {@code charset} is null.
     */
    public String string(java.nio.charset.Charset charset) {
        if (null == charset) {
            throw new IllegalArgumentException("charset == null");
        }
        return new String(data, charset);
    }

    /**
     * Returns this byte string encoded as <a href="http://www.ietf.org/rfc/rfc2045.txt">Base64</a>. In violation of the
     * RFC, the returned string does not wrap lines at 76 columns.
     *
     * @return The Base64 encoded string.
     */
    public String base64() {
        return Base64.encode(data);
    }

    /**
     * Returns the 128-bit MD5 hash of this byte string.
     *
     * @return A {@link ByteString} representing the MD5 hash.
     */
    public ByteString md5() {
        return digest(Algorithm.MD5.getValue());
    }

    /**
     * Returns the 160-bit SHA-1 hash of this byte string.
     *
     * @return A {@link ByteString} representing the SHA-1 hash.
     */
    public ByteString sha1() {
        return digest(Algorithm.SHA1.getValue());
    }

    /**
     * Returns the 256-bit SHA-256 hash of this byte string.
     *
     * @return A {@link ByteString} representing the SHA-256 hash.
     */
    public ByteString sha256() {
        return digest(Algorithm.SHA256.getValue());
    }

    /**
     * Returns the 512-bit SHA-512 hash of this byte string.
     *
     * @return A {@link ByteString} representing the SHA-512 hash.
     */
    public ByteString sha512() {
        return digest(Algorithm.SHA512.getValue());
    }

    /**
     * Computes the hash of this byte string using the specified algorithm.
     *
     * @param algorithm The hashing algorithm (e.g., "MD5", "SHA-1", "SHA-256", "SHA-512").
     * @return A {@link ByteString} representing the hash.
     * @throws AssertionError if the algorithm is not found.
     */
    private ByteString digest(String algorithm) {
        try {
            return ByteString.of(MessageDigest.getInstance(algorithm).digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the 160-bit SHA-1 HMAC of this byte string.
     *
     * @param key The secret key for the HMAC.
     * @return A {@link ByteString} representing the HMAC-SHA1.
     */
    public ByteString hmacSha1(ByteString key) {
        return hmac(Algorithm.HMACSHA1.getValue(), key);
    }

    /**
     * Returns the 256-bit SHA-256 HMAC of this byte string.
     *
     * @param key The secret key for the HMAC.
     * @return A {@link ByteString} representing the HMAC-SHA256.
     */
    public ByteString hmacSha256(ByteString key) {
        return hmac(Algorithm.HMACSHA256.getValue(), key);
    }

    /**
     * Returns the 512-bit SHA-512 HMAC of this byte string.
     *
     * @param key The secret key for the HMAC.
     * @return A {@link ByteString} representing the HMAC-SHA512.
     */
    public ByteString hmacSha512(ByteString key) {
        return hmac(Algorithm.HMACSHA512.getValue(), key);
    }

    /**
     * Computes the HMAC of this byte string using the specified algorithm and key.
     *
     * @param algorithm The HMAC algorithm (e.g., "HmacSHA1", "HmacSHA256", "HmacSHA512").
     * @param key       The secret key for the HMAC.
     * @return A {@link ByteString} representing the HMAC.
     * @throws AssertionError           if the algorithm is not found.
     * @throws IllegalArgumentException if the key is invalid.
     */
    private ByteString hmac(String algorithm, ByteString key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
            return ByteString.of(mac.doFinal(data));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns this byte string encoded as <a href="http://www.ietf.org/rfc/rfc4648.txt">URL-safe Base64</a>.
     *
     * @return The URL-safe Base64 encoded string.
     */
    public String base64Url() {
        return Base64.encodeUrlSafe(data);
    }

    /**
     * Returns this byte string encoded in hexadecimal.
     *
     * @return The hexadecimal string representation.
     */
    public String hex() {
        char[] result = new char[data.length * 2];
        int c = 0;
        for (byte b : data) {
            result[c++] = Normal.DIGITS_16_LOWER[(b >> 4) & 0xf];
            result[c++] = Normal.DIGITS_16_LOWER[b & 0xf];
        }
        return new String(result);
    }

    /**
     * Returns a byte string equal to this byte string, but with the bytes 'A' through 'Z' replaced with the
     * corresponding byte in 'a' through 'z'. Returns this byte string if it contains no bytes in 'A' through 'Z'.
     *
     * @return A new {@link ByteString} with uppercase ASCII characters converted to lowercase, or this instance if no
     *         changes are needed.
     */
    public ByteString toAsciiLowercase() {
        // Search for an uppercase character. If we don't find one, return this.
        for (int i = 0; i < data.length; i++) {
            byte c = data[i];
            if (c < 'A' || c > 'Z')
                continue;

            // If we reach this point, this string is not not lowercase. Create and
            // return a new byte string.
            byte[] lowercase = data.clone();
            lowercase[i++] = (byte) (c - ('A' - 'a'));
            for (; i < lowercase.length; i++) {
                c = lowercase[i];
                if (c < 'A' || c > 'Z')
                    continue;
                lowercase[i] = (byte) (c - ('A' - 'a'));
            }
            return new ByteString(lowercase);
        }
        return this;
    }

    /**
     * Returns a byte string equal to this byte string, but with the bytes 'a' through 'z' replaced with the
     * corresponding byte in 'A' through 'Z'. Returns this byte string if it contains no bytes in 'a' through 'z'.
     *
     * @return A new {@link ByteString} with lowercase ASCII characters converted to uppercase, or this instance if no
     *         changes are needed.
     */
    public ByteString toAsciiUppercase() {
        // Search for an lowercase character. If we don't find one, return this.
        for (int i = 0; i < data.length; i++) {
            byte c = data[i];
            if (c < 'a' || c > 'z')
                continue;

            // If we reach this point, this string is not not uppercase. Create and
            // return a new byte string.
            byte[] lowercase = data.clone();
            lowercase[i++] = (byte) (c - ('a' - 'A'));
            for (; i < lowercase.length; i++) {
                c = lowercase[i];
                if (c < 'a' || c > 'z')
                    continue;
                lowercase[i] = (byte) (c - ('a' - 'A'));
            }
            return new ByteString(lowercase);
        }
        return this;
    }

    /**
     * Returns a byte string that is a substring of this byte string, beginning at the specified index until the end of
     * this string. Returns this byte string if {@code beginIndex} is 0.
     *
     * @param beginIndex The beginning index, inclusive.
     * @return A new {@link ByteString} representing the substring.
     * @throws IllegalArgumentException if {@code beginIndex} is negative.
     */
    public ByteString substring(int beginIndex) {
        return substring(beginIndex, data.length);
    }

    /**
     * Returns a byte string that is a substring of this byte string, beginning at the specified {@code beginIndex} and
     * ends at the specified {@code endIndex}. Returns this byte string if {@code beginIndex} is 0 and {@code endIndex}
     * is the length of this byte string.
     *
     * @param beginIndex The beginning index, inclusive.
     * @param endIndex   The ending index, exclusive.
     * @return A new {@link ByteString} representing the substring.
     * @throws IllegalArgumentException if {@code beginIndex} is negative, {@code endIndex} is greater than the length,
     *                                  or {@code beginIndex} is greater than {@code endIndex}.
     */
    public ByteString substring(int beginIndex, int endIndex) {
        if (beginIndex < 0)
            throw new IllegalArgumentException("beginIndex < 0");
        if (endIndex > data.length) {
            throw new IllegalArgumentException("endIndex > length(" + data.length + Symbol.PARENTHESE_RIGHT);
        }

        int subLen = endIndex - beginIndex;
        if (subLen < 0)
            throw new IllegalArgumentException("endIndex < beginIndex");

        if ((beginIndex == 0) && (endIndex == data.length)) {
            return this;
        }

        byte[] copy = new byte[subLen];
        System.arraycopy(data, beginIndex, copy, 0, subLen);
        return new ByteString(copy);
    }

    /**
     * Returns the byte at {@code pos}.
     *
     * @param pos The index of the byte to return.
     * @return The byte at the specified position.
     */
    public byte getByte(int pos) {
        return data[pos];
    }

    /**
     * Returns the number of bytes in this ByteString.
     *
     * @return The size of this byte string.
     */
    public int size() {
        return data.length;
    }

    /**
     * Returns a byte array containing a copy of the bytes in this {@code ByteString}.
     *
     * @return A new byte array with the contents of this byte string.
     */
    public byte[] toByteArray() {
        return data.clone();
    }

    /**
     * Returns the bytes of this string without a defensive copy. Do not mutate the returned array!
     *
     * @return The internal byte array.
     */
    public byte[] internalArray() {
        return data;
    }

    /**
     * Returns a {@code ByteBuffer} view of the bytes in this {@code ByteString}.
     *
     * @return A read-only {@link ByteBuffer} containing the bytes of this byte string.
     */
    public ByteBuffer asByteBuffer() {
        return ByteBuffer.wrap(data).asReadOnlyBuffer();
    }

    /**
     * Writes the contents of this byte string to {@code out}.
     *
     * @param out The output stream to write to.
     * @throws IllegalArgumentException if {@code out} is null.
     * @throws IOException              if an I/O error occurs.
     */
    public void write(OutputStream out) throws IOException {
        if (out == null)
            throw new IllegalArgumentException("out == null");
        out.write(data);
    }

    /**
     * Writes the contents of this byte string to {@code buffer}.
     *
     * @param buffer The buffer to write to.
     */
    public void write(Buffer buffer) {
        buffer.write(data, 0, data.length);
    }

    /**
     * Returns true if the bytes of this in {@code [offset..offset+byteCount)} equal the bytes of {@code other} in
     * {@code [otherOffset..otherOffset+byteCount)}. Returns false if either range is out of bounds.
     *
     * @param offset      The starting offset in this byte string.
     * @param other       The other byte string to compare with.
     * @param otherOffset The starting offset in the other byte string.
     * @param byteCount   The number of bytes to compare.
     * @return True if the ranges are equal, false otherwise.
     */
    public boolean rangeEquals(int offset, ByteString other, int otherOffset, int byteCount) {
        return other.rangeEquals(otherOffset, this.data, offset, byteCount);
    }

    /**
     * Returns true if the bytes of this in {@code [offset..offset+byteCount)} equal the bytes of {@code other} in
     * {@code [otherOffset..otherOffset+byteCount)}. Returns false if either range is out of bounds.
     *
     * @param offset      The starting offset in this byte string.
     * @param other       The other byte array to compare with.
     * @param otherOffset The starting offset in the other byte array.
     * @param byteCount   The number of bytes to compare.
     * @return True if the ranges are equal, false otherwise.
     */
    public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
        return offset >= 0 && offset <= data.length - byteCount && otherOffset >= 0
                && otherOffset <= other.length - byteCount
                && IoKit.arrayRangeEquals(data, offset, other, otherOffset, byteCount);
    }

    /**
     * Checks if this byte string starts with the given prefix.
     *
     * @param prefix The prefix to check.
     * @return True if this byte string starts with the prefix, false otherwise.
     */
    public final boolean startsWith(ByteString prefix) {
        return rangeEquals(0, prefix, 0, prefix.size());
    }

    /**
     * Checks if this byte string starts with the given byte array prefix.
     *
     * @param prefix The byte array prefix to check.
     * @return True if this byte string starts with the prefix, false otherwise.
     */
    public final boolean startsWith(byte[] prefix) {
        return rangeEquals(0, prefix, 0, prefix.length);
    }

    /**
     * Checks if this byte string ends with the given suffix.
     *
     * @param suffix The suffix to check.
     * @return True if this byte string ends with the suffix, false otherwise.
     */
    public final boolean endsWith(ByteString suffix) {
        return rangeEquals(size() - suffix.size(), suffix, 0, suffix.size());
    }

    /**
     * Checks if this byte string ends with the given byte array suffix.
     *
     * @param suffix The byte array suffix to check.
     * @return True if this byte string ends with the suffix, false otherwise.
     */
    public final boolean endsWith(byte[] suffix) {
        return rangeEquals(size() - suffix.length, suffix, 0, suffix.length);
    }

    /**
     * Returns the index within this byte string of the first occurrence of the specified byte string.
     *
     * @param other The byte string to search for.
     * @return The index of the first occurrence of the specified byte string, or -1 if not found.
     */
    public final int indexOf(ByteString other) {
        return indexOf(other.internalArray(), 0);
    }

    /**
     * Returns the index within this byte string of the first occurrence of the specified byte string, starting the
     * search at the specified index.
     *
     * @param other     The byte string to search for.
     * @param fromIndex The index from which to start the search.
     * @return The index of the first occurrence of the specified byte string, or -1 if not found.
     */
    public final int indexOf(ByteString other, int fromIndex) {
        return indexOf(other.internalArray(), fromIndex);
    }

    /**
     * Returns the index within this byte string of the first occurrence of the specified byte array.
     *
     * @param other The byte array to search for.
     * @return The index of the first occurrence of the specified byte array, or -1 if not found.
     */
    public final int indexOf(byte[] other) {
        return indexOf(other, 0);
    }

    /**
     * Returns the index within this byte string of the first occurrence of the specified byte array, starting the
     * search at the specified index.
     *
     * @param other     The byte array to search for.
     * @param fromIndex The index from which to start the search.
     * @return The index of the first occurrence of the specified byte array, or -1 if not found.
     */
    public int indexOf(byte[] other, int fromIndex) {
        fromIndex = Math.max(fromIndex, 0);
        for (int i = fromIndex, limit = data.length - other.length; i <= limit; i++) {
            if (IoKit.arrayRangeEquals(data, i, other, 0, other.length)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index within this byte string of the last occurrence of the specified byte string.
     *
     * @param other The byte string to search for.
     * @return The index of the last occurrence of the specified byte string, or -1 if not found.
     */
    public final int lastIndexOf(ByteString other) {
        return lastIndexOf(other.internalArray(), size());
    }

    /**
     * Returns the index within this byte string of the last occurrence of the specified byte string, searching backward
     * from the specified index.
     *
     * @param other     The byte string to search for.
     * @param fromIndex The index from which to start the search backward.
     * @return The index of the last occurrence of the specified byte string, or -1 if not found.
     */
    public final int lastIndexOf(ByteString other, int fromIndex) {
        return lastIndexOf(other.internalArray(), fromIndex);
    }

    /**
     * Returns the index within this byte string of the last occurrence of the specified byte array.
     *
     * @param other The byte array to search for.
     * @return The index of the last occurrence of the specified byte array, or -1 if not found.
     */
    public final int lastIndexOf(byte[] other) {
        return lastIndexOf(other, size());
    }

    /**
     * Returns the index within this byte string of the last occurrence of the specified byte array, searching backward
     * from the specified index.
     *
     * @param other     The byte array to search for.
     * @param fromIndex The index from which to start the search backward.
     * @return The index of the last occurrence of the specified byte array, or -1 if not found.
     */
    public int lastIndexOf(byte[] other, int fromIndex) {
        fromIndex = Math.min(fromIndex, data.length - other.length);
        for (int i = fromIndex; i >= 0; i--) {
            if (IoKit.arrayRangeEquals(data, i, other, 0, other.length)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Compares this {@code ByteString} to the specified object. The result is {@code true} if and only if the argument
     * is not {@code null} and is a {@code ByteString} object that represents the same sequence of bytes as this object.
     *
     * @param o The object to compare this {@code ByteString} against.
     * @return {@code true} if the given object represents a {@code ByteString} equivalent to this byte string,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        return o instanceof ByteString && ((ByteString) o).size() == data.length
                && ((ByteString) o).rangeEquals(0, data, 0, data.length);
    }

    /**
     * Returns a hash code for this byte string. The hash code is computed lazily.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = hashCode;
        return result != 0 ? result : (hashCode = Arrays.hashCode(data));
    }

    /**
     * Compares this byte string to another byte string lexicographically.
     *
     * @param byteString The other byte string to compare to.
     * @return A negative integer, zero, or a positive integer as this byte string is less than, equal to, or greater
     *         than the specified byte string.
     */
    @Override
    public int compareTo(ByteString byteString) {
        int sizeA = size();
        int sizeB = byteString.size();
        for (int i = 0, size = Math.min(sizeA, sizeB); i < size; i++) {
            int byteA = getByte(i) & 0xff;
            int byteB = byteString.getByte(i) & 0xff;
            if (byteA == byteB)
                continue;
            return byteA < byteB ? -1 : 1;
        }
        if (sizeA == sizeB)
            return 0;
        return sizeA < sizeB ? -1 : 1;
    }

    /**
     * Reads the object from an {@link ObjectInputStream}.
     *
     * @param in The input stream.
     * @throws IOException    if an I/O error occurs.
     * @throws AssertionError if reflection fails.
     */
    private void readObject(ObjectInputStream in) throws IOException {
        int dataLength = in.readInt();
        ByteString byteString = ByteString.read(in, dataLength);
        try {
            Field field = ByteString.class.getDeclaredField(Consts.DATA);
            field.setAccessible(true);
            field.set(this, byteString.data);
        } catch (NoSuchFieldException e) {
            throw new AssertionError();
        } catch (IllegalAccessException e) {
            throw new AssertionError();
        }
    }

    /**
     * Writes the object to an {@link ObjectOutputStream}.
     *
     * @param out The output stream.
     * @throws IOException if an I/O error occurs.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }

}
