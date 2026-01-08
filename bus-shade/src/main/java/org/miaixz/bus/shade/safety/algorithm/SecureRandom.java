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
package org.miaixz.bus.shade.safety.algorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serial;

import org.miaixz.bus.core.lang.Normal;

/**
 * A custom implementation of {@link java.security.SecureRandom} that uses a predefined byte array as its seed. This
 * class is useful for deterministic random number generation in testing or specific cryptographic scenarios where
 * reproducibility is required.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SecureRandom extends java.security.SecureRandom {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * The byte array containing the seed data for random number generation.
     */
    private byte[] _data;
    /**
     * The current index within the {@code _data} array, indicating the next byte to be used.
     */
    private int _index;
    /**
     * A padding value used for integer generation, ensuring proper byte alignment.
     */
    private int _intPad;

    /**
     * Constructs a {@code SecureRandom} instance with a single byte array as its seed. Integer padding is disabled by
     * default.
     *
     * @param value The byte array to use as the seed.
     */
    public SecureRandom(byte[] value) {
        this(false, new byte[][] { value });
    }

    /**
     * Constructs a {@code SecureRandom} instance with multiple byte arrays as its seed. The arrays are concatenated to
     * form the complete seed. Integer padding is disabled by default.
     *
     * @param values An array of byte arrays to use as the seed.
     */
    public SecureRandom(byte[][] values) {
        this(false, values);
    }

    /**
     * Constructs a {@code SecureRandom} instance with a single byte array as its seed, and specifies integer padding
     * behavior.
     *
     * @param intPad If {@code true}, integer padding will be applied during {@code nextInt()} generation.
     * @param value  The byte array to use as the seed.
     */
    public SecureRandom(boolean intPad, byte[] value) {
        this(intPad, new byte[][] { value });
    }

    /**
     * Constructs a {@code SecureRandom} instance with multiple byte arrays as its seed, and specifies integer padding
     * behavior. The arrays are concatenated to form the complete seed.
     *
     * @param intPad If {@code true}, integer padding will be applied during {@code nextInt()} generation.
     * @param values An array of byte arrays to use as the seed.
     * @throws IllegalArgumentException If an {@link IOException} occurs during concatenation of the seed arrays.
     */
    public SecureRandom(boolean intPad, byte[][] values) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        for (int i = 0; i != values.length; i++) {
            try {
                bOut.write(values[i]);
            } catch (IOException e) {
                throw new IllegalArgumentException("can't save value array.");
            }
        }

        _data = bOut.toByteArray();

        if (intPad) {
            _intPad = _data.length % 4;
        }
    }

    /**
     * Fills the specified byte array with random bytes from the internal seed data. The internal index is advanced by
     * the length of the byte array.
     *
     * @param bytes The byte array to fill with random bytes.
     */
    public void nextBytes(byte[] bytes) {
        System.arraycopy(_data, _index, bytes, 0, bytes.length);

        _index += bytes.length;
    }

    /**
     * Generates a seed of the specified number of bytes using the internal seed data.
     *
     * @param numBytes The number of bytes to generate for the seed.
     * @return A new byte array containing the generated seed.
     */
    public byte[] generateSeed(int numBytes) {
        byte[] bytes = new byte[numBytes];

        this.nextBytes(bytes);

        return bytes;
    }

    /**
     * Generates the next pseudo-random integer from this random number generator's sequence. This method considers the
     * {@code _intPad} for byte alignment.
     *
     * @return The next pseudo-random integer.
     */
    public int nextInt() {
        int val = 0;

        val |= nextValue() << 24;
        val |= nextValue() << Normal._16;

        if (_intPad == 2) {
            _intPad--;
        } else {
            val |= nextValue() << 8;
        }

        if (_intPad == 1) {
            _intPad--;
        } else {
            val |= nextValue();
        }

        return val;
    }

    /**
     * Generates the next pseudo-random long from this random number generator's sequence.
     *
     * @return The next pseudo-random long.
     */
    public long nextLong() {
        long val = 0;

        val |= (long) nextValue() << 56;
        val |= (long) nextValue() << 48;
        val |= (long) nextValue() << 40;
        val |= (long) nextValue() << Normal._32;
        val |= (long) nextValue() << 24;
        val |= (long) nextValue() << Normal._16;
        val |= (long) nextValue() << 8;
        val |= nextValue();

        return val;
    }

    /**
     * Checks if all the seed data has been consumed.
     *
     * @return {@code true} if the internal index has reached the end of the data array; {@code false} otherwise.
     */
    public boolean isExhausted() {
        return _index == _data.length;
    }

    /**
     * Retrieves the next byte from the internal seed data and advances the index.
     *
     * @return The next byte as an integer (0-255).
     */
    private int nextValue() {
        return _data[_index++] & 0xff;
    }

}
